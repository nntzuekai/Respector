package com.senzing.datagen;

import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Implements {@link RecordHandler} to output "JSON Lines" text to a {@link
 * PrintWriter}.
 */
public class JsonLinesRecordHandler implements RecordHandler {
  /**
   * The underlying {@link Writer}.
   */
  private PrintWriter writer;

  /**
   * Constructs with the specified {@link Writer}.
   *
   * @param writer The {@link Writer} to receive the JSON text.
   */
  public JsonLinesRecordHandler(Writer writer)
    throws IOException
  {
    this(new PrintWriter(writer));
  }

  /**
   * Constructs with the specified {@link Writer}.
   *
   * @param writer The {@link Writer} to receive the JSON text.
   */
  public JsonLinesRecordHandler(PrintWriter writer)
      throws IOException
  {
    this.writer = writer;
  }

  /**
   * Handles the generated record described by the specified
   * {@link JsonObject} by adding a line of output to the underlying writer.
   *
   * @param recordBuilder The {@link JsonObject} describing the record.
   */
  public void handle(JsonObjectBuilder recordBuilder) {
    if (this.writer == null) {
      throw new IllegalStateException(
          this.getClass().getSimpleName() + " already closed .");
    }

    // build the object
    JsonObject record = recordBuilder.build();

    // check if empty
    if (record.size() == 0) return;

    // get the JSON text WITHOUT pretty printing
    String jsonText = JsonUtilities.toJsonText(record, false);

    // write the line
    this.writer.println(jsonText);

    // flush
    this.writer.flush();
  }

  /**
   * Close out the array, flush the generator and the writer.
   */
  public void close() {
    // flush the writer
    if (this.writer != null) {
      try {
        this.writer.flush();
      } catch (Exception ignore) {
        // ignore
      }
      try {
        this.writer.close();
      } catch (Exception ignore) {
        // ignore
      }
    }
    this.writer = null;
  }
}
