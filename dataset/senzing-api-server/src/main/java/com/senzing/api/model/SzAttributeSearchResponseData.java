package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzAttributeSearchResponseDataImpl;
import com.senzing.api.model.impl.SzAttributeSearchResponseImpl;

import java.util.Collection;
import java.util.List;

/**
 * The response containing a list of {@link SzAttributeSearchResult} instances
 * describing the search results.
 *
 */
@JsonDeserialize(using= SzAttributeSearchResponseData.Factory.class)
public interface SzAttributeSearchResponseData {
  /**
   * Gets the {@link List} of {@linkplain SzAttributeSearchResult search
   * results}.
   *
   * @return {@link List} of {@linkplain SzAttributeSearchResult search
   *          results}
   */
  List<SzAttributeSearchResult> getSearchResults();

  /**
   * Adds the specified {@link SzAttributeSearchResult} to the list of results.
   *
   * @param result The {@link SzAttributeSearchResult} result to add.
   */
  void addSearchResult(SzAttributeSearchResult result);

  /**
   * Sets the {@link List} of {@link SzAttributeSearchResult} instances to the
   * specified list of results.
   *
   * @param results The {@link List} of {@link SzAttributeSearchResult} results.
   */
  void setSearchResults(Collection<? extends SzAttributeSearchResult> results);

  /**
   * A {@link ModelProvider} for instances of {@link SzAttributeSearchResponseData}.
   */
  interface Provider extends ModelProvider<SzAttributeSearchResponseData> {
    /**
     * Creates an instance of {@link SzAttributeSearchResponseData} with the
     * no attribute search results.
     *
     * @return The {@link SzAttributeSearchResponseData} that was created.
     */
    SzAttributeSearchResponseData create();

    /**
     * Creates an instance of {@link SzAttributeSearchResponseData} with the
     * specified {@link Collection} of {@link SzAttributeSearchResult}
     * instances.
     *
     * @param results The {@link Collection} of {@link SzAttributeSearchResult}
     *                instances to initialize the new instance with.
     *
     * @return The {@link SzAttributeSearchResponseData} that was created.
     */
    SzAttributeSearchResponseData create(
        Collection<? extends SzAttributeSearchResult> results);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzAttributeSearchResponseData} that produces instances of
   * {@link SzAttributeSearchResponseImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzAttributeSearchResponseData>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzAttributeSearchResponseData.class,
            SzAttributeSearchResponseDataImpl.class);
    }

    @Override
    public SzAttributeSearchResponseData create() {
      return new SzAttributeSearchResponseDataImpl();
    }

    @Override
    public SzAttributeSearchResponseData create(
        Collection<? extends SzAttributeSearchResult> results)
    {
      return new SzAttributeSearchResponseDataImpl(results);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzAttributeSearchResponseData}.
   */
  class Factory extends ModelFactory<SzAttributeSearchResponseData, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzAttributeSearchResponseData.class);
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
     * Creates an instance of {@link SzAttributeSearchResponseData} with the
     * no attribute search results.
     *
     * @return The {@link SzAttributeSearchResponseData} that was created.
     */
    public SzAttributeSearchResponseData create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance of {@link SzAttributeSearchResponseData} with the
     * specified {@link Collection} of {@link SzAttributeSearchResult}
     * instances.
     *
     * @param results The {@link Collection} of {@link SzAttributeSearchResult}
     *                instances to initialize the new instance with.
     *
     * @return The {@link SzAttributeSearchResponseData} that was created.
     */
    public SzAttributeSearchResponseData create(
        Collection<? extends SzAttributeSearchResult> results)
    {
      return this.getProvider().create(results);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
