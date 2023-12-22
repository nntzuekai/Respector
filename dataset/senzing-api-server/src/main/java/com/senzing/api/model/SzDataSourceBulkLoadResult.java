package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourceBulkLoadResultImpl;

/**
 * Describes an analysis of bulk data records associated with a specific
 * data source (or no data source at all).
 */
@JsonDeserialize(using=SzDataSourceBulkLoadResult.Factory.class)
public interface SzDataSourceBulkLoadResult extends SzBaseBulkLoadResult {
  /**
   * Returns the data source with which this instance was constructed.
   *
   * @return The data source with which this instance was constructed.
   */
  String getDataSource();

  /**
   * A {@link ModelProvider} for instances of {@link
   * SzDataSourceBulkLoadResult}.
   */
  interface Provider extends ModelProvider<SzDataSourceBulkLoadResult> {
    /**
     * Creates a new instance of {@link SzDataSourceBulkLoadResult}.
     * @param dataSource The data source code for the new instance.
     * @return The new instance of {@link SzDataSourceBulkLoadResult}
     */
    SzDataSourceBulkLoadResult create(String dataSource);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSourceBulkLoadResult} that produces instances of {@link
   * SzDataSourceBulkLoadResultImpl}.
   */
  class DefaultProvider
      extends AbstractModelProvider<SzDataSourceBulkLoadResult>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSourceBulkLoadResult.class,
            SzDataSourceBulkLoadResultImpl.class);
    }

    @Override
    public SzDataSourceBulkLoadResult create(String dataSource) {
      return new SzDataSourceBulkLoadResultImpl(dataSource);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzDataSourceBulkLoadResult}.
   */
  class Factory extends ModelFactory<SzDataSourceBulkLoadResult, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSourceBulkLoadResult.class);
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
     * Creates a new instance of {@link SzDataSourceBulkLoadResult}.
     * @param dataSource The data source code for the new instance.
     * @return The new instance of {@link SzDataSourceBulkLoadResult}.
     */
    public SzDataSourceBulkLoadResult create(String dataSource)
    {
      return this.getProvider().create(dataSource);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}

