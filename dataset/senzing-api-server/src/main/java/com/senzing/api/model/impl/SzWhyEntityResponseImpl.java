package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * A response object that contains the {@link SzWhyEntityResult} describing
 * why an entity resolved.
 */
@JsonDeserialize
public class SzWhyEntityResponseImpl extends SzResponseWithRawDataImpl
  implements SzWhyEntityResponse
{
  /**
   * The {@link SzWhyEntityResponseData} describing the result data.
   */
  private SzWhyEntityResponseData data = null;

  /**
   * Package-private default constructor.
   */
  protected SzWhyEntityResponseImpl() {
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
  public SzWhyEntityResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, SzWhyEntityResponseData.FACTORY.create());
  }

  /**
   * Constructs with the specified {@link SzMeta}, {@link SzLinks} and
   * {@link SzWhyEntityResponseData}.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The data for the response.
   */
  public SzWhyEntityResponseImpl(SzMeta                   meta,
                                 SzLinks                  links,
                                 SzWhyEntityResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzWhyEntityResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzWhyEntityResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setWhyResults(Collection<? extends SzWhyEntityResult> results) {
    this.data.setWhyResults(results);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addWhyResult(SzWhyEntityResult result) {
    this.data.addWhyResult(result);
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
