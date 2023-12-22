package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzEntityPathResponse}.
 */
@JsonDeserialize
public class SzEntityPathResponseImpl extends SzResponseWithRawDataImpl
  implements SzEntityPathResponse
{
  /**
   * The {@link SzEntityPathData} describing the entity.
   */
  private SzEntityPathData entityPathData;

  /**
   * Package-private default constructor.
   */
  protected SzEntityPathResponseImpl() {
    this.entityPathData = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * entity data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzEntityPathResponseImpl(SzMeta meta, SzLinks links) {
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
  public SzEntityPathResponseImpl(SzMeta            meta,
                                  SzLinks           links,
                                  SzEntityPathData  data)
  {
    super(meta, links);
    this.entityPathData = data;
  }

  /**
   * Returns the data associated with this response which is an
   * {@link SzEntityPathData}.
   *
   * @return The data associated with this response.
   */
  public SzEntityPathData getData() {
    return this.entityPathData;
  }

  /**
   * Sets the data associated with this response with an
   * {@link SzEntityPathData}.
   *
   * @param data The {@link SzEntityPathData} describing the record.
   */
  public void setData(SzEntityPathData data) {
    this.entityPathData = data;
  }
}
