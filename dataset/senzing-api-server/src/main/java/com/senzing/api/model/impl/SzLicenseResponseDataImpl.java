package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzLicenseInfo;
import com.senzing.api.model.SzLicenseResponseData;

/**
 * Provides a default implementation of {@link SzLicenseResponseData}.
 */
@JsonDeserialize
public class SzLicenseResponseDataImpl implements SzLicenseResponseData {
  /**
   * The {@link SzLicenseInfo} describing the license.
   */
  private SzLicenseInfo license;

  /**
   * Default constructor.
   */
  public SzLicenseResponseDataImpl() {
    this.license = null;
  }

  /**
   * Constructs with the specified {@link SzLicenseInfo}.
   *
   * @param licenseInfo The {@link SzLicenseInfo} for the new instance.
   */
  public SzLicenseResponseDataImpl(SzLicenseInfo licenseInfo) {
    this.license = licenseInfo;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzLicenseInfo getLicense() {
    return this.license;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLicense(SzLicenseInfo licenseInfo) {
    this.license = licenseInfo;
  }
}
