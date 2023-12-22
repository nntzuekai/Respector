package com.senzing.api.model;

/**
 * The enumeration of scoring frequency values.
 */
public enum SzScoringFrequency {
  /**
   * The feature value belongs to exactly one entity so if two records share
   * this value they will always merge together.
   */
  ALWAYS_ONE("A1"),

  /**
   * The feature value typically belongs to one entity (like a Social Security
   * Number, Tax ID or Drivers License Number).
   */
  ONE("F1"),

  /**
   * The feature value typically belongs to at most a few entities (like an
   * Address or Phone Number).
   */
  FEW("FF"),

  /**
   * The feature value can belong to many entities (like a date of birth).
   */
  MANY("FM"),

  /**
   * The feature can belong to very many entities (like a gender).
   */
  VERY_MANY("FVM"),

  /**
   * A special frequency used for name features since they have unique
   * properties.
   */
  NAME("NAME");

  /**
   * The frequency code associated with this instance.
   */
  private String code;

  /**
   * Constructs with the specified code.
   *
   * @param code The associated code for the frequency.
   */
  SzScoringFrequency(String code) {
    this.code = code;
  }

  /**
   * Returns the code associated with this frequency.
   *
   * @return The code associated with this frequency.
   */
  public String code() {
    return this.code;
  }

  /**
   * Parses the specified code to determine the {@link SzScoringFrequency} or
   * returns <tt>null</tt> if the specified code is not recognized.
   *
   * @param code The code to parse.
   * @return The {@link SzScoringFrequency} matching the specified code, or
   *         <tt>null</tt> if the code is not recognized.
   */
  public static SzScoringFrequency parse(String code) {
    code = code.toUpperCase();
    for (SzScoringFrequency frequency : SzScoringFrequency.values()) {
      if (frequency.code().equals(code)) return frequency;
    }
    return null;
  }
}
