package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzRelatedFeatures;
import com.senzing.api.model.SzScoredFeature;
import java.util.Objects;

/**
 * Provides a default implementation of {@link SzRelatedFeatures}.
 */
@JsonDeserialize
public class SzRelatedFeaturesImpl implements SzRelatedFeatures {
  /**
   * The feature belonging to the first entity.
   */
  private SzScoredFeature feature1;

  /**
   * The feature belonging to the second entity.
   */
  private SzScoredFeature feature2;

  /**
   * Default constructor.
   */
  public SzRelatedFeaturesImpl() {
    this.feature1 = null;
    this.feature2 = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzScoredFeature getFeature1() {
    return this.feature1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFeature1(SzScoredFeature feature) {
    this.feature1 = feature;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzScoredFeature getFeature2() {
    return this.feature2;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFeature2(SzScoredFeature feature) {
    this.feature2 = feature;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    SzRelatedFeaturesImpl that = (SzRelatedFeaturesImpl) object;
    return Objects.equals(this.getFeature1(), that.getFeature1()) &&
        Objects.equals(this.getFeature2(), that.getFeature2());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getFeature1(), this.getFeature2());
  }

  @Override
  public String toString() {
    return "SzRelatedFeatures{" +
        "feature1=" + this.getFeature1() +
        ", feature2=" + this.getFeature2() +
        '}';
  }
}
