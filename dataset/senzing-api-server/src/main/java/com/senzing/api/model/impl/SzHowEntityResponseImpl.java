package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityRecord;
import com.senzing.api.model.SzLinks;
import com.senzing.api.model.SzMeta;
import com.senzing.api.model.SzHowEntityResult;
import com.senzing.api.model.SzHowEntityResponse;

/**
 * A response object that contains entity data.
 *
 */
@JsonDeserialize
public class SzHowEntityResponseImpl extends SzResponseWithRawDataImpl
  implements SzHowEntityResponse
{
  /**
   * The {@link SzHowEntityResult} describing how the entity resolved.
   */
  private SzHowEntityResult howResult;

  /**
   * Package-private default constructor.
   */
  protected SzHowEntityResponseImpl() {
    this.howResult = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * entity data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzHowEntityResponseImpl(SzMeta meta, SzLinks links) {
    this(meta, links, null);
  }

  /**
   * Constructs with the HTTP method, self link and the {@link
   * SzHowEntityResult} describing the record.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzEntityRecord} describing the record.
   */
  public SzHowEntityResponseImpl(SzMeta             meta,
                                 SzLinks            links,
                                 SzHowEntityResult  data)
  {
    super(meta, links);
    this.howResult = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzHowEntityResult getData() {
    return this.howResult;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzHowEntityResult data) {
    this.howResult = data;
  }
}
