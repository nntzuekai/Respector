package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzFeatureScoreImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Describes the scoring between {@link SzScoredFeature} instances.
 */
@JsonDeserialize(using=SzFeatureScore.Factory.class)
public interface SzFeatureScore {
  /**
   * Gets the feature type for the features being scored.
   *
   * @return The feature type for the features being scored.
   */
  String getFeatureType();

  /**
   * Sets the feature type for the features being scored.
   *
   * @param featureType The feature type for the features being scored.
   */
  void setFeatureType(String featureType);

  /**
   * Gets the inbound feature described as an {@link SzScoredFeature}.
   *
   * @return The inbound feature described as an {@link SzScoredFeature}.
   */
  SzScoredFeature getInboundFeature();

  /**
   * Sets the inbound feature described as an {@link SzScoredFeature}.
   *
   * @param inboundFeature The inbound feature described as an {@link
   *                       SzScoredFeature}.
   */
  void setInboundFeature(SzScoredFeature inboundFeature);

  /**
   * Gets the {@link SzScoredFeature} that describes the candidate match for
   * the inbound feature.
   *
   * @return The {@link SzScoredFeature} that describes the candidate match for
   *         the inbound feature.
   */
  SzScoredFeature getCandidateFeature();

  /**
   * Sets the {@link SzScoredFeature} that describes the candidate match for
   * the inbound feature.
   *
   * @param candidateFeature The {@link SzScoredFeature} that describes the
   *                         candidate match for the inbound feature.
   */
  void setCandidateFeature(SzScoredFeature candidateFeature);

  /**
   * Gets the integer score between the two feature values (typically from 0
   * and 100).  If the score has not been explicitly set, but the {@linkplain
   * #setNameScoringDetails(SzNameScoring) name scoring details} have been set
   * then this returns {@link SzNameScoring#asFullScore()}.
   *
   * @return The integer score between the two feature values (typically from 0
   *         and 100).
   */
  Integer getScore();

  /**
   * Sets the integer score between the two feature values (typically from 0
   * and 100).
   *
   * @param score The integer score between the two feature values (typically
   *              from 0 and 100).
   */
  void setScore(Integer score);

  /**
   * Gets the name scoring details if any exist.  This method returns
   * <tt>null</tt> if the scored feature was not a name.
   *
   * @return The name scoring details, or <tt>null</tt> if the scored feature
   *         was not a name.
   */
  @JsonInclude(NON_EMPTY)
  SzNameScoring getNameScoringDetails();

  /**
   * Sets the name scoring details if any exist.  Set the value to <tt>null</tt>
   * if the scored feature was not a name.
   *
   * @param scoring The {@link SzNameScoring} describing the name-scoring
   *                details.
   */
  void setNameScoringDetails(SzNameScoring scoring);

  /**
   * Gets the {@link SzScoringBucket} describing the meaning of the score.
   *
   * @return The {@link SzScoringBucket} describing the meaning of the score.
   */
  SzScoringBucket getScoringBucket();

  /**
   * Sets the {@link SzScoringBucket} describing the meaning of the score.
   *
   * @param scoringBucket The {@link SzScoringBucket} describing the meaning
   *                      of the score.
   */
  void setScoringBucket(SzScoringBucket scoringBucket);

  /**
   * Gets the {@link SzScoringBehavior} describing the scoring behavior for the
   * features.
   *
   * @return The {@link SzScoringBehavior} describing the scoring behavior for
   *         the features.
   */
  SzScoringBehavior getScoringBehavior();

  /**
   * Sets the {@link SzScoringBehavior} describing the scoring behavior for the
   * features.
   *
   * @param scoringBehavior The {@link SzScoringBehavior} describing the scoring
   *                        behavior for the features.
   */
  void setScoringBehavior(SzScoringBehavior scoringBehavior);

  /**
   * A {@link ModelProvider} for instances of {@link SzFeatureScore}.
   */
  interface Provider extends ModelProvider<SzFeatureScore> {
    /**
     * Creates a new instance of {@link SzFeatureScore}.
     *
     * @return The new instance of {@link SzFeatureScore}
     */
    SzFeatureScore create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzFeatureScore} that produces instances of {@link SzFeatureScoreImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzFeatureScore>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzFeatureScore.class, SzFeatureScoreImpl.class);
    }

    @Override
    public SzFeatureScore create() {
      return new SzFeatureScoreImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzFeatureScore}.
   */
  class Factory extends ModelFactory<SzFeatureScore, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzFeatureScore.class);
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
     * Creates a new instance of {@link SzFeatureScore}.
     * @return The new instance of {@link SzFeatureScore}.
     */
    public SzFeatureScore create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the {@link SzFeatureScore} from a {@link JsonObject} describing JSON
   * for the Senzing native API format for a feature score to create a new
   * instance.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @param featureType The feature type for the {@link SzFeatureScore}.
   *
   * @return The {@link SzFeatureScore} that was created.
   */
  static SzFeatureScore parseFeatureScore(JsonObject  jsonObject,
                                          String      featureType)
  {
    SzScoredFeature inboundFeature = SzScoredFeature.parseScoredFeature(
        jsonObject, "INBOUND_", featureType);

    SzScoredFeature candidateFeature = SzScoredFeature.parseScoredFeature(
        jsonObject, "CANDIDATE_", featureType);

    String  bucket    = JsonUtilities.getString(jsonObject, "SCORE_BUCKET");
    String  behavior  = JsonUtilities.getString(jsonObject, "SCORE_BEHAVIOR");
    Integer score     = JsonUtilities.getInteger(jsonObject, "FULL_SCORE");

    SzNameScoring nameScoring = null;
    if (score == null || featureType.equalsIgnoreCase("NAME")) {
      nameScoring = SzNameScoring.parseNameScoring(jsonObject);
      if (score == null && nameScoring != null) {
        score = nameScoring.asFullScore();
      }
    }

    SzScoringBucket scoringBucket = null;
    try {
      scoringBucket = SzScoringBucket.valueOf(bucket);
    } catch (Exception e) {
      System.err.println("FAILED TO PARSE SCORE_BUCKET: " + bucket);
      e.printStackTrace();
    }
    SzScoringBehavior scoringBehavior = null;
    try {
      scoringBehavior = SzScoringBehavior.parse(behavior);
    } catch (Exception e) {
      System.err.println("FAILED TO PARSE SCORE_BEHAVIOR: " + behavior);
      e.printStackTrace();
    }

    SzFeatureScore result = SzFeatureScore.FACTORY.create();

    result.setFeatureType(featureType);
    result.setInboundFeature(inboundFeature);
    result.setCandidateFeature(candidateFeature);
    result.setScore(score);
    result.setNameScoringDetails(nameScoring);
    result.setScoringBehavior(scoringBehavior);
    result.setScoringBucket(scoringBucket);

    return result;
  }

  /**
   * Parses and populates a {@link List} of {@link SzFeatureScore} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for feature scores to create new
   * instances.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @param featureType The feature type for the {@link SzFeatureScore}
   *                    instances.
   *
   * @return The {@link List} of {@link SzFeatureScore} instances that were
   *         populated.
   */
  static List<SzFeatureScore> parseFeatureScoreList(
      JsonArray   jsonArray,
      String      featureType)
  {
    return parseFeatureScoreList(null, jsonArray, featureType);
  }

  /**
   * Parses and populates a {@link List} of {@link SzFeatureScore} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for candidate keys to create new
   * instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @param featureType The feature type for the {@link SzFeatureScore}
   *                    instances.
   *
   * @return The {@link List} of {@link SzFeatureScore} instances that were
   *         populated.
   */
  static List<SzFeatureScore> parseFeatureScoreList(
      List<SzFeatureScore>  list,
      JsonArray             jsonArray,
      String                featureType)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseFeatureScore(jsonObject, featureType));
    }

    return list;
  }

}
