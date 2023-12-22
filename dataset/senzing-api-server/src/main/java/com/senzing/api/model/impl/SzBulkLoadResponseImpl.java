package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzBulkLoadResponse}.
 */
@JsonDeserialize
public class SzBulkLoadResponseImpl extends SzBasicResponseImpl
  implements SzBulkLoadResponse
{
  /**
   * The {@link SzBulkLoadResult} describing the record.
   */
  private SzBulkLoadResult bulkLoadResult;

  /**
   * Protected default constructor.
   */
  protected SzBulkLoadResponseImpl() {
    this.bulkLoadResult = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * record data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzBulkLoadResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, null);
  }

  /**
   * Constructs with the HTTP method, self link and the {@link
   * SzBulkLoadResult} describing the record.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param bulkLoadResult The {@link SzBulkLoadResult} describing the result
   *                       of the bulk load.
   */
  public SzBulkLoadResponseImpl(SzMeta            meta,
                                SzLinks           links,
                                SzBulkLoadResult  bulkLoadResult)
  {
    super(meta, links);
    this.bulkLoadResult = bulkLoadResult;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzBulkLoadResult getData() {
    return this.bulkLoadResult;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzBulkLoadResult bulkLoadResult) {
    this.bulkLoadResult = bulkLoadResult;
  }
}
