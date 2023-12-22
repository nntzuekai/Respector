package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzAttributeTypesResponseDataImpl;
import com.senzing.api.model.impl.SzAttributeTypesResponseImpl;

import java.util.Collection;
import java.util.List;

/**
 * The response containing a list of attribute types.  Typically this is the
 * list of all configured attribute types (excluding the "internal" ones).
 *
 */
@JsonDeserialize(using= SzAttributeTypesResponseData.Factory.class)
public interface SzAttributeTypesResponseData {
  /**
   * Gets the unmodifiable {@link List} of {@link SzAttributeType} instances.
   *
   * @return The unmodifiable {@link List} of {@link SzAttributeType}
   *         instances.
   */
  List<SzAttributeType> getAttributeTypes();

  /**
   * Adds the specified {@link SzAttributeType}.
   *
   * @param attributeType The {@link SzAttributeType} to add.
   */
  void addAttributeType(SzAttributeType attributeType);

  /**
   * Sets the attribute types for this instance to the specified {@link
   * Collection} of {@link SzAttributeType} instances.
   *
   * @param attrTypes The {@link SzAttributeType} instances to set for this
   *                  instance.
   */
  void setAttributeTypes(Collection<? extends SzAttributeType> attrTypes);

  /**
   * A {@link ModelProvider} for instances of {@link SzAttributeTypesResponseData}.
   */
  interface Provider extends ModelProvider<SzAttributeTypesResponseData> {
    /**
     * Creates an instance with no attribute types.
     *
     * @return The created instance of {@link SzAttributeTypesResponseData}.
     */
    SzAttributeTypesResponseData create();

    /**
     * Creates an instance with the specified attribute types.
     *
     * @param attributeTypes The {@link Collection} of {@link SzAttributeType}
     *                       instances for this instance.
     *
     * @return The created instance of {@link SzAttributeTypesResponseData}.
     */
    SzAttributeTypesResponseData create(
        Collection<? extends SzAttributeType> attributeTypes);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzAttributeTypesResponseData} that produces instances of
   * {@link SzAttributeTypesResponseImpl}.
   */
  class DefaultProvider
      extends AbstractModelProvider<SzAttributeTypesResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzAttributeTypesResponseData.class,
            SzAttributeTypesResponseDataImpl.class);
    }

    @Override
    public SzAttributeTypesResponseData create() {
      return new SzAttributeTypesResponseDataImpl();
    }

    @Override
    public SzAttributeTypesResponseData create(
        Collection<? extends SzAttributeType> attributeTypes)
    {
      return new SzAttributeTypesResponseDataImpl(attributeTypes);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzAttributeTypesResponseData}.
   */
  class Factory extends ModelFactory<SzAttributeTypesResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzAttributeTypesResponseData.class);
    }

    /**
     * Constructs with the default provider.  This constructor is private and
     * is used for the master singleton instance.
     * @param defaultProvider The default provider.
     */
    private Factory(Provider defaultProvider) {
      super(defaultProvider);
    }

    /**
     * Creates an instance with no attribute types.
     *
     * @return The created instance of {@link SzAttributeTypesResponseData}.
     */
    public SzAttributeTypesResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance with the specified attribute types.
     *
     * @param attributeTypes The {@link Collection} of {@link SzAttributeType}
     *                       instances for this instance.
     *
     * @return The created instance of {@link SzAttributeTypesResponseData}.
     */
    public SzAttributeTypesResponseData create(
        Collection<? extends SzAttributeType> attributeTypes)
    {
      return this.getProvider().create(attributeTypes);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
