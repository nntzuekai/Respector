package com.senzing.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.senzing.api.BuildInfo;
import com.senzing.api.model.*;
import com.senzing.nativeapi.NativeApiFactory;
import com.senzing.g2.engine.G2Product;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.regex.Pattern;

import static com.senzing.api.BuildInfo.MAVEN_VERSION;
import static com.senzing.api.BuildInfo.REST_API_VERSION;
import static com.senzing.api.model.SzFeatureMode.NONE;
import static com.senzing.api.model.SzFeatureMode.REPRESENTATIVE;
import static com.senzing.api.model.SzRelationshipMode.*;
import static com.senzing.api.model.SzHttpMethod.GET;
import static org.junit.jupiter.api.Assertions.*;
import static com.senzing.api.model.SzDetailLevel.*;

public class ResponseValidators {
  /**
   * Validates the basic response fields for the specified {@link
   * SzBasicResponse} using the specified self link, and the maximum duration
   * for the timings in nanoseconds.
   *
   * @param response    The {@link SzBasicResponse} to validate.
   * @param selfLink    The self link to be expected.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateBasics(SzBasicResponse response,
                                    String          selfLink,
                                    long            maxDuration)
  {
    validateBasics(null, response, GET, selfLink, maxDuration);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzBasicResponse} using the specified self link, and the maximum duration
   * for the timings in nanoseconds.
   *
   * @param testInfo        Additional test information to be logged with failures.
   * @param response        The {@link SzBasicResponse} to validate.
   * @param selfLink        The self link to be expected.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateBasics(String          testInfo,
                                    SzBasicResponse response,
                                    String          selfLink,
                                    long            maxDuration)
  {
    validateBasics(testInfo, response, GET, selfLink, maxDuration);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzBasicResponse} using the specified self link, and the maximum duration
   * for the timings in nanoseconds.
   *
   * @param response           The {@link SzBasicResponse} to validate.
   * @param expectedHttpMethod The {@link SzHttpMethod} that was used.
   * @param selfLink           The self link to be expected.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateBasics(SzBasicResponse response,
                                    SzHttpMethod    expectedHttpMethod,
                                    String          selfLink,
                                    long            maxDuration)
  {
    validateBasics(
        null, response, expectedHttpMethod, selfLink, maxDuration);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzBasicResponse} using the specified self link, and the maximum duration
   * for the timings in nanoseconds.
   *
   * @param response The {@link SzBasicResponse} to validate.
   * @param expectedResponseCode The expected HTTP response code.
   * @param expectedHttpMethod The {@link SzHttpMethod} that was used.
   * @param selfLink The self link to be expected.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateBasics(SzBasicResponse response,
                                    int             expectedResponseCode,
                                    SzHttpMethod    expectedHttpMethod,
                                    String          selfLink,
                                    long            maxDuration)
  {
    validateBasics(null,
                   response,
                   expectedResponseCode,
                   expectedHttpMethod,
                   selfLink,
                   maxDuration);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzBasicResponse} using the specified self link, and the maximum duration
   * for the timings in nanoseconds.
   *
   * @param testInfo           Additional test information to be logged with failures.
   * @param response           The {@link SzBasicResponse} to validate.
   * @param expectedHttpMethod The {@link SzHttpMethod} that was used.
   * @param selfLink           The self link to be expected.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateBasics(String          testInfo,
                                    SzBasicResponse response,
                                    SzHttpMethod    expectedHttpMethod,
                                    String          selfLink,
                                    long            maxDuration)
  {
    validateBasics(testInfo,
                   response,
                   200,
                   expectedHttpMethod,
                   selfLink,
                   maxDuration);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzBasicResponse} using the specified self link, and the maximum duration
   * for the timings in nanoseconds.
   *
   * @param testInfo Additional test information to be logged with failures.
   * @param response The {@link SzBasicResponse} to validate.
   * @param expectedResponseCode The expected HTTP responsec code.
   * @param expectedHttpMethod The {@link SzHttpMethod} that was used.
   * @param selfLink The self link to be expected.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateBasics(String          testInfo,
                                    SzBasicResponse response,
                                    int             expectedResponseCode,
                                    SzHttpMethod    expectedHttpMethod,
                                    String          selfLink,
                                    long            maxDuration)
  {
    validateBasics(testInfo,
                   response,
                   expectedResponseCode,
                   expectedHttpMethod,
                   selfLink,
                   maxDuration,
                  1);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzBasicResponse} using the specified self link, the maximum duration
   * for the timings in nanoseconds and the server concurrency.
   *
   * @param testInfo Additional test information to be logged with failures.
   * @param response The {@link SzBasicResponse} to validate.
   * @param expectedResponseCode The expected HTTP responsec code.
   * @param expectedHttpMethod The {@link SzHttpMethod} that was used.
   * @param selfLink The self link to be expected.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   * @param serverConcurrency The concurrency for the server.
   */
  public static void validateBasics(String          testInfo,
                                    SzBasicResponse response,
                                    int             expectedResponseCode,
                                    SzHttpMethod    expectedHttpMethod,
                                    String          selfLink,
                                    long            maxDuration,
                                    int             serverConcurrency)
  {
    String suffix = (testInfo != null && testInfo.trim().length() > 0)
        ? " ( " + testInfo + " )" : "";

    SzLinks links = response.getLinks();
    SzMeta meta = response.getMeta();

    String expectedLink = selfLink.replaceAll("%20", "+");
    String actualLink   = links.getSelf().replaceAll("%20", "+");
    assertEquals(expectedLink, actualLink, "Unexpected self link" + suffix);
    assertEquals(expectedHttpMethod, meta.getHttpMethod(),
                 "Unexpected HTTP method" + suffix);
    assertEquals(expectedResponseCode, meta.getHttpStatusCode(), "Unexpected HTTP status code" + suffix);
    assertEquals(MAVEN_VERSION, meta.getVersion(), "Unexpected server version" + suffix);
    assertEquals(REST_API_VERSION, meta.getRestApiVersion(), "Unexpected REST API version" + suffix);
    assertNotNull(meta.getTimestamp(), "Timestamp unexpectedly null" + suffix);

    // NOTE: we do not validate the timestamp because System.currentTimeMillis()
    // is **NOT** monotonic (adjustments to system clock to sync with NTP
    // servers can cause the value to go backwards

    Map<String, Long> timings = meta.getTimings();

    // determine max duration for concurrency and convert nanos to millis
    final long concurrentMax = ((maxDuration * serverConcurrency) / 1000) + 1;

    timings.forEach((key, duration)-> {
      if (duration > concurrentMax) {
        fail("Timing value too large: " + key + " = "
                 + duration + "ms VS " + concurrentMax + "ms" + suffix);
      }
    });
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzResponseWithRawData} using the specified self link, the maximum duration
   * for the timings in nanoseconds, and flag indicating if raw data should be
   * expected.
   *
   * @param response      The {@link SzBasicResponse} to validate.
   * @param selfLink      The self link to be expected.
   * @param maxDuration   The maximum duration for the timers in nanoseconds.
   * @param expectRawData <tt>true</tt> if raw data should be expected,
   *                      otherwise <tt>false</tt>
   */
  public static void validateBasics(SzResponseWithRawData response,
                                    String                selfLink,
                                    long                  maxDuration,
                                    boolean               expectRawData)
  {
    validateBasics(null,
                   response,
                   selfLink,
                   maxDuration,
                   expectRawData);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzResponseWithRawData} using the specified self link, the maximum duration
   * for the timings in nanoseconds, and flag indicating if raw data should be
   * expected.
   *
   * @param response      The {@link SzBasicResponse} to validate.
   * @param selfLink      The self link to be expected.
   * @param maxDuration   The maximum duration for the timers in nanoseconds.
   * @param expectRawData <tt>true</tt> if raw data should be expected,
   *                      otherwise <tt>false</tt>
   */
  public static void validateBasics(SzResponseWithRawData response,
                                    SzHttpMethod          expectedHttpMethod,
                                    String                selfLink,
                                    long                  maxDuration,
                                    boolean               expectRawData)
  {
    validateBasics(null,
                   response,
                   expectedHttpMethod,
                   selfLink,
                   maxDuration,
                   expectRawData);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzResponseWithRawData} using the specified self link, the maximum duration
   * for the timings in nanoseconds, and flag indicating if raw data should be
   * expected.
   *
   * @param testInfo      Additional test information to be logged with failures.
   * @param response      The {@link SzBasicResponse} to validate.
   * @param selfLink      The self link to be expected.
   * @param maxDuration   The maximum duration for the timers in nanoseconds.
   * @param expectRawData <tt>true</tt> if raw data should be expected,
   *                      otherwise <tt>false</tt>
   */
  public static void validateBasics(String                testInfo,
                                    SzResponseWithRawData response,
                                    String                selfLink,
                                    long                  maxDuration,
                                    boolean               expectRawData)
  {
    validateBasics(testInfo,
                   response,
                   GET,
                   selfLink,
                   maxDuration,
                   expectRawData);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzResponseWithRawData} using the specified self link, the maximum duration
   * for the timings in nanoseconds., and flag indicating if raw data should be
   * expected.
   *
   * @param testInfo        Additional test information to be logged with failures.
   * @param response        The {@link SzBasicResponse} to validate.
   * @param expectedHttpMethod The expected HTTP method.
   * @param selfLink        The self link to be expected.
   * @param maxDuration     The maximum duration for the timers in nanoseconds.
   * @param expectRawData   <tt>true</tt> if raw data should be expected,
   *                        otherwise <tt>false</tt>
   */
  public static void validateBasics(String                testInfo,
                                    SzResponseWithRawData response,
                                    SzHttpMethod          expectedHttpMethod,
                                    String                selfLink,
                                    long                  maxDuration,
                                    boolean               expectRawData)
  {
    String suffix = (testInfo != null && testInfo.trim().length() > 0)
        ? " ( " + testInfo + " )" : "";

    validateBasics(testInfo,
                   response,
                   expectedHttpMethod,
                   selfLink,
                   maxDuration);

    Object rawData = response.getRawData();
    if (expectRawData) {
      assertNotNull(rawData, "Raw data unexpectedly non-null" + suffix);
    } else {
      assertNull(rawData, "Raw data unexpectedly null" + suffix);
    }
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzBasicResponse} using the specified self link, and the maximum duration
   * for the timings in nanoseconds.
   *
   * @param response    The {@link SzBasicResponse} to validate.
   * @param uriInfo     The {@link UriInfo} to self link to be expected.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateOpenApiSpecResponse(
      SzOpenApiSpecResponse response,
      String                selfLink,
      String                baseUri,
      long                  maxDuration)
  {
    validateBasics(response, GET, selfLink, maxDuration);

    validateOpenApiSpecResponse(response.getData(), baseUri);
  }

  /**
   * Validates the basic response fields for the specified {@link
   * SzBasicResponse} using the specified self link, and the maximum duration
   * for the timings in nanoseconds.
   *
   * @param response  The {@link SzBasicResponse} to validate.
   * @param baseUri   The base URI for the server.
   */
  public static void validateOpenApiSpecResponse(Object response,
                                                 String baseUri)
  {
    String      jsonText    = toJsonString(response);
    JsonObject  jsonObject  = JsonUtilities.parseJsonObject(jsonText);

    // check for expected segments
    String[] segments = {
        "openapi", "info", "servers", "tags", "paths", "components" };
    for (String segment: segments) {
      if (!jsonObject.containsKey(segment)) {
        fail("Open API specification missing expected segment: " + segment);
      }
    }

    // get the servers segment
    JsonArray servers = jsonObject.getJsonArray("servers");
    if (servers.size() == 0) {
      fail("No servers defined in servers segment");
    }

    // get the first server and check the URL
    JsonObject server = servers.getJsonObject(0);
    assertEquals(baseUri, server.getString("url"),
                 "Unexpected server URL in Open API specification");
  }

  /**
   * Converts the specified object to JSON.
   */
  public static String toJsonString(Object object) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JodaModule());
    try {
      String jsonText = objectMapper.writeValueAsString(object);
      JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonText);
      return JsonUtilities.toJsonText(jsonObject, true);

    } catch (Exception e) {
      return "FAILED TO CONVERT TO JSON: " + e.getMessage();
    }
  }

  /**
   * Validates the raw data and ensures the expected JSON property keys are
   * present and that no unexpected keys are present.
   *
   * @param rawData      The raw data to validate.
   * @param expectedKeys The zero or more expected property keys.
   */
  public static void validateRawDataMap(Object rawData, String... expectedKeys)
  {
    validateRawDataMap(null,
                            rawData,
                            true,
                            expectedKeys);
  }

  /**
   * Validates the raw data and ensures the expected JSON property keys are
   * present and that no unexpected keys are present.
   *
   * @param testInfo     Additional test information to be logged with failures.
   * @param rawData      The raw data to validate.
   * @param expectedKeys The zero or more expected property keys.
   */
  public static void validateRawDataMap(String    testInfo,
                                        Object    rawData,
                                        String... expectedKeys)
  {
    validateRawDataMap(testInfo, rawData, true, expectedKeys);
  }

  /**
   * Validates the raw data and ensures the expected JSON property keys are
   * present and that, optionally, no unexpected keys are present.
   *
   * @param rawData      The raw data to validate.
   * @param strict       Whether or not property keys other than those specified are
   *                     allowed to be present.
   * @param expectedKeys The zero or more expected property keys -- these are
   *                     either a minimum or exact set depending on the
   *                     <tt>strict</tt> parameter.
   */
  public static void validateRawDataMap(Object    rawData,
                                        boolean   strict,
                                        String... expectedKeys)
  {
    validateRawDataMap(null, rawData, strict, expectedKeys);
  }

  /**
   * Validates the raw data and ensures the expected JSON property keys are
   * present and that, optionally, no unexpected keys are present.
   *
   *
   * @param testInfo     Additional test information to be logged with failures.
   * @param rawData      The raw data to validate.
   * @param strict       Whether or not property keys other than those specified are
   *                     allowed to be present.
   * @param expectedKeys The zero or more expected property keys -- these are
   *                     either a minimum or exact set depending on the
   *                     <tt>strict</tt> parameter.
   */
  public static void validateRawDataMap(String    testInfo,
                                        Object    rawData,
                                        boolean   strict,
                                        String... expectedKeys)
  {
    String suffix = (testInfo != null && testInfo.trim().length() > 0)
        ? " ( " + testInfo + " )" : "";

    if (rawData == null) {
      fail("Expected raw data but got null value" + suffix);
    }

    if (!(rawData instanceof Map)) {
      fail("Raw data is not a JSON object: " + rawData + suffix);
    }

    Map<String, Object> map = (Map<String, Object>) rawData;
    Set<String> expectedKeySet = new HashSet<>();
    Set<String> actualKeySet = map.keySet();
    for (String key : expectedKeys) {
      expectedKeySet.add(key);
      if (!actualKeySet.contains(key)) {
        fail("JSON property missing from raw data: " + key + " / " + map
                 + suffix);
      }
    }
    if (strict && expectedKeySet.size() != actualKeySet.size()) {
      Set<String> extraKeySet = new HashSet<>(actualKeySet);
      extraKeySet.removeAll(expectedKeySet);
      fail("Unexpected JSON properties in raw data: " + extraKeySet + suffix);
    }

  }


  /**
   * Validates the raw data and ensures it is a collection of objects and the
   * expected JSON property keys are present in the array objects and that no
   * unexpected keys are present.
   *
   * @param rawData      The raw data to validate.
   * @param expectedKeys The zero or more expected property keys.
   */
  public static void validateRawDataMapArray(Object     rawData,
                                             String...  expectedKeys)
  {
    validateRawDataMapArray(null, rawData, true, expectedKeys);
  }

  /**
   * Validates the raw data and ensures it is a collection of objects and the
   * expected JSON property keys are present in the array objects and that no
   * unexpected keys are present.
   *
   * @param testInfo     Additional test information to be logged with failures.
   * @param rawData      The raw data to validate.
   * @param expectedKeys The zero or more expected property keys.
   */
  public static void validateRawDataMapArray(String     testInfo,
                                             Object     rawData,
                                             String...  expectedKeys)
  {
    validateRawDataMapArray(testInfo, rawData, true, expectedKeys);
  }

  /**
   * Validates the raw data and ensures it is a collection of objects and the
   * expected JSON property keys are present in the array objects and that,
   * optionally, no unexpected keys are present.
   *
   * @param rawData      The raw data to validate.
   * @param strict       Whether or not property keys other than those specified are
   *                     allowed to be present.
   * @param expectedKeys The zero or more expected property keys for the array
   *                     objects -- these are either a minimum or exact set
   *                     depending on the <tt>strict</tt> parameter.
   */
  public static void validateRawDataMapArray(Object     rawData,
                                             boolean    strict,
                                             String...  expectedKeys)
  {
    validateRawDataMapArray(null, rawData, strict, expectedKeys);
  }

  /**
   * Validates the raw data and ensures it is a collection of objects and the
   * expected JSON property keys are present in the array objects and that,
   * optionally, no unexpected keys are present.
   *
   * @param testInfo     Additional test information to be logged with failures.
   * @param rawData      The raw data to validate.
   * @param strict       Whether or not property keys other than those specified are
   *                     allowed to be present.
   * @param expectedKeys The zero or more expected property keys for the array
   *                     objects -- these are either a minimum or exact set
   *                     depending on the <tt>strict</tt> parameter.
   */
  public static void validateRawDataMapArray(String     testInfo,
                                             Object     rawData,
                                             boolean    strict,
                                             String...  expectedKeys)
  {
    String suffix = (testInfo != null && testInfo.trim().length() > 0)
        ? " ( " + testInfo + " )" : "";

    if (rawData == null) {
      fail("Expected raw data but got null value" + suffix);
    }

    if (!(rawData instanceof Collection)) {
      fail("Raw data is not a JSON array: " + rawData + suffix);
    }

    Collection<Object> collection = (Collection<Object>) rawData;
    Set<String> expectedKeySet = new HashSet<>();
    for (String key : expectedKeys) {
      expectedKeySet.add(key);
    }

    for (Object obj : collection) {
      if (!(obj instanceof Map)) {
        fail("Raw data is not a JSON array of JSON objects: " + rawData + suffix);
      }

      Map<String, Object> map = (Map<String, Object>) obj;

      Set<String> actualKeySet = map.keySet();
      for (String key : expectedKeySet) {
        if (!actualKeySet.contains(key)) {
          fail("JSON property missing from raw data array element: "
                   + key + " / " + map + suffix);
        }
      }
      if (strict && expectedKeySet.size() != actualKeySet.size()) {
        Set<String> extraKeySet = new HashSet<>(actualKeySet);
        extraKeySet.removeAll(expectedKeySet);
        fail("Unexpected JSON properties in raw data: " + extraKeySet + suffix);
      }
    }
  }

  /**
   * Compares two collections to ensure they have the same elements.
   *
   */
  public static void assertSameElements(Collection expected,
                                        Collection actual,
                                        String     description)
  {
    if (expected != null) {
      expected = upperCase(expected);
      actual   = upperCase(actual);
      assertNotNull(actual, "Unexpected null " + description);
      if (!actual.containsAll(expected)) {
        Set missing = new HashSet(expected);
        missing.removeAll(actual);
        fail("Missing one or more expected " + description + ".  missing=[ "
                 + missing + " ], actual=[ " + actual + " ]");
      }
      if (!expected.containsAll(actual)) {
        Set extras = new HashSet(actual);
        extras.removeAll(expected);
        fail("One or more extra " + description + ".  extras=[ "
                 + extras + " ], actual=[ " + actual + " ]");
      }
    }
  }

  /**
   * Converts the {@link String} elements in the specified {@link Collection}
   * to upper case and returns a {@link Set} contianing all values.
   *
   * @param c The {@link Collection} to process.
   *
   * @return The {@link Set} containing the same elements with the {@link
   *         String} elements converted to upper case.
   */
  protected static Set upperCase(Collection c) {
    Set set = new LinkedHashSet();
    for (Object obj : c) {
      if (obj instanceof String) {
        obj = ((String) obj).toUpperCase();
      }
      set.add(obj);
    }
    return set;
  }

  /**
   * Validates an entity
   */
  public static void validateEntity(
      String                              testInfo,
      SzResolvedEntity                    entity,
      List<SzRelatedEntity>               relatedEntities,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      boolean                             withFeatureStats,
      boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Set<SzRecordId>                     expectedRecordIds,
      Boolean                             relatedSuppressed,
      Integer                             relatedEntityCount,
      Boolean                             relatedPartial,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    if (expectedRecordCount != null) {
      int recordCount = 0;
      for (SzDataSourceRecordSummary summary : entity.getRecordSummaries()) {
        recordCount += summary.getRecordCount();
      }
      assertEquals(expectedRecordCount, recordCount,
                   "Unexpected number of records in summary: "
                       + testInfo);

      if (detailLevel != SUMMARY) {
        assertEquals(expectedRecordCount, entity.getRecords().size(),
                     "Unexpected number of records: " + testInfo);
      }
    }
    if (expectedRecordIds != null) {
      Map<String, Integer> expectedCounts = new HashMap<>();
      for (SzRecordId recordId : expectedRecordIds) {
        Integer count = expectedCounts.get(recordId.getDataSourceCode());
        if (count == null) {
          expectedCounts.put(recordId.getDataSourceCode(), 1);
        } else {
          expectedCounts.put(recordId.getDataSourceCode(), count + 1);
        }
      }
      // get the records and convert to record ID set
      Set<SzRecordId> actualRecordIds = new HashSet<>();
      Map<String, Integer> recordCounts = new HashMap<>();
      List<SzMatchedRecord> matchedRecords = entity.getRecords();
      if (detailLevel != SUMMARY) {
        for (SzMatchedRecord record : matchedRecords) {
          SzRecordId recordId = SzRecordId.FACTORY.create(record.getDataSource(),
                                                          record.getRecordId());
          actualRecordIds.add(recordId);
          Integer count = recordCounts.get(recordId.getDataSourceCode());
          if (count == null) {
            recordCounts.put(record.getDataSource(), 1);
          } else {
            recordCounts.put(record.getDataSource(), count + 1);
          }
        }
        assertSameElements(expectedRecordIds, actualRecordIds,
                           "record IDs: " + testInfo);
      } else {
        for (SzDataSourceRecordSummary summary : entity.getRecordSummaries()) {
          recordCounts.put(summary.getDataSource(), summary.getRecordCount());
        }
      }
      assertEquals(expectedCounts, recordCounts,
                   "The record counts by data source do not match.  "
                       + testInfo);

    }

    // check the features
    if (forceMinimal != null && forceMinimal) {
      assertEquals(0, entity.getFeatures().size(),
                   "Features included in minimal results: " + testInfo
                       + " / " + entity.getFeatures());
    } else if (featureMode != null && featureMode == NONE) {
      assertEquals(
          0, entity.getFeatures().size(),
          "Features included despite NONE feature mode: " + testInfo
              + " / " + entity.getFeatures());

    } else {
      assertNotEquals(0, entity.getFeatures().size(),
                      "Features not present for entity: " + testInfo);

      Set<String> featureKeys = entity.getFeatures().keySet();
      if (withInternalFeatures) {
        if (featureKeys.contains("NAME") && !featureKeys.contains("NAME_KEY")) {
          fail("Missing NAME_KEY, but found NAME with internal features "
                   + "requested: " + testInfo + " / " + featureKeys);
        }
        if (featureKeys.contains("ADDRESS")
            && !featureKeys.contains("ADDR_KEY"))
        {
          fail("Missing ADDR_KEY, but found ADDRESS with internal features "
                   + "requested: " + testInfo + " / " + featureKeys);
        }
      } else {
        if (featureKeys.contains("NAME_KEY")) {
          fail("Found NAME_KEY with internal features suppressed: "
                   + testInfo + " / " + featureKeys);
        }
        if (featureKeys.contains("ADDR_KEY")) {
          fail("Found ADDR_KEY with internal features suppressed: "
                   + testInfo + " / " + featureKeys);
        }
      }
      // validate representative feature mode
      if (featureMode == REPRESENTATIVE) {
        entity.getFeatures().entrySet().forEach(entry -> {
          String                featureKey    = entry.getKey();
          List<SzEntityFeature> featureValues = entry.getValue();
          featureValues.forEach(featureValue -> {
            if (featureValue.getDuplicateValues().size() != 0) {
              fail("Duplicate feature values present for " + featureKey
                       + " feature despite REPRESENTATIVE feature mode: "
                       + testInfo + " / " + featureValue);
            }
          });
        });
      }

      // check if statistics are present
      entity.getFeatures().entrySet().forEach(entry -> {
        String                featureKey    = entry.getKey();
        List<SzEntityFeature> featureValues = entry.getValue();
        featureValues.forEach(featureValue -> {
          List<SzEntityFeatureDetail> list = featureValue.getFeatureDetails();
          for (SzEntityFeatureDetail detail: list) {
            if (withFeatureStats) {
              assertNotNull(detail.getStatistics(),
                            "Expected feature statistics: " + testInfo
                                + " / " + detail);
            } else {
              assertNull(detail.getStatistics(),
                         "Unexpected feature statistics: " + testInfo
                         + " / " + detail);
            }
          }
        });
      });

      // validate the feature counts (if any)
      if (expectedFeatureCounts != null) {
        expectedFeatureCounts.entrySet().forEach(entry -> {
          String featureKey = entry.getKey();
          int expectedCount = entry.getValue();
          List<SzEntityFeature> featureValues
              = entity.getFeatures().get(featureKey);
          assertEquals(expectedCount, featureValues.size(),
                       "Unexpected feature count for " + featureKey
                           + " feature: " + testInfo + " / " + featureValues);
        });
      }

      // validate the feature values (if any)
      if (primaryFeatureValues != null) {
        primaryFeatureValues.entrySet().forEach(entry -> {
          String      featureKey    = entry.getKey();
          Set<String> primaryValues = entry.getValue();

          List<SzEntityFeature> featureValues
              = entity.getFeatures().get(featureKey);

          primaryValues.forEach(primaryValue -> {
            boolean found = false;
            for (SzEntityFeature featureValue : featureValues) {
              if (primaryValue.equalsIgnoreCase(featureValue.getPrimaryValue())) {
                found = true;
                break;
              }
            }
            if (!found) {
              fail("Could not find \"" + primaryValue + "\" among the "
                       + featureKey + " primary feature values: " + testInfo
                       + " / " + featureValues);
            }
          });
        });
      }
      if (duplicateFeatureValues != null && (featureMode != REPRESENTATIVE)) {
        duplicateFeatureValues.entrySet().forEach(entry -> {
          String      featureKey      = entry.getKey();
          Set<String> duplicateValues = entry.getValue();

          List<SzEntityFeature> featureValues
              = entity.getFeatures().get(featureKey);

          duplicateValues.forEach(expectedDuplicate -> {
            boolean found = false;
            for (SzEntityFeature featureValue : featureValues) {
              for (String duplicateValue : featureValue.getDuplicateValues()) {
                if (expectedDuplicate.equalsIgnoreCase(duplicateValue)) {
                  found = true;
                  break;
                }
              }
            }
            if (!found) {
              fail("Could not find \"" + expectedDuplicate + "\" among the "
                       + featureKey + " duplicate feature values: " + testInfo
                       + " / " + featureValues);
            }
          });
        });
      }

      if ((forceMinimal == null || !forceMinimal)
          && (detailLevel != MINIMAL && detailLevel != BRIEF))
      {
        Date lastSeenTimestamp = entity.getLastSeenTimestamp();
        assertNotNull(lastSeenTimestamp,
                      "Last-seen timestamp is null: " + testInfo);
        long now = System.currentTimeMillis();
        long lastSeen = lastSeenTimestamp.getTime();
        assertTrue(now > lastSeen,
                   "Last-seen timestamp in the future: " + lastSeenTimestamp
                       + " / " + (new Date(now)) + " / " + testInfo);
      }

      // validate the features versus the data elements
      SzApiProvider provider = SzApiProvider.Factory.getProvider();
      entity.getFeatures().entrySet().forEach(entry -> {
        String featureKey = entry.getKey();
        List<SzEntityFeature> featureValues = entry.getValue();

        SzAttributeClass attrClass = SzAttributeClass.parseAttributeClass(
            provider.getAttributeClassForFeature(featureKey));

        if (attrClass == null) {
          // skip this feature if working with internal features
          if (withInternalFeatures) return;

          // otherwise fail
          fail("Unrecognized feature key (" + featureKey + "): " + testInfo
               + " / " + entity.getFeatures());
        }

        List<String> dataSet = getDataElements(entity, attrClass);
        if (dataSet == null) return;

        for (SzEntityFeature feature : featureValues) {
          String featureValue = feature.getPrimaryValue().trim().toUpperCase();
          boolean found = false;
          for (String dataValue : dataSet) {
            if (dataValue.toUpperCase().indexOf(featureValue) >= 0) {
              found = true;
              break;
            }
          }
          if (!found) {
            fail(featureKey + " feature value (" + featureValue
                     + ") not found in " + attrClass + " data values: "
                     + dataSet + " (" + testInfo + ")");
          }
        }
      });
    }

    // check if related entities are provided to validate
    if (relatedEntities != null) {
      if (relatedSuppressed == null || !relatedSuppressed) {
        // check if verifying the number of related entities
        if (relatedEntityCount != null) {
          assertEquals(relatedEntityCount, relatedEntities.size(),
                       "Unexpected number of related entities: "
                           + testInfo);
        }

        // check if verifying if related entities are partial
        if (relatedPartial != null || (forceMinimal != null && forceMinimal)) {
          boolean partial = ((relatedPartial != null && relatedPartial)
              || (forceMinimal != null && forceMinimal)
              || (featureMode == NONE));

          for (SzRelatedEntity related : relatedEntities) {
            if (related.isPartial() != partial) {
              if (partial) {
                fail("Entity " + entity.getEntityId() + " has a complete "
                         + "related entity (" + related.getEntityId()
                         + ") where partial entities were expected: " + testInfo);
              } else {
                fail("Entity " + entity.getEntityId() + " has a partial "
                         + "related entity (" + related.getEntityId()
                         + ") where complete entities were expected: " + testInfo);
              }
            }
          }
        }
      }
    }

    if (expectedDataValues != null
        && (forceMinimal == null || !forceMinimal)
        && (featureMode == null || featureMode != NONE))
    {
      expectedDataValues.entrySet().forEach(entry -> {
        SzAttributeClass attrClass      = entry.getKey();
        Set<String>      expectedValues = entry.getValue();
        List<String>     actualValues   = getDataElements(entity, attrClass);
        assertSameElements(expectedValues,
                                actualValues,
                                attrClass.toString() + " (" + testInfo + ")");
      });
    }
    if (expectedOtherDataValues != null
        && (forceMinimal == null || !forceMinimal) )
    {
      List<String> actualValues = entity.getOtherData();
      assertSameElements(expectedOtherDataValues, actualValues,
                              "OTHER DATA (" + testInfo + ")");
    }
  }

  /**
   * Gets the data elements from the specified entity for the given attribute
   * class.
   *
   * @param entity The entity to get the data from.
   * @param attrClass The attribute class identifying the type of data
   * @return The {@link List} of data elements.
   */
  public static List<String> getDataElements(SzResolvedEntity entity,
                                             SzAttributeClass attrClass)
  {
    switch (attrClass) {
      case NAME:
        return entity.getNameData();
      case CHARACTERISTIC:
        return entity.getCharacteristicData();
      case PHONE:
        return entity.getPhoneData();
      case IDENTIFIER:
        return entity.getIdentifierData();
      case ADDRESS:
        return entity.getAddressData();
      case RELATIONSHIP:
        return entity.getRelationshipData();
      default:
        return null;
    }
  }

  /**
   * Validates an {@link SzDataSourcesResponse} instance.
   *
   * @param response The response to validate.
   * @param selfLink The HTTP request URI
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   * @param expectRawData Whether or not to expect raw data.
   * @param expectedDataSources The expected data sources.
   */
  public static void validateDataSourcesResponse(
      SzDataSourcesResponse     response,
      SzHttpMethod              httpMethod,
      String                    selfLink,
      long                      maxDuration,
      boolean                   expectRawData,
      Map<String, SzDataSource> expectedDataSources)
  {
    validateDataSourcesResponse(null,
                                response,
                                httpMethod,
                                selfLink,
                                maxDuration,
                                expectRawData,
                                expectedDataSources);
  }

  /**
   * Validates an {@link SzDataSourcesResponse} instance.
   *
   * @param response The response to validate.
   * @param testInfo The optional test info describing the test.
   * @param selfLink The HTTP request URI
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   * @param expectRawData Whether or not to expect raw data.
   * @param expectedDataSources The expected data sources.
   */
  public static void validateDataSourcesResponse(
      String                    testInfo,
      SzDataSourcesResponse     response,
      SzHttpMethod              httpMethod,
      String                    selfLink,
      long                      maxDuration,
      boolean                   expectRawData,
      Map<String, SzDataSource> expectedDataSources)
  {
    validateBasics(testInfo,
                   response,
                   httpMethod,
                   selfLink,
                   maxDuration,
                   expectRawData);

    String testSuffix = (testInfo == null) ? "" : ": " + testInfo;
    String info = (testInfo == null) ? "" : "testInfo=[ " + testInfo + " ], ";

    SzDataSourcesResponseData data = response.getData();

    assertNotNull(data, "Response data is null" + testSuffix);

    Set<String> sources = data.getDataSources();
    Map<String, SzDataSource> details = data.getDataSourceDetails();

    assertNotNull(sources, "Data sources set is null" + testSuffix);
    assertNotNull(details, "Data source details map is null" + testSuffix);

    assertEquals(expectedDataSources.keySet(), sources,
                 "Unexpected or missing data sources in set.  "
                     + info
                     + "unexpected=[ "
                     + diffSets(sources, expectedDataSources.keySet())
                     + " ], missing=[ "
                     + diffSets(expectedDataSources.keySet(), sources)
                     + " ]" + testSuffix);

    assertEquals(expectedDataSources.keySet(), details.keySet(),
                 "Unexpected or missing data source details");

    details.entrySet().forEach(entry -> {
      String code = entry.getKey();
      SzDataSource source = entry.getValue();
      assertEquals(code, source.getDataSourceCode(),
                   "Data source code property key ("
                       + code + ") in details does not match the data source "
                       + "code in the corresponding detail object: "
                       + info + "detail=[ " + source.toString() + " ]");
    });

    expectedDataSources.values().forEach(expected -> {
      String code = expected.getDataSourceCode();
      SzDataSource actual = details.get(code);
      if (expected.getDataSourceId() != null) {
        assertEquals(expected, actual,
                     "Unexpected data source details" + testSuffix);
      }
    });

    if (expectRawData) {
      validateRawDataMap(testInfo, response.getRawData(), "DATA_SOURCES");
      Object array = ((Map) response.getRawData()).get("DATA_SOURCES");
      validateRawDataMapArray(
          testInfo, array,false,"DSRC_CODE", "DSRC_ID");
    }
  }

  /**
   * Validates an {@link SzDataSourceResponse} instance.
   *
   * @param response The response to validate.
   * @param expectedHttpMethod The expected HTTP method.
   * @param selfLink The HTTP request URI
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   * @param expectRawData Whether or not to expect raw data.
   * @param expectedDataSource The expected data source.
   */
  public static void validateDataSourceResponse(
      SzDataSourceResponse    response,
      SzHttpMethod            expectedHttpMethod,
      String                  selfLink,
      long                    maxDuration,
      boolean                 expectRawData,
      SzDataSource            expectedDataSource)
  {
    validateBasics(response,
                   expectedHttpMethod,
                   selfLink,
                   maxDuration,
                   expectRawData);

    SzDataSourceResponseData data = response.getData();

    assertNotNull(data, "Response data is null");

    SzDataSource dataSource = data.getDataSource();

    assertNotNull(dataSource, "Data source is null");

    assertEquals(expectedDataSource, dataSource,
                 "Unexpected data source");

    if (expectRawData) {
      validateRawDataMap(
          response.getRawData(), "DSRC_CODE", "DSRC_ID");
    }
  }

  private static Set diffSets(Set s1, Set s2) {
    Set diff = new LinkedHashSet<>(s1);
    diff.removeAll(s2);
    return diff;
  }

  /**
   * Validates an {@link SzAttributeTypesResponse} instance.
   *
   * @param response The response to validate.
   * @param selfLink The expected meta data self link.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   * @param expectRawData Whether or not to expect raw data.
   * @param expectedAttrTypeCodes The expected attribute type codes.
   */
  public static void validateAttributeTypesResponse(
      String                    testInfo,
      SzAttributeTypesResponse  response,
      String                    selfLink,
      long                      maxDuration,
      Boolean                   expectRawData,
      Set<String>               expectedAttrTypeCodes)
  {
    if (expectRawData == null) {
      expectRawData = false;
    }

    validateBasics(testInfo,
                   response,
                   selfLink,
                   maxDuration,
                   expectRawData);

    SzAttributeTypesResponseData data = response.getData();

    assertNotNull(data, "Response data is null: " + testInfo);

    List<SzAttributeType> attrTypes = data.getAttributeTypes();

    assertNotNull(attrTypes, "List of attribute types is null: " + testInfo);

    Map<String, SzAttributeType> map = new LinkedHashMap<>();
    for (SzAttributeType attrType : attrTypes) {
      map.put(attrType.getAttributeCode(), attrType);
    }

    assertEquals(expectedAttrTypeCodes, map.keySet(),
                 "Unexpected or missing attribute types: "
                     + "unexpected=[ "
                     + diffSets(map.keySet(), expectedAttrTypeCodes)
                     + " ], missing=[ "
                     + diffSets(expectedAttrTypeCodes, map.keySet())
                     + " ], testInfo=[ " + testInfo + " ]");

    if (expectRawData) {
      validateRawDataMap(
          response.getRawData(), true, "CFG_ATTR");

      Object attrs = ((Map) response.getRawData()).get("CFG_ATTR");

      validateRawDataMapArray(testInfo,
                              attrs,
                              false,
                              "DEFAULT_VALUE",
                              "ATTR_CODE",
                              "FELEM_REQ",
                              "ATTR_CLASS",
                              "INTERNAL",
                              "ATTR_ID",
                              "FTYPE_CODE",
                              "FELEM_CODE",
                              "ADVANCED");
    }
  }

  /**
   /**
   * Validates an {@link SzAttributeTypeResponse} instance.
   *
   * @param response The response to validate.
   * @param attributeCode The requested attribute code.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   * @param expectRawData Whether or not to expect raw data.
   */
  public static void validateAttributeTypeResponse(
      SzAttributeTypeResponse response,
      String                  selfLink,
      String                  attributeCode,
      long                    maxDuration,
      Boolean                 expectRawData)
  {
    if (expectRawData == null) {
      expectRawData = false;
    }

    validateBasics(response, selfLink, maxDuration, expectRawData);

    SzAttributeTypeResponseData data = response.getData();

    assertNotNull(data, "Response data is null");

    SzAttributeType attrType = data.getAttributeType();

    assertNotNull(attrType, "Attribute Type is null");

    assertEquals(attributeCode, attrType.getAttributeCode(),
                 "Unexpected attribute type code");

    if (expectRawData) {
      validateRawDataMap(response.getRawData(),
                              "DEFAULT_VALUE",
                              "ATTR_CODE",
                              "FELEM_REQ",
                              "ATTR_CLASS",
                              "INTERNAL",
                              "ATTR_ID",
                              "FTYPE_CODE",
                              "FELEM_CODE",
                              "ADVANCED");
    }
  }

  /**
   * Validates an {@link SzConfigResponse} instance.
   *
   * @param response The response to validate.
   * @param selfLink The expected meta data self link.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   * @param expectedDataSources The expected data sources.
   */
  public static void validateConfigResponse(
      SzConfigResponse        response,
      String                  selfLink,
      long                    maxDuration,
      Set<String>             expectedDataSources)
  {
    validateBasics(response, selfLink, maxDuration, true);

    Object rawData = response.getRawData();

    validateRawDataMap(rawData, true, "G2_CONFIG");

    Object g2Config = ((Map) rawData).get("G2_CONFIG");

    validateRawDataMap(g2Config,
                       false,
                       "CFG_ATTR",
                       "CFG_FELEM",
                       "CFG_DSRC");

    Object cfgDsrc = ((Map) g2Config).get("CFG_DSRC");

    validateRawDataMapArray(cfgDsrc,
                            false,
                            "DSRC_ID",
                            "DSRC_DESC",
                            "DSRC_CODE");

    Set<String> actualDataSources = new LinkedHashSet<>();
    for (Object dsrc : ((Collection) cfgDsrc)) {
      Map dsrcMap = (Map) dsrc;
      String dsrcCode = (String) dsrcMap.get("DSRC_CODE");
      actualDataSources.add(dsrcCode);
    }

    assertEquals(expectedDataSources, actualDataSources,
                 "Unexpected set of data sources in config.");
  }

  /**
   * Validates an {@link SzRecordResponse} instance.
   *
   * @param response The response to validate.
   * @param dataSourceCode The data source code for the requested record.
   * @param expectedRecordId The record ID for the requested record.
   * @param expectedNameData The expected name data or <tt>null</tt> if not
   *                         validating the name data.
   * @param expectedAddressData The expected address data or <tt>null</tt> if
   *                            not validating the address data.
   * @param expectedPhoneData The expected phone data or <tt>null</tt> if not
   *                          validating the phone data.
   * @param expectedIdentifierData The expected identifier data or <tt>null</tt>
   *                               if not validating the identifier data.
   * @param expectedAttributeData The expected attribute data or <tt>null</tt>
   *                              if not validating the attribute data.
   * @param expectedOtherData The expected other data or <tt>null</tt>
   *                          if not validating the other data.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   * @param expectRawData Whether or not to expect raw data.
   */
  public static void validateRecordResponse(
      SzRecordResponse  response,
      SzHttpMethod      httpMethod,
      String            selfLink,
      String            dataSourceCode,
      String            expectedRecordId,
      Set<String>       expectedNameData,
      Set<String>       expectedAddressData,
      Set<String>       expectedPhoneData,
      Set<String>       expectedIdentifierData,
      Set<String>       expectedAttributeData,
      Set<String>       expectedRelationshipData,
      Set<String>       expectedOtherData,
      long              maxDuration,
      Boolean           expectRawData)
  {
    if (expectRawData == null) {
      expectRawData = false;
    }

    validateBasics(response, httpMethod, selfLink, maxDuration);

    SzRecordResponseData data = response.getData();

    assertNotNull(data, "Response data is null");

    SzEntityRecord record = data.getRecord();

    assertNotNull(record, "Response record is null");

    String dataSource = record.getDataSource();
    assertNotNull(dataSource, "Data source is null");
    assertEquals(dataSourceCode, dataSource, "Unexpected data source value");

    String recordId = record.getRecordId();
    assertNotNull(recordId, "Record ID is null");
    assertEquals(expectedRecordId, recordId, "Unexpected record ID value");

    Date lastSeenTimestamp = record.getLastSeenTimestamp();
    assertNotNull(lastSeenTimestamp, "Last-seen timestamp is null: "
                  + record + " / " + response.getRawData());
    long now = System.currentTimeMillis();
    long lastSeen = lastSeenTimestamp.getTime();
    assertTrue(now > lastSeen,
               "Last-seen timestamp in the future: " + lastSeenTimestamp
                   + " / " + (new Date(now)));

    assertSameElements(
        expectedNameData, record.getNameData(), "names");
    assertSameElements(
        expectedAddressData, record.getAddressData(), "addresses");
    assertSameElements(
        expectedPhoneData, record.getPhoneData(), "phone numbers");
    assertSameElements(
        expectedIdentifierData, record.getIdentifierData(), "identifiers");
    assertSameElements(
        expectedAttributeData, record.getCharacteristicData(), "characteristics");
    assertSameElements(
        expectedRelationshipData, record.getRelationshipData(), "relationships");
    assertSameElements(
        expectedOtherData, record.getOtherData(), "other");

    if (expectRawData) {
      validateRawDataMap(response.getRawData(),
                              false,
                              "JSON_DATA",
                              "NAME_DATA",
                              "ATTRIBUTE_DATA",
                              "IDENTIFIER_DATA",
                              "ADDRESS_DATA",
                              "PHONE_DATA",
                              "RELATIONSHIP_DATA",
                              "ENTITY_DATA",
                              "OTHER_DATA",
                              "DATA_SOURCE",
                              "RECORD_ID");
    }

  }

  /**
   * Validates an {@link SzEntityResponse} instance.
   *
   * @param testInfo The test information describing the test.
   * @param response The response to validate.
   * @param selfLink The expected meta data self link.
   * @param withRaw <tt>true</tt> if requested with raw data, <tt>false</tt>
   *                if requested without raw data and <tt>null</tt> if this is
   *                not being validated.
   * @param withRelated The {@link SzRelationshipMode} value or <tt>null</tt>
   *                    if this aspect is not being validated.
   * @param detailLevel The {@link SzDetailLevel} describing the level of
   *                    detail that was requested for the entity.
   * @param forceMinimal <tt>true</tt> if requested with minimal data,
   *                     <tt>false</tt> if requested with standard data and
   *                     <tt>null</tt> if this aspect is not being validated.
   * @param featureMode The {@link SzFeatureMode} requested or
   *                    <tt>null</tt> if this is not being validated.
   * @param withFeatureStats <tt>true</tt> if request with feature statistics,
   *                         otherwise <tt>false</tt>.
   * @param withInternalFeatures <tt>true</tt> if request with internal features,
   *                            otherwise <tt>false</tt>.
   * @param expectedRecordCount The number of expected records for the entity,
   *                            or <tt>null</tt> if this is not being validated.
   * @param expectedRecordIds The expected record IDs for the entity to have or
   *                          <tt>null</tt> if this is not being validated.
   * @param relatedEntityCount The expected number of related entities or
   *                           <tt>null</tt> if this is not being validated.
   * @param expectedFeatureCounts The expected number of features by feature
   *                              type, or <tt>null</tt> if this is not being
   *                              validated.
   * @param primaryFeatureValues The expected primary feature values by feature
   *                             type, or <tt>null</tt> if this is not being
   *                             validated.
   * @param duplicateFeatureValues The expected duplicate fature values by
   *                               feature type, or <tt>null</tt> if this is not
   *                               being validated.
   * @param expectedDataValues The expected data values by attribute class, or
   *                           <tt>null</tt> if this is not being validated.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateEntityResponse(
      String                              testInfo,
      SzEntityResponse                    response,
      SzHttpMethod                        httpMethod,
      String                              selfLink,
      Boolean                             withRaw,
      SzRelationshipMode                  withRelated,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      boolean                             withFeatureStats,
      boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Set<SzRecordId>                     expectedRecordIds,
      Integer                             relatedEntityCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues,
      long                                maxDuration)
  {
    validateBasics(testInfo,
                   response,
                   httpMethod,
                   selfLink,
                   maxDuration);

    SzEntityData entityData = response.getData();

    assertNotNull(entityData, "Response data is null: " + testInfo);

    SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();

    assertNotNull(resolvedEntity, "Resolved entity is null: " + testInfo);

    List<SzRelatedEntity> relatedEntities = entityData.getRelatedEntities();

    assertNotNull(relatedEntities,
                  "Related entities list is null: " + testInfo);

    validateEntity(
        testInfo,
        resolvedEntity,
        relatedEntities,
        forceMinimal,
        detailLevel,
        featureMode,
        withFeatureStats,
        withInternalFeatures,
        expectedRecordCount,
        expectedRecordIds,
        (withRelated == SzRelationshipMode.NONE),
        (withRelated == SzRelationshipMode.NONE ? 0 : relatedEntityCount),
        (withRelated != SzRelationshipMode.FULL),
        expectedFeatureCounts,
        primaryFeatureValues,
        duplicateFeatureValues,
        expectedDataValues,
        (detailLevel != null && detailLevel != VERBOSE)
            ? Collections.emptySet() : expectedOtherDataValues);

    if (withRaw != null && withRaw) {
      if ((withRelated == FULL) && (forceMinimal == null || !forceMinimal))
      {
        validateRawDataMap(testInfo,
                           response.getRawData(),
                           true,
                           "ENTITY_PATHS", "ENTITIES");

        Object entities = ((Map) response.getRawData()).get("ENTITIES");
        validateRawDataMapArray(testInfo,
                                entities,
                                false,
                                "RESOLVED_ENTITY",
                                "RELATED_ENTITIES");


        for (Object entity : ((Collection) entities)) {
          validateRawDataMap(
              testInfo,
              ((Map) entity).get("RESOLVED_ENTITY"),
              false,
              rawEntityKeys(forceMinimal, detailLevel, featureMode));
        }

      } else {
        if (withRelated == PARTIAL) {
          validateRawDataMap(testInfo,
                             response.getRawData(),
                             false,
                             "RESOLVED_ENTITY",
                             "RELATED_ENTITIES");
        } else {
          validateRawDataMap(testInfo,
                             response.getRawData(),
                             false,
                             "RESOLVED_ENTITY");
        }

        Object entity = ((Map) response.getRawData()).get("RESOLVED_ENTITY");
        validateRawDataMap(
            testInfo,
            entity,
            false,
            rawEntityKeys(forceMinimal, detailLevel, featureMode));

      }
    }
  }

  /**
   * Validates an {@link SzEntityResponse} instance.
   *
   * @param testInfo The test information describing the test.
   * @param response The response to validate.
   * @param selfLink The expected meta data self link.
   * @param withRaw <tt>true</tt> if requested with raw data, <tt>false</tt>
   *                if requested without raw data and <tt>null</tt> if this is
   *                not being validated.
   * @param detailLevel The {@link SzDetailLevel} describing the level of
   *                    detail that was requested for the entity.
   * @param forceMinimal <tt>true</tt> if requested with minimal data,
   *                     <tt>false</tt> if requested with standard data and
   *                     <tt>null</tt> if this aspect is not being validated.
   * @param featureMode The {@link SzFeatureMode} requested or
   *                    <tt>null</tt> if this is not being validated.
   * @param withFeatureStats <tt>true</tt> if request with feature statistics,
   *                         otherwise <tt>false</tt>.
   * @param withInternalFeatures <tt>true</tt> if request with internal features,
   *                            otherwise <tt>false</tt>.
   * @param expectedRecordCount The number of expected records for the entity,
   *                            or <tt>null</tt> if this is not being validated.
   * @param expectedRecordIds The expected record IDs for the entity to have or
   *                          <tt>null</tt> if this is not being validated.
   * @param expectedFeatureCounts The expected number of features by feature
   *                              type, or <tt>null</tt> if this is not being
   *                              validated.
   * @param primaryFeatureValues The expected primary feature values by feature
   *                             type, or <tt>null</tt> if this is not being
   *                             validated.
   * @param duplicateFeatureValues The expected duplicate fature values by
   *                               feature type, or <tt>null</tt> if this is not
   *                               being validated.
   * @param expectedDataValues The expected data values by attribute class, or
   *                           <tt>null</tt> if this is not being validated.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateVirtualEntityResponse(
      String                              testInfo,
      SzVirtualEntityResponse             response,
      SzHttpMethod                        httpMethod,
      String                              selfLink,
      Boolean                             withRaw,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      boolean                             withFeatureStats,
      boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Set<SzRecordId>                     expectedRecordIds,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues,
      long                                maxDuration)
  {
    validateBasics(testInfo,
                   response,
                   httpMethod,
                   selfLink,
                   maxDuration);

    SzVirtualEntityData entityData = response.getData();

    assertNotNull(entityData, "Response data is null: " + testInfo);

    SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();

    assertNotNull(resolvedEntity, "Resolved entity is null: " + testInfo);

    validateEntity(
        testInfo,
        resolvedEntity,
        null,
        forceMinimal,
        detailLevel,
        featureMode,
        withFeatureStats,
        withInternalFeatures,
        expectedRecordCount,
        expectedRecordIds,
        true,
        null,
        null,
        expectedFeatureCounts,
        primaryFeatureValues,
        duplicateFeatureValues,
        expectedDataValues,
        (detailLevel != null && detailLevel != VERBOSE)
            ? Collections.emptySet() : expectedOtherDataValues);

    if (withRaw != null && withRaw) {
      validateRawDataMap(testInfo,
                         response.getRawData(),
                         false,
                         "RESOLVED_ENTITY");

      Object entity = ((Map) response.getRawData()).get("RESOLVED_ENTITY");
      validateRawDataMap(
          testInfo,
          entity,
          false,
          rawEntityKeys(forceMinimal, detailLevel, featureMode));
    }
  }

  /**
   * Validate an {@link SzAttributeSearchResponse} instance.
   *
   * @param testInfo The test information describing the test.
   * @param response The response to validate.
   * @param selfLink The expected meta data self link.
   * @param expectedCount The number of expected matching entities for the
   *                      search, or <tt>null</tt> if this is not being
   *                      validated.
   * @param withRelationships <tt>true</tt> if requested with relationship
   *                          information should be included with the entity
   *                          results, <tt>false</tt> or <tt>null</tt> if the
   *                          relationship information should be excluded.
   * @param forceMinimal <tt>true</tt> if requested with minimal data,
   *                     <tt>false</tt> if requested with standard data and
   *                     <tt>null</tt> if this aspect is not being validated.
   * @param featureMode The {@link SzFeatureMode} requested or
   *                         <tt>null</tt> if this is not being validated.
   * @param withFeatureStats <tt>true</tt> if request with feature statistics,
   *                         otherwise <tt>false</tt>.
   * @param withInternalFeatures <tt>true</tt> if request with internal features,
   *                            otherwise <tt>false</tt>.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   * @param expectRawData Whether or not to expect raw data.
   */
  public static void validateSearchResponse(
      String                    testInfo,
      SzAttributeSearchResponse response,
      SzHttpMethod              httpMethod,
      String                    selfLink,
      Integer                   expectedCount,
      Boolean                   withRelationships,
      Boolean                   forceMinimal,
      SzDetailLevel             detailLevel,
      SzFeatureMode             featureMode,
      boolean                   withFeatureStats,
      boolean                   withInternalFeatures,
      long                      maxDuration,
      Boolean                   expectRawData)
  {
    if (expectRawData == null) {
      expectRawData = false;
    }

    validateBasics(testInfo, response, httpMethod, selfLink, maxDuration);

    SzAttributeSearchResponseData data = response.getData();

    assertNotNull(data, "Response data is null: " + testInfo);

    List<SzAttributeSearchResult> results = data.getSearchResults();

    assertNotNull(results, "Result list is null: " + testInfo);

    if (expectedCount != null) {
      assertEquals(expectedCount, results.size(),
                   "Unexpected number of results: " + testInfo);
    }

    for (SzAttributeSearchResult result : results) {

      validateEntity(testInfo,
                     result,
                     result.getRelatedEntities(),
                     forceMinimal,
                     detailLevel,
                     featureMode,
                     withFeatureStats,
                     withInternalFeatures,
                     null,
                     null,
                     (withRelationships == null ? false : withRelationships),
                     null,
                     true,
                     null,
                     null,
                     null,
                     null,
                     null);

      Map<String, List<SzSearchFeatureScore>> featureScores
          = result.getFeatureScores();
      assertNotNull(featureScores, "Feature scores was null for entity "
          + result.getEntityId() + ": " + testInfo);
      if (featureScores.containsKey("NAME")) {
        Integer bestNameScore = result.getBestNameScore();
        assertNotNull(bestNameScore, "Best name score is null for "
            + "entity " + result.getEntityId() + " even though NAME feature "
            + "scores exist (" + featureScores.get("NAME") + "): " + testInfo);

        List<SzSearchFeatureScore> nameScores = featureScores.get("NAME");
        int expectedBestNameScore = -1;
        for (SzSearchFeatureScore nameScore : nameScores) {
          SzNameScoring nameScoringDetails = nameScore.getNameScoringDetails();
          assertEquals(nameScore.getScore(),
                       nameScoringDetails.asFullScore(),
                       "Overall score not equal to overall name score "
                           + "for entity " + result.getEntityId() + " with name "
                           + "scoring details (" + nameScoringDetails
                           + "): " + testInfo);
          Integer fullNameScore = nameScoringDetails.getFullNameScore();
          Integer orgNameScore  = nameScoringDetails.getOrgNameScore();
          if (fullNameScore == null) fullNameScore = -1;
          if (orgNameScore == null) orgNameScore = -1;
          int maxScore = Integer.max(fullNameScore, orgNameScore);
          if (maxScore > expectedBestNameScore) {
            expectedBestNameScore = maxScore;
          }
        }
        assertEquals(bestNameScore, expectedBestNameScore,
                     "Unexpected best name score for entity "
                         + result.getEntityId() + " with name feature scores ("
                         + nameScores + "): " + testInfo);

      }
    }

    if (expectRawData) {
      validateRawDataMap(testInfo,
                         response.getRawData(),
                         false,
                         "RESOLVED_ENTITIES");

      Object entities = ((Map) response.getRawData()).get("RESOLVED_ENTITIES");
      validateRawDataMapArray(testInfo,
                              entities,
                              false,
                              "MATCH_INFO", "ENTITY");
      for (Object obj : ((Collection) entities)) {
        Object matchInfo = ((Map) obj).get("MATCH_INFO");
        validateRawDataMap(testInfo,
                           matchInfo,
                           false,
                           "MATCH_LEVEL",
                           "MATCH_KEY",
                           "ERRULE_CODE",
                           "FEATURE_SCORES");
        Object entity = ((Map) obj).get("ENTITY");
        Object resolvedEntity = ((Map) entity).get("RESOLVED_ENTITY");
        validateRawDataMap(
            testInfo,
            resolvedEntity,
            false,
            rawEntityKeys(forceMinimal, detailLevel, featureMode));
      }
    }

  }

  /**
   * Creates an array of {@link String} property names that are expected for an
   * entity.
   *
   * @param forceMinimal Flag indicating if minimal format is forced.
   * @param detailLevel The {@link SzDetailLevel} for the detail level.
   * @param featureMode The {@link SzFeatureMode} for the expected features.
   *
   * @return An array of {@link String} property names that are expected for an
   *         entity.
   */
  public static String[] rawEntityKeys(
      Boolean                   forceMinimal,
      SzDetailLevel             detailLevel,
      SzFeatureMode             featureMode)
  {
    boolean minForced = (forceMinimal != null && forceMinimal.booleanValue());
    List<String> expectedKeys = new LinkedList<>();
    expectedKeys.add("ENTITY_ID");
    if (minForced && detailLevel != SUMMARY) {
      expectedKeys.add("RECORDS");
    }

    // check if minimal is not forced
    if (!minForced) {
      // check if features are expected
      if (featureMode != NONE) {
        expectedKeys.add("FEATURES");
      }

      // check if the record summary is expected
      if (detailLevel != MINIMAL && detailLevel != BRIEF) {
        expectedKeys.add("RECORD_SUMMARY");
      }
    }
    String[] result = new String[expectedKeys.size()];
    return expectedKeys.toArray(result);
  }

  /**
   * Validates an {@Link SzLoadRecordResponse} instance.
   *
   * @param response The response to validate.
   * @param httpMethod The HTTP method used to load the record.
   * @param dataSourceCode The data source code fo the loaded record.
   * @param expectedRecordId The record ID of the loaded record.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateLoadRecordResponse(
      SzLoadRecordResponse  response,
      SzHttpMethod          httpMethod,
      String                selfLink,
      String                dataSourceCode,
      String                expectedRecordId,
      Boolean               withInfo,
      Boolean               withRaw,
      Integer               expectedAffectedCount,
      Integer               expectedFlaggedCount,
      Set<String>           expectedFlags,
      long                  maxDuration)
  {
    try {
      String testInfo = "method=[ " + httpMethod + " ], path=[ " + selfLink
          + " ], dataSource=[ " + dataSourceCode + " ], expectedRecordId=[ "
          + expectedRecordId + " ], withInfo=[ " + withInfo + " ], withRaw=[ "
          + withRaw + " ]";

      validateBasics(testInfo, response, httpMethod, selfLink, maxDuration);

      SzLoadRecordResponseData data = response.getData();

      assertNotNull(data, "Response data is null: " + testInfo);

      String recordId = data.getRecordId();

      assertNotNull(recordId, "Record ID is null: " + testInfo);

      if (expectedRecordId != null) {
        assertEquals(expectedRecordId, recordId,
                     "Unexpected record ID value: " + testInfo);
      }

      // if withInfo is null then don't check the info at all (or raw data)
      if (withInfo == null) return;

      // check for info
      SzResolutionInfo info = data.getInfo();
      if (withInfo) {
        assertNotNull(info, "Info requested, but was null: " + testInfo);
      } else {
        assertNull(info, "Info not requested, but was found: " + testInfo);
      }

      if (withInfo) {
        if (expectedRecordId != null) {
          assertEquals(expectedRecordId, info.getRecordId(),
                       "Unexpected record ID in info: " + testInfo);
        }
        if (dataSourceCode != null) {
          assertEquals(dataSourceCode, info.getDataSource(),
                       "Unexpected data source in info: " + testInfo);
        }
        // check the affected entities
        if (expectedAffectedCount != null && expectedAffectedCount > 0) {
          Set<Long> affected = info.getAffectedEntities();
          assertNotNull(affected,
                        "Affected entities set is null: " + testInfo);
          assertEquals(expectedAffectedCount, affected.size(),
                       "Affected entities set is the wrong size: "
                           + affected);
        }

        // check the interesting entites
        if (expectedFlaggedCount != null && expectedFlaggedCount > 0) {
          List<SzFlaggedEntity> flagged = info.getFlaggedEntities();
          assertNotNull(flagged,
                        "Flagged entities list is null: " + testInfo);
          assertEquals(expectedAffectedCount, flagged.size(),
                       "Flagged entities set is the wrong size: "
                           + flagged);

          if (expectedFlags != null && expectedFlags.size() > 0) {
            Set<String> entityFlags = new LinkedHashSet<>();
            for (SzFlaggedEntity flaggedEntity : flagged) {
              entityFlags.addAll(flaggedEntity.getFlags());
            }
            assertEquals(expectedFlags, entityFlags,
                         "Unexpected flags for flagged entities: "
                             + flagged);

            Set<String> recordFlags = new LinkedHashSet<>();
            for (SzFlaggedEntity flaggedEntity : flagged) {
              for (SzFlaggedRecord flaggedRecord : flaggedEntity.getSampleRecords()) {
                recordFlags.addAll(flaggedRecord.getFlags());
              }
            }
            assertEquals(expectedFlags, recordFlags,
                         "Unexpected flags for flagged records: "
                             + flagged);
          }
        }
      }

      // check for raw data
      if (withInfo && withRaw != null) {
        Object rawData = response.getRawData();
        if (withRaw) {
          assertNotNull(rawData, "Raw data requested, but was null: "
              + testInfo);

          validateRawDataMap(
              rawData,
              false,
              "DATA_SOURCE", "RECORD_ID");

          // check the raw data affected entities
          if (expectedAffectedCount != null && expectedAffectedCount > 0) {
            validateRawDataMap(
                rawData,
                false,
                "AFFECTED_ENTITIES");

            Object array = ((Map) response.getRawData()).get("AFFECTED_ENTITIES");
            validateRawDataMapArray(
                testInfo, array, false, "ENTITY_ID");
          }

          // check the raw data interesting entities
          if (expectedFlaggedCount != null && expectedFlaggedCount > 0) {
            validateRawDataMap(
                rawData,
                false,
                "INTERESTING_ENTITIES");

            Object object = ((Map) response.getRawData()).get("INTERESTING_ENTITIES");
            validateRawDataMap(
                testInfo, object, false, "ENTITIES");

            Object array = ((Map) object).get("ENTITIES");
            validateRawDataMapArray(
                testInfo, array, false,
                "ENTITY_ID", "DEGREES", "FLAGS",
                "SAMPLE_RECORDS");
          }

        } else {
          assertNull(rawData, "Raw data not requested, but was found: "
              + testInfo);
        }
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Validates an {@Link SzReevaluateRecordResponse} instance.
   *
   * @param response The response to validate.
   * @param httpMethod The HTTP method used to load the record.
   * @param dataSourceCode The data source code fo the loaded record.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateReevaluateResponse(
      SzReevaluateResponse  response,
      SzHttpMethod          httpMethod,
      String                selfLink,
      Boolean               withInfo,
      Boolean               withRaw,
      String                dataSourceCode,
      String                expectedRecordId,
      Integer               expectedAffectedCount,
      Integer               expectedFlaggedCount,
      Set<String>           expectedFlags,
      long                  maxDuration)
  {
    try {
      String testInfo = "method=[ " + httpMethod + " ], path=[ " + selfLink
          + " ], dataSource=[ " + dataSourceCode + " ], expectedRecordId=[ "
          + expectedRecordId + " ], withInfo=[ " + withInfo + " ], withRaw=[ "
          + withRaw + " ]";

      validateBasics(testInfo, response, httpMethod, selfLink, maxDuration);

      SzReevaluateResponseData data = response.getData();

      assertNotNull(data, "Response data is null: " + testInfo);

      // if withInfo is null then don't check the info at all (or raw data)
      if (withInfo == null) return;

      // check for info
      SzResolutionInfo info = data.getInfo();
      if (withInfo) {
        assertNotNull(info, "Info requested, but was null: " + testInfo);
      } else {
        assertNull(info, "Info not requested, but was found: " + testInfo);
      }

      if (withInfo) {
        if (expectedRecordId != null) {
          assertEquals(expectedRecordId, info.getRecordId(),
                       "Unexpected record ID in info: " + testInfo);
        }
        if (dataSourceCode != null) {
          assertEquals(dataSourceCode, info.getDataSource(),
                       "Unexpected data source in info: " + testInfo);
        }
        // check the affected entities
        if (expectedAffectedCount != null && expectedAffectedCount > 0) {
          Set<Long> affected = info.getAffectedEntities();
          assertNotNull(affected,
                        "Affected entities set is null: " + testInfo);
          assertEquals(expectedAffectedCount, affected.size(),
                       "Affected entities set is the wrong size: "
                           + affected);
        }

        // check the interesting entites
        if (expectedFlaggedCount != null && expectedFlaggedCount > 0) {
          List<SzFlaggedEntity> flagged = info.getFlaggedEntities();
          assertNotNull(flagged,
                        "Flagged entities list is null: " + testInfo);
          assertEquals(expectedAffectedCount, flagged.size(),
                       "Flagged entities set is the wrong size: "
                           + flagged);

          if (expectedFlags != null && expectedFlags.size() > 0) {
            Set<String> entityFlags = new LinkedHashSet<>();
            for (SzFlaggedEntity flaggedEntity : flagged) {
              entityFlags.addAll(flaggedEntity.getFlags());
            }
            assertEquals(expectedFlags, entityFlags,
                         "Unexpected flags for flagged entities: "
                             + flagged);

            Set<String> recordFlags = new LinkedHashSet<>();
            for (SzFlaggedEntity flaggedEntity : flagged) {
              for (SzFlaggedRecord flaggedRecord : flaggedEntity.getSampleRecords()) {
                recordFlags.addAll(flaggedRecord.getFlags());
              }
            }
            assertEquals(expectedFlags, recordFlags,
                         "Unexpected flags for flagged records: "
                             + flagged);
          }
        }
      }

      // check for raw data
      if (withInfo && withRaw != null) {
        Object rawData = response.getRawData();
        if (withRaw) {
          assertNotNull(rawData, "Raw data requested, but was null: "
              + testInfo);

          validateRawDataMap(
              rawData,
              false,
              "DATA_SOURCE", "RECORD_ID");

          // check the raw data affected entities
          if (expectedAffectedCount != null && expectedAffectedCount > 0) {
            validateRawDataMap(
                rawData,
                false,
                "AFFECTED_ENTITIES");

            Object array = ((Map) response.getRawData()).get("AFFECTED_ENTITIES");
            validateRawDataMapArray(
                testInfo, array, false, "ENTITY_ID");
          }

          // check the raw data interesting entities
          if (expectedFlaggedCount != null && expectedFlaggedCount > 0) {
            validateRawDataMap(
                rawData,
                false,
                "INTERESTING_ENTITIES");

            Object object = ((Map) response.getRawData()).get("INTERESTING_ENTITIES");
            validateRawDataMap(
                testInfo, object, false, "ENTITIES");

            Object array = ((Map) object).get("ENTITIES");
            validateRawDataMapArray(
                testInfo, array, false,
                "ENTITY_ID", "DEGREES", "FLAGS",
                "SAMPLE_RECORDS");
          }

        } else {
          assertNull(rawData, "Raw data not requested, but was found: "
              + testInfo);
        }
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
  }


  /**
   * Validates an {@Link SzDeleteRecordResponse} instance.
   *
   * @param response The response to validate.
   * @param httpMethod The HTTP method used to load the record.
   * @param dataSourceCode The data source code fo the loaded record.
   * @param maxDuration The maximum duration for the timers in nanoseconds.
   */
  public static void validateDeleteRecordResponse(
      SzDeleteRecordResponse  response,
      SzHttpMethod            httpMethod,
      String                  selfLink,
      Boolean                 withInfo,
      Boolean                 withRaw,
      String                  dataSourceCode,
      String                  expectedRecordId,
      Integer                 expectedAffectedCount,
      Integer                 expectedFlaggedCount,
      Set<String>             expectedFlags,
      long                    maxDuration)
  {
    try {
      String testInfo = "method=[ " + httpMethod + " ], path=[ " + selfLink
          + " ], dataSource=[ " + dataSourceCode + " ], expectedRecordId=[ "
          + expectedRecordId + " ], withInfo=[ " + withInfo + " ], withRaw=[ "
          + withRaw + " ]";

      validateBasics(testInfo, response, httpMethod, selfLink, maxDuration);

      SzDeleteRecordResponseData data = response.getData();

      assertNotNull(data, "Response data is null: " + testInfo);

      // if withInfo is null then don't check the info at all (or raw data)
      if (withInfo == null) return;

      // check for info
      SzResolutionInfo info = data.getInfo();
      if (withInfo) {
        assertNotNull(info, "Info requested, but was null: " + testInfo);
      } else {
        assertNull(info, "Info not requested, but was found: " + testInfo);
      }

      if (withInfo) {
        if (expectedRecordId != null) {
          assertEquals(expectedRecordId, info.getRecordId(),
                       "Unexpected record ID in info: " + testInfo);
        }
        if (dataSourceCode != null) {
          assertEquals(dataSourceCode, info.getDataSource(),
                       "Unexpected data source in info: " + testInfo);
        }
        // check the affected entities
        if (expectedAffectedCount != null && expectedAffectedCount >= 0) {
          Set<Long> affected = info.getAffectedEntities();
          assertNotNull(affected,
                        "Affected entities set is null: " + testInfo);
          assertEquals(expectedAffectedCount, affected.size(),
                       "Affected entities set is the wrong size: "
                           + affected);
        }

        // check the interesting entites
        if (expectedFlaggedCount != null && expectedFlaggedCount >= 0) {
          List<SzFlaggedEntity> flagged = info.getFlaggedEntities();
          assertNotNull(flagged,
                        "Flagged entities list is null: " + testInfo);
          assertEquals(expectedFlaggedCount, flagged.size(),
                       "Flagged entities set is the wrong size: "
                           + flagged);

          if (expectedFlags != null && expectedFlags.size() > 0) {
            Set<String> entityFlags = new LinkedHashSet<>();
            for (SzFlaggedEntity flaggedEntity : flagged) {
              entityFlags.addAll(flaggedEntity.getFlags());
            }
            assertEquals(expectedFlags, entityFlags,
                         "Unexpected flags for flagged entities: "
                             + flagged);

            Set<String> recordFlags = new LinkedHashSet<>();
            for (SzFlaggedEntity flaggedEntity : flagged) {
              for (SzFlaggedRecord flaggedRecord : flaggedEntity.getSampleRecords()) {
                recordFlags.addAll(flaggedRecord.getFlags());
              }
            }
            assertEquals(expectedFlags, recordFlags,
                         "Unexpected flags for flagged records: "
                             + flagged);
          }
        }
      }

      // check for raw data
      if (withInfo && withRaw != null) {
        Object rawData = response.getRawData();
        if (withRaw) {
          assertNotNull(rawData, "Raw data requested, but was null: "
              + testInfo);

          validateRawDataMap(
              rawData,
              false,
              "DATA_SOURCE", "RECORD_ID");

          // check the raw data affected entities
          if (expectedAffectedCount != null && expectedAffectedCount > 0) {
            validateRawDataMap(
                rawData,
                false,
                "AFFECTED_ENTITIES");

            Object array = ((Map) response.getRawData()).get("AFFECTED_ENTITIES");
            validateRawDataMapArray(
                testInfo, array, false, "ENTITY_ID");
          }

          // check the raw data interesting entities
          if (expectedFlaggedCount != null && expectedFlaggedCount > 0) {
            validateRawDataMap(
                rawData,
                false,
                "INTERESTING_ENTITIES");

            Object object = ((Map) response.getRawData()).get("INTERESTING_ENTITIES");
            validateRawDataMap(
                testInfo, object, false, "ENTITIES");

            Object array = ((Map) object).get("ENTITIES");
            validateRawDataMapArray(
                testInfo, array, false,
                "ENTITY_ID", "DEGREES", "FLAGS",
                "SAMPLE_RECORDS");
          }

        } else {
          assertNull(rawData, "Raw data not requested, but was found: "
              + testInfo);
        }
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
  }


  /**
   * Validates a license response.
   *
   * @param response
   * @param selfLink
   * @param maxDuration
   * @param expectRawData
   * @param licenseTypePattern
   * @param expectedRecordLimit
   */
  public static void validateLicenseResponse(
      SzLicenseResponse response,
      String            selfLink,
      long              maxDuration,
      Boolean           expectRawData,
      Pattern           licenseTypePattern,
      Long              expectedRecordLimit)
  {
    if (expectRawData == null) {
      expectRawData = false;
    }

    validateBasics(response, selfLink, maxDuration, expectRawData);

    SzLicenseResponseData data = response.getData();

    assertNotNull(data, "Response data is null");

    SzLicenseInfo licenseInfo = data.getLicense();

    assertNotNull(licenseInfo, "License data is null");

    if (expectedRecordLimit != null) {
      assertEquals(expectedRecordLimit,
                   licenseInfo.getRecordLimit(),
                   "Record limit wrong");
    }

    if (licenseTypePattern != null) {
      String licenseType = licenseInfo.getLicenseType();

      assertTrue(
          licenseTypePattern.matcher(licenseType).matches(),
          "Unexpected license type: expectedPattern=[ "
              + licenseTypePattern + " ], actual=[ " + licenseType + " ]");

    }

    if (expectRawData) {
      validateRawDataMap(
          response.getRawData(),
          "customer", "contract", "issueDate", "licenseType",
          "licenseLevel", "billing", "expireDate", "recordLimit");

    }
  }


  /**
   *
   * @param response
   * @param selfLink
   * @param maxDuration
   * @param expectRawData
   * @param repoInitJson
   */
  public static void validateVersionResponse(
      SzVersionResponse  response,
      String             selfLink,
      long               maxDuration,
      Boolean            expectRawData,
      String             repoInitJson)
  {
    if (expectRawData == null) {
      expectRawData = false;
    }

    validateBasics(response, selfLink, maxDuration, expectRawData);

    SzVersionInfo info = response.getData();

    assertNotNull(info, "Response data is null");

    assertEquals(BuildInfo.MAVEN_VERSION,
                 info.getApiServerVersion(),
                 "API Server Version wrong");

    assertEquals(BuildInfo.REST_API_VERSION,
                 info.getRestApiVersion(),
                 "REST API Version wrong");

    // assume we can reinitialize the product API since it does not really do
    // anything when we initialize it
    G2Product product = NativeApiFactory.createProductApi();
    product.init("testApiServer", repoInitJson, false);
    try {
      String versionJson = product.version();

      JsonObject jsonObject = JsonUtilities.parseJsonObject(versionJson);
      String expectedVersion = JsonUtilities.getString(jsonObject, "VERSION");
      String expectedBuildNum = JsonUtilities.getString(jsonObject, "BUILD_NUMBER");

      JsonObject subObject = JsonUtilities.getJsonObject(
          jsonObject, "COMPATIBILITY_VERSION");

      String configCompatVers = JsonUtilities.getString(subObject,
                                                    "CONFIG_VERSION");

      assertEquals(expectedVersion, info.getNativeApiVersion(),
                   "Native API Version wrong");

      assertEquals(expectedBuildNum, info.getNativeApiBuildNumber(),
                   "Native API Build Number wrong");

      assertEquals(configCompatVers, info.getConfigCompatibilityVersion(),
                   "Native API Config Compatibility wrong");
    } finally {
      product.destroy();

    }
    if (expectRawData) {
      validateRawDataMap(
          response.getRawData(),
          false,
          "VERSION", "BUILD_NUMBER", "BUILD_DATE", "COMPATIBILITY_VERSION");
    }
  }

}
