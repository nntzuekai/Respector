package com.senzing.datagen;

/**
 * Describes how densely populated the values for a given feature type should
 * be generated.
 */
public enum FeatureDensity {
  /**
   * Values for all usage types should be generated.
   */
  GUARANTEED(1.0,1.0),

  /**
   * The value for the first usage type should be generated with the remaining
   * being commonly generated.
   */
  FIRST_THEN_COMMON(1.0, 0.8),

  /**
   * The value for the first usage type should be generated with the remaining
   * being uncommonly generated.
   */
  FIRST_THEN_UNCOMMON(1.0, 0.30),

  /**
   * The value for the first usage type should be generated with the remaining
   * being sparsely generated.
   */
  FIRST_THEN_SPARSE(1.0, 0.15),

  /**
   * The value for each usage type should be commonly generated so that each
   * value is likely to exist.
   */
  COMMON(0.75, 0.8),

  /**
   * The value for each usage type should be uncommonly generated so that each
   * value is less likely to exist.
   */
  UNCOMMON(0.30, 0.30),

  /**
   * The values for each usage type has a low probability of being generated.
   */
  SPARSE(0.15, 0.15);

  /**
   * The probability that the value for the first usage type should be
   * generated.
   */
  private double primaryProbability;

  /**
   * The probability that the value for the subsequent usage types after the
   * first should be generated.
   */
  private double subsequentProbability;

  /**
   * Constructs with the specified parameters.
   */
  FeatureDensity(double primaryProbability, double subsequentProbability) {
    this.primaryProbability     = primaryProbability;
    this.subsequentProbability  = subsequentProbability;
  }

  /**
   * Gets the probability between zero (0.0) and one (1.0) that the value with
   * the specified index should be generated.
   *
   * @param index The zero-based index of the value being generated.
   *
   * @return The probability between zero (0.0) and one (1.0) that the value with
   *         the specified index should be generated.
   */
  public double probability(int index) {
    return (index == 0) ? this.primaryProbability : this.subsequentProbability;
  }
}
