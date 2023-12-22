package com.senzing.api.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Provides an {@link Encoder} implementation that converts an object to
 * JSON text.
 */
public class JsonEncoder implements Encoder.Text<Object> {
  /**
   * The object mapper for this instance.
   */
  private ObjectMapper objectMapper;

  @Override
  public String encode(Object object) throws EncodeException {
    try {
      return this.objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new EncodeException(object, e.getMessage());
    }
  }

  @Override
  public void init(EndpointConfig config) {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JodaModule());
  }

  @Override
  public void destroy() {
    // do nothing
  }
}
