package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzServerInfoResponseImpl;

/**
 * A response object that contains server info data.
 *
 */
@JsonDeserialize(using=SzServerInfoResponse.Factory.class)
public interface SzServerInfoResponse extends SzBasicResponse {
  /**
   * Returns the {@link SzServerInfo} associated with this response.
   *
   * @return The data associated with this response.
   */
  SzServerInfo getData();

  /**
   * Sets the data associated with this response with an {@link SzServerInfo}.
   *
   * @param info The {@link SzServerInfo} describing the license.
   */
  void setData(SzServerInfo info);

  /**
   * A {@link ModelProvider} for instances of {@link SzServerInfoResponse}.
   */
  interface Provider extends ModelProvider<SzServerInfoResponse> {
    /**
     * Creates an instance of {@link SzServerInfoResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzServerInfoResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzServerInfoResponse} with the specified
     * {}with the HTTP method, self link and the {@link SzVersionInfo}
     * describing the version.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param serverInfo The {@link SzServerInfo} describing the version.
     */
    SzServerInfoResponse create(SzMeta        meta,
                                SzLinks       links,
                                SzServerInfo  serverInfo);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzServerInfoResponse} that produces instances of
   * {@link SzServerInfoResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzServerInfoResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzServerInfoResponse.class, SzServerInfoResponseImpl.class);
    }

    @Override
    public SzServerInfoResponse create(SzMeta meta, SzLinks links) {
      return new SzServerInfoResponseImpl(meta, links);
    }

    @Override
    public SzServerInfoResponse create(SzMeta        meta,
                                       SzLinks       links,
                                       SzServerInfo  serverInfo) {
      return new SzServerInfoResponseImpl(meta, links, serverInfo);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzServerInfoResponse}.
   */
  class Factory extends ModelFactory<SzServerInfoResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzServerInfoResponse.class);
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
     * Creates an instance of {@link SzServerInfoResponse} with the specified
     * {@link SzMeta} and {@link SzLinks} instances.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzServerInfoResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzServerInfoResponse} with the specified
     * {}with the HTTP method, self link and the {@link SzVersionInfo}
     * describing the version.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param serverInfo The {@link SzServerInfo} describing the version.
     */
    public SzServerInfoResponse create(SzMeta        meta,
                                       SzLinks       links,
                                       SzServerInfo  serverInfo)
    {
      return this.getProvider().create(meta, links, serverInfo);
    }

  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
