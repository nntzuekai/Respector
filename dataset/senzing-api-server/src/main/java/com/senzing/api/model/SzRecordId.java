package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzRecordIdImpl;
import com.senzing.util.JsonUtilities;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Describes a record ID with a data source.
 */
@JsonDeserialize(using=SzRecordId.Factory.class)
public interface SzRecordId extends SzEntityIdentifier {
  /**
   * Gets the data source code for the record.
   *
   * @return The data source code for the record.
   */
  String getDataSourceCode();

  /**
   * Return the record ID identifying the record.
   *
   * @return The record ID identifying the record.
   */
  String getRecordId();

  /**
   * A {@link ModelProvider} for instances of {@link SzRecordId}.
   */
  interface Provider extends ModelProvider<SzRecordId> {
    /**
     * Constructs with the specified data source code and record ID.
     *
     * @param dataSourceCode The data source code.
     * @param recordId The record ID identifying the record.
     */
    SzRecordId create(String dataSourceCode, String recordId);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzRecordId} that produces instances of
   * {@link SzRecordIdImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzRecordId>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzRecordId.class, SzRecordIdImpl.class);
    }

    @Override
    public SzRecordId create(String dataSourceCode, String recordId) {
      return new SzRecordIdImpl(dataSourceCode, recordId);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzRecordId}.
   */
  class Factory extends ModelFactory<SzRecordId, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzRecordId.class);
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
     * Creates a new instance of {@link SzRecordId} with the specified
     * data source code and record ID.
     *
     * @param dataSourceCode The data source code.
     * @param recordId The record ID identifying the record.
     */
    public SzRecordId create(String dataSourceCode, String recordId) {
      return this.getProvider().create(dataSourceCode, recordId);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the specified JSON text for the record ID.  Alternatively, instead
   * of JSON text a delimited string can be specified with the first character
   * is the delimiter and the characters after the first character and up to
   * excluding the second occurrence of the delimiter are the data source and
   * the characters after the second occurrence of the delimiter are the record
   * ID.  For example the following are both valid for parsing:
   * <ul>
   *   <li><code>{"src":"PEOPLE", "id": "12345ABC"}</code></li>
   *   <li><code>:PEOPLE:12345ABC</code></li>
   * </ul>
   *
   * @param text The JSON text to parse.
   * @return The {@link SzRecordId} that was created.
   */
  static SzRecordId valueOf(String text) {
    RuntimeException failure = null;
    text = text.trim();
    int length = text.length();

    // first try to parse as JSON
    if (length > 2 && text.charAt(0) == '{' && text.charAt(length - 1) == '}') {
      try {
        JsonObject jsonObject = JsonUtilities.parseJsonObject(text);
        String source = jsonObject.getString("src");
        String id = jsonObject.getString("id");
        return SzRecordId.FACTORY.create(source, id);
      } catch (RuntimeException e) {
        failure = e;
      }
    }

    // try to parse as basic delimited string
    if (length > 2) {
      char sep = text.charAt(0);
      int index = text.indexOf(sep, 1);
      if (index < 0 || index == length - 1) {
        if (failure != null) throw failure;
        throw new IllegalArgumentException("Invalid record ID: " + text);
      }
      String prefix = text.substring(1, index);
      String suffix = text.substring(index + 1);
      return SzRecordId.FACTORY.create(prefix, suffix);

    } else {
      if (failure != null) throw failure;
      throw new IllegalArgumentException("Invalid record ID: " + text);
    }
  }

  /**
   * Parses the specified {@link JsonObject} as a record ID.  This expects
   * (and prefers) the <tt>"src"</tt> and <tt>"id"</tt> properties but will
   * alternatively accept the <tt>"dataSourceCode"</tt> and <tt>"recordId"</tt>
   * properties.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzRecordId} that was created.
   */
  static SzRecordId parse(JsonObject jsonObject) {
    String src = JsonUtilities.getString(jsonObject, "src");
    if (src == null) {
      src = JsonUtilities.getString(jsonObject, "dataSourceCode");
    }
    if (src == null) {
      src = JsonUtilities.getString(jsonObject, "DATA_SOURCE");
    }
    String id = JsonUtilities.getString(jsonObject, "id");
    if (id == null) {
      id = JsonUtilities.getString(jsonObject, "recordId");
    }
    if (id == null) {
      id = JsonUtilities.getString(jsonObject, "RECORD_ID");
    }
    if (src == null || id == null) {
      throw new IllegalArgumentException(
          "The specified JsonObject does not have the required fields.  src=[ "
          + src + " ], id=[ " + id + " ], jsonObject=[ " + jsonObject + " ]");
    }
    return SzRecordId.FACTORY.create(src, id);
  }

  /**
   * Parses the {@link SzRecordId} from a {@link JsonObject} describing JSON
   * for the Senzing native API format for record ID to create a new instance.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzRecordId} that was created.
   */
  static SzRecordId parseRecordId(JsonObject jsonObject) {
    String src  = JsonUtilities.getString(jsonObject, "DATA_SOURCE");
    String id   = JsonUtilities.getString(jsonObject, "RECORD_ID");

    if (src == null || id == null) {
      throw new IllegalArgumentException(
          "The specified JsonObject does not have the required fields.  src=[ "
          + src + " ], id=[ " + id + " ], jsonObject=[ " + jsonObject + " ]");
    }
    return SzRecordId.FACTORY.create(src, id);
  }

  /**
   * Parses and populates a {@link List} of {@link SzRecordId} instances from
   * a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzRecordId} instances that were
   *         populated.
   */
  static List<SzRecordId> parseRecordIdList(JsonArray jsonArray) {
    return parseRecordIdList(null, jsonArray);
  }

  /**
   * Parses and populates a {@link List} of {@link SzRecordId} instances from
   * a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzRecordId} instances that were
   *         populated.
   */
  static List<SzRecordId> parseRecordIdList(List<SzRecordId> list,
                                            JsonArray        jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseRecordId(jsonObject));
    }

    return list;
  }
}
