package com.senzing.api.services;

import com.senzing.api.model.SzErrorResponse;
import com.senzing.api.server.SzApiServer;
import com.senzing.api.server.SzApiServerOptions;
import com.senzing.util.JsonUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.client.HttpStatusCodeException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.UriInfo;
import java.util.*;

import static com.senzing.api.model.SzHttpMethod.POST;
import static com.senzing.api.model.SzHttpMethod.PUT;
import static com.senzing.api.model.SzHttpMethod.DELETE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static com.senzing.api.services.ResponseValidators.*;

@TestInstance(Lifecycle.PER_CLASS)
public class ReadOnlyEntityDataWriteServicesTest
    extends EntityDataWriteServicesTest
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
    options.setSkippingEnginePriming(true);
  }

  @Test
  @Override
  public void postRecordTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "Joe");
      job.add("NAME_LAST", "Schmoe");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      try {
        this.entityDataServices.loadRecord(
            WATCHLIST_DATA_SOURCE, null, false, false, uriInfo, jsonText);
        fail("Did not get expected 403 ForbiddenException in read-only mode");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, POST, uriText, after - before);
      }
    });
  }

  @Test
  @Override
  public void postRecordViaHttpTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      Map recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joanne");
      recordBody.put("NAME_LAST", "Smith");
      recordBody.put("PHONE_NUMBER", "212-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          POST, uriText, null, recordBody, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response, 403, POST, uriText, after - before);
    });
  }

  @Test
  @Override
  public void postMismatchedDataSourceTest() {
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

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, POST, uriText, after - before);
      }
    });
  }

  @Test
  @Override
  public void postMismatchedDataSourceViaHttpTest() {
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
                     403,
                     POST,
                     uriText,
                     after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("postWithInfoParams")
  @Override
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
      try {
        this.entityDataServices.loadRecord(
            WATCHLIST_DATA_SOURCE,
            null,
            (withInfo != null ? withInfo : false),
            (withRaw != null ? withRaw : false),
            uriInfo,
            jsonText);

        fail("Did not get expected 403 ForbiddenException in read-only mode");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, POST, uriText, after - before);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("postWithInfoParams")
  @Override
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
      SzErrorResponse response = this.invokeServerViaHttp(
          POST, uriText, null, recordBody, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response, 403, POST, uriText, after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("postWithInfoParams")
  @Override
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
      try {
        this.entityDataApi.addRecordWithReturnedRecordId(recordBody,
                                                         WATCHLIST_DATA_SOURCE,
                                                         null,
                                                         withInfo,
                                                         withRaw);
      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       POST,
                       uriText,
                       after - before);
      }

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records",
          queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      try {
        this.entityDataApi.addRecordWithReturnedRecordId(
            recordBody, CUSTOMER_DATA_SOURCE, null, withInfo, withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       POST,
                       uriText,
                       after - before);
      }
    });
  }

  @Test
  @Override
  public void putRecordTest() {
    this.performTest(() -> {
      final String recordId = "ABC123";

      String  uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("NAME_FIRST", "John");
      job.add("NAME_LAST", "Doe");
      job.add("PHONE_NUMBER", "818-555-1313");
      job.add("ADDR_FULL", "100 Main Street, Los Angeles, CA 90012");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      try {
        this.entityDataServices.loadRecord(
            WATCHLIST_DATA_SOURCE, recordId, null, false, false, uriInfo, jsonText);

        fail("Did not get expected 403 ForbiddenException in read-only mode");
      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, PUT, uriText, after - before);
      }
    });
  }

  @Test @Override public void putRecordViaHttpTest() {
    this.performTest(() -> {
      final String recordId = "XYZ456";

      String  uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      Map recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Jane");
      recordBody.put("NAME_LAST", "Doe");
      recordBody.put("PHONE_NUMBER", "818-555-1212");
      recordBody.put("ADDR_FULL", "500 First Street, Los Angeles, CA 90033");

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response, 403, PUT, uriText, after - before);
    });
  }

  @Test @Override public void putMismatchedRecordTest() {
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

        fail("Did not get expected 403 ForbiddenException in read-only mode");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, PUT, uriText, after - before);
      }
    });
  }

  @Test @Override public void putMismatchedRecordViaHttpTest() {
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
                     403,
                     PUT,
                     uriText,
                     after - before);
    });
  }

  @Test @Override public void putMismatchedRecordViaJavaClientTest() {
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
                       403,
                       PUT,
                       uriText,
                       after - before);
      }
    });
  }

  @Test @Override public void putMismatchedDataSourceTest() {
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

        fail("Did not get expected 403 ForbiddenException in read-only mode");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, PUT, uriText, after - before);
      }
    });
  }

  @Test @Override public void putMismatchedDataSourceViaHttpTest() {
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
                     403,
                     PUT,
                     uriText,
                     after - before);
    });
  }

  @Test @Override public void putMismatchedDataSourceViaJavaClientTest() {
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

        fail("Expected forbidden failure, but got success for mismatched data "
                 + "source: urlDataSource=[ " + CUSTOMER_DATA_SOURCE
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
                       403,
                       PUT,
                       uriText,
                       after - before);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
  public void putRecordWithInfoTest(Boolean  withInfo,
                                    Boolean  withRaw)
  {
    this.performTest(() -> {
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/ABC123",
          queryParams);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      job.add("RECORD_ID", "ABC123");
      job.add("NAME_FIRST", "James");
      job.add("NAME_LAST", "Moriarty");
      job.add("PHONE_NUMBER", "702-555-1212");
      job.add("ADDR_FULL", "101 Main Street, Las Vegas, NV 89101");
      JsonObject  jsonObject  = job.build();
      String      jsonText    = JsonUtilities.toJsonText(jsonObject);

      long before = System.nanoTime();
      try {
        this.entityDataServices.loadRecord(
            WATCHLIST_DATA_SOURCE,
            "ABC123",
            null,
            (withInfo != null ? withInfo : false),
            (withRaw != null ? withRaw : false),
            uriInfo,
            jsonText);

        fail("Did not get expected 403 ForbiddenException in read-only mode");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, PUT, uriText, after - before);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
  public void putRecordWithInfoViaHttpTest(Boolean withInfo,
                                           Boolean withRaw)
  {
    this.performTest(() -> {
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE + "/records/ABC123",
          queryParams);

      Map recordBody = new HashMap();
      recordBody.put("DATA_SOURCE", WATCHLIST_DATA_SOURCE);
      recordBody.put("RECORD_ID", "ABC123");
      recordBody.put("NAME_FIRST", "James");
      recordBody.put("NAME_LAST", "Moriarty");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          PUT, uriText, null, recordBody, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response,
                     403,
                     PUT,
                     uriText,
                     after - before);

    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
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
      try {
        this.entityDataApi.addRecord(recordBody,
                                     WATCHLIST_DATA_SOURCE,
                                     recordId1,
                                     null,
                                     withInfo,
                                     withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       PUT,
                       uriText,
                       after - before);
      }

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      try {
        this.entityDataApi.addRecord(recordBody,
                                     CUSTOMER_DATA_SOURCE,
                                     recordId2,
                                     null,
                                     withInfo,
                                     withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       PUT,
                       uriText,
                       after - before);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
  public void reevaluateRecordTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE
              + "/records/" + recordId1 + "/reevaluate", queryParams);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      try {
        this.entityDataServices.reevaluateRecord(
            WATCHLIST_DATA_SOURCE,
            recordId1,
            (withInfo != null ? withInfo : false),
            (withRaw != null ? withRaw : false),
            uriInfo);
        fail("Did not get expected 403 ForbiddenException in read-only mode");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, POST, uriText, after - before);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
  public void reevaluateRecordViaHttpTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE
              + "/records/" + recordId1 + "/reevaluate", queryParams);

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          POST, uriText, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response,
                     403,
                     POST,
                     uriText,
                     after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
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
      try {
        this.entityDataApi.addRecord(recordBody,
                                     WATCHLIST_DATA_SOURCE,
                                     recordId1,
                                     null,
                                     withInfo,
                                     withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       PUT,
                       uriText,
                       after - before);
      }

      uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE
              + "/records/" + recordId1 + "/reevaluate", queryParams);

      before = System.nanoTime();
      try {
        this.entityDataApi.reevaluateRecord(WATCHLIST_DATA_SOURCE,
                                            recordId1,
                                            withInfo,
                                            withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       POST,
                       uriText,
                       after - before);
      }

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      try {
        this.entityDataApi.addRecord(recordBody,
                                     CUSTOMER_DATA_SOURCE,
                                     recordId2,
                                     null,
                                     withInfo,
                                     withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       PUT,
                       uriText,
                       after - before);
      }

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2 + "/reevaluate", queryParams);

      before = System.nanoTime();
      try {
        this.entityDataApi.reevaluateRecord(CUSTOMER_DATA_SOURCE,
                                            recordId2,
                                            withInfo,
                                            withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       POST,
                       uriText,
                       after - before);
      }
    });
  }
  
  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
  public void deleteRecordTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE
              + "/records/" + recordId1, queryParams);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      try {
        this.entityDataServices.deleteRecord(
            WATCHLIST_DATA_SOURCE,
            recordId1,
            null,
            (withInfo != null ? withInfo : false),
            (withRaw != null ? withRaw : false),
            uriInfo);
        fail("Did not get expected 403 ForbiddenException in read-only mode");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, DELETE, uriText, after - before);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
  public void deleteRecordViaHttpTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);

      String uriText = this.formatServerUri(
          "data-sources/" + WATCHLIST_DATA_SOURCE
              + "/records/" + recordId1, queryParams);

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          DELETE, uriText, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response,
                     403,
                     DELETE,
                     uriText,
                     after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
  public void deleteRecordViaJavaClientTest(Boolean withInfo, Boolean withRaw)
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
      try {
        this.entityDataApi.addRecord(recordBody,
                                     WATCHLIST_DATA_SOURCE,
                                     recordId1,
                                     null,
                                     withInfo,
                                     withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       PUT,
                       uriText,
                       after - before);
      }

      before = System.nanoTime();
      try {
        this.entityDataApi.deleteRecord(WATCHLIST_DATA_SOURCE,
                                        recordId1,
                                        null,
                                        withInfo,
                                        withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       DELETE,
                       uriText,
                       after - before);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
  public void reevaluateEntityTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      final String recordId1 = "ABC123";
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);
      queryParams.put("entityId", 100L);

      String uriText = this.formatServerUri("reevaluate-entity", queryParams);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      try {
        this.entityDataServices.reevaluateEntity(
            100L,
            (withInfo != null ? withInfo : false),
            (withRaw != null ? withRaw : false),
            uriInfo);
        fail("Did not get expected 403 ForbiddenException in read-only mode");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(response, 403, POST, uriText, after - before);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
  public void reevaluateEntityViaHttpTest(Boolean withInfo, Boolean withRaw)
  {
    this.performTest(() -> {
      Map<String, Object> queryParams = new LinkedHashMap<>();
      if (withInfo != null) queryParams.put("withInfo", withInfo);
      if (withRaw != null) queryParams.put("withRaw", withRaw);
      queryParams.put("entityId", 100L);

      String uriText = this.formatServerUri("reevaluate-entity", queryParams);

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          POST, uriText, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response,
                     403,
                     POST,
                     uriText,
                     after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("withInfoParams")
  @Override
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
      try {
        this.entityDataApi.addRecord(recordBody,
                                     WATCHLIST_DATA_SOURCE,
                                     recordId1,
                                     null,
                                     withInfo,
                                     withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       PUT,
                       uriText,
                       after - before);
      }

      Long entityId1 = 10L;

      Map<String, Object> queryParams2 = new LinkedHashMap<>();
      queryParams2.put("entityId", entityId1);
      queryParams2.putAll(queryParams);
      uriText = this.formatServerUri("reevaluate-entity", queryParams2);

      before = System.nanoTime();
      try {
        this.entityDataApi.reevaluateEntity(entityId1, withInfo, withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       POST,
                       uriText,
                       after - before);
      }

      uriText = this.formatServerUri(
          "data-sources/" + CUSTOMER_DATA_SOURCE + "/records/"
              + recordId2, queryParams);

      recordBody = new HashMap();
      recordBody.put("NAME_FIRST", "Joe");
      recordBody.put("NAME_LAST", "Schmoe");
      recordBody.put("PHONE_NUMBER", "702-555-1212");
      recordBody.put("ADDR_FULL", "101 Fifth Ave, Las Vegas, NV 10018");

      before = System.nanoTime();
      try {
        this.entityDataApi.addRecord(recordBody,
                                     CUSTOMER_DATA_SOURCE,
                                     recordId2,
                                     null,
                                     withInfo,
                                     withRaw);

      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       PUT,
                       uriText,
                       after - before);
      }

      Long entityId2 = 20L;

      queryParams2.clear();
      queryParams2.put("entityId", entityId2);
      queryParams2.putAll(queryParams);
      uriText = this.formatServerUri("reevaluate-entity", queryParams2);

      before = System.nanoTime();
      try {
        this.entityDataApi.reevaluateEntity(entityId2, withInfo, withRaw);
      } catch (HttpStatusCodeException expected) {
        long after = System.nanoTime();
        com.senzing.gen.api.model.SzErrorResponse clientResponse
            = jsonParse(expected.getResponseBodyAsString(),
                        com.senzing.gen.api.model.SzErrorResponse.class);

        SzErrorResponse response = jsonCopy(clientResponse,
                                            SzErrorResponse.class);

        validateBasics(response,
                       403,
                       POST,
                       uriText,
                       after - before);
      }

    });
  }

}
