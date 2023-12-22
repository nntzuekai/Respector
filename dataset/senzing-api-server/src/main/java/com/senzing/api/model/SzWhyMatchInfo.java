package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyMatchInfoImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.senzing.api.model.SzMatchLevel.*;

/**
 * The match info describing why two entities (or records) resolve or
 * relate to one another.
 */
@JsonDeserialize(using= SzWhyMatchInfo.Factory.class)
public interface SzWhyMatchInfo {
  /**
   * Gets the why key indicating the components of the match (similar to the
   * match key).
   *
   * @return The why key indicating the components of the match.
   */
  @JsonInclude(NON_EMPTY)
  String getWhyKey();

  /**
   * Sets the why key indicating the components of the match (similar to the
   * match key).
   *
   * @param whyKey The why key indicating the components of the match.
   */
  void setWhyKey(String whyKey);

  /**
   * Returns the {@link SzMatchLevel} describing how the records resolve
   * against each other.
   *
   * @return The {@link SzMatchLevel} describing how the records resolve
   *         against each other.
   */
  SzMatchLevel getMatchLevel();

  /**
   * Sets the {@link SzMatchLevel} describing how the records resolve
   * against each other.
   *
   * @param matchLevel The {@link SzMatchLevel} describing how the records
   *                   resolve against each other.
   */
  void setMatchLevel(SzMatchLevel matchLevel);

  /**
   * Gets the resolution rule that triggered the match.
   *
   * @return The resolution rule that triggered the match.
   */
  @JsonInclude(NON_EMPTY)
  String getResolutionRule();

  /**
   * Sets the resolution rule that triggered the match.
   *
   * @param resolutionRule The resolution rule that triggered the match.
   */
  void setResolutionRule(String resolutionRule);

  /**
   * Gets the <b>unmodifiable</b> {@link Map} of {@link String} feature type
   * keys to <b>unmodifiable</b> {@link List} values containing instances of
   * {@link SzCandidateKey} describing the candidate keys for that type.
   *
   * @return The <b>unmodifiable</b> {@link Map} of {@link String} feature type
   *         keys to <b>unmodifiable</b> {@link List} values containing
   *         instances of {@link SzCandidateKey} describing the candidate keys
   *         for that type.
   */
  @JsonInclude(NON_EMPTY)
  Map<String, List<SzCandidateKey>> getCandidateKeys();

  /**
   * Adds the specified {@link SzCandidateKey} to this instance.
   *
   * @param candidateKey The {@link SzCandidateKey} to add.
   */
  void addCandidateKey(SzCandidateKey candidateKey);

  /**
   * Sets the candidate keys using the specified {@link Map} of {@link String}
   * feature type keys to {@link SzCandidateKey} values.
   *
   * @param candidateKeys The {@link Map} of {@link String} feature type
   *                      keys to {@link SzCandidateKey} values.
   */
  void setCandidateKeys(Map<String, List<SzCandidateKey>> candidateKeys);

  /**
   * Gets the <b>unmodifiable</b> {@link Map} of {@link String} feature type
   * keys to <b>unmodifiable</b> {@link List} values containing instances of
   * {@link SzFeatureScore} describing the feature scores for that type.
   *
   * @return The <b>unmodifiable</b> {@link Map} of {@link String} feature type
   *         keys to <b>unmodifiable</b> {@link List} values containing
   *         instances of {@link SzFeatureScore} describing the feature scores
   *         for that type.
   */
  @JsonInclude(NON_EMPTY)
  Map<String, List<SzFeatureScore>> getFeatureScores();

  /**
   * Adds the specified {@link SzFeatureScore} to this instance.
   *
   * @param featureScore The {@link SzFeatureScore} to add.
   */
  void addFeatureScore(SzFeatureScore featureScore);

  /**
   * Sets the feature scores using the specified {@link Map} of {@link String}
   * feature type keys to {@link SzFeatureScore} values.
   *
   * @param featureScores The {@link Map} of {@link String} feature type
   *                      keys to {@link SzFeatureScore} values.
   */
  void setFeatureScores(Map<String, List<SzFeatureScore>> featureScores);

  /**
   * Gets the <b>unmodifiable</b> {@link List} of {@link
   * SzDisclosedRelation} objects describing the disclosed relationships
   * between two entities.  If this {@link SzWhyMatchInfo} instance is for a
   * single entity then this list is empty.
   *
   * @return The <b>unmodifiable</b> {@link List} of {@link
   *         SzDisclosedRelation} objects describing the disclosed
   *         relationships between two entities.
   */
  @JsonInclude(NON_EMPTY)
  List<SzDisclosedRelation> getDisclosedRelations();

  /**
   * Sets the disclosed relationships for this match info to those in the
   * specified {@link Collection} of {@link SzDisclosedRelation}
   * instances.
   *
   * @param relations The {@link Collection} of {@link SzDisclosedRelation}
   *                  instances for this instance.
   */
  void setDisclosedRelations(Collection<SzDisclosedRelation> relations);

  /**
   * Adds the specified {@link SzDisclosedRelation} to the list of
   * disclosed relationships for this match info.
   *
   * @param relation The {@link SzDisclosedRelation} instance describing the
   *                 disclosed relationship to add.
   */
  void addDisclosedRelation(SzDisclosedRelation relation);

  /**
   * Removes all disclosed relationships from the list of disclosed
   * relationships for this match info.
   */
  void clearDisclosedRelations();

  /**
   * A {@link ModelProvider} for instances of {@link SzWhyMatchInfo}.
   */
  interface Provider extends ModelProvider<SzWhyMatchInfo> {
    /**
     * Creates a new instance of {@link SzWhyMatchInfo}.
     *
     * @return The new instance of {@link SzWhyMatchInfo}
     */
    SzWhyMatchInfo create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyMatchInfo} that produces instances of {@link SzWhyMatchInfoImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyMatchInfo>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyMatchInfo.class, SzWhyMatchInfoImpl.class);
    }

    @Override
    public SzWhyMatchInfo create() {
      return new SzWhyMatchInfoImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzWhyMatchInfo}.
   */
  class Factory extends ModelFactory<SzWhyMatchInfo, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyMatchInfo.class);
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
     * Creates a new instance of {@link SzWhyMatchInfo}.
     * @return The new instance of {@link SzWhyMatchInfo}.
     */
    public SzWhyMatchInfo create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API JSON to build an instance of {@link SzWhyMatchInfo}.
   *
   * @param jsonObject The {@link JsonObject} describing the match info using
   *                   the native API JSON format.
   *
   * @return The created instance of {@link SzWhyMatchInfo}.
   */
  static SzWhyMatchInfo parseMatchInfo(JsonObject jsonObject)
  {
    SzWhyMatchInfo result = SzWhyMatchInfo.FACTORY.create();

    String whyKey   = JsonUtilities.getString(jsonObject, "WHY_KEY");
    if (whyKey != null && whyKey.trim().length() == 0) whyKey = null;

    SzMatchLevel matchLevel = NO_MATCH;
    String matchLevelCode
        = JsonUtilities.getString(jsonObject, "MATCH_LEVEL_CODE");
    if (matchLevelCode != null && matchLevelCode.trim().length() > 0) {
      matchLevel = SzMatchLevel.valueOf(matchLevelCode);
    }

    String ruleCode = JsonUtilities.getString(jsonObject, "WHY_ERRULE_CODE");
    if (ruleCode != null && ruleCode.trim().length() == 0) ruleCode = null;

    JsonObject candidateKeysObject
        = JsonUtilities.getJsonObject(jsonObject, "CANDIDATE_KEYS");

    Map<String, List<SzCandidateKey>> candidateKeyMap
        = new LinkedHashMap<>();

    candidateKeysObject.entrySet().forEach(entry -> {
      String featureType = entry.getKey();

      JsonValue jsonValue = entry.getValue();

      JsonArray jsonArray = jsonValue.asJsonArray();

      List<SzCandidateKey> candidateKeys
          = SzCandidateKey.parseCandidateKeyList(jsonArray, featureType);

      candidateKeyMap.put(featureType,  candidateKeys);
    });

    JsonObject featureScoresObject
        = JsonUtilities.getJsonObject(jsonObject, "FEATURE_SCORES");

    Map<String, List<SzFeatureScore>> featureScoreMap
        = new LinkedHashMap<>();

    featureScoresObject.entrySet().forEach(entry -> {
      String featureType = entry.getKey();

      JsonValue jsonValue = entry.getValue();

      JsonArray jsonArray = jsonValue.asJsonArray();

      List<SzFeatureScore> featureScores
          = SzFeatureScore.parseFeatureScoreList(jsonArray, featureType);

      featureScoreMap.put(featureType, featureScores);
    });

    JsonObject disclosedRelationshipObject
        = JsonUtilities.getJsonObject(jsonObject, "DISCLOSED_RELATIONS");

    List<SzDisclosedRelation> disclosedRelations
        = SzDisclosedRelation.parseDisclosedRelationships(
            disclosedRelationshipObject, whyKey);

    result.setWhyKey(whyKey);
    result.setMatchLevel(matchLevel);
    result.setResolutionRule(ruleCode);
    result.setCandidateKeys(candidateKeyMap);
    result.setFeatureScores(featureScoreMap);
    result.setDisclosedRelations(disclosedRelations);

    return result;
  }

}
