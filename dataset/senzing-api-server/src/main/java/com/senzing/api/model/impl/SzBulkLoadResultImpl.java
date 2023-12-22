package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Fallible;

import java.util.*;

import static com.senzing.api.model.SzBulkDataStatus.IN_PROGRESS;
import static com.senzing.api.model.SzBulkDataStatus.NOT_STARTED;

/**
 * Provides a default implementation of {@link SzBulkLoadResult}.
 */
@JsonDeserialize
public class SzBulkLoadResultImpl extends SzBaseBulkLoadResultImpl
    implements SzBulkLoadResult
{
  /**
   * The character encoding used to interpret the bulk data file.
   */
  private String characterEncoding;

  /**
   * The media type of the bulk data file.
   */
  private String mediaType;

  /**
   * The number of incomplete records that are missing a DATA_SOURCE.
   */
  private int missingDataSourceCount;

  /**
   * The status of the bulk load.
   */
  private SzBulkDataStatus status;

  /**
   * Internal {@link Map} for tracking the analysis by data source.
   */
  private Map<String, SzDataSourceBulkLoadResult> resultsByDataSource;

  /**
   * Default constructor.
   */
  public SzBulkLoadResultImpl() {
    this.missingDataSourceCount = 0;
    this.status = NOT_STARTED;
    this.resultsByDataSource = new HashMap<>();
  }

  /**
   * Gets the {@linkplain SzBulkDataStatus status} of the bulk load.
   *
   * @return The status of the bulk load.
   */
  public SzBulkDataStatus getStatus() {
    return status;
  }

  /**
   * Sets the {@linkplain SzBulkDataStatus status} of the bulk load.
   *
   * @param status The status of the bulk load.
   */
  public void setStatus(SzBulkDataStatus status) {
    this.status = status;
  }

  /**
   * Gets the character encoding with which the records were processed.
   *
   * @return The character encoding with which the records were processed.
   */
  public String getCharacterEncoding() {
    return this.characterEncoding;
  }

  /**
   * Sets the character encoding with which the bulk data was processed.
   *
   * @param encoding The character encoding used to process the bulk data.
   */
  public void setCharacterEncoding(String encoding) {
    this.characterEncoding = encoding;
  }

  /**
   * Gets the media type of the bulk record data.
   *
   * @return The media type of the bulk record data.
   */
  public String getMediaType() {
    return this.mediaType;
  }

  /**
   * Sets the media type of the bulk record data.
   *
   * @param mediaType The media type of the bulk record data.
   */
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  /**
   * Return the number of records that are incomplete because they are missing
   * the <tt>"DATA_SOURCE"</tt> field.
   *
   * @return The number of records that are incomplete because they are missing
   *         a <tt>"DATA_SOURCE"</tt> field.
   */
  public int getMissingDataSourceCount() {
    return this.missingDataSourceCount;
  }

  /**
   * Sets the number of records that are incomplete because they are missing
   * the <tt>"DATA_SOURCE"</tt> field.
   *
   * @param recordCount The number of records that are incomplete because they
   *                    are missing a <tt>"DATA_SOURCE"</tt> field.
   */
  private void setMissingDataSourceCount(int recordCount) {
    this.missingDataSourceCount = recordCount;
  }

  /**
   * Increments the number of records that are incomplete because they are
   * missing the <tt>"DATA_SOURCE"</tt> field.
   *
   * @return The incremented the count of records missing a data source.
   */
  protected int incrementMissingDataSourceCount() {
    return ++this.missingDataSourceCount;
  }

  /**
   * Internal method to help sort instances of {@link SzBaseBulkLoadResult}
   */
  protected static int compareCounts(SzBaseBulkLoadResult r1,
                                     SzBaseBulkLoadResult r2)
  {
    int diff = r1.getRecordCount() - r2.getRecordCount();
    if (diff != 0) return diff;
    diff = r1.getLoadedRecordCount() - r2.getLoadedRecordCount();
    if (diff != 0) return diff;
    return r1.getFailedRecordCount() - r2.getFailedRecordCount();
  }

  /**
   * Gets the list of {@link SzDataSourceBulkLoadResult} instances for the
   * bulk data load describing the statistics by data source.
   *
   * @return A {@link List} of {@link SzDataSourceBulkLoadResult} instances
   * describing the statistics for the bulk data load.
   */
  public List<SzDataSourceBulkLoadResult> getResultsByDataSource() {
    List<SzDataSourceBulkLoadResult> list
        = new ArrayList<>(this.resultsByDataSource.values());
    list.sort((r1, r2) -> {
      int diff = compareCounts(r1, r2);
      if (diff == 0) {
        String ds1 = r1.getDataSource();
        String ds2 = r2.getDataSource();
        if (ds1 == null) return -1;
        if (ds2 == null) return 1;
        return ds1.compareTo(ds2);
      } else {
        return diff;
      }
    });
    return list;
  }

  /**
   * Set the bulk load results by data source for this instance.  This will
   * reset the top-level counts according to what is discovered in the specified
   * collection of {@link SzDataSourceBulkLoadResult} instances.
   *
   * @param resultList The {@link Collection} of
   *                   {@link SzDataSourceBulkLoadResult} instances.
   */
  protected void setResultsByDataSource(
      Collection<SzDataSourceBulkLoadResult> resultList)
  {
    // count the records
    int recordCount = resultList.stream()
        .mapToInt(SzDataSourceBulkLoadResult::getRecordCount).sum();
    this.setRecordCount(recordCount);

    // count the records that have been loaded
    int loadedCount = resultList.stream()
        .filter(a -> a.getDataSource() != null)
        .mapToInt(SzDataSourceBulkLoadResult::getLoadedRecordCount)
        .sum();
    this.setLoadedRecordCount(loadedCount);

    // count the records that failed to load
    int failedCount = resultList.stream()
        .filter(a -> a.getDataSource() != null)
        .mapToInt(SzDataSourceBulkLoadResult::getFailedRecordCount)
        .sum();
    this.setFailedRecordCount(failedCount);

    // clear the current analysis map and repopulate it
    this.resultsByDataSource.clear();
    for (SzDataSourceBulkLoadResult loadResult : resultList) {
      this.resultsByDataSource.put(loadResult.getDataSource(), loadResult);
    }
  }

  /**
   * Utility method for tracking the successful loading of a record with the
   * specified non-null data source.
   *
   * @param dataSource The non-null data source for the record.
   * @throws NullPointerException If the specified parameter is <tt>null</tt>.
   */
  @Override
  public void trackLoadedRecord(String dataSource) {
    Objects.requireNonNull(dataSource, "The data source cannot be null");

    // get the results for that data source
    SzDataSourceBulkLoadResult dsrcResult
        = this.getDataSourceResult(dataSource);

    // increment the record counts
    dsrcResult.incrementRecordCount();
    this.incrementRecordCount();

    dsrcResult.incrementLoadedRecordCount();
    this.incrementLoadedRecordCount();
    if (this.status == NOT_STARTED) this.status = IN_PROGRESS;
  }

  /**
   * Utility method for tracking a failed attempt to load a record with the
   * specified non-null data source.  The failure is recorded with the specified
   * error code and error message.
   *
   * @param dataSource The data source for the record, or <tt>null</tt> if it
   *                   does not have an <tt>"DATA_SOURCE"</tt> property.
   * @param errorCode  The error code for the failure.
   * @param errorMsg   The error message associated with the failure.
   */
  public void trackFailedRecord(String dataSource,
                                String errorCode,
                                String errorMsg)
  {
    this.trackFailedRecord(
        dataSource, SzError.FACTORY.create(errorCode, errorMsg));
  }

  /**
   * Utility method for tracking a failed attempt to load a record with the
   * specified non-null data source.  The failure is recorded with the specified
   * error code and error message.
   *
   * @param dataSource The data source for the record, or <tt>null</tt> if it
   *                   does not have a <tt>"DATA_SOURCE"</tt> property.
   * @param g2Fallible The {@link G2Fallible} instance that had the failure.
   */
  public void trackFailedRecord(String dataSource, G2Fallible g2Fallible) {
    this.trackFailedRecord(
        dataSource, SzError.FACTORY.create(g2Fallible));
  }

  /**
   * Utility method for tracking a failed attempt to load a record with the
   * specified non-null data source.  The failure is recorded with the specified
   * error code and error message.
   *
   * @param dataSource The data source for the record, or <tt>null</tt> if it
   *                   does not have a <tt>"DATA_SOURCE"</tt> property.
   * @param error      The {@link SzError} describing the error that occurred.
   */
  public void trackFailedRecord(String dataSource, SzError error) {
    // get the analysis for that data source
    SzDataSourceBulkLoadResult dsrcResult
        = this.getDataSourceResult(dataSource);

    // increment the global grouped record counts
    dsrcResult.incrementRecordCount();
    this.incrementRecordCount();

    // increment the failed record counts
    dsrcResult.trackFailedRecord(error);
    super.trackFailedRecord(error);

    if (this.status == NOT_STARTED) this.status = IN_PROGRESS;
  }

  /**
   * Tracks the occurrence of an incomplete record.
   */
  @Override
  public void trackIncompleteRecord(String dataSource) {
    if (dataSource!=null && dataSource.trim().length()==0) dataSource = null;
    if (dataSource != null) {
      throw new IllegalArgumentException(
          "Record is not incomplete if it has a data source.  dataSource=[ "
          + dataSource + " ]");
    }

    // count the record as incomplete
    this.incrementIncompleteRecordCount();

    // check whether or not we have a data source
    if (dataSource == null) {
      // if no data source then increment the missing data source count
      this.incrementMissingDataSourceCount();

    } else {
      // otherwise increment the incomplete count by data source
      this.getDataSourceResult(dataSource).incrementIncompleteRecordCount();
    }

    if (this.status == NOT_STARTED) this.status = IN_PROGRESS;
  }

  /**
   * Gets the {@link SzDataSourceBulkLoadResult} for the specified data source.
   *
   * @param dataSource The data source.
   * @return The {@link SzDataSourceBulkLoadResult} for the data source.
   */
  protected SzDataSourceBulkLoadResult getDataSourceResult(String dataSource) {
    // check if data source is empty string and if so, normalize it to null
    if (dataSource != null && dataSource.trim().length() == 0) {
      dataSource = null;
    }

    // get the analysis for that data source
    SzDataSourceBulkLoadResult result
        = this.resultsByDataSource.get(dataSource);

    // check if it does not yet exist
    if (result == null) {
      // if not, create it and store it for later
      result = SzDataSourceBulkLoadResult.FACTORY.create(dataSource);
      this.resultsByDataSource.put(dataSource, result);
    }

    return result;
  }
}
