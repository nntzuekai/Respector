package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzVirtualEntityData;
import com.senzing.api.model.SzRelatedEntity;
import com.senzing.api.model.SzResolvedEntity;

import java.util.Collections;
import java.util.List;

/**
 * Describes a resolved entity and its related entities.
 *
 */
@JsonDeserialize
public class SzVirtualEntityDataImpl implements SzVirtualEntityData {
  /**
   * The resolved entity.
   */
  private SzResolvedEntity resolvedEntity;

  /**
   * Default constructor.
   */
  public SzVirtualEntityDataImpl() {
    this.resolvedEntity = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzResolvedEntity getResolvedEntity() {
    return resolvedEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setResolvedEntity(SzResolvedEntity resolvedEntity) {
    this.resolvedEntity = resolvedEntity;
  }

  @Override
  public String toString() {
    return "SzVirtualEntityData{" +
        "resolvedEntity=" + resolvedEntity +
        '}';
  }
}
