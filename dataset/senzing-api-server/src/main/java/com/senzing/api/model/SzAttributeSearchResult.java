package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzAttributeSearchResultImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;
import java.util.function.Function;

import static com.senzing.api.model.SzAttributeSearchResultType.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

/**
 * Describes a search attribute result which extends the {@link
 * SzBaseRelatedEntity} to add the {@link SzAttributeSearchResultType} and
 * the {@link SzSearchFeatureScore} instances.
 */
@JsonDeserialize(using=SzAttributeSearchResult.Factory.class)
public interface SzAttributeSearchResult extends SzBaseRelatedEntity {
  /**
   * Gets the {@link SzRelationshipType} describing the type of relation.
   *
   * @return The {@link SzRelationshipType} describing the type of relation.
   */
  SzAttributeSearchResultType getResultType();

  /**
   * Sets the {@link SzAttributeSearchResultType} describing the type of
   * relation.
   *
   * @param resultType The {@link SzAttributeSearchResultType} describing the
   *                   type of relation.
   */
  void setResultType(SzAttributeSearchResultType resultType);

  /**
   * Gets the best name score from the search match.  This is the best of the
   * full name scores and organization name scores.  This is <tt>null</tt> if
   * there are no such name scores.
   *
   * @return The best name score from the search match, or <tt>null</tt> if
   *         no full name or organization scores.
   */
  @JsonInclude(NON_NULL)
  Integer getBestNameScore();

  /**
   * Sets the best full name score from the search match.  This is the best of
   * the full name scores and organization name scores.  Set this to
   * <tt>null</tt> if there are no such name scores.
   *
   * @param score The best name score from the search match, or
   *              <tt>null</tt> if no full name or organization name scores.
   */
  void setBestNameScore(Integer score);

  /**
   * Gets the <b>unmodifiable</b> {@link Map} of {@link String} feature type
   * keys to <b>unmodifiable</b> {@link List} values containing instances of
   * {@link SzSearchFeatureScore} describing the feature scores for that type.
   *
   * @return The <b>unmodifiable</b> {@link Map} of {@link String} feature type
   *         keys to <b>unmodifiable</b> {@link List} values contianing
   *         instances of {@link SzSearchFeatureScore} describing the feature scores
   *         for that type.
   */
  @JsonInclude(NON_EMPTY)
  Map<String, List<SzSearchFeatureScore>> getFeatureScores();

  /**
   * Adds the specified {@link SzSearchFeatureScore} to this instance.
   *
   * @param featureScore The {@link SzSearchFeatureScore} to add.
   */
  void addFeatureScore(SzSearchFeatureScore featureScore);

  /**
   * Sets the feature scores using a {@link Map} of {@link String} feature type
   * keys to {@link SzSearchFeatureScore} values.
   *
   * @param featureScores The {@link Map} of {@link String} feature type
   *                      keys to {@link SzSearchFeatureScore} values.
   */
  void setFeatureScores(Map<String, List<SzSearchFeatureScore>> featureScores);

  /**
   * Gets the {@link List} of {@linkplain SzRelatedEntity related entities}.
   *
   * @return The {@link List} of {@linkplain SzRelatedEntity related entities}.
   */
  @JsonInclude(NON_EMPTY)
  List<SzRelatedEntity> getRelatedEntities();

  /**
   * Sets the {@link List} of {@linkplain SzRelatedEntity related entities}.
   *
   * @param relatedEntities The {@link List} of {@linkplain SzRelatedEntity
   *                        related entities}.
   */
  void setRelatedEntities(List<SzRelatedEntity> relatedEntities);

  /**
   * Adds the specified {@link SzRelatedEntity}
   */
  void addRelatedEntity(SzRelatedEntity relatedEntity);

  /**
   * A {@link ModelProvider} for instances of {@link SzAttributeSearchResult}.
   */
  interface Provider extends ModelProvider<SzAttributeSearchResult> {
    /**
     * Creates a new instance of {@link SzAttributeSearchResult}.
     *
     * @return The new instance of {@link SzAttributeSearchResult}
     */
    SzAttributeSearchResult create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzAttributeSearchResult} that produces instances of
   * {@link SzAttributeSearchResultImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzAttributeSearchResult>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzAttributeSearchResult.class, SzAttributeSearchResultImpl.class);
    }

    @Override
    public SzAttributeSearchResult create() {
      return new SzAttributeSearchResultImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzAttributeSearchResult}.
   */
  class Factory extends ModelFactory<SzAttributeSearchResult, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzAttributeSearchResult.class);
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
     * Creates a new instance of {@link SzAttributeSearchResult}.
     * @return The new instance of {@link SzAttributeSearchResult}.
     */
    public SzAttributeSearchResult create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a list of resolved entities from a {@link JsonArray} describing a
   * JSON array in the Senzing native API format for entity features and
   * populates the specified {@link List} or creates a new {@link List}.
   *
   * @param list The {@link List} of {@link SzAttributeSearchResult} instances to
   *             populate, or <tt>null</tt> if a new {@link List}
   *             should be created.
   *
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link List} of {@link
   *         SzAttributeSearchResult} instances.
   */
  static List<SzAttributeSearchResult> parseSearchResultList(
      List<SzAttributeSearchResult> list,
      JsonArray                     jsonArray,
      Function<String,String>       featureToAttrClassMapper)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseSearchResult(null,
                                 jsonObject,
                                 featureToAttrClassMapper));
    }
    return list;
  }

  /**
   * Parses the entity feature from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity feature and populates
   * the specified {@link SzAttributeSearchResult} or creates a new instance.
   *
   * @param searchResult The {@link SzAttributeSearchResult} instance to
   *                     populate, or <tt>null</tt> if a new instance should
   *                     be created.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link SzAttributeSearchResult}.
   */
  static SzAttributeSearchResult parseSearchResult(
      SzAttributeSearchResult searchResult,
      JsonObject              jsonObject,
      Function<String,String> featureToAttrClassMapper)
  {
    SzAttributeSearchResult result = (searchResult != null)
        ? searchResult : SzAttributeSearchResult.FACTORY.create();

    Function<String,String> mapper = featureToAttrClassMapper;

    SzBaseRelatedEntity.parseBaseRelatedEntity(result, jsonObject, mapper);

    JsonObject entityObject = JsonUtilities.getJsonObject(jsonObject, "ENTITY");
    if (entityObject == null) {
      entityObject = jsonObject;
    }
    JsonArray relatedArray = JsonUtilities.getJsonArray(entityObject,
                                                    "RELATED_ENTITIES");

    List<SzRelatedEntity> relatedEntities = null;
    if (relatedArray != null) {
      relatedEntities = SzRelatedEntity.parseRelatedEntityList(null,
                                                               relatedArray,
                                                               mapper);
    }

    SzAttributeSearchResultType resultType = null;
    switch (result.getMatchLevel()) {
      case 1:
        resultType = MATCH;
        break;
      case 2:
        resultType = POSSIBLE_MATCH;
        break;
      case 3:
        resultType = POSSIBLE_RELATION;
        break;
      case 4:
        resultType = NAME_ONLY_MATCH;
        break;
    }
    result.setResultType(resultType);
    if (relatedEntities != null) {
      result.setRelatedEntities(relatedEntities);
    }

    // check if we have a MATCH_INFO object and if so use it for match information
    JsonObject matchInfo = JsonUtilities.getJsonObject(jsonObject,"MATCH_INFO");

    // parse the feature scores
    if (matchInfo != null) {
      JsonObject featureScoresObject
          = JsonUtilities.getJsonObject(matchInfo, "FEATURE_SCORES");

      Map<String, List<SzSearchFeatureScore>> featureScoreMap
          = new LinkedHashMap<>();

      featureScoresObject.entrySet().forEach(entry -> {
        String featureType = entry.getKey();

        JsonValue jsonValue = entry.getValue();

        JsonArray jsonArray = jsonValue.asJsonArray();

        List<SzSearchFeatureScore> featureScores
            = SzSearchFeatureScore.parseFeatureScoreList(jsonArray, featureType);

        featureScoreMap.put(featureType, featureScores);

        // check if this is for a name
        if (featureType.equals("NAME")) {
          // find the best name score
          Integer bestNameScore = null;

          // iterate through the search feature scores
          for (SzSearchFeatureScore featureScore : featureScores) {
            // get the name scoring details
            SzNameScoring nameScoring = featureScore.getNameScoringDetails();
            if (nameScoring == null) continue;

            // retrieve the full name and org name score
            Integer fullNameScore = nameScoring.getFullNameScore();
            Integer orgNameScore  = nameScoring.getOrgNameScore();

            // check for null values and get the maximum of the two scores
            if (fullNameScore == null) fullNameScore = -1;
            if (orgNameScore == null) orgNameScore = -1;
            int maxScore = Integer.max(fullNameScore, orgNameScore);

            // if we have a positive score and it is higher, then update
            if (maxScore > 0
                && (bestNameScore == null || maxScore > bestNameScore))
            {
              bestNameScore = maxScore;
            }
          }

          // set the best name score
          result.setBestNameScore(bestNameScore);
        }
      });
      // set the feature scores
      result.setFeatureScores(featureScoreMap);
    }

    // return the result
    return result;
  }
}
