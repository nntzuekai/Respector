package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzAttributeTypeResponseDataImpl;
import com.senzing.api.model.impl.SzAttributeTypeResponseImpl;
import com.senzing.api.model.impl.SzAttributeTypesResponseDataImpl;

/**
 * A response object that contains attribute type data.
 *
 */
@JsonDeserialize(using= SzAttributeTypeResponseData.Factory.class)
public interface SzAttributeTypeResponseData {
  /**
   * Gets the {@link SzAttributeType} describing the attribute type.
   *
   * @return The {@link SzAttributeType} describing the attribute type.
   */
  SzAttributeType getAttributeType();

  /**
   * Sets the {@link SzAttributeType} that describes the attribute type for
   * this instance.
   *
   * @param attributeType The {@link SzAttributeType} describing the attribute
   *                      type.
   */
  void setAttributeType(SzAttributeType attributeType);

  /**
   * A {@link ModelProvider} for instances of {@link SzAttributeTypeResponseData}.
   */
  interface Provider extends ModelProvider<SzAttributeTypeResponseData> {
    /**
     * Creates an instance of {@link SzAttributeTypeResponseData} that has no
     * attribute type.
     *
     * @return The created instance of {@link SzAttributeTypeResponseData}.
     */
    SzAttributeTypeResponseData create();

    /**
     * Creates an instance of {@link SzAttributeTypeResponseData} with the
     * specified {@link SzAttributeType} describing the attribute type.
     *
     * @param data The {@link SzAttributeType} describing the attribute type.
     */
    SzAttributeTypeResponseData create(SzAttributeType data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzAttributeTypeResponseData} that produces instances of
   * {@link SzAttributeTypeResponseImpl}.
   */
  class DefaultProvider
      extends AbstractModelProvider<SzAttributeTypeResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzAttributeTypeResponseData.class,
            SzAttributeTypeResponseDataImpl.class);
    }

    @Override
    public SzAttributeTypeResponseData create() {
      return new SzAttributeTypeResponseDataImpl();
    }

    @Override
    public SzAttributeTypeResponseData create(SzAttributeType data) {
      return new SzAttributeTypeResponseDataImpl(data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzAttributeTypeResponseData}.
   */
  class Factory extends ModelFactory<SzAttributeTypeResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzAttributeTypeResponseData.class);
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
     * Creates an instance of {@link SzAttributeTypeResponseData} that has no
     * attribute type.
     *
     * @return The created instance of {@link SzAttributeTypeResponseData}.
     */
    public SzAttributeTypeResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance of {@link SzAttributeTypeResponseData} with the
     * specified {@link SzAttributeType} describing the attribute type.
     *
     * @param data The {@link SzAttributeType} describing the attribute type.
     */
    public SzAttributeTypeResponseData create(SzAttributeType data) {
      return this.getProvider().create(data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
