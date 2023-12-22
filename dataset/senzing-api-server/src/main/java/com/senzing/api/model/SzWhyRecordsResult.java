package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyRecordsResultImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes why an entity resolved.
 */
@JsonDeserialize(using=SzWhyRecordsResult.Factory.class)
public interface SzWhyRecordsResult {
  /**
   * Gets the {@link SzWhyPerspective} identifying and describing the
   * perspective for this why result from the first record.
   *
   * @return The {@link SzWhyPerspective} identifying and describing the
   *         perspective for this why result from the first record.
   */
  @JsonInclude(NON_NULL)
  SzWhyPerspective getPerspective1();

  /**
   * Sets the {@link SzWhyPerspective} identifying and describing the
   * perspective for this why result from the first record.
   *
   * @param perspective The {@link SzWhyPerspective} identifying and describing
   *                    the perspective for this why result from the first
   *                    record.
   */
  void setPerspective1(SzWhyPerspective perspective);

  /**
   * Gets the {@link SzWhyPerspective} identifying and describing the
   * perspective for this why result from the first record.
   *
   * @return The {@link SzWhyPerspective} identifying and describing the
   *         perspective for this why result from the first record.
   */
  @JsonInclude(NON_NULL)
  SzWhyPerspective getPerspective2();

  /**
   * Sets the {@link SzWhyPerspective} identifying and describing the
   * perspective for this why result from the first record.
   *
   * @param perspective The {@link SzWhyPerspective} identifying and describing
   *                    the perspective for this why result from the first
   *                    record.
   */
  void setPerspective2(SzWhyPerspective perspective);

  /**
   * Gets the {@link SzWhyMatchInfo} providing the details of the result.
   *
   * @return The {@link SzWhyMatchInfo} providing the details of the result.
   */
  @JsonInclude(NON_NULL)
  SzWhyMatchInfo getMatchInfo();

  /**
   * Sets the {@link SzWhyMatchInfo} providing the details of the result.
   *
   * @param matchInfo The {@link SzWhyMatchInfo} providing the details of the
   *                  result.
   */
  void setMatchInfo(SzWhyMatchInfo matchInfo);

  /**
   * A {@link ModelProvider} for instances of {@link SzWhyRecordsResult}.
   */
  interface Provider extends ModelProvider<SzWhyRecordsResult> {
    /**
     * Creates a new instance of {@link SzWhyRecordsResult}.
     *
     * @return The new instance of {@link SzWhyRecordsResult}
     */
    SzWhyRecordsResult create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyRecordsResult} that produces instances of
   * {@link SzWhyRecordsResultImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyRecordsResult>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyRecordsResult.class, SzWhyRecordsResultImpl.class);
    }

    @Override
    public SzWhyRecordsResult create() {
      return new SzWhyRecordsResultImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzWhyRecordsResult}.
   */
  class Factory extends ModelFactory<SzWhyRecordsResult, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyRecordsResult.class);
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
     * Creates a new instance of {@link SzWhyRecordsResult}.
     * @return The new instance of {@link SzWhyRecordsResult}.
     */
    public SzWhyRecordsResult create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzWhyRecordsResult}.
   *
   * @param jsonObject The {@link JsonObject} describing the why entity result
   *                   using the native API JSON format.
   *
   * @return The created instance of {@link SzWhyRecordsResult}.
   */
  static SzWhyRecordsResult parseWhyRecordsResult(JsonObject jsonObject)
  {
    SzWhyPerspective perspective1
        = SzWhyPerspective.parseWhyPerspective(jsonObject);

    SzWhyPerspective perspective2
        = SzWhyPerspective.parseWhyPerspective(jsonObject, "_2");

    JsonObject infoJson = JsonUtilities.getJsonObject(jsonObject, "MATCH_INFO");

    SzWhyMatchInfo matchInfo
        = SzWhyMatchInfo.parseMatchInfo(infoJson);

    SzWhyRecordsResult result = SzWhyRecordsResult.FACTORY.create();
    result.setPerspective1(perspective1);
    result.setPerspective2(perspective2);
    result.setMatchInfo(matchInfo);

    return result;
  }

  /**
   * Parses the native API JSON array to populate a list of {@link
   * SzWhyRecordsResult} instances.
   *
   * @param list The {@link List} of {@link SzWhyRecordsResult} instances to
   *             populate or <tt>null</tt> if a new list should be created.
   *
   * @param jsonArray The {@link JsonArray} of {@link JsonObject} instances to
   *                  be parsed as instances of {@link SzWhyRecordsResult}.
   *
   * @return The {@link List} of {@link SzWhyRecordsResult} instances that was
   *         populated.
   */
  static List<SzWhyRecordsResult> parseWhyRecordsResultList(
      List<SzWhyRecordsResult>  list,
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
