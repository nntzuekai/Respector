package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityData;
import com.senzing.api.model.SzEntityNetworkData;
import com.senzing.api.model.SzEntityPath;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;
import java.util.function.Function;

/**
 * Provides a default implementation of {@link SzEntityNetworkData}.
 */
@JsonDeserialize
public class SzEntityNetworkDataImpl implements SzEntityNetworkData {
  /**
   * The {@link List} of {@link SzEntityPath} describing the entity paths.
   */
  private List<SzEntityPath> entityPaths;

  /**
   * The {@link List} of {@link SzEntityData} instances describing the entities
   * in the path.
   */
  private List<SzEntityData> entities;

  /**
   * Package-private default constructor.
   */
  public SzEntityNetworkDataImpl() {
    this.entityPaths  = null;
    this.entities     = null;
  }

  /**
   * Constructs with the specified {@link List} of {@link SzEntityPath}
   * instances and {@link List} of {@link SzEntityData} instances describing
   * the entities in the path.
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
  public SzEntityNetworkDataImpl(List<SzEntityPath>  entityPaths,
                                 List<SzEntityData>  entities)
    throws IllegalArgumentException
  {
    // check the sets of entity IDs
    Set<Long> set1 = new HashSet<>();
    Set<Long> set2 = new HashSet<>();
    entities.forEach(e -> set1.add(e.getResolvedEntity().getEntityId()));
    entityPaths.forEach(entityPath -> set2.addAll(entityPath.getEntityIds()));

    if (!set1.containsAll(set2)) {
      throw new IllegalArgumentException(
          "Some of the entities on the paths are not in included in the "
          + "enitty list.  pathEntities=[ " + set2 + " ], listEntities=[ "
          + set1 + " ]");
    }

    this.entityPaths = Collections.unmodifiableList(
        new ArrayList<>(entityPaths));
    this.entities = Collections.unmodifiableList(new ArrayList<>(entities));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzEntityPath> getEntityPaths() {
    return this.entityPaths;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzEntityData> getEntities() {
    return this.entities;
  }

  @Override
  public String toString() {
    return "SzEntityNetworkData{" +
        "entityPaths=" + entityPaths +
        ", entities=" + entities +
        '}';
  }
}
