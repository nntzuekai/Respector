package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.gen.api.invoker.ApiClient;
import com.senzing.gen.api.services.EntityDataApi;
import com.senzing.repomgr.RepositoryManager;
import com.senzing.util.JsonUtilities;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.client.HttpStatusCodeException;

import javax.json.*;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.UriInfo;

import java.util.*;

import static com.senzing.api.model.SzDetailLevel.*;
import static com.senzing.api.model.SzFeatureMode.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static com.senzing.api.model.SzHttpMethod.*;
import static com.senzing.api.services.ResponseValidators.*;
import static com.senzing.repomgr.RepositoryManager.Configuration;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
public class EntityDataWriteServicesTest extends AbstractServiceTest {
  protected static final String CUSTOMER_DATA_SOURCE = "CUSTOMERS";
  protected static final String WATCHLIST_DATA_SOURCE = "WATCHLIST";
  protected static final String WATCHLIST_FLAG = "WATCHLIST-THREAT";

  protected EntityDataServices entityDataServices;
  protected EntityDataApi entityDataApi;

  @BeforeAll public void initializeEnvironment() {
    this.beginTests();
    this.initializeTestEnvironment();
    this.entityDataServices = new EntityDataServices();
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(this.formatServerUri(""));
    this.entityDataApi = new EntityDataApi(apiClient);
  }

  @BeforeEach public void preTestPurge() {
    this.livePurgeRepository();
  }

  /**
   * Overridden to configure some data sources.
   */
  protected void prepareRepository() {
    Configuration config = RepositoryManager.configSources(
        this.getRepositoryDirectory(),
        Set.of(CUSTOMER_DATA_SOURCE, WATCHLIST_DATA_SOURCE),
        true);

    // get the data source ID for the watch list
    JsonObject g2Config = config.getConfigJson().getJsonObject("G2_CONFIG");
    JsonArray cfgDsrc = g2Config.getJsonArray("CFG_DSRC");
    Integer watchListId = null;
    for (JsonObject dsrcObj : cfgDsrc.getValuesAs(JsonObject.class)) {
      if (dsrcObj.getString("DSRC_CODE").equals(WATCHLIST_DATA_SOURCE)) {
        watchListId = dsrcObj.getInt("DSRC_ID");
        break;
      }
    }

    // configure the flags for interesting entities
    JsonObjectBuilder g2ConfigJob = Json.createObjectBuilder(g2Config);
    g2ConfigJob.remove("CFG_DSRC_INTEREST");
    JsonArrayBuilder  jab = Json.createArrayBuilder();
    JsonObjectBuilder job = Json.createObjectBuilder();
    job.add("DSRC_ID", watchListId);
    job.add("MAX_DEGREE", 2);
    job.add("INTEREST_FLAG", WATCHLIST_FLAG);
    jab.add(job);
    g2ConfigJob.add("CFG_DSRC_INTEREST", jab);
    JsonObjectBuilder configJob = Json.createObjectBuilder();
    configJob.add("G2_CONFIG", g2ConfigJob);
    JsonObject configJson = configJob.build();

    // update the config
    RepositoryManager.updateConfig(this.getRepositoryDirectory(),
                                   configJson,
                                   "Added interesting entity flags",
                                   false);
  }

  @AfterAll public void teardownEnvironment() {
    try {
      this.teardownTestEnvironment();
      this.conditionallyLogCounts(true);
    } finally {
      this.endTests();
    }
  }

  @Test public void postRecordTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "Joe");
      job.add("NAME_LAST", "Schmoe");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      SzLoadRecordResponse response = this.entityDataServices.loadRecord(
          CUSTOMER_DATA_SOURCE, null, false, false, uriInfo, jsonText);
      response.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 POST,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 null,
                                 false,
                                 false,
                                 0,
                                 0,
                                 Collections.emptySet(),
                                 after - before);
    });
  }

  @Test public void postRecordViaHttpTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      Map recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joanne");
      recordBody.put("NAME_LAST", "Smith");
      recordBody.put("PHONE_NUMBER", "212-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      SzLoadRecordResponse response = this.invokeServerViaHttp(
          POST, uriText, null, recordBody, SzLoadRecordResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 POST,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 null,
                                 false,
                                 false,
                                 0,
                                 0,
                                 Collections.emptySet(),
                                 after - before);
    });
  }

  @Test public void postMismatchedDataSourceTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      job.add("NAME_FIRST", "John");
      job.add("NAME_LAST", "Doe");
      job.add("PHONE_NUMBER", "818-555-1313");
      job.add("ADDR_FULL", "100 Main Street, Los Angeles, CA 90012");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      try {
        this.entityDataServices.loadRecord(CUSTOMER_DATA_SOURCE,
                                           null,
                                           false,
                                           false,
                                           uriInfo,
                                           jsonText);

        fail("Expected BadRequestException for mismatched DATA_SOURCE");

      } catch (BadRequestException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 400, POST, uriText, after - before);
      }
    });
  }

  @Test public void postMismatchedDataSourceViaHttpTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("NAME_FIRST", "Jane");
      recordBody.put("NAME_LAST", "Doe");
      recordBody.put("PHONE_NUMBER", "818-555-1212");
      recordBody.put("ADDR_FULL", "500 First Street, Los Angeles, CA 90033");

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          POST, uriText, null, recordBody, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response,
                     400,
                     POST,
                     uriText,
                     after - before);
    });
  }

  protected List<Arguments> withInfoParams() {
    List<Arguments> argumentsList = new LinkedList<>();
    Boolean[] booleans = { null, Boolean.TRUE, Boolean.FALSE };
    for (Boolean withInfo : booleans) {
      for (Boolean withRaw : booleans) {
        argumentsList.add(arguments(withInfo, withRaw));
      }
    }
    return argumentsList;
  }

  protected List<Arguments> postWithInfoParams() {
    List<Arguments> argumentsList = new LinkedList<>();
    Boolean[] booleans = { null, Boolean.TRUE, Boolean.FALSE };
    String[] recordIds = { "ABC123", "", "  ", null };
    for (String recordId : recordIds) {
     for (Boolean withInfo : booleans) {
       for (Boolean withRaw : booleans) {
         argumentsList.add(arguments(recordId, withInfo, withRaw));
       }
     }
    }
    return argumentsList;
  }

  @ParameterizedTest
  @MethodSource("postWithInfoParams")
  public void postRecordWithInfoTest(String   recordId,
                                     Boolean  withInfo,
                                     Boolean  withRaw)
  {
    this.performTest(() -> {
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records",
          queryParams);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      if (recordId != null) job.add("RECORD_ID", recordId);
      job.add("NAME_FIRST", "James");
      job.add("NAME_LAST", "Moriarty");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      SzLoadRecordResponse response = this.entityDataServices.loadRecord(
          WATCHLIST_DATA_SOURCE,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          jsonText);
      response.concludeTimers();
      long after = System.nanoTime();

      String expectedRecordId = null;
      if (recordId != null && recordId.trim().length() > 0) {
        expectedRecordId = recordId;
      }

      validateLoadRecordResponse(response,
                                 POST,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 expectedRecordId,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records",
          queryParams);
      uriInfo = this.newProxyUriInfo(uriText);

      job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "Joe");
      job.add("NAME_LAST", "Schmoe");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      jsonObject  = job.build();
      jsonText    = JsonUtilities.toJsonText(jsonObject);

      before = System.nanoTime();
      response = this.entityDataServices.loadRecord(
          CUSTOMER_DATA_SOURCE,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          jsonText);
      response.concludeTimers();
      after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 POST,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 null,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("postWithInfoParams")
  public void postRecordWithInfoViaHttpTest(String  recordId,
                                            Boolean withInfo,
                                            Boolean withRaw)
  {
    this.performTest(() -> {
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records",
          queryParams);

      Map recordBody = new HashMap();
      if (recordId != null) recordBody.put("RECORD_ID", recordId);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      SzLoadRecordResponse response = this.invokeServerViaHttp(
          POST, uriText, null, recordBody, SzLoadRecordResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      String expectedRecordId = null;
      if (recordId != null && recordId.trim().length() > 0) {
        expectedRecordId = recordId;
      }

      validateLoadRecordResponse(response,
                                 POST,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 expectedRecordId,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records",
          queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      response = this.invokeServerViaHttp(
          POST, uriText, null, recordBody, SzLoadRecordResponse.class);
      response.concludeTimers();
      after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 POST,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 null,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("postWithInfoParams")
  public void postRecordWithInfoViaJavaClientTest(String  recordId,
                                                  Boolean withInfo,
                                                  Boolean withRaw)
  {
    this.performTest(() -> {
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records",
          queryParams);

      Map recordBody = new HashMap();
      if (recordId != null) recordBody.put("RECORD_ID", recordId);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzLoadRecordResponse clientResponse
          = this.entityDataApi.addRecordWithReturnedRecordId(recordBody,
                                                             WATCHLIST_DATA_SOURCE,
                                                             null,
                                                             withInfo,
                                                             withRaw);
      long after = System.nanoTime();

      SzLoadRecordResponse response = jsonCopy(clientResponse,
                                               SzLoadRecordResponse.class);

      String expectedRecordId = null;
      if (recordId != null && recordId.trim().length() > 0) {
        expectedRecordId = recordId;
      }

      validateLoadRecordResponse(response,
                                 POST,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 expectedRecordId,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records",
          queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      clientResponse = this.entityDataApi.addRecordWithReturnedRecordId(
          recordBody, CUSTOMER_DATA_SOURCE, null, withInfo, withRaw);
      after = System.nanoTime();

      response = jsonCopy(clientResponse, SzLoadRecordResponse.class);

      validateLoadRecordResponse(response,
                                 POST,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 null,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);
    });
  }

  @Test
  public void putRecordTest()
  {
    this.performTest(() -> {
      final String recordId = "ABC123";

      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "John");
      job.add("NAME_LAST", "Doe");
      job.add("PHONE_NUMBER", "818-555-1313");
      job.add("ADDR_FULL", "100 Main Street, Los Angeles, CA 90012");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      SzLoadRecordResponse response = this.entityDataServices.loadRecord(
          CUSTOMER_DATA_SOURCE,
          recordId,
          null,
          false,
          false,
          uriInfo,
          jsonText);
      response.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId,
                                 false,
                                 false,
                                 0,
                                 0,
                                 Collections.emptySet(),
                                 after - before);
    });
  }

  @Test public void putRecordViaHttpTest() {
    this.performTest(() -> {
      final String recordId = "XYZ456";

      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      Map recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Jane");
      recordBody.put("NAME_LAST", "Doe");
      recordBody.put("PHONE_NUMBER", "818-555-1212");
      recordBody.put("ADDR_FULL", "500 First Street, Los Angeles, CA 90033");

      long before = System.nanoTime();
      SzLoadRecordResponse response = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzLoadRecordResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId,
                                 false,
                                 false,
                                 0,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

    });
  }

  @Test public void putMismatchedRecordTest() {
    this.performTest(() -> {
      final String recordId = "ABC123";

      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("RECORD_ID", "DEF456");
      job.add("NAME_FIRST", "John");
      job.add("NAME_LAST", "Doe");
      job.add("PHONE_NUMBER", "818-555-1313");
      job.add("ADDR_FULL", "100 Main Street, Los Angeles, CA 90012");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      try {
        this.entityDataServices.loadRecord(CUSTOMER_DATA_SOURCE,
                                           recordId,
                                           null,
                                           false,
                                           false,
                                           uriInfo,
                                           jsonText);

        fail("Expected BadRequestException for mismatched RECORD_ID");

      } catch (BadRequestException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 400, PUT, uriText, after - before);
      }
    });
  }

  @Test public void putMismatchedRecordViaHttpTest() {
    this.performTest(() -> {
      final String recordId = "XYZ456";

      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      Map recordBody = new HashMap();
      recordBody.put("RECORD_ID", "DEF456");
      recordBody.put("NAME_FIRST", "Jane");
      recordBody.put("NAME_LAST", "Doe");
      recordBody.put("PHONE_NUMBER", "818-555-1212");
      recordBody.put("ADDR_FULL", "500 First Street, Los Angeles, CA 90033");

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response,
                     400,
                     PUT,
                     uriText,
                     after - before);
    });
  }

  @Test public void putMismatchedRecordViaJavaClientTest() {
    this.performTest(() -> {
      final String recordId = "XYZ456";

      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      Map recordBody = new HashMap();
      recordBody.put("RECORD_ID", "DEF456");
      recordBody.put("NAME_FIRST", "Jane");
      recordBody.put("NAME_LAST", "Doe");
      recordBody.put("PHONE_NUMBER", "818-555-1212");
      recordBody.put("ADDR_FULL", "500 First Street, Los Angeles, CA 90033");

      long before = System.nanoTime();
      try {
        com.senzing.gen.api.model.SzLoadRecordResponse clientResponse
            = this.entityDataApi.addRecord(recordBody,
                                           CUSTOMER_DATA_SOURCE,
                                           recordId,
                                           null,
                                           null,
                                           null);

        fail("Expected failure, but got success for mismatched record: "
                 + "dataSource=[ " + CUSTOMER_DATA_SOURCE
                 + " ], urlRecordId=[ " + recordId
                 + " ], bodyRecordId=[ DEF456 ], response=[ "
                 + clientResponse + " ]");

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       400,
                       PUT,
                       uriText,
                       after - before);
      }
    });
  }

  @Test public void putMismatchedDataSourceTest() {
    this.performTest(() -> {
      final String recordId = "ABC123";

      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      job.add("NAME_FIRST", "John");
      job.add("NAME_LAST", "Doe");
      job.add("PHONE_NUMBER", "818-555-1313");
      job.add("ADDR_FULL", "100 Main Street, Los Angeles, CA 90012");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      try {
        this.entityDataServices.loadRecord(CUSTOMER_DATA_SOURCE,
                                           recordId,
                                           null,
                                           false,
                                           false,
                                           uriInfo,
                                           jsonText);

        fail("Expected BadRequestException for mismatched DATA_SOURCE");

      } catch (BadRequestException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 400, PUT, uriText, after - before);
      }
    });
  }

  @Test public void putMismatchedDataSourceViaHttpTest() {
    this.performTest(() -> {
      final String recordId = "XYZ456";

      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("NAME_FIRST", "Jane");
      recordBody.put("NAME_LAST", "Doe");
      recordBody.put("PHONE_NUMBER", "818-555-1212");
      recordBody.put("ADDR_FULL", "500 First Street, Los Angeles, CA 90033");

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response,
                     400,
                     PUT,
                     uriText,
                     after - before);
    });
  }

  @Test public void putMismatchedDataSourceViaJavaClientTest() {
    this.performTest(() -> {
      final String recordId = "XYZ456";

      String  uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("NAME_FIRST", "Jane");
      recordBody.put("NAME_LAST", "Doe");
      recordBody.put("PHONE_NUMBER", "818-555-1212");
      recordBody.put("ADDR_FULL", "500 First Street, Los Angeles, CA 90033");

      long before = System.nanoTime();
      try {
        com.senzing.gen.api.model.SzLoadRecordResponse clientResponse
            = this.entityDataApi.addRecord(recordBody,
                                           CUSTOMER_DATA_SOURCE,
                                           recordId,
                                           null,
                                           null,
                                           null);

        fail("Expected failure, but got success for mismatched data source: "
                 + "urlDataSource=[ " + CUSTOMER_DATA_SOURCE
                 + " ], bodyDataSource=[ " + WATCHLIST_DATA_SOURCE
                 + " ], response=[ "
                 + clientResponse + " ]");

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       400,
                       PUT,
                       uriText,
                       after - before);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void putRecordWithInfoTest(Boolean  withInfo,
                                    Boolean  withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
          + recordId1, queryParams);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      job.add("RECORD_ID", recordId1);
      job.add("NAME_FIRST", "James");
      job.add("NAME_LAST", "Moriarty");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      SzLoadRecordResponse response = this.entityDataServices.loadRecord(
          WATCHLIST_DATA_SOURCE,
          recordId1,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          jsonText);
      response.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
          + recordId2, queryParams);
      uriInfo = this.newProxyUriInfo(uriText);

      job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "Joe");
      job.add("NAME_LAST", "Schmoe");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      jsonObject  = job.build();
      jsonText    = JsonUtilities.toJsonText(jsonObject);

      before = System.nanoTime();
      response = this.entityDataServices.loadRecord(
          CUSTOMER_DATA_SOURCE,
          recordId2,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          jsonText);
      response.concludeTimers();
      after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void putRecordWithInfoViaHttpTest(Boolean withInfo,
                                           Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
          + recordId1, queryParams);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("RECORD_ID", recordId1);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      SzLoadRecordResponse response = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzLoadRecordResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
          + recordId2, queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      response = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzLoadRecordResponse.class);
      response.concludeTimers();
      after = System.nanoTime();

      validateLoadRecordResponse(response,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void putRecordWithInfoViaJavaClientTest(Boolean withInfo,
                                                 Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("RECORD_ID", recordId1);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzLoadRecordResponse clientResponse
          = this.entityDataApi.addRecord(recordBody,
                                         WATCHLIST_DATA_SOURCE,
                                         recordId1,
                                         null,
                                         withInfo,
                                         withRaw);
      long after = System.nanoTime();

      SzLoadRecordResponse response = jsonCopy(clientResponse,
                                               SzLoadRecordResponse.class);

      validateLoadRecordResponse(response,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      clientResponse = this.entityDataApi.addRecord(recordBody,
                                                    CUSTOMER_DATA_SOURCE,
                                                    recordId2,
                                                    null,
                                                    withInfo,
                                                    withRaw);
      after = System.nanoTime();

      response = jsonCopy(clientResponse, SzLoadRecordResponse.class);

      validateLoadRecordResponse(response,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void reevaluateRecordTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      job.add("RECORD_ID", recordId1);
      job.add("NAME_FIRST", "James");
      job.add("NAME_LAST", "Moriarty");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      SzLoadRecordResponse loadResponse = this.entityDataServices.loadRecord(
          WATCHLIST_DATA_SOURCE,
          recordId1,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          jsonText);
      loadResponse.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE
              + "/records/" + recordId1 + "/reevaluate", queryParams);
      uriInfo = this.newProxyUriInfo(uriText);

      before = System.nanoTime();
      SzReevaluateResponse response = this.entityDataServices.reevaluateRecord(
          WATCHLIST_DATA_SOURCE,
          recordId1,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo);
      response.concludeTimers();
      after = System.nanoTime();

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);
      uriInfo = this.newProxyUriInfo(uriText);

      job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "Joe");
      job.add("NAME_LAST", "Schmoe");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      jsonObject  = job.build();
      jsonText    = JsonUtilities.toJsonText(jsonObject);

      before = System.nanoTime();
      loadResponse = this.entityDataServices.loadRecord(
          CUSTOMER_DATA_SOURCE,
          recordId2,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          jsonText);
      loadResponse.concludeTimers();
      after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE
              + "/records/" + recordId2 + "/reevaluate", queryParams);
      uriInfo = this.newProxyUriInfo(uriText);

      before = System.nanoTime();
      response = this.entityDataServices.reevaluateRecord(
          CUSTOMER_DATA_SOURCE,
          recordId2,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo);
      response.concludeTimers();
      after = System.nanoTime();

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void reevaluateRecordViaHttpTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("RECORD_ID", recordId1);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      SzLoadRecordResponse loadResponse = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzLoadRecordResponse.class);
      loadResponse.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE
              + "/records/" + recordId1 + "/reevaluate", queryParams);

      before = System.nanoTime();
      SzReevaluateResponse response = this.invokeServerViaHttp(
          POST, uriText, SzReevaluateResponse.class);
      response.concludeTimers();
      after = System.nanoTime();

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      loadResponse = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzLoadRecordResponse.class);
      loadResponse.concludeTimers();
      after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2 + "/reevaluate", queryParams);

      before = System.nanoTime();
      response = this.invokeServerViaHttp(
          POST, uriText, SzReevaluateResponse.class);
      response.concludeTimers();
      after = System.nanoTime();

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void reevaluateRecordViaJavaClientTest(Boolean withInfo,
                                                Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("RECORD_ID", recordId1);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzLoadRecordResponse clientLoadResponse
          = this.entityDataApi.addRecord(recordBody,
                                         WATCHLIST_DATA_SOURCE,
                                         recordId1,
                                         null,
                                         withInfo,
                                         withRaw);
      long after = System.nanoTime();

      SzLoadRecordResponse loadResponse = jsonCopy(clientLoadResponse,
                                                   SzLoadRecordResponse.class);

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE
              + "/records/" + recordId1 + "/reevaluate", queryParams);

      before = System.nanoTime();
      com.senzing.gen.api.model.SzReevaluateResponse clientResponse
          = this.entityDataApi.reevaluateRecord(WATCHLIST_DATA_SOURCE,
                                                recordId1,
                                                withInfo,
                                                withRaw);
      after = System.nanoTime();

      SzReevaluateResponse response = jsonCopy(clientResponse,
                                               SzReevaluateResponse.class);

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      clientLoadResponse = this.entityDataApi.addRecord(recordBody,
                                                        CUSTOMER_DATA_SOURCE,
                                                        recordId2,
                                                        null,
                                                        withInfo,
                                                        withRaw);
      after = System.nanoTime();

      loadResponse = jsonCopy(clientLoadResponse, SzLoadRecordResponse.class);

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2 + "/reevaluate", queryParams);

      before = System.nanoTime();
      clientResponse = this.entityDataApi.reevaluateRecord(CUSTOMER_DATA_SOURCE,
                                                           recordId2,
                                                           withInfo,
                                                           withRaw);
      after = System.nanoTime();

      response = jsonCopy(clientResponse, SzReevaluateResponse.class);

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

    });
  }

  //----------------------
  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void deleteRecordTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      job.add("RECORD_ID", recordId1);
      job.add("NAME_FIRST", "James");
      job.add("NAME_LAST", "Moriarty");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      SzLoadRecordResponse loadResponse = this.entityDataServices.loadRecord(
          WATCHLIST_DATA_SOURCE,
          recordId1,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          jsonText);
      loadResponse.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      before = System.nanoTime();
      SzDeleteRecordResponse response = this.entityDataServices.deleteRecord(
          WATCHLIST_DATA_SOURCE,
          recordId1,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo);
      response.concludeTimers();
      after = System.nanoTime();

      validateDeleteRecordResponse(response,
                                   DELETE,
                                   uriText,
                                   (withInfo != null ? withInfo : false),
                                   (withRaw != null ? withRaw : false),
                                   WATCHLIST_DATA_SOURCE,
                                   recordId1,
                                   1,
                                   0,
                                   Collections.emptySet(),
                                   after - before);

      // try a second delete (prove idempotence)
      before = System.nanoTime();
      response = this.entityDataServices.deleteRecord(
          WATCHLIST_DATA_SOURCE,
          recordId1,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo);
      response.concludeTimers();
      after = System.nanoTime();

      validateDeleteRecordResponse(response,
                                   DELETE,
                                   uriText,
                                   (withInfo != null ? withInfo : false),
                                   (withRaw != null ? withRaw : false),
                                   WATCHLIST_DATA_SOURCE,
                                   recordId1,
                                   0, // second delete
                                   0,
                                   Collections.emptySet(),
                                   after - before);

    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void deleteRecordViaHttpTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("RECORD_ID", recordId1);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      SzLoadRecordResponse loadResponse = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzLoadRecordResponse.class);
      loadResponse.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      before = System.nanoTime();
      SzDeleteRecordResponse response = this.invokeServerViaHttp(
          DELETE, uriText, SzDeleteRecordResponse.class);
      response.concludeTimers();
      after = System.nanoTime();

      validateDeleteRecordResponse(response,
                                   DELETE,
                                   uriText,
                                   (withInfo != null ? withInfo : false),
                                   (withRaw != null ? withRaw : false),
                                   WATCHLIST_DATA_SOURCE,
                                   recordId1,
                                   1,
                                   0,
                                   Collections.emptySet(),
                                   after - before);

      before = System.nanoTime();
      response = this.invokeServerViaHttp(
          DELETE, uriText, SzDeleteRecordResponse.class);
      response.concludeTimers();
      after = System.nanoTime();

      validateDeleteRecordResponse(response,
                                   DELETE,
                                   uriText,
                                   (withInfo != null ? withInfo : false),
                                   (withRaw != null ? withRaw : false),
                                   WATCHLIST_DATA_SOURCE,
                                   recordId1,
                                   0, // second delete
                                   0,
                                   Collections.emptySet(),
                                   after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void deleteRecordViaJavaClientTest(Boolean withInfo,
                                                Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("RECORD_ID", recordId1);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzLoadRecordResponse clientLoadResponse
          = this.entityDataApi.addRecord(recordBody,
                                         WATCHLIST_DATA_SOURCE,
                                         recordId1,
                                         null,
                                         withInfo,
                                         withRaw);
      long after = System.nanoTime();

      SzLoadRecordResponse loadResponse = jsonCopy(clientLoadResponse,
                                                   SzLoadRecordResponse.class);

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      before = System.nanoTime();
      com.senzing.gen.api.model.SzDeleteRecordResponse clientResponse
          = this.entityDataApi.deleteRecord(WATCHLIST_DATA_SOURCE,
                                            recordId1,
                                            null,
                                            withInfo,
                                            withRaw);
      after = System.nanoTime();

      SzDeleteRecordResponse response = jsonCopy(clientResponse,
                                                 SzDeleteRecordResponse.class);

      validateDeleteRecordResponse(response,
                                   DELETE,
                                   uriText,
                                   (withInfo != null ? withInfo : false),
                                   (withRaw != null ? withRaw : false),
                                   WATCHLIST_DATA_SOURCE,
                                   recordId1,
                                   1,
                                   0,
                                   Collections.emptySet(),
                                   after - before);

      clientResponse = this.entityDataApi.deleteRecord(WATCHLIST_DATA_SOURCE,
                                                       recordId1,
                                                       null,
                                                       withInfo,
                                                       withRaw);
      after = System.nanoTime();

      response = jsonCopy(clientResponse, SzDeleteRecordResponse.class);

      validateDeleteRecordResponse(response,
                                   DELETE,
                                   uriText,
                                   (withInfo != null ? withInfo : false),
                                   (withRaw != null ? withRaw : false),
                                   WATCHLIST_DATA_SOURCE,
                                   recordId1,
                                   0, // second delete
                                   0,
                                   Collections.emptySet(),
                                   after - before);


    });
  }

  private Long getEntityIdForRecordId(SzRecordId recordId) {
    String uriText = this.formatServerUri(
        "data-sources/" + recordId.getDataSourceCode() + "/records/"
            + recordId.getRecordId() + "/entity");
    UriInfo uriInfo = this.newProxyUriInfo(uriText);

    SzEntityResponse response = this.entityDataServices.getEntityByRecordId(
        recordId.getDataSourceCode(),
        recordId.getRecordId(),
        false,
        SzRelationshipMode.NONE,
        true,
        VERBOSE,
        WITH_DUPLICATES,
        false,
        false,
        uriInfo);

    SzEntityData data = response.getData();

    SzResolvedEntity entity = data.getResolvedEntity();

    return entity.getEntityId();
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void reevaluateEntityTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      job.add("RECORD_ID", recordId1);
      job.add("NAME_FIRST", "James");
      job.add("NAME_LAST", "Moriarty");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      SzLoadRecordResponse loadResponse = this.entityDataServices.loadRecord(
          WATCHLIST_DATA_SOURCE,
          recordId1,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          jsonText);
      loadResponse.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      Long entityId1 = this.getEntityIdForRecordId(
          SzRecordId.FACTORY.create(WATCHLIST_DATA_SOURCE, recordId1));

      queryParams.put("entityId", entityId1);
      uriText = this.formatServerUri("reevaluate-entity", queryParams);
      uriInfo = this.newProxyUriInfo(uriText);
      queryParams.remove("entityId");

      before = System.nanoTime();
      SzReevaluateResponse response = this.entityDataServices.reevaluateEntity(
          entityId1,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo);
      response.concludeTimers();
      after = System.nanoTime();

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);
      uriInfo = this.newProxyUriInfo(uriText);

      job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "Joe");
      job.add("NAME_LAST", "Schmoe");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      jsonObject  = job.build();
      jsonText    = JsonUtilities.toJsonText(jsonObject);

      before = System.nanoTime();
      loadResponse = this.entityDataServices.loadRecord(
          CUSTOMER_DATA_SOURCE,
          recordId2,
          null,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          jsonText);
      loadResponse.concludeTimers();
      after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

      Long entityId2 = this.getEntityIdForRecordId(
          SzRecordId.FACTORY.create(CUSTOMER_DATA_SOURCE, recordId2));

      queryParams.put("entityId", entityId2);
      uriText = this.formatServerUri("reevaluate-entity", queryParams);
      uriInfo = this.newProxyUriInfo(uriText);
      queryParams.remove("entityId");

      before = System.nanoTime();
      response = this.entityDataServices.reevaluateEntity(
          entityId2,
          (withInfo != null ? withInfo : false),
          (withRaw != null ? withRaw : false),
          uriInfo);
      response.concludeTimers();
      after = System.nanoTime();

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void reevaluateEntityViaHttpTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("RECORD_ID", recordId1);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      SzLoadRecordResponse loadResponse = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzLoadRecordResponse.class);
      loadResponse.concludeTimers();
      long after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      Long entityId1 = this.getEntityIdForRecordId(
          SzRecordId.FACTORY.create(WATCHLIST_DATA_SOURCE, recordId1));

      queryParams.put("entityId", entityId1);
      uriText = this.formatServerUri("reevaluate-entity", queryParams);
      queryParams.remove("entityId");

      before = System.nanoTime();
      SzReevaluateResponse response = this.invokeServerViaHttp(
          POST, uriText, SzReevaluateResponse.class);
      response.concludeTimers();
      after = System.nanoTime();

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      loadResponse = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzLoadRecordResponse.class);
      loadResponse.concludeTimers();
      after = System.nanoTime();

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

      Long entityId2 = this.getEntityIdForRecordId(
          SzRecordId.FACTORY.create(CUSTOMER_DATA_SOURCE, recordId2));

      queryParams.put("entityId", entityId2);
      uriText = this.formatServerUri("reevaluate-entity", queryParams);
      queryParams.remove("entityId");

      before = System.nanoTime();
      response = this.invokeServerViaHttp(
          POST, uriText, SzReevaluateResponse.class);
      response.concludeTimers();
      after = System.nanoTime();

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  public void reevaluateEntityViaJavaClientTest(Boolean withInfo,
                                                Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      final String recordId2 = "DEF456";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/"
              + recordId1, queryParams);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("RECORD_ID", recordId1);
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzLoadRecordResponse clientLoadResponse
          = this.entityDataApi.addRecord(recordBody,
                                         WATCHLIST_DATA_SOURCE,
                                         recordId1,
                                         null,
                                         withInfo,
                                         withRaw);
      long after = System.nanoTime();

      SzLoadRecordResponse loadResponse =  jsonCopy(clientLoadResponse,
                                                    SzLoadRecordResponse.class);

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      Long entityId1 = this.getEntityIdForRecordId(
          SzRecordId.FACTORY.create(WATCHLIST_DATA_SOURCE, recordId1));

      Map<String, Object> queryParams2 = new LinkedHashMap<>();
      queryParams2.put("entityId", entityId1);
      queryParams2.putAll(queryParams);
      uriText = this.formatServerUri("reevaluate-entity", queryParams2);

      before = System.nanoTime();
      com.senzing.gen.api.model.SzReevaluateResponse clientResponse
          = this.entityDataApi.reevaluateEntity(entityId1, withInfo, withRaw);
      after = System.nanoTime();

      SzReevaluateResponse response = jsonCopy(clientResponse,
                                               SzReevaluateResponse.class);

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 WATCHLIST_DATA_SOURCE,
                                 recordId1,
                                 1,
                                 0,
                                 Collections.emptySet(),
                                 after - before);

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      clientLoadResponse = this.entityDataApi.addRecord(recordBody,
                                                        CUSTOMER_DATA_SOURCE,
                                                        recordId2,
                                                        null,
                                                        withInfo,
                                                        withRaw);
      after = System.nanoTime();

      loadResponse = jsonCopy(clientLoadResponse, SzLoadRecordResponse.class);

      validateLoadRecordResponse(loadResponse,
                                 PUT,
                                 uriText,
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);

      Long entityId2 = this.getEntityIdForRecordId(
          SzRecordId.FACTORY.create(CUSTOMER_DATA_SOURCE, recordId2));

      queryParams2.clear();
      queryParams2.put("entityId", entityId2);
      queryParams2.putAll(queryParams);
      uriText = this.formatServerUri("reevaluate-entity", queryParams2);

      before = System.nanoTime();
      clientResponse = this.entityDataApi.reevaluateEntity(entityId2,
                                                           withInfo,
                                                           withRaw);
      after = System.nanoTime();

      response = jsonCopy(clientResponse, SzReevaluateResponse.class);

      validateReevaluateResponse(response,
                                 POST,
                                 uriText,
                                 (withInfo != null ? withInfo : false),
                                 (withRaw != null ? withRaw : false),
                                 CUSTOMER_DATA_SOURCE,
                                 recordId2,
                                 1,
                                 1,
                                 Set.of(WATCHLIST_FLAG),
                                 after - before);
    });
  }

}
