package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzFlaggedRecordImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes a record for a flagged entity.
 */
@JsonDeserialize(using=SzFlaggedRecord.Factory.class)
public interface SzFlaggedRecord {
  /**
   * Gets the data source for the flagged record.
   *
   * @return The data source for the flagged record.
   */
  @JsonInclude(NON_NULL)
  String getDataSource();

  /**
   * Sets the data source for the flagged record.
   *
   * @param dataSource The data source for the flagged record.
   */
  void setDataSource(String dataSource);

  /**
   * Gets the record ID for the flagged record.
   *
   * @return The record ID for the flagged record.
   */
  @JsonInclude(NON_NULL)
  String getRecordId();

  /**
   * Sets the record ID for the flagged record.
   *
   * @param recordId The record ID for the flagged record.
   */
  void setRecordId(String recordId);

  /**
   * Returns the {@link Set} of {@link String} flags that were flagged for
   * this record.
   *
   * @return The {@link Set} of {@link String} flags that were flagged for
   * this record, or <tt>null</tt> if none.
   */
  @JsonInclude(NON_EMPTY)
  Set<String> getFlags();

  /**
   * Adds the specified {@link String} flag as one flagged for this record.
   *
   * @param flag The flag to add to this instance.
   */
  void addFlag(String flag);

  /**
   * Sets the {@link String} flags that were flagged for this record.
   *
   * @param flags The {@link Collection} of {@link String} flags that were
   *              flagged for this record.
   */
  void setFlags(Collection<String> flags);

  /**
   * A {@link ModelProvider} for instances of {@link SzFlaggedRecord}.
   */
  interface Provider extends ModelProvider<SzFlaggedRecord> {
    /**
     * Creates a new instance of {@link SzFlaggedRecord}.
     *
     * @return The new instance of {@link SzFlaggedRecord}
     */
    SzFlaggedRecord create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzFlaggedRecord} that produces instances of
   * {@link SzFlaggedRecordImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzFlaggedRecord>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzFlaggedRecord.class, SzFlaggedRecordImpl.class);
    }

    @Override
    public SzFlaggedRecord create() {
      return new SzFlaggedRecordImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzFlaggedRecord}.
   */
  class Factory extends ModelFactory<SzFlaggedRecord, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzFlaggedRecord.class);
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
     * Creates a new instance of {@link SzFlaggedRecord}.
     * @return The new instance of {@link SzFlaggedRecord}.
     */
    public SzFlaggedRecord create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a list of flagged records from a {@link JsonArray} describing a
   * JSON array in the Senzing native API format for flagged record info and
   * populates the specified {@link List} or creates a new {@link List}.
   *
   * @param list      The {@link List} of {@link SzFlaggedRecord} instances to
   *                  populate, or <tt>null</tt> if a new {@link List}
   *                  should be created.
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   * @return The populated (or created) {@link List} of {@link
   * SzFlaggedRecord} instances.
   */
  static List<SzFlaggedRecord> parseFlaggedRecordList(
      List<SzFlaggedRecord> list,
      JsonArray jsonArray) {
    if (list == null) {
      list = new ArrayList<>(jsonArray == null ? 0 : jsonArray.size());
    }

    if (jsonArray == null) return list;

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseFlaggedRecord(null, jsonObject));
    }
    return list;
  }

  /**
   * Parses the flagged record from a {@link JsonObject} describing JSON
   * for the Senzing native API format for flagged record info and populates
   * the specified {@link SzFlaggedRecord} or creates a new instance.
   *
   * @param record     The {@link SzFlaggedRecord} instance to populate, or
   *                   <tt>null</tt> if a new instance should be created.
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   * @return The populated (or created) {@link SzFlaggedRecord}.
   */
  static SzFlaggedRecord parseFlaggedRecord(
      SzFlaggedRecord record,
      JsonObject jsonObject) {
    if (record == null) record = SzFlaggedRecord.FACTORY.create();

    record.setDataSource(JsonUtilities.getString(jsonObject, "DATA_SOURCE"));
    record.setRecordId(JsonUtilities.getString(jsonObject, "RECORD_ID"));

    JsonArray jsonArray = JsonUtilities.getJsonArray(jsonObject, "FLAGS");
    if (jsonArray != null) {
      for (JsonString flag : jsonArray.getValuesAs(JsonString.class)) {
        record.addFlag(flag.getString());
      }
    }
    return record;
  }
}
