package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzDataSourceRecordSummary;
import com.senzing.api.model.SzEntityFeature;
import com.senzing.api.model.SzMatchedRecord;
import com.senzing.api.model.SzResolvedEntity;
import java.util.*;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Provides a default impleemntation of {@link SzResolvedEntity}.
 */
@JsonDeserialize
public class SzResolvedEntityImpl implements SzResolvedEntity {
  /**
   * The entity ID.
   */
  private Long entityId;

  /**
   * The assigned name to the entity.
   */
  private String entityName;

  /**
   * The best name for the entity.
   */
  private String bestName;

  /**
   * The {@link List} of {@link SzDataSourceRecordSummary} instances.
   */
  private List<SzDataSourceRecordSummary> recordSummaries;

  /**
   * The list of address data strings.
   */
  private List<String> addressData;

  /**
   * The list of attribute data strings.
   */
  private List<String> characteristicData;

  /**
   * The list of identifier data strings.
   */
  private List<String> identifierData;

  /**
   * The list of name data strings.
   */
  private List<String> nameData;

  /**
   * The list of phone data strings.
   */
  private List<String> phoneData;

  /**
   * The list of relationship data strings.
   */
  private List<String> relationshipData;

  /**
   * The list of other data strings.
   */
  private List<String> otherData;

  /**
   * The {@link Map} of features.
   */
  private Map<String, List<SzEntityFeature>> features;

  /**
   * The {@link Map} of unmodifiable features.
   */
  private Map<String, List<SzEntityFeature>> unmodifiableFeatures;

  /**
   * The {@link List} of {@link SzMatchedRecord} instances for the
   * records in the entity.
   */
  private List<SzMatchedRecord> records;

  /**
   * Whether or not this entity is partially populated.
   */
  private boolean partial;

  /**
   * The last seen timestamp.
   */
  private Date lastSeenTimestamp;

  /**
   * Default constructor.
   */
  public SzResolvedEntityImpl() {
    this.entityId             = null;
    this.entityName           = null;
    this.bestName             = null;
    this.recordSummaries      = new LinkedList<>();
    this.addressData          = new LinkedList<>();
    this.characteristicData   = new LinkedList<>();
    this.identifierData       = new LinkedList<>();
    this.nameData             = new LinkedList<>();
    this.phoneData            = new LinkedList<>();
    this.relationshipData     = new LinkedList<>();
    this.otherData            = new LinkedList<>();
    this.features             = new LinkedHashMap<>();
    this.unmodifiableFeatures = new LinkedHashMap<>();
    this.records              = new LinkedList<>();
    this.lastSeenTimestamp    = null;
    this.partial              = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getEntityId() {
    return entityId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntityId(Long entityId) {
    this.entityId = entityId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getEntityName() {
    return this.entityName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getBestName() {
    return this.bestName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setBestName(String bestName) {
    this.bestName = bestName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzMatchedRecord> getRecords() {
    return Collections.unmodifiableList(this.records);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRecords(List<SzMatchedRecord> records) {
    this.records.clear();
    if (records != null) {
      this.records.addAll(records);

      // recalculate the "other data"
      this.otherData.clear();
      Set<String> set = new LinkedHashSet<>();
      for (SzMatchedRecord record : records) {
        List<String> recordOtherData = record.getOtherData();
        if (recordOtherData != null) {
          for (String data : recordOtherData) {
            set.add(data);
          }
        }
      }
      this.otherData.addAll(set);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRecord(SzMatchedRecord record)
  {
    this.records.add(record);
    List<String> recordOtherData = record.getOtherData();
    if (recordOtherData != null) {
      for (String data: recordOtherData) {
        if (! this.otherData.contains(data)) {
          this.otherData.add(data);
        }
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public List<SzDataSourceRecordSummary> getRecordSummaries() {
    return Collections.unmodifiableList(this.recordSummaries);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRecordSummaries(List<SzDataSourceRecordSummary> summaries) {
    this.recordSummaries.clear();
    if (summaries != null) {
      this.recordSummaries.addAll(summaries);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRecordSummary(SzDataSourceRecordSummary summary)
  {
    this.recordSummaries.add(summary);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getAddressData() {
    return Collections.unmodifiableList(this.addressData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAddressData(List<String> addressData) {
    this.addressData.clear();
    if (addressData != null) {
      this.addressData.addAll(addressData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addAddressData(String addressData) {
    this.addressData.add(addressData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getCharacteristicData() {
    return Collections.unmodifiableList(this.characteristicData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCharacteristicData(List<String> characteristicData) {
    this.characteristicData.clear();
    if (characteristicData != null) {
      this.characteristicData.addAll(characteristicData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addCharacteristicData(String attributeData) {
    this.characteristicData.add(attributeData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getIdentifierData() {
    return Collections.unmodifiableList(this.identifierData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setIdentifierData(List<String> identifierData) {
    this.identifierData.clear();
    if (identifierData != null) {
      this.identifierData.addAll(identifierData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addIdentifierData(String identifierData) {
    this.identifierData.add(identifierData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getNameData() {
    return Collections.unmodifiableList(this.nameData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setNameData(List<String> nameData) {
    this.nameData.clear();
    if (nameData != null) {
      this.nameData.addAll(nameData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNameData(String nameData)
  {
    this.nameData.add(nameData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getPhoneData() {
    return Collections.unmodifiableList(this.phoneData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setPhoneData(List<String> phoneData) {
    this.phoneData.clear();
    if (phoneData != null) {
      this.phoneData.addAll(phoneData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addPhoneData(String phoneData)
  {
    this.phoneData.add(phoneData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getRelationshipData() {
    return Collections.unmodifiableList(this.relationshipData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRelationshipData(List<String> relationshipData) {
    this.relationshipData.clear();
    if (relationshipData != null) {
      this.relationshipData.addAll(relationshipData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRelationshipData(String relationshipData)
  {
    this.relationshipData.add(relationshipData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getOtherData() {
    return Collections.unmodifiableList(this.otherData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setOtherData(List<String> otherData) {
    this.otherData.clear();
    if (otherData != null) {
      this.otherData.addAll(otherData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addOtherData(String otherData)
  {
    this.otherData.add(otherData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, List<SzEntityFeature>> getFeatures() {
    return Collections.unmodifiableMap(this.unmodifiableFeatures);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFeatures(
      Map<String, ? extends Collection<? extends SzEntityFeature>> featureMap)
  {
    this.features.clear();
    this.unmodifiableFeatures.clear();

    if (featureMap != null) {
      featureMap.entrySet().forEach(entry -> {
        String                                featureName = entry.getKey();
        Collection<? extends SzEntityFeature> list        = entry.getValue();
        List<SzEntityFeature>                 copiedList  = new ArrayList<>(list);

        List<SzEntityFeature> unmodifiableList
            = Collections.unmodifiableList(copiedList);

        this.features.put(featureName, copiedList);
        this.unmodifiableFeatures.put(featureName, unmodifiableList);
      });
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFeatures(
      Map<String, ? extends Collection<? extends SzEntityFeature>>  featureMap,
      Function<String, String> featureToAttrClassMapper)
  {
    this.setFeatures(featureMap);

    // clear out the data lists
    this.addressData.clear();
    this.characteristicData.clear();
    this.identifierData.clear();
    this.nameData.clear();
    this.phoneData.clear();

    if (featureMap == null) return;

    Function<String,String> mapper = featureToAttrClassMapper;
    getDataFields("NAME", featureMap, mapper).forEach((name) -> {
      this.addNameData(name);
    });

    getDataFields("ATTRIBUTE", featureMap, mapper).forEach((attr) -> {
      this.addCharacteristicData(attr);
    });

    getDataFields("ADDRESS", featureMap, mapper).forEach((addr) -> {
      this.addAddressData(addr);
    });

    getDataFields("PHONE", featureMap, mapper).forEach((phone) -> {
      this.addPhoneData(phone);
    });

    getDataFields("IDENTIFIER", featureMap, mapper).forEach((ident) -> {
      this.addIdentifierData(ident);
    });

    getDataFields("RELATIONSHIP", featureMap, mapper).forEach((rel) -> {
      this.addRelationshipData(rel);
    });
  }

  /**
   * Sets the specified feature with the specified feature name to the
   * {@link List} of {@link SzEntityFeature} instances.
   *
   * @param featureName The name of the feature.
   *
   * @param values The {@link List} of {@link SzEntityFeature} instances
   *               describing the feature values.
   */
  public void setFeature(String                                 featureName,
                         Collection<? extends SzEntityFeature>  values)
  {
    List<SzEntityFeature> featureValues = this.features.get(featureName);

    if (featureValues != null && (values == null || values.size() == 0)) {
      // feature exists, so remove the feature since no specified values
      this.features.remove(featureName);

    } else if (featureValues != null) {
      // feature exists, but are being replaced
      featureValues.clear();
      featureValues.addAll(values);

    } else if (values != null && values.size() > 0) {
      // the feature does not exist but new values are being added
      featureValues = new LinkedList<>();
      featureValues.addAll(values);
      this.features.put(featureName, featureValues);

      List<SzEntityFeature> unmodifiableFeatureValues
          = Collections.unmodifiableList(featureValues);

      this.unmodifiableFeatures.put(featureName, unmodifiableFeatureValues);
    }
  }

  /**
   * Adds a {@link SzEntityFeature} value to the feature with the specified
   * feature name.
   *
   * @param featureName The name of the feature.
   *
   * @param value The {@link SzEntityFeature} describing the feature value.
   */
  public void addFeature(String featureName, SzEntityFeature value)
  {
    if (value == null) return;
    List<SzEntityFeature> featureValues = this.features.get(featureName);
    if (featureValues == null) {
      featureValues = new LinkedList<>();

      List<SzEntityFeature> unmodifiableFeatureValues
          = Collections.unmodifiableList(featureValues);

      this.features.put(featureName, featureValues);
      this.unmodifiableFeatures.put(featureName, unmodifiableFeatureValues);
    }
    featureValues.add(value);
  }

  /**
   * Checks whether or not the entity data is only partially populated.
   * If partially populated then it will not have complete features or records
   * and the record summaries may be missing the top record IDs.
   *
   * @return <tt>true</tt> if the entity data is only partially
   *         populated, otherwise <tt>false</tt>.
   */
  public boolean isPartial() {
    return this.partial;
  }

  /**
   * Sets whether or not the entity data is only partially populated.
   * If partially populated then it will not have complete features or records
   * and the record summaries may be missing the top record IDs.
   *
   * @param partial <tt>true</tt> if the entity data is only partially
   *                populated, otherwise <tt>false</tt>.
   */
  public void setPartial(boolean partial) {
    this.partial = partial;
  }

  /**
   * Gets the last-seen timestamp for the entity.
   *
   * @return The last-seen timestamp for the entity.
   */
  @JsonInclude(NON_NULL)
  public Date getLastSeenTimestamp() {
    return this.lastSeenTimestamp;
  }

  /**
   * Sets the last-seen timestamp for the entity.
   *
   * @param timestamp The last-seen timestamp for the entity.
   */
  public void setLastSeenTimestamp(Date timestamp) {
    this.lastSeenTimestamp = timestamp;
  }

  /**
   * Utility method to get the "data values" from the features.
   *
   * @param attrClass The attribute class for which to pull the values.
   *
   * @param featureMap The {@link Map} of features.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The {@link List} of {@link String} values.
   */
  private static List<String> getDataFields(
      String                                                        attrClass,
      Map<String, ? extends Collection<? extends SzEntityFeature>>  featureMap,
      Function<String,String> featureToAttrClassMapper)
  {
    List<String> dataList = new LinkedList<>();
    List<String> result   = Collections.unmodifiableList(dataList);

    featureMap.entrySet().forEach(entry -> {
      String ftypeCode = entry.getKey();
      Collection<? extends SzEntityFeature> values = entry.getValue();

      String ac = featureToAttrClassMapper.apply(ftypeCode);

      if (ac == null) return;
      if (!ac.equalsIgnoreCase(attrClass)) return;

      String prefix = (attrClass.equalsIgnoreCase(ftypeCode)
          ? "" : ftypeCode + ": ");

      boolean relLink = ftypeCode.equalsIgnoreCase("REL_LINK");
      values.forEach(val -> {
        String usageType = val.getUsageType();
        if (usageType != null && usageType.length() > 0) {
          usageType = usageType.trim()
              + (!relLink && prefix.endsWith(": ") ? " " : ": ");
        } else {
          usageType = "";
        }

        String dataValue = (relLink)
            ? prefix + usageType + val.getPrimaryValue()
            : usageType + prefix + val.getPrimaryValue();

        // add the value to the list
        dataList.add(dataValue);
      });
    });

    Collections.sort(dataList, (v1, v2) -> {
      if (v1.startsWith("PRIMARY") && !v2.startsWith("PRIMARY")) {
        return -1;
      } else if (v2.startsWith("PRIMARY") && !v1.startsWith("PRIMARY")) {
        return 1;
      }
      int comp = v2.length() - v1.length();
      if (comp != 0) return comp;
      return v1.compareTo(v2);
    });

    return result;
  }

  @Override
  public String toString() {
    return "SzResolvedEntity{" +
        "entityId=" + entityId +
        ", partial=" + partial +
        ", entityName='" + entityName + '\'' +
        ", bestName='" + bestName + '\'' +
        ", recordSummaries=" + recordSummaries +
        ", lastSeenTimestamp=" + lastSeenTimestamp +
        ", addressData=" + addressData +
        ", attributeData=" + characteristicData +
        ", identifierData=" + identifierData +
        ", nameData=" + nameData +
        ", phoneData=" + phoneData +
        ", relationshipData=" + relationshipData +
        ", otherData=" + otherData +
        ", features=" + features +
        ", records=" + records +
        '}';
  }
}
