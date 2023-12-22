package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDeleteRecordResponseImpl;

/**
 * The response for a record deletion operation.
 *
 */
@JsonDeserialize(using=SzDeleteRecordResponse.Factory.class)
public interface SzDeleteRecordResponse extends SzResponseWithRawData
{
  /**
   * Returns the {@link SzDeleteRecordResponseData} for this instance.
   *
   * @return The {@link SzDeleteRecordResponseData} for this instance.
   */
  SzDeleteRecordResponseData getData();

  /**
   * Sets the {@link SzDeleteRecordResponseData} for this instance.
   *
   * @param data The {@link SzDeleteRecordResponseData} for this instance.
   */
  void setData(SzDeleteRecordResponseData data);

  /**
   * Convenience method to set the @link SzResolutionInfo} on the underlying
   * {@link SzDeleteRecordResponseData}.
   *
   * @param info The @link SzResolutionInfo} providing the information
   *             associated with the resolution of the record.
   */
  void setInfo(SzResolutionInfo info);

  /**
   * A {@link ModelProvider} for instances of {@link SzDeleteRecordResponse}.
   */
  interface Provider extends ModelProvider<SzDeleteRecordResponse> {
    /**
     * Creates an instance of {@link SzDeleteRecordResponse} initialized with
     * the specified {@link SzMeta} and {@link SzLinks}, but no data.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzDeleteRecordResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzDeleteRecordResponse} initialized with
     * the specified {@link SzMeta}, {@link SzLinks} and the specified
     * {@link SzResolutionInfo}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzDeleteRecordResponseData} describing the data
     *             for the response..
     */
    SzDeleteRecordResponse create(SzMeta                      meta,
                                  SzLinks                     links,
                                  SzDeleteRecordResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDeleteRecordResponse} that produces instances of
   * {@link SzDeleteRecordResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDeleteRecordResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDeleteRecordResponse.class, SzDeleteRecordResponseImpl.class);
    }

    @Override
    public SzDeleteRecordResponse create(SzMeta meta, SzLinks links) {
      return new SzDeleteRecordResponseImpl(meta, links);
    }

    @Override
    public SzDeleteRecordResponse create(SzMeta                     meta,
                                         SzLinks                    links,
                                         SzDeleteRecordResponseData data)
    {
      return new SzDeleteRecordResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzDeleteRecordResponse}.
   */
  class Factory extends ModelFactory<SzDeleteRecordResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDeleteRecordResponse.class);
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
     * Creates an instance of {@link SzDeleteRecordResponse} initialized with
     * the specified {@link SzMeta} and {@link SzLinks}, but no data.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzDeleteRecordResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzDeleteRecordResponse} initialized with
     * the specified {@link SzMeta}, {@link SzLinks} and the specified
     * {@link SzResolutionInfo}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzDeleteRecordResponseData} describing the data
     *             for the response..
     */
    public SzDeleteRecordResponse create(SzMeta                     meta,
                                         SzLinks                    links,
                                         SzDeleteRecordResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
