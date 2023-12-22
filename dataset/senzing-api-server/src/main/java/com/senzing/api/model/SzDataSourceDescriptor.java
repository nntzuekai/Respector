package com.senzing.api.model;

/**
 * A tagging interface for entity identifiers.
 */
public interface SzDataSourceDescriptor extends SzJsonConvertible {
  /**
   * Implemented to return either an instance of {@link SzDataSourceCode}
   * or {@link SzDataSource}.
   *
   * @param text The text to parse.
   *
   * @return The {@link SzDataSourceDescriptor} for the specified text.
   */
  static SzDataSourceDescriptor valueOf(String text) {
    text = text.trim();
    if (text.length() > 2 && text.startsWith("{") && text.endsWith("}")) {
      return SzDataSource.valueOf(text);
    } else {
      return SzDataSourceCode.valueOf(text);
    }
  }

  /**
   * Converts this instance to an instance of {@link SzDataSource}
   * which completely describes a data source.
   *
   * @return The {@link SzDataSource} describing the data source.
   */
  SzDataSource toDataSource();
}
