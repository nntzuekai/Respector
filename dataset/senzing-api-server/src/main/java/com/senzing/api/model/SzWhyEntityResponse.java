package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyEntityResponseImpl;

import java.util.*;

/**
 * A response object that contains the {@link SzWhyEntityResult} describing
 * why an entity resolved.
 */
@JsonDeserialize(using=SzWhyEntityResponse.Factory.class)
public interface SzWhyEntityResponse extends SzResponseWithRawData {
  /**
   * Returns the data associated with this response.
   *
   * @return The data associated with this response.
   */
  SzWhyEntityResponseData getData();

  /**
   * Sets the data associated with this response.
   *
   * @param data The data associated with this response.
   */
  void setData(SzWhyEntityResponseData data);

  /**
   * Convenience method to add an {@link SzWhyEntityResult} to the list of
   * results associated with the underlying {@link SzWhyEntityResponseData}.
   *
   * @param result The {@link SzWhyEntityResult} to add to the list of results
   *               describing why the records in the entity resolved.
   */
  void addWhyResult(SzWhyEntityResult result);

  /**
   * Convenience method to set the {@link SzWhyEntityResult} instances
   * associated with the underlying {@link SzWhyEntityResponseData} to those
   * in the specified {@link Collection}.
   *
   * @param results The {@link Collection} of {@link SzWhyEntityResult}
   *                instances describing why the records in the entity resolved.
   */
  void setWhyResults(Collection<? extends SzWhyEntityResult> results);

  /**
   * Convenience method to add an entity to the list of entities associated with
   * the underlying {@link SzWhyEntityResponseData}.
   *
   * @param entity The {@link SzEntityData} describing the entity to add.
   */
  void addEntity(SzEntityData entity);

  /**
   * Convenience method to set the list of {@link SzEntityData} instances
   * associated with the underlying {@link SzWhyEntityResponseData} to those
   * in the specified {@link Collection}.
   *
   * @param entities The {@link Collection} of {@link SzEntityData} instances
   *                 to use for the entities.
   */
  void setEntities(Collection<? extends SzEntityData> entities);

  /**
   * A {@link ModelProvider} for instances of {@link SzWhyEntityResponse}.
   */
  interface Provider extends ModelProvider<SzWhyEntityResponse> {
    /**
     * Creates an instance with the specified {@link SzMeta} and
     * {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzWhyEntityResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance with the specified {@link SzMeta}, {@link SzLinks}
     * and {@link SzWhyEntityResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzWhyEntityResponseData} for the response.
     */
    SzWhyEntityResponse create(SzMeta                   meta,
                               SzLinks                  links,
                               SzWhyEntityResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyEntityResponse} that produces instances of
   * {@link SzWhyEntityResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyEntityResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyEntityResponse.class, SzWhyEntityResponseImpl.class);
    }

    @Override
    public SzWhyEntityResponse create(SzMeta meta, SzLinks links) {
      return new SzWhyEntityResponseImpl(meta, links);
    }

    @Override
    public SzWhyEntityResponse create(SzMeta                  meta,
                                      SzLinks                 links,
                                      SzWhyEntityResponseData data)
    {
      return new SzWhyEntityResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzWhyEntityResponse}.
   */
  class Factory extends ModelFactory<SzWhyEntityResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyEntityResponse.class);
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
    public SzWhyEntityResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance with the specified {@link SzMeta}, {@link SzLinks}
     * and {@link SzWhyEntityResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzWhyEntityResponseData} for the response.
     */
    public SzWhyEntityResponse create(SzMeta                  meta,
                                      SzLinks                 links,
                                      SzWhyEntityResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }

  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
