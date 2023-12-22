package com.senzing.datagen;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * The interface for handling generated records as {@link JsonObjectBuilder}
 * instances.  An implementation of this class may accumulate the records,
 * write them as text to an output stream as JSON or CSV, or may augment the
 * records in some way as well.
 *
 */
public interface RecordHandler extends AutoCloseable {
  /**
   * Handles a generated record that has been generated as part of the specified
   * {@link JsonObject}.
   *
   * @param recordBuilder The {@link JsonObjectBuilder} describing the record.
   */
  void handle(JsonObjectBuilder recordBuilder);

  /**
   * Completes the handling of the records.
   */
  void close();
}
