package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityResponseImpl;

/**
 * A response object that contains entity data.
 */
@JsonDeserialize(using=SzEntityResponse.Factory.class)
public interface SzEntityResponse extends SzResponseWithRawData {
  /**
   * Returns the data associated with this response which is an
   * {@link SzEntityData}.
   *
   * @return The data associated with this response.
   */
  SzEntityData getData();

  /**
   * Sets the data associated with this response with an {@link SzEntityData}.
   *
   * @param data The {@link SzEntityData} describing the entity.
   */
  void setData(SzEntityData data);

    /**
   * A {@link ModelProvider} for instances of {@link SzEntityResponse}.
   */
  interface Provider extends ModelProvider<SzEntityResponse> {
    /**
     * Creates an instance of {@link SzEntityResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzEntityResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzEntityResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzEntityData} describing the
     * entity.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzEntityData} describing the entity.
     */
    SzEntityResponse create(SzMeta         meta,
                            SzLinks        links,
                            SzEntityData   data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityResponse} that produces instances of
   * {@link SzEntityResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityResponse.class, SzEntityResponseImpl.class);
    }

    /**
     * Creates an instance of {@link SzEntityResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzEntityResponse create(SzMeta meta, SzLinks links) {
      return new SzEntityResponseImpl(meta, links);
    }

    /**
     * Creates an instance of {@link SzEntityResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzEntityData} describing the
     * entity.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzEntityData} describing the entity.
     */
    public SzEntityResponse create(SzMeta         meta,
                                   SzLinks        links,
                                   SzEntityData   data)
    {
      return new SzEntityResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzEntityPathResponse}.
   */
  class Factory extends ModelFactory<SzEntityResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityResponse.class);
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
     * Creates an instance of {@link SzEntityResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzEntityResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzEntityResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzEntityData} describing the
     * entity.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzEntityData} describing the entity.
     */
    public SzEntityResponse create(SzMeta         meta,
                                   SzLinks        links,
                                   SzEntityData   data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
