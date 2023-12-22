package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityFeatureDetail;
import com.senzing.api.model.SzEntityFeatureStatistics;

/**
 * Describes the details of an entity feature value, optionally including
 * statistics if they have been requested.
 */
@JsonDeserialize
public class SzEntityFeatureDetailImpl implements SzEntityFeatureDetail {
  /**
   * The internal ID for the feature value.
   */
  private Long internalId;

  /**
   * The feature value.
   */
  private String featureValue;

  /**
   * The {@link SzEntityFeatureStatistics} describing the statistics for the
   * feature value.  This may be <tt>null</tt> if the statistics were not
   * requested.
   */
  private SzEntityFeatureStatistics statistics;

  /**
   * Default constructor.
   */
  public SzEntityFeatureDetailImpl() {
    this.internalId   = null;
    this.featureValue = null;
    this.statistics   = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getInternalId() {
    return internalId;
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
  public String getFeatureValue() {
    return featureValue;
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
  public SzEntityFeatureStatistics getStatistics() {
    return statistics;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setStatistics(SzEntityFeatureStatistics statistics) {
    this.statistics = statistics;
  }

  @Override
  public String toString() {
    return "SzEntityFeatureDetail{" +
        "internalId=" + internalId +
        ", featureValue='" + featureValue + '\'' +
        ", statistics=" + statistics +
        '}';
  }
}
