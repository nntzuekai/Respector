package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityRecordImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import static com.senzing.util.JsonUtilities.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes an entity record.
 */
@JsonDeserialize(using=SzEntityRecord.Factory.class)
public interface SzEntityRecord {
  /**
   * Gets the data source code for the record.
   *
   * @return The data source code for the record.
   */
  String getDataSource();

  /**
   * Sets the data source code for the record.
   *
   * @param dataSource The data source code for the record.
   */
  void setDataSource(String dataSource);

  /**
   * Gets the record ID for the record.
   *
   * @return The record ID for the record.
   */
  String getRecordId();

  /**
   * Sets the record ID for the record.
   *
   * @param recordId The record ID for the record.
   */
  void setRecordId(String recordId);

  /**
   * Gets the {@link List} of {@link SzFeatureReference} instances for the
   * entity features contributed by this record and the associated usage type
   * associated with that feature.
   *
   * @return The {@link List} of {@link SzFeatureReference} instances
   *         identifying the entity features contributed by this record along
   *         with the associated usage type for each feature.
   */
  @JsonInclude(NON_EMPTY)
  List<SzFeatureReference> getFeatureReferences();

  /**
   * Sets the {@link SzFeatureReference} instances for this record to those
   * in the specified {@link Collection}.
   *
   * @param featureRefs The {@link Collection} of {@link SzFeatureReference}
   *                    instances to set for this entity record.
   */
  void setFeatureReferences(Collection<SzFeatureReference> featureRefs);

  /**
   * Adds the specified {@link SzFeatureReference} to the feature reference
   * instances for this record.
   *
   * @param featureRef The {@link SzFeatureReference} to add to the feature
   *                   references for this record.
   */
  void addFeatureReference(SzFeatureReference featureRef);

  /**
   * Gets the last-seen timestamp for the entity.
   *
   * @return The last-seen timestamp for the entity.
   */
  @JsonInclude(NON_NULL)
  @JsonFormat(shape   = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
      locale  = "en_GB")
  Date getLastSeenTimestamp();

  /**
   * Sets the last-seen timestamp for the entity.
   *
   * @param timestamp The last-seen timestamp for the entity.
   */
  void setLastSeenTimestamp(Date timestamp);

  /**
   * Returns the list of address data strings for the record.
   *
   * @return The list of address data strings for the record.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getAddressData();

  /**
   * Sets the address data list for the record.
   *
   * @param addressData The list of address data strings.
   */
  void setAddressData(List<String> addressData);

  /**
   * Adds to the address data list for the record.
   *
   * @param addressData The address data string to add to the address data list.
   */
  void addAddressData(String addressData);

  /**
   * Returns the list of characteristic data strings for the record.
   *
   * @return The list of characteristic data strings for the record.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getCharacteristicData();

  /**
   * Sets the characteristic data list for the record.
   *
   * @param characteristicData The list of characteristic data strings.
   */
  void setCharacteristicData(List<String> characteristicData);

  /**
   * Adds to the characteristic data list for the record.
   *
   * @param characteristicData The characteristic data string to add to the address
   *                      data list.
   */
  void addCharacteristicData(String characteristicData);

  /**
   * Returns the list of identifier data strings for the record.
   *
   * @return The list of identifier data strings for the record.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getIdentifierData();

  /**
   * Sets the identifier data list for the record.
   *
   * @param identifierData The list of identifier data strings.
   */
  void setIdentifierData(List<String> identifierData);

  /**
   * Adds to the identifier data list for the record.
   *
   * @param identifierData The identifier data string to add to the identifier
   *                       data list.
   */
  void addIdentifierData(String identifierData);

  /**
   * Returns the list of name data strings for the record.
   *
   * @return The list of name data strings for the record.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getNameData();

  /**
   * Sets the name data list for the record.
   *
   * @param nameData The list of name data strings.
   */
  void setNameData(List<String> nameData);

  /**
   * Adds to the name data list for the record.
   *
   * @param nameData The name data string to add to the name data list.
   */
  void addNameData(String nameData);

  /**
   * Returns the list of phone data strings for the record.
   *
   * @return The list of phone data strings for the record.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getPhoneData();

  /**
   * Sets the phone data list for the record.
   *
   * @param phoneData The list of name data strings.
   */
  void setPhoneData(List<String> phoneData);

  /**
   * Adds to the phone data list for the record.
   *
   * @param phoneData The phone data string to add to the phone data list.
   */
  void addPhoneData(String phoneData);

  /**
   * Returns the list of relationship data strings for the record.
   *
   * @return The list of relationship data strings for the record.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getRelationshipData();

  /**
   * Sets the relationship data list for the record.
   *
   * @param relationshipData The list of relationship data strings.
   */
   void setRelationshipData(List<String> relationshipData);

   /**
   * Adds to the relationship data list for the record.
   *
   * @param relationshipData The relationship data string to add to the
   *                         relationship data list.
   */
  void addRelationshipData(String relationshipData);

  /**
   * Returns the list of other data strings for the record.
   *
   * @return The list of other data strings for the record.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getOtherData();

  /**
   * Sets the other data list for the record.
   *
   * @param otherData The list of other data strings.
   */
  void setOtherData(List<String> otherData);

  /**
   * Adds to the other data list for the record.
   *
   * @param otherData The other data string to add to the other data list.
   */
  void addOtherData(String otherData);

  /**
   * Returns the original source data that was used to load the record.
   *
   * @return The original source data that was used to load the record.
   */
  @JsonInclude(NON_NULL)
  Object getOriginalSourceData();

  /**
   * Sets the original source data using the specified object.
   *
   * @param jsonObject The object representation of the JSON for the
   *                   original source data.
   */
  void setOriginalSourceData(Object jsonObject);

  /**
   * Sets the original source data using the specified JSON text.
   *
   * @param jsonText The JSON text for the original source data.
   */
  void setOriginalSourceDataFromText(String jsonText);

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityRecord}.
   */
  interface Provider extends ModelProvider<SzEntityRecord> {
    /**
     * Creates a new instance of {@link SzEntityRecord}.
     *
     * @return The new instance of {@link SzEntityRecord}
     */
    SzEntityRecord create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityRecord} that produces instances of {@link SzEntityRecordImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityRecord>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityRecord.class, SzEntityRecordImpl.class);
    }

    @Override
    public SzEntityRecord create() {
      return new SzEntityRecordImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzEntityRecord}.
   */
  class Factory extends ModelFactory<SzEntityRecord, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityRecord.class);
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
     * Creates a new instance of {@link SzEntityRecord}.
     * @return The new instance of {@link SzEntityRecord}.
     */
    public SzEntityRecord create()
    {
      return this.getProvider().create();
    }

    /**
     * The pattern for parsing the date values returned from the native API.
     */
    private static final String NATIVE_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * The time zone used for the time component of the build number.
     */
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    /**
     * The {@link DateTimeFormatter} for interpreting the timestamps from the
     * native API.
     */
    private static final DateTimeFormatter NATIVE_DATE_FORMATTER
        = DateTimeFormatter.ofPattern(NATIVE_DATE_PATTERN);
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native JSON to construct/populate a {@link List}
   * of {@link SzEntityRecord} instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   * @param jsonArray The {@link JsonArray} describing the native JSON array.
   *
   * @return The specified (or constructed) {@link List} of {@link
   *         SzEntityRecord} instances.
   */
  static List<SzEntityRecord> parseEntityRecordList(
      List<SzEntityRecord>  list,
      JsonArray             jsonArray)
  {
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      if (list == null) {
        list = new ArrayList<>(jsonArray.size());
      }
      list.add(parseEntityRecord(null, jsonObject));
    }
    if (list != null) {
      list = Collections.unmodifiableList(list);
    } else {
      list = Collections.emptyList();
    }
    return list;
  }

  /**
   * Private method to get the various *Data fields for the record.
   *
   * @param jsonObject The {@link JsonObject} to pull from.
   * @param key The key for the attribute of the JSON object.
   * @return The {@link List} of strings.
   */
  private static void getValueList(JsonObject       jsonObject,
                                   String           key,
                                   Consumer<String> consumer)
  {
    JsonArray jsonArray = getJsonArray(jsonObject, key);
    if (jsonArray == null) return;
    for (JsonString value : jsonArray.getValuesAs(JsonString.class)) {
      consumer.accept(value.getString());
    }
  }

  /**
   * Parses the native API JSON to build an populate or create an instance of
   * {@link SzEntityRecord}.
   *
   * @param record The {@link SzEntityRecord} to populate or <tt>null</tt> if
   *               a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} describing the record using the
   *                   native API JSON format.
   *
   * @return The populated (or created) instance of {@link SzEntityRecord}
   */
  static SzEntityRecord parseEntityRecord(SzEntityRecord  record,
                                          JsonObject      jsonObject)
  {
    final DateTimeFormatter NATIVE_DATE_FORMATTER
        = Factory.NATIVE_DATE_FORMATTER;
    final ZoneId UTC_ZONE = Factory.UTC_ZONE;

    if (record == null) record = SzEntityRecord.FACTORY.create();
    final SzEntityRecord rec = record;

    // get the data source and record ID
    String dataSource = jsonObject.getString("DATA_SOURCE");
    String recordId   = jsonObject.getString("RECORD_ID");

    record.setDataSource(dataSource);
    record.setRecordId(recordId);

    JsonArray featureArray = getJsonArray(jsonObject, "FEATURES");
    if (featureArray != null) {
      record.setFeatureReferences(
          SzFeatureReference.parseFeatureReferenceList(null, featureArray));
    }

    // get the last seen date
    String lastSeen = JsonUtilities.getString(jsonObject, "LAST_SEEN_DT");
    Date lastSeenDate = null;
    if (lastSeen != null && lastSeen.trim().length() > 0) {
      LocalDateTime localDateTime
          = LocalDateTime.parse(lastSeen, NATIVE_DATE_FORMATTER);
      ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, UTC_ZONE);
      lastSeenDate = Date.from(zonedDateTime.toInstant());
    }

    // get the raw data map
    JsonObject  jsonData    = jsonObject.getJsonObject("JSON_DATA");
    String      sourceData  = JsonUtilities.toJsonText(jsonData);

    record.setLastSeenTimestamp(lastSeenDate);
    record.setOriginalSourceDataFromText(sourceData);

    getValueList(jsonObject, "ADDRESS_DATA", (addr) -> {
      rec.addAddressData(addr);
    });

    getValueList(jsonObject, "ATTRIBUTE_DATA", (attr) -> {
      rec.addCharacteristicData(attr);
    });

    getValueList(jsonObject, "IDENTIFIER_DATA", (ident) -> {
      rec.addIdentifierData(ident);
    });

    getValueList(jsonObject, "NAME_DATA", (name) -> {
      rec.addNameData(name);
    });

    getValueList(jsonObject, "PHONE_DATA", (phone) -> {
      rec.addPhoneData(phone);
    });

    getValueList(jsonObject, "RELATIONSHIP_DATA", (rel) -> {
      rec.addRelationshipData(rel);
    });

    getValueList(jsonObject, "OTHER_DATA", (other) -> {
      rec.addOtherData(other);
    });

    return record;
  }
}
