package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * A response object that contains entity record data.
 *
 */
@JsonDeserialize
public class SzRecordResponseImpl extends SzResponseWithRawDataImpl
  implements SzRecordResponse
{
  /**
   * The data for this instance.
   */
  private SzRecordResponseData data = null;

  /**
   * Protected default constructor.
   */
  protected SzRecordResponseImpl() {
    this.data = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * record data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzRecordResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, SzRecordResponseData.FACTORY.create());
  }

  /**
   * Constructs with the HTTP method, self link and the {@link SzEntityRecord}
   * describing the record.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzRecordResponseData} describing the data for the
   *             response.
   */
  public SzRecordResponseImpl(SzMeta                meta,
                              SzLinks               links,
                              SzRecordResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzRecordResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzRecordResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRecord(SzEntityRecord record) {
    this.data.setRecord(record);
  }
}
