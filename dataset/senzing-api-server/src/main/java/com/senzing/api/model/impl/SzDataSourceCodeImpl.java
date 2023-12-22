package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzDataSourceBulkLoadResult;
import com.senzing.api.model.SzDataSourceCode;

import java.util.Objects;

/**
 * Provides a default implementation of {@link SzDataSourceCode}
 */
@JsonDeserialize
public class SzDataSourceCodeImpl implements SzDataSourceCode {
  /**
   * The data source code that identifiers the data source.
   */
  private String value;

  /**
   * Constructs with the specified data source code.  The specified data
   * source code is trimmed of leading and trailing white space and converted
   * to all upper case.
   *
   * @param code The non-null data source code which will be trimmed and
   *             converted to upper-case.
   *
   * @throws NullPointerException If the specified code is <tt>null</tt>.
   */
  public SzDataSourceCodeImpl(String code)
    throws NullPointerException
  {
    Objects.requireNonNull(code, "The data source code cannot be null");
    this.value = code.trim().toUpperCase();
  }

  /**
   * Return the data source code identifying the data source.
   *
   * @return The data source code identifying the data source.
   */
  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SzDataSourceCodeImpl that = (SzDataSourceCodeImpl) o;
    return this.value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.value);
  }

  @Override
  public String toString() {
    return this.getValue();
  }
}
