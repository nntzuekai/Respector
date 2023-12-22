package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzDeleteRecordResponse}.
 */
@JsonDeserialize
public class SzDeleteRecordResponseImpl extends SzResponseWithRawDataImpl
  implements SzDeleteRecordResponse
{
  /**
   * The data for this instance.
   */
  private SzDeleteRecordResponseData data = null;

  /**
   * Protected default constructor.
   */
  protected SzDeleteRecordResponseImpl() {
    this.data = null;
  }

  /**
   * Constructs with only the HTTP method and the self link.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzDeleteRecordResponseImpl(SzMeta meta, SzLinks links) {
    this(meta, links, SzDeleteRecordResponseData.FACTORY.create());
  }

  /**
   * Constructs with the HTTP method, the self link and the info.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzDeleteRecordResponseData} describing the data
   *             for this instance.
   */
  public SzDeleteRecordResponseImpl(SzMeta                      meta,
                                    SzLinks                     links,
                                    SzDeleteRecordResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzDeleteRecordResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzDeleteRecordResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInfo(SzResolutionInfo info) {
    this.data.setInfo(info);
  }
}
