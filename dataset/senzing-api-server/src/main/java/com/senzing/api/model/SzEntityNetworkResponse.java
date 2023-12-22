package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityNetworkResponseImpl;
import com.senzing.util.Timers;

import javax.ws.rs.core.UriInfo;

/**
 * A response object that contains entity path data.
 */
@JsonDeserialize(using=SzEntityNetworkResponse.Factory.class)
public interface SzEntityNetworkResponse extends SzResponseWithRawData {
  /**
   * Returns the data associated with this response which is an
   * {@link SzEntityNetworkData}.
   *
   * @return The data associated with this response.
   */
  SzEntityNetworkData getData();

  /**
   * Sets the data associated with this response with an
   * {@link SzEntityNetworkData}.
   *
   * @param data The {@link SzEntityNetworkData} describing the record.
   */
  void setData(SzEntityNetworkData data);

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityNetworkResponse}.
   */
  interface Provider extends ModelProvider<SzEntityNetworkResponse> {
      /**
       * Creates an instance of {@link SzEntityNetworkResponse} with the
       * specified {@link SzMeta} and {@link SzLinks}.
       *
       * @param meta The response meta data.
       *
       * @param links The links for the response.
       */
      SzEntityNetworkResponse create(SzMeta meta, SzLinks links);

      /**
       * Creates an instance of {@link SzEntityNetworkResponse} with the
       * specified {@link SzMeta}, {@link SzLinks} and
       * {@link SzEntityNetworkData} describing the entity network.
       *
       * @param meta The response meta data.
       *
       * @param links The links for the response.
       *
       * @param data The {@link SzEntityNetworkData} describing the
       *             entity network.
       */
      SzEntityNetworkResponse create(SzMeta               meta,
                                     SzLinks              links,
                                     SzEntityNetworkData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityNetworkResponse} that produces instances of
   * {@link SzEntityNetworkResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityNetworkResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityNetworkResponse.class, SzEntityNetworkResponseImpl.class);
    }

    @Override
    public SzEntityNetworkResponse create(SzMeta meta, SzLinks links) {
      return new SzEntityNetworkResponseImpl(meta, links);
    }

    @Override
    public SzEntityNetworkResponse create(SzMeta              meta,
                                          SzLinks             links,
                                          SzEntityNetworkData data) {
      return new SzEntityNetworkResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzEntityNetworkResponse}.
   */
  class Factory extends ModelFactory<SzEntityNetworkResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityNetworkResponse.class);
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
     * Creates an instance of {@link SzEntityNetworkResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzEntityNetworkResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzEntityNetworkResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzEntityNetworkData} describing
     * the entity network.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzEntityNetworkData} describing the entity network.
     */
    public SzEntityNetworkResponse create(SzMeta              meta,
                                          SzLinks             links,
                                          SzEntityNetworkData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
