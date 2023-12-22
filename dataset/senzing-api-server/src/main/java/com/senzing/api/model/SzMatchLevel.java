package com.senzing.api.model;

/**
 * Enumerates the various match levels describing how two records resolve
 * against each other.
 */
public enum SzMatchLevel {
  /**
   * No match was found between the records.
   */
  NO_MATCH,

  /**
   * The records resolved to the same entity.
   */
  RESOLVED,

  /**
   * The records were not close enough to resolve but may represent the same
   * entity if more data was provided.
   */
  POSSIBLY_SAME,

  /**
   * The records share some attributes that suggest a relationship.
   */
  POSSIBLY_RELATED,

  /**
   * The records match in name only.
   */
  NAME_ONLY,

  /**
   * An explicit relationship has been disclosed between the records.
   */
  DISCLOSED;
}
