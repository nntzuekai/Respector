package com.senzing.api.model;

/**
 * The enumeration of scoring bucker values.
 */
public enum SzScoringBucket {
  /**
   * The respective features were not scored.
   */
  NOT_SCORED,

  /**
   * The two feature values are considered to be the same.
   */
  SAME,

  /**
   * The two feature values are considered to be close.
   */
  CLOSE,

  /**
   * The two feature values are similar, but not enough to be considered
   * {@link #CLOSE}.
   */
  LIKELY,

  /**
   * It's possible that the two feature values are the same but almost just as
   * likely that they are not.
   */
  PLAUSIBLE,

  /**
   * It's unlikely that the two feature values represent the same value.
   */
  UNLIKELY,

  /**
   * The two feature values obviously represent different values.
   */
  NO_CHANCE;
}
