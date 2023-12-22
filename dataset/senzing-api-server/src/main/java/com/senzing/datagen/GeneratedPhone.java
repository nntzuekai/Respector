package com.senzing.datagen;

import java.util.Objects;

import static com.senzing.datagen.FeatureType.PHONE;

/**
 * Describes a generated phone number.
 */
public class GeneratedPhone extends AbstractGeneratedFeature {
  /**
   * The phone number.
   */
  private String phoneNumber;

  /**
   * Constructs with the specified phone number value.
   * @param recordType The {@link RecordType} describing the type of record
   *                   that the phone number was generated for.
   * @param phoneNumber The value for the phone number.
   */
  public GeneratedPhone(RecordType recordType, String phoneNumber) {
    super(PHONE, recordType);
    this.phoneNumber = phoneNumber;
  }

  /**
   * Returns the value for the phone attribute type.
   * @param attrType The {@link GeneratedAttributeType} for which the value is
   *                 being requested.
   * @return The value for the attribute type.
   */
  public String doGetValue(GeneratedAttributeType attrType) {
    switch (attrType) {
      case PHONE_NUMBER:
        return this.getPhoneNumber();

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
  public String getPhoneNumber() {
    return this.phoneNumber;
  }

  /**
   * Returns a hash code for this instance.
   */
  public int hashCode() {
    return Objects.hash(this.getPhoneNumber());
  }

  /**
   * Implemented to check if the phone numbers are equal.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (this == object) return true;
    GeneratedPhone phone = (GeneratedPhone) object;
    return Objects.equals(this.getPhoneNumber(), phone.getPhoneNumber());
  }

  /**
   * Returns the phone number value.
   * @return The phone number value.
   */
  public String toString() {
    return this.getPhoneNumber();
  }
}
