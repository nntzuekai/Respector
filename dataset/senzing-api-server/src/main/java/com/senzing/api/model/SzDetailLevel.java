package com.senzing.api.model;

import static com.senzing.g2.engine.G2Engine.*;

/**
 * Enumerates the levels of detail desired for entity data when obtained via
 * the various endpoints that return entity data.  Details for features of
 * entities as well as the related entities of entities are controlled by
 * other flags.
 */
public enum SzDetailLevel {
  /**
   * The entities returned will include at most their entity ID's as well as
   * identifiers for their constituent records (i.e.: data source code and
   * record ID for each record).  This detail level is optimized for the fastest
   * possible processing time.
   */
  MINIMAL(G2_ENTITY_INCLUDE_RECORD_DATA
              | G2_SEARCH_INCLUDE_FEATURE_SCORES,
          G2_ENTITY_INCLUDE_ALL_RELATIONS
              | G2_ENTITY_INCLUDE_RELATED_RECORD_DATA),

  /**
   * Builds upon {@link #MINIMAL} to add the entity name and related entity
   * match info when related entity match info when related entities are
   * included.  This detail level aims to maintain as much speed as possible
   * while providing names and relationship information for rendering a graph.
   */
  BRIEF(MINIMAL.getEntityFlags() | G2_ENTITY_INCLUDE_ENTITY_NAME,
        MINIMAL.getRelatedFlags()
            | G2_ENTITY_INCLUDE_RELATED_ENTITY_NAME
            | G2_ENTITY_INCLUDE_RELATED_MATCHING_INFO),

  /**
   * Identical to {@link #BRIEF} except that individual record identifier
   * information is excluded, leaving only the record summary (i.e.: a record
   * count by data source code).  This reduces the size of the JSON document for
   * large entities with thousands of records.  It may take longer to process
   * than `BRIEF` but less data is returned as well, speeding up network
   * transfer times.
   */
  SUMMARY(G2_ENTITY_INCLUDE_ENTITY_NAME
              | G2_ENTITY_INCLUDE_RECORD_SUMMARY
              | G2_SEARCH_INCLUDE_FEATURE_SCORES,
          G2_ENTITY_INCLUDE_ALL_RELATIONS
              | G2_ENTITY_INCLUDE_RELATED_ENTITY_NAME
              | G2_ENTITY_INCLUDE_RELATED_RECORD_SUMMARY
              | G2_ENTITY_INCLUDE_RELATED_MATCHING_INFO),

  /**
   * <p>
   * Combines {@link #BRIEF} and {@link #SUMMARY} and then adds the original
   * JSON data for each record, the record-level matching info, as well as
   * formatted record data.
   * <p>
   * </p><b>NOTE:</b> the record-level matching info returned via "how" and
   * "why" is often more useful than that embedded in the entity.  Further, the
   * formatted record data, while readable, is not formatted according to locale
   * (i.e.: address, name and date formatting may not appear as expected to a
   * user).
   */
  VERBOSE(BRIEF.getEntityFlags() | SUMMARY.getEntityFlags()
          | G2_ENTITY_INCLUDE_RECORD_FORMATTED_DATA
          | G2_ENTITY_INCLUDE_RECORD_JSON_DATA
          | G2_ENTITY_INCLUDE_RECORD_MATCHING_INFO,
          BRIEF.getRelatedFlags() | SUMMARY.getRelatedFlags());

  /**
   * Constructs with the flags to apply to base entity and to related entities.
   *
   * @param entityFlags The base entity flags to apply.
   * @param relatedFlags The related entity flags to apply.
   */
  SzDetailLevel(long entityFlags, long relatedFlags) {
    this.entityFlags  = entityFlags;
    this.relatedFlags = relatedFlags;
  }

  /**
   * The flags to apply for the entity.
   */
  private long entityFlags = 0L;

  /**
   * The flags to apply if also retrieving related entities.
   */
  private long relatedFlags = 0L;

  /**
   * Gets the entity flags associated with this instance.
   *
   * @return The entity flags associated with this instance.
   */
  public long getEntityFlags() {
    return this.entityFlags;
  }

  /**
   * Gets the flags for related entities associated with this instance.
   *
   * @return The flags for related entities associated with this instance.
   */
  public long getRelatedFlags() {
    return this.relatedFlags;
  }
}
