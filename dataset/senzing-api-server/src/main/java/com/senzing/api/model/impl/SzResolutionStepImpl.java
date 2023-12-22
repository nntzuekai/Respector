package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzHowMatchInfo;
import com.senzing.api.model.SzResolutionStep;
import com.senzing.api.model.SzVirtualEntity;

import java.util.*;

/**
 * Describes a resolution step for a "how entity" result.
 */
@JsonDeserialize
public class SzResolutionStepImpl implements SzResolutionStep {
  /**
   * The step number for this step.
   */
  private int stepNumber = 0;

  /**
   * The {@link SzVirtualEntity} describing the inbound virtual entity.
   */
  private SzVirtualEntity inboundVirtualEntity;

  /**
   * The {@link SzVirtualEntity} describing the candidate virtual entity.
   */
  private SzVirtualEntity candidateVirtualEntity;

  /**
   * The {@link SzHowMatchInfo} describing how the two virtual entities
   * resolved.
   */
  private SzHowMatchInfo matchInfo;

  /**
   * The virtual entity ID of the resolved virtual entity that is the result
   * of this resolution step.
   */
  private String resolvedVirtualEntityId;

  /**
   * Default constructor.  This construct with a step number of zero (0).
   */
  public SzResolutionStepImpl() {
    this(0);
  }

  /**
   * Constructs with the specified step number.
   *
   * @param stepNumber The step number with which to construct.
   */
  public SzResolutionStepImpl(int stepNumber) {
    this.stepNumber = stepNumber;
  }

  @Override
  public int getStepNumber() {
    return this.stepNumber;
  }

  @Override
  public void setStepNumber(int stepNumber) {
    this.stepNumber = stepNumber;
  }

  @Override
  public SzVirtualEntity getInboundVirtualEntity() {
    return this.inboundVirtualEntity;
  }

  @Override
  public void setInboundVirtualEntity(SzVirtualEntity virtualEntity) {
    this.inboundVirtualEntity = virtualEntity;
  }

  @Override
  public SzVirtualEntity getCandidateVirtualEntity() {
    return this.candidateVirtualEntity;
  }

  @Override
  public void setCandidateVirtualEntity(SzVirtualEntity virtualEntity) {
    this.candidateVirtualEntity = virtualEntity;
  }

  @Override
  public SzHowMatchInfo getMatchInfo() {
    return this.matchInfo;
  }

  @Override
  public void setMatchInfo(SzHowMatchInfo matchInfo) {
    this.matchInfo = matchInfo;
  }

  @Override
  public String getResolvedVirtualEntityId() {
    return this.resolvedVirtualEntityId;
  }

  @Override
  public void setResolvedVirtualEntityId(String virtualEntityId) {
    this.resolvedVirtualEntityId = virtualEntityId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SzResolutionStepImpl that = (SzResolutionStepImpl) o;
    return this.getStepNumber() == that.getStepNumber()
        && Objects.equals(that.getInboundVirtualEntity(),
                          that.getInboundVirtualEntity())
        && Objects.equals(this.getCandidateVirtualEntity(),
                          that.getCandidateVirtualEntity())
        && Objects.equals(this.getMatchInfo(),
                          that.getMatchInfo())
        && Objects.equals(this.getResolvedVirtualEntityId(),
                          that.getResolvedVirtualEntityId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getStepNumber(),
                        this.getInboundVirtualEntity(),
                        this.getCandidateVirtualEntity(),
                        this.getMatchInfo(),
                        this.getResolvedVirtualEntityId());
  }

  @Override
  public String toString() {
    return "SzResolutionStepImpl{" +
        "stepNumber=[ " + this.getStepNumber()
        + " ], inboundVirtualEntity=[ " + this.getInboundVirtualEntity()
        + " ], candidateVirtualEntity=[ " + this.getCandidateVirtualEntity()
        + " ], matchInfo=[ " + this.getMatchInfo()
        + " ], resolvedVirtualEntityId=[ " + this.getResolvedVirtualEntityId()
        + " ]}";
  }
}
