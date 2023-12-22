package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzEntityIdentifier;
import com.senzing.api.model.SzRecordId;
import com.senzing.util.JsonUtilities;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides a default implementation of {@link SzRecordId}.
 */
@JsonDeserialize
public class SzRecordIdImpl implements SzRecordId {
  /**
   * The data source code.
   */
  private String dataSourceCode;

  /**
   * The record ID identifying the record within the data source.
   */
  private String recordId;

  /**
   * Default constructor.
   */
  private SzRecordIdImpl() {
    this.dataSourceCode = null;
    this.recordId = null;
  }

  /**
   * Constructs with the specified data source code and record ID.
   *
   * @param dataSourceCode The data source code.
   * @param recordId The record ID identifying the record.
   */
  public SzRecordIdImpl(String dataSourceCode, String recordId) {
    this.dataSourceCode = dataSourceCode.toUpperCase().trim();
    this.recordId = recordId.trim();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDataSourceCode() {
    return dataSourceCode;
  }

  /**
   * Sets the data source code for the record.
   *
   * @param dataSourceCode The data source code for the record.
   */
  private void setDataSourceCode(String dataSourceCode) {
    this.dataSourceCode = dataSourceCode.toUpperCase().trim();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRecordId() {
    return recordId;
  }

  /**
   * Sets the record ID identifying the record.
   *
   * @param recordId The record ID identifying the record.
   */
  private void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SzRecordIdImpl recordId1 = (SzRecordIdImpl) o;
    return Objects.equals(getDataSourceCode(), recordId1.getDataSourceCode()) &&
        Objects.equals(getRecordId(), recordId1.getRecordId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDataSourceCode(), getRecordId());
  }

  @Override
  public String toString() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("src", this.getDataSourceCode());
    builder.add("id", this.getRecordId());
    return JsonUtilities.toJsonText(builder);
  }
}
