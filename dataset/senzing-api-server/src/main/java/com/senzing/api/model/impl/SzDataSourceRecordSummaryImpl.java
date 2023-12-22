package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzDataSourceRecordSummary;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describes a record summary by data source.
 */
@JsonDeserialize
public class SzDataSourceRecordSummaryImpl
    implements SzDataSourceRecordSummary
{
  /**
   * The data source for the record summary.
   */
  private String dataSource;

  /**
   * The number of records in the entity from the data source.
   */
  private int recordCount;

  /**
   * The list of record IDs for the entity from the data source.
   */
  private List<String> topRecordIds;

  /**
   * Default constructor.
   */
  public SzDataSourceRecordSummaryImpl() {
    this(null, 0);
  }

  /**
   * Constructs with the specified data source and record count.
   *
   * @param dataSource The data source associated with the summary.
   * @param recordCount The number of records from the data source in
   *                    the entity.
   */
  public SzDataSourceRecordSummaryImpl(String dataSource, int recordCount) {
    this.dataSource   = dataSource;
    this.recordCount  = recordCount;
    this.topRecordIds = new LinkedList<>();
  }

  /**
   * Returns the associated data source.
   *
   * @return The associated data source.
   */
  @Override
  public String getDataSource() {
    return this.dataSource;
  }

  /**
   * Sets the associated data source.
   *
   * @param dataSource The data source for the summary.
   */
  @Override
  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Returns the record count for the summary.
   *
   * @return The record count for the summary.
   */
  @Override
  public int getRecordCount() {
    return this.recordCount;
  }

  /**
   * Sets the record count for the summary.
   *
   * @param recordCount The number of records in the entity from the
   *                    data source.
   */
  @Override
  public void setRecordCount(int recordCount) {
    this.recordCount = recordCount;
  }

  /**
   * Returns an unmodifiable {@link List} of the top record IDs.
   *
   * @return An unmodifiable {@link List} of the top record IDs.
   */
  @Override
  public List<String> getTopRecordIds() {
    return Collections.unmodifiableList(this.topRecordIds);
  }

  /**
   * Sets the top record IDs to the specified {@link List} of record IDs.
   *
   * @param topRecordIds The top record IDs for the data source.
   */
  @Override
  public void setTopRecordIds(List<String> topRecordIds) {
    this.topRecordIds.clear();
    if (topRecordIds != null) {
      this.topRecordIds.addAll(topRecordIds);
    }
  }

  /**
   * Adds a record ID to the {@link List} of top record IDs for the summary.
   *
   * @param recordId The record ID to add to the list of top record IDs.
   */
  @Override
  public void addTopRecordId(String recordId) {
    this.topRecordIds.add(recordId);
  }


  @Override
  public String toString() {
    return "SzRecordSummary{" +
        "dataSource='" + dataSource + '\'' +
        ", recordCount=" + recordCount +
        ", topRecordIds=" + topRecordIds +
        '}';
  }
}
