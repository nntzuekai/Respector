package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityPathResponseImpl;

/**
 * A response object that contains entity path data.
 */
@JsonDeserialize(using=SzEntityPathResponse.Factory.class)
public interface SzEntityPathResponse extends SzResponseWithRawData {
  /**
   * Returns the data associated with this response which is an
   * {@link SzEntityPathData}.
   *
   * @return The data associated with this response.
   */
  SzEntityPathData getData();

  /**
   * Sets the data associated with this response with an
   * {@link SzEntityPathData}.
   *
   * @param data The {@link SzEntityPathData} describing the record.
   */
  void setData(SzEntityPathData data);

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityPathResponse}.
   */
  interface Provider extends ModelProvider<SzEntityPathResponse> {
    /**
     * Creates an instance of {@link SzEntityPathResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzEntityPathResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzEntityPathResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzEntityPathData} describing
     * the entity path.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzEntityPathData} describing the entity path.
     */
    SzEntityPathResponse create(SzMeta            meta,
                                SzLinks           links,
                                SzEntityPathData  data);

  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityNetworkResponse} that produces instances of
   * {@link SzEntityPathResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityPathResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityPathResponse.class, SzEntityPathResponseImpl.class);
    }

    /**
     * Creates an instance of {@link SzEntityPathResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzEntityPathResponse create(SzMeta meta, SzLinks links) {
      return new SzEntityPathResponseImpl(meta, links);
    }

    /**
     * Creates an instance of {@link SzEntityPathResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzEntityPathData} describing
     * the entity path.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzEntityPathData} describing the entity path.
     */
    public SzEntityPathResponse create(SzMeta           meta,
                                       SzLinks          links,
                                       SzEntityPathData data)
    {
      return new SzEntityPathResponseImpl(meta, links, data);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzEntityPathResponse}.
   */
  class Factory extends ModelFactory<SzEntityPathResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityPathResponse.class);
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
     * Creates an instance of {@link SzEntityPathResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzEntityPathResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzEntityPathResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzEntityPathData} describing
     * the entity path.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzEntityPathData} describing the entity path.
     */
    public SzEntityPathResponse create(SzMeta           meta,
                                       SzLinks          links,
                                       SzEntityPathData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
