package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityPathImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;
import java.util.function.Function;

/**
 * Represents a path between entities consisting of one or more entity IDs
 * identifying the entities in the path in order.
 */
@JsonDeserialize(using=SzEntityPath.Factory.class)
public interface SzEntityPath {
  /**
   * Returns the entity ID of the first entity in the path.
   *
   * @return The entity ID of the first entity in the path.
   */
  long getStartEntityId();

  /**
   * Returns the entity ID of the last entity in the path.
   *
   * @return The entity ID of the last entity in the path.
   */
  long getEndEntityId();

  /**
   * Returns the {@link List} of entity IDs identifying the entities in the
   * path in order of the path.
   *
   * @return The {@link List} of entity IDs identifying the entities in the
   *         path in order of the path.
   */
  List<Long> getEntityIds();

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityPath}.
   */
  interface Provider extends ModelProvider<SzEntityPath> {
    /**
     * Constructs with the specified list of entity IDs.
     *
     * @param startEntityId The starting entity ID for the path.
     *
     * @param endEntityId The ending entity ID for the path.
     *
     * @param entityIds The {@link List} of entity IDs, or an empty
     *                  {@link List} if there is no path between the entities.
     *
     * @throws IllegalArgumentException If the specified {@link List} contains
     *                                  duplicate entity IDs is empty.
     */
    SzEntityPath create(long        startEntityId,
                        long        endEntityId,
                        List<Long>  entityIds);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityPath} that produces instances of {@link SzEntityPathImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityPath>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityPath.class, SzEntityPathImpl.class);
    }

    @Override
    public SzEntityPath create(long        startEntityId,
                               long        endEntityId,
                               List<Long>  entityIds)
    {
      return new SzEntityPathImpl(startEntityId, endEntityId, entityIds);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzEntityPath}.
   */
  class Factory extends ModelFactory<SzEntityPath, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityPath.class);
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
     * Constructs with the specified list of entity IDs.
     *
     * @param startEntityId The starting entity ID for the path.
     *
     * @param endEntityId The ending entity ID for the path.
     *
     * @param entityIds The {@link List} of entity IDs, or an empty
     *                  {@link List} if there is no path between the entities.
     *
     * @throws IllegalArgumentException If the specified {@link List} contains
     *                                  duplicate entity IDs is empty.
     */
    public SzEntityPath create(long       startEntityId,
                               long       endEntityId,
                               List<Long> entityIds)
        throws IllegalArgumentException
    {
      return this.getProvider().create(startEntityId, endEntityId, entityIds);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a list of entity path instances from a {@link JsonArray}
   * describing a JSON array in the Senzing native API format for entity
   * features and populates the specified {@link List} or creates a new
   * {@link List}.
   *
   * @param list The {@link List} of {@link SzEntityPath} instances to
   *             populate, or <tt>null</tt> if a new {@link List}
   *             should be created.
   *
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   *
   * @return The populated (or created) {@link List} of {@link
   *         SzEntityPath} instances.
   */
  static List<SzEntityPath> parseEntityPathList(
      List<SzEntityPath>      list,
      JsonArray               jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseEntityPath(jsonObject));
    }
    return list;
  }

  /**
   * Parses the entity feature from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity feature and populates
   * the specified {@link SzEntityPath} or creates a new instance.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @return The populated (or created) {@link SzEntityPath}.
   */
  static SzEntityPath parseEntityPath(JsonObject jsonObject)
  {
    Long startId = JsonUtilities.getLong(jsonObject, "START_ENTITY_ID");
    Long endId = JsonUtilities.getLong(jsonObject, "END_ENTITY_ID");
    JsonArray entities = jsonObject.getJsonArray("ENTITIES");
    int count = entities.size();

    List<Long> list = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      list.add(entities.getJsonNumber(index).longValue());
    }

    return SzEntityPath.FACTORY.create(startId, endId, list);
  }
}
