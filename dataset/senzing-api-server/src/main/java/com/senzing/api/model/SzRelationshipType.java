package com.senzing.api.model;

/**
 * Enumerates the various relationship types.
 *
 */
public enum SzRelationshipType {
  /**
   * The related entity is a possible match.
   */
  POSSIBLE_MATCH,

  /**
   * The related entity is a possible relation.
   */
  POSSIBLE_RELATION,

  /**
   * The related entity is a disclosed relation.
   */
  DISCLOSED_RELATION;
}
