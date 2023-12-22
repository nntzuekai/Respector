package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourceResponseDataImpl;
import com.senzing.api.model.impl.SzDataSourceResponseImpl;

/**
 * Represents the data for {@link SzDataSourceResponse}.
 *
 */
@JsonDeserialize(using= SzDataSourceResponseData.Factory.class)
public interface SzDataSourceResponseData {
  /**
   * Gets the {@link SzDataSource} describing the data source.
   *
   * @return The {@link SzDataSource} describing the data source.
   */
  SzDataSource getDataSource();

  /**
   * Sets the {@link SzDataSource} describing the data source.
   *
   * @param dataSource The {@link SzDataSource} describing the data source.
   */
  void setDataSource(SzDataSource dataSource);

  /**
   * A {@link ModelProvider} for instances of {@link SzDataSourceResponseData}.
   */
  interface Provider extends ModelProvider<SzDataSourceResponseData> {
    /**
     * Creates an instance with no data source.
     *
     * @return The {@link SzDataSourceResponseData} instance that was created.
     */
    SzDataSourceResponseData create();

    /**
     * Creates an instance with the specified {@link SzDataSource}.
     *
     * @param dataSource The {@link SzDataSource} for the new instance.
     *
     * @return The {@link SzDataSourceResponseData} instance that was created.
     */
    SzDataSourceResponseData create(SzDataSource dataSource);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSourceResponseData} that produces instances of
   * {@link SzDataSourceResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDataSourceResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSourceResponseData.class, SzDataSourceResponseDataImpl.class);
    }

    @Override
    public SzDataSourceResponseData create() {
      return new SzDataSourceResponseDataImpl();
    }

    @Override
    public SzDataSourceResponseData create(SzDataSource dataSource) {
      return new SzDataSourceResponseDataImpl(dataSource);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzDataSourceResponseData}.
   */
  class Factory extends ModelFactory<SzDataSourceResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSourceResponseData.class);
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
     * Creates an instance with no data source.
     *
     * @return The {@link SzDataSourceResponseData} instance that was created.
     */
    public SzDataSourceResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance with the specified {@link SzDataSource}.
     *
     * @param dataSource The {@link SzDataSource} for the new instance.
     *
     * @return The {@link SzDataSourceResponseData} instance that was created.
     */
    public SzDataSourceResponseData create(SzDataSource dataSource) {
      return this.getProvider().create(dataSource);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
