package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyEntityResultImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes why an entity resolved.
 */
@JsonDeserialize(using=SzWhyEntityResult.Factory.class)
public interface SzWhyEntityResult {
  /**
   * Gets the {@link SzWhyPerspective} identifying and describing the perspective
   * for this why result.
   *
   * @return The {@link SzWhyPerspective} identifying and describing the perspective
   *         for this why result.
   */
  @JsonInclude(NON_NULL)
  SzWhyPerspective getPerspective();

  /**
   * Sets the {@link SzWhyPerspective} identifying and describing the
   * perspective for this why result.
   *
   * @param perspective The {@link SzWhyPerspective} identifying and describing
   *                    the perspective for this why result.
   */
  void setPerspective(SzWhyPerspective perspective);

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
   * A {@link ModelProvider} for instances of {@link SzWhyEntityResult}.
   */
  interface Provider extends ModelProvider<SzWhyEntityResult> {
    /**
     * Creates a new instance of {@link SzWhyEntityResult}.
     *
     * @return The new instance of {@link SzWhyEntityResult}
     */
    SzWhyEntityResult create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyEntityResult} that produces instances of
   * {@link SzWhyEntityResultImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyEntityResult>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyEntityResult.class, SzWhyEntityResultImpl.class);
    }

    @Override
    public SzWhyEntityResult create() {
      return new SzWhyEntityResultImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzWhyEntityResult}.
   */
  class Factory extends ModelFactory<SzWhyEntityResult, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyEntityResult.class);
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
     * Creates a new instance of {@link SzWhyEntityResult}.
     * @return The new instance of {@link SzWhyEntityResult}.
     */
    public SzWhyEntityResult create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzWhyEntityResult}.
   *
   * @param jsonObject The {@link JsonObject} describing the why entity result
   *                   using the native API JSON format.
   *
   * @return The created instance of {@link SzWhyEntityResult}.
   */
  static SzWhyEntityResult parseWhyEntityResult(JsonObject jsonObject)
  {
    SzWhyPerspective perspective
        = SzWhyPerspective.parseWhyPerspective(jsonObject);

    JsonObject infoJson = JsonUtilities.getJsonObject(jsonObject, "MATCH_INFO");

    SzWhyMatchInfo matchInfo
        = SzWhyMatchInfo.parseMatchInfo(infoJson);

    SzWhyEntityResult result = SzWhyEntityResult.FACTORY.create();
    result.setPerspective(perspective);
    result.setMatchInfo(matchInfo);

    return result;
  }

  /**
   * Parses the native API JSON array to populate a list of {@link
   * SzWhyEntityResult} instances.
   *
   * @param list The {@link List} of {@link SzWhyEntityResult} instances to
   *             populate or <tt>null</tt> if a new list should be created.
   *
   * @param jsonArray The {@link JsonArray} of {@link JsonObject} instances to
   *                  be parsed as instances of {@link SzWhyEntityResult}.
   *
   * @return The {@link List} of {@link SzWhyEntityResult} instances that was
   *         populated.
   */
  static List<SzWhyEntityResult> parseWhyEntityResultList(
      List<SzWhyEntityResult> list,
      JsonArray               jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject: jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseWhyEntityResult(jsonObject));
    }

    return list;
  }
}
