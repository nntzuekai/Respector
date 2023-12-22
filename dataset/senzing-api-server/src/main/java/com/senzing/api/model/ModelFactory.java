package com.senzing.api.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.NameTransformer;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

/**
 * Provides a factory for creating model instances.
 *
 * @param <T> The model class.
 */
@SuppressWarnings("unchecked")
public abstract class ModelFactory<T, P extends ModelProvider<T>>
  extends JsonDeserializer<T>
{
  private static InvocationHandler DUMMY_HANDLER = new InvocationHandler() {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
      throw new UnsupportedOperationException();
    }
  };

  /**
   * The factory state which is kept on a per-class basis (i.e.: globally).
   */
  private static class FactoryState<T, P extends ModelProvider<T>> {
    /**
     * The interface class for the instances produced by this builder.
     */
    private Class<T> interfaceClass;

    /**
     * The current provider for this factory.
     */
    private P currentProvider;

    /**
     * The original default provider.
     */
    private P defaultProvider;
  }

  /**
   * The map of factory state instances (per class).
   */
  private static final
    Map<Class<? extends ModelFactory>, FactoryState> STATE_MAP
      = new LinkedHashMap<>();

  /**
   * The {@link FactoryState} for this instance.
   */
  private final FactoryState<T, P> state;

  /**
   * Constructs with the specified interface class.
   *
   * @param defaultProvider The default provider for this factory.
   */
  protected ModelFactory(P defaultProvider)
  {
    Objects.requireNonNull(defaultProvider,
                           "The default provider cannot be null");

    AbstractModelProvider.validateClasses(defaultProvider.getInterfaceClass(),
                                          defaultProvider.getRuntimeClass());

    // set the interface and runtime classes
    synchronized (STATE_MAP) {
      FactoryState<T,P> state = STATE_MAP.get(this.getClass());
      // check if this is the first/master instance
      if (state == null) {
        state = new FactoryState<T,P>();
        state.interfaceClass  = defaultProvider.getInterfaceClass();
        state.currentProvider = defaultProvider;
        state.defaultProvider = defaultProvider;
        STATE_MAP.put(this.getClass(), state);

      } else {
        // verify that nobody is trying to change the interface class
        if (defaultProvider.getInterfaceClass() != state.interfaceClass) {
          throw new IllegalStateException(
              "The master state already exists and the interface class of the "
              + "specified default provider does not match.  expected=[ "
              + state.interfaceClass + " ], specified=[ "
              + defaultProvider.getInterfaceClass() + " ]");
        }

        // verify the default provider
        if (defaultProvider.getRuntimeClass()
            != state.defaultProvider.getRuntimeClass())
        {
          throw new IllegalStateException(
              "The master state already exists and the runtime class for the "
              + "original default provider does not match the runtime class of "
              + "the specified default provider.  expected=[ "
                  + state.defaultProvider.getRuntimeClass() + " ], specified=[ "
                  + defaultProvider.getRuntimeClass() + " ]");
        }
      }
      this.state = state;
    }
  }

  /**
   * Default constructor which simply inherits an existing singleton
   * "master" state.
   */
  protected ModelFactory(Class<T> interfaceClass)
  {
    Objects.requireNonNull(
        interfaceClass, "The interface class cannot be null");
    // check if the master instance already created the shared state
    FactoryState<T,P> state = null;
    // synchronize
    synchronized (STATE_MAP) {
      // get the state instance
      state = STATE_MAP.get(this.getClass());

      // check if it does not exist
      if (state == null) {
        // try to force initialization of the host class (which should create
        // the master instance if it conforms to the model factory pattern)
        try {
          // create a dummy proxy instance to force initialization
          Class[] interfaces = { interfaceClass };
          Object proxy = Proxy.newProxyInstance(
              this.getClass().getClassLoader(), interfaces, DUMMY_HANDLER);

          // use the proxy instance to ensure it is not optimized out
          if (proxy == null) {
            throw new IllegalStateException(
                "Failed to create proxy instance for " + interfaceClass);
          }
        } catch (Exception e) {
          // ignore
        }
      }
    }

    // check if the state is null at this point (only should be if the
    // initialization had to be forced above)
    if (state == null) {
      synchronized (STATE_MAP) {
        state = STATE_MAP.get(this.getClass());
        // check if this is the first/master instance
        if (state == null) {
          throw new IllegalStateException(
              "The master state does not yet exist.  Cannot construct: "
                  + this.getClass().getName());
        }
      }
    }

    // validate the interface class matches
    if (interfaceClass != state.interfaceClass) {
      throw new IllegalStateException(
          "The specified interface class does not match that of the shared "
          + "state.  expected=[ " + state.interfaceClass + " ], specified=[ "
          + interfaceClass + " ]");
    }

    // set the state
    this.state = state;
  }

  /**
   * Gets the interface class provided.
   *
   * @return The interface class provided.
   */
  public final Class<T> getInterfaceClass() {
    return this.getProvider().getInterfaceClass();
  }

  /**
   * Gets the runtime class instantiated that implements the interface class.
   *
   * @return The runtime class instantiated that implements the runtime class.
   */
  public final Class<? extends T> getRuntimeClass() {
    return this.getProvider().getRuntimeClass();
  }

  /**
   * Returns the currently installed {@link ModelProvider} for this instance.
   *
   * @return The currently installed {@link ModelProvider} for this instance.
   */
  public final P getProvider() {
    synchronized (this.state) {
      return this.state.currentProvider;
    }
  }

  /**
   * Installs a new {@link ModelProvider} instance for this factory.
   *
   * @param provider The {@link ModelProvider} to install.
   *
   * @throws IllegalArgumentException If the specified {@link ModelProvider}
   *                                  does not have the correct interface class.
   */
  public final void installProvider(P provider) {
    Objects.requireNonNull(provider, "The provider cannot be null");
    AbstractModelProvider.validateClasses(provider.getInterfaceClass(),
                                          provider.getRuntimeClass());
    synchronized (this.state) {
      this.state.currentProvider = provider;
    }
  }

  /**
   * Method to determine the {@link JsonDeserializer} for the current
   * {@linkplain #getRuntimeClass() runtime class}.
   *
   * @param context The {@link DeserializationContext} to use for finding the
   *                deserializer.
   * @return The {@link JsonDeserializer} to use.
   *
   * @throws JsonMappingException If a JSON mapping falure occurs.
   */
  protected JsonDeserializer<Object> getRuntimeDeserializer(
      DeserializationContext context)
      throws JsonMappingException
  {
    Class runtimeClass = this.getRuntimeClass();
    JavaType runtimeType = context.constructType(runtimeClass);
    JsonDeserializer result = context.findRootValueDeserializer(runtimeType);

    // handle the case where the implementation does not override the JSON
    // deserializer and attempt to find the bean deserializer to use
    if (result.getClass() == this.getClass()) {
      throw new IllegalStateException(
          "Runtime implementation class MUST override the deserializer "
          + "specified in the interface class via @JsonDeserialize annotation "
          + "or by some other means: " + runtimeClass);
    }

    // return the deserializer
    return result;
  }

  /**
   * Overridden to delegate to the deserializer returned by {@link
   * #getRuntimeDeserializer(DeserializationContext)}.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public T deserialize(JsonParser parser, DeserializationContext context)
      throws JsonProcessingException, IOException
  {
    JsonDeserializer<Object> rtDeserializer
        = this.getRuntimeDeserializer(context);
    return (T) rtDeserializer.deserialize(parser, context);
  }

  /**
   * Overridden to delegate to the deserializer returned by {@link
   * #getRuntimeDeserializer(DeserializationContext)}.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public T deserialize(JsonParser             parser,
                       DeserializationContext context,
                       T                      intoValue)
      throws JsonProcessingException, IOException
  {
    JsonDeserializer<Object> rtDeserializer
        = this.getRuntimeDeserializer(context);
    return (T) rtDeserializer.deserialize(parser, context, intoValue);
  }

  /**
   * Overridden to delegate to the deserializer returned by {@link
   * #getRuntimeDeserializer(DeserializationContext)}.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public T deserializeWithType(JsonParser             parser,
                               DeserializationContext context,
                               TypeDeserializer       typeDeserializer)
      throws JsonProcessingException, IOException
  {
    JsonDeserializer<Object> rtDeserializer
        = this.getRuntimeDeserializer(context);
    return (T) rtDeserializer.deserializeWithType(
        parser, context, typeDeserializer);
  }

  /**
   * Overridden to return a reference to this instance.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public JsonDeserializer<T> unwrappingDeserializer(NameTransformer unwrapper)
  {
    return this;
  }

  /**
   * Overridden to return a reference to this instance.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public JsonDeserializer<?> replaceDelegatee(JsonDeserializer<?> delegatee)
  {
    return this;
  }

  /**
   * Overridden to return the result from {@link #getInterfaceClass()}.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public Class<?> handledType() {
    return this.getInterfaceClass();
  }

  /**
   * Overridden to return <tt>false</tt>.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public boolean isCachable() {
    return false;
  }

  /**
   * Overridden to return a reference to this instance.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public JsonDeserializer<?> getDelegatee() {
    return this;
  }

  /**
   * Overridden to determine the property names for the current runtime class
   * and return them.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public Collection<Object> getKnownPropertyNames() {
    try {
      Class<? extends T> runtimeClass = this.getRuntimeClass();
      BeanInfo beanInfo = Introspector.getBeanInfo(runtimeClass);
      Set<Object> result = new LinkedHashSet<>();
      for (PropertyDescriptor propDesc : beanInfo.getPropertyDescriptors()) {
        result.add(propDesc.getName());
      }
      return result;

    } catch (IntrospectionException e) {
      e.printStackTrace();
      return Collections.emptySet();
    }
  }

  @Override
  public T getNullValue(DeserializationContext context)
      throws JsonMappingException
  {
    JsonDeserializer<Object> rtDeserializer
        = this.getRuntimeDeserializer(context);
    return (T) rtDeserializer.getNullValue(context);
  }
}
