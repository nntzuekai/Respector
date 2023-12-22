package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzAttributeSearchResponseImpl;

import java.util.*;

/**
 * The response containing a list of {@link SzAttributeSearchResult} instances
 * describing the search results.
 *
 */
@JsonDeserialize(using=SzAttributeSearchResponse.Factory.class)
public interface SzAttributeSearchResponse extends SzResponseWithRawData
{
  /**
   * Returns the {@link SzAttributeSearchResponseData} for this instance.
   *
   * @return The {@link SzAttributeSearchResponseData} for this instance.
   */
  SzAttributeSearchResponseData getData();

  /**
   * Sets the {@link SzAttributeSearchResponseData} for this instance.
   *
   * @param data The {@link SzAttributeSearchResponseData} for this instance.
   */
  void setData(SzAttributeSearchResponseData data);

  /**
   * Convenience method to set the {@link List} of {@link
   * SzAttributeSearchResult} instances on the contained {@link
   * SzAttributeSearchResponseData} instance to the specified list of results.
   *
   * @param results The {@link List} of {@link SzAttributeSearchResult} results.
   */
  void setSearchResults(List<SzAttributeSearchResult> results);

  /**
   * Adds the specified {@link SzAttributeSearchResult} to the list of results.
   *
   * @param result The {@link SzAttributeSearchResult} result to add.
   */
  void addSearchResult(SzAttributeSearchResult result);

  /**
   * A {@link ModelProvider} for instances of {@link SzAttributeSearchResponse}.
   */
  interface Provider extends ModelProvider<SzAttributeSearchResponse> {
    /**
     * Creates an instance of {@link SzAttributeSearchResponse} with the
     * specified {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    SzAttributeSearchResponse create(SzMeta meta, SzLinks links);

    /**
     * Creates an instance of {@link SzAttributeSearchResponse} with the
     * specified {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzAttributeSearchResponseData} for the response.
     */
    SzAttributeSearchResponse create(SzMeta                         meta,
                                     SzLinks                        links,
                                     SzAttributeSearchResponseData  data);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzAttributeSearchResponse} that produces instances of
   * {@link SzAttributeSearchResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzAttributeSearchResponse>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzAttributeSearchResponse.class,
            SzAttributeSearchResponseImpl.class);
    }

    @Override
    public SzAttributeSearchResponse create(SzMeta meta, SzLinks links) {
      return new SzAttributeSearchResponseImpl(meta, links);
    }

    @Override
    public SzAttributeSearchResponse create(SzMeta                        meta,
                                            SzLinks                       links,
                                            SzAttributeSearchResponseData data)
    {
      return new SzAttributeSearchResponseImpl(meta, links, data);
    }

  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzAttributeSearchResponse}.
   */
  class Factory extends ModelFactory<SzAttributeSearchResponse, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzAttributeSearchResponse.class);
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
     * Creates an instance of {@link SzAttributeSearchResponse} with the
     * specified {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     */
    public SzAttributeSearchResponse create(SzMeta meta, SzLinks links) {
      return this.getProvider().create(meta, links);
    }

    /**
     * Creates an instance of {@link SzAttributeSearchResponse} with the
     * specified {@link SzMeta} and {@link SzLinks}.
     *
     * @param meta The response meta data.
     *
     * @param links The links for the response.
     *
     * @param data The {@link SzAttributeSearchResponseData} for the response.
     */
    public SzAttributeSearchResponse create(SzMeta                        meta,
                                            SzLinks                       links,
                                            SzAttributeSearchResponseData data)
    {
      return this.getProvider().create(meta, links, data);
    }

  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
