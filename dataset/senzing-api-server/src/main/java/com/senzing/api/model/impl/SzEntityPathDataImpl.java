package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityData;
import com.senzing.api.model.SzEntityPath;
import com.senzing.api.model.SzEntityPathData;

import java.util.*;

/**
 * Provides a default implementation of {@link SzEntityPathData}.
 */
@JsonDeserialize
public class SzEntityPathDataImpl implements SzEntityPathData {
  /**
   * The {@link SzEntityPath} describing the entity path.
   */
  private SzEntityPath entityPath;

  /**
   * The {@link List} of {@link SzEntityData} instances describing the entities
   * in the path.
   */
  private List<SzEntityData> entities;

  /**
   * Package-private default constructor.
   */
  SzEntityPathDataImpl() {
    this.entityPath = null;
    this.entities   = null;
  }

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
  public SzEntityPathDataImpl(SzEntityPath        entityPath,
                              List<SzEntityData>  entities)
    throws IllegalArgumentException
  {
    if (entityPath.getEntityIds().size() > 0
        && entities.size() != entityPath.getEntityIds().size()) {
      throw new IllegalArgumentException(
          "The specified entity path and entities list are not consistent.  "
          + "pathSize=[ " + entityPath.getEntityIds().size()
          + " ], entityCount=[ " + entities.size() + " ]");
    }

    // check the sets of entity IDs
    Set<Long> set1 = new HashSet<>();
    Set<Long> set2 = new HashSet<>();
    entities.forEach(e -> set1.add(e.getResolvedEntity().getEntityId()));
    set2.addAll(entityPath.getEntityIds());

    if ((set2.size() > 0)
        && (!set1.containsAll(set2) || !set2.containsAll(set1)))
    {
      throw new IllegalArgumentException(
          "The specified entity path and entities list have different "
          + "entity IDs.  pathEntities=[ " + set2 + " ], listEntities=[ "
          + set1 + " ]");
    }

    this.entityPath = entityPath;
    this.entities = Collections.unmodifiableList(new ArrayList<>(entities));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzEntityPath getEntityPath() {
    return this.entityPath;
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
    return "SzEntityPathData{" +
        "entityPath=" + entityPath +
        ", entities=" + entities +
        '}';
  }
}
