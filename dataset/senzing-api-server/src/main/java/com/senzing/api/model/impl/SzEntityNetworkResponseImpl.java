package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzEntityNetworkResponse}.
 */
@JsonDeserialize
public class SzEntityNetworkResponseImpl extends SzResponseWithRawDataImpl
  implements SzEntityNetworkResponse
{
  /**
   * The {@link SzEntityNetworkData} describing the entity.
   */
  private SzEntityNetworkData entityNetworkData;

  /**
   * Package-private default constructor.
   */
  protected SzEntityNetworkResponseImpl() {
    this.entityNetworkData = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * entity data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzEntityNetworkResponseImpl(SzMeta meta, SzLinks links)
  {
    this(meta, links, null);
  }

  /**
   * Constructs with the HTTP method, self link and the {@link
   * SzEntityPathData} describing the record.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzEntityRecord} describing the record.
   */
  public SzEntityNetworkResponseImpl(SzMeta               meta,
                                     SzLinks              links,
                                     SzEntityNetworkData  data)
  {
    super(meta, links);
    this.entityNetworkData = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzEntityNetworkData getData() {
    return this.entityNetworkData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzEntityNetworkData data) {
    this.entityNetworkData = data;
  }
}
