package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityData;
import com.senzing.api.model.SzWhyRecordsResponse;
import com.senzing.api.model.SzWhyRecordsResponseData;
import com.senzing.api.model.SzWhyRecordsResult;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a default implementation of {@link SzWhyRecordsResponseData}.
 */
@JsonDeserialize
public class SzWhyRecordsResponseDataImpl implements SzWhyRecordsResponseData
{
  /**
   * The {@link List} of {@link SzWhyRecordsResult} instances for the
   * entities.
   */
  private SzWhyRecordsResult whyResult;

  /**
   * The {@link List} of {@link SzEntityData} instances decribing the
   * entities in the response.
   */
  private List<SzEntityData> entities;

  /**
   * Default constructor.
   */
  public SzWhyRecordsResponseDataImpl() {
    this.whyResult  = null;
    this.entities   = new LinkedList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzWhyRecordsResult getWhyResult() {
    return this.whyResult;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setWhyResult(SzWhyRecordsResult whyResult) {
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
