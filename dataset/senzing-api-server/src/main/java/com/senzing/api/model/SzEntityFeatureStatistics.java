package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityFeatureStatisticsImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;

/**
 * Describes the entity resolution statistics for the feature value.
 */
@JsonDeserialize(using=SzEntityFeatureStatistics.Factory.class)
public interface SzEntityFeatureStatistics {
  /**
   * Checks if the feature is used for finding candidates during entity
   * resolution.
   *
   * @return <tt>true</tt> if used for finding candidates during entity
   *         resolution, otherwise <tt>false</tt>.
   */
  Boolean isUsedForCandidates();

  /**
   * Sets whether or not the feature is used for finding candidates during
   * entity resolution.
   *
   * @param usedForCandidates <tt>true</tt> if used for finding candidates
   *                          during entity resolution, otherwise
   *                          <tt>false</tt>.
   */
  void setUsedForCandidates(Boolean usedForCandidates);

  /**
   * Checks if the feature is used for scoring during entity resolution.
   *
   * @return <tt>true</tt> if used for scoring during entity resolution,
   *         otherwise <tt>false</tt>.
   */
  Boolean isUsedForScoring();

  /**
   * Sets whether or not the feature is used for scoring during entity
   * resolution.
   *
   * @param usedForScoring <tt>true</tt> if used for scoring during entity
   *                       resolution, otherwise <tt>false</tt>.
   */
  void setUsedForScoring(Boolean usedForScoring);

  /**
   * Gets the number of entities having this feature value.
   *
   * @return The number of entities having this feature value.
   */
  Long getEntityCount();

  /**
   * Sets the number of entities having this feature value.
   *
   * @param entityCount The number of entities having this feature value.
   */
  void setEntityCount(Long entityCount);

  /**
   * Checks if this feature value is no longer being used to find candidates
   * because too many entities share the same value.
   *
   * @return <tt>true</tt> if this feature value is no longer being used to
   *         find candidates because too many entities share the same value,
   *         otherwise <tt>false</tt>.
   */
  Boolean isCandidateCapReached();

  /**
   * Sets whether or not this feature value is no longer being used to find
   * candidates because too many entities share the same value.
   *
   * @param candidateCapReached <tt>true</tt> if this feature value is no longer
   *                            being used to find candidates because too many
   *                            entities share the same value, otherwise
   *                            <tt>false</tt>.
   */
  void setCandidateCapReached(Boolean candidateCapReached);

  /**
   * Checks if this feature value is no longer being used in entity scoring
   * because too many entities share the same value.
   *
   * @return <tt>true</tt> if this feature value is no longer being used in
   *         entity scoring because too many entities share the same value,
   *         otherwise <tt>false</tt>.
   */
  Boolean isScoringCapReached();

  /**
   * Sets whether or not this feature value is no longer being used in entity
   * scoring because too many entities share the same value.
   *
   * @param scoringCapReached <tt>true</tt> if this feature value is no longer
   *                          being used in entity scoring because too many
   *                          entities share the same value, otherwise
   *                          <tt>false</tt>.
   */
  void setScoringCapReached(Boolean scoringCapReached);

  /**
   * Checks if this value was suppressed in favor of a more complete value.
   *
   * @return <tt>true</tt> if this value was suppressed in favor of a more
   *         complete value, otherwise <tt>false</tt>.
   */
  Boolean isSuppressed();

  /**
   * Sets whether or not this value was suppressed in favor of a more complete
   * value.
   *
   * @param suppressed <tt>true</tt> if this value was suppressed in favor of a
   *                   more complete value, otherwise <tt>false</tt>.
   */
  void setSuppressed(Boolean suppressed);

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityFeatureStatistics}.
   */
  interface Provider extends ModelProvider<SzEntityFeatureStatistics> {
    /**
     * Creates a new instance of {@link SzEntityFeatureStatistics}.
     *
     * @return The new instance of {@link SzEntityFeatureStatistics}
     */
    SzEntityFeatureStatistics create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityFeatureStatistics} that produces instances of {@link
   * SzEntityFeatureStatisticsImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityFeatureStatistics>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityFeatureStatistics.class,
            SzEntityFeatureStatisticsImpl.class);
    }

    @Override
    public SzEntityFeatureStatistics create() {
      return new SzEntityFeatureStatisticsImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzEntityFeatureStatistics}.
   */
  class Factory extends ModelFactory<SzEntityFeatureStatistics, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityFeatureStatistics.class);
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
     * Creates a new instance of {@link SzEntityFeatureStatistics}.
     * @return The new instance of {@link SzEntityFeatureStatistics}.
     */
    public SzEntityFeatureStatistics create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API Senzing JSON to create an instance of
   * {@link SzEntityFeatureStatistics} and returns <tt>null</tt> if none of
   * the statistics are available.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzEntityFeatureStatistics} that was parsed.
   */
  static SzEntityFeatureStatistics parseEntityFeatureStatistics(
      JsonObject            jsonObject)
  {
    Boolean candidateUse = getBoolean(jsonObject, "USED_FOR_CAND");
    Boolean scoringUse   = getBoolean(jsonObject, "USED_FOR_SCORING");
    Long    entityCount  = JsonUtilities.getLong(jsonObject, "ENTITY_COUNT");
    Boolean candidateCap = getBoolean(jsonObject, "CANDIDATE_CAP_REACHED");
    Boolean scoringCap   = getBoolean(jsonObject, "SCORING_CAP_REACHED");
    Boolean suppressed   = getBoolean(jsonObject, "SUPPRESSED");

    // check if we have no stats
    if (candidateUse == null && scoringUse == null && entityCount == null
        && candidateCap == null && scoringCap == null && suppressed == null) {
      return null;
    }

    SzEntityFeatureStatistics statistics
        = SzEntityFeatureStatistics.FACTORY.create();
    statistics.setUsedForCandidates(candidateUse);
    statistics.setUsedForScoring(scoringUse);
    statistics.setEntityCount(entityCount);
    statistics.setCandidateCapReached(candidateCap);
    statistics.setScoringCapReached(scoringCap);
    statistics.setSuppressed(suppressed);

    return statistics;
  }

  /**
   * Gets a {@link Boolean} value that is designated as <tt>"Y"</tt> for
   * <tt>true</tt> and <tt>"N"</tt> for <tt>false</tt>.
   *
   * @param jsonObject The {@link JsonObject} to obtain the value from.
   * @param key The property key to obtain the value for.
   * @return {@link Boolean#TRUE} if <tt>true</tt>, {@link Boolean#FALSE} if
   *         <tt>false</tt> and <tt>null</tt> if missing, <tt>null</tt> or
   *         empty string.
   */
  private static Boolean getBoolean(JsonObject jsonObject, String key) {
    String text = JsonUtilities.getString(jsonObject, key);
    if (text == null || text.trim().length() == 0) return null;
    text = text.trim();
    return text.equals("Y");
  }

}
