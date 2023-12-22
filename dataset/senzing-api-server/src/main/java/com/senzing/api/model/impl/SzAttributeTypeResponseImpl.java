package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzAttributeTypeResponse}.
 */
@JsonDeserialize
public class SzAttributeTypeResponseImpl extends SzResponseWithRawDataImpl
  implements SzAttributeTypeResponse
{
  /**
   * The data for this instance.
   */
  private SzAttributeTypeResponseData data = null;

  /**
   * Protected default constructor.
   */
  protected SzAttributeTypeResponseImpl() {
    super();
    this.data = null;
  }

  /**
   * Constructs with only the HTTP method and the self link, leaving the
   * attribute type data to be initialized later.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzAttributeTypeResponseImpl(SzMeta meta, SzLinks links) {
    this(meta, links, SzAttributeTypeResponseData.FACTORY.create());
  }

  /**
   * Constructs with the HTTP method, self link and the {@link SzAttributeType}
   * describing the attribute type.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The {@link SzAttributeTypeResponseData} which has the
   *             attribute type.
   */
  public SzAttributeTypeResponseImpl(SzMeta                       meta,
                                     SzLinks                      links,
                                     SzAttributeTypeResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzAttributeTypeResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzAttributeTypeResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAttributeType(SzAttributeType attributeType) {
    this.data.setAttributeType(attributeType);
  }
}
