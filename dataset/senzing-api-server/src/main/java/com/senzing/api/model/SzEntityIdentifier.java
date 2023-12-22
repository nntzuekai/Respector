package com.senzing.api.model;

/**
 * A tagging interface for entity identifiers.
 */
public interface SzEntityIdentifier {
  /**
   * Implemented to return either an instance of {@link SzRecordId}
   * or {@link SzEntityId}.
   *
   * @param text The text to parse.
   *
   * @return The {@link SzEntityIdentifier} for the specified text.
   */
  static SzEntityIdentifier valueOf(String text) {
    if (text.matches("-?[\\d]+")) {
      return SzEntityId.valueOf(text);
    } else {
      return SzRecordId.valueOf(text);
    }
  }
}
