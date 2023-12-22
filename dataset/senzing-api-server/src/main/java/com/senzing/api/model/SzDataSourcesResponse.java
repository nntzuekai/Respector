package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourcesResponseImpl;
import com.senzing.util.Timers;

import javax.ws.rs.core.UriInfo;
import java.util.*;

/**
 * The response containing a set of data source codes.  Typically this is the
 * list of all configured data source codes.
 *
 */
@JsonDeserialize(using=SzDataSourcesResponse.Factory.class)
public interface SzDataSourcesResponse extends SzResponseWithRawData
{
  /**
   * Returns the {@link SzDataSourcesResponseData} for this instance.
   *
   * @return The {@link SzDataSourcesResponseData} for this instance.
   */
  SzDataSourcesResponseData getData();

  /**
   * Sets the {@link SzDataSourcesResponseData} for this instance.
   *
   * @param data The {@link SzDataSourcesResponseData} for this instance.
   */
  void setData(SzDataSourcesResponseData data);

  /**
   * Convenience method to add the specified data source to the list of
   * data sources for the underlying {@link SzDataSourcesResponseData}.
   *
   * @param dataSource The data source code to add.
   */
  void addDataSource(SzDataSource dataSource);

  /**
   * Convenience method to set the data sources on the underlying
   * {@link SzDataSourcesResponseData} to the specified {@link Collection}
   * of {@link SzDataSource} instances (removing duplicates).
   *
   * @param dataSources The {@link Collection} of data sources to set.
   */
  void setDataSources(Collection<? extends SzDataSource> dataSources);

  /**
   * A {@link ModelProvider} for instances of {@link SzDataSourcesResponse}.
   */
  interface Provider extends ModelProvider<SzDataSourcesResponse> {
    /**
     * Creates an instance of {@link SzDataSourcesResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzDataSourcesResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzDataSourcesResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzDataSourcesResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    SzDataSourcesResponse create(SzMeta                     meta,
                                 SzLinks                    links,
                                 SzDataSourcesResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSourcesResponse} that produces instances of
   * {@link SzDataSourcesResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDataSourcesResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSourcesResponse.class, SzDataSourcesResponseImpl.class);
    }

    @Override
    public SzDataSourcesResponse create(SzMeta meta, SzLinks links) {
      return new SzDataSourcesResponseImpl(meta, links);
    }

    @Override
    public SzDataSourcesResponse create(SzMeta                    meta,
                                        SzLinks                   links,
                                        SzDataSourcesResponseData data)
    {
      return new SzDataSourcesResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzDataSourcesResponse}.
   */
  class Factory extends ModelFactory<SzDataSourcesResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSourcesResponse.class);
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
     * Creates an instance of {@link SzDataSourcesResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzDataSourcesResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzDataSourcesResponse} with the specified
     * {@link SzMeta}, {@link SzLinks} and {@link SzDataSourcesResponseData}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    public SzDataSourcesResponse create(SzMeta                    meta,
                                        SzLinks                   links,
                                        SzDataSourcesResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }

  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
