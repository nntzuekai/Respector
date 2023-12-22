package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzWhyMatchInfo;
import com.senzing.api.model.SzWhyPerspective;
import com.senzing.api.model.SzWhyRecordsResult;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Provides a default implementation of {@link SzWhyRecordsResult}
 */
@JsonDeserialize
public class SzWhyRecordsResultImpl implements SzWhyRecordsResult {
  /**
   * The {@link SzWhyPerspective} identifying and describing the perspective
   * from the first record.
   */
  private SzWhyPerspective perspective1;

  /**
   * The {@link SzWhyPerspective} identifying and describing the perspective
   * from the second record.
   */
  private SzWhyPerspective perspective2;

  /**
   * The {@link SzWhyMatchInfo} providing the details of the result.
   */
  private SzWhyMatchInfo matchInfo;

  /**
   * Default constructor.
   */
  public SzWhyRecordsResultImpl() {
    this.perspective1 = null;
    this.perspective2 = null;
    this.matchInfo    = null;
  }

  /**
   * Gets the {@link SzWhyPerspective} identifying and describing the
   * perspective for this why result from the first record.
   *
   * @return The {@link SzWhyPerspective} identifying and describing the
   *         perspective for this why result from the first record.
   */
  @JsonInclude(NON_NULL)
  public SzWhyPerspective getPerspective1() {
    return this.perspective1;
  }

  /**
   * Sets the {@link SzWhyPerspective} identifying and describing the
   * perspective for this why result from the first record.
   *
   * @param perspective The {@link SzWhyPerspective} identifying and describing
   *                    the perspective for this why result from the first
   *                    record.
   */
  public void setPerspective1(SzWhyPerspective perspective) {
    this.perspective1 = perspective;
  }

  /**
   * Gets the {@link SzWhyPerspective} identifying and describing the
   * perspective for this why result from the first record.
   *
   * @return The {@link SzWhyPerspective} identifying and describing the
   *         perspective for this why result from the first record.
   */
  @JsonInclude(NON_NULL)
  public SzWhyPerspective getPerspective2() {
    return this.perspective2;
  }

  /**
   * Sets the {@link SzWhyPerspective} identifying and describing the
   * perspective for this why result from the first record.
   *
   * @param perspective The {@link SzWhyPerspective} identifying and describing
   *                    the perspective for this why result from the first
   *                    record.
   */
  public void setPerspective2(SzWhyPerspective perspective) {
    this.perspective2 = perspective;
  }

  /**
   * Gets the {@link SzWhyMatchInfo} providing the details of the result.
   *
   * @return The {@link SzWhyMatchInfo} providing the details of the result.
   */
  @JsonInclude(NON_NULL)
  public SzWhyMatchInfo getMatchInfo() {
    return this.matchInfo;
  }

  /**
   * Sets the {@link SzWhyMatchInfo} providing the details of the result.
   *
   * @param matchInfo The {@link SzWhyMatchInfo} providing the details of the
   *                  result.
   */
  public void setMatchInfo(SzWhyMatchInfo matchInfo) {
    this.matchInfo = matchInfo;
  }

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzWhyRecordsResultImpl}.
   *
   * @param jsonObject The {@link JsonObject} describing the why entity result
   *                   using the native API JSON format.
   *
   * @return The created instance of {@link SzWhyRecordsResultImpl}.
   */
  public static SzWhyRecordsResultImpl parseWhyRecordsResult(JsonObject jsonObject)
  {
    SzWhyPerspective perspective1
        = SzWhyPerspective.parseWhyPerspective(jsonObject);

    SzWhyPerspective perspective2
        = SzWhyPerspective.parseWhyPerspective(jsonObject, "_2");

    JsonObject infoJson = JsonUtilities.getJsonObject(jsonObject, "MATCH_INFO");

    SzWhyMatchInfo matchInfo
        = SzWhyMatchInfo.parseMatchInfo(infoJson);

    SzWhyRecordsResultImpl result = new SzWhyRecordsResultImpl();
    result.setPerspective1(perspective1);
    result.setPerspective2(perspective2);
    result.setMatchInfo(matchInfo);

    return result;
  }

  /**
   * Parses the native API JSON array to populate a list of {@link
   * SzWhyRecordsResultImpl} instances.
   *
   * @param list The {@link List} of {@link SzWhyRecordsResultImpl} instances to
   *             populate or <tt>null</tt> if a new list should be created.
   *
   * @param jsonArray The {@link JsonArray} of {@link JsonObject} instances to
   *                  be parsed as instances of {@link SzWhyRecordsResultImpl}.
   *
   * @return The {@link List} of {@link SzWhyRecordsResultImpl} instances that was
   *         populated.
   */
  public static List<SzWhyRecordsResultImpl> parseWhyRecordsResultList(
      List<SzWhyRecordsResultImpl>  list,
      JsonArray                 jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject: jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseWhyRecordsResult(jsonObject));
    }

    return list;
  }
}
