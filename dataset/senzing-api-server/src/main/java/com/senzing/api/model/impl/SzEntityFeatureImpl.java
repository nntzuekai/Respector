package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityFeature;
import com.senzing.api.model.SzEntityFeatureDetail;
import java.util.*;

/**
 * Describes a feature for an entity.
 */
@JsonDeserialize
public class SzEntityFeatureImpl implements SzEntityFeature {
  /**
   * The internal ID of the primary feature value.
   */
  private Long primaryId;

  /**
   * The primary value for the feature.
   */
  private String primaryValue;

  /**
   * The usage type associated with the feature.
   */
  private String usageType;

  /**
   * The set of duplicate values.
   */
  private Set<String> duplicateValues;

  /**
   * The {@link List} of {@link SzEntityFeatureDetail} instances describing
   * the details of each of the clustered feature values for this feature.
   */
  private List<SzEntityFeatureDetail> featureDetails;

  /**
   * Default constructor.
   */
  public SzEntityFeatureImpl() {
    this.primaryId        = null;
    this.primaryValue     = null;
    this.usageType        = null;
    this.duplicateValues  = new LinkedHashSet<>();
    this.featureDetails   = new LinkedList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getPrimaryId() {
    return this.primaryId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setPrimaryId(Long primaryId) {
    this.primaryId = primaryId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getPrimaryValue() {
    return primaryValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setPrimaryValue(String primaryValue) {
    this.primaryValue = primaryValue;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getDuplicateValues() {
    return Collections.unmodifiableSet(this.duplicateValues);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDuplicateValues(Collection<String> duplicateValues) {
    this.duplicateValues.clear();
    if (duplicateValues != null) {
      this.duplicateValues.addAll(duplicateValues);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addDuplicateValue(String value)
  {
    this.duplicateValues.add(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzEntityFeatureDetail> getFeatureDetails() {
    return Collections.unmodifiableList(this.featureDetails);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFeatureDetails(Collection<SzEntityFeatureDetail> details) {
    this.featureDetails.clear();
    if (details != null) {
      this.featureDetails.addAll(details);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFeatureDetail(SzEntityFeatureDetail featureDetail) {
    this.featureDetails.add(featureDetail);
  }

  @Override
  public String toString() {
    return "SzEntityFeature{" +
        "primaryId=" + primaryId +
        ", primaryValue='" + primaryValue + '\'' +
        ", usageType='" + usageType + '\'' +
        ", duplicateValues=" + duplicateValues +
        '}';
  }
}
