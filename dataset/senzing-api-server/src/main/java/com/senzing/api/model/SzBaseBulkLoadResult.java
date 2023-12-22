package com.senzing.api.model;

import java.util.Collection;
import java.util.List;

/**
 * Provides the minimum set of properties for describing the load of a set of
 * bulk data records either in entirety or by some aggregate group (e.g.: by
 * data source).
 */
public interface SzBaseBulkLoadResult {
  /**
   * Gets the total number of records.
   *
   * @return The total number of records.
   */
  int getRecordCount();

  /**
   * Sets the total number of records.
   *
   * @param recordCount The total number of records.
   */
  void setRecordCount(int recordCount);

  /**
   * Increments the total number of records and returns the new count.
   *
   * @return The total number of records after incrementing.
   */
  long incrementRecordCount();

  /**
   * Gets the number of records that were successfully loaded.
   *
   * @return The number of records that were successfully loaded.
   */
  int getLoadedRecordCount();

  /**
   * Sets the number of records that were successfully loaded.
   *
   * @param recordCount The number of records that were successfully loaded.
   */
  void setLoadedRecordCount(int recordCount);

  /**
   * Increments the number of records that were successfully loaded and
   * returns the new count.
   *
   * @return The number of records that were successfully loaded after
   *         incrementing.
   */
  long incrementLoadedRecordCount();

  /**
   * Return the number of records associated that are deemed incomplete.
   *
   * @return The number of records that are incomplete.
   */
  int getIncompleteRecordCount();

  /**
   * Sets the number of records that are incomplete.
   *
   * @param recordCount The number of records that are incomplete.
   */
  void setIncompleteRecordCount(int recordCount);

  /**
   * Increments the number of records that are incomplete.
   *
   * @return The incremented incomplete record count.
   */
  int incrementIncompleteRecordCount();

  /**
   * Gets the number of records that failed to load.
   *
   * @return The number of records that failed to load.
   */
  int getFailedRecordCount();

  /**
   * Sets the number of records that failed to load.
   *
   * @param recordCount The number of records that failed to load.
   */
  void setFailedRecordCount(int recordCount);

  /**
   * Tracks the specified error and increments the number of records failed
   * to load.
   *
   * @param error The {@link SzError} describing the failure.
   *
   * @return The number of records that failed to load after incrementing.
   */
  long trackFailedRecord(SzError error);

  /**
   * Gets the unmodifiable {@link List} of {@link SzBulkLoadError} instances
   * describing the top errors.
   *
   * @return The {@link List} of {@link SzBulkLoadError} instances describing
   * the top errors.
   */
  List<SzBulkLoadError> getTopErrors();

  /**
   * Sets the {@link List} of {@link SzBulkLoadError} instances describing the
   * top errors.
   *
   * @param errors The list of top errors.
   */
  void setTopErrors(Collection<SzBulkLoadError> errors);
}

