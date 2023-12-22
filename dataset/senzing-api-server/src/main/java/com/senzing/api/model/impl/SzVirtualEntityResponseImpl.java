package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * A response object that contains entity data.
 *
 */
@JsonDeserialize
public class SzVirtualEntityResponseImpl extends SzResponseWithRawDataImpl
  implements SzVirtualEntityResponse
{
  /**
   * The {@link SzVirtualEntityData} describing the entity.
   */
  private SzVirtualEntityData entityData;

  /**
   * Package-private default constructor.
   */
  protected SzVirtualEntityResponseImpl() {
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
  public SzVirtualEntityResponseImpl(SzMeta meta, SzLinks links) {
    this(meta, links, null);
  }

  /**
   * Constructs with the HTTP method, self link and the {@link
   * SzVirtualEntityData} describing the record.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzEntityRecord} describing the record.
   */
  public SzVirtualEntityResponseImpl(SzMeta               meta,
                                     SzLinks              links,
                                     SzVirtualEntityData  data)
  {
    super(meta, links);
    this.entityData = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzVirtualEntityData getData() {
    return this.entityData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzVirtualEntityData data) {
    this.entityData = data;
  }
}
