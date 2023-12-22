package com.senzing.api.services;

import com.senzing.api.server.SzApiServer;
import com.senzing.api.server.SzApiServerOptions;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ReadOnlyEntityDataReadServicesTest
    extends EntityDataReadServicesTest
{
  /**
   * Sets the desired options for the {@link SzApiServer} during server
   * initialization.
   *
   * @param options The {@link SzApiServerOptions} to initialize.
   */
  protected void initializeServerOptions(SzApiServerOptions options) {
    super.initializeServerOptions(options);
    options.setReadOnly(true);
  }
}
