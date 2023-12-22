package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * A response object that contains the {@link SzWhyRecordsResult} describing
 * why or why not two records did or did not resolve.
 */
@JsonDeserialize
public class SzWhyRecordsResponseImpl extends SzResponseWithRawDataImpl
  implements SzWhyRecordsResponse
{
  /**
   * The data for this response.
   */
  private SzWhyRecordsResponseData data = null;

  /**
   * Package-private default constructor.
   */
  protected SzWhyRecordsResponseImpl() {
    this.data = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * entity data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzWhyRecordsResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, SzWhyRecordsResponseData.FACTORY.create());
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * entity data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The data for the response.
   */
  public SzWhyRecordsResponseImpl(SzMeta                    meta,
                                  SzLinks                   links,
                                  SzWhyRecordsResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzWhyRecordsResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzWhyRecordsResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setWhyResult(SzWhyRecordsResult result) {
    this.data.setWhyResult(result);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEntity(SzEntityData entity) {
    this.data.addEntity(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntities(Collection<? extends SzEntityData> entities) {
    this.data.setEntities(entities);
  }
}
