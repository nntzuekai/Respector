package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzBulkDataAnalysisResponseImpl;
import com.senzing.util.Timers;

import javax.ws.rs.core.UriInfo;

/**
 * A response object that contains entity record data.
 *
 */
@JsonDeserialize(using=SzBulkDataAnalysisResponse.Factory.class)
public interface SzBulkDataAnalysisResponse extends SzBasicResponse {
  /**
   * Returns the data associated with this response which is an
   * {@link SzBulkDataAnalysis}.
   *
   * @return The data associated with this response.
   */
  SzBulkDataAnalysis getData();

  /**
   * Sets the data associated with this response with an {@link
   * SzBulkDataAnalysis}.
   *
   * @param dataAnalysis The {@link SzBulkDataAnalysis} describing the record.
   */
  void setData(SzBulkDataAnalysis dataAnalysis);

  /**
   * A {@link ModelProvider} for instances of {@link SzBulkDataAnalysisResponse}.
   */
  interface Provider extends ModelProvider<SzBulkDataAnalysisResponse> {
    /**
     * Creates an instance of {@link SzBulkDataAnalysisResponse} with the
     * specified {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzBulkDataAnalysisResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzBulkDataAnalysisResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and the speicified {@link
     * SzBulkDataAnalysis} describing the bulk records..
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param dataAnalysis The {@link SzEntityRecord} describing the record.
     */
    SzBulkDataAnalysisResponse create(SzMeta              meta,
                                      SzLinks             links,
                                      SzBulkDataAnalysis  dataAnalysis);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzBulkDataAnalysisResponse} that produces instances of
   * {@link SzBulkDataAnalysisResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzBulkDataAnalysisResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzBulkDataAnalysisResponse.class,
            SzBulkDataAnalysisResponseImpl.class);
    }

    @Override
    public SzBulkDataAnalysisResponse create(SzMeta meta, SzLinks links){
      return new SzBulkDataAnalysisResponseImpl(meta, links);
    }

    @Override
    public SzBulkDataAnalysisResponse create(SzMeta              meta,
                                             SzLinks             links,
                                             SzBulkDataAnalysis  dataAnalysis)
    {
      return new SzBulkDataAnalysisResponseImpl(meta, links, dataAnalysis);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzBulkDataAnalysisResponse}.
   */
  class Factory extends ModelFactory<SzBulkDataAnalysisResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzBulkDataAnalysisResponse.class);
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
     * Creates an instance of {@link SzBulkDataAnalysisResponse} with the
     * specified {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzBulkDataAnalysisResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzBulkDataAnalysisResponse} with the
     * specified {@link SzMeta}, {@link SzLinks} and the speicified {@link
     * SzBulkDataAnalysis} describing the bulk records.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param dataAnalysis The {@link SzBulkDataAnalysis} describing the
     *                     bulk records.
     */
    public SzBulkDataAnalysisResponse create(SzMeta              meta,
                                             SzLinks             links,
                                             SzBulkDataAnalysis  dataAnalysis)
    {
      return this.getProvider().create(meta, links, dataAnalysis);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
