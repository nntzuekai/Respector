package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzResolutionInfoImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes the information associated with resolution of a record.
 */
@JsonDeserialize(using=SzResolutionInfo.Factory.class)
public interface SzResolutionInfo {
  /**
   * Gets the data source for the focal record.
   *
   * @return The data source for the focal record.
   */
  @JsonInclude(NON_NULL)
  String getDataSource();

  /**
   * Sets the data source for the focal record.
   *
   * @param dataSource The data source for the focal record.
   */
  void setDataSource(String dataSource);

  /**
   * Gets the record ID for the focal record.
   *
   * @return The record ID for the focal record.
   */
  @JsonInclude(NON_NULL)
  String getRecordId();

  /**
   * Sets the record ID for the focal record.
   *
   * @param recordId The record ID for the focal record.
   */
  void setRecordId(String recordId);

  /**
   * Get the <b>unmodifiable</b> {@link Set} of entity ID's for the affected
   * entities.
   *
   * @return The <b>unmodifiable</b> {@link Set} of entity ID's for the
   *         affected entities.
   */
  @JsonInclude(NON_EMPTY)
  Set<Long> getAffectedEntities();

  /**
   * Adds the specified entity ID to the {@Link Set} of affected entities.
   * If the specified value is <tt>null</tt> then this method does nothing.
   *
   * @param entityId The entity ID to add.
   */
  void addAffectedEntity(Long entityId);

  /**
   * Sets the {@link Set} of affected entity IDs to those in the specified
   * {@link Collection}.  If the specified parameter is <tt>null</tt> then no
   * entity IDs are added and any <tt>null</tt> values in the {@link Collection}
   * are ignored.
   *
   * @param affectedEntities
   */
  void setAffectedEntities(Collection<Long> affectedEntities);

  /**
   * Gets the <b>unmodifiable</b> {@link List} of {@link SzFlaggedEntity}
   * instances.
   *
   * @return The <b>unmodifiable</b> {@link List} of {@link SzFlaggedEntity}
   *         instances.
   */
  @JsonInclude(NON_EMPTY)
  List<SzFlaggedEntity> getFlaggedEntities();

  /**
   * Adds the specified {@link SzFlaggedEntity} to the {@link List} of flagged
   * entities for this instance.  This method does nothing if the specified
   * parameter is <tt>null</tt>.
   *
   * @param flaggedEntity The {@link SzFlaggedEntity} to add to the {@link List}
   */
  void addFlaggedEntity(SzFlaggedEntity flaggedEntity);

  /**
   * Sets the {@link List} of flagged entities to those in the specified
   * {@link Collection} of {@link SzFlaggedEntity} instances.  If the specified
   * parameter is <tt>null</tt> then {@link List} of flagged entities is
   * cleared.  Any null instances in the specified {@link List} are ignored.
   *
   * @param flaggedEntities The {@link Collection} of {@link SzFlaggedEntity}
   *                        instances to add to the {@link List} of flagged
   *                        entities.
   */
  void setFlaggedEntities(Collection<SzFlaggedEntity> flaggedEntities);

  /**
   * A {@link ModelProvider} for instances of {@link SzResolutionInfo}.
   */
  interface Provider extends ModelProvider<SzResolutionInfo> {
    /**
     * Creates a new instance of {@link SzResolutionInfo}.
     *
     * @return The new instance of {@link SzResolutionInfo}
     */
    SzResolutionInfo create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzResolutionInfo} that produces instances of
   * {@link SzResolutionInfoImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzResolutionInfo>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzResolutionInfo.class, SzResolutionInfoImpl.class);
    }

    @Override
    public SzResolutionInfo create() {
      return new SzResolutionInfoImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzResolutionInfo}.
   */
  class Factory extends ModelFactory<SzResolutionInfo, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzResolutionInfo.class);
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
     * Creates a new instance of {@link SzResolutionInfo}.
     * @return The new instance of {@link SzResolutionInfo}.
     */
    public SzResolutionInfo create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the resolution info from a {@link JsonObject} describing JSON
   * for the Senzing native API format for resolution info and populates
   * the specified {@link SzResolutionInfo} or creates a new instance.
   *
   * @param info The {@link SzResolutionInfo} instance to populate, or
   *             <tt>null</tt> if a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @return The populated (or created) {@link SzResolutionInfo}.
   */
  static SzResolutionInfo parseResolutionInfo(
      SzResolutionInfo  info,
      JsonObject        jsonObject)
  {
    if (info == null) info = SzResolutionInfo.FACTORY.create();

    info.setDataSource(JsonUtilities.getString(jsonObject, "DATA_SOURCE"));
    info.setRecordId(JsonUtilities.getString(jsonObject, "RECORD_ID"));

    JsonArray jsonArray = JsonUtilities.getJsonArray(jsonObject,
                                                 "AFFECTED_ENTITIES");

    if (jsonArray != null) {
      for (JsonObject jsonObj : jsonArray.getValuesAs(JsonObject.class)) {
        info.addAffectedEntity(JsonUtilities.getLong(jsonObj, "ENTITY_ID"));
      }
    }

    JsonObject interestObj = JsonUtilities.getJsonObject(
        jsonObject, "INTERESTING_ENTITIES");

    jsonArray = JsonUtilities.getJsonArray(interestObj, "ENTITIES");
    if (jsonArray != null) {
      List<SzFlaggedEntity> flaggedEntities
          = SzFlaggedEntity.parseFlaggedEntityList(null, jsonArray);
      info.setFlaggedEntities(flaggedEntities);
    }

    return info;
  }
}
