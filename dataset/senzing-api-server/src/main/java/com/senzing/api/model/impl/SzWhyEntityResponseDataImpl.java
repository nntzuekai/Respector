package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a default implementation of {@link SzWhyEntitiesResponseData}.
 */
@JsonDeserialize
public class SzWhyEntityResponseDataImpl implements SzWhyEntityResponseData
{
  /**
   * The {@link List} of {@link SzWhyEntitiesResult} instances for the
   * entities.
   */
  private List<SzWhyEntityResult> whyResults;

  /**
   * The {@link List} of {@link SzEntityData} instances decribing the
   * entities in the response.
   */
  private List<SzEntityData> entities;

  /**
   * Default constructor.
   */
  public SzWhyEntityResponseDataImpl() {
    this.whyResults = new LinkedList<>();
    this.entities   = new LinkedList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzWhyEntityResult> getWhyResults() {
    return Collections.unmodifiableList(this.whyResults);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addWhyResult(SzWhyEntityResult result) {
    this.whyResults.add(result);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setWhyResults(Collection<? extends SzWhyEntityResult> results) {
    this.whyResults.clear();
    if (results != null) {
      this.whyResults.addAll(results);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzEntityData> getEntities() {
    return Collections.unmodifiableList(this.entities);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEntity(SzEntityData entity) {
      this.entities.add(entity);
    }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntities(Collection<? extends SzEntityData> entities) {
    this.entities.clear();
    if (entities != null) this.entities.addAll(entities);
  }
}
