package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzBulkLoadResultImpl;
import com.senzing.g2.engine.G2Fallible;
import java.util.*;

import static com.senzing.api.model.SzBulkDataStatus.*;

/**
 * Describes an analysis of bulk data records that are being prepared for
 * loading.
 */
@JsonDeserialize(using=SzBulkLoadResult.Factory.class)
public interface SzBulkLoadResult extends SzBaseBulkLoadResult {
  /**
   * Gets the {@linkplain SzBulkDataStatus status} of the bulk load.
   *
   * @return The status of the bulk load.
   */
  SzBulkDataStatus getStatus();

  /**
   * Sets the {@linkplain SzBulkDataStatus status} of the bulk load.
   *
   * @param status The status of the bulk load.
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
  String getMediaType();

  /**
   * Sets the media type of the bulk record data.
   *
   * @param mediaType The media type of the bulk record data.
   */
  void setMediaType(String mediaType);

  /**
   * Return the number of records that are incomplete because they are missing
   * the <tt>"DATA_SOURCE"</tt> field.
   *
   * @return The number of records that are incomplete because they are missing
   *         a <tt>"DATA_SOURCE"</tt> field.
   */
  int getMissingDataSourceCount();

  /**
   * Gets the list of {@link SzDataSourceBulkLoadResult} instances for the
   * bulk data load describing the statistics by data source.
   *
   * @return A {@link List} of {@link SzDataSourceBulkLoadResult} instances
   * describing the statistics for the bulk data load.
   */
  List<SzDataSourceBulkLoadResult> getResultsByDataSource();

  /**
   * Utility method for tracking the successful loading of a record with the
   * specified non-null data source.
   *
   * @param dataSource The non-null data source for the record.
   * @throws NullPointerException If the specified parameter is <tt>null</tt>.
   */
  void trackLoadedRecord(String dataSource);

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
  void trackFailedRecord(String dataSource, String errorCode, String errorMsg);

  /**
   * Utility method for tracking a failed attempt to load a record with the
   * specified non-null data source.  The failure is recorded with the specified
   * error code and error message.
   *
   * @param dataSource The data source for the record, or <tt>null</tt> if it
   *                   does not have a <tt>"DATA_SOURCE"</tt> property.
   * @param g2Fallible The {@link G2Fallible} instance that had the failure.
   */
  void trackFailedRecord(String      dataSource,
                         G2Fallible  g2Fallible);

  /**
   * Utility method for tracking a failed attempt to load a record with the
   * specified non-null data source.  The failure is recorded with the specified
   * error code and error message.
   *
   * @param dataSource The data source for the record, or <tt>null</tt> if it
   *                   does not have a <tt>"DATA_SOURCE"</tt> property.
   * @param error      The {@link SzError} describing the error that occurred.
   */
  void trackFailedRecord(String dataSource, SzError error);

  /**
   * Tracks the occurrence of an incomplete record.
   */
  void trackIncompleteRecord(String dataSource);

    /**
   * A {@link ModelProvider} for instances of {@link SzBulkLoadResult}.
   */
  interface Provider extends ModelProvider<SzBulkLoadResult> {
    /**
     * Creates a new instance of {@link SzBulkLoadResult}.
     *
     * @return The new instance of {@link SzBulkLoadResult}
     */
    SzBulkLoadResult create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzBulkLoadResult} that produces instances of {@link SzBulkLoadResult}.
   */
  class DefaultProvider extends AbstractModelProvider<SzBulkLoadResult>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzBulkLoadResult.class, SzBulkLoadResultImpl.class);
    }

    @Override
    public SzBulkLoadResult create() {
      return new SzBulkLoadResultImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzBulkLoadResult}.
   */
  class Factory extends ModelFactory<SzBulkLoadResult, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzBulkLoadResult.class);
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
     * Creates a new instance of {@link SzBulkLoadResult}.
     * @return The new instance of {@link SzBulkLoadResult}.
     */
    public SzBulkLoadResult create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

}
