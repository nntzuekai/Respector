package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourceResponseImpl;
import com.senzing.util.Timers;

import javax.ws.rs.core.UriInfo;
import java.util.*;

/**
 * The response containing a set of data source codes.  Typically this is the
 * list of all configured data source codes.
 *
 */
@JsonDeserialize(using=SzDataSourceResponse.Factory.class)
public interface SzDataSourceResponse extends SzResponseWithRawData
{
  /**
   * Returns the {@link SzDataSourceResponseData} for this instance.
   *
   * @return The {@link SzDataSourceResponseData} for this instance.
   */
  SzDataSourceResponseData getData();

  /**
   * Sets the {@link SzDataSourceResponseData} for this instance.
   *
   * @param data The {@link SzDataSourceResponseData} for this instance.
   */
  void setData(SzDataSourceResponseData data);

  /**
   * Convenience method to set the specified data source providing the
   * specified data source to the underlying {@link SzDataSourceResponseData}.
   *
   * @param dataSource The data source code to add.
   */
  void setDataSource(SzDataSource dataSource);

  /**
   * A {@link ModelProvider} for instances of {@link SzDataSourceResponse}.
   */
  interface Provider extends ModelProvider<SzDataSourceResponse> {
    /**
     * Creates an instance of {@link SzDataSourceResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzDataSourceResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzDataSourceResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    SzDataSourceResponse create(SzMeta                    meta,
                                SzLinks                   links,
                                SzDataSourceResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSourceResponse} that produces instances of
   * {@link SzDataSourceResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDataSourceResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSourceResponse.class, SzDataSourceResponseImpl.class);
    }

    @Override
    public SzDataSourceResponse create(SzMeta meta, SzLinks links) {
      return new SzDataSourceResponseImpl(meta, links);
    }

    @Override
    public SzDataSourceResponse create(SzMeta                   meta,
                                       SzLinks                  links,
                                       SzDataSourceResponseData data)
    {
      return new SzDataSourceResponseImpl(meta, links, data);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzDataSourceResponse}.
   */
  class Factory extends ModelFactory<SzDataSourceResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSourceResponse.class);
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
     * Creates an instance of {@link SzDataSourceResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzDataSourceResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzDataSourceResponse} with the specified
     * {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The data for the response.
     */
    public SzDataSourceResponse create(SzMeta                   meta,
                                       SzLinks                  links,
                                       SzDataSourceResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }

  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
