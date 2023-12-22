package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzVirtualEntityImpl;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.senzing.util.JsonUtilities.*;
import static com.senzing.api.model.SzVirtualEntityRecord.*;

/**
 * Describes a virtual entity.
 */
@JsonDeserialize(using= SzVirtualEntity.Factory.class)
public interface SzVirtualEntity {
  /**
   * Gets the virtual entity ID for the virtual entity.
   *
   * @return The virtual entity ID for the virtual entity.
   */
  String getVirtualEntityId();

  /**
   * Sets the virtual entity ID for this virtual entity.
   *
   * @param virtualEntityId The virtual entity ID for this virtual entity.
   */
  void setVirtualEntityId(String virtualEntityId);

  /**
   * Checks if this virtual entity is a singleton.  <b>NOTE:</b> A virtual
   * entity with multiple {@linkplain #getRecords() records} may be a singleton
   * if the records all have the same {@linkplain
   * SzVirtualEntityRecord#getInternalId() internal ID}.
   *
   * @return <code>true</code> if the virtual entity is a singleton, otherwise
   *         <code>false</code>.
   */
  boolean isSingleton();

  /**
   * Gets the {@link Set} of {@link SzVirtualEntityRecord} instances describing
   * the member records for this virtual entity.  Those {@link
   * SzVirtualEntityRecord} instances with the same {@linkplain
   * SzVirtualEntityRecord#getInternalId() internal ID} are effectively
   * identical for the purposes of entity resolution.
   * 
   * @return The {@link Set} of {@link SzVirtualEntityRecord} instances
   *         describing the member records for this virtual entity.
   */
  Set<SzVirtualEntityRecord> getRecords();

  /**
   * Adds the specified {@link SzVirtualEntityRecord} to the {@link Set} of
   * member records for this virtual entity.
   *
   * @param record The {@link SzVirtualEntityRecord} to add to the {@link Set}
   *               of member records.
   */
  void addRecord(SzVirtualEntityRecord record);

  /**
   * Sets the {@link Set} of {@link SzVirtualEntityRecord} instances describing
   * the member records for this virtual entity.
   *
   * @param records The {@link Collection} of {@link SzVirtualEntityRecord}
   *                instances describing the member records for this virtual
   *                entity.
   */
  void setRecords(Collection<SzVirtualEntityRecord> records);

  /**
   * A {@link ModelProvider} for instances of {@link SzVirtualEntity}.
   */
  interface Provider extends ModelProvider<SzVirtualEntity> {
    /**
     * Creates an uninitialized instance with no arguments.
     *
     * @return The {@link SzVirtualEntity} that was created.
     */
    SzVirtualEntity create();

    /**
     * Creates an instance with the specified virtual entity ID.
     *
     * @param virtualEntityId The virtual entity ID for the created instance.
     *
     * @return The {@link SzVirtualEntity} that was created.
     */
    SzVirtualEntity create(String virtualEntityId);
  }

  /**
   * Implements a default {@link Provider} for {@link SzVirtualEntity} that
   * produces instances of {@link SzVirtualEntityImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzVirtualEntity>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzVirtualEntity.class, SzVirtualEntityImpl.class);
    }

    @Override
    public SzVirtualEntity create() {
      return new SzVirtualEntityImpl();
    }

    @Override
    public SzVirtualEntity create(String virtualEntityId) {
      return new SzVirtualEntityImpl(virtualEntityId);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzVirtualEntity}.
   */
  class Factory extends ModelFactory<SzVirtualEntity, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzVirtualEntity.class);
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
     * Creates an uninitialized instance with no arguments.
     *
     * @return The {@link SzVirtualEntity} that was created.
     */
    public SzVirtualEntity create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance with the specified virtual entity ID.
     *
     * @param virtualEntityId The virtual entity ID for the created instance.
     *
     * @return The {@link SzVirtualEntity} that was created.
     */
    public SzVirtualEntity create(String virtualEntityId) {
      return this.getProvider().create(virtualEntityId);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the Senzing native API JSON described by the specified {@link
   * JsonObject} as an instance of {@link SzVirtualEntity}.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzVirtualEntity} that was created.
   */
  static SzVirtualEntity parseVirtualEntity(JsonObject jsonObject)
  {
    String    virtualEntityId = getString(jsonObject, "VIRTUAL_ENTITY_ID");
    JsonArray jsonArray       = getJsonArray(jsonObject, "MEMBER_RECORDS");

    // loop over the records
    List<SzVirtualEntityRecord> records
        = parseVirtualEntityRecordList(jsonArray);

    // construct the record
    SzVirtualEntity entity = SzVirtualEntity.FACTORY.create(virtualEntityId);

    // set the records on the entity
    entity.setRecords(records);

    // return the records list
    return entity;
  }

  /**
   * Parses and populates a {@link List} of {@link SzVirtualEntity} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzVirtualEntity} instances that were
   *         populated.
   */
  static List<SzVirtualEntity> parseVirtualEntityList(
      JsonArray jsonArray)
  {
    return parseVirtualEntityList(null, jsonArray);
  }

  /**
   * Parses and populates a {@link List} of {@link SzVirtualEntity} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzVirtualEntity} instances that were
   *         populated.
   */
  static List<SzVirtualEntity> parseVirtualEntityList(
      List<SzVirtualEntity> list,
      JsonArray             jsonArray)
  {
    // construct the list
    if (list == null) {
      // construct an array list
      list = new ArrayList<>(jsonArray.size());
    }

    // parse the records for each object
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseVirtualEntity(jsonObject));
    }

    // return the list
    return list;
  }
}
