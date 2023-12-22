package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzSearchFeatureScoreImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Describes the scoring between {@link SzScoredFeature} instances.
 */
@JsonDeserialize(using=SzSearchFeatureScore.Factory.class)
public interface SzSearchFeatureScore {
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
   * Gets the inbound feature value as a {@link String}.
   *
   * @return The inbound feature value as a {@link String}.
   */
  String getInboundFeature();

  /**
   * Sets the inbound feature value as a {@link String}.
   *
   * @param inboundFeature The inbound feature value as a {@link String}.
   */
  void setInboundFeature(String inboundFeature);

  /**
   * Gets the feature value of the candidate match for the inbound feature as
   * a {@link String}.
   *
   * @return The feature value of the candidate match for the inbound feature as
   *         a {@link String}.
   */
  String getCandidateFeature();

  /**
   * Sets the feature value that describes the candidate match for the inbound
   * feature.
   *
   * @param candidateFeature The feature value that describes the candidate
   *                         match for the inbound feature.
   */
  void setCandidateFeature(String candidateFeature);

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
   * A {@link ModelProvider} for instances of {@link SzSearchFeatureScore}.
   */
  interface Provider extends ModelProvider<SzSearchFeatureScore> {
    /**
     * Creates a new instance of {@link SzSearchFeatureScore}.
     *
     * @return The new instance of {@link SzSearchFeatureScore}
     */
    SzSearchFeatureScore create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzSearchFeatureScore} that produces instances of
   * {@link SzSearchFeatureScoreImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzSearchFeatureScore>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzSearchFeatureScore.class, SzSearchFeatureScoreImpl.class);
    }

    @Override
    public SzSearchFeatureScore create() {
      return new SzSearchFeatureScoreImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzSearchFeatureScore}.
   */
  class Factory extends ModelFactory<SzSearchFeatureScore, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzSearchFeatureScore.class);
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
     * Creates a new instance of {@link SzSearchFeatureScore}.
     * @return The new instance of {@link SzSearchFeatureScore}.
     */
    public SzSearchFeatureScore create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the {@link SzSearchFeatureScore} from a {@link JsonObject}
   * describing JSON for the Senzing native API format for a feature score to
   * create a new instance.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @param featureType The feature type for the {@link SzSearchFeatureScore}.
   *
   * @return The {@link SzSearchFeatureScore} that was created.
   */
  static SzSearchFeatureScore parseFeatureScore(JsonObject jsonObject,
                                                String     featureType)
  {
    Integer score     = JsonUtilities.getInteger(jsonObject, "FULL_SCORE");
    String  inbound   = JsonUtilities.getString(jsonObject, "INBOUND_FEAT");
    String  candidate = JsonUtilities.getString(jsonObject, "CANDIDATE_FEAT");

    SzNameScoring nameScoring = null;
    if (score == null || featureType.equalsIgnoreCase("NAME")) {
      nameScoring = SzNameScoring.parseNameScoring(jsonObject);
      if (score == null && nameScoring != null) {
        score = nameScoring.asFullScore();
      }
    }

    SzSearchFeatureScore result = SzSearchFeatureScore.FACTORY.create();

    result.setFeatureType(featureType);
    result.setInboundFeature(inbound);
    result.setCandidateFeature(candidate);
    result.setScore(score);
    result.setNameScoringDetails(nameScoring);

    return result;
  }

  /**
   * Parses and populates a {@link List} of {@link SzSearchFeatureScore} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for feature scores to create new
   * instances.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @param featureType The feature type for the {@link SzSearchFeatureScore}
   *                    instances.
   *
   * @return The {@link List} of {@link SzSearchFeatureScore} instances that were
   *         populated.
   */
  static List<SzSearchFeatureScore> parseFeatureScoreList(
      JsonArray   jsonArray,
      String      featureType)
  {
    return parseFeatureScoreList(null, jsonArray, featureType);
  }

  /**
   * Parses and populates a {@link List} of {@link SzSearchFeatureScore} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for candidate keys to create new
   * instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @param featureType The feature type for the {@link SzSearchFeatureScore}
   *                    instances.
   *
   * @return The {@link List} of {@link SzSearchFeatureScore} instances that were
   *         populated.
   */
  static List<SzSearchFeatureScore> parseFeatureScoreList(
      List<SzSearchFeatureScore>  list,
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
