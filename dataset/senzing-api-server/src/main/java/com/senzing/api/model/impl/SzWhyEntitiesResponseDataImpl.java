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
public class SzWhyEntitiesResponseDataImpl implements SzWhyEntitiesResponseData
{
  /**
   * The {@link List} of {@link SzWhyEntitiesResult} instances for the
   * entities.
   */
  private SzWhyEntitiesResult whyResult;

  /**
   * The {@link List} of {@link SzEntityData} instances decribing the
   * entities in the response.
   */
  private List<SzEntityData> entities;

  /**
   * Default constructor.
   */
  public SzWhyEntitiesResponseDataImpl() {
    this.whyResult  = null;
    this.entities   = new LinkedList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzWhyEntitiesResult getWhyResult() {
    return this.whyResult;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setWhyResult(SzWhyEntitiesResult whyResult) {
    this.whyResult = whyResult;
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
