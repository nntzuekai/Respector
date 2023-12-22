package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * A response object that contains entity data.
 *
 */
@JsonDeserialize
public class SzEntityResponseImpl extends SzResponseWithRawDataImpl
  implements SzEntityResponse
{
  /**
   * The {@link SzEntityData} describing the entity.
   */
  private SzEntityData entityData;

  /**
   * Package-private default constructor.
   */
  protected SzEntityResponseImpl() {
    this.entityData = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * entity data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzEntityResponseImpl(SzMeta meta, SzLinks links) {
    this(meta, links, null);
  }

  /**
   * Constructs with the HTTP method, self link and the {@link SzEntityData}
   * describing the record.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzEntityRecord} describing the record.
   */
  public SzEntityResponseImpl(SzMeta         meta,
                              SzLinks        links,
                              SzEntityData   data)
  {
    super(meta, links);
    this.entityData = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzEntityData getData() {
    return this.entityData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzEntityData data) {
    this.entityData = data;
  }
}
