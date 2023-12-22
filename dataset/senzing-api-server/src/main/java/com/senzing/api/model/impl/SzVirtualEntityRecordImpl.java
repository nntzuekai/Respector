package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzVirtualEntityRecord;

import java.util.Objects;

/**
 * Describes a record ID with a data source.
 */
@JsonDeserialize
public class SzVirtualEntityRecordImpl implements SzVirtualEntityRecord {
  /**
   * The data source code.
   */
  private String dataSource;

  /**
   * The record ID identifying the record within the data source.
   */
  private String recordId;

  /**
   * The internal ID associated with the record.  Instances with the same
   * internal ID are considered to be effectively identical for the purpose of
   * entity resolution.
   */
  private Long internalId;

  /**
   * Default constructor.
   */
  private SzVirtualEntityRecordImpl() {
    this(null, null, null);
  }

  /**
   * Constructs with the specified data source code, record ID and internal ID.
   *
   * @param dataSource The data source code.
   * @param recordId The record ID identifying the record.
   * @param internalId The internal ID associated with the virtual entity
   *                   record.
   */
  public SzVirtualEntityRecordImpl(String dataSource,
                                   String recordId,
                                   Long   internalId)
  {
    this.dataSource = (dataSource != null)
        ? dataSource.toUpperCase().trim() : null;
    this.recordId   = (recordId != null) ? recordId.trim() : null;
    this.internalId = internalId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDataSource() {
    return this.dataSource;
  }

  /**
   * Sets the data source code for the record.
   *
   * @param dataSource The data source code for the record.
   */
  private void setDataSource(String dataSource) {
    this.dataSource = (dataSource == null) ? null
        : dataSource.toUpperCase().trim();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRecordId() {
    return this.recordId;
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
  public Long getInternalId() {
    return this.internalId;
  }

  /**
   * Sets the internal ID for this instances.  Instances with the same internal
   * ID identify records that are effectively identical for the purposes of
   * entity resolution.
   *
   * @param internalId The internal ID for this instance.
   */
  public void setInternalId(Long internalId) {
    this.internalId = internalId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SzVirtualEntityRecordImpl that = (SzVirtualEntityRecordImpl) o;
    return Objects.equals(this.getDataSource(), that.getDataSource())
        && Objects.equals(this.getRecordId(), that.getRecordId())
        && Objects.equals(this.getInternalId(), that.getInternalId());

  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getDataSource(),
                        this.getRecordId(),
                        this.getInternalId());
  }

  @Override
  public String toString() {
    return "SzVirtualEntityRecord{" +
        "dataSource=[ " + this.getDataSource()
        + " ], recordId=[ " + this.getRecordId()
        + " ], internalId=[ " + this.getInternalId()
        + " ]}";
  }
}
