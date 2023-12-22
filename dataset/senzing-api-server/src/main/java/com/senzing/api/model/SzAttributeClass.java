package com.senzing.api.model;

/**
 * Enumerates the various classes for attribute types.
 *
 */
public enum SzAttributeClass {
  /**
   * The attribute pertains to an address (like a postal code, city or
   * country).
   */
  ADDRESS,

  /**
   * The attribute pertains to a characteristic feature which is typically
   * an unchanging or long-lived physical attribute (like a birth date).
   */
  CHARACTERISTIC,

  /**
   * The attribute pertain to an identifier feature like a drivers license number,
   * passport number or email address.
   */
  IDENTIFIER,

  /**
   * The attribute pertains to a name (like a given name or surname).
   */
  NAME,

  /**
   * The attribute pertains record level meta information such as a data source,
   * record ID, or load ID.  Such attributes are typically not real-life
   *
   */
  OBSERVATION,

  /**
   * The attribute pertains to a phone number (like an area code, phone
   * exchange or extension).
   */
  PHONE,

  /**
   * The attribute pertains to describing a relationship (such as a relationship
   * type to indicate a spouse or sibling or co-worker).
   */
  RELATIONSHIP,

  /**
   * The attribute pertains to a custom feature or an attribute that is
   * included in the load for informational purposes, but not mapped otherwise.
   */
  OTHER;

  /**
   * The RAW API value associated with this attribute class.
   *
   * @return The RAW API value associated with this instance.
   */
  public String getRawValue() {
    if (this == CHARACTERISTIC) return "ATTRIBUTE";
    return this.toString();
  }

  /**
   * Converts the raw JSON Senzing attribute class names to the user-facing
   * enumerated type.  If the specified text is <tt>null</tt> or empty string
   * then <tt>null</tt> is returned.
   *
   * @param attrClass The attribute class text to parse as an instance of
   *                  {@link SzAttributeClass}
   *
   * @return The {@link SzAttributeClass} corresponding to the specified text,
   *         or <tt>null</tt> if the specified text is <tt>null</tt> or
   *         empty-string.
   *
   * @throws IllegalArgumentException If the specified text is not recognized.
   */
  public static SzAttributeClass parseAttributeClass(String attrClass) {
    if (attrClass == null) return null;
    attrClass = attrClass.trim().toUpperCase();
    if (attrClass.length() == 0) return null;
    if (attrClass.equals("ATTRIBUTE")) return CHARACTERISTIC;
    return SzAttributeClass.valueOf(attrClass);
  }
}
