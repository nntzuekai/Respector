package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzScoringBehavior;
import com.senzing.api.model.SzScoringFrequency;

import java.util.Objects;
import static com.senzing.api.model.SzScoringBehavior.computeCode;

/**
 * Provides the default implementation of {@link SzScoringBehavior}.
 */
@JsonDeserialize
public class SzScoringBehaviorImpl implements SzScoringBehavior {
  /**
   * The code for the scoring behavior.
   */
  private String code;

  /**
   * The scoring frequency for the behavior.
   */
  private SzScoringFrequency frequency;

  /**
   * Whether or not the feature is considered exclusive for scoring purposes.
   */
  private boolean exclusive;

  /**
   * Whether or not the feature is considered stable for scoring purposes.
   */
  private boolean stable;

  /**
   * Constructs with the specified {@link SzScoringFrequency}, exclusivity
   * and stability flags.
   *
   * @param frequency The {@link SzScoringFrequency} for the scoring behavior.
   * @param exclusive <tt>true</tt> if the feature value is considered
   *                  exclusive, and <tt>false</tt> if not.
   * @param stable <tt>true</tt> if the feature value is considered stable,
   *               and <tt>false</tt> if not.
   */
  public SzScoringBehaviorImpl(SzScoringFrequency frequency,
                               boolean            exclusive,
                               boolean            stable)
  {
    if (frequency == null) {
      throw new NullPointerException(
          "The specified frequency cannot be null");
    }
    this.code       = computeCode(frequency, exclusive, stable);
    this.frequency  = frequency;
    this.exclusive  = exclusive;
    this.stable     = stable;
  }

  /**
   * Private default constructor for JSON marshalling.
   */
  private SzScoringBehaviorImpl() {
    this.code       = null;
    this.frequency  = null;
    this.exclusive  = false;
    this.stable     = false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCode() {
    return this.code;
  }

  /**
   * Private setter for JSON marshalling.
   */
  private void setCode(String code) {
    this.code = code;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzScoringFrequency getFrequency() {
    return this.frequency;
  }

  /**
   * Private setter for JSON marshalling.
   */
  private void setFrequency(SzScoringFrequency frequency) {
    this.frequency = frequency;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isExclusive() {
    return this.exclusive;
  }

  /**
   * Private setter for JSON marshalling.
   */
  private void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isStable() {
    return this.stable;
  }

  /**
   * Private setter for JSON marshalling.
   */
  private void setStable(boolean stable) {
    this.exclusive = stable;
  }

  /**
   * Implemented to check if two scoring behavior instances are equal.
   *
   * @param object The object to compare against.
   *
   * @return <tt>true</tt> if the objects are equal, otherwise <tt>false</tt>.
   */
  @Override
  public boolean equals(Object object) {
    if (object == null) return false;
    if (this == object) return true;
    if (this.getClass() != object.getClass()) return false;
    SzScoringBehaviorImpl behavior = (SzScoringBehaviorImpl) object;
    return (this.getFrequency().equals(behavior.getFrequency())
            && (this.isExclusive() == behavior.isExclusive())
            && (this.isStable() == behavior.isStable()));
  }

  /**
   * Implemented to implement a hash code that is consistent with the
   * {@link #equals(Object)} implementation.
   *
   * @return The hash code for this instance.
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.getFrequency(),
                        this.isExclusive(),
                        this.isStable());
  }

  /**
   * Implemented to return the text code for this instance.
   *
   * @return The text code for this instance.
   */
  @Override
  public String toString() {
    return this.getCode();
  }
}
