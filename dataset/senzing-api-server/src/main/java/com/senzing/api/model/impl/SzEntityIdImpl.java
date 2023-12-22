package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityId;
import com.senzing.api.model.SzEntityIdentifier;

import java.util.Objects;

/**
 * Describes an entity ID to identify an entity.
 */
@JsonDeserialize
public class SzEntityIdImpl implements SzEntityId {
  /**
   * The entity ID that identifies the entity.
   */
  private long value;

  /**
   * Constructs with the specified entity ID.
   * @param id The entity ID.
   */
  public SzEntityIdImpl(long id) {
    this.value = id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SzEntityIdImpl that = (SzEntityIdImpl) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return String.valueOf(this.getValue());
  }
}
