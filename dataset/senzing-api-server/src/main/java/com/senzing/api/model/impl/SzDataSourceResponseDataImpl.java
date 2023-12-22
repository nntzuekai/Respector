package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzDataSourceResponseData}.
 */
@JsonDeserialize
public class SzDataSourceResponseDataImpl implements SzDataSourceResponseData
{
  /**
   * The {@link SzDataSource} for this instance.
   */
  private SzDataSource dataSource;

  /**
   * Default constructor.
   */
  public SzDataSourceResponseDataImpl() {
      this.dataSource = null;
    }

  /**
   * Constructs with the specified {@link SzDataSource} describing the data
   * source.
   *
   * @param dataSource The {@link SzDataSource} describing the data source.
   */
  public SzDataSourceResponseDataImpl(SzDataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzDataSource getDataSource() {
    return this.dataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDataSource(SzDataSource dataSource) {
    this.dataSource = dataSource;
  }
}
