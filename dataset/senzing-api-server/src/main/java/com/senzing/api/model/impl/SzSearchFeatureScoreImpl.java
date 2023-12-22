package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzNameScoring;
import com.senzing.api.model.SzScoredFeature;
import com.senzing.api.model.SzSearchFeatureScore;

/**
 * Describes the scoring between {@link SzScoredFeature} instances.
 */
@JsonDeserialize
public class SzSearchFeatureScoreImpl implements SzSearchFeatureScore {
  /**
   * The feature type of the features being scored.
   */
  private String featureType;

  /**
   * The inbound feature value as a {@link String}.
   */
  private String inboundFeature;

  /**
   * The feature value that was a candidate match for the inbound feature as a
   * {@link String}.
   */
  private String candidateFeature;

  /**
   * The integer score between the two feature values (typically from 0 and 100)
   */
  private Integer score;

  /**
   * The optional name scoring details.
   */
  private SzNameScoring nameScoringDetails;

  /**
   * Default constructor.
   */
  public SzSearchFeatureScoreImpl() {
    this.featureType        = null;
    this.inboundFeature     = null;
    this.candidateFeature   = null;
    this.score              = null;
    this.nameScoringDetails = null;
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
  public String getInboundFeature() {
    return this.inboundFeature;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInboundFeature(String inboundFeature) {
    this.inboundFeature = inboundFeature;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCandidateFeature() {
    return this.candidateFeature;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCandidateFeature(String candidateFeature) {
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
}
