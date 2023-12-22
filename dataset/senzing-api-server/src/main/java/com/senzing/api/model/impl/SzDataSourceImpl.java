package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzDataSource;
import java.util.Objects;

/**
 * Provides a default implementation of {@link SzDataSource}.
 */
@JsonDeserialize
public class SzDataSourceImpl implements SzDataSource {
  /**
   * The data source code.
   */
  private String dataSourceCode;

  /**
   * The data source ID.
   */
  private Integer dataSourceId;

  /**
   * Default constructor.
   */
  public SzDataSourceImpl() {
    this.dataSourceCode = null;
    this.dataSourceId   = null;
  }

  /**
   * Constructs with the specified data source code and a <tt>null</tt>
   * data source ID.
   *
   * @param dataSourceCode The data source code for the data source.
   */
  public SzDataSourceImpl(String dataSourceCode) {
    this(dataSourceCode, null);
  }

  /**
   * Constructs with the specified data source code and data source ID.
   *
   * @param dataSourceCode The data source code for the data source.
   * @param dataSourceId The data source ID for the data source, or
   *                     <tt>null</tt> if the data source ID is not
   *                     specified.
   */
  public SzDataSourceImpl(String dataSourceCode, Integer dataSourceId) {
    this.dataSourceCode = dataSourceCode.toUpperCase().trim();
    this.dataSourceId   = dataSourceId;
  }

  /**
   * Gets the data source code for the data source.
   *
   * @return The data source code for the data source.
   */
  @Override
  public String getDataSourceCode() {
    return this.dataSourceCode;
  }

  /**
   * Sets the data source code for the data source.
   *
   * @param code The data source code for the data source.
   */
  @Override
  public void setDataSourceCode(String code) {
    this.dataSourceCode = code;
    if (this.dataSourceCode != null) {
      this.dataSourceCode = this.dataSourceCode.toUpperCase().trim();
    }
  }

  /**
   * Return the data source ID associated with the data source.
   *
   * @return The data source ID associated with the data source.
   */
  @Override
  public Integer getDataSourceId() {
    return this.dataSourceId;
  }

  /**
   * Sets the data source ID associated with the data source.
   *
   * @param dataSourceId The data source ID associated with the data source.
   */
  @Override
  public void setDataSourceId(Integer dataSourceId) {
    this.dataSourceId = dataSourceId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SzDataSourceImpl dataSource = (SzDataSourceImpl) o;
    return Objects.equals(getDataSourceCode(), dataSource.getDataSourceCode())
        && Objects.equals(this.getDataSourceId(), dataSource.getDataSourceId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getDataSourceCode(), this.getDataSourceId());
  }

  @Override
  public String toString() {
    return this.toJson();
  }
}
