package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzLicenseInfoImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Describes a Senzing license.
 */
@JsonDeserialize(using=SzLicenseInfo.Factory.class)
public interface SzLicenseInfo {
  /**
   * Gets the customer string associated with the license.
   *
   * @return The customer string associated with the license.
   */
  String getCustomer();

  /**
   * Sets the customer string associated with the license.
   *
   * @param customer The customer string associated with the license.
   */
  void setCustomer(String customer);

  /**
   * Gets the contract string associated with the license.
   *
   * @return The contract string associated with the license.
   */
  String getContract();

  /**
   * Sets the contract string associated with the license.
   *
   * @param contract The contract string associated with the license.
   */
  void setContract(String contract);

  /**
   * Gets the license type associated with the license.
   *
   * @return The license type associated with the license.
   */
  String getLicenseType();

  /**
   * Sets the license type associated with the license.
   *
   * @param licenseType The license type associated with the license.
   */
  void setLicenseType(String licenseType);

  /**
   * Gets the license level associated with the license.
   *
   * @return The license level associated with the license.
   */
  String getLicenseLevel();

  /**
   * Sets the license level associated with the licenese.
   *
   * @param licenseLevel The license level associated with the license.
   */
  void setLicenseLevel(String licenseLevel);

  /**
   * Gets the billing string associated with the license.
   *
   * @return The billing string associated with the license.
   */
  String getBilling();

  /**
   * Sets the billing string associated with the license.
   *
   * @param billing The billing string associated with the license.
   */
  void setBilling(String billing);

  /**
   * Gets the issuance {@link Date} associated with the license.
   *
   * @return The issuance date associated with the license.
   */
  @JsonFormat(shape   = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
      locale  = "en_GB")
  Date getIssuanceDate();

  /**
   * Sets the issuance {@link Date} associated with the license.
   *
   * @param issuanceDate The issuance {@link Date} to be associated with the
   *                     license.
   */
  void setIssuanceDate(Date issuanceDate);

  /**
   * Gets the expiration {@link Date} associated with the license.
   *
   * @return The expiration {@link Date} associated with the license.
   */
  @JsonFormat(shape   = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
      locale  = "en_GB")
  Date getExpirationDate();

  /**
   * Sets the expiration date associated with the license.
   *
   * @param expirationDate The expiration date associated with the license.
   */
  void setExpirationDate(Date expirationDate);

  /**
   * Gets the record limit associated with the license.
   *
   * @return The record limit associated with the license.
   */
  long getRecordLimit();

  /**
   * Sets the record limit associated with the license.
   *
   * @param recordLimit The record limit associated with the license.
   */
  void setRecordLimit(long recordLimit);

  /**
   * A {@link ModelProvider} for instances of {@link SzLicenseInfo}.
   */
  interface Provider extends ModelProvider<SzLicenseInfo> {
    /**
     * Creates a new instance of {@link SzLicenseInfo}.
     *
     * @return The new instance of {@link SzLicenseInfo}
     */
    SzLicenseInfo create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzLicenseInfo} that produces instances of {@link SzLicenseInfoImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzLicenseInfo>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzLicenseInfo.class, SzLicenseInfoImpl.class);
    }

    @Override
    public SzLicenseInfo create() {
      return new SzLicenseInfoImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzLicenseInfo}.
   */
  class Factory extends ModelFactory<SzLicenseInfo, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzLicenseInfo.class);
    }

    /**
     * Constructs with the default provider.  This constructor is private and
     * is used for the master singleton instance.
     * @param defaultProvider The default provider.
     */
    private Factory(Provider defaultProvider) {
      super(defaultProvider);
    }

    /**
     * Creates a new instance of {@link SzLicenseInfo}.
     * @return The new instance of {@link SzLicenseInfo}.
     */
    public SzLicenseInfo create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a JSON array of the engine API JSON to create or populate a
   * {@link List} of {@link com.senzing.api.model.SzLicenseInfo} instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new
   *             {@link List} should be created.
   *
   * @param jsonArray The {@link JsonArray} of {@link JsonObject} instances
   *                  to parse from the engine API.
   *
   * @return An unmodifiable view of the specified (or newly created) {@link
   *         List} of {@link com.senzing.api.model.SzLicenseInfo} instances.
   */
  static List<SzLicenseInfo> parseLicenseInfoList(List<SzLicenseInfo> list,
                                                  JsonArray           jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseLicenseInfo(null, jsonObject));
    }
    return list;
  }

  /**
   * Parses the engine API JSON to create an instance of {@link com.senzing.api.model.SzLicenseInfo}.
   *
   * @param info The {@link com.senzing.api.model.SzLicenseInfo} object to initialize or <tt>null</tt>
   *             if a new one should be created.
   *
   * @param jsonObject The {@link JsonObject} to parse from the engine API.
   *
   * @return The specified (or newly created) {@link com.senzing.api.model.SzLicenseInfo}
   */
  static SzLicenseInfo parseLicenseInfo(SzLicenseInfo info,
                                        JsonObject    jsonObject)
  {
    if (info == null) info = SzLicenseInfo.FACTORY.create();

    String customer     = JsonUtilities.getString(jsonObject, "customer");
    String contract     = JsonUtilities.getString(jsonObject, "contract");
    String issueDate    = JsonUtilities.getString(jsonObject, "issueDate");
    String licenseType  = JsonUtilities.getString(jsonObject, "licenseType");
    String licenseLevel = JsonUtilities.getString(jsonObject, "licenseLevel");
    String billing      = JsonUtilities.getString(jsonObject, "billing");
    String expireDate   = JsonUtilities.getString(jsonObject, "expireDate");
    Long   recordLimit  = JsonUtilities.getLong(jsonObject, "recordLimit");

    ZoneId defaultZone = ZoneId.systemDefault();

    Date issuanceDate = null;
    if (issueDate != null && issueDate.length() > 0) {
      LocalDate localDate = LocalDate.parse(issueDate);
      LocalDateTime localDateTime = localDate.atStartOfDay();
      Instant instant = localDateTime.atZone(defaultZone).toInstant();
      issuanceDate = Date.from(instant);
    }

    Date expirationDate = null;
    if (expireDate != null && expireDate.length() > 0) {
      LocalDate localDate = LocalDate.parse(expireDate);
      LocalDateTime localDateTime = localDate.atTime(23,59,59);
      Instant instant = localDateTime.atZone(defaultZone).toInstant();
      expirationDate = Date.from(instant);
    }

    info.setCustomer(customer);
    info.setContract(contract);
    info.setIssuanceDate(issuanceDate);
    info.setExpirationDate(expirationDate);
    info.setLicenseType(licenseType);
    info.setLicenseLevel(licenseLevel);
    info.setBilling(billing);
    if (recordLimit != null) {
      info.setRecordLimit(recordLimit);
    }

    return info;
  }
}
