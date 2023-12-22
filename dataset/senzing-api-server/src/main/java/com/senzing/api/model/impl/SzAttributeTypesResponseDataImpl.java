package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * Provides a default implementation of {@link SzAttributeTypesResponse}.
 */
@JsonDeserialize
public class SzAttributeTypesResponseDataImpl
    implements SzAttributeTypesResponseData
{
  /**
   * The list of {@link SzAttributeType} instances describing the attribute
   * types.
   */
  private List<SzAttributeType> attributeTypes = null;

  /**
   * Default constructor.
   */
  public SzAttributeTypesResponseDataImpl() {
    this.attributeTypes = new LinkedList<>();
  }

  /**
   * Constructs witht he specified {@link Collection} of {@link SzAttributeType}
   * instances.
   *
   * @param attributeTypes The {@link Collection} of {@link SzAttributeType}
   *                       instances with which to construct.
   */
  public SzAttributeTypesResponseDataImpl(
      Collection<? extends SzAttributeType> attributeTypes)
  {
    this.attributeTypes = new ArrayList<>(attributeTypes);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzAttributeType> getAttributeTypes() {
    List<SzAttributeType> list = this.attributeTypes;
    return Collections.unmodifiableList(list);
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
    this.attributeTypes.add(attributeType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAttributeTypes(Collection<? extends SzAttributeType> attrTypes)
  {
    this.attributeTypes.clear();
    if (attrTypes != null) {
      this.attributeTypes.addAll(attrTypes);
    }
  }
}
