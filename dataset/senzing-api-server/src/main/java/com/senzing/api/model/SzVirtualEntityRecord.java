package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzVirtualEntityRecordImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.senzing.util.JsonUtilities.*;

/**
 * Describes a record ID with a data source and an internal ID.  The
 * internal ID is used to determine which records are effectively identical
 * for the purposes of entity resolution.
 */
@JsonDeserialize(using= SzVirtualEntityRecord.Factory.class)
public interface SzVirtualEntityRecord {
  /**
   * Gets the data source code for the record.
   *
   * @return The data source code for the record.
   */
  String getDataSource();

  /**
   * Gets the record ID identifying the record.
   *
   * @return The record ID identifying the record.
   */
  String getRecordId();

  /**
   * Gets the internal ID associated with the record.  Instances with the same
   * internal ID are effectively identical for the purposes of entity 
   * resolution.
   * 
   * @return The internal ID associated with the record.
   */
  Long getInternalId();
  
  /**
   * A {@link ModelProvider} for instances of {@link SzVirtualEntityRecord}.
   */
  interface Provider extends ModelProvider<SzVirtualEntityRecord> {
    /**
     * Creates an instance with the specified data source code, record ID and
     * internal ID.  Instances with the same internal ID are considered to be
     * effectively identical for the purpose of entity resolution.
     *
     * @param dataSource The data source code.
     * @param recordId The record ID identifying the record.
     * @param internalId The internal ID associated with the record.
     */
    SzVirtualEntityRecord create(String dataSource,
                                 String recordId,
                                 Long   internalId);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzVirtualEntityRecord} that produces instances of
   * {@link SzVirtualEntityRecordImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzVirtualEntityRecord>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzVirtualEntityRecord.class, SzVirtualEntityRecordImpl.class);
    }

    @Override
    public SzVirtualEntityRecord create(String  dataSource,
                                        String  recordId,
                                        Long    internalId)
    {
      return new SzVirtualEntityRecordImpl(dataSource, recordId, internalId);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzVirtualEntityRecord}.
   */
  class Factory extends ModelFactory<SzVirtualEntityRecord, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzVirtualEntityRecord.class);
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
     * Creates an instance with the specified data source code, record ID and
     * internal ID.  Instances with the same internal ID are considered to be
     * effectively identical for the purpose of entity resolution.
     *
     * @param dataSource The data source code.
     * @param recordId The record ID identifying the record.
     * @param internalId The internal ID associated with the record.
     */
    public SzVirtualEntityRecord create(String  dataSource,
                                        String  recordId,
                                        Long    internalId)
    {
      return this.getProvider().create(dataSource, recordId, internalId);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses one or more {@link SzVirtualEntityRecord} from a {@link JsonObject}
   * describing JSON for the Senzing native API format for the internal ID and
   * its associated record.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzVirtualEntityRecord} that was created.
   */
  static List<SzVirtualEntityRecord> parseVirtualEntityRecords(
      JsonObject  jsonObject)
  {
    Long      internalId  = getLong(jsonObject, "INTERNAL_ID");
    JsonArray jsonArray   = getJsonArray(jsonObject, "RECORDS");

    // loop over the records
    List<SzVirtualEntityRecord> records = new ArrayList<>(jsonArray.size());
    for (JsonObject recordObj : jsonArray.getValuesAs(JsonObject.class)) {
      // get the data source code and record ID
      String dataSource = getString(recordObj, "DATA_SOURCE");
      String recordId = getString(recordObj, "RECORD_ID");

      // construct the record
      SzVirtualEntityRecord record = SzVirtualEntityRecord.FACTORY.create(
          dataSource, recordId, internalId);

      // add to the list
      records.add(record);
    }

    // return the records list
    return records;
  }

  /**
   * Parses and populates a {@link List} of {@link SzVirtualEntityRecord} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzVirtualEntityRecord} instances that were
   *         populated.
   */
  static List<SzVirtualEntityRecord> parseVirtualEntityRecordList(
      JsonArray jsonArray)
  {
    return parseVirtualEntityRecordList(null, jsonArray);
  }

  /**
   * Parses and populates a {@link List} of {@link SzVirtualEntityRecord} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzVirtualEntityRecord} instances that were
   *         populated.
   */
  static List<SzVirtualEntityRecord> parseVirtualEntityRecordList(
      List<SzVirtualEntityRecord> list,
      JsonArray                   jsonArray)
  {
    // construct the list
    if (list == null) {
      // count the number of records
      int totalCount = 0;
      for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
        JsonArray recordsArr = getJsonArray(jsonObject, "RECORDS");
        totalCount += recordsArr.size();
      }

      // construct an array list
      list = new ArrayList<>(jsonArray.size());
    }

    // parse the records for each object
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.addAll(parseVirtualEntityRecords(jsonObject));
    }

    // return the list
    return list;
  }
}
