package com.senzing.datagen;

import java.util.Objects;

import static com.senzing.datagen.FeatureType.EMAIL;

/**
 * Describes a generated email address.
 */
public class GeneratedEmail extends AbstractGeneratedFeature {
  /**
   * The email address.
   */
  private String emailAddress;

  /**
   * Constructs with the specified email address value.
   * @param recordType The {@link RecordType} describing the type of record
   *                   that the email address was generated for.
   * @param emailAddress The value for the email address.
   */
  public GeneratedEmail(RecordType recordType, String emailAddress) {
    super(EMAIL, recordType);
    this.emailAddress = emailAddress;
  }

  /**
   * Returns the value for the email attribute type.
   * @param attrType The {@link GeneratedAttributeType} for which the value is
   *                 being requested.
   * @return The value for the attribute type.
   */
  public String doGetValue(GeneratedAttributeType attrType) {
    switch (attrType) {
      case EMAIL_ADDRESS:
        return this.getEmailAddress();

      default:
        throw new IllegalStateException(
            "Unhandled value for " + attrType.getClass().getName() + ": "
                + attrType);
    }
  }

  /**
   * Gets the email address value.
   * @return The email address value.
   */
  public String getEmailAddress() {
    return this.emailAddress;
  }

  /**
   * Returns a hash code for this instance.
   */
  public int hashCode() {
    return Objects.hash(this.getEmailAddress());
  }

  /**
   * Implemented to check if the email addresses are equal.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (this == object) return true;
    GeneratedEmail email = (GeneratedEmail) object;
    return Objects.equals(this.getEmailAddress(), email.getEmailAddress());
  }

  /**
   * Returns the email address value.
   * @return The email address value.
   */
  public String toString() {
    return this.getEmailAddress();
  }
}
