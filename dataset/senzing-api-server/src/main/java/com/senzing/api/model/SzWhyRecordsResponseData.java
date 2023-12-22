package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyEntitiesResponseDataImpl;
import com.senzing.api.model.impl.SzWhyEntitiesResponseImpl;
import com.senzing.api.model.impl.SzWhyRecordsResponseDataImpl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Describes the data associated with an {@link SzWhyEntitiesResponse}.
 */
@JsonDeserialize(using= SzWhyRecordsResponseData.Factory.class)
public interface SzWhyRecordsResponseData {
  /**
   * Gets the {@link SzWhyRecordsResult} describing why the records did or did
   * not resolve.
   *
   * @return The {@link SzWhyRecordsResult} describing why the records did or
   *         did not resolve.
   */
  SzWhyRecordsResult getWhyResult();

  /**
   * Sets the {@link SzWhyRecordsResult} describing why the records the records
   * did or did not resolve.
   *
   * @param result The {@link SzWhyRecordsResult} describing why the records
   *               the records did or did not resolve.
   */
  void setWhyResult(SzWhyRecordsResult result);

  /**
   * Gets the unmodifiable {@link List} of {@link SzEntityData} instances
   * describing the entities involved in this why operation.
   *
   * @return The unmodifiable {@link Map} of {@link String} data source codes
   *         to {@link SzDataSource} values describing the configured data
   *         sources.
   */
  List<SzEntityData> getEntities();

  /**
   * Adds an entity to the {@link List} of associated entities for the response.
   *
   * @param entityData The {@link SzEntityData} describing the entity to add.
   */
  void addEntity(SzEntityData entityData);

  /**
   * Sets the entities associated with the response to the entities described
   * by the {@link SzEntityData} instances in the specified {@link Collection}.
   *
   * @param entities The {@link Collection} of {@link SzEntityData} instances
   *                 describing the entities for the response.
   */
  void setEntities(Collection<? extends SzEntityData> entities);

  /**
   * A {@link ModelProvider} for instances of {@link SzWhyRecordsResponseData}.
   */
  interface Provider extends ModelProvider<SzWhyRecordsResponseData> {
    /**
     * Creates an uninitialized instance with no data.
     */
    SzWhyRecordsResponseData create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyRecordsResponseData} that produces instances of
   * {@link SzWhyEntitiesResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyRecordsResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyRecordsResponseData.class,
            SzWhyRecordsResponseDataImpl.class);
    }

    @Override
    public SzWhyRecordsResponseData create() {
      return new SzWhyRecordsResponseDataImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzWhyRecordsResponseData}.
   */
  class Factory extends ModelFactory<SzWhyRecordsResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyRecordsResponseData.class);
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
     * Creates an uninitialized instance with no data.
     */
    public SzWhyRecordsResponseData create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
