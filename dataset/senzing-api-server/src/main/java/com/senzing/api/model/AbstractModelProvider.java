package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

/**
 * The abstract builder class that other builders can extend.
 *
 * @param <T> The model class produced by this instance.
 */
public abstract class AbstractModelProvider<T> implements ModelProvider<T> {
  /**
   * The interface class for the instances produced by this builder.
   */
  private Class<T> interfaceClass;

  /**
   * The runtime class produced by this instance.
   */
  private Class<? extends T> runtimeClass;

  /**
   * Constructs with the specified interface class.
   *
   * @param interfaceClass The interface class produced by the builder.
   *
   * @param runtimeClass The runtime class produced by the builder.
   */
  public AbstractModelProvider(Class<T>           interfaceClass,
                                  Class<? extends T> runtimeClass)
  {
    validateClasses(interfaceClass, runtimeClass);

    // set the interface and runtime classes
    this.interfaceClass = interfaceClass;
    this.runtimeClass   = runtimeClass;
  }

  /**
   * Validates the specified interface and runtime class.  The interface class
   * must be an interface.  The runtime class must implement the interface class
   * and be a concrete class and have a default constructor (whether public or
   * private) so the object can be deserialized from JSON.
   *
   * @param interfaceClass The interface class produced by the builder.
   *
   * @param runtimeClass The runtime class produced by the builder.
   */
  public static <T> void validateClasses(Class<T>           interfaceClass,
                                         Class<? extends T> runtimeClass)
  {
    try {
      Objects.requireNonNull(interfaceClass,
                             "The interface class cannot be null");
      Objects.requireNonNull(runtimeClass,
                             "The runtime class cannot be null");

      // ensure the interface class is an interface
      if (!interfaceClass.isInterface()) {
        throw new IllegalArgumentException(
            "The specified interface class must be an interface: "
                + interfaceClass);
      }

      // ensures the runtime class extends the interface class
      if (!interfaceClass.isAssignableFrom(runtimeClass)) {
        throw new IllegalArgumentException(
            "The runtime class must implement the interface class.  "
                + "runtimeClass=[ " + runtimeClass.getName()
                + " ], interfaceClass=[ " + interfaceClass.getName() + " ]");
      }

      // check the runtime class
      if (!runtimeClass.isAnnotationPresent(JsonDeserialize.class)) {
        throw new IllegalArgumentException(
            "The runtime class (" + runtimeClass + ") must be annotated with "
                + "@JsonDeserialize to override the @JsonDeserialize specified in "
                + "the interface class (" + interfaceClass + ")");
      }

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Returns the interface class with which this instance was constructed.
   *
   * @return The interface class with which this instance was constructed.
   */
  @Override
  public Class<T> getInterfaceClass() {
    return this.interfaceClass;
  }

  /**
   * Returns the runtime class with which this instance was constructed.
   *
   * @return The runtime class with which this instance was constructed.
   */
  @Override
  public Class<? extends T> getRuntimeClass() {
    return this.runtimeClass;
  }
}
