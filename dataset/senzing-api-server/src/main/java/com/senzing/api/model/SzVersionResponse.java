package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzVersionResponseImpl;
import com.senzing.util.Timers;

import javax.ws.rs.core.UriInfo;

/**
 * A response object that contains version data.
 *
 */
@JsonDeserialize(using=SzVersionResponse.Factory.class)
public interface SzVersionResponse extends SzResponseWithRawData {
  /**
   * Returns the {@link SzVersionInfo} associated with this response.
   *
   * @return The data associated with this response.
   */
  SzVersionInfo getData();

  /**
   * Sets the data associated with this response with an {@link SzVersionInfo}.
   *
   * @param info The {@link SzVersionInfo} describing the license.
   */
  void setData(SzVersionInfo info);
  
  /**
   * A {@link ModelProvider} for instances of {@link SzVersionResponse}.
   */
  interface Provider extends ModelProvider<SzVersionResponse> {
    /**
     * Creates an instance of {@link SzVersionResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzVersionResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzVersionResponse} with the specified
     * {}with the HTTP method, self link and the {@link SzVersionInfo}
     * describing the version.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param versionInfo The {@link SzVersionInfo} describing the version.
     */
    SzVersionResponse create(SzMeta        meta,
                                SzLinks       links,
                                SzVersionInfo  versionInfo);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzVersionResponse} that produces instances of
   * {@link SzVersionResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzVersionResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzVersionResponse.class, SzVersionResponseImpl.class);
    }

    @Override
    public SzVersionResponse create(SzMeta meta, SzLinks links) {
      return new SzVersionResponseImpl(meta, links);
    }

    @Override
    public SzVersionResponse create(SzMeta        meta,
                                    SzLinks       links,
                                    SzVersionInfo versionInfo)
    {
      return new SzVersionResponseImpl(meta, links, versionInfo);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzVersionResponse}.
   */
  class Factory extends ModelFactory<SzVersionResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzVersionResponse.class);
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
     * Creates an instance of {@link SzVersionResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzVersionResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzVersionResponse} with the specified
     * {}with the HTTP method, self link and the {@link SzVersionInfo}
     * describing the version.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param versionInfo The {@link SzVersionInfo} describing the version.
     */
    public SzVersionResponse create(SzMeta        meta,
                                       SzLinks       links,
                                       SzVersionInfo  versionInfo)
    {
      return this.getProvider().create(meta, links, versionInfo);
    }

  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
  
}
