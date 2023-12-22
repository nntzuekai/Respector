package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.*;

import java.util.*;

/**
 * Provides a default implementation of {@link SzAttributeSearchResponseData}.
 */
@JsonDeserialize
public class SzAttributeSearchResponseDataImpl
  implements SzAttributeSearchResponseData
{
  /**
   * The list of {@link SzAttributeSearchResult} instances describing the
   * results.
   */
  private List<SzAttributeSearchResult> searchResults;

  /**
   * Default constructor.
   */
  public SzAttributeSearchResponseDataImpl() {
    this.searchResults = new LinkedList<>();
  }

  /**
   * Constructs with the specified {@link Collection} of
   * {@link SzAttributeSearchResult} instances.
   *
   * @param results The {@link Collection} of {@link SzAttributeSearchResult}
   *                instances to initialize the new instance with.
   */
  public SzAttributeSearchResponseDataImpl(
      Collection<? extends SzAttributeSearchResult> results)
  {
    this.searchResults = new ArrayList<>(results);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzAttributeSearchResult> getSearchResults() {
    List<SzAttributeSearchResult> list = this.searchResults;
    return Collections.unmodifiableList(list);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSearchResults(
      Collection<? extends SzAttributeSearchResult> results)
  {
    this.searchResults.clear();
    if (results != null) {
      this.searchResults.addAll(results);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addSearchResult(SzAttributeSearchResult result) {
    Objects.requireNonNull(
        result, "The specified search result cannot be null");
    this.searchResults.add(result);
  }
}
