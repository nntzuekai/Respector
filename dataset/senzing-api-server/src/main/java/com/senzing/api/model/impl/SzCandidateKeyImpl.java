package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzCandidateKey;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a default implementation of {@link SzCandidateKey}.
 */
@JsonDeserialize
public class SzCandidateKeyImpl implements SzCandidateKey {
  /**
   * The identifier for the candidate feature.
   */
  private Long featureId;

  /**
   * The feature type for the candidate feature.
   */
  private String featureType;

  /**
   * The feature value for the candidate feature.
   */
  private String featureValue;

  /**
   * Default constructor.
   */
  public SzCandidateKeyImpl() {
    this.featureId    = null;
    this.featureType  = null;
    this.featureValue = null;
  }

  /**
   * Gets the identifier for the candidate feature.
   *
   * @return The identifier for the candidate feature.
   */
  public Long getFeatureId() {
    return featureId;
  }

  /**
   * Sets the identifier for the candidate feature.
   *
   * @param featureId The identifier for the candidate feature.
   */
  public void setFeatureId(Long featureId) {
    this.featureId = featureId;
  }

  /**
   * Gets the feature type for the candidate feature.
   *
   * @return The feature type for the candidate feature.
   */
  public String getFeatureType() {
    return featureType;
  }

  /**
   * Sets the feature type for the candidate feature.
   *
   * @param featureType The feature type for the candidate feature.
   */
  public void setFeatureType(String featureType) {
    this.featureType = featureType;
  }

  /**
   * Gets the feature value for the candidate feature.
   *
   * @return The feature value for the candidate feature.
   */
  public String getFeatureValue() {
    return featureValue;
  }

  /**
   * Sets the feature value for the candidate feature.
   *
   * @param featureValue The feature value for the candidate feature.
   */
  public void setFeatureValue(String featureValue) {
    this.featureValue = featureValue;
  }

  /**
   * Parses the {@link SzCandidateKeyImpl} from a {@link JsonObject} describing JSON
   * for the Senzing native API format for a candidate key to create a new
   * instance.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @param featureType The feature type for the {@link SzCandidateKeyImpl}.
   *
   * @return The {@link SzCandidateKeyImpl} that was created.
   */
  public static SzCandidateKeyImpl parseCandidateKey(JsonObject jsonObject,
                                                     String     featureType)
  {
    Long    featureId     = JsonUtilities.getLong(jsonObject, "FEAT_ID");
    String  featureValue  = JsonUtilities.getString(jsonObject, "FEAT_DESC");

    SzCandidateKeyImpl result = new SzCandidateKeyImpl();

    result.setFeatureId(featureId);
    result.setFeatureType(featureType);
    result.setFeatureValue(featureValue);

    return result;
  }

  /**
   * Parses and populates a {@link List} of {@link SzCandidateKeyImpl} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for candidate keys to create new
   * instances.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @param featureType The feature type for the {@link SzCandidateKeyImpl}
   *                    instances.
   *
   * @return The {@link List} of {@link SzCandidateKeyImpl} instances that were
   *         populated.
   */
  public static List<SzCandidateKeyImpl> parseCandidateKeyList(
      JsonArray   jsonArray,
      String      featureType)
  {
    return parseCandidateKeyList(null, jsonArray, featureType);
  }

  /**
   * Parses and populates a {@link List} of {@link SzCandidateKeyImpl} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for candidate keys to create new
   * instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @param featureType The feature type for the {@link SzCandidateKeyImpl}
   *                    instances.
   *
   * @return The {@link List} of {@link SzCandidateKeyImpl} instances that were
   *         populated.
   */
  public static List<SzCandidateKeyImpl> parseCandidateKeyList(
      List<SzCandidateKeyImpl>  list,
      JsonArray             jsonArray,
      String                featureType)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseCandidateKey(jsonObject, featureType));
    }

    return list;
  }

}
