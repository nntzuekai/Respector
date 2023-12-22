package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzCandidateKey;
import com.senzing.api.model.SzScoredFeature;
import com.senzing.api.model.SzWhyPerspective;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Describes a feature value that has been scored.
 */
@JsonDeserialize
public class SzScoredFeatureImpl implements SzScoredFeature {
  /**
   * The feature ID for the scored feature.
   */
  private Long featureId;

  /**
   * The feature type for the scored feature.
   */
  private String featureType;

  /**
   * The feature value for the scored feature.
   */
  private String featureValue;

  /**
   * The usage type for the scored feature.
   */
  private String usageType;

  /**
   * Constructs with the specified parameters.
   */
  public SzScoredFeatureImpl(Long   featureId,
                             String featureType,
                             String featureValue,
                             String usageType)
  {
    this.featureId    = featureId;
    this.featureType  = featureType;
    this.featureValue = featureValue;
    this.usageType    = usageType;
  }

  /**
   * Default constructor.
   */
  public SzScoredFeatureImpl() {
    this.featureId    = null;
    this.featureType  = null;
    this.featureValue = null;
    this.usageType    = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getFeatureId() {
    return this.featureId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFeatureId(Long featureId) {
    this.featureId = featureId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFeatureType() {
    return this.featureType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFeatureType(String featureType) {
    this.featureType = featureType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFeatureValue() {
    return this.featureValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFeatureValue(String featureValue) {
    this.featureValue = featureValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUsageType() {
    return this.usageType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUsageType(String usageType) {
    this.usageType = usageType;
  }


  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    SzScoredFeatureImpl that = (SzScoredFeatureImpl) object;
    return Objects.equals(this.getFeatureId(), that.getFeatureId()) &&
        Objects.equals(this.getFeatureType(), that.getFeatureType()) &&
        Objects.equals(this.getFeatureValue(), that.getFeatureValue()) &&
        Objects.equals(this.getUsageType(), that.getUsageType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getFeatureId(),
                        this.getFeatureType(),
                        this.getFeatureValue(),
                        this.getUsageType());
  }

  @Override
  public String toString() {
    return "SzScoredFeature{" +
        "featureId=" + this.getFeatureId() +
        ", featureType='" + this.getFeatureType() + '\'' +
        ", featureValue='" + this.getFeatureValue() + '\'' +
        ", usageType='" + this.getUsageType() + '\'' +
        '}';
  }
}
