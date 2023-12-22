package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * A response object that contains the {@link SzWhyEntitiesResult} describing
 * why two entities related or did not resolve.
 */
@JsonDeserialize
public class SzWhyEntitiesResponseImpl extends SzResponseWithRawDataImpl
  implements SzWhyEntitiesResponse
{
  /**
   * The {@link SzWhyEntitiesResponseData} describing the result data.
   */
  private SzWhyEntitiesResponseData data = null;

  /**
   * Package-private default constructor.
   */
  protected SzWhyEntitiesResponseImpl() {
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
  public SzWhyEntitiesResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, SzWhyEntitiesResponseData.FACTORY.create());
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * entity data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzWhyEntitiesResponseImpl(SzMeta                     meta,
                                   SzLinks                    links,
                                   SzWhyEntitiesResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzWhyEntitiesResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzWhyEntitiesResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setWhyResult(SzWhyEntitiesResult result) {
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
