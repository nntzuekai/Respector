package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * Provides a default implementation of {@Link SzDataSourcesResponse}.
 */
@JsonDeserialize
public class SzDataSourcesResponseImpl extends SzResponseWithRawDataImpl
  implements SzDataSourcesResponse
{
  /**
   * The data for this instance.
   */
  private SzDataSourcesResponseData data = null;

  /**
   * Package-private default constructor for JSON deserialization.
   */
  protected SzDataSourcesResponseImpl() {
    this.data = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * data sources to be added later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   */
  public SzDataSourcesResponseImpl(SzMeta meta, SzLinks links) {
    this(meta, links, SzDataSourcesResponseData.FACTORY.create());
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * data sources to be added later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   */
  public SzDataSourcesResponseImpl(SzMeta                     meta,
                                   SzLinks                    links,
                                   SzDataSourcesResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzDataSourcesResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzDataSourcesResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addDataSource(SzDataSource dataSource) {
    this.data.addDataSource(dataSource);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDataSources(Collection<? extends SzDataSource> dataSources)
  {
    this.data.setDataSources(dataSources);
  }
}
