package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzBulkLoadResponseImpl;

/**
 * A response object that contains entity record data.
 *
 */
@JsonDeserialize(using=SzBulkLoadResponse.Factory.class)
public interface SzBulkLoadResponse extends SzBasicResponse {
  /**
   * Returns the data associated with this response which is an
   * {@link SzBulkLoadResult}.
   *
   * @return The data associated with this response.
   */
  SzBulkLoadResult getData();

  /**
   * Sets the data associated with this response with an {@link
   * SzBulkLoadResult}.
   *
   * @param bulkLoadResult The {@link SzBulkLoadResult} describing the record.
   */
  void setData(SzBulkLoadResult bulkLoadResult);

  /**
   * A {@link ModelProvider} for instances of {@link SzBulkLoadResponse}.
   */
  interface Provider extends ModelProvider<SzBulkLoadResponse> {
      /**
       * Creates an instance of {@link SzBulkLoadResponse} with the
       * specified {@link SzMeta} and {@link SzLinks}.
       *
       * @param meta The response meta data.
       *
       * @param links The links for the response.
       */
      SzBulkLoadResponse create(SzMeta meta, SzLinks links);

      /**
       * Creates an instance of {@link SzBulkLoadResponse} with the
       * specified {@link SzMeta}, {@link SzLinks} and the specified {@link
       * SzBulkLoadResult} describing the bulk records..
       *
       * @param meta The response meta data.
       *
       * @param links The links for the response.
       *
       * @param loadResult The {@link SzBulkLoadResult} describing the results
       *                   of the bulk load.
       */
      SzBulkLoadResponse create(SzMeta            meta,
                                SzLinks           links,
                                SzBulkLoadResult  loadResult);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzBulkLoadResponse} that produces instances of
   * {@link SzBulkLoadResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzBulkLoadResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzBulkLoadResponse.class, SzBulkLoadResponseImpl.class);
    }

    @Override
    public SzBulkLoadResponse create(SzMeta meta, SzLinks links){
      return new SzBulkLoadResponseImpl(meta, links);
    }

    @Override
    public SzBulkLoadResponse create(SzMeta           meta,
                                     SzLinks          links,
                                     SzBulkLoadResult loadResult)
    {
      return new SzBulkLoadResponseImpl(meta, links, loadResult);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzBulkLoadResponse}.
   */
  class Factory extends ModelFactory<SzBulkLoadResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzBulkLoadResponse.class);
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
     * Creates an instance of {@link SzBulkLoadResponse} with the
     * specified {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzBulkLoadResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzBulkLoadResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and the speicified {@link
     * SzBulkLoadResult} describing the result of the bulk load.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param loadResult The {@link SzBulkLoadResult} describing the results
     *                   of the bulk load.
     */
    public SzBulkLoadResponse create(SzMeta           meta,
                                     SzLinks          links,
                                     SzBulkLoadResult loadResult)
    {
      return this.getProvider().create(meta, links, loadResult);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
