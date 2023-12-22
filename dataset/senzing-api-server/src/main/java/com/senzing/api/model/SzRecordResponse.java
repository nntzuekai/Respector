package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzRecordResponseImpl;
import com.senzing.util.Timers;

import javax.ws.rs.core.UriInfo;
import java.util.*;

/**
 * A response object that contains entity record data.
 *
 */
@JsonDeserialize(using=SzRecordResponse.Factory.class)
public interface SzRecordResponse extends SzResponseWithRawData {
  /**
   * Returns the data associated with this response which contains an
   * {@link SzEntityRecord}.
   *
   * @return The data associated with this response.
   */
  SzRecordResponseData getData();

  /**
   * Sets the data associated with this response which contains an
   * {@link SzEntityRecord}.
   *
   * @return The data associated with this response.
   */
  void setData(SzRecordResponseData data);

  /**
   * Sets the data associated with this response with an {@link SzEntityRecord}.
   *
   * @param record The {@link SzEntityRecord} describing the record.
   */
  void setRecord(SzEntityRecord record);

  /**
   * A {@link ModelProvider} for instances of {@link SzRecordResponse}.
   */
  interface Provider extends ModelProvider<SzRecordResponse> {
    /**
     * Creates an instance of {@link SzRecordResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} and no data.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzRecordResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzRecordResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and the specified
     * {@link SzRecordResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzRecordResponseData} describing the data for
     *             the response.
     */
    SzRecordResponse create(SzMeta                meta,
                            SzLinks               links,
                            SzRecordResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzRecordResponse} that produces instances of
   * {@link SzRecordResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzRecordResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzRecordResponse.class, SzRecordResponseImpl.class);
    }

    @Override
    public SzRecordResponse create(SzMeta meta, SzLinks links) {
      return new SzRecordResponseImpl(meta, links);
    }

    @Override
    public SzRecordResponse create(SzMeta               meta,
                                   SzLinks              links,
                                   SzRecordResponseData data)
    {
      return new SzRecordResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzRecordResponse}.
   */
  class Factory extends ModelFactory<SzRecordResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzRecordResponse.class);
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
     * Creates an instance of {@link SzRecordResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} and no data.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzRecordResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzRecordResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and the specified
     * {@link SzRecordResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzRecordResponseData} describing the data for
     *             the response.
     */
    public SzRecordResponse create(SzMeta               meta,
                                   SzLinks              links,
                                   SzRecordResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
