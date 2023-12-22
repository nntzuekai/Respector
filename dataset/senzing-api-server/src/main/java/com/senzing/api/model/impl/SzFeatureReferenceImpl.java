package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzFeatureReference;

/**
 * Describes a feature for an entity.
 */
@JsonDeserialize
public class SzFeatureReferenceImpl implements SzFeatureReference {
  /**
   * The internal ID of the primary feature value.
   */
  private Long internalId;

  /**
   * The primary value for the feature.
   */
  private String primaryValue;

  /**
   * The usage type associated with the feature.
   */
  private String usageType;

  /**
   * Default constructor.
   */
  public SzFeatureReferenceImpl() {
    this.internalId = null;
    this.usageType  = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getInternalId() {
    return this.internalId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInternalId(Long internalId) {
    this.internalId = internalId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUsageType() {
    return usageType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUsageType(String usageType) {
    this.usageType = usageType;
  }

  @Override
  public String toString() {
    return "SzFeatureReference{" +
        "internalId=" + internalId +
        ", usageType='" + usageType + '\'' +
        '}';
  }
}
