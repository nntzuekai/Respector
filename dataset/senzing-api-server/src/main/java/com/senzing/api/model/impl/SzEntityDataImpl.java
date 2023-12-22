package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityData;
import com.senzing.api.model.SzRelatedEntity;
import com.senzing.api.model.SzResolvedEntity;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Describes a resolved entity and its related entities.
 *
 */
@JsonDeserialize
public class SzEntityDataImpl implements SzEntityData {
  /**
   * The resolved entity.
   */
  private SzResolvedEntity resolvedEntity;

  /**
   * The entities related to the resolved entity.
   */
  private List<SzRelatedEntity> relatedEntities;

  /**
   * Default constructor.
   */
  public SzEntityDataImpl() {
    this.resolvedEntity = null;
    this.relatedEntities = new LinkedList<>();
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

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzRelatedEntity> getRelatedEntities() {
    return Collections.unmodifiableList(this.relatedEntities);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRelatedEntities(List<SzRelatedEntity> relatedEntities) {
    this.relatedEntities.clear();
    if (relatedEntities != null) {
      this.relatedEntities.addAll(relatedEntities);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRelatedEntity(SzRelatedEntity relatedEntity) {
    if (relatedEntity != null) {
      this.relatedEntities.add(relatedEntity);
    }
  }

  @Override
  public String toString() {
    return "SzEntityData{" +
        "resolvedEntity=" + resolvedEntity +
        ", relatedEntities=" + relatedEntities +
        '}';
  }
}
