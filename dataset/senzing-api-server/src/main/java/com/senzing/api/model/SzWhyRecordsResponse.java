package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyRecordsResponseImpl;
import java.util.*;

/**
 * A response object that contains the {@link SzWhyRecordsResult} describing
 * why or why not two records did or did not resolve.
 */
@JsonDeserialize(using=SzWhyRecordsResponse.Factory.class)
public interface SzWhyRecordsResponse extends SzResponseWithRawData {
  /**
   * Returns the data associated with this response which is an
   * {@link SzWhyRecordsResult}.
   *
   * @return The data associated with this response.
   */
  SzWhyRecordsResponseData getData();

  /**
   * Sets the data associated with this response which is an
   * {@link SzWhyRecordsResult}.
   *
   * @param data The data associated with this response.
   */
  void setData(SzWhyRecordsResponseData data);

  /**
   * Convenience method to set the {@link SzWhyRecordsResult} associated with
   * the underlying {@link SzWhyRecordsResponseData}.
   *
   * @param result The {@link SzWhyRecordsResult} for the response data.
   */
  void setWhyResult(SzWhyRecordsResult result);

  /**
   * Convenience method to add the specified {@link SzEntityData} to those
   * instances associated with the underlying {@link SzWhyRecordsResponseData}.
   *
   * @param entity The {@link SzEntityData} describing the entity to add.
   */
  void addEntity(SzEntityData entity);

  /**
   * Sets the list of {@link SzEntityData} instances associated with the
   * underlying {@link SzWhyRecordsResponseData}.
   *
   * @param entities The {@link Collection} of {@link SzEntityData} instances
   *                 to set.
   */
  void setEntities(Collection<? extends SzEntityData> entities);

  /**
   * A {@link ModelProvider} for instances of {@link SzWhyRecordsResponse}.
   */
  interface Provider extends ModelProvider<SzWhyRecordsResponse> {
    /**
     * Creates an instance with the specified {@link SzMeta} and
     * {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzWhyRecordsResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance with the specified {@link SzMeta}, {@link SzLinks}
     * and {@link SzWhyRecordsResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    SzWhyRecordsResponse create(SzMeta                    meta,
                                SzLinks                   links,
                                SzWhyRecordsResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyRecordsResponse} that produces instances of
   * {@link SzWhyRecordsResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyRecordsResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyRecordsResponse.class, SzWhyRecordsResponseImpl.class);
    }

    @Override
    public SzWhyRecordsResponse create(SzMeta meta, SzLinks links) {
      return new SzWhyRecordsResponseImpl(meta, links);
    }

    @Override
    public SzWhyRecordsResponse create(SzMeta                   meta,
                                       SzLinks                  links,
                                       SzWhyRecordsResponseData data)
    {
      return new SzWhyRecordsResponseImpl(meta, links, data);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzWhyRecordsResponse}.
   */
  class Factory extends ModelFactory<SzWhyRecordsResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyRecordsResponse.class);
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
    public SzWhyRecordsResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance with the specified {@link SzMeta}, {@link SzLinks}
     * and {@link SzWhyRecordsResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    public SzWhyRecordsResponse create(SzMeta                   meta,
                                       SzLinks                  links,
                                       SzWhyRecordsResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }

  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
  
}
