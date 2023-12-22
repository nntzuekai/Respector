package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzFlaggedEntity;
import com.senzing.api.model.SzResolutionInfo;
import java.util.*;

/**
 * Provides a default implementation of {@link SzResolutionInfo}.
 */
@JsonDeserialize
public class SzResolutionInfoImpl implements SzResolutionInfo {
  /**
   * The data source for the focus record.
   */
  private String dataSource;

  /**
   * The record ID for the focus record.
   */
  private String recordId;

  /**
   * The {@link List} of {@link Long} entity ID's for the affected entities.
   */
  private Set<Long> affectedEntities;

  /**
   * The {@link List} of {@link SzFlaggedEntity} instances describing the
   * flagged entities.
   */
  private List<SzFlaggedEntity> flaggedEntities;

  /**
   * Default constructor.
   */
  public SzResolutionInfoImpl() {
    this.dataSource       = null;
    this.recordId         = null;
    this.affectedEntities = new LinkedHashSet<>();
    this.flaggedEntities  = new LinkedList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDataSource() {
    return dataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRecordId() {
    return recordId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Long> getAffectedEntities() {
    return Collections.unmodifiableSet(this.affectedEntities);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addAffectedEntity(Long entityId) {
    if (entityId != null) this.affectedEntities.add(entityId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAffectedEntities(Collection<Long> affectedEntities) {
    this.affectedEntities.clear();
    if (affectedEntities != null) {
      for (Long entityId : affectedEntities) {
        if (entityId != null) {
          this.affectedEntities.add(entityId);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzFlaggedEntity> getFlaggedEntities() {
    return Collections.unmodifiableList(this.flaggedEntities);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFlaggedEntity(SzFlaggedEntity flaggedEntity) {
    if (flaggedEntity != null) this.flaggedEntities.add(flaggedEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFlaggedEntities(Collection<SzFlaggedEntity> flaggedEntities) {
    this.flaggedEntities.clear();
    if (flaggedEntities != null) {
      for (SzFlaggedEntity entity : flaggedEntities) {
        this.flaggedEntities.add(entity);
      }
    }
  }

  @Override
  public String toString() {
    return "SzResolutionInfo{" +
        "dataSource='" + dataSource + '\'' +
        ", recordId='" + recordId + '\'' +
        ", affectedEntities=" + affectedEntities +
        ", flaggedEntities=" + flaggedEntities +
        '}';
  }
}
