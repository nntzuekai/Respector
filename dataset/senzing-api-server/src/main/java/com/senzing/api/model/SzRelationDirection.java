package com.senzing.api.model;

/**
 * Enumerates the direction of a relationship between two entities (typically
 * a disclosed relationships since discovered relationships are typically
 * bidirectional).
 */
public enum SzRelationDirection {
  /**
   * The relationship is from the first entity to the second entity.
   */
  OUTBOUND,

  /**
   * The relationship is from the second entity to the first entity.
   */
  INBOUND,

  /**
   * The relationship is from the first entity to the second entity and
   * from the second entity to the first entity (i.e.: it goes in both
   * directions).
   */
  BIDIRECTIONAL;

  /**
   * Merges this direction with the specified direction.
   * @param dir The {@link SzRelationDirection} to merge with.
   * @return The merged {@link SzRelationDirection}.
   */
  public SzRelationDirection and(SzRelationDirection dir) {
    if (dir == null) return this;
    switch (this) {
      case OUTBOUND:
        if (dir != OUTBOUND) return BIDIRECTIONAL;
        return this;

      case INBOUND:
        if (dir != INBOUND) return BIDIRECTIONAL;
        return this;

      case BIDIRECTIONAL:
        return this;

      default:
        throw new IllegalStateException(
            "Unhandled SzRelationshipDirection: " + this);
    }
  }
}
