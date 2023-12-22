package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzLicenseResponseImpl;

/**
 * A response object that contains license data.
 *
 */
@JsonDeserialize(using=SzLicenseResponse.Factory.class)
public interface SzLicenseResponse extends SzResponseWithRawData {
  /**
   * Returns the {@link SzLicenseResponseData} associated with this response
   * which contains an {@link SzLicenseInfo}.
   *
   * @return The data associated with this response.
   */
  SzLicenseResponseData getData();

  /**
   * Sets the {@link SzLicenseResponseData} associated with this response
   * which contains an {@link SzLicenseInfo}.
   *
   * @param data The data associated with this response.
   */
  void setData(SzLicenseResponseData data);

  /**
   * Convenience method to set the {@link SzLicenseInfo} associated with the
   * underlying {@link SzLicenseResponseData}.
   *
   * @param info The {@link SzLicenseInfo} describing the license.
   */
  void setLicense(SzLicenseInfo info);

  /**
   * A {@link ModelProvider} for instances of {@link SzLicenseResponse}.
   */
  interface Provider extends ModelProvider<SzLicenseResponse> {
    /**
     * Creates an instance of {@link SzLicenseResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzLicenseResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzLicenseResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzLicenseResponseData}
     * describing the license.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzLicenseResponseData} describing the license.
     */
    SzLicenseResponse create(SzMeta                 meta,
                             SzLinks                links,
                             SzLicenseResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzLicenseResponse} that produces instances of
   * {@link SzLicenseResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzLicenseResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzLicenseResponse.class, SzLicenseResponseImpl.class);
    }

    @Override
    public SzLicenseResponse create(SzMeta meta, SzLinks links) {
      return new SzLicenseResponseImpl(meta, links);
    }

    @Override
    public SzLicenseResponse create(SzMeta                 meta,
                                    SzLinks                links,
                                    SzLicenseResponseData  data)
    {
      return new SzLicenseResponseImpl(meta, links, data);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzLicenseResponse}.
   */
  class Factory extends ModelFactory<SzLicenseResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzLicenseResponse.class);
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
     * Creates an instance with the specified {@link SzMeta} and
     * {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzLicenseResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzLicenseResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzLicenseResponseData}
     * describing the license.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzLicenseResponseData} describing the license.
     */
    public SzLicenseResponse create(SzMeta                meta,
                                    SzLinks               links,
                                    SzLicenseResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
