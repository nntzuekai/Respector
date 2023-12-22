package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityRecord;
import com.senzing.api.model.SzFeatureReference;
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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Provides a default implementation of {@link SzEntityRecord}.
 */
@JsonDeserialize
public class SzEntityRecordImpl implements SzEntityRecord {
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
   * The data source code for the record.
   */
  private String dataSource;

  /**
   * The record ID for the record.
   */
  private String recordId;

  /**
   * The {@link List} of {@link SzFeatureReference} instances referencing the
   * features that are contributed by this record along with their associated
   * usage types.
   */
  private List<SzFeatureReference> featureReferences;

  /**
   * The list of address data strings.
   */
  private List<String> addressData;

  /**
   * The list of characteristic data strings.
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
   * The object representing the original source data.
   */
  private Object originalSourceData;

  /**
   * The last seen timestamp.
   */
  @JsonFormat(shape   = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
      locale  = "en_GB")
  private Date lastSeenTimestamp;

  /**
   * Default constructor.
   */
  public SzEntityRecordImpl() {
    this.dataSource         = null;
    this.recordId           = null;
    this.lastSeenTimestamp  = null;
    this.featureReferences  = new LinkedList<>();
    this.addressData        = new LinkedList<>();
    this.characteristicData = new LinkedList<>();
    this.identifierData     = new LinkedList<>();
    this.nameData           = new LinkedList<>();
    this.phoneData          = new LinkedList<>();
    this.relationshipData   = new LinkedList<>();
    this.otherData          = new LinkedList<>();
    this.originalSourceData = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDataSource()
  {
    return dataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRecordId()
  {
    return recordId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRecordId(String recordId)
  {
    this.recordId = recordId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Date getLastSeenTimestamp() {
    return this.lastSeenTimestamp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLastSeenTimestamp(Date timestamp) {
    this.lastSeenTimestamp = timestamp;
  }

  @Override
  public List<SzFeatureReference> getFeatureReferences() {
    return Collections.unmodifiableList(this.featureReferences);
  }

  @Override
  public void setFeatureReferences(Collection<SzFeatureReference> featureRefs) {
    this.featureReferences.clear();
    if (featureRefs != null) {
      this.featureReferences.addAll(featureRefs);
    }
  }

  @Override
  public void addFeatureReference(SzFeatureReference featureRef) {
    this.featureReferences.add(featureRef);
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
  public void setAddressData(List<String> addressData)
  {
    this.addressData.clear();
    if (addressData != null) {
      this.addressData.addAll(addressData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addAddressData(String addressData)
  {
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
  public void setCharacteristicData(List<String> characteristicData)
  {
    this.characteristicData.clear();
    if (characteristicData != null) {
      this.characteristicData.addAll(characteristicData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addCharacteristicData(String characteristicData)
  {
    this.characteristicData.add(characteristicData);
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
  public void setIdentifierData(List<String> identifierData)
  {
    this.identifierData.clear();
    if (identifierData != null) {
      this.identifierData.addAll(identifierData);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addIdentifierData(String identifierData)
  {
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
  public Object getOriginalSourceData() {
    return originalSourceData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setOriginalSourceData(Object jsonObject)
  {
    if (jsonObject != null && jsonObject instanceof String) {
      this.setOriginalSourceDataFromText((String) jsonObject);
    } else {
      this.originalSourceData = jsonObject;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setOriginalSourceDataFromText(String jsonText)
  {
    this.originalSourceData = JsonUtilities.normalizeJsonText(jsonText);
  }


  @Override
  public String toString() {
    return "SzEntityRecord{" + this.fieldsToString() + "}";
  }

  protected String fieldsToString() {
    return "dataSource=[ " + this.getDataSource()
        + " ], recordId=[ " + this.getRecordId()
        + " ], lastSeenTimestamp=[ " + this.getLastSeenTimestamp()
        + " ], featureReferences=[ " + this.getFeatureReferences()
        + " ], addressData=[ " + this.getAddressData()
        + " ], characteristicData=[ " + this.getCharacteristicData()
        + " ], identifierData=[ " + this.getIdentifierData()
        + " ], nameData=[ " + this.getNameData()
        + " ], phoneData=[ " + this.getPhoneData()
        + " ], relationshipData=[ " + this.getRelationshipData()
        + " ], otherData=[ " + this.getOtherData()
        + " ], originalSourceData=[ " + this.getOriginalSourceData()
        + " ]";
  }
}
