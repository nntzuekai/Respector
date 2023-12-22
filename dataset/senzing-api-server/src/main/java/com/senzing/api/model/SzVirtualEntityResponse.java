package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzVirtualEntityResponseImpl;

/**
 * A response object that contains the {@link SzVirtualEntityData} for a
 * "how entity" operation.
 *
 */
@JsonDeserialize(using= SzVirtualEntityResponse.Factory.class)
public interface SzVirtualEntityResponse extends SzResponseWithRawData {
  /**
   * Returns the data associated with this response which is an
   * {@link SzVirtualEntityData}.
   *
   * @return The data associated with this response.
   */
  SzVirtualEntityData getData();

  /**
   * Sets the data associated with this response with an {@link
   * SzVirtualEntityData}.
   *
   * @param data The {@link SzVirtualEntityData} describing the virtual entity.
   */
  void setData(SzVirtualEntityData data);

    /**
   * A {@link ModelProvider} for instances of {@link SzVirtualEntityResponse}.
   */
  interface Provider extends ModelProvider<SzVirtualEntityResponse> {
    /**
     * Creates an instance of {@link SzVirtualEntityResponse} with the
     * specified {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzVirtualEntityResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzVirtualEntityResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and {@link
     * SzVirtualEntityData} describing the virtual entity.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzVirtualEntityData} describing the virtual
     *             entity.
     */
    SzVirtualEntityResponse create(SzMeta               meta,
                                   SzLinks              links,
                                   SzVirtualEntityData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzVirtualEntityResponse} that produces instances of
   * {@link SzVirtualEntityResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzVirtualEntityResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzVirtualEntityResponse.class, SzVirtualEntityResponseImpl.class);
    }

    /**
     * Creates an instance of {@link SzVirtualEntityResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzVirtualEntityResponse create(SzMeta meta, SzLinks links) {
      return new SzVirtualEntityResponseImpl(meta, links);
    }

    /**
     * Creates an instance of {@link SzVirtualEntityResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and {@link SzVirtualEntityData}
     * describing the virtual entity.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzVirtualEntityData} describing the virtual
     *             entity.
     */
    public SzVirtualEntityResponse create(SzMeta                meta,
                                          SzLinks               links,
                                          SzVirtualEntityData   data)
    {
      return new SzVirtualEntityResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzEntityPathResponse}.
   */
  class Factory extends ModelFactory<SzVirtualEntityResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzVirtualEntityResponse.class);
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
     * Creates an instance of {@link SzVirtualEntityResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzVirtualEntityResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzVirtualEntityResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and {@link
     * SzVirtualEntityData} describing the virtual entity.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzVirtualEntityData} describing the virtual
     *             entity.
     */
    public SzVirtualEntityResponse create(SzMeta              meta,
                                          SzLinks             links,
                                          SzVirtualEntityData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
