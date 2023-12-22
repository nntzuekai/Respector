package com.senzing.datagen;

import java.util.Objects;

import static com.senzing.datagen.FeatureType.*;
import static com.senzing.datagen.RecordType.*;

/**
 * Describes a generated name.
 */
public class GeneratedName extends AbstractGeneratedFeature {
  /**
   * The given name.
   */
  private String givenName;

  /**
   * The surname.
   */
  private String surname;

  /**
   * The org name.
   */
  private String orgName;

  /**
   * Constructs with the specified given name and surname to create an instance
   * that uses the {@link RecordType#PERSON} record type.
   *
   * @param givenName The given name.
   * @param surname The surname.
   */
  public GeneratedName(String givenName, String surname) {
    super(NAME, PERSON);
    this.givenName  = givenName;
    this.surname    = surname;
    this.orgName    = null;
  }

  /**
   * Constructs with the specified organization name to create an instance that
   * uses the {@link RecordType#BUSINESS} or {@link RecordType#ORGANIZATION}
   * {@link RecordType}.
   *
   * @param orgName The organization name.
   * @param business <tt>true</tt> if the {@link RecordType} is {@link
   *                 RecordType#BUSINESS} and <tt>false</tt> if {@link
   *                 RecordType#ORGANIZATION}.
   */
  public GeneratedName(String orgName, boolean business) {
    super(NAME, (business ? BUSINESS: ORGANIZATION));
    this.orgName    = orgName;
    this.givenName  = null;
    this.surname    = null;
  }

  /**
   * Returns the value for the name attribute type.
   * @param attrType The {@link GeneratedAttributeType} for which the value is
   *                 being requested.
   * @return The value for the attribute type.
   */
  public String doGetValue(GeneratedAttributeType attrType) {
    switch (attrType) {
      case NAME_FIRST:
        return this.getGivenName();

      case NAME_LAST:
        return this.getSurname();

      case NAME_FULL:
        return this.getFullName();

      case NAME_ORG:
        return this.getOrgName();

      default:
        throw new IllegalStateException(
            "Unhandled value for " + attrType.getClass().getName() + ": "
            + attrType);
    }
  }

  /**
   * Returns the associated given name, or <tt>null</tt> if this
   * instance represents an organization name.
   *
   * @return The associated given name, or <tt>null</tt> if this
   *         represents an organization name.
   */
  public String getGivenName() {
    return this.givenName;
  }

  /**
   * Returns the associated surname, or <tt>null</tt> if this
   * instance represents an organization name.
   *
   * @return The associated surname, or <tt>null</tt> if this
   *         represents an organization name.
   */
  public String getSurname() {
    return this.surname;
  }

  /**
   * Gets the full name containing the surname and given name if the associated
   * {@link RecordType} is {@link RecordType#PERSON}, otherwise this returns
   * <tt>null</tt>.
   *
   * @return The full name containing the surname and the given name, or
   *         <tt>null</tt> if the associated {@link RecordType} is <b>not</b>
   *         {@link RecordType#PERSON}.
   */
  public String getFullName() {
    return (this.getRecordType() == PERSON)
        ? (this.getGivenName() + " " + this.getSurname())  : null;
  }

  /**
   * Returns the associated organization name, or <tt>null</tt> if this
   * instance represents an person's name.
   *
   * @return The associated organization name, or <tt>null</tt> if this
   *         represents an person's name.
   */
  public String getOrgName() {
    return this.orgName;
  }

  /**
   * Returns a hash code for this instance.
   */
  public int hashCode() {
    return Objects.hash(
        this.getGivenName(), this.getSurname(), this.getOrgName());
  }

  /**
   * Implemented to check if given name and surname are equal.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (this == object) return true;
    GeneratedName name = (GeneratedName) object;
    return (Objects.equals(this.getGivenName(), name.getGivenName())
            && Objects.equals(this.getSurname(), name.getSurname())
            && Objects.equals(this.getOrgName(), name.getOrgName()));
  }

  /**
   * If this is an organization name then the organization name is returned,
   * otherwise the name is formatted as a full name with given name then a space
   * then surname.
   *
   * @return The formatted organization name or full name.
   */
  public String toString() {
    return (this.getRecordType() == PERSON)
        ? this.getFullName() : this.getOrgName();
  }
}
