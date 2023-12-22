package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzFocusRecordIdImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a record ID with a data source.
 */
@JsonDeserialize(using=SzFocusRecordId.Factory.class)
public interface SzFocusRecordId {
  /**
   * Gets the data source code for the record.
   *
   * @return The data source code for the record.
   */
  String getDataSource();

  /**
   * Return the record ID identifying the record.
   *
   * @return The record ID identifying the record.
   */
  String getRecordId();

  /**
   * A {@link ModelProvider} for instances of {@link SzFocusRecordId}.
   */
  interface Provider extends ModelProvider<SzFocusRecordId> {
    /**
     * Constructs with the specified data source code and record ID.
     *
     * @param dataSource The data source code.
     * @param recordId The record ID identifying the record.
     */
    SzFocusRecordId create(String dataSource, String recordId);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzFocusRecordId} that produces instances of
   * {@link SzFocusRecordIdImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzFocusRecordId>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzFocusRecordId.class, SzFocusRecordIdImpl.class);
    }

    @Override
    public SzFocusRecordId create(String dataSource, String recordId) {
      return new SzFocusRecordIdImpl(dataSource, recordId);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzFocusRecordId}.
   */
  class Factory extends ModelFactory<SzFocusRecordId, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzFocusRecordId.class);
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
     * Constructs with the specified data source code and record ID.
     *
     * @param dataSource The data source code.
     * @param recordId The record ID identifying the record.
     */
    public SzFocusRecordId create(String dataSource, String recordId) {
      return this.getProvider().create(dataSource, recordId);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the {@link SzFocusRecordId} from a {@link JsonObject} describing
   * JSON for the Senzing native API format for record ID to create a new
   * instance.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzFocusRecordId} that was created.
   */
  static SzFocusRecordId parseFocusRecordId(JsonObject jsonObject) {
    String src  = JsonUtilities.getString(jsonObject, "DATA_SOURCE");
    String id   = JsonUtilities.getString(jsonObject, "RECORD_ID");
    return SzFocusRecordId.FACTORY.create(src, id);
  }

  /**
   * Parses and populates a {@link List} of {@link SzFocusRecordId} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzFocusRecordId} instances that were
   *         populated.
   */
  static List<SzFocusRecordId> parseFocusRecordIdList(JsonArray jsonArray)
  {
    return parseFocusRecordIdList(null, jsonArray);
  }

  /**
   * Parses and populates a {@link List} of {@link SzFocusRecordId} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzFocusRecordId} instances that were
   *         populated.
   */
  static List<SzFocusRecordId> parseFocusRecordIdList(
      List<SzFocusRecordId> list,
      JsonArray             jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseFocusRecordId(jsonObject));
    }

    return list;
  }
}
