package com.senzing.api.model;

/**
 * Enumerates the various methods for retrieving features for entities.
 *
 */
public enum SzRelationshipMode {
  /**
   * Do not include any data on first-degree related entities -- this is the
   * fastest option from a performance perspective because related entities do
   * not have to be retrieved.
   */
  NONE,

  /**
   * Include only partial stub information for related entities with the
   * `partial` property of the `SzRelatedEntity` instances set to `true`.
   * Obtaining additional information requires subsequent API calls.
   */
  PARTIAL,

  /**
   * Include full data on the first-degree related entities.  This option
   * obtains entity network at one degree for the requested entity and will
   * fully populate up to 1000 related entities and sets the `partial` property
   * of those `SzRelatedEntity` instances to `false`.  Related entities beyond
   * the first 1000 will be left incomplete and have their `partial` property
   * will be set to `true`.
   */
  FULL;
}
