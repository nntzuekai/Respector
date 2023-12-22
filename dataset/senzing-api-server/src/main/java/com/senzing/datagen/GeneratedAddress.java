package com.senzing.datagen;

import javax.json.JsonObjectBuilder;
import java.util.Objects;

import static com.senzing.datagen.FeatureType.ADDRESS;

/**
 * Describes a generated name.
 */
public class GeneratedAddress extends AbstractGeneratedFeature {
  /**
   * The street address.
   */
  private String street;

  /**
   * The city.
   */
  private String city;

  /**
   * The state.
   */
  private String state;

  /**
   * The zip code.
   */
  private String postalCode;

  /**
   * Constructs with the specified non-null address parts.
   *
   * @param recordType The {@link RecordType} describing the type of record
   *                   that the address was generated for.
   * @param street The non-null street address.
   * @param city The non-null city for the address.
   * @param state The non-null state for the address.
   * @param postalCode The non-null postal code for the address.
   * @throws NullPointerException If any of the specified parameters is
   *                              <tt>null</tt>
   */
  public GeneratedAddress(RecordType  recordType,
                          String      street,
                          String      city,
                          String      state,
                          String      postalCode)
  {
    super(ADDRESS, recordType);

    Objects.requireNonNull(street, "The street cannot be null");
    Objects.requireNonNull(city, "The city cannot be null");
    Objects.requireNonNull(state, "The state cannot be null");
    Objects.requireNonNull(postalCode, "The postal code cannot be null");

    this.street     = street;
    this.city       = city;
    this.state      = state;
    this.postalCode = postalCode;
  }

  /**
   * Constructs with the specified properly formatted address.
   * The format is as follows:
   * <pre>
   *   [street address]; [city], [state] [postal-code]
   * </pre>
   *
   * @param recordType The {@link RecordType} describing the type of record
   *                   that the address was generated for.
   * @param fullAddress The non-null full address.
   * @throws NullPointerException If the specified parameter is <tt>null</tt>
   * @throws IllegalArgumentException If the specified parameter is not
   *                                  formatted properly.
   */
  public GeneratedAddress(RecordType recordType, String fullAddress)
  {
    super(ADDRESS, recordType);
    Objects.requireNonNull(fullAddress, "The full address cannot be null");
    String[] tokens = fullAddress.split("[;,]");
    if (tokens.length != 3) {
      throw new IllegalArgumentException(
          "The specified full address is not formatted as expected: "
          + fullAddress);
    }

    // get the street & city
    this.street = tokens[0].trim();
    this.city   = tokens[1].trim();

    // parse the state and zip code
    tokens = tokens[2].trim().split("[\\s]+");
    if (tokens.length != 2) {
      throw new IllegalArgumentException(
          "The specified full address is not formatted as expected: "
          + fullAddress);
    }
    this.state      = tokens[0].trim();
    this.postalCode = tokens[1].trim();
  }

  /**
   * Returns the value for the address attribute type.
   * @param attrType The {@link GeneratedAttributeType} for which the value is
   *                 being requested.
   * @return The value for the attribute type.
   */
  public String doGetValue(GeneratedAttributeType attrType) {
    switch (attrType) {
      case ADDR_FULL:
        return this.getFullValue();

      case ADDR_LINE1:
        return this.getStreet();

      case ADDR_CITY:
        return this.getCity();

      case ADDR_STATE:
        return this.getState();

      case ADDR_POSTAL_CODE:
        return this.getPostalCode();

      default:
        throw new IllegalStateException(
            "Unhandled value for " + attrType.getClass().getName() + ": "
                + attrType);
    }
  }

  /**
   * Gets the street for the address.
   * @return The street for the address.
   */
  public String getStreet() {
    return this.street;
  }

  /**
   * Gets the city for the address.
   * @return The city for the address.
   */
  public String getCity() {
    return this.city;
  }

  /**
   * Gets the state for the address.
   * @return The state for the address.
   */
  public String getState() {
    return this.state;
  }

  /**
   * Gets the postal code for the address.
   * @return The postal code for the address.
   */
  public String getPostalCode() {
    return this.postalCode;
  }

  /**
   * Returns a hash code for this instance.
   */
  public int hashCode() {
    return Objects.hash(this.getStreet(),
                        this.getCity(),
                        this.getState(),
                        this.getPostalCode());
  }

  /**
   * Implemented to check if the address parts are equal.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (this == object) return true;

    GeneratedAddress addr = (GeneratedAddress) object;

    return (Objects.equals(this.getStreet(), addr.getStreet())
        && Objects.equals(this.getCity(), addr.getCity())
        && Objects.equals(this.getState(), addr.getState())
        && Objects.equals(this.getPostalCode(), addr.getPostalCode()));
  }

  /**
   * Returns the full address formatted as:
   * <pre>
   *   [street address]; [city], [state] [postal-code]
   * </pre>
   *
   * @return The formatted full address.
   */
  public String toString() {
    return this.getStreet() + "; " + this.getCity() + ", " + this.getState()
        + " " + this.getPostalCode();
  }
}
