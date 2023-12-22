package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyEntitiesResponseImpl;
import java.util.*;

/**
 * A response object that contains the {@link SzWhyEntitiesResult} describing
 * why two entities related or did not resolve.
 */
@JsonDeserialize(using=SzWhyEntitiesResponse.Factory.class)
public interface SzWhyEntitiesResponse extends SzResponseWithRawData {
  /**
   * Returns the data associated with this response.
   *
   * @return The data associated with this response.
   */
  SzWhyEntitiesResponseData getData();

  /**
   * Sets the data associated with this response.
   *
   * @param data The data associated with this response.
   */
  void setData(SzWhyEntitiesResponseData data);

  /**
   * Convenience method to set the {@link SzWhyEntitiesResult} on the underlying
   * {@link SzWhyEntitiesResponseData}.
   *
   * @param result The {@link SzWhyEntitiesResult} the result describing why
   *               the entities did not resolve or why they related.
   */
  void setWhyResult(SzWhyEntitiesResult result);

  /**
   * Convenience method to add an entity to the list of entities associated
   * with the underlying {@link SzWhyEntitiesResponseData}.
   *
   * @param entity The {@link SzEntityData} describing the entity to add.
   */
  void addEntity(SzEntityData entity);

  /**
   * Convenience method to set the entities associated with the underlying
   * {@link SzWhyEntitiesResponseData}.
   */
  void setEntities(Collection<? extends SzEntityData> entities);

  /**
   * A {@link ModelProvider} for instances of {@link SzWhyEntitiesResponse}.
   */
  interface Provider extends ModelProvider<SzWhyEntitiesResponse> {
    /**
     * Creates an instance with the specified {@link SzMeta} and
     * {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzWhyEntitiesResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance with the specified {@link SzMeta}, {@link SzLinks}
     * and {@link SzWhyEntitiesResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    SzWhyEntitiesResponse create(SzMeta                     meta,
                                 SzLinks                    links,
                                 SzWhyEntitiesResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyEntitiesResponse} that produces instances of
   * {@link SzWhyEntitiesResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyEntitiesResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyEntitiesResponse.class, SzWhyEntitiesResponseImpl.class);
    }

    @Override
    public SzWhyEntitiesResponse create(SzMeta meta, SzLinks links) {
      return new SzWhyEntitiesResponseImpl(meta, links);
    }

    @Override
    public SzWhyEntitiesResponse create(SzMeta                    meta,
                                        SzLinks                   links,
                                        SzWhyEntitiesResponseData data)
    {
      return new SzWhyEntitiesResponseImpl(meta, links, data);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzWhyEntitiesResponse}.
   */
  class Factory extends ModelFactory<SzWhyEntitiesResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyEntitiesResponse.class);
    }

    /**
     * Constructs with the default provider.  This constructor is private and
     * is used for the master singleton instance.
     *
     * @param defaultProvider The default provider.
     */
    private Factory(Provider defaultProvider) {
      super(defaultProvider);
    }

    /**
     * Creates an instance with the specified {@link SzMeta} and
     * {@link SzLinks}.
     *
     * @param meta  The response meta data.
     * @param links The links for the response.
     */
    public SzWhyEntitiesResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance with the specified {@link SzMeta}, {@link SzLinks}
     * and {@link SzWhyEntitiesResponseData}.
     *
     * @param meta  The response meta data.
     * @param links The links for the response.
     * @param data  The data for the response.
     */
    public SzWhyEntitiesResponse create(SzMeta meta,
                                        SzLinks links,
                                        SzWhyEntitiesResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
