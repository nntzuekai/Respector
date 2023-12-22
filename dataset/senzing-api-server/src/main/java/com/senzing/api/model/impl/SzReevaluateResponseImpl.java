package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzReevaluateResponse}.
 *
 */
@JsonDeserialize
public class SzReevaluateResponseImpl extends SzResponseWithRawDataImpl
  implements SzReevaluateResponse
{
  /**
   * The data for this instance.
   */
  private SzReevaluateResponseData data = null;

  /**
   * Protected default constructor.
   */
  protected SzReevaluateResponseImpl() {
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
  public SzReevaluateResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, SzReevaluateResponseData.FACTORY.create());
  }

  /**
   * Constructs with the HTTP method, the self link, and the record ID.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzReevaluateResponseData} providing the information
   *             associated with the resolution of the record.
   */
  public SzReevaluateResponseImpl(SzMeta                    meta,
                                  SzLinks                   links,
                                  SzReevaluateResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzReevaluateResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzReevaluateResponseData data) {
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
