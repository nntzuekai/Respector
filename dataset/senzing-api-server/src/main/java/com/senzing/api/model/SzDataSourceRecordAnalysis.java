package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourceRecordAnalysisImpl;

/**
 * Describes an analysis of bulk data records associated with a specific
 * data source (or no data source at all).
 */
@JsonDeserialize(using=SzDataSourceRecordAnalysis.Factory.class)
public interface SzDataSourceRecordAnalysis {
  /**
   * Returns the data source with which this instance was constructed.
   *
   * @return The data source with which this instance was constructed.
   */
  String getDataSource();

  /**
   * Gets the number of records that have the associated data source.
   *
   * @return The number of records that have the associated data source.
   */
  int getRecordCount();

  /**
   * Sets the number of records that have the associated data source.
   *
   * @param recordCount The number of records that have the associated
   *                    data source.
   */
  void setRecordCount(int recordCount);

  /**
   * Increments the number of records that have the associated data source
   * and returns the new count.
   *
   * @return The new count after incrementing.
   */
  long incrementRecordCount();

  /**
   * Increments the number of records that have the associated data source
   * and returns the new count.
   *
   * @param increment The number of records to increment by.
   *
   * @return The new count after incrementing.
   */
  long incrementRecordCount(int increment);

  /**
   * Gets the number of records that have the associated data source and also
   * have a <tt>"RECORD_ID"</tt>.
   *
   * @return The number of records that have the associated data source and
   *         also have a <tt>"RECORD_ID"</tt>.
   */
  int getRecordsWithRecordIdCount();

  /**
   * Sets the number of records that have the associated data source and also
   * have a <tt>"RECORD_ID"</tt>.
   *
   * @param recordIdCount The number of records that have the associated
   *                      data source and also have a <tt>"RECORD_ID"</tt>.
   */
  void setRecordsWithRecordIdCount(int recordIdCount);

  /**
   * Increments the number of records that have the associated data source
   * and also have a <tt>"RECORD_ID"</tt> and returns the new count.
   *
   * @return The new count after incrementing.
   */
  int incrementRecordsWithRecordIdCount();

  /**
   * Increments the number of records that have the associated data source
   * and also have a <tt>"RECORD_ID"</tt> and returns the new count.
   *
   * @param increment The number of records to increment by.
   *
   * @return The new count after incrementing.
   */
  int incrementRecordsWithRecordIdCount(int increment);

    /**
   * A {@link ModelProvider} for instances of {@link
   * SzDataSourceRecordAnalysis}.
   */
  interface Provider extends ModelProvider<SzDataSourceRecordAnalysis> {
    /**
     * Creates a new instance of {@link SzDataSourceRecordAnalysis}.
     * @param dataSource The data source code for the new instance.
     * @return The new instance of {@link SzDataSourceRecordAnalysis}
     */
    SzDataSourceRecordAnalysis create(String dataSource);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSourceRecordAnalysis} that produces instances of {@link
   * SzDataSourceRecordAnalysisImpl}.
   */
  class DefaultProvider
      extends AbstractModelProvider<SzDataSourceRecordAnalysis>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSourceRecordAnalysis.class,
            SzDataSourceRecordAnalysisImpl.class);
    }

    @Override
    public SzDataSourceRecordAnalysis create(String dataSource) {
      return new SzDataSourceRecordAnalysisImpl(dataSource);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzDataSourceRecordAnalysis}.
   */
  class Factory extends ModelFactory<SzDataSourceRecordAnalysis, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSourceRecordAnalysis.class);
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
     * Creates a new instance of {@link SzDataSourceRecordAnalysis}.
     * @param dataSource The data source code for the new instance.
     * @return The new instance of {@link SzDataSourceRecordAnalysis}.
     */
    public SzDataSourceRecordAnalysis create(String dataSource)
    {
      return this.getProvider().create(dataSource);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}

