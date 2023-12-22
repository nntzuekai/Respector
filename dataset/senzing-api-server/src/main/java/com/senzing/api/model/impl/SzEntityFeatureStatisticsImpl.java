package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityFeatureStatistics;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;

/**
 * Describes the entity resolution statistics for the feature value.
 */
@JsonDeserialize
public class SzEntityFeatureStatisticsImpl
    implements SzEntityFeatureStatistics
{
  /**
   * Indicates if the feature is used for finding candidates during entity
   * resolution.
   */
  private boolean usedForCandidates;

  /**
   * Indicates if the feature is used for scoring during entity resolution.
   */
  private Boolean usedForScoring;

  /**
   * The number of entities having this feature value.
   */
  private Long entityCount;

  /**
   * Indicates if this feature value is no longer being used to find candidates
   * because too many entities share the same value.
   */
  private Boolean candidateCapReached;

  /**
   * Indicates if this feature value is no longer being used in entity
   * scoring because too many entities share the same value.
   */
  private Boolean scoringCapReached;

  /**
   * Indicates if this value was suppressed in favor of a more complete value.
   */
  private Boolean suppressed;

  /**
   * Default constructor.
   */
  public SzEntityFeatureStatisticsImpl() {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean isUsedForCandidates() {
    return usedForCandidates;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUsedForCandidates(Boolean usedForCandidates) {
    this.usedForCandidates = usedForCandidates;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean isUsedForScoring() {
    return usedForScoring;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUsedForScoring(Boolean usedForScoring) {
    this.usedForScoring = usedForScoring;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getEntityCount() {
    return entityCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntityCount(Long entityCount) {
    this.entityCount = entityCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean isCandidateCapReached() {
    return candidateCapReached;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCandidateCapReached(Boolean candidateCapReached) {
    this.candidateCapReached = candidateCapReached;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean isScoringCapReached() {
    return scoringCapReached;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setScoringCapReached(Boolean scoringCapReached) {
    this.scoringCapReached = scoringCapReached;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean isSuppressed() {
    return suppressed;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSuppressed(Boolean suppressed) {
    this.suppressed = suppressed;
  }

  @Override
  public String toString() {
    return "SzEntityFeatureStatistics{" +
        "usedForCandidates=" + usedForCandidates +
        ", usedForScoring=" + usedForScoring +
        ", entityCount=" + entityCount +
        ", candidateCapReached=" + candidateCapReached +
        ", scoringCapReached=" + scoringCapReached +
        ", suppressed=" + suppressed +
        '}';
  }
}
