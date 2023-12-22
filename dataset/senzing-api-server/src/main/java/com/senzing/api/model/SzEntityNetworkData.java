package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityNetworkDataImpl;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;
import java.util.function.Function;

/**
 * Describes an entity path and the entities in the path.
 */
@JsonDeserialize(using=SzEntityNetworkData.Factory.class)
public interface SzEntityNetworkData {
  /**
   * Returns the {@link List} of {@link SzEntityPath} instances describing
   * the entity paths.
   *
   * @return The {@link List} of {@link SzEntityPath} instances describing
   *         the entity paths.
   */
  List<SzEntityPath> getEntityPaths();

  /**
   * Returns the {@link List} of {@link SzEntityData} instances describing
   * the entities in the path.
   *
   * @return The {@link List} of {@link SzEntityData} instances describing
   *         the entities in the path.
   */
  List<SzEntityData> getEntities();

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityNetworkData}.
   */
  interface Provider extends ModelProvider<SzEntityNetworkData> {
    /**
     * Creates an uninitialized instance.
     */
    SzEntityNetworkData create();

    /**
     * Creates an instance with the specified {@link List} of {@link
     * SzEntityPath} instances and {@link List} of {@link SzEntityData}
     * instances describing the entities in the path.
     *
     * @param entityPaths The {@link List} of {@link SzEntityPath} instances
     *                    describing the entity paths.
     *
     * @param entities The {@link List} of {@link SzEntityData} instances
     *                 describing the entities in the path.
     *
     * @throws IllegalArgumentException If the entities list is not consistent
     *                                  with the specified entity paths.
     */
    SzEntityNetworkData create(List<SzEntityPath> entityPaths,
                               List<SzEntityData> entities)
        throws IllegalArgumentException;
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityNetworkData} that produces instances of {@link
   * SzEntityNetworkDataImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityNetworkData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityNetworkData.class, SzEntityNetworkDataImpl.class);
    }

    @Override
    public SzEntityNetworkData create() {
      return new SzEntityNetworkDataImpl();
    }

    @Override
    public SzEntityNetworkData create(List<SzEntityPath> entityPaths,
                                      List<SzEntityData> entities)
        throws IllegalArgumentException
    {
      return new SzEntityNetworkDataImpl(entityPaths, entities);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzEntityNetworkData}.
   */
  class Factory extends ModelFactory<SzEntityNetworkData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityNetworkData.class);
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
     * Creates an uninitialized instance.
     */
    public SzEntityNetworkData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance with the specified {@link List} of {@link
     * SzEntityPath} instances and {@link List} of {@link SzEntityData}
     * instances describing the entities in the path.
     *
     * @param entityPaths The {@link List} of {@link SzEntityPath} instances
     *                    describing the entity paths.
     *
     * @param entities The {@link List} of {@link SzEntityData} instances
     *                 describing the entities in the path.
     *
     * @throws IllegalArgumentException If the entities list is not consistent
     *                                  with the specified entity paths.
     */
    public SzEntityNetworkData create(List<SzEntityPath> entityPaths,
                                      List<SzEntityData> entities)
        throws IllegalArgumentException
    {
      return this.getProvider().create(entityPaths, entities);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the entity feature from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity feature and populates
   * the specified {@link SzEntityNetworkData} or creates a new instance.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link SzEntityNetworkData}.
   */
  static SzEntityNetworkData parseEntityNetworkData(
      JsonObject              jsonObject,
      Function<String,String> featureToAttrClassMapper)
  {
    JsonArray jsonArray = jsonObject.getJsonArray("ENTITY_PATHS");
    List<SzEntityPath> entityPaths
        = SzEntityPath.parseEntityPathList(null, jsonArray);

    jsonArray = jsonObject.getJsonArray("ENTITIES");

    List<SzEntityData> dataList = SzEntityData.parseEntityDataList(
        null, jsonArray, featureToAttrClassMapper);

    return SzEntityNetworkData.FACTORY.create(entityPaths, dataList);
  }

}
