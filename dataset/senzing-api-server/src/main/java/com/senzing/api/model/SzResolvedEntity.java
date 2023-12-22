package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzResolvedEntityImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes a resolved entity.
 */
@JsonDeserialize(using=SzResolvedEntity.Factory.class)
public interface SzResolvedEntity {
  /**
   * Gets the entity ID for the entity.
   *
   * @return The entity ID for the entity.
   */
  Long getEntityId();

  /**
   * Sets the entity ID for the entity.
   *
   * @param entityId The entity ID for the entity.
   */
  void setEntityId(Long entityId);

  /**
   * Gets the entity name.  This is usually the same as the {@link
   * #getBestName() best name} except in the case of search results
   * where it may differ.
   *
   * @return The highest fidelity name for the entity.
   */
  @JsonInclude(NON_NULL)
  String getEntityName();

  /**
   * Sets the entity name.  This is usually the same as the {@link
   * #getBestName() best name} except in the case of search results
   * where it may differ.
   *
   * @param entityName The highest fidelity name for the entity.
   */
  void setEntityName(String entityName);

  /**
   * Gets the best name.  This is usually the same as the {@link
   * #getEntityName() entity name} except in the case of search results
   * where it may differ.
   *
   * @return The best name for the entity.
   */
  @JsonInclude(NON_NULL)
  String getBestName();

  /**
   * Sets the best name.  This is usually the same as the {@link
   * #getEntityName() entity name} except in the case of search results
   * where it may differ.
   *
   * @param bestName The best name for the entity.
   */
  void setBestName(String bestName);

  /**
   * Returns the list of {@link SzMatchedRecord} instances describing the records
   * for the entity.
   *
   * @return The list of {@link SzMatchedRecord} instances describing the records
   *         for the entity.
   */
  @JsonInclude(NON_EMPTY)
  List<SzMatchedRecord> getRecords();

  /**
   * Sets the list {@linkplain SzMatchedRecord records} for the entity.
   *
   * @param records The list {@linkplain SzMatchedRecord records} for the entity.
   */
  void setRecords(List<SzMatchedRecord> records);

  /**
   * Adds the specified {@link SzMatchedRecord} to the list of {@linkplain
   * SzMatchedRecord records}.
   *
   * @param record The {@link SzMatchedRecord} to add to the record list.
   */
  void addRecord(SzMatchedRecord record);

  /**
   * Returns the list of {@link SzDataSourceRecordSummary} instances for the entity.
   *
   * @return The list of {@link SzDataSourceRecordSummary} instances for the entity.
   */
  @JsonInclude(NON_EMPTY)
  List<SzDataSourceRecordSummary> getRecordSummaries();

  /**
   * Sets the list {@link SzDataSourceRecordSummary record summaries} for the entity.
   *
   * @param summaries The list {@link SzDataSourceRecordSummary record summaries}
   *                  for the entity.
   */
  void setRecordSummaries(List<SzDataSourceRecordSummary> summaries);

  /**
   * Adds the specified {@link SzDataSourceRecordSummary} to the list of associated
   * {@linkplain SzDataSourceRecordSummary record summaries}.
   *
   * @param summary The {@link SzDataSourceRecordSummary} to add to the record summaries.
   */
  void addRecordSummary(SzDataSourceRecordSummary summary);

  /**
   * Returns the list of address data strings for the entity.
   *
   * @return The list of address data strings for the entity.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getAddressData();

  /**
   * Sets the address data list for the entity.
   *
   * @param addressData The list of address data strings.
   */
  void setAddressData(List<String> addressData);

  /**
   * Adds to the address data list for the entity.
   *
   * @param addressData The address data string to add to the address data list.
   */
  void addAddressData(String addressData);

  /**
   * Returns the list of attribute data strings for the entity.
   *
   * @return The list of attribute data strings for the entity.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getCharacteristicData();

  /**
   * Sets the attribute data list for the entity.
   *
   * @param characteristicData The list of attribute data strings.
   */
  void setCharacteristicData(List<String> characteristicData);

  /**
   * Adds to the attribute data list for the entity.
   *
   * @param attributeData The attribute data string to add to the address
   *                      data list.
   */
  void addCharacteristicData(String attributeData);

  /**
   * Returns the list of identifier data strings for the entity.
   *
   * @return The list of identifier data strings for the entity.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getIdentifierData();

  /**
   * Sets the identifier data list for the entity.
   *
   * @param identifierData The list of identifier data strings.
   */
  void setIdentifierData(List<String> identifierData);

  /**
   * Adds to the identifier data list for the entity.
   *
   * @param identifierData The identifier data string to add to the identifier
   *                       data list.
   */
  void addIdentifierData(String identifierData);

  /**
   * Returns the list of name data strings for the entity.
   *
   * @return The list of name data strings for the entity.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getNameData();

  /**
   * Sets the name data list for the entity.
   *
   * @param nameData The list of name data strings.
   */
  void setNameData(List<String> nameData);

  /**
   * Adds to the name data list for the entity.
   *
   * @param nameData The name data string to add to the name data list.
   */
  void addNameData(String nameData);

  /**
   * Returns the list of phone data strings for the entity.
   *
   * @return The list of phone data strings for the entity.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getPhoneData();

  /**
   * Sets the phone data list for the entity.
   *
   * @param phoneData The list of name data strings.
   */
  void setPhoneData(List<String> phoneData);

  /**
   * Adds to the phone data list for the entity.
   *
   * @param phoneData The phone data string to add to the phone data list.
   */
  void addPhoneData(String phoneData);

  /**
   * Returns the list of relationship data strings for the entity.
   *
   * @return The list of relationship data strings for the entity.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getRelationshipData();

  /**
   * Sets the relationship data list for the entity.
   *
   * @param relationshipData The list of relationship data strings.
   */
  void setRelationshipData(List<String> relationshipData);

  /**
   * Adds to the relationship data list for the entity.
   *
   * @param relationshipData The relationship data string to add to the
   *                         relationship data list.
   */
  void addRelationshipData(String relationshipData);

  /**
   * Returns the list of other data strings for the entity.
   *
   * @return The list of other data strings for the entity.
   */
  @JsonInclude(NON_EMPTY)
  List<String> getOtherData();

  /**
   * Sets the other data list for the entity.
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
   * Returns an <b>unmodifiable</b> {@link Map} of {@link String} feature
   * names to {@link SzEntityFeature} instances describing those features.
   *
   * @return As <b>unmodifiable</b> {@link Map} of {@link String} feature
   *         names to {@link SzEntityFeature} instances describing those
   *         features.
   */
  @JsonInclude(NON_EMPTY)
  Map<String, List<SzEntityFeature>> getFeatures();

  /**
   * Sets the features using the specified {@link Map} of feature type to
   * {@link Collection} values containing {@link SzEntityFeature} instances.
   *
   * @param featureMap The {@link Map} describing the features as {@link String}
   *                   key values to {@link Collection} values containing
   *                   {@link SzEntityFeature} instances.
   */
  void setFeatures(
      Map<String, ? extends Collection<? extends SzEntityFeature>> featureMap);

  /**
   * Sets the features using the specified {@link Map} of feature type to
   * {@link Collection} values containing {@link SzEntityFeature} instances.
   *
   * @param featureMap The {@link Map} describing the features as {@link String}
   *                   key values to {@link Collection} values containing
   *                   {@link SzEntityFeature} instances.
   * @param featureToAttrClassMapper The mapping function to map feature types
   *                                 to attribute class values.
   */
  void setFeatures(
      Map<String, ? extends Collection<? extends SzEntityFeature>>  featureMap,
      Function<String,String> featureToAttrClassMapper);

  /**
   * Sets the specified feature with the specified feature name to the
   * {@link List} of {@link SzEntityFeature} instances.
   *
   * @param featureName The name of the feature.
   *
   * @param values The {@link List} of {@link SzEntityFeature} instances
   *               describing the feature values.
   */
  void setFeature(String                                featureName,
                  Collection<? extends SzEntityFeature> values);

  /**
   * Adds a {@link SzEntityFeature} value to the feature with the specified
   * feature name.
   *
   * @param featureName The name of the feature.
   *
   * @param value The {@link SzEntityFeature} describing the feature value.
   */
  void addFeature(String featureName, SzEntityFeature value);

  /**
   * Checks whether or not the entity data is only partially populated.
   * If partially populated then it will not have complete features or records
   * and the record summaries may be missing the top record IDs.
   *
   * @return <tt>true</tt> if the entity data is only partially
   *         populated, otherwise <tt>false</tt>.
   */
  boolean isPartial();

  /**
   * Sets whether or not the entity data is only partially populated.
   * If partially populated then it will not have complete features or records
   * and the record summaries may be missing the top record IDs.
   *
   * @param partial <tt>true</tt> if the entity data is only partially
   *                populated, otherwise <tt>false</tt>.
   */
  void setPartial(boolean partial);

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
   * A {@link ModelProvider} for instances of {@link SzResolvedEntity}.
   */
  interface Provider extends ModelProvider<SzResolvedEntity> {
    /**
     * Creates a new instance of {@link SzResolvedEntity}.
     *
     * @return The new instance of {@link SzResolvedEntity}
     */
    SzResolvedEntity create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzResolvedEntity} that produces instances of {@link SzResolvedEntityImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzResolvedEntity>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzResolvedEntity.class, SzResolvedEntityImpl.class);
    }

    @Override
    public SzResolvedEntity create() {
      return new SzResolvedEntityImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzResolvedEntity}.
   */
  class Factory extends ModelFactory<SzResolvedEntity, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzResolvedEntity.class);
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
     * Creates a new instance of {@link SzResolvedEntity}.
     * @return The new instance of {@link SzResolvedEntity}.
     */
    public SzResolvedEntity create()
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

    /**
     * The number of records to consider to be the top records.
     */
    private static final int TOP_COUNT = 10;
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
  
  /**
   * Parses a list of resolved entities from a {@link JsonArray} describing a
   * JSON array in the Senzing native API format for entity features and
   * populates the specified {@link List} or creates a new {@link List}.
   *
   * @param list The {@link List} of {@link SzResolvedEntity} instances to
   *             populate, or <tt>null</tt> if a new {@link List}
   *             should be created.
   *
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link List} of {@link
   *         SzResolvedEntity} instances.
   */
  static List<SzResolvedEntity> parseResolvedEntityList(
      List<SzResolvedEntity>  list,
      JsonArray               jsonArray,
      Function<String,String> featureToAttrClassMapper)
  {
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {

      if (list == null) {
        list = new ArrayList<>(jsonArray.size());
      }

      list.add(parseResolvedEntity(null,
                                   jsonObject,
                                   featureToAttrClassMapper));
    }
    if (list != null) {
      list = Collections.unmodifiableList(list);
    } else {
      list = Collections.emptyList();
    }
    return list;
  }

  /**
   * Parses the resolved entity from a {@link JsonObject} describing JSON
   * for the Senzing native API format for a resolved entity and populates
   * the specified {@link SzResolvedEntity} or creates a new instance.
   *
   * @param entity The {@link SzResolvedEntity} instance to populate, or
   *               <tt>null</tt> if a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link SzResolvedEntity}.
   */
  static SzResolvedEntity parseResolvedEntity(
      SzResolvedEntity        entity,
      JsonObject              jsonObject,
      Function<String,String> featureToAttrClassMapper)
  {
    final DateTimeFormatter NATIVE_DATE_FORMATTER
        = Factory.NATIVE_DATE_FORMATTER;
    final ZoneId UTC_ZONE = Factory.UTC_ZONE;

    if (entity == null) entity = SzResolvedEntity.FACTORY.create();

    long entityId     = jsonObject.getJsonNumber("ENTITY_ID").longValue();
    String entityName = JsonUtilities.getString(jsonObject, "ENTITY_NAME");

    Map<String,List<SzEntityFeature>> featureMap = null;

    boolean partial = (!jsonObject.containsKey("FEATURES")
                      || !jsonObject.containsKey("RECORDS"));

    if (jsonObject.containsKey("FEATURES")) {
      JsonObject features = jsonObject.getJsonObject("FEATURES");
      for (String key : features.keySet()) {
        JsonArray jsonArray = features.getJsonArray(key);
        List<SzEntityFeature> featureValues
            = SzEntityFeature.parseEntityFeatureList(null, jsonArray);
        if (featureMap == null) {
          featureMap = new LinkedHashMap<>();
        }
        featureMap.put(key, featureValues);
      }

      if (featureMap != null) {
        featureMap = Collections.unmodifiableMap(featureMap);
      }
    }

    // get the records
    List<SzMatchedRecord> recordList = null;
    List<SzDataSourceRecordSummary> summaries = null;

    if (jsonObject.containsKey("RECORDS")) {
      JsonArray records = jsonObject.getJsonArray("RECORDS");
      recordList = SzMatchedRecord.parseMatchedRecordList(null, records);
      summaries = summarizeRecords(recordList);

    } else if (jsonObject.containsKey("RECORD_SUMMARY")) {
      JsonArray jsonArray = jsonObject.getJsonArray("RECORD_SUMMARY");
      summaries = SzDataSourceRecordSummary.parseRecordSummaryList(null, jsonArray);

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

    entity.setLastSeenTimestamp(lastSeenDate);
    entity.setEntityName(entityName);
    entity.setEntityId(entityId);
    entity.setFeatures(featureMap, featureToAttrClassMapper);
    entity.setRecords(recordList);
    entity.setRecordSummaries(summaries);
    entity.setPartial(partial);

    // iterate over the feature map
    return entity;
  }

  /**
   * Summarizes the specified {@link List} of {@linkplain SzMatchedRecord
   * records} and produces a {@link List} of {@link SzDataSourceRecordSummary} instances.
   *
   * @param records The records to be summarized.
   * @return The {@link List} of {@link SzDataSourceRecordSummary} instances describing
   *         the summaries.
   */
  static List<SzDataSourceRecordSummary> summarizeRecords(
      List<SzMatchedRecord>  records)
  {
    final int TOP_COUNT = Factory.TOP_COUNT;

    // check if we have no records
    if (records.size() == 0) return Collections.emptyList();

    // calculate the result by accumulating records by data source
    Map<String, List<String>> map = new LinkedHashMap<>();

    // for each record....
    records.stream().forEach(record -> {
      // get the data source and record ID
      String dataSource = record.getDataSource();
      String recordId = record.getRecordId();

      // check if we already have a list of record IDs for this data source
      List<String> list = map.get(dataSource);
      if (list == null) {
        // if not, then create the list and keep it
        list = new LinkedList<>();
        map.put(dataSource, list);
      }
      // add to the list
      list.add(recordId);
    });

    // construct the result list
    final List<SzDataSourceRecordSummary> tempList = new ArrayList<>(map.size());

    // for each entry in the map....
    map.entrySet().stream().forEach(entry -> {
      // get the data source and record ID's
      String        dataSource  = entry.getKey();
      List<String>  recordIds   = entry.getValue();
      int           recordCount = recordIds.size();

      // sort the record ID's to ensure consistent responses
      Collections.sort(recordIds);

      // check if we have lots of record ID's (more than TOP_COUNT)
      if (recordIds.size() > TOP_COUNT) {
        // if so, truncate the list of "top record ID's"
        recordIds = new ArrayList<>(recordIds.subList(0, TOP_COUNT));
      }

      recordIds = Collections.unmodifiableList(recordIds);

      // create a new record summary
      SzDataSourceRecordSummary summary
          = SzDataSourceRecordSummary.FACTORY.create();
      summary.setDataSource(dataSource);
      summary.setRecordCount(recordCount);
      summary.setTopRecordIds(recordIds);

      tempList.add(summary);
    });

    Collections.sort(
        tempList, Comparator.comparing(SzDataSourceRecordSummary::getDataSource));

    List<SzDataSourceRecordSummary> result = Collections.unmodifiableList(tempList);

    return result;
  }
}
