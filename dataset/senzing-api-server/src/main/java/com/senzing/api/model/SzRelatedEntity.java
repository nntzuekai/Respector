package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzRelatedEntityImpl;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;
import java.util.function.Function;

import static com.senzing.api.model.SzRelationshipType.*;

/**
 * Describes an entity related to the base entity.
 */
@JsonDeserialize(using=SzRelatedEntity.Factory.class)
public interface SzRelatedEntity extends SzBaseRelatedEntity {
  /**
   * Checks whether or not the relationship between the entities is disclosed.
   *
   * @return <tt>true</tt> if the relationship is disclosed, or <tt>false</tt>
   *         if not disclosed.
   */
  boolean isDisclosed();

  /**
   * Sets whether or not the relationship between the entities is disclosed.
   *
   * @param disclosed <tt>true</tt> if the relationship is disclosed, or
   *                  <tt>false</tt> if not disclosed.
   */
  void setDisclosed(boolean disclosed);

  /**
   * Checks whether or not the relationship between the entities is an
   * ambiguous possible match.
   *
   * @return <tt>true</tt> if the relationship is an ambiguous possible match,
   *         or <tt>false</tt> if not disclosed.
   */
  boolean isAmbiguous();

  /**
   * Sets whether or not the relationship between the entities is an
   * ambiguous possible match.
   *
   * @param ambiguous <tt>true</tt> if the relationship is an ambiguous
   *                  possible match, or <tt>false</tt> if not disclosed.
   */
  void setAmbiguous(boolean ambiguous);

  /**
   * Gets the {@link SzRelationshipType} describing the type of relation.
   *
   * @return The {@link SzRelationshipType} describing the type of relation.
   */
  SzRelationshipType getRelationType();

  /**
   * Sets the {@link SzRelationshipType} describing the type of relation.
   *
   * @param relationType The {@link SzRelationshipType} describing the type
   *                     of relation.
   */
  void setRelationType(SzRelationshipType relationType);

  /**
   * A {@link ModelProvider} for instances of {@link SzRelatedEntity}.
   */
  interface Provider extends ModelProvider<SzRelatedEntity> {
    /**
     * Creates a new instance of {@link SzRelatedEntity}.
     *
     * @return The new instance of {@link SzRelatedEntity}
     */
    SzRelatedEntity create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzRelatedEntity} that produces instances of {@link SzRelatedEntityImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzRelatedEntity>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzRelatedEntity.class, SzRelatedEntityImpl.class);
    }

    @Override
    public SzRelatedEntity create() {
      return new SzRelatedEntityImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzRelatedEntity}.
   */
  class Factory extends ModelFactory<SzRelatedEntity, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzRelatedEntity.class);
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
     * Creates a new instance of {@link SzRelatedEntity}.
     * @return The new instance of {@link SzRelatedEntity}.
     */
    public SzRelatedEntity create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a list of resolved entities from a {@link JsonArray} describing a
   * JSON array in the Senzing native API format for entity features and
   * populates the specified {@link List} or creates a new {@link List}.
   *
   * @param list The {@link List} of {@link SzRelatedEntity} instances to
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
   *         SzRelatedEntity} instances.
   */
  static List<SzRelatedEntity> parseRelatedEntityList(
      List<SzRelatedEntity>   list,
      JsonArray               jsonArray,
      Function<String,String> featureToAttrClassMapper)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray == null ? 0 : jsonArray.size());
    }

    if (jsonArray == null) return list;

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseRelatedEntity(null,
                                  jsonObject,
                                  featureToAttrClassMapper));
    }
    return list;
  }

  /**
   * Parses the entity feature from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity feature and populates
   * the specified {@link SzRelatedEntity} or creates a new instance.
   *
   * @param entity The {@link SzRelatedEntity} instance to populate, or
   *               <tt>null</tt> if a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link SzRelatedEntity}.
   */
  static SzRelatedEntity parseRelatedEntity(
      SzRelatedEntity         entity,
      JsonObject              jsonObject,
      Function<String,String> featureToAttrClassMapper)
  {
    if (entity == null) entity = SzRelatedEntity.FACTORY.create();

    Function<String,String> mapper = featureToAttrClassMapper;
    SzBaseRelatedEntity.parseBaseRelatedEntity(entity, jsonObject, mapper);

    if (jsonObject.containsKey("IS_DISCLOSED")) {
      boolean disclosed = jsonObject.getInt("IS_DISCLOSED") != 0;
      entity.setDisclosed(disclosed);
    }

    if (jsonObject.containsKey("IS_AMBIGUOUS")) {
      boolean ambiguous = jsonObject.getInt("IS_AMBIGUOUS") != 0;
      entity.setAmbiguous(ambiguous);
    }

    if (entity.getMatchLevel() != null) {
      if (entity.isDisclosed()) {
        entity.setRelationType(DISCLOSED_RELATION);
      } else if (entity.getMatchLevel() == 2) {
        entity.setRelationType(POSSIBLE_MATCH);
      } else {
        entity.setRelationType(POSSIBLE_RELATION);
      }
    }

    // iterate over the feature map
    return entity;
  }
}
