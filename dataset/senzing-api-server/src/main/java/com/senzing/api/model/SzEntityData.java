package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityDataImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

/**
 * Provides a default implementation of {@link SzEntityData}.
 */
@JsonDeserialize(using=SzEntityData.Factory.class)
public interface SzEntityData {
  /**
   * Gets the {@link SzResolvedEntity} describing the resolved entity.
   *
   * @return The {@link SzResolvedEntity} describing the resolved entity.
   */
  SzResolvedEntity getResolvedEntity();

  /**
   * Sets the {@link SzResolvedEntity} describing the resolved entity.
   *
   * @param resolvedEntity The {@link SzResolvedEntity} describing the
   *                       resolved entity.
   */
  void setResolvedEntity(SzResolvedEntity resolvedEntity);

  /**
   * Gets the {@link List} of {@linkplain SzRelatedEntity related entities}.
   *
   * @return The {@link List} of {@linkplain SzRelatedEntity related entities}.
   */
  @JsonInclude(NON_EMPTY)
  List<SzRelatedEntity> getRelatedEntities();

  /**
   * Sets the {@link List} of {@linkplain SzRelatedEntity related entities}.
   *
   * @param relatedEntities The {@link List} of {@linkplain SzRelatedEntity
   *                        related entities}.
   */
  void setRelatedEntities(List<SzRelatedEntity> relatedEntities);

  /**
   * Adds the specified {@link SzRelatedEntity}
   */
  void addRelatedEntity(SzRelatedEntity relatedEntity);

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityData}.
   */
  interface Provider extends ModelProvider<SzEntityData> {
    /**
     * Creates a new instance of {@link SzEntityData}.
     *
     * @return The new instance of {@link SzEntityData}
     */
    SzEntityData create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityData} that produces instances of {@link SzEntityDataImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityData.class, SzEntityDataImpl.class);
    }

    @Override
    public SzEntityData create() {
      return new SzEntityDataImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzEntityData}.
   */
  class Factory extends ModelFactory<SzEntityData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityData.class);
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
     * Creates a new instance of {@link SzEntityData}.
     * @return The new instance of {@link SzEntityData}.
     */
    public SzEntityData create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a list of entity data instances from a {@link JsonArray}
   * describing a JSON array in the Senzing native API format for entity
   * features and populates the specified {@link List} or creates a new
   * {@link List}.
   *
   * @param list The {@link List} of {@link SzEntityData} instances to
   *             populate, or <tt>null</tt> if a new {@link List}
   *             should be created.
   *
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link List} of {@link
   *         SzEntityData} instances.
   */
  static List<SzEntityData> parseEntityDataList(
      List<SzEntityData>      list,
      JsonArray               jsonArray,
      Function<String,String> featureToAttrClassMapper)
  {
    Function<String,String> mapper = featureToAttrClassMapper;

    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseEntityData(null, jsonObject, mapper));
    }
    return list;
  }

  /**
   * Parses the entity data from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity data and populates
   * the specified {@link SzEntityData} or creates a new instance.
   *
   * @param entityData The {@link SzEntityData} instance to populate, or
   *                   <tt>null</tt> if a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link SzEntityData}.
   */
  static SzEntityData parseEntityData(
      SzEntityData            entityData,
      JsonObject              jsonObject,
      Function<String,String> featureToAttrClassMapper)
  {
    if (entityData == null) entityData = SzEntityData.FACTORY.create();

    Function<String,String> mapper = featureToAttrClassMapper;

    JsonObject resEntObj = jsonObject.getJsonObject("RESOLVED_ENTITY");

    SzResolvedEntity resolvedEntity
        = SzResolvedEntity.parseResolvedEntity(null, resEntObj, mapper);

    JsonArray relatedArray
        = JsonUtilities.getJsonArray(jsonObject,"RELATED_ENTITIES");

    List<SzRelatedEntity> relatedEntities
        = SzRelatedEntity.parseRelatedEntityList(null,
                                                 relatedArray,
                                                 mapper);

    entityData.setResolvedEntity(resolvedEntity);
    entityData.setRelatedEntities(relatedEntities);

    return entityData;
  }
}
