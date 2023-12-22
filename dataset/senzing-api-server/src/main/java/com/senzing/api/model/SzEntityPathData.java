package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityPathDataImpl;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;
import java.util.function.Function;

/**
 * Describes an entity path and the entities in the path.
 */
@JsonDeserialize(using=SzEntityPathData.Factory.class)
public interface SzEntityPathData {
  /**
   * Returns the {@link SzEntityPath} describing the entity path.
   *
   * @return The {@link SzEntityPath} describing the entity path.
   */
  SzEntityPath getEntityPath();

  /**
   * Returns the {@link List} of {@link SzEntityData} instances describing
   * the entities in the path.
   *
   * @return The {@link List} of {@link SzEntityData} instances describing
   *         the entities in the path.
   */
  List<SzEntityData> getEntities();

    /**
   * A {@link ModelProvider} for instances of {@link SzEntityData}.
   */
  interface Provider extends ModelProvider<SzEntityPathData> {
    /**
     * Constructs with the specified {@link SzEntityPath} and {@link
     * SzEntityData} instances describing the entities in the path.
     *
     * @param entityPath The {@link SzEntityPath} describing the entity path.
     *
     * @param entities The {@link List} of {@link SzEntityData} instances
     *                 describing the entities in the path.
     *
     * @throws IllegalArgumentException If the entities list is not consistent
     *                                  with the specified entity path.
     */
    SzEntityPathData create(SzEntityPath        entityPath,
                            List<SzEntityData>  entities)
        throws IllegalArgumentException;
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityPathData} that produces instances of {@link SzEntityPathDataImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityPathData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityPathData.class, SzEntityPathDataImpl.class);
    }

    @Override
    public SzEntityPathData create(SzEntityPath       entityPath,
                                   List<SzEntityData> entities)
        throws IllegalArgumentException
    {
      return new SzEntityPathDataImpl(entityPath, entities);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzEntityPathData}.
   */
  class Factory extends ModelFactory<SzEntityPathData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityPathData.class);
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
     * Creates a new instance of {@link SzEntityPathData}.
     * @return The new instance of {@link SzEntityPathData}.
     */
    public SzEntityPathData create(SzEntityPath       entityPath,
                                   List<SzEntityData> entities)
        throws IllegalArgumentException
    {
      return this.getProvider().create(entityPath, entities);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the entity path data from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity feature and populates
   * the specified {@link SzEntityPathData} or creates a new instance.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link SzEntityPathData}.
   */
  static SzEntityPathData parseEntityPathData(
      JsonObject              jsonObject,
      Function<String,String> featureToAttrClassMapper)
  {
    JsonArray pathArray = jsonObject.getJsonArray("ENTITY_PATHS");
    if (pathArray.size() == 0) return null;
    JsonObject pathObject = pathArray.get(0).asJsonObject();
    SzEntityPath entityPath = SzEntityPath.parseEntityPath(pathObject);

    JsonArray jsonArray = jsonObject.getJsonArray("ENTITIES");

    List<SzEntityData> dataList = SzEntityData.parseEntityDataList(
        null, jsonArray, featureToAttrClassMapper);

    return SzEntityPathData.FACTORY.create(entityPath, dataList);
  }

}
