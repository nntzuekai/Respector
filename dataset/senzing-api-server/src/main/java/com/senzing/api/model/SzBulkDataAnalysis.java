package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzBulkDataAnalysisImpl;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.senzing.api.model.SzBulkDataStatus.IN_PROGRESS;
import static com.senzing.api.model.SzBulkDataStatus.NOT_STARTED;

/**
 * Describes an analysis of bulk data records that are being prepared for
 * loading.
 */
@JsonDeserialize(using=SzBulkDataAnalysis.Factory.class)
public interface SzBulkDataAnalysis {
  /**
   * Gets the {@linkplain SzBulkDataStatus status} of the bulk data analysis.
   *
   * @return The status of the bulk data analysis.
   */
  SzBulkDataStatus getStatus();

  /**
   * Sets the {@linkplain SzBulkDataStatus status} of the bulk data analysis.
   *
   * @param status The status of the bulk data analysis.
   */
  void setStatus(SzBulkDataStatus status);

  /**
   * Gets the character encoding with which the records were processed.
   *
   * @return The character encoding with which the records were processed.
   */
  String getCharacterEncoding();

  /**
   * Sets the character encoding with which the bulk data was processed.
   *
   * @param encoding The character encoding used to process the bulk data.
   */
  void setCharacterEncoding(String encoding);

  /**
   * Gets the media type of the bulk record data.
   *
   * @return The media type of the bulk record data.
   */
  @JsonInclude(NON_NULL)
  String getMediaType();

  /**
   * Sets the media type of the bulk record data.
   *
   * @param mediaType The media type of the bulk record data.
   */
  void setMediaType(String mediaType);

  /**
   * Return the number of records in the bulk data set.
   *
   * @return The number of records in the bulk data set.
   */
  int getRecordCount();

  /**
   * Sets the number of records in the bulk data set.
   *
   * @param recordCount The number of records in the bulk data set.
   */
  void setRecordCount(int recordCount);

  /**
   * Increments the number of records in the bulk data set and returns
   * the new record count.
   *
   * @return The incremented record count.
   */
  int incrementRecordCount();

  /**
   * Increments the number of records in the bulk data set and returns
   * the new record count.
   *
   * @param increment The number of records to increment by.
   *
   * @return The incremented record count.
   */
  int incrementRecordCount(int increment);

  /**
   * Gets the number of records in the bulk data set that have a
   * <tt>"RECORD_ID"</tt> property.
   *
   * @return The number of records in the bulk data set that have a
   *         <tt>"RECORD_ID"</tt> property.
   */
  int getRecordsWithRecordIdCount();

  /**
   * Sets the number of records in the bulk data set that have a
   * <tt>"RECORD_ID"</tt> property.
   *
   * @param recordIdCount The number of records in the bulk data set that have
   *                      a <tt>"RECORD_ID"</tt> property.
   */
  void setRecordsWithRecordIdCount(int recordIdCount);

  /**
   * Increments the number of records in the bulk data set that have a
   * <tt>"RECORD_ID"</tt> property and returns the new count.
   *
   * @return The newly incremented count of records in the bulk data set that
   *         have a <tt>"RECORD_ID"</tt> property.
   */
  int incrementRecordsWithRecordIdCount();

  /**
   * Increments the number of records in the bulk data set that have a
   * <tt>"RECORD_ID"</tt> property and returns the new count.
   *
   * @param increment The number of records to increment by.
   *
   * @return The newly incremented count of records in the bulk data set that
   *         have a <tt>"RECORD_ID"</tt> property.
   */
  int incrementRecordsWithRecordIdCount(int increment);

  /**
   * Gets the number of records in the bulk data set that have a
   * <tt>"DATA_SOURCE"</tt> property.
   *
   * @return The number of records in the bulk data set that have a
   *         <tt>"DATA_SOURCE"</tt> property.
   */
  int getRecordsWithDataSourceCount();

  /**
   * Sets the number of records in the bulk data set that have a
   * <tt>"DATA_SOURCE"</tt> property.
   *
   * @param dataSourceCount The number of records in the bulk data set that
   *                        have a <tt>"DATA_SOURCE"</tt> property.
   */
  void setRecordsWithDataSourceCount(int dataSourceCount);

  /**
   * Increments the number of records in the bulk data set that have a
   * <tt>"DATA_SOURCE"</tt> property and returns the new count.
   *
   * @return The newly incremented count of records in the bulk data set that
   *         have a <tt>"DATA_SOURCE"</tt> property.
   */
  int incrementRecordsWithDataSourceCount();

  /**
   * Increments the number of records in the bulk data set that have a
   * <tt>"DATA_SOURCE"</tt> property and returns the new count.
   *
   * @param increment The number of records to increment by.
   * @return The newly incremented count of records in the bulk data set that
   *         have a <tt>"DATA_SOURCE"</tt> property.
   */
  int incrementRecordsWithDataSourceCount(int increment);

  /**
   * Gets the list of {@link SzDataSourceRecordAnalysis} instances for the
   * bulk data describing the statistics by data source (including those with
   * no data source).
   *
   * @return A {@link List} of {@link SzDataSourceRecordAnalysis} instances
   *         describing the statistics for the bulk data.
   */
  @JsonInclude(NON_EMPTY)
  List<SzDataSourceRecordAnalysis> getAnalysisByDataSource();

  /**
   * Set the analysis by data source for this instance.  This will reset the
   * top-level counts according to what is discovered in the specified
   * collection of {@link SzDataSourceRecordAnalysis} instances.
   *
   * @param analysisList The {@link Collection} of
   *                     {@link SzDataSourceRecordAnalysis} instances.
   */
  void setAnalysisByDataSource(
      Collection<SzDataSourceRecordAnalysis> analysisList);

  /**
   * Utility method for tracking a record that has been analyzed with the
   * specified data source and record ID (any of which may be <tt>null</tt> to
   * indicate if they are absent in the record).
   *
   * @param dataSource The data source for the record, or <tt>null</tt> if it
   *                   does not have a <tt>"DATA_SOURCE"</tt> property.
   * @param recordId The record ID for the record, or <tt>null</tt> if it does
   *                 not have a <tt>"RECORD_ID"</tt> property.
   */
  void trackRecord(String dataSource, String recordId);

  /**
   * Utility method for tracking a record that has been analyzed with the
   * specified data source and record ID (any of which may be <tt>null</tt>
   * to indicate if they are absent in the record).
   *
   * @param recordCount The number of records being tracked.
   * @param dataSource The data source for the record, or <tt>null</tt> if it
   *                   does not have a <tt>"DATA_SOURCE"</tt> property.
   * @param withRecordId <tt>true</tt> if the records being tracked have record
   *                     ID's, and <tt>false</tt> if they do not.
   */
  void trackRecords(int      recordCount,
                    String   dataSource,
                    boolean  withRecordId);

  /**
   * A {@link ModelProvider} for instances of {@link SzBulkDataAnalysis}.
   */
  interface Provider extends ModelProvider<SzBulkDataAnalysis> {
    /**
     * Creates a new instance of {@link SzBulkDataAnalysis}.
     *
     * @return The new instance of {@link SzBulkDataAnalysis}
     */
    SzBulkDataAnalysis create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzBulkDataAnalysis} that produces instances of
   * {@link SzBulkDataAnalysisImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzBulkDataAnalysis>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzBulkDataAnalysis.class, SzBulkDataAnalysisImpl.class);
    }

    @Override
    public SzBulkDataAnalysis create() {
      return new SzBulkDataAnalysisImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzBulkDataAnalysis}.
   */
  class Factory extends ModelFactory<SzBulkDataAnalysis, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzBulkDataAnalysis.class);
    }

    /**
     * Constructs with the default provider.  This constructor is private and
     * is used for the master singleton instance.
     * @param defaultProvider The default provider.
     */
    private Factory(Provider defaultProvider) {
      super(defaultProvider);
    }

    /**
     * Creates a new instance of {@link SzBulkDataAnalysis}.
     * @return The new instance of {@link SzBulkDataAnalysis}.
     */
    public SzBulkDataAnalysis create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
