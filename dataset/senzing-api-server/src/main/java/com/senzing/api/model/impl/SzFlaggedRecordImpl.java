package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzFlaggedRecord;
import java.util.*;

/**
 * Provides a default implementation for {@link SzFlaggedRecord}
 */
@JsonDeserialize
public class SzFlaggedRecordImpl implements SzFlaggedRecord {
  /**
   * The data source for the record.
   */
  private String dataSource;

  /**
   * The record ID for the record.
   */
  private String recordId;

  /**
   * The {@link Set} of flags for the record.
   */
  private Set<String> flags;

  /**
   * Default constructor.
   */
  public SzFlaggedRecordImpl() {
    this.dataSource = null;
    this.recordId = null;
    this.flags = new LinkedHashSet<>();
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
  public Set<String> getFlags() {
    return Collections.unmodifiableSet(this.flags);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFlag(String flag) {
    this.flags.add(flag);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFlags(Collection<String> flags) {
    this.flags.clear();
    if (flags != null) {
      for (String flag : flags) {
        if (flag != null) this.flags.add(flag);
      }
    }
  }

  @Override
  public String toString() {
    return "SzFlaggedRecord{" +
        "dataSource='" + dataSource + '\'' +
        ", recordId='" + recordId + '\'' +
        ", flags=" + flags +
        '}';
  }
}
