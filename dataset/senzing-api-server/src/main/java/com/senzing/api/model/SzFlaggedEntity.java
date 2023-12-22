package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzFlaggedEntityImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Describes an entity that was flagged during entity resolution.
 */
@JsonDeserialize(using=SzFlaggedEntity.Factory.class)
public interface SzFlaggedEntity {
  /**
   * Gets the entity ID for the flagged entity.
   *
   * @return The entity ID for the flagged entity.
   */
  @JsonInclude(NON_NULL)
  Long getEntityId();

  /**
   * Sets the entity ID for the flagged entity.
   *
   * @param entityId The entity ID for the flagged entity.
   */
  void setEntityId(Long entityId);

  /**
   * Gets the number of degrees of separation for the flagged entity.
   *
   * @return The number of degrees of separation for the flagged entity.
   */
  @JsonInclude(NON_NULL)
  Integer getDegrees();

  /**
   * Sets the number of degrees of separation for the flagged entity.
   *
   * @param degrees The number of degrees of separation for the flagged entity.
   */
  void setDegrees(Integer degrees);

  /**
   * Gets the {@link Set} of {@link String} flags that were triggered for this
   * entity.
   *
   * @return The {@link Set} of {@link String} flags that were triggered for
   *         this entity.
   */
  @JsonInclude(NON_EMPTY)
  Set<String> getFlags();

  /**
   * Adds the specified {@link String} flag to the {@link Set} of flags
   * triggered by this flagged entity.
   *
   * @param flag The {@link String} flag to add to the {@link Set} of flags.
   */
  void addFlag(String flag);

  /**
   * Sets the {@link Set} of {@link String} flags that were triggered for this
   * entity.
   *
   * @return The {@link Set} of {@link String} flags that were triggered for
   *         this entity, or <tt>null</tt> if none.
   */
  void setFlags(Set<String> flags);

  /**
   * Gets the {@link List} of {@link SzFlaggedRecord} instances describing the
   * sample of records that were flagged for this entity.
   *
   * @return The {@link List} of {@link SzFlaggedRecord} instances describing
   *         the sample of records that were flagged for this entity.
   */
  @JsonInclude(NON_EMPTY)
  List<SzFlaggedRecord> getSampleRecords();

  /**
   * Adds the specified {@link SzFlaggedRecord} to the {@link List} of sample
   * records for this instance.  If the specified record is <tt>null</tt> then
   * it is not added.
   *
   * @param sampleRecord The {@link SzFlaggedRecord} to add as a sample record.
   */
  void addSampleRecord(SzFlaggedRecord sampleRecord);

  /**
   * Sets the {@link List} of {@link SzFlaggedRecord} instances using those
   * contained in the specified {@link Collection}.
   *
   * @param sampleRecords The sample records to set, or <tt>null</tt> if none.
   */
  void setSampleRecords(Collection<SzFlaggedRecord> sampleRecords);

  /**
   * A {@link ModelProvider} for instances of {@link SzFlaggedEntity}.
   */
  interface Provider extends ModelProvider<SzFlaggedEntity> {
    /**
     * Creates a new instance of {@link SzFlaggedEntity}.
     *
     * @return The new instance of {@link SzFlaggedEntity}
     */
    SzFlaggedEntity create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzFlaggedEntity} that produces instances of
   * {@link SzFlaggedEntityImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzFlaggedEntity>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzFlaggedEntity.class, SzFlaggedEntityImpl.class);
    }

    @Override
    public SzFlaggedEntity create() {
      return new SzFlaggedEntityImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzFlaggedEntity}.
   */
  class Factory extends ModelFactory<SzFlaggedEntity, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzFlaggedEntity.class);
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
     * Creates a new instance of {@link SzFlaggedEntity}.
     * @return The new instance of {@link SzFlaggedEntity}.
     */
    public SzFlaggedEntity create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a list of flagged entities from a {@link JsonArray} describing a
   * JSON array in the Senzing native API format for flagged entity info and
   * populates the specified {@link List} or creates a new {@link List}.
   *
   * @param list      The {@link List} of {@link SzFlaggedEntity} instances to
   *                  populate, or <tt>null</tt> if a new {@link List}
   *                  should be created.
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   * @return The populated (or created) {@link List} of {@link
   *         SzFlaggedEntity} instances.
   */
  static List<SzFlaggedEntity> parseFlaggedEntityList(
      List<SzFlaggedEntity> list,
      JsonArray             jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray == null ? 0 : jsonArray.size());
    }

    if (jsonArray == null) return list;

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseFlaggedEntity(null, jsonObject));
    }
    return list;
  }

  /**
   * Parses the flagged entity from a {@link JsonObject} describing JSON
   * for the Senzing native API format for flagged entity info and populates
   * the specified {@link SzFlaggedEntity} or creates a new instance.
   *
   * @param entity     The {@link SzFlaggedEntity} instance to populate, or
   *                   <tt>null</tt> if a new instance should be created.
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   * @return The populated (or created) {@link SzFlaggedEntity}.
   */
  static SzFlaggedEntity parseFlaggedEntity(
      SzFlaggedEntity entity,
      JsonObject      jsonObject)
  {
    if (entity == null) entity = SzFlaggedEntity.FACTORY.create();

    entity.setEntityId(JsonUtilities.getLong(jsonObject, "ENTITY_ID"));
    entity.setDegrees(JsonUtilities.getInteger(jsonObject, "DEGREES"));

    JsonArray jsonArray = JsonUtilities.getJsonArray(jsonObject, "FLAGS");
    if (jsonArray != null) {
      for (JsonString flag : jsonArray.getValuesAs(JsonString.class)) {
        entity.addFlag(flag.getString());
      }
    }

    jsonArray = JsonUtilities.getJsonArray(jsonObject, "SAMPLE_RECORDS");
    if (jsonArray != null) {
      List<SzFlaggedRecord> sampleRecords
          = SzFlaggedRecord.parseFlaggedRecordList(
              null, jsonArray);
      entity.setSampleRecords(sampleRecords);
    }

    return entity;
  }

}
