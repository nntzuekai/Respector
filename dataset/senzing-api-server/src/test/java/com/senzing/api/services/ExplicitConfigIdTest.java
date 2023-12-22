package com.senzing.api.services;

import com.senzing.api.model.SzConfigResponse;
import com.senzing.api.model.SzDataSourcesResponse;
import com.senzing.api.model.SzErrorResponse;
import com.senzing.api.server.SzApiServer;
import com.senzing.api.server.SzApiServerOptions;
import com.senzing.g2.engine.*;
import com.senzing.util.JsonUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;

import static com.senzing.api.model.SzHttpMethod.*;
import static com.senzing.util.LoggingUtilities.formatError;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static com.senzing.api.services.ResponseValidators.*;

@TestInstance(Lifecycle.PER_CLASS)
public class ExplicitConfigIdTest extends AutoReinitializeTest
{
  /**
   * Sets the desired options for the {@link SzApiServer} during server
   * initialization.
   *
   * @param options The {@link SzApiServerOptions} to initialize.
   */
  protected void initializeServerOptions(SzApiServerOptions options) {
    super.initializeServerOptions(options);
    if (this.checkNativeApiAvailable()) {
      Result<Long> result = new Result<>();
      int returnCode = this.configMgrApi.getDefaultConfigID(result);
      if (returnCode != 0) {
        fail(formatError("G2ConfigMgr.getDefaultConfigID",
                         this.configMgrApi));
      }
      options.setConfigurationId(result.getValue());
    }
  }

  @Test public void getDataSourcesTest() {
    this.performTest(() -> {
      final String newDataSource = "FOO";
      String  uriText = this.formatServerUri("data-sources");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      this.addDataSource(newDataSource);

      // now request a config refresh check
      this.requestConfigRefreshCheck();

      // now retry the request to get the data sources
      long before = System.nanoTime();
      SzDataSourcesResponse response
          = this.configServices.getDataSources(false, uriInfo);
      response.concludeTimers();
      long after = System.nanoTime();

      synchronized (this.expectedDataSources) {
        validateDataSourcesResponse(response,
                                    GET,
                                    uriText,
                                    after - before,
                                    false,
                                    INITIAL_DATA_SOURCES);
      }
    });
  }

  @Test public void getActiveConfigTest() {
    this.performTest(() -> {
      final String newDataSource = "PHOO";
      String  uriText = this.formatServerUri("config/active");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      this.addDataSource(newDataSource);

      // now request a config refresh check
      this.requestConfigRefreshCheck();

      long before = System.nanoTime();
      SzConfigResponse response
          = this.configServices.getActiveConfig(uriInfo);
      response.concludeTimers();
      long after = System.nanoTime();

      synchronized (this.expectedDataSources) {
        validateConfigResponse(response,
                               uriText,
                               after - before,
                               INITIAL_DATA_SOURCES.keySet());
      }
    });
  }


  @Test public void postRecordTest() {
    this.performTest(() -> {
      final String newDataSource = "FOOX";
      String  uriText = this.formatServerUri(
          "data-sources/" + newDataSource + "/records");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "Joe");
      job.add("NAME_LAST", "Schmoe");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      // add the data source (so it is there for retry)
      this.addDataSource(newDataSource);

      // now add the record -- this should succeed on retry
      long before = System.nanoTime();
      try {
        this.entityDataServices.loadRecord(
            newDataSource, null, false, false, uriInfo, jsonText);
        fail("Expected for data source \"" + newDataSource
                 + "\" to trigger a NotFoundException");
      } catch (NotFoundException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();

        validateBasics(response, 404, POST, uriText, after - before);
      }
    });
  }

  @Test public void putRecordTest() {
    this.performTest(() -> {
      final String recordId = "ABC123";
      final String newDataSource = "PHOOX";

      String  uriText = this.formatServerUri(
          "data-sources/" + newDataSource + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "John");
      job.add("NAME_LAST", "Doe");
      job.add("PHONE_NUMBER", "818-555-1313");
      job.add("ADDR_FULL", "100 Main Street, Los Angeles, CA 90012");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      // add the data source (so it is there for retry)
      this.addDataSource(newDataSource);

      // now add the record -- this should succeed on retry
      long before = System.nanoTime();
      try {
        this.entityDataServices.loadRecord(
            newDataSource, recordId, null, false, false, uriInfo, jsonText);
        fail("Expected for data source \"" + newDataSource
                 + "\" to trigger a NotFoundException");
      } catch (NotFoundException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();

        validateBasics(response, 404, PUT, uriText, after - before);
      }
    });
  }
}
