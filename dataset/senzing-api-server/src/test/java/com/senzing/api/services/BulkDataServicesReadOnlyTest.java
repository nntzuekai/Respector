package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.api.server.SzApiServer;
import com.senzing.api.server.SzApiServerOptions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.senzing.api.services.ResponseValidators.validateBasics;
import static org.junit.jupiter.api.Assertions.fail;
import static com.senzing.api.model.SzHttpMethod.POST;
import static com.senzing.api.model.SzHttpMethod.GET;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BulkDataServicesReadOnlyTest extends BulkDataServicesTest {
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


  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  @Override
  public void loadBulkRecordsViaFormTest(
      String              testInfo,
      MediaType mediaType,
      File bulkDataFile,
      SzBulkDataAnalysis analysis,
      Map<String,String> dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String  uriText = this.formatServerUri("bulk-data/load");

      MultivaluedMap  queryParams       = new MultivaluedHashMap();
      String          mapDataSources    = null;
      List<String>    mapDataSourceList = new LinkedList<>();
      if (dataSourceMap != null) {
        boolean[]         jsonFlag    = { true };
        boolean[]         overlapFlag = { true };
        JsonObjectBuilder builder   = Json.createObjectBuilder();
        dataSourceMap.entrySet().forEach(entry -> {
          String  key   = entry.getKey();
          String  value = entry.getValue();
          if (jsonFlag[0] || overlapFlag[0]) {
            builder.add(key, value);

          } else {
            String mapping = ":" + key + ":" + value;
            mapDataSourceList.add(mapping);
            queryParams.add("mapDataSource", mapping);
            overlapFlag[0] = !overlapFlag[0];
          }
          jsonFlag[0] = !jsonFlag[0];
        });
        JsonObject jsonObject = builder.build();
        if (jsonObject.size() > 0) {
          mapDataSources = jsonObject.toString();
          queryParams.add("mapDataSources", mapDataSources);
        }
      }

      UriInfo uriInfo = this.newProxyUriInfo(uriText, queryParams);
      long before = System.nanoTime();

      try (FileInputStream fis = new FileInputStream(bulkDataFile)) {
        SzBulkLoadResponse response
            = this.bulkDataServices.loadBulkRecordsViaForm(
            CONTACTS_DATA_SOURCE,
            mapDataSources,
            mapDataSourceList,
            null,
            0,
            mediaType,
            fis,
            null,
            uriInfo);

        fail("Expected bulk load to be forbidden, but it succeeded.");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(
            testInfo, response, 403, POST, uriText, after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  @Override
  public void loadBulkRecordsViaDirectHttpTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  analysis,
      Map<String,String>  dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String uriText = this.formatServerUri(formatLoadURL(
          CONTACTS_DATA_SOURCE, null, null,
          dataSourceMap, null));

      try (FileInputStream fis = new FileInputStream(bulkDataFile)) {
        long before = System.nanoTime();
        SzErrorResponse response = this.invokeServerViaHttp(
            POST, uriText, null, mediaType.toString(),
            bulkDataFile.length(), new FileInputStream(bulkDataFile),
            SzErrorResponse.class);
        response.concludeTimers();
        long after = System.nanoTime();

        validateBasics(
            testInfo, response, 403, POST, uriText, after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  @Override
  public void loadBulkRecordsDirectJavaClientTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  analysis,
      Map<String,String>  dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String uriText = this.formatServerUri(formatLoadURL(
          CONTACTS_DATA_SOURCE, null, null,
          dataSourceMap, null));

      try (FileInputStream fis = new FileInputStream(bulkDataFile)) {
        long before = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = this.invokeServerViaHttp(
            POST, uriText, null, mediaType.toString(),
            bulkDataFile.length(), new FileInputStream(bulkDataFile),
            com.senzing.gen.api.model.SzErrorResponse.class);
        long after = System.nanoTime();

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(
            testInfo, response, 403, POST, uriText, after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  @Override
  public void loadBulkRecordsViaWebSocketsTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  analysis,
      Map<String,String>  dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String uriText = this.formatServerUri(formatLoadURL(
          CONTACTS_DATA_SOURCE, null, null,
          dataSourceMap, null));
      uriText = uriText.replaceAll("^http:(.*)", "ws:$1");

      BulkDataWebSocketClient client = null;
      try {
        client = new BulkDataWebSocketClient(bulkDataFile, mediaType);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(client, URI.create(uriText));

        fail("Successfully connected to web socket for bulk load when started "
             + "in read-only mode: " + testInfo);

      } catch (Exception expected) {
        if (client != null) {
          Object next = client.getNextResponse();
          if (next != null) {
            if (! (next instanceof Throwable)) {
              fail("Expected failure on Web Socket connection to read-only "
                   + "server, but got a non-failure response instead: " + next);
            }
            Throwable throwable = (Throwable) next;
            String message = throwable.getMessage();
            if (!message.contains("403")) {
              fail("Got an exception on Web Socket connection to "
                   + "read-only server, but it was not a 403 failure",
                   throwable);
            }
          }
        }
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  @Override
  public void loadBulkRecordsViaSSETest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  analysis,
      Map<String,String>  dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String uriText = this.formatServerUri(formatLoadURL(
          CONTACTS_DATA_SOURCE, null, null,
          dataSourceMap, null));

      try {
        long before = System.nanoTime();

        URL url = new URL(uriText);

        BulkDataSSEClient client = new BulkDataSSEClient(url,
                                                         bulkDataFile,
                                                         mediaType);

        client.start();

        SzErrorResponse errorResponse = null;
        // grab the results
        for (Object next = client.getNextResponse();
             next != null;
             next = client.getNextResponse())
        {
          // check if there was a failure
          if (next instanceof Throwable) {
            ((Throwable)next).printStackTrace();
            fail((Throwable) next);
          }

          // get as a string
          String jsonText = next.toString();
          if (jsonText.matches(".*\"httpStatusCode\":\\s*200.*") ) {
            fail("Received 200 response for read-only: " + jsonText);

          } else {
            errorResponse = jsonParse(jsonText, SzErrorResponse.class);
            errorResponse.concludeTimers();
            break;
          }
        }
        long after = System.nanoTime();

        if (errorResponse == null) {
          fail("Did not receive an error response for SSE bulk-load test "
               + "against a read-only API Server: " + testInfo);
        }

        validateBasics(
            testInfo, errorResponse, 403, POST,
            uriText, after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getMaxFailureArgs")
  @Override
  public void testMaxFailuresOnLoad(
      int                   recordCount,
      Integer               maxFailures,
      SzBulkDataStatus expectedStatus,
      Map<String, Integer>  failuresByDataSource,
      File                  dataFile)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String testInfo = "recordCount=[ " + recordCount + " ], maxFailures=[ "
          + maxFailures + " ], status=[ "
          + expectedStatus + " ], failuresByDataSource=[ "
          + failuresByDataSource + " ], dataFile=[ " + dataFile + " ]";

      String uriText = this.formatServerUri("bulk-data/load");

      MultivaluedMap queryParams = new MultivaluedHashMap();
      if (maxFailures != null) {
        queryParams.add("maxFailures", String.valueOf(maxFailures));
      }
      UriInfo uriInfo = this.newProxyUriInfo(uriText, queryParams);

      long before = System.nanoTime();
      try (InputStream is = new FileInputStream(dataFile);
           BufferedInputStream bis = new BufferedInputStream(is)) {
        this.bulkDataServices.loadBulkRecordsViaForm(
            null,
            null,
            null,
            null,
            maxFailures == null ? -1 : maxFailures,
            MediaType.valueOf("text/plain"),
            bis,
            null,
            uriInfo);

        fail("Expected bulk load to be forbidden, but it succeeded.");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(
            testInfo, response, 403, POST, uriText, after - before);


      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

}
