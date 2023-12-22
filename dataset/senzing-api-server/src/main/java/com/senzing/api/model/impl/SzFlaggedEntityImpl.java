package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzFlaggedEntity;
import com.senzing.api.model.SzFlaggedRecord;
import java.util.*;

/**
 * Provides a default implementation for {@link SzFlaggedEntity}.
 */
@JsonDeserialize
public class SzFlaggedEntityImpl implements SzFlaggedEntity {
  /**
   * The entity ID of this entity.
   */
  private Long entityId;

  /**
   * The number of degrees that this entity is separated from the resolved
   * entity.
   */
  private Integer degrees;

  /**
   * The {@link Set} of {@link String} flags for this entity.
   */
  private Set<String> flags;

  /**
   * The {@link List} of {@link SzFlaggedRecord} instances.
   */
  private List<SzFlaggedRecord> sampleRecords;

  /**
   * Default constructor.
   */
  public SzFlaggedEntityImpl() {
    this.entityId       = null;
    this.degrees        = null;
    this.flags          = new LinkedHashSet<>();
    this.sampleRecords  = new LinkedList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getEntityId() {
    return entityId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntityId(Long entityId) {
    this.entityId = entityId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getDegrees() {
    return degrees;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDegrees(Integer degrees) {
    this.degrees = degrees;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getFlags() {
    return Collections.unmodifiableSet(this.flags);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFlag(String flag) {
    if (flag != null) this.flags.add(flag);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFlags(Set<String> flags) {
    this.flags.clear();
    if (flags != null) {
      for (String flag : flags) {
        if (flag != null) this.flags.add(flag);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzFlaggedRecord> getSampleRecords() {
    return Collections.unmodifiableList(sampleRecords);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addSampleRecord(SzFlaggedRecord sampleRecord) {
    if (sampleRecord != null) this.sampleRecords.add(sampleRecord);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSampleRecords(Collection<SzFlaggedRecord> sampleRecords) {
    this.sampleRecords.clear();
    if (sampleRecords != null) {
      for (SzFlaggedRecord record: sampleRecords) {
        if (record != null) this.sampleRecords.add(record);
      }
    }
  }

  @Override
  public String toString() {
    return "SzFlaggedEntity{" +
        "entityId=" + entityId +
        ", degrees=" + degrees +
        ", flags=" + flags +
        ", sampleRecords=" + sampleRecords +
        '}';
  }
}
