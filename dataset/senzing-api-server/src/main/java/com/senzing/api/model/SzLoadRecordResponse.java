package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzLoadRecordResponseImpl;
import com.senzing.util.Timers;

import javax.ws.rs.core.UriInfo;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * The response containing the record ID of the record that was loaded.
 *
 */
@JsonDeserialize(using=SzLoadRecordResponse.Factory.class)
public interface SzLoadRecordResponse extends SzResponseWithRawData {
  /**
   * Returns the {@link SzLoadRecordResponseData} for this instance.
   *
   * @return The {@link SzLoadRecordResponseData} for this instance.
   */
  SzLoadRecordResponseData getData();

  /**
   * Sets the {@link SzLoadRecordResponseData} for this instance.
   *
   * @param data The {@link SzLoadRecordResponseData} for this instance.
   */
  void setData(SzLoadRecordResponseData data);

  /**
   * Convenience method to set the record ID of the record that was loaded
   * on the underlying {@link SzLoadRecordResponseData}.
   *
   * @param recordId The record ID of the record.
   */
  void setRecordId(String recordId);

  /**
   * Convenience method to set the {@link SzResolutionInfo} for the resolution
   * of the record on the underlying {@link SzLoadRecordResponseData}.
   *
   * @param info The @link SzResolutionInfo} providing the information associated
   *             with the resolution of the record.
   */
  void setInfo(SzResolutionInfo info);

  /**
   * A {@link ModelProvider} for instances of {@link SzLoadRecordResponse}.
   */
  interface Provider extends ModelProvider<SzLoadRecordResponse> {
    /**
     * Creates an instance of {@link SzLoadRecordResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzLoadRecordResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzLoadRecordResponse} with the specified
     * {@link SzMeta}, {@link SzLinks}, and {@link SzLoadRecordResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    SzLoadRecordResponse create(SzMeta                    meta,
                                SzLinks                   links,
                                SzLoadRecordResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzLoadRecordResponse} that produces instances of
   * {@link SzLoadRecordResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzLoadRecordResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzLoadRecordResponse.class, SzLoadRecordResponseImpl.class);
    }

    @Override
    public SzLoadRecordResponse create(SzMeta meta, SzLinks links) {
      return new SzLoadRecordResponseImpl(meta, links);
    }

    @Override
    public SzLoadRecordResponse create(SzMeta                   meta,
                                       SzLinks                  links,
                                       SzLoadRecordResponseData data)
    {
      return new SzLoadRecordResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzLoadRecordResponse}.
   */
  class Factory extends ModelFactory<SzLoadRecordResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzLoadRecordResponse.class);
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
     * Creates an instance of {@link SzLoadRecordResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzLoadRecordResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzLoadRecordResponse} with the specified
     * {@link SzMeta}, {@link SzLinks}, and {@link SzLoadRecordResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    public SzLoadRecordResponse create(SzMeta                   meta,
                                       SzLinks                  links,
                                       SzLoadRecordResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
