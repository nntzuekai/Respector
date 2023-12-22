package com.senzing.api.server;

public interface AppLifeCycle {
  void init() throws Exception;
  void shutdown();
}
