package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzBasicResponseImpl;
import com.senzing.api.model.impl.SzLicenseResponseDataImpl;

/**
 * Describes the data segment of an {@link SzLicenseResponse}.
 */
@JsonDeserialize(using= SzLicenseResponseData.Factory.class)
public interface SzLicenseResponseData {
  /**
   * Gets the {@link SzLicenseInfo} describing the license.
   *
   * @return The {@link SzLicenseInfo} describing the license.
   */
  SzLicenseInfo getLicense();

  /**
   * Sets the {@link SzLicenseInfo} describing the license.
   *
   * @param licenseInfo The {@link SzLicenseInfo} describing the license.
   */
  void setLicense(SzLicenseInfo licenseInfo);

  /**
   * A {@link ModelProvider} for instances of {@link SzLicenseResponseData}.
   */
  interface Provider extends ModelProvider<SzLicenseResponseData> {
    /**
     * Creates an instance with no license info.
     *
     * @return The created {@link SzLicenseResponseData} instance.
     */
    SzLicenseResponseData create();

    /**
     * Creates an instance with the specified {@link SzLicenseInfo}.
     *
     * @param licenseInfo The {@link SzLicenseInfo} to initialize with.
     *
     * @return The created {@link SzLicenseResponseData} instance.
     */
    SzLicenseResponseData create(SzLicenseInfo licenseInfo);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzBasicResponse} that produces instances of
   * {@link SzBasicResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzLicenseResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzLicenseResponseData.class, SzLicenseResponseDataImpl.class);
    }

    @Override
    public SzLicenseResponseData create() {
      return new SzLicenseResponseDataImpl();
    }

    @Override
    public SzLicenseResponseData create(SzLicenseInfo licenseInfo) {
      return new SzLicenseResponseDataImpl(licenseInfo);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzLicenseResponseData}.
   */
  class Factory extends ModelFactory<SzLicenseResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzLicenseResponseData.class);
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
     * Creates an instance with no license info.
     *
     * @return The created {@link SzLicenseResponseData} instance.
     */
    public SzLicenseResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance with the specified {@link SzLicenseInfo}.
     *
     * @param licenseInfo The {@link SzLicenseInfo} to initialize with.
     *
     * @return The created {@link SzLicenseResponseData} instance.
     */
    public SzLicenseResponseData create(SzLicenseInfo licenseInfo) {
      return this.getProvider().create(licenseInfo);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
