package com.senzing.api.services;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describes a Senzing message.
 */
public class SzMessage {
  /**
   * The {@link Map} of message properties.
   */
  private Map<String, String> properties = null;

  /**
   * The message body.
   */
  private String body = null;

  /**
   * Default constructor.
   */
  public SzMessage() {
    this(null);
  }

  /**
   * Constructs with the specified message body.
   */
  public SzMessage(String body) {
    this.body = body;
  }

  /**
   * Gets an <b>unmodifiable</b> {@link Map} of the properties associated with
   * the message.
   *
   * @return An <b>unmodifiable</b> {@link Map} of the properties associated
   *         with the message.
   */
  public Map<String, String> getProperties() {
    if (this.properties == null) return Collections.emptyMap();
    return Collections.unmodifiableMap(this.properties);
  }

  /**
   * Gets the property value associated with the specified property key.  This
   * returns <tt>null</tt> if no property is set for the specified key.
   *
   * @param key The key for the property.
   *
   * @return The property value associated with the key, or <tt>null</tt> if no
   *         property value is set for the specified key.
   */
  public String getProperty(String key) {
    if (this.properties == null) return null;
    return this.properties.get(key);
  }

  /**
   * Sets the property value associated with the specified property key.
   *
   * @param key The key for the property.
   *
   * @param value The value for the property.
   */
  public void setProperty(String key, String value) {
    if (this.properties == null) {
      this.properties = new LinkedHashMap<>();
    }
    this.properties.put(key, value);
  }

  /**
   * Returns the message body associated with this message.
   *
   * @return The message body associated with this message.
   */
  public String getBody() {
    return this.body;
  }

  /**
   * Sets the message body associated with this message.
   *
   * @param body The message body associated with this message.
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Implemented to a return a hash code for this instance.
   *
   * @return A hash code for this instance.
   */
  public int hashCode() {
    return Objects.hash(this.properties, this.body);
  }

  /**
   * Implemented to return <tt>true</tt> if and only if the specified parameter
   * is a non-null reference to an object of the same class with equivalent
   * message properties and message body.
   *
   * @param obj The object to compare against for equality.
   *
   * @return <tt>true</tt> if the objects are equal, otherwise <tt>false</tt>.
   */
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    SzMessage msg = (SzMessage) obj;
    if (!Objects.equals(this.properties, msg.properties)) return false;
    if (!Objects.equals(this.body, msg.body)) return false;
    return true;
  }
  /**
   * Implemented to return a diagnostic {@link String} describing this message.
   *
   * @return A diagnostic {@link String} describing this message.
   */
  public String toString() {
    // if there are then format as JSON
    return ("properties=[ " + this.getProperties()
            + " ], body=[ " + this.getBody() + " ]");
  }
}
