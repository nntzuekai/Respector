package com.senzing.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Engine;
import com.senzing.gen.api.invoker.ApiClient;
import com.senzing.gen.api.services.EntityDataApi;
import com.senzing.repomgr.RepositoryManager;
import com.senzing.util.JsonUtilities;
import com.senzing.util.SemanticVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.util.*;

import static com.senzing.api.model.SzDetailLevel.VERBOSE;
import static com.senzing.api.model.SzHttpMethod.GET;
import static com.senzing.api.model.SzHttpMethod.POST;
import static com.senzing.api.model.SzAttributeClass.*;
import static com.senzing.util.CollectionUtilities.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static com.senzing.api.model.SzFeatureMode.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static com.senzing.api.services.ResponseValidators.*;
import static com.senzing.api.model.SzRelationshipMode.*;
import static com.senzing.api.model.SzAttributeSearchResultType.*;
import static com.senzing.api.services.EntityDataServices.*;

@TestInstance(Lifecycle.PER_CLASS)
public class EntityDataReadServicesTest extends AbstractServiceTest {
  private static final long RANDOM_SEED = 1234567890L;

  private static final String PASSENGERS = "PASSENGERS";
  private static final String EMPLOYEES  = "EMPLOYEES";
  private static final String VIPS       = "VIPS";
  private static final String MARRIAGES  = "MARRIAGES";

  private static final SzRecordId ABC123
      = SzRecordId.FACTORY.create(PASSENGERS,"ABC123");
  private static final SzRecordId DEF456
      = SzRecordId.FACTORY.create(PASSENGERS, "DEF456");
  private static final SzRecordId GHI789
      = SzRecordId.FACTORY.create(PASSENGERS, "GHI789");
  private static final SzRecordId JKL012
      = SzRecordId.FACTORY.create(PASSENGERS, "JKL012");
  private static final SzRecordId MNO345
      = SzRecordId.FACTORY.create(EMPLOYEES, "MNO345");
  private static final SzRecordId PQR678
      = SzRecordId.FACTORY.create(EMPLOYEES, "PQR678");
  private static final SzRecordId STU901
      = SzRecordId.FACTORY.create(VIPS, "STU901");
  private static final SzRecordId XYZ234
      = SzRecordId.FACTORY.create(VIPS, "XYZ234");
  private static final SzRecordId ZYX321
      = SzRecordId.FACTORY.create(EMPLOYEES, "ZYX321");
  private static final SzRecordId CBA654
      = SzRecordId.FACTORY.create(EMPLOYEES, "CBA654");

  private static final SzRecordId BCD123
      = SzRecordId.FACTORY.create(MARRIAGES, "BCD123");
  private static final SzRecordId CDE456
      = SzRecordId.FACTORY.create(MARRIAGES, "CDE456");
  private static final SzRecordId EFG789
      = SzRecordId.FACTORY.create(MARRIAGES, "EFG789");
  private static final SzRecordId FGH012
      = SzRecordId.FACTORY.create(MARRIAGES, "FGH012");

  private EntityDataServices entityDataServices;
  private EntityDataApi entityDataApi;

  @BeforeAll
  public void initializeEnvironment() {
    this.beginTests();
    this.initializeTestEnvironment();
    this.entityDataServices = new EntityDataServices();
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(this.formatServerUri(""));
    this.entityDataApi = new EntityDataApi(apiClient);
  }

  /**
   * Overridden to configure some data sources.
   */
  protected void prepareRepository() {
    File repoDirectory = this.getRepositoryDirectory();

    Set<String> dataSources = new LinkedHashSet<>();
    dataSources.add("PASSENGERS");
    dataSources.add("EMPLOYEES");
    dataSources.add("VIPS");
    dataSources.add("MARRIAGES");

    File passengerFile = this.preparePassengerFile();
    File employeeFile = this.prepareEmployeeFile();
    File vipFile = this.prepareVipFile();
    File marriagesFile = this.prepareMarriagesFile();

    employeeFile.deleteOnExit();
    passengerFile.deleteOnExit();
    vipFile.deleteOnExit();
    marriagesFile.deleteOnExit();

    RepositoryManager.configSources(repoDirectory,
                                    dataSources,
                                    true);

    RepositoryManager.loadFile(repoDirectory,
                               passengerFile,
                               PASSENGERS,
                               true);

    RepositoryManager.loadFile(repoDirectory,
                               employeeFile,
                               EMPLOYEES,
                               true);

    RepositoryManager.loadFile(repoDirectory,
                               vipFile,
                               VIPS,
                               true);

    RepositoryManager.loadFile(repoDirectory,
                               marriagesFile,
                               MARRIAGES,
                               true);
  }

  private static String relationshipKey(SzRecordId recordId1,
                                        SzRecordId recordId2) {
    String rec1 = recordId1.getRecordId();
    String rec2 = recordId2.getRecordId();
    if (rec1.compareTo(rec2) <= 0) {
      return rec1 + "|" + rec2;
    } else {
      return rec2 + "|" + rec1;
    }
  }

  private File preparePassengerFile() {
    String[] headers = {
        "RECORD_ID", "NAME_FIRST", "NAME_LAST", "PHONE_NUMBER", "ADDR_FULL",
        "DATE_OF_BIRTH"};

    String[][] passengers = {
        {ABC123.getRecordId(), "Joe", "Schmoe", "702-555-1212",
            "101 Main Street, Las Vegas, NV 89101", "1981-01-12"},
        {DEF456.getRecordId(), "Joanne", "Smith", "212-555-1212",
            "101 Fifth Ave, Las Vegas, NV 10018", "1983-05-15"},
        {GHI789.getRecordId(), "John", "Doe", "818-555-1313",
            "100 Main Street, Los Angeles, CA 90012", "1978-10-17"},
        {JKL012.getRecordId(), "Jane", "Doe", "818-555-1212",
            "100 Main Street, Los Angeles, CA 90012", "1979-02-05"}
    };
    return this.prepareCSVFile("test-passengers-", headers, passengers);
  }

  private File prepareEmployeeFile() {
    String[] headers = {
        "RECORD_ID", "NAME_FIRST", "NAME_LAST", "PHONE_NUMBER", "ADDR_FULL",
        "DATE_OF_BIRTH","MOTHERS_MAIDEN_NAME", "SSN_NUMBER"};

    String[][] employees = {
        {MNO345.getRecordId(), "Joseph", "Schmoe", "702-555-1212",
            "101 Main Street, Las Vegas, NV 89101", "1981-01-12", "WILSON",
            "145-45-9866"},
        {PQR678.getRecordId(), "Jo Anne", "Smith", "212-555-1212",
            "101 Fifth Ave, Las Vegas, NV 10018", "1983-05-15", "JACOBS",
            "213-98-9374"},
        {ZYX321.getRecordId(), "Mark", "Hightower", "563-927-2833",
            "1882 Meadows Lane, Las Vegas, NV 89125", "1981-06-22", "JENKINS",
            "873-22-4213"},
        {CBA654.getRecordId(), "Mark", "Hightower", "781-332-2824",
            "2121 Roscoe Blvd, Los Angeles, CA 90232", "1980-09-09", "BROOKS",
            "827-27-4829"}
    };

    return this.prepareJsonArrayFile("test-employees-", headers, employees);
  }

  private File prepareVipFile() {
    String[] headers = {
        "RECORD_ID", "NAME_FIRST", "NAME_LAST", "PHONE_NUMBER", "ADDR_FULL",
        "DATE_OF_BIRTH","MOTHERS_MAIDEN_NAME"};

    String[][] vips = {
        {STU901.getRecordId(), "John", "Doe", "818-555-1313",
            "100 Main Street, Los Angeles, CA 90012", "1978-10-17", "GREEN"},
        {XYZ234.getRecordId(), "Jane", "Doe", "818-555-1212",
            "100 Main Street, Los Angeles, CA 90012", "1979-02-05", "GRAHAM"}
    };

    return this.prepareJsonFile("test-vips-", headers, vips);
  }

  private File prepareMarriagesFile() {
    String[] headers = {
        "RECORD_ID", "NAME_FULL", "AKA_NAME_FULL", "PHONE_NUMBER", "ADDR_FULL",
        "MARRIAGE_DATE", "DATE_OF_BIRTH", "GENDER", "RELATIONSHIP_TYPE",
        "RELATIONSHIP_ROLE", "RELATIONSHIP_KEY" };

    String[][] spouses = {
        {BCD123.getRecordId(), "Bruce Wayne", "Batman", "201-765-3451",
            "101 Wayne Manor Rd; Gotham City, NJ 07017", "2008-06-05",
            "1971-09-08", "M", "SPOUSE", "HUSBAND",
            relationshipKey(BCD123, CDE456)},
        {CDE456.getRecordId(), "Selina Kyle", "Catwoman", "201-875-2314",
            "101 Wayne Manor Rd; Gotham City, NJ 07017", "2008-06-05",
            "1981-12-05", "F", "SPOUSE", "WIFE",
            relationshipKey(BCD123, CDE456)},
        {EFG789.getRecordId(), "Barry Allen", "The Flash", "330-982-2133",
            "1201 Main Street; Star City, OH 44308", "2014-11-07",
            "1986-03-04", "M", "SPOUSE", "HUSBAND",
            relationshipKey(EFG789, FGH012)},
        {FGH012.getRecordId(), "Iris West-Allen", "", "330-675-1231",
            "1201 Main Street; Star City, OH 44308", "2014-11-07",
            "1986-05-14", "F", "SPOUSE", "WIFE",
            relationshipKey(EFG789, FGH012)}
    };

    return this.prepareJsonFile("test-marriages-", headers, spouses);
  }

  @AfterAll
  public void teardownEnvironment() {
    try {
      this.teardownTestEnvironment();
      this.conditionallyLogCounts(true);
    } finally {
      this.endTests();
    }
  }

  @Test
  public void getRecordTest() {
    this.performTest(() -> {
      final String dataSource = ABC123.getDataSourceCode();
      final String recordId = ABC123.getRecordId();

      String uriText = this.formatServerUri(
          "data-sources/" + dataSource + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);
      long before = System.nanoTime();
      SzRecordResponse response = this.entityDataServices.getRecord(
          dataSource, recordId, false, uriInfo);
      response.concludeTimers();
      long after = System.nanoTime();

      validateRecordResponse(
          response,
          GET,
          uriText,
          dataSource,
          recordId,
          Collections.singleton("Schmoe Joe"),
          Collections.singleton("101 Main Street, Las Vegas, NV 89101"),
          Collections.singleton("702-555-1212"),
          null,
          Collections.singleton("DOB: 1981-01-12"),
          null,
          null,
          after - before,
          null);
    });
  }

  @Test
  public void getRecordViaHttpTest() {
    this.performTest(() -> {
      final String dataSource = DEF456.getDataSourceCode();
      final String recordId = DEF456.getRecordId();

      String uriText = this.formatServerUri(
          "data-sources/" + dataSource + "/records/" + recordId);

      long before = System.nanoTime();
      SzRecordResponse response = this.invokeServerViaHttp(
          GET, uriText, SzRecordResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateRecordResponse(
          response,
          GET,
          uriText,
          dataSource,
          recordId,
          Collections.singleton("Smith Joanne"),
          Collections.singleton("101 Fifth Ave, Las Vegas, NV 10018"),
          Collections.singleton("212-555-1212"),
          null,
          Collections.singleton("DOB: 1983-05-15"),
          null,
          null,
          after - before,
          null);
    });
  }

  @Test
  public void getRecordWithRawTest() {
    this.performTest(() -> {
      final String dataSource = GHI789.getDataSourceCode();
      final String recordId = GHI789.getRecordId();

      String uriText = this.formatServerUri(
          "data-sources/" + dataSource + "/records/" + recordId
              + "?withRaw=true");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);
      long before = System.nanoTime();
      SzRecordResponse response = this.entityDataServices.getRecord(
          dataSource, recordId, true, uriInfo);
      response.concludeTimers();
      long after = System.nanoTime();

      validateRecordResponse(
          response,
          GET,
          uriText,
          dataSource,
          recordId,
          Collections.singleton("Doe John"),
          Collections.singleton("100 Main Street, Los Angeles, CA 90012"),
          Collections.singleton("818-555-1313"),
          null,
          Collections.singleton("DOB: 1978-10-17"),
          null,
          null,
          after - before,
          true);
    });
  }

  @Test
  public void getRecordWithRawViaHttpTest() {
    this.performTest(() -> {
      final String dataSource = JKL012.getDataSourceCode();
      final String recordId = JKL012.getRecordId();

      String uriText = this.formatServerUri(
          "data-sources/" + dataSource + "/records/" + recordId
              + "?withRaw=true");

      long before = System.nanoTime();
      SzRecordResponse response = this.invokeServerViaHttp(
          GET, uriText, SzRecordResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateRecordResponse(
          response,
          GET,
          uriText,
          dataSource,
          recordId,
          Collections.singleton("Doe Jane"),
          Collections.singleton("100 Main Street, Los Angeles, CA 90012"),
          Collections.singleton("818-555-1212"),
          null,
          Collections.singleton("DOB: 1979-02-05"),
          null,
          null,
          after - before,
          true);
    });
  }

  @Test
  public void getRecordWithoutRawTest() {
    this.performTest(() -> {
      final String dataSource = MNO345.getDataSourceCode();
      final String recordId = MNO345.getRecordId();

      String uriText = this.formatServerUri(
          "data-sources/" + dataSource + "/records/" + recordId
              + "?withRaw=false");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);
      long before = System.nanoTime();
      SzRecordResponse response = this.entityDataServices.getRecord(
          dataSource, recordId, false, uriInfo);
      response.concludeTimers();
      long after = System.nanoTime();

      validateRecordResponse(
          response,
          GET,
          uriText,
          dataSource,
          recordId,
          Collections.singleton("Schmoe Joseph"),
          Collections.singleton("101 Main Street, Las Vegas, NV 89101"),
          Collections.singleton("702-555-1212"),
          null,
          Collections.singleton("DOB: 1981-01-12"),
          null,
          Collections.singleton("MOTHERS_MAIDEN_NAME: WILSON"),
          after - before,
          false);
    });
  }

  @Test
  public void getRecordWithoutRawViaHttpTest() {
    this.performTest(() -> {
      final String dataSource = PQR678.getDataSourceCode();
      final String recordId = PQR678.getRecordId();

      String uriText = this.formatServerUri(
          "data-sources/" + dataSource + "/records/" + recordId
              + "?withRaw=false");

      long before = System.nanoTime();
      SzRecordResponse response = this.invokeServerViaHttp(
          GET, uriText, SzRecordResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateRecordResponse(
          response,
          GET,
          uriText,
          dataSource,
          recordId,
          Collections.singleton("Smith Jo Anne"),
          Collections.singleton("101 Fifth Ave, Las Vegas, NV 10018"),
          Collections.singleton("212-555-1212"),
          null,
          Collections.singleton("DOB: 1983-05-15"),
          null,
          Collections.singleton("MOTHERS_MAIDEN_NAME: JACOBS"),
          after - before,
          false);
    });
  }


  @MethodSource("getWithRawVariants")
  @ParameterizedTest
  public void getRecordViaJavaClientTest(Boolean withRaw) {
    this.performTest(() -> {
      final String dataSource = DEF456.getDataSourceCode();
      final String recordId = DEF456.getRecordId();

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/" + dataSource + "/records/" + recordId);
      if (withRaw != null) {
        sb.append("?withRaw=").append(withRaw);
      }
      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzRecordResponse clientResponse
          = this.entityDataApi.getRecord(dataSource, recordId, withRaw);
      long after = System.nanoTime();

      SzRecordResponse response = jsonCopy(clientResponse,
                                           SzRecordResponse.class);

      validateRecordResponse(
          response,
          GET,
          uriText,
          dataSource,
          recordId,
          Collections.singleton("Smith Joanne"),
          Collections.singleton("101 Fifth Ave, Las Vegas, NV 10018"),
          Collections.singleton("212-555-1212"),
          null,
          Collections.singleton("DOB: 1983-05-15"),
          null,
          null,
          after - before,
          (withRaw != null ? withRaw : false));
    });
  }

  @Test
  public void getRelatedRecordTest() {
    this.performTest(() -> {
      final String dataSource = BCD123.getDataSourceCode();
      final String recordId = BCD123.getRecordId();
      final String relKey = relationshipKey(BCD123, CDE456);

      String uriText = this.formatServerUri(
          "data-sources/" + dataSource + "/records/" + recordId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);
      long before = System.nanoTime();
      SzRecordResponse response = this.entityDataServices.getRecord(
          dataSource, recordId, false, uriInfo);
      response.concludeTimers();
      long after = System.nanoTime();

      validateRecordResponse(
          response,
          GET,
          uriText,
          dataSource,
          recordId,
          set("Bruce Wayne", "AKA: Batman"),
          Collections.singleton("101 Wayne Manor Rd; Gotham City, NJ 07017"),
          Collections.singleton("201-765-3451"),
          null,
          set("DOB: 1971-09-08", "GENDER: M"),
          Collections.singleton("REL_LINK: HUSBAND: SPOUSE " + relKey),
          null,
          after - before,
          null);
    });
  }

  @Test
  public void getRelatedRecordViaHttpTest() {
    this.performTest(() -> {
      final String dataSource = CDE456.getDataSourceCode();
      final String recordId = CDE456.getRecordId();
      final String relKey = relationshipKey(BCD123, CDE456);

      String uriText = this.formatServerUri(
          "data-sources/" + dataSource + "/records/" + recordId);

      long before = System.nanoTime();
      SzRecordResponse response = this.invokeServerViaHttp(
          GET, uriText, SzRecordResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateRecordResponse(
          response,
          GET,
          uriText,
          dataSource,
          recordId,
          set("Selina Kyle", "AKA: Catwoman"),
          Collections.singleton("101 Wayne Manor Rd; Gotham City, NJ 07017"),
          Collections.singleton("201-875-2314"),
          null,
          set("DOB: 1981-12-05", "GENDER: F"),
          Collections.singleton("REL_LINK: WIFE: SPOUSE " + relKey),
          null,
          after - before,
          null);
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
        null,
        WITH_DUPLICATES,
        false,
        false,
        uriInfo);

    SzEntityData data = response.getData();

    SzResolvedEntity entity = data.getResolvedEntity();

    return entity.getEntityId();
  }

  private List<List> joeSchmoeEntityArgs() {
    final SzRecordId recordId1 = ABC123;
    final SzRecordId recordId2 = MNO345;

    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("Joseph Schmoe"));
    expectedDataMap.put(ADDRESS, set("101 Main Street, Las Vegas, NV 89101"));
    expectedDataMap.put(PHONE, set("702-555-1212"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1981-01-12"));

    Set<String> expectedOtherData = set("MOTHERS_MAIDEN_NAME: WILSON");

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("Joseph Schmoe"));

    Map<String, Set<String>> duplicateFeatureValues = new LinkedHashMap<>();
    duplicateFeatureValues.put("NAME", set("Joe Schmoe"));

    final int expectedRecordCount = 2;
    final int expectedRelatedCount = 0;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 1);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 1);
    expectedFeatureCounts.put("PHONE", 1);

    Set<SzRecordId> expectedRecordIds = set(recordId1, recordId2);
    List<List> result = new ArrayList<>(2);
    result.add(
        list(recordId1,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             expectedOtherData));
    result.add(
        list(recordId2,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             expectedOtherData));
    return result;
  }

  private List<List> joanneSmithEntityArgs() {
    final SzRecordId recordId1 = DEF456;
    final SzRecordId recordId2 = PQR678;
    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("Joanne Smith", "Jo Anne Smith"));
    expectedDataMap.put(ADDRESS, set("101 Fifth Ave, Las Vegas, NV 10018"));
    expectedDataMap.put(PHONE, set("212-555-1212"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1983-05-15"));

    Set<String> expectedOtherData = set("MOTHERS_MAIDEN_NAME: JACOBS");

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("Joanne Smith", "Jo Anne Smith"));
    Map<String, Set<String>> duplicateFeatureValues = null;

    final int expectedRecordCount = 2;
    final int expectedRelatedCount = 0;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 2);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 1);
    expectedFeatureCounts.put("PHONE", 1);

    Set<SzRecordId> expectedRecordIds = set(recordId1, recordId2);
    List<List> result = new ArrayList<>(2);
    result.add(
        list(recordId1,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             expectedOtherData));
    result.add(
        list(recordId2,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             expectedOtherData));
    return result;
  }

  private List<List> johnDoeEntityArgs() {
    final SzRecordId recordId1 = GHI789;
    final SzRecordId recordId2 = STU901;
    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("John Doe"));
    expectedDataMap.put(ADDRESS, set("100 Main Street, Los Angeles, CA 90012"));
    expectedDataMap.put(PHONE, set("818-555-1313"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1978-10-17"));

    Set<String> expectedOtherData = set("MOTHERS_MAIDEN_NAME: GREEN");

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("John Doe"));
    Map<String, Set<String>> duplicateFeatureValues = null;

    final int expectedRecordCount = 2;
    final int expectedRelatedCount = 1;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 1);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 1);
    expectedFeatureCounts.put("PHONE", 1);

    Set<SzRecordId> expectedRecordIds = set(recordId1, recordId2);
    List<List> result = new ArrayList<>(2);
    result.add(
        list(recordId1,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             expectedOtherData));
    result.add(
        list(recordId2,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             expectedOtherData));
    return result;
  }

  private List<List> janeDoeEntityArgs() {
    final SzRecordId recordId1 = JKL012;
    final SzRecordId recordId2 = XYZ234;
    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("Jane Doe"));
    expectedDataMap.put(ADDRESS, set("100 Main Street, Los Angeles, CA 90012"));
    expectedDataMap.put(PHONE, set("818-555-1212"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1979-02-05"));

    Set<String> expectedOtherData = set("MOTHERS_MAIDEN_NAME: GRAHAM");

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("Jane Doe"));
    Map<String, Set<String>> duplicateFeatureValues = null;

    final int expectedRecordCount = 2;
    final int expectedRelatedCount = 1;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 1);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 1);
    expectedFeatureCounts.put("PHONE", 1);

    Set<SzRecordId> expectedRecordIds = set(recordId1, recordId2);
    List<List> result = new ArrayList<>(2);
    result.add(
        list(recordId1,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             expectedOtherData));
    result.add(
        list(recordId2,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             expectedOtherData));
    return result;
  }

  private List<List> bruceWayneEntityArgs() {
    final SzRecordId recordId1 = BCD123;
    final SzRecordId recordId2 = CDE456;
    final String key = relationshipKey(recordId1, recordId2);
    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("Bruce Wayne", "AKA: Batman"));
    expectedDataMap.put(ADDRESS, set("101 Wayne Manor Rd; Gotham City, NJ 07017"));
    expectedDataMap.put(PHONE, set("201-765-3451"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1971-09-08", "GENDER: M"));
    expectedDataMap.put(RELATIONSHIP, set("REL_LINK: HUSBAND: SPOUSE " + key));

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("Bruce Wayne", "Batman"));
    Map<String, Set<String>> duplicateFeatureValues = null;

    final int expectedRecordCount = 1;
    final int expectedRelatedCount = 1;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 2);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 1);
    expectedFeatureCounts.put("PHONE", 1);
    expectedFeatureCounts.put("GENDER", 1);
    expectedFeatureCounts.put("REL_LINK", 1);

    Set<SzRecordId> expectedRecordIds = Collections.singleton(recordId1);
    List<List> result = new ArrayList<>(1);
    result.add(
        list(recordId1,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             null));
    return result;
  }

  private List<List> selinaKyleEntityArgs() {
    final SzRecordId recordId1 = CDE456;
    final SzRecordId recordId2 = BCD123;
    final String key = relationshipKey(recordId1, recordId2);
    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("Selina Kyle", "AKA: Catwoman"));
    expectedDataMap.put(ADDRESS, set("101 Wayne Manor Rd; Gotham City, NJ 07017"));
    expectedDataMap.put(PHONE, set("201-875-2314"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1981-12-05", "GENDER: F"));
    expectedDataMap.put(RELATIONSHIP, set("REL_LINK: WIFE: SPOUSE " + key));

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("Selina Kyle", "Catwoman"));
    Map<String, Set<String>> duplicateFeatureValues = null;

    final int expectedRecordCount = 1;
    final int expectedRelatedCount = 1;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 2);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 1);
    expectedFeatureCounts.put("PHONE", 1);
    expectedFeatureCounts.put("GENDER", 1);
    expectedFeatureCounts.put("REL_LINK", 1);

    Set<SzRecordId> expectedRecordIds = Collections.singleton(recordId1);
    List<List> result = new ArrayList<>(1);
    result.add(
        list(recordId1,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             null));
    return result;
  }

  private List<List> barryAllenEntityArgs() {
    final SzRecordId recordId1 = EFG789;
    final SzRecordId recordId2 = FGH012;
    final String key = relationshipKey(recordId1, recordId2);
    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("Barry Allen", "AKA: The Flash"));
    expectedDataMap.put(ADDRESS, set("1201 Main Street; Star City, OH 44308"));
    expectedDataMap.put(PHONE, set("330-982-2133"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1986-03-04", "GENDER: M"));
    expectedDataMap.put(RELATIONSHIP, set("REL_LINK: HUSBAND: SPOUSE " + key));

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("Barry Allen", "The Flash"));
    Map<String, Set<String>> duplicateFeatureValues = null;

    final int expectedRecordCount = 1;
    final int expectedRelatedCount = 1;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 2);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 1);
    expectedFeatureCounts.put("PHONE", 1);
    expectedFeatureCounts.put("GENDER", 1);
    expectedFeatureCounts.put("REL_LINK", 1);

    Set<SzRecordId> expectedRecordIds = Collections.singleton(recordId1);
    List<List> result = new ArrayList<>(1);
    result.add(
        list(recordId1,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             null));
    return result;
  }

  private List<List> irisWestAllenEntityArgs() {
    final SzRecordId recordId1 = FGH012;
    final SzRecordId recordId2 = EFG789;
    final String key = relationshipKey(recordId1, recordId2);
    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("Iris West-Allen"));
    expectedDataMap.put(ADDRESS, set("1201 Main Street; Star City, OH 44308"));
    expectedDataMap.put(PHONE, set("330-675-1231"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1986-05-14", "GENDER: F"));
    expectedDataMap.put(RELATIONSHIP, set("REL_LINK: WIFE: SPOUSE " + key));

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("Iris West-Allen"));
    Map<String, Set<String>> duplicateFeatureValues = null;

    final int expectedRecordCount = 1;
    final int expectedRelatedCount = 1;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 1);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 1);
    expectedFeatureCounts.put("PHONE", 1);
    expectedFeatureCounts.put("GENDER", 1);
    expectedFeatureCounts.put("REL_LINK", 1);

    Set<SzRecordId> expectedRecordIds = Collections.singleton(recordId1);
    List<List> result = new ArrayList<>(1);
    result.add(
        list(recordId1,
             null,  // withRaw
             null,  // withRelated
             null,  // forceMinimal
             null,  // detailLevel
             null,  // featureMode
             null,  // withFeatureStats
             null,  // withInternalFeatures
             expectedRecordCount,
             expectedRecordIds,
             expectedRelatedCount,
             expectedFeatureCounts,
             primaryFeatureValues,
             duplicateFeatureValues,
             expectedDataMap,
             null));
    return result;
  }

  private List<Arguments> getEntityParameters() {

    List<List> baseArgs = new LinkedList<>();
    baseArgs.addAll(joeSchmoeEntityArgs());
    baseArgs.addAll(joanneSmithEntityArgs());
    baseArgs.addAll(johnDoeEntityArgs());
    baseArgs.addAll(janeDoeEntityArgs());
    baseArgs.addAll(bruceWayneEntityArgs());
    baseArgs.addAll(selinaKyleEntityArgs());
    baseArgs.addAll(barryAllenEntityArgs());
    baseArgs.addAll(irisWestAllenEntityArgs());

    List<Arguments> result = new LinkedList<>();

    List<SzDetailLevel> detailLevels = new LinkedList<>();
    detailLevels.add(null);
    for (SzDetailLevel detailLevel : SzDetailLevel.values()) {
      detailLevels.add(detailLevel);
    }

    List<SzFeatureMode> featureModes = new LinkedList<>();
    featureModes.add(null);
    for (SzFeatureMode featureMode : SzFeatureMode.values()) {
      featureModes.add(featureMode);
    }

    List<SzRelationshipMode> relationshipModes = new LinkedList<>();
    relationshipModes.add(null);
    for (SzRelationshipMode mode : SzRelationshipMode.values()) {
      relationshipModes.add(mode);
    }

    Random prng = new Random(RANDOM_SEED);

    List<List<Boolean>> booleanCombos = getBooleanVariants(4);
    Collections.shuffle(booleanCombos, prng);
    Iterator<List<Boolean>> booleansIter = circularIterator(booleanCombos);

    List<List> optionCombos = generateCombinations(relationshipModes,
                                                   detailLevels,
                                                   featureModes);
    Collections.shuffle(optionCombos, prng);
    Iterator<List> optionsIter = circularIterator(optionCombos);

    int loopCount
        = Math.max(booleanCombos.size(), optionCombos.size()) * 15
        / baseArgs.size();

    baseArgs.forEach(baseArgList -> {
      for (int index = 0; index < loopCount; index++) {
        List<Object>        optsList   = optionsIter.next();
        SzRelationshipMode  withRelated = (SzRelationshipMode) optsList.get(0);
        SzDetailLevel       detailLevel = (SzDetailLevel) optsList.get(1);
        SzFeatureMode       featureMode = (SzFeatureMode) optsList.get(2);

        List<Boolean> booleansList = booleansIter.next();
        Boolean withRaw               = booleansList.get(0);
        Boolean forceMinimal          = booleansList.get(1);
        Boolean withFeatureStats      = booleansList.get(2);
        Boolean withInternalFeatures  = booleansList.get(3);

        Object[] argArray = baseArgList.toArray();

        argArray[1] = withRaw;
        argArray[2] = withRelated;
        argArray[3] = forceMinimal;
        argArray[4] = detailLevel;
        argArray[5] = featureMode;
        argArray[6] = withFeatureStats;
        argArray[7] = withInternalFeatures;

        result.add(arguments(argArray));
      }
    });

    return result;

  }

  private StringBuilder buildEntityQueryString(
      StringBuilder       sb,
      Boolean             withRaw,
      SzRelationshipMode  withRelated,
      Boolean             forceMinimal,
      SzDetailLevel       detailLevel,
      SzFeatureMode       featureMode,
      Boolean             withFeatureStats,
      Boolean             withInternalFeatures)
  {
    String prefix = "?";
    if (detailLevel != null) {
      sb.append(prefix).append("detailLevel=").append(detailLevel);
      prefix = "&";
    }
    if (featureMode != null) {
      sb.append(prefix).append("featureMode=").append(featureMode);
      prefix = "&";
    }
    if (withFeatureStats != null) {
      sb.append(prefix).append("withFeatureStats=").append(withFeatureStats);
      prefix = "&";
    }
    if (withInternalFeatures != null) {
      sb.append(prefix).append("withInternalFeatures=")
          .append(withInternalFeatures);
      prefix = "&";
    }
    if (forceMinimal != null) {
      sb.append(prefix).append("forceMinimal=").append(forceMinimal);
      prefix = "&";
    }
    if (withRelated != null) {
      sb.append(prefix).append("withRelated=").append(withRelated);
      prefix = "&";
    }
    if (withRaw != null) {
      sb.append(prefix).append("withRaw=").append(withRaw);
    }
    return sb;
  }

  @ParameterizedTest
  @MethodSource("getEntityParameters")
  public void getEntityByRecordIdTest(
      SzRecordId                          keyRecordId,
      Boolean                             withRaw,
      SzRelationshipMode                  withRelated,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      Boolean                             withFeatureStats,
      Boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Set<SzRecordId>                     expectedRecordIds,
      Integer                             relatedEntityCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    this.performTest(() -> {
      String testInfo = "keyRecord=[ " + keyRecordId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelated=[ " + withRelated
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(keyRecordId.getDataSourceCode());
      sb.append("/records/").append(keyRecordId.getRecordId()).append("/entity");
      buildEntityQueryString(sb,
                             withRaw,
                             withRelated,
                             forceMinimal,
                             detailLevel,
                             featureMode,
                             withFeatureStats,
                             withInternalFeatures);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzEntityResponse response = this.entityDataServices.getEntityByRecordId(
          keyRecordId.getDataSourceCode(),
          keyRecordId.getRecordId(),
          (withRaw != null ? withRaw : false),
          (withRelated != null ? withRelated : PARTIAL),
          (forceMinimal != null ? forceMinimal : false),
          (detailLevel != null ? detailLevel : VERBOSE),
          (featureMode != null ? featureMode : WITH_DUPLICATES),
          (withFeatureStats != null ? withFeatureStats : false),
          (withInternalFeatures != null ? withInternalFeatures : false),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      validateEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          withRelated,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          expectedRecordCount,
          expectedRecordIds,
          relatedEntityCount,
          expectedFeatureCounts,
          primaryFeatureValues,
          duplicateFeatureValues,
          expectedDataValues,
          expectedOtherDataValues,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityParameters")
  public void getEntityByRecordIdViaHttpTest(
      SzRecordId                          keyRecordId,
      Boolean                             withRaw,
      SzRelationshipMode                  withRelated,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      Boolean                             withFeatureStats,
      Boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Set<SzRecordId>                     expectedRecordIds,
      Integer                             relatedEntityCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    this.performTest(() -> {
      String testInfo = "keyRecord=[ " + keyRecordId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelated=[ " + withRelated
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(keyRecordId.getDataSourceCode());
      sb.append("/records/").append(keyRecordId.getRecordId()).append("/entity");
      buildEntityQueryString(sb,
                             withRaw,
                             withRelated,
                             forceMinimal,
                             detailLevel,
                             featureMode,
                             withFeatureStats,
                             withInternalFeatures);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzEntityResponse response = this.invokeServerViaHttp(
          GET, uriText, SzEntityResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          withRelated,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          expectedRecordCount,
          expectedRecordIds,
          relatedEntityCount,
          expectedFeatureCounts,
          primaryFeatureValues,
          duplicateFeatureValues,
          expectedDataValues,
          expectedOtherDataValues,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityParameters")
  public void getEntityByRecordIdViaJavaClientTest(
      SzRecordId                          keyRecordId,
      Boolean                             withRaw,
      SzRelationshipMode                  withRelated,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      Boolean                             withFeatureStats,
      Boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Set<SzRecordId>                     expectedRecordIds,
      Integer                             relatedEntityCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    this.performTest(() -> {
      String testInfo = "keyRecord=[ " + keyRecordId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelated=[ " + withRelated
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(keyRecordId.getDataSourceCode());
      sb.append("/records/").append(keyRecordId.getRecordId()).append("/entity");
      buildEntityQueryString(sb,
                             withRaw,
                             withRelated,
                             forceMinimal,
                             detailLevel,
                             featureMode,
                             withFeatureStats,
                             withInternalFeatures);

      String uriText = this.formatServerUri(sb.toString());

      com.senzing.gen.api.model.SzDetailLevel detailLvl = null;
      if (detailLevel != null) {
        detailLvl = com.senzing.gen.api.model.SzDetailLevel.valueOf(
            detailLevel.toString());
      }

      com.senzing.gen.api.model.SzFeatureMode featMode = null;
      if (featureMode != null) {
        featMode = com.senzing.gen.api.model.SzFeatureMode.valueOf(
            featureMode.toString());
      }

      com.senzing.gen.api.model.SzRelationshipMode relMode = null;
      if (withRelated != null) {
        relMode = com.senzing.gen.api.model.SzRelationshipMode.valueOf(
            withRelated.toString());
      }

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzEntityResponse clientResponse
          = this.entityDataApi.getEntityByRecordId(
              keyRecordId.getDataSourceCode(),
              keyRecordId.getRecordId(),
              detailLvl,
              featMode,
              withFeatureStats,
              withInternalFeatures,
              forceMinimal,
              relMode,
              withRaw);

      long after = System.nanoTime();

      SzEntityResponse response = jsonCopy(clientResponse,
                                           SzEntityResponse.class);

      validateEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          withRelated,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          expectedRecordCount,
          expectedRecordIds,
          relatedEntityCount,
          expectedFeatureCounts,
          primaryFeatureValues,
          duplicateFeatureValues,
          expectedDataValues,
          expectedOtherDataValues,
          after - before);
    });
  }

  @Test
  public void getNotFoundEntityByBadRecordIdTest()
  {
    this.performTest(() -> {
      final String badRecordId = "ABC123DEF456GHI789";
      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(PASSENGERS);
      sb.append("/records/").append(badRecordId).append("/entity");

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      try {
        this.entityDataServices.getEntityByRecordId(
            PASSENGERS,
            badRecordId,
            false,
            PARTIAL,
            false,
            null,
            WITH_DUPLICATES,
            false,
            false,
            uriInfo);

        fail("Expected entity for data source \"" + PASSENGERS
                 + "\" and record ID \"" + badRecordId + "\" to NOT be found");
      } catch (NotFoundException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();

        validateBasics(
            response, 404, GET, uriText, after - before);
      }
    });
  }

  @Test
  public void getNotFoundEntityByBadDataSourceTest()
  {
    this.performTest(() -> {
      final String badDataSource = "FOOBAR";
      final String badRecordId = "ABC123DEF456GHI789";
      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(badDataSource);
      sb.append("/records/").append(badRecordId).append("/entity");

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      try {
        this.entityDataServices.getEntityByRecordId(
            PASSENGERS,
            badRecordId,
            false,
            PARTIAL,
            false,
            null,
            WITH_DUPLICATES,
            false,
            false,
            uriInfo);

        fail("Expected entity for data source \"" + badDataSource
                 + "\" and record ID \"" + badRecordId + "\" to NOT be found");
      } catch (NotFoundException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();

        validateBasics(
            response, 404, GET, uriText, after - before);
      }
    });
  }

  @Test
  public void getNotFoundEntityByBadRecordIdViaHttpTest()
  {
    this.performTest(() -> {
      final String badRecordId = "ABC123DEF456GHI789";
      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(PASSENGERS);
      sb.append("/records/").append(badRecordId).append("/entity");

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          GET, uriText, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(
          response, 404, GET, uriText, after - before);
    });
  }

  @Test
  public void getNotFoundEntityByBadDataSourceViaHttpTest()
  {
    this.performTest(() -> {
      final String badDataSource = "FOOBAR";
      final String badRecordId = "ABC123DEF456GHI789";
      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(badDataSource);
      sb.append("/records/").append(badRecordId).append("/entity");

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          GET, uriText, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(
          response, 404, GET, uriText, after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityParameters")
  public void getEntityByEntityIdTest(
      SzRecordId                          keyRecordId,
      Boolean                             withRaw,
      SzRelationshipMode                  withRelated,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      Boolean                             withFeatureStats,
      Boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Set<SzRecordId>                     expectedRecordIds,
      Integer                             relatedEntityCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    this.performTest(() -> {
      String testInfo = "keyRecord=[ " + keyRecordId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelated=[ " + withRelated
          + " ], withRaw=[ " + withRaw + " ]";

      final Long entityId = this.getEntityIdForRecordId(keyRecordId);

      StringBuilder sb = new StringBuilder();
      sb.append("entities/").append(entityId);
      buildEntityQueryString(sb,
                             withRaw,
                             withRelated,
                             forceMinimal,
                             detailLevel,
                             featureMode,
                             withFeatureStats,
                             withInternalFeatures);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzEntityResponse response = this.entityDataServices.getEntityByEntityId(
          entityId,
          (withRaw != null ? withRaw : false),
          (withRelated == null ? PARTIAL : withRelated),
          (forceMinimal != null ? forceMinimal : false),
          (detailLevel != null ? detailLevel : VERBOSE),
          (featureMode != null ? featureMode : WITH_DUPLICATES),
          (withFeatureStats != null ? withFeatureStats : false),
          (withInternalFeatures != null ? withInternalFeatures : false),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      validateEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          withRelated,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          expectedRecordCount,
          expectedRecordIds,
          relatedEntityCount,
          expectedFeatureCounts,
          primaryFeatureValues,
          duplicateFeatureValues,
          expectedDataValues,
          expectedOtherDataValues,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityParameters")
  public void getEntityByEntityIdViaHttpTest(
      SzRecordId                          keyRecordId,
      Boolean                             withRaw,
      SzRelationshipMode                  withRelated,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      Boolean                             withFeatureStats,
      Boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Set<SzRecordId>                     expectedRecordIds,
      Integer                             relatedEntityCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    this.performTest(() -> {
      String testInfo = "keyRecord=[ " + keyRecordId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelated=[ " + withRelated
          + " ], withRaw=[ " + withRaw + " ]";

      final Long entityId = this.getEntityIdForRecordId(keyRecordId);

      StringBuilder sb = new StringBuilder();
      sb.append("entities/").append(entityId);
      buildEntityQueryString(sb,
                             withRaw,
                             withRelated,
                             forceMinimal,
                             detailLevel,
                             featureMode,
                             withFeatureStats,
                             withInternalFeatures);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzEntityResponse response = this.invokeServerViaHttp(
          GET, uriText, SzEntityResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          withRelated,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          expectedRecordCount,
          expectedRecordIds,
          relatedEntityCount,
          expectedFeatureCounts,
          primaryFeatureValues,
          duplicateFeatureValues,
          expectedDataValues,
          expectedOtherDataValues,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityParameters")
  public void getEntityByEntityIdViaJavaClientTest(
      SzRecordId                          keyRecordId,
      Boolean                             withRaw,
      SzRelationshipMode                  withRelated,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      Boolean                             withFeatureStats,
      Boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Set<SzRecordId>                     expectedRecordIds,
      Integer                             relatedEntityCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    this.performTest(() -> {
      String testInfo = "keyRecord=[ " + keyRecordId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelated=[ " + withRelated
          + " ], withRaw=[ " + withRaw + " ]";

      final Long entityId = this.getEntityIdForRecordId(keyRecordId);

      StringBuilder sb = new StringBuilder();
      sb.append("entities/").append(entityId);
      buildEntityQueryString(sb,
                             withRaw,
                             withRelated,
                             forceMinimal,
                             detailLevel,
                             featureMode,
                             withFeatureStats,
                             withInternalFeatures);

      String uriText = this.formatServerUri(sb.toString());

      com.senzing.gen.api.model.SzDetailLevel detailLvl = null;
      if (detailLevel != null) {
        detailLvl = com.senzing.gen.api.model.SzDetailLevel.valueOf(
            detailLevel.toString());
      }

      com.senzing.gen.api.model.SzFeatureMode featMode = null;
      if (featureMode != null) {
        featMode = com.senzing.gen.api.model.SzFeatureMode.valueOf(
            featureMode.toString());
      }

      com.senzing.gen.api.model.SzRelationshipMode relMode = null;
      if (withRelated != null) {
        relMode = com.senzing.gen.api.model.SzRelationshipMode.valueOf(
            withRelated.toString());
      }

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzEntityResponse clientResponse
          = this.entityDataApi.getEntityByEntityId(entityId,
                                                   detailLvl,
                                                   featMode,
                                                   withFeatureStats,
                                                   withInternalFeatures,
                                                   forceMinimal,
                                                   relMode,
                                                   withRaw);

      long after = System.nanoTime();

      SzEntityResponse response = jsonCopy(clientResponse,
                                           SzEntityResponse.class);

      validateEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          withRelated,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          expectedRecordCount,
          expectedRecordIds,
          relatedEntityCount,
          expectedFeatureCounts,
          primaryFeatureValues,
          duplicateFeatureValues,
          expectedDataValues,
          expectedOtherDataValues,
          after - before);
    });
  }

  @Test
  public void getNotFoundEntityByBadEntityIdTest()
  {
    this.performTest(() -> {
      final long badEntityId = Long.MAX_VALUE;

      String uriText = this.formatServerUri("entities/" + badEntityId);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      try {
        this.entityDataServices.getEntityByEntityId(
            badEntityId,
            false,
            SzRelationshipMode.NONE,
            false,
            null,
            WITH_DUPLICATES,
            false,
            false,
            uriInfo);

        fail("Expected entity for entity ID " + badEntityId + " to NOT be found");

      } catch (NotFoundException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();

        validateBasics(
            response, 404, GET, uriText, after - before);
      }
    });
  }

  @Test
  public void getNotFoundEntityByBadEntityIdViaHttpTest()
  {
    this.performTest(() -> {
      final long badEntityId = Long.MAX_VALUE;

      String uriText = this.formatServerUri("entities/" + badEntityId);

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          GET, uriText, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(
          response, 404, GET, uriText, after - before);

    });
  }

  private static class Criterion {
    private String key;
    private Set<String> values;

    private Criterion(String key, String... values) {
      this.key = key;
      this.values = new LinkedHashSet<>();
      for (String value : values) {
        this.values.add(value);
      }
    }
  }

  private static Criterion criterion(String key, String... values) {
    return new Criterion(key, values);
  }

  private static Map<String, Set<String>> criteria(String key, String... values) {
    Criterion criterion = criterion(key, values);
    return criteria(criterion);
  }

  private static Map<String, Set<String>> criteria(Criterion... criteria) {
    Map<String, Set<String>> result = new LinkedHashMap<>();
    for (Criterion criterion : criteria) {
      Set<String> values = result.get(criterion.key);
      if (values == null) {
        result.put(criterion.key, criterion.values);
      } else {
        values.addAll(criterion.values);
      }
    }
    return result;
  }

  private <T extends Comparable<T>> Set<T> sortedSet(Set<T> set) {
    List<T> list = new ArrayList<>(set.size());
    list.addAll(set);
    Collections.sort(list);
    Set<T> sortedSet = new LinkedHashSet<>();
    sortedSet.addAll(list);
    return sortedSet;
  }

  private <K extends Comparable<K>, V> Map<K, V> sortedMap(Map<K, V> map)
  {
    List<K> list = new ArrayList<>(map.size());
    list.addAll(map.keySet());
    Collections.sort(list);
    Map<K, V> sortedMap = new LinkedHashMap<>();
    for (K key: list) {
      sortedMap.put(key, map.get(key));
    }
    return sortedMap;
  }

  private List<Arguments> searchParameters() {
    String versionString = this.getNativeApiVersion();

    SemanticVersion version = (versionString == null)
        ? null : new SemanticVersion(versionString);

    boolean supportFiltering = (version == null) ? true // assume latest version
        : (MINIMUM_SEARCH_FILTERING_VERSION.compareTo(version) <= 0);

    Map<Map<String, Set<String>>, Map<SzAttributeSearchResultType, Integer>>
        searchCountMap = new LinkedHashMap<>();

    searchCountMap.put(criteria("PHONE_NUMBER", "702-555-1212"),
                       sortedMap(Map.of(POSSIBLE_RELATION, 1)));

    searchCountMap.put(criteria("PHONE_NUMBER", "212-555-1212"),
                       sortedMap(Map.of(POSSIBLE_RELATION, 1)));

    searchCountMap.put(criteria("PHONE_NUMBER", "818-555-1313"),
                       sortedMap(Map.of(POSSIBLE_RELATION, 1)));

    searchCountMap.put(criteria("PHONE_NUMBER", "818-555-1212"),
                       sortedMap(Map.of(POSSIBLE_RELATION, 1)));

    searchCountMap.put(
        criteria("PHONE_NUMBER", "818-555-1212", "818-555-1313"),
        sortedMap(Map.of(POSSIBLE_RELATION, 2)));

    searchCountMap.put(
        criteria(criterion("ADDR_LINE1", "100 MAIN STREET"),
                 criterion("ADDR_CITY", "LOS ANGELES"),
                 criterion("ADDR_STATE", "CALIFORNIA"),
                 criterion("ADDR_POSTAL_CODE", "90012")),
        sortedMap(Map.of(POSSIBLE_RELATION, 2)));

    searchCountMap.put(
        criteria(criterion("NAME_FULL", "JOHN DOE", "JANE DOE"),
                 criterion("ADDR_LINE1", "100 MAIN STREET"),
                 criterion("ADDR_CITY", "LOS ANGELES"),
                 criterion("ADDR_STATE", "CALIFORNIA"),
                 criterion("ADDR_POSTAL_CODE", "90012")),
        sortedMap(Map.of(MATCH, 2)));

    searchCountMap.put(
        criteria(criterion("NAME_FULL", "JOHN DOE"),
                 criterion("ADDR_LINE1", "100 MAIN STREET"),
                 criterion("ADDR_CITY", "LOS ANGELES"),
                 criterion("ADDR_STATE", "CALIFORNIA"),
                 criterion("ADDR_POSTAL_CODE", "90012")),
        sortedMap(Map.of(MATCH, 1, POSSIBLE_RELATION, 1)));

    searchCountMap.put(
        criteria(criterion("NAME_FULL", "Mark Hightower"),
                 criterion("PHONE_NUMBER", "563-927-2833")),
        sortedMap(Map.of(MATCH, 1, NAME_ONLY_MATCH, 1)));

    searchCountMap.put(
        criteria(criterion("NAME_FULL", "Mark Hightower"),
                 criterion("DATE_OF_BIRTH", "1981-03-22")),
        sortedMap(Map.of(POSSIBLE_MATCH, 1)));

    searchCountMap.put(
        criteria(criterion("NAME_FULL", "Mark Hightower"),
                 criterion("PHONE_NUMBER", "563-927-2833"),
                 criterion("PHONE_NUMBER", "781-332-2824"),
                 criterion("DATE_OF_BIRTH", "1981-06-22")),
        sortedMap(Map.of(MATCH, 1, POSSIBLE_MATCH, 1)));

    List<Arguments> list = new LinkedList<>();

    List<SzDetailLevel> detailLevels = new LinkedList<>();
    detailLevels.add(null);
    for (SzDetailLevel detailLevel : SzDetailLevel.values()) {
      detailLevels.add(detailLevel);
    }

    List<SzFeatureMode> featureModes = new LinkedList<>();
    featureModes.add(null);
    for (SzFeatureMode featureMode : SzFeatureMode.values()) {
      featureModes.add(featureMode);
    }

    Random prng = new Random(RANDOM_SEED);

    List<List<Boolean>> booleanCombos = getBooleanVariants(5);
    Collections.shuffle(booleanCombos, prng);
    Iterator<List<Boolean>> booleansIter = circularIterator(booleanCombos);

    List<List> optionCombos = generateCombinations(detailLevels, featureModes);
    Collections.shuffle(optionCombos, prng);
    Iterator<List> optionsIter = circularIterator(optionCombos);

    int loopCount
        = Math.max(booleanCombos.size(), optionCombos.size()) * 15
        / searchCountMap.size();

    searchCountMap.entrySet().forEach(entry -> {
      Map<String, Set<String>> criteria = entry.getKey();
      Map<SzAttributeSearchResultType, Integer> resultCounts = entry.getValue();

      List<Set<SzAttributeSearchResultType>> typeSetList = new ArrayList<>(20);
      List<Integer> countList = new ArrayList<>(20);

      int sum = resultCounts.values().stream()
          .reduce(0, Integer::sum);

      typeSetList.add(Collections.emptySet());
      countList.add(sum);

      // iterate over the result types and try singleton sets
      resultCounts.forEach((resultType, count) -> {
        // handle the singleton set
        EnumSet<SzAttributeSearchResultType> singletonSet
            = EnumSet.of(resultType);

        typeSetList.add(singletonSet);
        countList.add(supportFiltering ? count : sum);
      });

      // try complemente sets
      EnumSet<SzAttributeSearchResultType> allEnumSet
          = EnumSet.copyOf(resultCounts.keySet());

      Set<SzAttributeSearchResultType> allSet
          = sortedSet(allEnumSet);

      // create the complement set
      EnumSet<SzAttributeSearchResultType> complementEnumSet
          = EnumSet.complementOf(allEnumSet);

      Set<SzAttributeSearchResultType> complementSet
          = sortedSet(complementEnumSet);

      if (complementSet.size() > 0) {
        typeSetList.add(complementSet);
        countList.add(supportFiltering ? 0 : sum);
      }

      // try sets of 2 if we have heterogeneous result types
      if (resultCounts.size() > 1) {
        // try sets of two
        resultCounts.forEach((type1, count1) -> {
          resultCounts.forEach((type2, count2) -> {
            if (type1 != type2) {
              typeSetList.add(sortedSet(Set.of(type1, type2)));
              countList.add(supportFiltering ? (count1 + count2) : sum);
            }
          });
        });
      }

      // check for more than 2 result types
      if (allSet.size() > 2) {
        // try all result types specified individually
        typeSetList.add(allSet);
        countList.add(sum);
      }

      int typeSetIndex = 0;

      for (int index = 0; index < loopCount; index++) {
        List<Object>  optsList    = optionsIter.next();
        SzDetailLevel detailLevel = (SzDetailLevel) optsList.get(0);
        SzFeatureMode featureMode = (SzFeatureMode) optsList.get(1);

        List<Boolean> booleanList           = booleansIter.next();
        Boolean       withRaw               = booleanList.get(0);
        Boolean       forceMinimal          = booleanList.get(1);
        Boolean       withRelationships     = booleanList.get(2);
        Boolean       withFeatureStats      = booleanList.get(3);
        Boolean       withInternalFeatures  = booleanList.get(4);

        // try with empty set of result types
        list.add(arguments(criteria,
                           typeSetList.get(typeSetIndex),
                           countList.get(typeSetIndex),
                           forceMinimal,
                           detailLevel,
                           featureMode,
                           withFeatureStats,
                           withInternalFeatures,
                           withRelationships,
                           withRaw));

        // increment the type set index
        typeSetIndex = (typeSetIndex + 1) % typeSetList.size();
      }
    });

    return list;
  }

  @ParameterizedTest
  @MethodSource("searchParameters")
  public void searchByGetJsonAttrsTest(
      Map<String, Set<String>>          criteria,
      Set<SzAttributeSearchResultType>  includeOnlySet,
      Integer                           expectedCount,
      Boolean                           forceMinimal,
      SzDetailLevel                     detailLevel,
      SzFeatureMode                     featureMode,
      Boolean                           withFeatureStats,
      Boolean                           withInternalFeatures,
      Boolean                           withRelationships,
      Boolean                           withRaw)
  {
    this.performTest(() -> {
      String testInfo = "criteria=[ " + criteria
          + " ], includeOnly=[ " + includeOnlySet
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      JsonObjectBuilder job = Json.createObjectBuilder();
      criteria.entrySet().forEach(entry -> {
        String key = entry.getKey();
        Set<String> values = entry.getValue();
        if (values.size() == 0) return;
        if (values.size() == 1) {
          job.add(key, values.iterator().next());
        } else {
          JsonArrayBuilder jab = Json.createArrayBuilder();
          for (String value : values) {
            JsonObjectBuilder job2 = Json.createObjectBuilder();
            job2.add(key, value);
            jab.add(job2);
          }
          job.add(key, jab);
        }
      });
      String attrs = JsonUtilities.toJsonText(job);

      StringBuilder sb = new StringBuilder();
      sb.append(this.formatServerUri(
          "entities?attrs=" + urlEncode(attrs)));
      if (includeOnlySet != null && includeOnlySet.size() > 0) {
        for (SzAttributeSearchResultType resultType: includeOnlySet) {
          sb.append("&includeOnly=").append(resultType);
        }
      }
      if (detailLevel != null) {
        sb.append("&detailLevel=").append(detailLevel);
      }
      if (featureMode != null) {
        sb.append("&featureMode=").append(featureMode);
      }
      if (withFeatureStats != null) {
        sb.append("&withFeatureStats=").append(withFeatureStats);
      }
      if (withInternalFeatures != null) {
        sb.append("&withInternalFeatures=").append(withInternalFeatures);
      }
      if (forceMinimal != null) {
        sb.append("&forceMinimal=").append(forceMinimal);
      }
      if (withRelationships != null) {
        sb.append("&withRelationships=").append(withRelationships);
      }
      if (withRaw != null) {
        sb.append("&withRaw=").append(withRaw);
      }
      Set<String> includeOnlyParams = new LinkedHashSet<>();
      includeOnlySet.forEach(
          resultType -> includeOnlyParams.add(resultType.toString()));

      String uriText = sb.toString();
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      SzAttributeSearchResponse response
          = this.entityDataServices.searchEntitiesByGet(
          attrs,
          null,
          includeOnlyParams,
          (forceMinimal != null ? forceMinimal : false),
          (detailLevel != null ? detailLevel : VERBOSE),
          (featureMode != null ? featureMode : WITH_DUPLICATES),
          (withFeatureStats != null ? withFeatureStats : false),
          (withInternalFeatures != null ? withInternalFeatures : false),
          (withRelationships != null ? withRelationships : false),
          (withRaw != null ? withRaw : false),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      // TODO(barry): remove this extra code
      long flags = this.entityDataServices.getFlags(
          (forceMinimal == null) ? false : forceMinimal,
          (detailLevel != null ? detailLevel : VERBOSE),
          (featureMode != null ? featureMode : WITH_DUPLICATES),
          (withFeatureStats != null ? withFeatureStats : false),
          (withInternalFeatures != null ? withInternalFeatures : false),
          (withRelationships != null ? withRelationships : false));

      try {
        ObjectMapper mapper = new ObjectMapper();
        String rawJsonText = mapper.writeValueAsString(response.getRawData());
        testInfo = testInfo + ", flags=[ " + flags + " ], featureFlag=[ "
            + (flags & G2Engine.G2_ENTITY_INCLUDE_REPRESENTATIVE_FEATURES)
            + " ], rawData=[ " + rawJsonText + " ], internalFeaturesFlag=[ "
            + (flags & G2Engine.G2_ENTITY_OPTION_INCLUDE_INTERNAL_FEATURES)
            + " ]";
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      validateSearchResponse(
          testInfo,
          response,
          GET,
          uriText,
          expectedCount,
          withRelationships,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          after - before,
          withRaw);

    });
  }

  @ParameterizedTest
  @MethodSource("searchParameters")
  public void searchByGetJsonAttrsViaHttpTest(
      Map<String, Set<String>>          criteria,
      Set<SzAttributeSearchResultType>  includeOnlySet,
      Integer                           expectedCount,
      Boolean                           forceMinimal,
      SzDetailLevel                     detailLevel,
      SzFeatureMode                     featureMode,
      Boolean                           withFeatureStats,
      Boolean                           withInternalFeatures,
      Boolean                           withRelationships,
      Boolean                           withRaw)
  {
    this.performTest(() -> {
      String testInfo = "criteria=[ " + criteria
          + " ], includeOnly=[ " + includeOnlySet
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      JsonObjectBuilder job = Json.createObjectBuilder();
      criteria.entrySet().forEach(entry -> {
        String key = entry.getKey();
        Set<String> values = entry.getValue();
        if (values.size() == 0) return;
        if (values.size() == 1) {
          job.add(key, values.iterator().next());
        } else {
          JsonArrayBuilder jab = Json.createArrayBuilder();
          for (String value : values) {
            JsonObjectBuilder job2 = Json.createObjectBuilder();
            job2.add(key, value);
            jab.add(job2);
          }
          job.add(key, jab);
        }
      });
      String attrs = JsonUtilities.toJsonText(job);

      StringBuilder sb = new StringBuilder();
      sb.append("entities?attrs=").append(urlEncode(attrs));
      if (includeOnlySet != null && includeOnlySet.size() > 0) {
        for (SzAttributeSearchResultType resultType: includeOnlySet) {
          sb.append("&includeOnly=").append(resultType);
        }
      }
      if (detailLevel != null) {
        sb.append("&detailLevel=").append(detailLevel);
      }
      if (featureMode != null) {
        sb.append("&featureMode=").append(featureMode);
      }
      if (withFeatureStats != null) {
        sb.append("&withFeatureStats=").append(withFeatureStats);
      }
      if (withInternalFeatures != null) {
        sb.append("&withInternalFeatures=").append(withInternalFeatures);
      }
      if (forceMinimal != null) {
        sb.append("&forceMinimal=").append(forceMinimal);
      }
      if (withRelationships != null) {
        sb.append("&withRelationships=").append(withRelationships);
      }
      if (withRaw != null) {
        sb.append("&withRaw=").append(withRaw);
      }
      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzAttributeSearchResponse response = this.invokeServerViaHttp(
          GET, uriText, SzAttributeSearchResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateSearchResponse(
          testInfo,
          response,
          GET,
          uriText,
          expectedCount,
          withRelationships,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          after - before,
          withRaw);
    });
  }

  @ParameterizedTest
  @MethodSource("searchParameters")
  public void searchByGetJsonAttrsViaJavaClientTest(
      Map<String, Set<String>>          criteria,
      Set<SzAttributeSearchResultType>  includeOnlySet,
      Integer                           expectedCount,
      Boolean                           forceMinimal,
      SzDetailLevel                     detailLevel,
      SzFeatureMode                     featureMode,
      Boolean                           withFeatureStats,
      Boolean                           withInternalFeatures,
      Boolean                           withRelationships,
      Boolean                           withRaw)
  {
    this.performTest(() -> {
      String testInfo = "criteria=[ " + criteria
          + " ], includeOnly=[ " + includeOnlySet
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      JsonObjectBuilder job = Json.createObjectBuilder();
      criteria.entrySet().forEach(entry -> {
        String key = entry.getKey();
        Set<String> values = entry.getValue();
        if (values.size() == 0) return;
        if (values.size() == 1) {
          job.add(key, values.iterator().next());
        } else {
          JsonArrayBuilder jab = Json.createArrayBuilder();
          for (String value : values) {
            JsonObjectBuilder job2 = Json.createObjectBuilder();
            job2.add(key, value);
            jab.add(job2);
          }
          job.add(key, jab);
        }
      });
      String attrs = JsonUtilities.toJsonText(job);

      StringBuilder sb = new StringBuilder();
      sb.append("entities?attrs=").append(urlEncode(attrs));
      if (includeOnlySet != null && includeOnlySet.size() > 0) {
        for (SzAttributeSearchResultType resultType: includeOnlySet) {
          sb.append("&includeOnly=").append(resultType);
        }
      }
      if (detailLevel != null) {
        sb.append("&detailLevel=").append(detailLevel);
      }
      if (featureMode != null) {
        sb.append("&featureMode=").append(featureMode);
      }
      if (withFeatureStats != null) {
        sb.append("&withFeatureStats=").append(withFeatureStats);
      }
      if (withInternalFeatures != null) {
        sb.append("&withInternalFeatures=").append(withInternalFeatures);
      }
      if (forceMinimal != null) {
        sb.append("&forceMinimal=").append(forceMinimal);
      }
      if (withRelationships != null) {
        sb.append("&withRelationships=").append(withRelationships);
      }
      if (withRaw != null) {
        sb.append("&withRaw=").append(withRaw);
      }
      String uriText = this.formatServerUri(sb.toString());

      com.senzing.gen.api.model.SzDetailLevel detailLvl = null;
      if (detailLevel != null) {
        detailLvl = com.senzing.gen.api.model.SzDetailLevel.valueOf(
            detailLevel.toString());
      }

      com.senzing.gen.api.model.SzFeatureMode featMode = null;
      if (featureMode != null) {
        featMode = com.senzing.gen.api.model.SzFeatureMode.valueOf(
            featureMode.toString());
      }

      List<com.senzing.gen.api.model.SzAttributeSearchResultType>
          includeOnlyParams = new ArrayList<>(includeOnlySet.size());
      includeOnlySet.forEach(
          resultType -> includeOnlyParams.add(
              com.senzing.gen.api.model.SzAttributeSearchResultType.valueOf(
                  resultType.toString())));

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzAttributeSearchResponse clientResponse
          = this.entityDataApi.searchEntitiesByGet(attrs,
                                                   null,
                                                   includeOnlyParams,
                                                   detailLvl,
                                                   featMode,
                                                   withFeatureStats,
                                                   withInternalFeatures,
                                                   forceMinimal,
                                                   withRelationships,
                                                   withRaw);
      long after = System.nanoTime();

      SzAttributeSearchResponse response = jsonCopy(
          clientResponse, SzAttributeSearchResponse.class);

      validateSearchResponse(
          testInfo,
          response,
          GET,
          uriText,
          expectedCount,
          withRelationships,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          after - before,
          withRaw);
    });
  }

  @ParameterizedTest
  @MethodSource("searchParameters")
  public void searchByPostJsonAttrsTest(
      Map<String, Set<String>>          criteria,
      Set<SzAttributeSearchResultType>  includeOnlySet,
      Integer                           expectedCount,
      Boolean                           forceMinimal,
      SzDetailLevel                     detailLevel,
      SzFeatureMode                     featureMode,
      Boolean                           withFeatureStats,
      Boolean                           withInternalFeatures,
      Boolean                           withRelationships,
      Boolean                           withRaw)
  {
    this.performTest(() -> {
      String testInfo = "criteria=[ " + criteria
          + " ], includeOnly=[ " + includeOnlySet
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      JsonObjectBuilder job = Json.createObjectBuilder();
      criteria.entrySet().forEach(entry -> {
        String key = entry.getKey();
        Set<String> values = entry.getValue();
        if (values.size() == 0) return;
        if (values.size() == 1) {
          job.add(key, values.iterator().next());
        } else {
          JsonArrayBuilder jab = Json.createArrayBuilder();
          for (String value : values) {
            JsonObjectBuilder job2 = Json.createObjectBuilder();
            job2.add(key, value);
            jab.add(job2);
          }
          job.add(key, jab);
        }
      });
      String attrs = JsonUtilities.toJsonText(job);

      StringBuilder sb = new StringBuilder();
      sb.append(this.formatServerUri("search-entities"));
      String prefix = "?";
      if (includeOnlySet != null && includeOnlySet.size() > 0) {
        for (SzAttributeSearchResultType resultType: includeOnlySet) {
          sb.append(prefix).append("includeOnly=").append(resultType);
          prefix = "&";
        }
      }
      if (featureMode != null) {
        sb.append(prefix).append("featureMode=").append(featureMode);
        prefix = "&";
      }
      if (withFeatureStats != null) {
        sb.append(prefix).append("withFeatureStats=").append(withFeatureStats);
        prefix = "&";
      }
      if (withInternalFeatures != null) {
        sb.append(prefix).append("withInternalFeatures=")
            .append(withInternalFeatures);
        prefix = "&";
      }
      if (forceMinimal != null) {
        sb.append(prefix).append("forceMinimal=").append(forceMinimal);
        prefix = "&";
      }
      if (withRelationships != null) {
        sb.append(prefix).append("withRelationships=")
            .append(withRelationships);
        prefix = "&";
      }
      if (withRaw != null) {
        sb.append(prefix).append("withRaw=").append(withRaw);
        prefix = "&";
      }
      Set<String> includeOnlyParams = new LinkedHashSet<>();
      includeOnlySet.forEach(
          resultType -> includeOnlyParams.add(resultType.toString()));

      String uriText = sb.toString();
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      SzAttributeSearchResponse response
          = this.entityDataServices.searchEntitiesByPost(
          includeOnlyParams,
          (forceMinimal != null ? forceMinimal : false),
          (detailLevel != null ? detailLevel : VERBOSE),
          (featureMode != null ? featureMode : WITH_DUPLICATES),
          (withFeatureStats != null ? withFeatureStats : false),
          (withInternalFeatures != null ? withInternalFeatures : false),
          (withRelationships != null ? withRelationships : false),
          (withRaw != null ? withRaw : false),
          uriInfo,
          attrs);

      response.concludeTimers();
      long after = System.nanoTime();

      validateSearchResponse(
          testInfo,
          response,
          POST,
          uriText,
          expectedCount,
          withRelationships,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          after - before,
          withRaw);

    });
  }

  @ParameterizedTest
  @MethodSource("searchParameters")
  public void searchByPostJsonAttrsViaHttpTest(
      Map<String, Set<String>>          criteria,
      Set<SzAttributeSearchResultType>  includeOnlySet,
      Integer                           expectedCount,
      Boolean                           forceMinimal,
      SzDetailLevel                     detailLevel,
      SzFeatureMode                     featureMode,
      Boolean                           withFeatureStats,
      Boolean                           withInternalFeatures,
      Boolean                           withRelationships,
      Boolean                           withRaw)
  {
    this.performTest(() -> {
      String testInfo = "criteria=[ " + criteria
          + " ], includeOnly=[ " + includeOnlySet
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      Map<String, Object> attrMap = new LinkedHashMap<>();
      criteria.forEach((key,set) -> {
        if (set.size() == 0) return;
        if (set.size() == 1) {
          attrMap.put(key, set.iterator().next());
        } else {
          List<Map<String, Object>> subMaps = new ArrayList<>(set.size());
          for (String val: set) {
            subMaps.add(Map.of(key, val));
          }
          attrMap.put(key, subMaps);
        }
      });

      StringBuilder sb = new StringBuilder();
      sb.append("search-entities");
      String prefix = "?";
      if (includeOnlySet != null && includeOnlySet.size() > 0) {
        for (SzAttributeSearchResultType resultType: includeOnlySet) {
          sb.append(prefix).append("includeOnly=").append(resultType);
          prefix = "&";
        }
      }
      if (featureMode != null) {
        sb.append(prefix).append("featureMode=").append(featureMode);
        prefix = "&";
      }
      if (withFeatureStats != null) {
        sb.append(prefix).append("withFeatureStats=").append(withFeatureStats);
        prefix = "&";
      }
      if (withInternalFeatures != null) {
        sb.append(prefix).append("withInternalFeatures=")
            .append(withInternalFeatures);
        prefix = "&";
      }
      if (forceMinimal != null) {
        sb.append(prefix).append("forceMinimal=").append(forceMinimal);
        prefix = "&";
      }
      if (withRelationships != null) {
        sb.append(prefix).append("withRelationships=")
            .append(withRelationships);
        prefix = "&";
      }
      if (withRaw != null) {
        sb.append(prefix).append("withRaw=").append(withRaw);
        prefix = "&";
      }
      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzAttributeSearchResponse response = this.invokeServerViaHttp(
          POST, uriText, null, attrMap,
          SzAttributeSearchResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateSearchResponse(
          testInfo,
          response,
          POST,
          uriText,
          expectedCount,
          withRelationships,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          after - before,
          withRaw);
    });
  }

  @ParameterizedTest
  @MethodSource("searchParameters")
  public void searchByPostJsonAttrsViaJavaClientTest(
      Map<String, Set<String>>          criteria,
      Set<SzAttributeSearchResultType>  includeOnlySet,
      Integer                           expectedCount,
      Boolean                           forceMinimal,
      SzDetailLevel                     detailLevel,
      SzFeatureMode                     featureMode,
      Boolean                           withFeatureStats,
      Boolean                           withInternalFeatures,
      Boolean                           withRelationships,
      Boolean                           withRaw)
  {
    this.performTest(() -> {
      String testInfo = "criteria=[ " + criteria
          + " ], includeOnly=[ " + includeOnlySet
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      Map<String, Object> attrMap = new LinkedHashMap<>();
      criteria.forEach((key,set) -> {
        if (set.size() == 0) return;
        if (set.size() == 1) {
          attrMap.put(key, set.iterator().next());
        } else {
          List<Map<String, Object>> subMaps = new ArrayList<>(set.size());
          for (String val: set) {
            subMaps.add(Map.of(key, val));
          }
          attrMap.put(key, subMaps);
        }
      });
      StringBuilder sb = new StringBuilder();
      sb.append("search-entities");
      String prefix = "?";
      if (includeOnlySet != null && includeOnlySet.size() > 0) {
        for (SzAttributeSearchResultType resultType: includeOnlySet) {
          sb.append(prefix).append("includeOnly=").append(resultType);
          prefix = "&";
        }
      }
      if (detailLevel != null) {
        sb.append(prefix).append("detailLevel=").append(detailLevel);
        prefix = "&";
      }
      if (featureMode != null) {
        sb.append(prefix).append("featureMode=").append(featureMode);
        prefix = "&";
      }
      if (withFeatureStats != null) {
        sb.append(prefix).append("withFeatureStats=").append(withFeatureStats);
        prefix = "&";
      }
      if (withInternalFeatures != null) {
        sb.append(prefix).append("withInternalFeatures=")
            .append(withInternalFeatures);
        prefix = "&";
      }
      if (forceMinimal != null) {
        sb.append(prefix).append("forceMinimal=").append(forceMinimal);
        prefix = "&";
      }
      if (withRelationships != null) {
        sb.append(prefix).append("withRelationships=")
            .append(withRelationships);
        prefix = "&";
      }
      if (withRaw != null) {
        sb.append(prefix).append("withRaw=").append(withRaw);
        prefix = "&";
      }
      String uriText = this.formatServerUri(sb.toString());

      com.senzing.gen.api.model.SzDetailLevel detailLvl = null;
      if (detailLevel != null) {
        detailLvl = com.senzing.gen.api.model.SzDetailLevel.valueOf(
            detailLevel.toString());
      }

      com.senzing.gen.api.model.SzFeatureMode featMode = null;
      if (featureMode != null) {
        featMode = com.senzing.gen.api.model.SzFeatureMode.valueOf(
            featureMode.toString());
      }

      List<com.senzing.gen.api.model.SzAttributeSearchResultType>
          includeOnlyParams = new ArrayList<>(includeOnlySet.size());
      includeOnlySet.forEach(
          resultType -> includeOnlyParams.add(
              com.senzing.gen.api.model.SzAttributeSearchResultType.valueOf(
                  resultType.toString())));

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzAttributeSearchResponse clientResponse
          = this.entityDataApi.searchEntitiesByPost(attrMap,
                                                    includeOnlyParams,
                                                    detailLvl,
                                                    featMode,
                                                    withFeatureStats,
                                                    withInternalFeatures,
                                                    forceMinimal,
                                                    withRelationships,
                                                    withRaw);
      long after = System.nanoTime();

      SzAttributeSearchResponse response = jsonCopy(
          clientResponse, SzAttributeSearchResponse.class);

      validateSearchResponse(
          testInfo,
          response,
          POST,
          uriText,
          expectedCount,
          withRelationships,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          after - before,
          withRaw);
    });
  }

  @ParameterizedTest
  @MethodSource("searchParameters")
  public void searchByParamAttrsTest(
      Map<String, Set<String>>          criteria,
      Set<SzAttributeSearchResultType>  includeOnlySet,
      Integer                           expectedCount,
      Boolean                           forceMinimal,
      SzDetailLevel                     detailLevel,
      SzFeatureMode                     featureMode,
      Boolean                           withFeatureStats,
      Boolean                           withInternalFeatures,
      Boolean                           withRelationships,
      Boolean                           withRaw)
  {
    this.performTest(() -> {
      String testInfo = "criteria=[ " + criteria
          + " ], includeOnly=[ " + includeOnlySet
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuffer sb = new StringBuffer();
      List<String> attrList = new LinkedList<>();
      criteria.entrySet().forEach(entry -> {
        String key = entry.getKey();
        Set<String> values = entry.getValue();
        for (String value : values) {
          attrList.add(key + ":" + value);
          String encodedVal = urlEncode(key + ":" + value);
          sb.append("&attr=").append(encodedVal);
        }
      });

      sb.setCharAt(0, '?');
      if (includeOnlySet != null && includeOnlySet.size() > 0) {
        for (SzAttributeSearchResultType resultType: includeOnlySet) {
          sb.append("&includeOnly=").append(resultType);
        }
      }
      if (detailLevel != null) {
        sb.append("&detailLevel=").append(detailLevel);
      }
      if (featureMode != null) {
        sb.append("&featureMode=").append(featureMode);
      }
      if (withFeatureStats != null) {
        sb.append("&withFeatureStats=").append(withFeatureStats);
      }
      if (withInternalFeatures != null) {
        sb.append("&withInternalFeatures=").append(withInternalFeatures);
      }
      if (forceMinimal != null) {
        sb.append("&forceMinimal=").append(forceMinimal);
      }
      if (withRelationships != null) {
        sb.append("&withRelationships=").append(withRelationships);
      }
      if (withRaw != null) {
        sb.append("&withRaw=").append(withRaw);
      }

      Set<String> includeOnlyParams = new LinkedHashSet<>();
      includeOnlySet.forEach(
          resultType -> includeOnlyParams.add(resultType.toString()));

      String uriText = this.formatServerUri(
          "entities" + sb.toString());

      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      SzAttributeSearchResponse response
          = this.entityDataServices.searchEntitiesByGet(
          null,
          attrList,
          includeOnlyParams,
          (forceMinimal != null ? forceMinimal : false),
          (detailLevel != null ? detailLevel : VERBOSE),
          (featureMode != null ? featureMode : WITH_DUPLICATES),
          (withFeatureStats != null ? withFeatureStats : false),
          (withInternalFeatures != null ? withInternalFeatures : false),
          (withRelationships != null ? withRelationships : true),
          (withRaw != null ? withRaw : false),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      validateSearchResponse(
          testInfo,
          response,
          GET,
          uriText,
          expectedCount,
          withRelationships,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          after - before,
          withRaw);

    });
  }

  @ParameterizedTest
  @MethodSource("searchParameters")
  public void searchByParamAttrsViaHttpTest(
      Map<String, Set<String>>          criteria,
      Set<SzAttributeSearchResultType>  includeOnlySet,
      Integer                           expectedCount,
      Boolean                           forceMinimal,
      SzDetailLevel                     detailLevel,
      SzFeatureMode                     featureMode,
      Boolean                           withFeatureStats,
      Boolean                           withInternalFeatures,
      Boolean                           withRelationships,
      Boolean                           withRaw)
  {
    this.performTest(() -> {
      String testInfo = "criteria=[ " + criteria
          + " ], includeOnly=[ " + includeOnlySet
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder(criteria.size() * 50);
      criteria.entrySet().forEach(entry -> {
        String key = entry.getKey();
        Set<String> values = entry.getValue();
        for (String value : values) {
          String encodedVal = urlEncode(key + ":" + value);
          sb.append("&attr=").append(encodedVal);
        }
      });

      // replace the "&" with a "?" at the start
      sb.setCharAt(0, '?');
      if (includeOnlySet != null && includeOnlySet.size() > 0) {
        for (SzAttributeSearchResultType resultType: includeOnlySet) {
          sb.append("&includeOnly=").append(resultType);
        }
      }
      if (detailLevel != null) {
        sb.append("&detailLevel=").append(detailLevel);
      }
      if (featureMode != null) {
        sb.append("&featureMode=").append(featureMode);
      }
      if (withFeatureStats != null) {
        sb.append("&withFeatureStats=").append(withFeatureStats);
      }
      if (withInternalFeatures != null) {
        sb.append("&withInternalFeatures=").append(withInternalFeatures);
      }
      if (forceMinimal != null) {
        sb.append("&forceMinimal=").append(forceMinimal);
      }
      if (withRelationships != null) {
        sb.append("&withRelationships=").append(withRelationships);
      }
      if (withRaw != null) {
        sb.append("&withRaw=").append(withRaw);
      }
      String uriText = this.formatServerUri("entities" + sb.toString());

      long before = System.nanoTime();
      SzAttributeSearchResponse response = this.invokeServerViaHttp(
          GET, uriText, SzAttributeSearchResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateSearchResponse(
          testInfo,
          response,
          GET,
          uriText,
          expectedCount,
          withRelationships,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          after - before,
          withRaw);
    });
  }

  @ParameterizedTest
  @MethodSource("searchParameters")
  public void searchByParamAttrsViaJavaClientTest(
      Map<String, Set<String>>          criteria,
      Set<SzAttributeSearchResultType>  includeOnlySet,
      Integer                           expectedCount,
      Boolean                           forceMinimal,
      SzDetailLevel                     detailLevel,
      SzFeatureMode                     featureMode,
      Boolean                           withFeatureStats,
      Boolean                           withInternalFeatures,
      Boolean                           withRelationships,
      Boolean                           withRaw)
  {
    this.performTest(() -> {
      String testInfo = "criteria=[ " + criteria
          + " ], includeOnly=[ " + includeOnlySet
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder(criteria.size() * 50);
      List<String> attrList = new LinkedList<>();
      criteria.entrySet().forEach(entry -> {
        String key = entry.getKey();
        Set<String> values = entry.getValue();
        for (String value : values) {
          String encodedVal = urlEncode(key + ":" + value);
          sb.append("&attr=").append(encodedVal);
          attrList.add(key + ":" + value);
        }
      });

      // replace the "&" with a "?" at the start
      sb.setCharAt(0, '?');
      if (includeOnlySet != null && includeOnlySet.size() > 0) {
        for (SzAttributeSearchResultType resultType: includeOnlySet) {
          sb.append("&includeOnly=").append(resultType);
        }
      }
      if (detailLevel != null) {
        sb.append("&detailLevel=").append(detailLevel);
      }
      if (featureMode != null) {
        sb.append("&featureMode=").append(featureMode);
      }
      if (withFeatureStats != null) {
        sb.append("&withFeatureStats=").append(withFeatureStats);
      }
      if (withInternalFeatures != null) {
        sb.append("&withInternalFeatures=").append(withInternalFeatures);
      }
      if (forceMinimal != null) {
        sb.append("&forceMinimal=").append(forceMinimal);
      }
      if (withRelationships != null) {
        sb.append("&withRelationships=").append(withRelationships);
      }
      if (withRaw != null) {
        sb.append("&withRaw=").append(withRaw);
      }
      String uriText = this.formatServerUri("entities" + sb.toString());

      com.senzing.gen.api.model.SzDetailLevel detailLvl = null;
      if (detailLevel != null) {
        detailLvl = com.senzing.gen.api.model.SzDetailLevel.valueOf(
            detailLevel.toString());
      }

      com.senzing.gen.api.model.SzFeatureMode featMode = null;
      if (featureMode != null) {
        featMode = com.senzing.gen.api.model.SzFeatureMode.valueOf(
            featureMode.toString());
      }

      List<com.senzing.gen.api.model.SzAttributeSearchResultType>
          includeOnlyParams = new ArrayList<>(includeOnlySet.size());
      includeOnlySet.forEach(
          resultType -> includeOnlyParams.add(
              com.senzing.gen.api.model.SzAttributeSearchResultType.valueOf(
                  resultType.toString())));

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzAttributeSearchResponse clientResponse
          = this.entityDataApi.searchEntitiesByGet(null,
                                                   attrList,
                                                   includeOnlyParams,
                                                   detailLvl,
                                                   featMode,
                                                   withFeatureStats,
                                                   withInternalFeatures,
                                                   forceMinimal,
                                                   withRelationships,
                                                   withRaw);
      long after = System.nanoTime();

      SzAttributeSearchResponse response = jsonCopy(
          clientResponse, SzAttributeSearchResponse.class);

      validateSearchResponse(
          testInfo,
          response,
          GET,
          uriText,
          expectedCount,
          withRelationships,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          after - before,
          withRaw);
    });
  }

}
