package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzConfigResponseImpl;
import com.senzing.util.Timers;

import javax.ws.rs.core.UriInfo;

/**
 * A response object that contains license data.
 *
 */
@JsonDeserialize(using=SzConfigResponse.Factory.class)
public interface SzConfigResponse extends SzResponseWithRawData {
  /**
   * A {@link ModelProvider} for instances of {@link SzConfigResponse}.
   */
  interface Provider extends ModelProvider<SzConfigResponse> {
    /**
     * Creates an instance of {@link SzConfigResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzConfigResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzConfigResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}. and {@link String} representing the
     * raw JSON config.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param rawData The raw data JSON config to associate with the response.
     */
    SzConfigResponse create(SzMeta meta, SzLinks links, String rawData);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzConfigResponse} that produces instances of
   * {@link SzConfigResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzConfigResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzConfigResponse.class, SzConfigResponseImpl.class);
    }

    @Override
    public SzConfigResponse create(SzMeta meta, SzLinks links) {
      return new SzConfigResponseImpl(meta, links);
    }

    @Override
    public SzConfigResponse create(SzMeta meta, SzLinks links, String rawData) {
      return new SzConfigResponseImpl(meta, links, rawData);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzConfigResponse}.
   */
  class Factory extends ModelFactory<SzConfigResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzConfigResponse.class);
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
     * Creates an instance of {@link SzConfigResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzConfigResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzConfigResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}. and {@link String} representing the
     * raw JSON config.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param rawData The raw data JSON config to associate with the response.
     */
    public SzConfigResponse create(SzMeta meta, SzLinks links, String rawData) {
      return this.getProvider().create(meta, links, rawData);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
