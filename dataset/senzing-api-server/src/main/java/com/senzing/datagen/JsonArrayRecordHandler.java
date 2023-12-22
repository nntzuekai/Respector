package com.senzing.datagen;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

import static javax.json.stream.JsonGenerator.*;

/**
 * Implements {@link RecordHandler} to output JSON array text to a {@link
 * Writer}.
 */
public class JsonArrayRecordHandler implements RecordHandler {
  /**
   * The pretty-printing {@link JsonGeneratorFactory}.
   */
  private static JsonGeneratorFactory PRETTY_PRINT_FACTORY
      = Json.createGeneratorFactory(
            Collections.singletonMap(PRETTY_PRINTING, true));

  /**
   * The standard {@link JsonGeneratorFactory}.
   */
  private static JsonGeneratorFactory STANDARD_FACTORY
      = Json.createGeneratorFactory(Collections.emptyMap());

  /**
   * The underlying {@link Writer}.
   */
  private Writer writer;

  /**
   * The {@link JsonGenerator} constructed with the {@link Writer}.
   */
  private JsonGenerator jsonGenerator;

  /**
   * Whether or not pretty printing should be used.
   */
  private boolean prettyPrint;

  /**
   * Constructs with the specified {@link Writer}.
   *
   * @param writer The {@link Writer} to receive the JSON text.
   */
  public JsonArrayRecordHandler(Writer writer)
      throws IOException
  {
    this(writer, true);
  }

  /**
   * Constructs with the specified {@link Writer}.
   *
   * @param writer The {@link Writer} to receive the JSON text.
   * @param prettyPrint <tt>true</tt> if the JSON should be pretty printed,
   *                    and <tt>false</tt> if not.
   */
  public JsonArrayRecordHandler(Writer writer, boolean prettyPrint)
    throws IOException
  {
    this.writer = writer;

    JsonGeneratorFactory factory = (prettyPrint)
        ? PRETTY_PRINT_FACTORY : STANDARD_FACTORY;
    synchronized (factory) {
      this.jsonGenerator = factory.createGenerator(this.writer);
    }

    this.jsonGenerator.writeStartArray();
    this.jsonGenerator.flush();
  }

  /**
   * Handles the generated record described by the specified
   * {@link JsonObject} by streaming another JSON object to the array.
   *
   * @param recordBuilder The {@link JsonObject} describing the record.
   */
  public void handle(JsonObjectBuilder recordBuilder) {
    if (this.jsonGenerator == null) {
      throw new IllegalStateException(
          this.getClass().getSimpleName() + " already closed .");
    }

    // build the object
    JsonObject record = recordBuilder.build();

    // check if empty
    if (record.size() == 0) return;

    // write out the object to the array
    this.jsonGenerator.write(record);
    this.jsonGenerator.flush();
  }

  /**
   * Close out the array, flush the generator and the writer.
   */
  public void close() {
    // end the array and flush
    if (this.jsonGenerator != null) {
      try {
        this.jsonGenerator.writeEnd();
      } catch (Exception ignore) {
        // ignore
      }
      try {
        this.jsonGenerator.flush();
      } catch (Exception ignore) {
        // ignore
      }
    }

    // flush the writer as well for good measure
    if (this.writer != null) {
      try {
        this.writer.flush();
      } catch (Exception e) {
        // ignore
      }
    }
    // close close everything up
    if (this.jsonGenerator != null) {
      try {
        this.jsonGenerator.close();
      } catch (Exception ignore) {
        // do nothing
      }
    }
    this.jsonGenerator = null;

    // close the writer as well for good measure
    if (this.writer != null) {
      try {
        this.writer.close();
      } catch (Exception e) {
        // ignore
      }
    }
    this.writer = null;
  }
}
