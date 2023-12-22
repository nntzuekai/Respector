package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyEntityResponseDataImpl;
import com.senzing.api.model.impl.SzWhyEntityResponseImpl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Describes the data associated with an {@link SzWhyEntityResponse}.
 */
@JsonDeserialize(using= SzWhyEntityResponseData.Factory.class)
public interface SzWhyEntityResponseData {
  /**
   * Gets the {@link List} of {@link SzWhyEntityResult} instances describing
   * why the records in the entity resolved.
   *
   * @return The {@link List} of {@link SzWhyEntityResult} instances
   *         describing why the records in the entity resolved.
   */
  List<SzWhyEntityResult> getWhyResults();

  /**
   * Adds the specified {@link SzWhyEntityResult} to the {@link List} of
   * {@link SzWhyEntityResult} instances describing why the records in
   * the entity resolved.
   *
   * @param result The {@link SzWhyEntityResult} instance to add.
   */
  void addWhyResult(SzWhyEntityResult result);

  /**
   * Sets the {@link SzWhyEntityResult} instances describing why the records in
   * the entity resolved to those in the specified {@link Collection}
   * of {@link SzWhyEntityResult} instances.
   *
   * @param results The {@link Collection} of {@link SzWhyEntityResult}
   *                instances describing why the records in the entity resolved.
   */
  void setWhyResults(Collection<? extends SzWhyEntityResult> results);
  
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
   * A {@link ModelProvider} for instances of {@link SzWhyEntityResponseData}.
   */
  interface Provider extends ModelProvider<SzWhyEntityResponseData> {
    /**
     * Creates an uninitialized instance with no data.
     */
    SzWhyEntityResponseData create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyEntityResponseData} that produces instances of
   * {@link SzWhyEntityResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyEntityResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyEntityResponseData.class,
            SzWhyEntityResponseDataImpl.class);
    }

    @Override
    public SzWhyEntityResponseData create() {
      return new SzWhyEntityResponseDataImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzWhyEntityResponseData}.
   */
  class Factory extends ModelFactory<SzWhyEntityResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyEntityResponseData.class);
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
    public SzWhyEntityResponseData create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
