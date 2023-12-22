package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzLicenseInfo;
import java.util.Date;

/**
 * Provides the default {@link SzLicenseInfo} implementation.
 */
@JsonDeserialize
public class SzLicenseInfoImpl implements SzLicenseInfo {
  /**
   * The customer associated with the license.
   */
  private String customer = null;

  /**
   * The constract associated with the license.
   */
  private String contract = null;

  /**
   * The license type associated with the license.
   */
  private String licenseType = null;

  /**
   * The license level associated with the license.
   */
  private String licenseLevel = null;

  /**
   * The billing string associated with the license.
   */
  private String billing = null;

  /**
   * The issuance date associated with the license.
   */
  @JsonFormat(shape   = JsonFormat.Shape.STRING,
              pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
              locale  = "en_GB")
  private Date issuanceDate = null;

  /**
   * The expiration date associated with the license.
   */
  @JsonFormat(shape   = JsonFormat.Shape.STRING,
              pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
              locale  = "en_GB")
  private Date expirationDate = null;

  /**
   * The record limit associated with the license.
   */
  private long recordLimit = 0L;

  /**
   * Default constructor.
   */
  public SzLicenseInfoImpl() {
    // do nothing
  }

  /**
   * Gets the customer string associated with the license.
   *
   * @return The customer string associated with the license.
   */
  @Override
  public String getCustomer() {
    return customer;
  }

  /**
   * Sets the customer string associated with the license.
   *
   * @param customer The customer string associated with the license.
   */
  @Override
  public void setCustomer(String customer) {
    this.customer = customer;
  }

  /**
   * Gets the contract string associated with the license.
   *
   * @return The contract string associated with the license.
   */
  @Override
  public String getContract() {
    return contract;
  }

  /**
   * Sets the contract string associated with the license.
   *
   * @param contract The contract string associated with the license.
   */
  @Override
  public void setContract(String contract) {
    this.contract = contract;
  }

  /**
   * Gets the license type associated with the license.
   *
   * @return The license type associated with the license.
   */
  @Override
  public String getLicenseType() {
    return licenseType;
  }

  /**
   * Sets the license type associated with the license.
   *
   * @param licenseType The license type associated with the license.
   */
  @Override
  public void setLicenseType(String licenseType) {
    this.licenseType = licenseType;
  }

  /**
   * Gets the license level associated with the license.
   *
   * @return The license level associated with the license.
   */
  @Override
  public String getLicenseLevel() {
    return licenseLevel;
  }

  /**
   * Sets the license level associated with the licenese.
   *
   * @param licenseLevel The license level associated with the license.
   */
  @Override
  public void setLicenseLevel(String licenseLevel) {
    this.licenseLevel = licenseLevel;
  }

  /**
   * Gets the billing string associated with the license.
   *
   * @return The billing string associated with the license.
   */
  @Override
  public String getBilling() {
    return billing;
  }

  /**
   * Sets the billing string associated with the license.
   *
   * @param billing The billing string associated with the license.
   */
  @Override
  public void setBilling(String billing) {
    this.billing = billing;
  }

  /**
   * Gets the issuance {@link Date} associated with the license.
   *
   * @return The issuance date associated with the license.
   */
  @Override
  public Date getIssuanceDate() {
    return issuanceDate;
  }

  /**
   * Sets the issuance {@link Date} associated with the license.
   *
   * @param issuanceDate The issuance {@link Date} to be associated with the
   *                     license.
   */
  @Override
  public void setIssuanceDate(Date issuanceDate) {
    this.issuanceDate = issuanceDate;
  }

  /**
   * Gets the expiration {@link Date} associated with the license.
   *
   * @return The expiration {@link Date} associated with the license.
   */
  @Override
  public Date getExpirationDate() {
    return expirationDate;
  }

  /**
   * Sets the expiration date associated with the license.
   *
   * @param expirationDate The expiration date associated with the license.
   */
  @Override
  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  /**
   * Gets the record limit associated with the license.
   *
   * @return The record limit associated with the license.
   */
  @Override
  public long getRecordLimit() {
    return recordLimit;
  }

  /**
   * Sets the record limit associated with the license.
   *
   * @param recordLimit The record limit associated with the license.
   */
  @Override
  public void setRecordLimit(long recordLimit) {
    this.recordLimit = recordLimit;
  }
}
