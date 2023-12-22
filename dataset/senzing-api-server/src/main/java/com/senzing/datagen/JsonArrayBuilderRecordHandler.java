package com.senzing.datagen;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 * A {@link RecordHandler} implementation that accumulates the records into
 * a {@link JsonArrayBuilder}.
 */
public class JsonArrayBuilderRecordHandler {
  /**
   * The {@link JsonArrayBuilder} to use.
   */
  private JsonArrayBuilder arrayBuilder;

  /**
   * Constructs with the specified {@link JsonArrayBuilder}.
   *
   * @param builder The {@link JsonArrayBuilder} to use.
   */
  public JsonArrayBuilderRecordHandler(JsonArrayBuilder builder) {
    this.arrayBuilder = builder;
  }

  /**
   * Constructs with a newly constructed {@link JsonArrayBuilder}.
   */
  public JsonArrayBuilderRecordHandler() {
    this(Json.createArrayBuilder());
  }

  /**
   * Handles the record by adding it to the {@link JsonArrayBuilder}.
   *
   * @param recordBuilder The {@link JsonObjectBuilder} describing the record.
   */
  public void handle(JsonObjectBuilder recordBuilder) {
    this.arrayBuilder.add(recordBuilder);
  }

  /**
   * Implemnented to do nothing.
   */
  public void close() {
    // do nothing
  }
}
