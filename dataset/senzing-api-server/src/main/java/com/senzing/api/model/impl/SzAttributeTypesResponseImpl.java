package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a default implementation of {@link SzAttributeTypesResponse}.
 */
@JsonDeserialize
public class SzAttributeTypesResponseImpl extends SzResponseWithRawDataImpl
  implements SzAttributeTypesResponse
{
  /**
   * The data for this instance.
   */
  private SzAttributeTypesResponseData data = null;

  /**
   * Default constructor.
   */
  public SzAttributeTypesResponseImpl() {
    super();
    this.data = null;
  }

  /**
   * Constructs with the specified {@link SzMeta} and {@link SzLinks}.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   */
  public SzAttributeTypesResponseImpl(SzMeta meta, SzLinks links) {
    this(meta, links, SzAttributeTypesResponseData.FACTORY.create());
  }

  /**
   * Constructs with the specified {@link SzMeta}, {@link SzLinks} and
   * {@link SzAttributeTypesResponseData}.
   *
   * @param meta The response meta data.
   *
   * @param links The links for the response.
   *
   * @param data The data for the response.
   */
  public SzAttributeTypesResponseImpl(SzMeta                        meta,
                                      SzLinks                       links,
                                      SzAttributeTypesResponseData  data)
  {
    super(meta, links);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzAttributeTypesResponseData getData() {
    return this.data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setData(SzAttributeTypesResponseData data) {
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addAttributeType(SzAttributeType attributeType) {
    if (attributeType == null) {
      throw new NullPointerException(
          "Cannot add a null attribute type.");
    }
    this.data.addAttributeType(attributeType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAttributeTypes(
      Collection<? extends SzAttributeType> attributeTypes)
  {
    this.data.setAttributeTypes(attributeTypes);
  }
}
