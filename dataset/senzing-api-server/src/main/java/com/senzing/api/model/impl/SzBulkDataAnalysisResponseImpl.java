package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * A response object that contains entity record data.
 *
 */
@JsonDeserialize
public class SzBulkDataAnalysisResponseImpl extends SzBasicResponseImpl
  implements SzBulkDataAnalysisResponse
{
  /**
   * The {@link SzBulkDataAnalysis} describing the record.
   */
  private SzBulkDataAnalysis bulkDataAnalysis;

  /**
   * Protected default constructor.
   */
  protected SzBulkDataAnalysisResponseImpl() {
    this.bulkDataAnalysis = null;
  }

  /**
   * Constructs with the specified {@link SzMeta} and {@Link SzLinks),
   * leaving the record data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzBulkDataAnalysisResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, null);
  }

  /**
   * Constructs with the HTTP method, self link and the {@link
   * SzBulkDataAnalysis} describing the record.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param dataAnalysis The {@link SzEntityRecord} describing the record.
   */
  public SzBulkDataAnalysisResponseImpl(SzMeta              meta,
                                        SzLinks             links,
                                        SzBulkDataAnalysis  dataAnalysis)
  {
    super(meta, links);
    this.bulkDataAnalysis = dataAnalysis;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzBulkDataAnalysis getData() {
    return this.bulkDataAnalysis;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzBulkDataAnalysis dataAnalysis) {
    this.bulkDataAnalysis = dataAnalysis;
  }
}
