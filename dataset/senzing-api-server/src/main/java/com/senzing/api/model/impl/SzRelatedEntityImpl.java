package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzRelatedEntity;
import com.senzing.api.model.SzRelationshipType;

/**
 * Describes an entity related to the base entity.
 */
@JsonDeserialize
public class SzRelatedEntityImpl extends SzBaseRelatedEntityImpl
    implements SzRelatedEntity
{
  /**
   * Whether or not the relationship is disclosed.
   */
  private boolean disclosed;

  /**
   * Whether or not the relationship is ambiguous.
   */
  private boolean ambiguous;

  /**
   * The relationship type.
   */
  private SzRelationshipType relationType;

  /**
   * Default constructor.
   */
  public SzRelatedEntityImpl() {
    this.disclosed    = false;
    this.ambiguous    = false;
    this.relationType = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDisclosed() {
    return this.disclosed;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDisclosed(boolean disclosed) {
    this.disclosed = disclosed;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAmbiguous() {
    return this.ambiguous;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAmbiguous(boolean ambiguous) {
    this.ambiguous = ambiguous;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzRelationshipType getRelationType() {
    return this.relationType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRelationType(SzRelationshipType relationType) {
    this.relationType = relationType;
  }

  @Override
  public String toString() {
    return "SzRelatedEntity{" +
        super.toString() +
        ", disclosed=" + disclosed +
        ", ambiguous=" + ambiguous +
        ", relationType=" + relationType +
        '}';
  }
}
