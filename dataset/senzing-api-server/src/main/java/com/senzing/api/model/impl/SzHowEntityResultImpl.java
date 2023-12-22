package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzHowEntityResult;
import com.senzing.api.model.SzResolutionStep;
import com.senzing.api.model.SzVirtualEntity;

import java.util.*;

/**
 * Describes a virtual entity.
 */
@JsonDeserialize
public class SzHowEntityResultImpl implements SzHowEntityResult {
  /**
   * The {@link List} of {@link SzVirtualEntity} instances describing the final
   * states for entity resolution.  If more than one, then reevaluation of the
   * entity is required.
   */
  private List<SzVirtualEntity> finalStates;

  /**
   * The {@link Map} of {@link String} virtual entity ID keys to {@link
   * SzResolutionStep} values for the respective step that resolved the virtual
   * entity with the same virtual entity ID.
   */
  private Map<String, SzResolutionStep> steps;

  /**
   * Default constructor.
   */
  public SzHowEntityResultImpl() {
    this.finalStates  = new LinkedList<>();
    this.steps        = new LinkedHashMap<>();
  }

  @Override
  public List<SzVirtualEntity> getFinalStates() {
    return Collections.unmodifiableList(this.finalStates);
  }

  @Override
  public void addFinalState(SzVirtualEntity finalState) {
    this.finalStates.add(finalState);
  }

  @Override
  public void setFinalStates(Collection<SzVirtualEntity> finalStates) {
    this.finalStates.clear();
    if (finalStates != null) {
      this.finalStates.addAll(finalStates);
    }
  }

  @Override
  public Map<String, SzResolutionStep> getResolutionSteps() {
    return Collections.unmodifiableMap(this.steps);
  }

  /**
   * Private setter for deserialization.
   *
   * @param stepMap The {@link Map} of {@link String} virtual entity ID keys to
   *                {@link SzResolutionStep} instances.
   */
  @JsonSetter("resolutionSteps")
  private void setResolutionSteps(Map<String, SzResolutionStep> stepMap) {
    this.setResolutionSteps(stepMap == null ? null : stepMap.values());
  }

  @Override
  public void addResolutionStep(SzResolutionStep step) {
    this.steps.put(step.getResolvedVirtualEntityId(), step);
  }

  @Override
  public void setResolutionSteps(Collection<SzResolutionStep> steps) {
    this.steps.clear();
    if (steps != null) {
      for (SzResolutionStep step : steps) {
        this.steps.put(step.getResolvedVirtualEntityId(), step);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SzHowEntityResultImpl that = (SzHowEntityResultImpl) o;
    return Objects.equals(this.getFinalStates(), that.getFinalStates())
        && Objects.equals(this.getResolutionSteps(), that.getResolutionSteps());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getFinalStates(), this.getResolutionSteps());
  }

  @Override
  public String toString() {
    return "SzHowEntityResultImpl{" +
        "finalStates=[ " + finalStates
        + " ], resolutionSteps=[ " + steps
        + " ]}";
  }
}
