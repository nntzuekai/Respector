package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyEntitiesResultImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes why an entity resolved.
 */
@JsonDeserialize(using=SzWhyEntitiesResult.Factory.class)
public interface SzWhyEntitiesResult {
  /**
   * Gets the entity ID of the first entity.
   *
   * @return The entity ID of the first entity.
   */
  @JsonInclude(NON_NULL)
  Long getEntityId1();

  /**
   * Sets the entity ID of the first entity.
   *
   * @param entityId The entity ID of the first entity.
   */
  void setEntityId1(Long entityId);

  /**
   * Gets the entity ID of the second entity.
   *
   * @return The entity ID of the second entity.
   */
  @JsonInclude(NON_NULL)
  Long getEntityId2();

  /**
   * Sets the entity ID of the second entity.
   *
   * @param entityId The entity ID of the second entity.
   */
  void setEntityId2(Long entityId);

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
   * A {@link ModelProvider} for instances of {@link SzWhyEntitiesResult}.
   */
  interface Provider extends ModelProvider<SzWhyEntitiesResult> {
    /**
     * Creates a new instance of {@link SzWhyEntitiesResult}.
     *
     * @return The new instance of {@link SzWhyEntitiesResult}
     */
    SzWhyEntitiesResult create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyEntitiesResult} that produces instances of
   * {@link SzWhyEntitiesResultImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyEntitiesResult>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyEntitiesResult.class, SzWhyEntitiesResultImpl.class);
    }

    @Override
    public SzWhyEntitiesResult create() {
      return new SzWhyEntitiesResultImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzWhyEntitiesResult}.
   */
  class Factory extends ModelFactory<SzWhyEntitiesResult, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyEntitiesResult.class);
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
     * Creates a new instance of {@link SzWhyEntitiesResult}.
     * @return The new instance of {@link SzWhyEntitiesResult}.
     */
    public SzWhyEntitiesResult create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzWhyEntitiesResult}.
   *
   * @param jsonObj The {@link JsonObject} describing the why entity result
   *                using the native API JSON format.
   *
   * @return The created instance of {@link SzWhyEntitiesResult}.
   */
  static SzWhyEntitiesResult parseWhyEntitiesResult(JsonObject jsonObj)
  {
    Long entityId1 = JsonUtilities.getLong(jsonObj, "ENTITY_ID");
    Long entityId2 = JsonUtilities.getLong(jsonObj, "ENTITY_ID_2");

    JsonObject infoJson = JsonUtilities.getJsonObject(jsonObj, "MATCH_INFO");

    SzWhyMatchInfo matchInfo
        = SzWhyMatchInfo.parseMatchInfo(infoJson);

    SzWhyEntitiesResult result = SzWhyEntitiesResult.FACTORY.create();
    result.setEntityId1(entityId1);
    result.setEntityId2(entityId2);
    result.setMatchInfo(matchInfo);

    return result;
  }

  /**
   * Parses the native API JSON array to populate a list of {@link
   * SzWhyEntitiesResult} instances.
   *
   * @param list The {@link List} of {@link SzWhyEntitiesResult} instances to
   *             populate or <tt>null</tt> if a new list should be created.
   *
   * @param jsonArray The {@link JsonArray} of {@link JsonObject} instances to
   *                  be parsed as instances of {@link SzWhyEntitiesResult}.
   *
   * @return The {@link List} of {@link SzWhyEntitiesResult} instances that was
   *         populated.
   */
  static List<SzWhyEntitiesResult> parseWhyEntitiesResultList(
      List<SzWhyEntitiesResult> list,
      JsonArray                 jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject: jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseWhyEntitiesResult(jsonObject));
    }

    return list;
  }
}
