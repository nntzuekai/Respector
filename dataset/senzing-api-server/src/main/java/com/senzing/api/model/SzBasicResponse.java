package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzBasicResponseImpl;

/**
 * The most basic response from the Senzing REST API.  Also servers as a basis
 * for other responses.
 */
@JsonDeserialize(using=SzBasicResponse.Factory.class)
public interface SzBasicResponse {
  /**
   * Returns the meta data associated with this response.
   *
   * @return The meta data associated with this response.
   */
  SzMeta getMeta();

  /**
   * Gets the links associated with this response.
   *
   * @return The links associated with this response.
   */
  SzLinks getLinks();

  /**
   * If any of the response's timers are still accumulating time, this
   * causes them to cease.  Generally, this is only used in testing since
   * converting the object to JSON to serialize the response will have the
   * effect of concluding all timers.
   *
   * If timers are already concluded then this method does nothing.
   */
  void concludeTimers();

  /**
   * A {@link ModelProvider} for instances of {@link SzBasicResponse}.
   */
  interface Provider extends ModelProvider<SzBasicResponse> {
    /**
     * Creates an instance with the specified {@link SzMeta} and
     * {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzBasicResponse create(SzMeta meta, SzLinks links);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzBasicResponse} that produces instances of
   * {@link SzBasicResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzBasicResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzBasicResponse.class, SzBasicResponseImpl.class);
    }

    @Override
    public SzBasicResponse create(SzMeta meta, SzLinks links) {
      return new SzBasicResponseImpl(meta, links);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzBasicResponse}.
   */
  class Factory extends ModelFactory<SzBasicResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzBasicResponse.class);
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
    public SzBasicResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
