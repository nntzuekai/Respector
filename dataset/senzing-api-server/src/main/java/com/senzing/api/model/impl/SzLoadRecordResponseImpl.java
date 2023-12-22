package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Provides a default implementation of {@link SzLoadRecordResponse}.
 */
@JsonDeserialize
public class SzLoadRecordResponseImpl extends SzResponseWithRawDataImpl
  implements SzLoadRecordResponse
{
  /**
   * The data for this instance.
   */
  private SzLoadRecordResponseData data = null;

  /**
   * Protected default constructor.
   */
  protected SzLoadRecordResponseImpl() {
    this.data = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * record ID to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzLoadRecordResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, SzLoadRecordResponseData.FACTORY.create());
  }

  /**
   * Constructs with the HTTP method, the self link, and the record ID.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The data for the response.
   */
  public SzLoadRecordResponseImpl(SzMeta                    meta,
                                  SzLinks                   links,
                                  SzLoadRecordResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzLoadRecordResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzLoadRecordResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRecordId(String recordId) {
    this.data.setRecordId(recordId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInfo(SzResolutionInfo info) {
    this.data.setInfo(info);
  }
}
