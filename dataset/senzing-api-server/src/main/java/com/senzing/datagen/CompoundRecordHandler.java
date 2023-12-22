package com.senzing.datagen;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * Provides an implementation that will delegate to one or more {@link
 * RecordHandler} instances in order.  The delegate {@link RecordHandler}
 * instances can augment the records or transform them into text or accumulate
 * them in some way.
 */
public class CompoundRecordHandler implements RecordHandler {
  /**
   * The {@link List} of {@link RecordHandler} instances to delegate to.
   */
  private List<RecordHandler> delegates;

  /**
   * Constructs with one or more delegate handlers.
   * @param delegateHandlers The {@link RecordHandler} instances to delegate to.
   */
  public CompoundRecordHandler(RecordHandler... delegateHandlers) {
    this(Arrays.asList(delegateHandlers));
  }

  /**
   * Constructs with specified collection of delegate handlers.
   * @param delegateHandlers The {@link RecordHandler} instances to delegate to.
   */
  public CompoundRecordHandler(Collection<RecordHandler> delegateHandlers) {
    this.delegates = Collections.unmodifiableList(
        new ArrayList<>(delegateHandlers));
  }

  /**
   * Implemented to call the {@link RecordHandler#handle(JsonObjectBuilder)}
   * method on each of the delegate {@link RecordHandler} instances in order.
   *
   * @param recordBuilder The {@link JsonObject} describing the record.
   */
  public void handle(JsonObjectBuilder recordBuilder) {
    if (this.delegates == null) {
      throw new IllegalStateException(
          "The RecordHandler has already been closed.");
    }
    JsonObject record = recordBuilder.build();
    for (RecordHandler handler: this.delegates) {
      handler.handle(Json.createObjectBuilder(record));
    }
  }

  /**
   * Implemented to call the {@link RecordHandler#close()} method
   * on each of the delegate {@link RecordHandler} instances in order.
   */
  public void close() {
    for (RecordHandler handler: this.delegates) {
      try {
        handler.close();
      } catch (Exception ignore) {
        // ignore the exception
      }
    }
    this.delegates = null;
  }
}
