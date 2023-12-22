package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a default implementation of {@link SzAttributeSearchResponse}.
 */
@JsonDeserialize
public class SzAttributeSearchResponseImpl extends SzResponseWithRawDataImpl
  implements SzAttributeSearchResponse
{
  /**
   * The data for this instance.
   */
  private SzAttributeSearchResponseData data = null;

  /**
   * Protected default constructor.
   */
  protected SzAttributeSearchResponseImpl() {
    this.data = null;
  }

  /**
   * Constructs with the specified {@link SzMeta} and {@link SzLinks} instances.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzAttributeSearchResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, SzAttributeSearchResponseData.FACTORY.create());
  }

  /**
   * Constructs with the specified {@link SzMeta} and {@link SzLinks} instances.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzAttributeSearchResponseData} for this instance.
   */
  public SzAttributeSearchResponseImpl(SzMeta                         meta,
                                       SzLinks                        links,
                                       SzAttributeSearchResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzAttributeSearchResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzAttributeSearchResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSearchResults(List<SzAttributeSearchResult> results)
  {
    this.data.setSearchResults(results);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addSearchResult(SzAttributeSearchResult result) {
    this.data.addSearchResult(result);
  }
}
