package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzHowEntityResponseImpl;

/**
 * A response object that contains the virtual entity data.
 *
 */
@JsonDeserialize(using=SzHowEntityResponse.Factory.class)
public interface SzHowEntityResponse extends SzResponseWithRawData {
  /**
   * Returns the data associated with this response which is an
   * {@link SzHowEntityResult}.
   *
   * @return The data associated with this response.
   */
  SzHowEntityResult getData();

  /**
   * Sets the data associated with this response with an {@link
   * SzHowEntityResult}.
   *
   * @param data The {@link SzHowEntityResult} describing how the entity
   *             resolved.
   */
  void setData(SzHowEntityResult data);

    /**
   * A {@link ModelProvider} for instances of {@link SzHowEntityResponse}.
   */
  interface Provider extends ModelProvider<SzHowEntityResponse> {
    /**
     * Creates an instance of {@link SzHowEntityResponse} with the
     * specified {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzHowEntityResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzHowEntityResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and {@link SzHowEntityResult}
     * describing how the entity resolved.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzHowEntityResult} describing how the entity
     *             resolved.
     */
    SzHowEntityResponse create(SzMeta             meta,
                               SzLinks            links,
                               SzHowEntityResult  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzHowEntityResponse} that produces instances of
   * {@link SzHowEntityResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzHowEntityResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzHowEntityResponse.class, SzHowEntityResponseImpl.class);
    }

    /**
     * Creates an instance of {@link SzHowEntityResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzHowEntityResponse create(SzMeta meta, SzLinks links) {
      return new SzHowEntityResponseImpl(meta, links);
    }

    /**
     * Creates an instance of {@link SzHowEntityResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and {@link SzHowEntityResult}
     * describing how the entity resolved.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzHowEntityResult} describing how the entity
     *             resolved.
     */
    public SzHowEntityResponse create(SzMeta            meta,
                                      SzLinks           links,
                                      SzHowEntityResult data)
    {
      return new SzHowEntityResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzEntityPathResponse}.
   */
  class Factory extends ModelFactory<SzHowEntityResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzHowEntityResponse.class);
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
     * Creates an instance of {@link SzHowEntityResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzHowEntityResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzHowEntityResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and {@link
     * SzHowEntityResult} describing how the entity resolved.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzHowEntityResult} describing how the entity
     *             resolved.
     */
    public SzHowEntityResponse create(SzMeta            meta,
                                      SzLinks           links,
                                      SzHowEntityResult data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
