package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzDataSourceResponse}.
 */
@JsonDeserialize
public class SzDataSourceResponseImpl extends SzResponseWithRawDataImpl
  implements SzDataSourceResponse
{
  /**
   * The data for this instance.
   */
  private SzDataSourceResponseData data = null;

  /**
   * Protected default constructor.
   */
  protected SzDataSourceResponseImpl() {
    this.data = null;
  }

  /**
   * Constructs with the specified {@link SzMeta} and {@link SzLinks}.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   */
  public SzDataSourceResponseImpl(SzMeta meta, SzLinks links) {
    this(meta, links, SzDataSourceResponseData.FACTORY.create());
  }

  /**
   * Constructs with the specified {@link SzMeta} and {@link SzLinks}.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The data for the response.
   */
  public SzDataSourceResponseImpl(SzMeta                    meta,
                                  SzLinks                   links,
                                  SzDataSourceResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzDataSourceResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzDataSourceResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDataSource(SzDataSource dataSource) {
    this.data.setDataSource(dataSource);
  }
}
