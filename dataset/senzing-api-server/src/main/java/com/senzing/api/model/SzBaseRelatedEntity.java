package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;
import java.util.Optional;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes the base features for a related entity.
 */
public interface SzBaseRelatedEntity extends SzResolvedEntity {
  /**
   * Gets the underlying match level from the entity resolution between the
   * entities.
   *
   * @return The underlying match level from the entity resolution between the
   *         entities.
   */
  @JsonInclude(NON_NULL)
  Integer getMatchLevel();

  /**
   * Sets the underlying match level from the entity resolution between the
   * entities.
   *
   * @param matchLevel The underlying match level from the entity resolution
   *                   between the entities.
   */
  void setMatchLevel(Integer matchLevel);

  /**
   * Gets the underlying match key from the entity resolution between
   * the entities.
   *
   * @return The underlying match key from the entity resolution between
   *         the entities.
   */
  @JsonInclude(NON_NULL)
  String getMatchKey();

  /**
   * Sets the underlying match key from the entity resolution between
   * the entities.
   *
   * @param matchKey The underlying match key from the entity resolution
   *                 between the entities.
   */
  void setMatchKey(String matchKey);

  /**
   * Gets the underlying resolution rule code from the entity resolution
   * between the entities.
   *
   * @return The underlying resolution rule code from the entity resolution
   *         between the entities.
   */
  @JsonInclude(NON_NULL)
  String getResolutionRuleCode();

  /**
   * Sets the underlying resolution rule code from the entity resolution
   * between the entities.
   *
   * @param code The underlying resolution rule code from the entity resolution
   *             between the entities.
   */
  void setResolutionRuleCode(String code);

  /**
   * Parses the entity feature from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity feature and populates
   * the specified {@link SzBaseRelatedEntity} or creates a new instance.
   *
   * @param entity The {@link SzBaseRelatedEntity} instance to populate, (this
   *               cannot be <tt>null</tt>).
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link SzBaseRelatedEntity}.
   *
   * @throws NullPointerException If the specified entity or JSON object is
   *                              <tt>null</tt>.
   */
  static SzBaseRelatedEntity parseBaseRelatedEntity(
      SzBaseRelatedEntity     entity,
      JsonObject              jsonObject,
      Function<String,String> featureToAttrClassMapper)
    throws NullPointerException
  {
    Function<String,String> mapper = featureToAttrClassMapper;

    // check if we have a MATCH_INFO object and if so use it for match information
    JsonObject matchInfo = JsonUtilities.getJsonObject(jsonObject,"MATCH_INFO");

    // check if we have a RESOLVED_ENTITY object and if so use it for other fields
    JsonObject entityObject = JsonUtilities.getJsonObject(jsonObject, "ENTITY");
    JsonObject resolvedObject
        = JsonUtilities.getJsonObject(entityObject, "RESOLVED_ENTITY");
    if (resolvedObject != null) {
      jsonObject = resolvedObject;
    }

    SzResolvedEntity.parseResolvedEntity(entity, jsonObject, mapper);

    // if no match info, then assume the data is in the base object
    if (matchInfo == null) matchInfo = jsonObject;

    Integer matchLevel  = JsonUtilities.getInteger(matchInfo, "MATCH_LEVEL");
    String  matchKey    = JsonUtilities.getString(matchInfo, "MATCH_KEY");
    String  ruleCode    = JsonUtilities.getString(matchInfo,"ERRULE_CODE");
    boolean partial     = (!jsonObject.containsKey("FEATURES")
                           || !jsonObject.containsKey("RECORDS")
                           || (matchLevel == null)
                           || (matchKey == null)
                           || (ruleCode == null));

    final JsonObject matchObject = matchInfo;

    entity.setMatchLevel(matchLevel);
    entity.setMatchKey(matchKey);
    entity.setResolutionRuleCode(ruleCode);

    entity.setPartial(partial);

    // iterate over the feature map
    return entity;
  }
}
