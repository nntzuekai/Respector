package com.senzing.api.model;

import com.senzing.util.JsonUtilities;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public interface SzJsonConvertible {
  /**
   * Adds the JSON properties to the specified {@link JsonObjectBuilder} to
   * describe this instance in its standard JSON format.
   *
   * @param builder The {@link JsonObjectBuilder} to add the properties.
   */
  void buildJson(JsonObjectBuilder builder);

  /**
   * Converts this object to its standard JSON representation as a
   * {@link JsonObject} instance.
   *
   * @return The {@link JsonObject} describing this instance.
   */
  default JsonObject toJsonObject() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    this.buildJson(builder);
    return builder.build();
  }

  /**
   * Converts this object to its standard JSON representation as JSON text.
   *
   * @return The {@link String} text representation of this object as JSON.
   */
  default String toJson() {
    return JsonUtilities.toJsonText(this.toJsonObject());
  }

  /**
   * Adds the JSON properties to the specified {@link JsonObjectBuilder} to
   * describe this instance as native Senzing JSON.
   *
   * @param builder The {@link JsonObjectBuilder} to add the properties.
   */
  void buildNativeJson(JsonObjectBuilder builder);

  /**
   * Converts this object to its native Senzing JSON represenation as a
   * {@link JsonObject} instance.
   *
   * @return The {@link JsonObject} describing this instance as its
   *         native Senzing JSON representation.
   */
  default JsonObject toNativeJsonObject() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    this.buildNativeJson(builder);
    return builder.build();
  }

  /**
   * Converts this object to its native Senzing JSON representation as
   * JSON text.
   *
   * @return The {@link String} text representation of this object as native
   *         Senzing JSON.
   */
  default String toNativeJson() {
    return JsonUtilities.toJsonText(this.toNativeJsonObject());
  }
}
