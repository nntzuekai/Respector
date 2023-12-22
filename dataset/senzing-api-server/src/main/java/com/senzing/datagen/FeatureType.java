package com.senzing.datagen;

/**
 * Enumerates the feature types for data generation.
 */
public enum FeatureType {
  /**
   * Represents name features.
   */
  NAME("NAMES"),

  /**
   * Represents address features.
   */
  ADDRESS("ADDRESSES"),

  /**
   * Represents phone features.
   */
  PHONE("PHONES"),

  /**
   * Represents email features.
   */
  EMAIL("EMAILS"),

  /**
   * Represents birth date features.
   */
  BIRTH_DATE(null);

  /**
   * The plural property name for hierarchical JSON.
   */
  private String pluralProperty;

  /**
   * Constructs with the specified parameters.
   *
   * @param pluralProperty The property to use for arrays of values for this
   *                       feature in hierarchical JSON.
   */
  FeatureType(String pluralProperty) {
    this.pluralProperty   = pluralProperty;
  }

  /**
   * Returns the property name to use for arrays of values of this feature type
   * when creating hierarchical JSON.
   * @return The property name to use for arrays of values of this feature type
   *         when creating hierarchical JSON.
   */
  public String getPluralProperty() {
    return this.pluralProperty;
  }
}
