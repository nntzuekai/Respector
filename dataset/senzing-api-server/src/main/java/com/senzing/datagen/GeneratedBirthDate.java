package com.senzing.datagen;

import java.util.Objects;

import static com.senzing.datagen.FeatureType.BIRTH_DATE;
import static com.senzing.datagen.RecordType.PERSON;

/**
 * Describes a generated birth date.
 */
public class GeneratedBirthDate extends AbstractGeneratedFeature {
  /**
   * The month.
   */
  private Integer month;

  /**
   * The day of month.
   */
  private Integer day;

  /**
   * The year.
   */
  private Integer year;

  /**
   * Constructs with the specified phone number value.
   * @param month The month number of the year between 1 and 12, inclusive,
   *              or <tt>null</tt> if the month is not known.
   * @param day The day of the month between 1 and 31, inclusive, or
   *            <tt>null</tt> if the day is not known.
   * @param year The year or <tt>null</tt> if the year is not known.
   */
  public GeneratedBirthDate(Integer month, Integer day, Integer year) {
    super(BIRTH_DATE, PERSON);
    this.month = month;
    this.day   = day;
    this.year  = year;
  }

  /**
   * Returns the value for the phone attribute type.
   * @param attrType The {@link GeneratedAttributeType} for which the value is
   *                 being requested.
   * @return The value for the attribute type.
   */
  public String doGetValue(GeneratedAttributeType attrType) {
    switch (attrType) {
      case DATE_OF_BIRTH:
        return this.getDateOfBirth();

      default:
        throw new IllegalStateException(
            "Unhandled value for " + attrType.getClass().getName() + ": "
                + attrType);
    }
  }

  /**
   * Gets the phone number value.
   * @return The phone number value.
   */
  public String getDateOfBirth() {
    StringBuilder sb = new StringBuilder();
    String sep = "";
    if (this.year != null) {
      sb.append(this.year);
      sep = "-";
    }
    if (this.month != null) {
      sb.append(sep);
      sb.append(this.month);
      sep = "-";
    }
    if (this.day != null) {
      sb.append(sep);
      sb.append(this.day);

    }
    return sb.toString();
  }

  /**
   * Returns a hash code for this instance.
   */
  public int hashCode() {
    return Objects.hash(this.year, this.month, this.day);
  }

  /**
   * Implemented to check if the dates of birth are equal.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (this == object) return true;
    GeneratedBirthDate dob = (GeneratedBirthDate) object;
    return Objects.equals(this.year, dob.year)
           && Objects.equals(this.month, dob.month)
           && Objects.equals(this.day, dob.day);
  }

  /**
   * Returns the date of birth value.
   * @return The date of birth value.
   */
  public String toString() {
    return this.getDateOfBirth();
  }
}
