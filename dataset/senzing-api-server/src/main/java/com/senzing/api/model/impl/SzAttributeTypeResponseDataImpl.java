package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzAttributeTypeResponse}.
 */
@JsonDeserialize
public class SzAttributeTypeResponseDataImpl
    implements SzAttributeTypeResponseData
{
  /**
   * The {@link SzAttributeType} describing the attribute type.
   */
  private SzAttributeType attributeType;

  /**
   * Default constructor.
   */
  public SzAttributeTypeResponseDataImpl() {
    this.attributeType = null;
  }

  /**
   * Constructs with the specified instance of {@link SzAttributeType}
   * describing the attribute type.
   *
   * @param attributeType The {@link SzAttributeType} describing the
   *                      attribute type.
   */
  public SzAttributeTypeResponseDataImpl(SzAttributeType attributeType) {
    this.attributeType = attributeType;
  }

  /**
   * Gets the {@link SzAttributeType} describing the attribute type.
   *
   * @return The {@link SzAttributeType} describing the attributeType.
   */
  public SzAttributeType getAttributeType() {
    return this.attributeType;
  }

  /**
   * Sets the {@link SzAttributeType} for this instance.
   *
   * @param attributeType The {@link SzAttributeType} describing the
   *                      attribute type.
   *
   */
  public void setAttributeType(SzAttributeType attributeType) {
    this.attributeType = attributeType;
  }
}
