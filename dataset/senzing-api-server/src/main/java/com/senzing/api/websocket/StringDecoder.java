package com.senzing.api.websocket;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class StringDecoder implements Decoder.Text<String> {
  @Override
  public String decode(String s) throws DecodeException {
    return s;
  }

  @Override
  public boolean willDecode(String s) {
    return (s != null);
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
