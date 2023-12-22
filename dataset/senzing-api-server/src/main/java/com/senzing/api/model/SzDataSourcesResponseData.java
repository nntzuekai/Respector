package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourcesResponseDataImpl;
import com.senzing.api.model.impl.SzDataSourcesResponseImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Describes the data segment for {@link SzDataSourceResponse}.
 */
@JsonDeserialize(using= SzDataSourcesResponseData.Factory.class)
public interface SzDataSourcesResponseData {
  /**
   * Gets the unmodifiable {@link Set} of data source codes.
   *
   * @return The unmodifiable {@link Set} of data source codes.
   */
  Set<String> getDataSources();

  /**
   * Gets the unmodifiable {@link Map} of {@link String} data source codes
   * to {@link SzDataSource} values describing the configured data sources.
   *
   * @return The unmodifiable {@link Map} of {@link String} data source codes
   *         to {@link SzDataSource} values describing the configured data
   *         sources.
   */
  Map<String, SzDataSource> getDataSourceDetails();

  /**
   * Adds the specified {@link SzDataSource} to the data sources for this
   * instance.
   *
   * @param dataSource The {@link SzDataSource} to add to the data sources
   *                   for this instance.
   */
  void addDataSource(SzDataSource dataSource);

  /**
   * Sets the data sources for this instance to those in the specified of
   * {@link Collection} of {@link SzDataSource} instances.
   *
   * @param dataSources The {@link Collection} of data source codes.
   */
  void setDataSources(Collection<? extends SzDataSource> dataSources);

  /**
   * A {@link ModelProvider} for instances of {@link SzDataSourcesResponseData}.
   */
  interface Provider extends ModelProvider<SzDataSourcesResponseData> {
    /**
     * Creates an instance of {@link SzDataSourcesResponseData} with no
     * data sources.
     *
     * @return The {@link SzDataSourcesResponseData} instance that was
     *         created.
     */
    SzDataSourcesResponseData create();

    /**
     * Creates an instance of {@link SzDataSourcesResponseData} with the
     * data sources described by the {@link SzDataSource} instances in the
     * specified {@link Collection}.
     *
     * @param dataSources The {@link Collection} of {@link SzDataSource}
     *                    instances describing the data sources.
     *
     * @return The {@link SzDataSourcesResponseData} instance that was
     *         created.
     */
    SzDataSourcesResponseData create(
        Collection<? extends SzDataSource> dataSources);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSourcesResponseData} that produces instances of
   * {@link SzDataSourcesResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDataSourcesResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSourcesResponseData.class,
            SzDataSourcesResponseDataImpl.class);
    }

    @Override
    public SzDataSourcesResponseData create() {
      return new SzDataSourcesResponseDataImpl();
    }

    @Override
    public SzDataSourcesResponseData create(
        Collection<? extends SzDataSource> dataSources)
    {
      return new SzDataSourcesResponseDataImpl(dataSources);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzDataSourcesResponseData}.
   */
  class Factory extends ModelFactory<SzDataSourcesResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSourcesResponseData.class);
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
     * Creates an instance of {@link SzDataSourcesResponseData} with no
     * data sources.
     *
     * @return The {@link SzDataSourcesResponseData} instance that was
     *         created.
     */
    public SzDataSourcesResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance of {@link SzDataSourcesResponseData} with the
     * data sources described by the {@link SzDataSource} instances in the
     * specified {@link Collection}.
     *
     * @param dataSources The {@link Collection} of {@link SzDataSource}
     *                    instances describing the data sources.
     *
     * @return The {@link SzDataSourcesResponseData} instance that was
     *         created.
     */
    public SzDataSourcesResponseData create(
        Collection<? extends SzDataSource> dataSources)
    {
      return this.getProvider().create(dataSources);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
