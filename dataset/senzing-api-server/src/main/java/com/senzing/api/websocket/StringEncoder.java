package com.senzing.api.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class StringEncoder implements Encoder.Text<String> {
  @Override
  public String encode(String text) throws EncodeException {
    return text;
  }

  @Override
  public void init(EndpointConfig config) {
    // do nothing
  }

  @Override
  public void destroy() {
    // do nothing
  }
}
