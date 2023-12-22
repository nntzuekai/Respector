package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzFocusRecordId;
import java.util.Objects;

/**
 * Describes a record ID with a data source.
 */
@JsonDeserialize
public class SzFocusRecordIdImpl implements SzFocusRecordId {
  /**
   * The data source code.
   */
  private String dataSource;

  /**
   * The record ID identifying the record within the data source.
   */
  private String recordId;

  /**
   * Default constructor.
   */
  private SzFocusRecordIdImpl() {
    this.dataSource = null;
    this.recordId = null;
  }

  /**
   * Constructs with the specified data source code and record ID.
   *
   * @param dataSource The data source code.
   * @param recordId The record ID identifying the record.
   */
  public SzFocusRecordIdImpl(String dataSource, String recordId) {
    this.dataSource = (dataSource != null)
        ? dataSource.toUpperCase().trim() : null;
    this.recordId = (recordId != null) ? recordId.trim() : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDataSource() {
    return dataSource;
  }

  /**
   * Sets the data source code for the record.
   *
   * @param dataSource The data source code for the record.
   */
  private void setDataSource(String dataSource) {
    this.dataSource = dataSource.toUpperCase().trim();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRecordId() {
    return recordId;
  }

  /**
   * Sets the record ID identifying the record.
   *
   * @param recordId The record ID identifying the record.
   */
  private void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SzFocusRecordIdImpl recordId1 = (SzFocusRecordIdImpl) o;
    return Objects.equals(getDataSource(), recordId1.getDataSource()) &&
        Objects.equals(getRecordId(), recordId1.getRecordId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDataSource(), getRecordId());
  }

  @Override
  public String toString() {
    return "SzFocusRecordId{" +
        "dataSource='" + this.getDataSource() + '\'' +
        ", recordId='" + this.getRecordId() + '\'' +
        '}';
  }
}
