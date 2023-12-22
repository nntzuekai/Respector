package com.senzing.api.model.impl;

import com.senzing.api.model.SzBaseBulkLoadResult;
import com.senzing.api.model.SzBulkLoadError;
import com.senzing.api.model.SzError;

import java.util.Collection;
import java.util.List;

/**
 * Provides a default implementation of {@link SzBaseBulkLoadResult}.
 */
public abstract class SzBaseBulkLoadResultImpl
  implements SzBaseBulkLoadResult
{
  /**
   * The total number of records.
   */
  private int recordCount;

  /**
   * The number of records that were successfully loaded.
   */
  private int loadedRecordCount;

  /**
   * The number of records that are incomplete.
   */
  private int incompleteRecordCount;

  /**
   * The number of records that failed to load.
   */
  private int failedRecordCount;

  /**
   * The tracker for instances of {@link SzBulkLoadError}.
   */
  private SzBulkLoadErrorTracker errorTracker;

  /**
   * Default constructor.
   */
  protected SzBaseBulkLoadResultImpl() {
    this.recordCount            = 0;
    this.loadedRecordCount      = 0;
    this.incompleteRecordCount  = 0;
    this.failedRecordCount      = 0;
    this.errorTracker           = new SzBulkLoadErrorTracker();
  }

  /**
   * Gets the total number of records.
   *
   * @return The total number of records.
   */
  @Override
  public int getRecordCount() {
    return recordCount;
  }

  /**
   * Sets the total number of records.
   *
   * @param recordCount The total number of records.
   */
  @Override
  public void setRecordCount(int recordCount) {
    this.recordCount = recordCount;
  }

  /**
   * Increments the total number of records and returns the new count.
   *
   * @return The total number of records after incrementing.
   */
  @Override
  public long incrementRecordCount() {
    return ++this.recordCount;
  }

  /**
   * Gets the number of records that were successfully loaded.
   *
   * @return The number of records that were successfully loaded.
   */
  @Override
  public int getLoadedRecordCount() {
    return this.loadedRecordCount;
  }

  /**
   * Sets the number of records that were successfully loaded.
   *
   * @param recordCount The number of records that were successfully loaded.
   */
  @Override
  public void setLoadedRecordCount(int recordCount) {
    this.loadedRecordCount = recordCount;
  }

  /**
   * Increments the number of records that were successfully loaded and
   * returns the new count.
   *
   * @return The number of records that were successfully loaded after
   *         incrementing.
   */
  @Override
  public long incrementLoadedRecordCount() {
    return ++this.loadedRecordCount;
  }

  /**
   * Return the number of records associated that are deemed incomplete.
   *
   * @return The number of records that are incomplete.
   */
  @Override
  public int getIncompleteRecordCount() {
    return this.incompleteRecordCount;
  }

  /**
   * Sets the number of records that are incomplete.
   *
   * @param recordCount The number of records that are incomplete.
   */
  @Override
  public void setIncompleteRecordCount(int recordCount) {
    this.incompleteRecordCount = recordCount;
  }

  /**
   * Increments the number of records that are incomplete.
   *
   * @return The incremented incomplete record count.
   */
  @Override
  public int incrementIncompleteRecordCount() {
    return ++this.incompleteRecordCount;
  }

  /**
   * Gets the number of records that failed to load.
   *
   * @return The number of records that failed to load.
   */
  @Override
  public int getFailedRecordCount() {
    return this.failedRecordCount;
  }

  /**
   * Sets the number of records that failed to load.
   *
   * @param recordCount The number of records that failed to load.
   */
  @Override
  public void setFailedRecordCount(int recordCount) {
    this.failedRecordCount = recordCount;
  }

  /**
   * Tracks the specified error and increments the number of records failed
   * to load.
   *
   * @param error The {@link SzError} describing the failure.
   *
   * @return The number of records that failed to load after incrementing.
   */
  @Override
  public long trackFailedRecord(SzError error) {
    this.errorTracker.trackError(error);
    return ++this.failedRecordCount;
  }

  /**
   * Gets the unmodifiable {@link List} of {@link SzBulkLoadError} instances
   * describing the top errors.
   *
   * @return The {@link List} of {@link SzBulkLoadError} instances describing
   * the top errors.
   */
  @Override
  public List<SzBulkLoadError> getTopErrors() {
    return this.errorTracker.getTopErrors();
  }

  /**
   * Sets the {@link List} of {@link SzBulkLoadError} instances describing the
   * top errors.
   *
   * @param errors The list of top errors.
   */
  @Override
  public void setTopErrors(Collection<SzBulkLoadError> errors) {
    this.errorTracker.setTopErrors(errors);
  }

  @Override
  public String toString() {
    return "SzAbstractBulkLoadResult{" +
        "recordCount=" + this.getRecordCount() +
        ", loadedRecordCount=" + this.getLoadedRecordCount() +
        ", incompleteRecordCount=" + this.getIncompleteRecordCount() +
        ", failedRecordCount=" + this.getFailedRecordCount() +
        ", topErrors=[ " + this.getTopErrors() +
        " ]}";
  }
}

