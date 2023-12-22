package com.senzing.api.model;

/**
 * Enumerates the various search result types.
 *
 */
public enum SzAttributeSearchResultType {
  /**
   * Indicates the search criteria describes an entity that would have
   * matched against the search result.
   */
  MATCH,

  /**
   * Indicates the search criteria describes an entity that would have
   * possibly matched against the search result.
   */
  POSSIBLE_MATCH,

  /**
   * Indicates the search criteria describes an entity that would have
   * possibly related to the search result.
   */
  POSSIBLE_RELATION,

  /**
   * Indicates the search criteria describes an entity that matches the search
   * result in name only.
   */
  NAME_ONLY_MATCH;
}
