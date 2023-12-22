package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Provides a default implementation of {@link SzEntityPath}.
 */
@JsonDeserialize
public class SzEntityPathImpl implements SzEntityPath {
  /**
   * The starting entity ID for the path.
   */
  private long startEntityId;

  /**
   * The ending entity ID for the path.
   */
  private long endEntityId;

  /**
   * The {@link List} of entity IDs.
   */
  private List<Long> entityIds;

  /**
   * Package-private default constructor.
   */
  SzEntityPathImpl() {
    this.startEntityId  = 0;
    this.endEntityId    = 0;
    this.entityIds      = null;
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
  public SzEntityPathImpl(long        startEntityId,
                          long        endEntityId,
                          List<Long>  entityIds)
    throws IllegalArgumentException
  {
    this.startEntityId = startEntityId;
    this.endEntityId   = endEntityId;
    this.entityIds = Collections.unmodifiableList(new ArrayList<>(entityIds));
    if (this.entityIds.size() > 0 && this.entityIds.get(0) != startEntityId
        && this.entityIds.get(this.entityIds.size()-1) != endEntityId) {
      throw new IllegalArgumentException(
          "The specified entity IDs list does not start and end with the "
          + "specified starting and ending entity ID.  startEntityId=[ "
          + startEntityId + " ], endEntityId=[ " + endEntityId
          + " ], entityIDs=[ " + entityIds + " ]");
    }
  }

  /**
   * Returns the entity ID of the first entity in the path.
   *
   * @return The entity ID of the first entity in the path.
   */
  public long getStartEntityId() {
    return this.startEntityId;
  }

  /**
   * Returns the entity ID of the last entity in the path.
   *
   * @return The entity ID of the last entity in the path.
   */
  public long getEndEntityId() {
    return this.endEntityId;
  }

  /**
   * Returns the {@link List} of entity IDs identifying the entities in the
   * path in order of the path.
   *
   * @return The {@link List} of entity IDs identifying the entities in the
   *         path in order of the path.
   */
  public List<Long> getEntityIds() {
    return this.entityIds;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || this.getClass() != obj.getClass()) return false;
    SzEntityPathImpl that = (SzEntityPathImpl) obj;
    return this.getStartEntityId() == that.getStartEntityId()
        && this.getEndEntityId() == that.getEndEntityId()
        && this.getEntityIds().equals(that.getEntityIds());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getStartEntityId(),
                        this.getEndEntityId(),
                        this.getEntityIds());
  }

  @Override
  public String toString() {
    return "SzEntityPath{" +
        "startEntityId=" + this.getStartEntityId() +
        ", endEntityId=" + this.getEndEntityId() +
        ", entityIds=" + this.getEntityIds() +
        '}';
  }
}
