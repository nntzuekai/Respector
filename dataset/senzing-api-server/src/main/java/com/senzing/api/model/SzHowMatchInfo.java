package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzHowMatchInfoImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.senzing.api.model.SzMatchLevel.NO_MATCH;

/**
 * The match info describing why two entities (or records) resolve or
 * relate to one another.
 */
@JsonDeserialize(using= SzHowMatchInfo.Factory.class)
public interface SzHowMatchInfo {
  /**
   * Gets the why key indicating the components of the match (similar to the
   * match key).
   *
   * @return The why key indicating the components of the match.
   */
  @JsonInclude(NON_EMPTY)
  String getMatchKey();

  /**
   * Sets the why key indicating the components of the match (similar to the
   * match key).
   *
   * @param matchKey The why key indicating the components of the match.
   */
  void setMatchKey(String matchKey);

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
   * A {@link ModelProvider} for instances of {@link SzHowMatchInfo}.
   */
  interface Provider extends ModelProvider<SzHowMatchInfo> {
    /**
     * Creates a new instance of {@link SzHowMatchInfo}.
     *
     * @return The new instance of {@link SzHowMatchInfo}
     */
    SzHowMatchInfo create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzHowMatchInfo} that produces instances of {@link SzHowMatchInfoImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzHowMatchInfo>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzHowMatchInfo.class, SzHowMatchInfoImpl.class);
    }

    @Override
    public SzHowMatchInfo create() {
      return new SzHowMatchInfoImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzHowMatchInfo}.
   */
  class Factory extends ModelFactory<SzHowMatchInfo, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzHowMatchInfo.class);
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
     * Creates a new instance of {@link SzHowMatchInfo}.
     * @return The new instance of {@link SzHowMatchInfo}.
     */
    public SzHowMatchInfo create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API JSON to build an instance of {@link SzHowMatchInfo}.
   *
   * @param jsonObject The {@link JsonObject} describing the match info using
   *                   the native API JSON format.
   *
   * @return The created instance of {@link SzHowMatchInfo}.
   */
  static SzHowMatchInfo parseMatchInfo(JsonObject jsonObject)
  {
    SzHowMatchInfo result = SzHowMatchInfo.FACTORY.create();

    String matchKey = JsonUtilities.getString(jsonObject, "MATCH_KEY");
    if (matchKey != null && matchKey.trim().length() == 0) matchKey = null;

    String ruleCode = JsonUtilities.getString(jsonObject, "WHY_ERRULE_CODE");
    if (ruleCode != null && ruleCode.trim().length() == 0) ruleCode = null;

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

    result.setMatchKey(matchKey);
    result.setResolutionRule(ruleCode);
    result.setFeatureScores(featureScoreMap);

    return result;
  }

}
