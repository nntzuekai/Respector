package com.senzing.api.services;

import com.senzing.nativeapi.NativeApiFactory;
import com.senzing.api.server.SzApiServer;
import com.senzing.api.server.SzApiServerOptions;
import com.senzing.g2.engine.G2ConfigMgr;
import com.senzing.g2.engine.Result;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import static com.senzing.util.LoggingUtilities.formatError;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static com.senzing.io.IOUtilities.*;

@TestInstance(Lifecycle.PER_CLASS)
public class ConfigServicesExplicitConfigIdTest extends ConfigServicesReadOnlyTest
{
  /**
   * Sets the desired options for the {@link SzApiServer} during server
   * initialization.
   *
   * @param options The {@link SzApiServerOptions} to initialize.
   */
  protected void initializeServerOptions(SzApiServerOptions options) {
    super.initializeServerOptions(options);
    options.setAdminEnabled(true);
    if (this.checkNativeApiAvailable()) {
      G2ConfigMgr configMgrApi = NativeApiFactory.createConfigMgrApi();
      boolean     initialized  = false;
      try {
        File initJsonFile = new File(this.getRepositoryDirectory(),
                                     "g2-init.json");

        String initJson = readTextFileAsString(initJsonFile, "UTF-8");
        configMgrApi.init(this.getModuleName("RepoMgr (reconfigure)"),
                          initJson,
                          this.isVerbose());
        Result<Long> result = new Result<>();
        int returnCode = configMgrApi.getDefaultConfigID(result);
        if (returnCode != 0) {
          fail(formatError("G2ConfigMgr.getDefaultConfigID",
                           configMgrApi));
        }
        initialized = true;
        options.setConfigurationId(result.getValue());

      } catch (IOException e) {
        throw new RuntimeException(e);

      } finally {
        if (initialized) configMgrApi.destroy();
      }
    }
  }
}
