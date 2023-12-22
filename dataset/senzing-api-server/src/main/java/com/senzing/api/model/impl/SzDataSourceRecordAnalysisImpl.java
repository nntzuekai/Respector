package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzDataSourceRecordAnalysis;

/**
 * Provides a default implementation of {@link SzDataSourceRecordAnalysis}.
 */
@JsonDeserialize
public class SzDataSourceRecordAnalysisImpl
    implements SzDataSourceRecordAnalysis
{
  /**
   * The associated data source or <tt>null</tt>.
   */
  private String dataSource;

  /**
   * The number of records with the associated data source.
   */
  private int recordCount;

  /**
   * The number of records with the associated data source that have a
   * <tt>"RECORD_ID"</tt> specified.
   */
  private int recordIdCount;

  /**
   * Default constructor that constructs with a <tt>null</tt> data source.
   */
  public SzDataSourceRecordAnalysisImpl() {
    this(null);
  }

  /**
   * Constructs with the specified data source.
   *
   * @param dataSource The data source or <tt>null</tt> if the constructed
   *                   instance is associated with those records that have
   *                   no data source.
   */
  public SzDataSourceRecordAnalysisImpl(String dataSource) {
    this.dataSource       = dataSource;
    this.recordCount      = 0;
    this.recordIdCount    = 0;
  }

  /**
   * Returns the data source with which this instance was constructed.
   *
   * @return The data source with which this instance was constructed.
   */
  @Override
  public String getDataSource() {
    return dataSource;
  }

  /**
   * Gets the number of records that have the associated data source.
   *
   * @return The number of records that have the associated data source.
   */
  @Override
  public int getRecordCount() {
    return recordCount;
  }

  /**
   * Sets the number of records that have the associated data source.
   *
   * @param recordCount The number of records that have the associated
   *                    data source.
   */
  @Override
  public void setRecordCount(int recordCount) {
    this.recordCount = recordCount;
  }

  /**
   * Increments the number of records that have the associated data source
   * and returns the new count.
   *
   * @return The new count after incrementing.
   */
  @Override
  public long incrementRecordCount() {
    return ++this.recordCount;
  }

  /**
   * Increments the number of records that have the associated data source
   * and returns the new count.
   *
   * @param increment The number of records to increment by.
   *
   * @return The new count after incrementing.
   */
  @Override
  public long incrementRecordCount(int increment) {
    this.recordCount += increment;
    return this.recordCount;
  }

  /**
   * Gets the number of records that have the associated data source and also
   * have a <tt>"RECORD_ID"</tt>.
   *
   * @return The number of records that have the associated data source and
   *         also have a <tt>"RECORD_ID"</tt>.
   */
  @Override
  public int getRecordsWithRecordIdCount() {
    return recordIdCount;
  }

  /**
   * Sets the number of records that have the associated data source and also
   * have a <tt>"RECORD_ID"</tt>.
   *
   * @param recordIdCount The number of records that have the associated
   *                      data source and also have a <tt>"RECORD_ID"</tt>.
   */
  @Override
  public void setRecordsWithRecordIdCount(int recordIdCount) {
    this.recordIdCount = recordIdCount;
  }

  /**
   * Increments the number of records that have the associated data source
   * and also have a <tt>"RECORD_ID"</tt> and returns the new count.
   *
   * @return The new count after incrementing.
   */
  @Override
  public int incrementRecordsWithRecordIdCount() {
    return ++this.recordIdCount;
  }

  /**
   * Increments the number of records that have the associated data source
   * and also have a <tt>"RECORD_ID"</tt> and returns the new count.
   *
   * @param increment The number of records to increment by.
   *
   * @return The new count after incrementing.
   */
  @Override
  public int incrementRecordsWithRecordIdCount(int increment) {
    this.recordIdCount += increment;
    return this.recordIdCount;
  }

  @Override
  public String toString() {
    return "SzDataSourceRecordAnalysis{" +
        "dataSource='" + dataSource + '\'' +
        ", recordCount=" + recordCount +
        ", recordIdCount=" + recordIdCount +
        '}';
  }
}

