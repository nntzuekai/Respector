package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

/**
 * Provides a default implementation of {@link SzFeatureScore}.
 */
@JsonDeserialize
public class SzFeatureScoreImpl implements SzFeatureScore {
  /**
   * The feature type of the features being scored.
   */
  private String featureType;

  /**
   * The inbound feature described as an {@link SzScoredFeature}.
   */
  private SzScoredFeature inboundFeature;

  /**
   * The feature that was a candidate match for the inbound feature (also
   * described as an {@link SzScoredFeature}).
   */
  private SzScoredFeature candidateFeature;

  /**
   * The integer score between the two feature values (typically from 0 and 100)
   */
  private Integer score;

  /**
   * The optional name scoring details.
   */
  private SzNameScoring nameScoringDetails;

  /**
   * The {@link SzScoringBucket} describing the meaning of the score.
   */
  private SzScoringBucket scoringBucket;

  /**
   * The {@link SzScoringBehavior} describing the scoring behavior for the
   * features.
   */
  private SzScoringBehavior scoringBehavior;

  /**
   * Default constructor.
   */
  public SzFeatureScoreImpl() {
    this.featureType        = null;
    this.inboundFeature     = null;
    this.candidateFeature   = null;
    this.score              = null;
    this.nameScoringDetails = null;
    this.scoringBucket      = null;
    this.scoringBehavior    = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFeatureType() {
    return featureType;
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
  public SzScoredFeature getInboundFeature() {
    return inboundFeature;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInboundFeature(SzScoredFeature inboundFeature) {
    this.inboundFeature = inboundFeature;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzScoredFeature getCandidateFeature() {
    return candidateFeature;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCandidateFeature(SzScoredFeature candidateFeature) {
    this.candidateFeature = candidateFeature;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getScore() {
    if (this.score != null) return this.score;
    if (this.nameScoringDetails != null) {
      return this.nameScoringDetails.asFullScore();
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setScore(Integer score) {
    this.score = score;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzNameScoring getNameScoringDetails() {
    return this.nameScoringDetails;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setNameScoringDetails(SzNameScoring scoring) {
    this.nameScoringDetails = scoring;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzScoringBucket getScoringBucket() {
    return scoringBucket;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setScoringBucket(SzScoringBucket scoringBucket) {
    this.scoringBucket = scoringBucket;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzScoringBehavior getScoringBehavior() {
    return scoringBehavior;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setScoringBehavior(SzScoringBehavior scoringBehavior) {
    this.scoringBehavior = scoringBehavior;
  }
}
