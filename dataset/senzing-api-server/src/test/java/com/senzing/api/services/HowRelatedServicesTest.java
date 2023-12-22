package com.senzing.api.services;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senzing.api.model.*;
import com.senzing.gen.api.invoker.ApiClient;
import com.senzing.gen.api.services.EntityDataApi;
import com.senzing.repomgr.RepositoryManager;
import com.senzing.util.JsonUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.util.*;

import static com.senzing.api.model.SzAttributeClass.*;
import static com.senzing.api.model.SzAttributeClass.CHARACTERISTIC;
import static com.senzing.api.model.SzDetailLevel.VERBOSE;
import static com.senzing.api.model.SzFeatureMode.WITH_DUPLICATES;
import static com.senzing.api.model.SzHttpMethod.GET;
import static com.senzing.api.services.ResponseValidators.*;
import static com.senzing.util.CollectionUtilities.list;
import static com.senzing.util.CollectionUtilities.set;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static com.senzing.io.IOUtilities.*;

@TestInstance(Lifecycle.PER_CLASS)
public class HowRelatedServicesTest extends AbstractServiceTest {
  private static final long RANDOM_SEED = 1234567890L;

  private static final String PASSENGERS      = "PASSENGERS";
  private static final String EMPLOYEES       = "EMPLOYEES";
  private static final String VIPS            = "VIPS";
  private static final String DUAL_IDENTITIES = "DUAL_IDENTITIES";

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
  private static final SzRecordId STU234
      = SzRecordId.FACTORY.create(VIPS, "STU234");
  private static final SzRecordId XYZ456
      = SzRecordId.FACTORY.create(VIPS, "XYZ456");
  private static final SzRecordId ZYX321
      = SzRecordId.FACTORY.create(EMPLOYEES, "ZYX321");
  private static final SzRecordId CBA654
      = SzRecordId.FACTORY.create(EMPLOYEES, "CBA654");

  private static final SzRecordId BCD123
      = SzRecordId.FACTORY.create(DUAL_IDENTITIES, "BCD123");
  private static final SzRecordId CDE456
      = SzRecordId.FACTORY.create(DUAL_IDENTITIES, "CDE456");
  private static final SzRecordId EFG789
      = SzRecordId.FACTORY.create(DUAL_IDENTITIES, "EFG789");
  private static final SzRecordId FGH012
      = SzRecordId.FACTORY.create(DUAL_IDENTITIES, "FGH012");

  private EntityDataServices entityDataServices;
  private HowRelatedServices howRelatedServices;
  private EntityDataApi entityDataApi;

  @BeforeAll
  public void initializeEnvironment() {
    this.beginTests();
    this.initializeTestEnvironment();
    this.entityDataServices = new EntityDataServices();
    this.howRelatedServices = new HowRelatedServices();
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
    dataSources.add("DUAL_IDENTITIES");

    File passengerFile      = this.preparePassengerFile();
    File employeeFile       = this.prepareEmployeeFile();
    File vipFile            = this.prepareVipFile();
    File dualIdentitiesFile = this.prepareDualIdentitiesFile();

    employeeFile.deleteOnExit();
    passengerFile.deleteOnExit();
    vipFile.deleteOnExit();
    dualIdentitiesFile.deleteOnExit();

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
                               dualIdentitiesFile,
                               DUAL_IDENTITIES,
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
        {STU901.getRecordId(), "Joe", "Schmoe", "702-555-1212",
            "101 Main Street, Las Vegas, NV 89101", "1981-12-01", "WILSON"},
        {XYZ234.getRecordId(), "Joanne", "Smith", "212-555-1212",
            "101 5th Avenue, Las Vegas, NV 10018", "1983-05-15", "JACOBS"},
        {STU234.getRecordId(), "John", "Doe", "818-555-1313",
            "100 Main Street Ste. A, Los Angeles, CA 90012", "1978-10-17",
            "WILLIAMS" },
        {XYZ456.getRecordId(), "Jane", "Doe", "818-555-1212",
            "100 Main Street Suite A, Los Angeles, CA 90012", "1979-02-05",
            "JENKINS" }
    };

    return this.prepareJsonFile("test-vips-", headers, vips);
  }

  private File prepareDualIdentitiesFile() {
    String[] headers = {
        "RECORD_ID", "NAME_FULL", "PHONE_NUMBER", "ADDR_FULL",
        "DATE_OF_BIRTH", "GENDER" };

    String[][] spouses = {
        {BCD123.getRecordId(), "Bruce Wayne", "201-765-3451",
            "101 Wayne Court; Gotham City, NJ 07017", "1974-06-05", "M" },
        {CDE456.getRecordId(), "Jack Napier", "201-875-2314",
            "101 Falconi Boulevard; Gotham City, NJ 07017", "1965-05-14", "M" },
        {EFG789.getRecordId(), "Batman", "201-782-3214",
            "Batcave; Gotham City, NJ 07020", "", "M" },
        {FGH012.getRecordId(), "Joker", "201-832-2321",
            "101 Arkham Road; Gotham City, NJ 07018", "1965-05-14", "M" }
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


  private List<List> batmanVirtualEntityArgs() {
    final SzRecordId recordId1 = BCD123;
    final SzRecordId recordId2 = EFG789;

    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("Bruce Wayne", "Batman"));
    expectedDataMap.put(ADDRESS, set("101 Wayne Court; Gotham City, NJ 07017",
                                     "Batcave; Gotham City, NJ 07020"));
    expectedDataMap.put(PHONE, set("201-765-3451", "201-782-3214"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1974-06-05", "GENDER: M"));

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("Bruce Wayne", "Batman"));

    final int expectedRecordCount = 2;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 2);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 2);
    expectedFeatureCounts.put("PHONE", 2);

    Set<SzRecordId> recordIds = set(recordId1, recordId2);
    List<List> result = new ArrayList<>(recordIds.size() + 1);
    for (int arrayCount = 0; arrayCount < recordIds.size(); arrayCount++) {
      result.add(list(recordIds,
                      arrayCount,
                      null,  // withRaw
                      null,  // forceMinimal
                      null,  // detailLevel
                      null,  // featureMode
                      null,  // withFeatureStats
                      null,  // withInternalFeatures
                      expectedRecordCount,
                      expectedFeatureCounts,
                      primaryFeatureValues,
                      null,
                      expectedDataMap,
                      null));
    }
    return result;
  }

  private List<List> jokerVirtualEntityArgs() {
    final SzRecordId recordId1 = CDE456;
    final SzRecordId recordId2 = FGH012;

    Map<SzAttributeClass, Set<String>> expectedDataMap = new LinkedHashMap<>();
    expectedDataMap.put(NAME, set("Jack Napier", "Joker"));
    expectedDataMap.put(ADDRESS,
                        set("101 Falconi Boulevard; Gotham City, NJ 07017",
                            "101 Arkham Road; Gotham City, NJ 07018"));
    expectedDataMap.put(PHONE, set("201-875-2314", "201-832-2321"));
    expectedDataMap.put(CHARACTERISTIC, set("DOB: 1965-05-14", "GENDER: M"));

    Map<String, Set<String>> primaryFeatureValues = new LinkedHashMap<>();
    primaryFeatureValues.put("NAME", set("Jack Napier", "Joker"));

    final int expectedRecordCount = 2;

    Map<String, Integer> expectedFeatureCounts = new LinkedHashMap<>();
    expectedFeatureCounts.put("NAME", 2);
    expectedFeatureCounts.put("DOB", 1);
    expectedFeatureCounts.put("ADDRESS", 2);
    expectedFeatureCounts.put("PHONE", 2);

    Set<SzRecordId> recordIds = set(recordId1, recordId2);
    List<List> result = new ArrayList<>(recordIds.size() + 1);
    for (int arrayCount = 0; arrayCount < recordIds.size(); arrayCount++) {
      result.add(list(recordIds,
                      arrayCount,
                      null,  // withRaw
                      null,  // forceMinimal
                      null,  // detailLevel
                      null,  // featureMode
                      null,  // withFeatureStats
                      null,  // withInternalFeatures
                      expectedRecordCount,
                      expectedFeatureCounts,
                      primaryFeatureValues,
                      null,
                      expectedDataMap,
                      null));
    }
    return result;
  }

  private List<Arguments> getVirtualEntityParameters() {

    List<List> baseArgs = new LinkedList<>();
    baseArgs.addAll(batmanVirtualEntityArgs());
    baseArgs.addAll(jokerVirtualEntityArgs());

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

    Random prng = new Random(RANDOM_SEED);

    List<List<Boolean>> booleanCombos = getBooleanVariants(4);
    Collections.shuffle(booleanCombos, prng);
    Iterator<List<Boolean>> booleansIter = circularIterator(booleanCombos);

    List<List> optionCombos = generateCombinations(detailLevels,
                                                   featureModes);
    Collections.shuffle(optionCombos, prng);
    Iterator<List> optionsIter = circularIterator(optionCombos);

    int loopCount
        = Math.max(booleanCombos.size(), optionCombos.size()) * 15
        / baseArgs.size();

    baseArgs.forEach(baseArgList -> {
      for (int index = 0; index < loopCount; index++) {
        List<Object>        optsList    = optionsIter.next();
        SzDetailLevel       detailLevel = (SzDetailLevel) optsList.get(0);
        SzFeatureMode       featureMode = (SzFeatureMode) optsList.get(1);

        List<Boolean> booleansList = booleansIter.next();
        Boolean withRaw               = booleansList.get(0);
        Boolean forceMinimal          = booleansList.get(1);
        Boolean withFeatureStats      = booleansList.get(2);
        Boolean withInternalFeatures  = booleansList.get(3);

        Object[] argArray = baseArgList.toArray();

        argArray[2] = withRaw;
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

  @MethodSource("getVirtualEntityParameters")
  @ParameterizedTest
  public void getVirtualEntityTest(
      Set<SzRecordId>                     recordIds,
      int                                 arrayCount,
      Boolean                             withRaw,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      Boolean                             withFeatureStats,
      Boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    this.performTest(() -> {
      String testInfo = "records=[ " + recordIds
          + " ], arrayCount=[ " + arrayCount
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("virtual-entities");
      List<String> recordsParamList = new ArrayList<>(recordIds.size());
      StringBuilder arrayBuilder = new StringBuilder();

      buildVirtualEntityQueryString(sb,
                                    recordsParamList,
                                    arrayBuilder,
                                    recordIds,
                                    arrayCount,
                                    withRaw,
                                    forceMinimal,
                                    detailLevel,
                                    featureMode,
                                    withFeatureStats,
                                    withInternalFeatures);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzVirtualEntityResponse response
          = this.howRelatedServices.getVirtualEntity(
              recordsParamList,
              (arrayBuilder.length() == 0 ? null : arrayBuilder.toString()),
              (withRaw != null ? withRaw : false),
              (forceMinimal != null ? forceMinimal : false),
              (detailLevel != null ? detailLevel : VERBOSE),
              (featureMode != null ? featureMode : WITH_DUPLICATES),
              (withFeatureStats != null ? withFeatureStats : false),
              (withInternalFeatures != null ? withInternalFeatures : false),
              uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      validateVirtualEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          expectedRecordCount,
          recordIds,
          expectedFeatureCounts,
          primaryFeatureValues,
          duplicateFeatureValues,
          expectedDataValues,
          expectedOtherDataValues,
          after - before);
    });

  }

  @MethodSource("getVirtualEntityParameters")
  @ParameterizedTest
  public void getVirtualEntityViaHttpTest(
      Set<SzRecordId>                     recordIds,
      int                                 arrayCount,
      Boolean                             withRaw,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      Boolean                             withFeatureStats,
      Boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    this.performTest(() -> {
      String testInfo = "records=[ " + recordIds
          + " ], arrayCount=[ " + arrayCount
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("virtual-entities");
      List<String> recordsParamList = new ArrayList<>(recordIds.size());
      StringBuilder arrayBuilder = new StringBuilder();

      buildVirtualEntityQueryString(sb,
                                    recordsParamList,
                                    arrayBuilder,
                                    recordIds,
                                    arrayCount,
                                    withRaw,
                                    forceMinimal,
                                    detailLevel,
                                    featureMode,
                                    withFeatureStats,
                                    withInternalFeatures);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzVirtualEntityResponse response = this.invokeServerViaHttp(
          GET, uriText, SzVirtualEntityResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateVirtualEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          expectedRecordCount,
          recordIds,
          expectedFeatureCounts,
          primaryFeatureValues,
          duplicateFeatureValues,
          expectedDataValues,
          expectedOtherDataValues,
          after - before);
    });
  }

  @MethodSource("getVirtualEntityParameters")
  @ParameterizedTest
  public void getVirtualEntityViaJavaClientTest(
      Set<SzRecordId>                     recordIds,
      int                                 arrayCount,
      Boolean                             withRaw,
      Boolean                             forceMinimal,
      SzDetailLevel                       detailLevel,
      SzFeatureMode                       featureMode,
      Boolean                             withFeatureStats,
      Boolean                             withInternalFeatures,
      Integer                             expectedRecordCount,
      Map<String,Integer>                 expectedFeatureCounts,
      Map<String,Set<String>>             primaryFeatureValues,
      Map<String,Set<String>>             duplicateFeatureValues,
      Map<SzAttributeClass, Set<String>>  expectedDataValues,
      Set<String>                         expectedOtherDataValues)
  {
    this.performTest(() -> {
      String testInfo = "records=[ " + recordIds
          + " ], arrayCount=[ " + arrayCount
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("virtual-entities");
      List<String> recordsParamList = new ArrayList<>(recordIds.size());
      StringBuilder arrayBuilder = new StringBuilder();

      buildVirtualEntityQueryString(sb,
                                    recordsParamList,
                                    arrayBuilder,
                                    recordIds,
                                    arrayCount,
                                    withRaw,
                                    forceMinimal,
                                    detailLevel,
                                    featureMode,
                                    withFeatureStats,
                                    withInternalFeatures);

      String uriText = this.formatServerUri(sb.toString());

      Collection<SzRecordId> recordParam  = new ArrayList<>(recordIds.size());
      Collection<SzRecordId> recordList   = new ArrayList<>(recordIds.size());
      for (SzRecordId recordId : recordIds) {
        if (recordList.size() < arrayCount) {
          recordList.add(recordId);
        } else {
          recordParam.add(recordId);
        }
      }

      List<com.senzing.gen.api.model.SzRecordIdentifier> clientParamIds
          = this.toClientIdList(recordParam);

      com.senzing.gen.api.model.SzRecordIdentifiers clientRecordList
          = this.toClientIds(recordList);

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

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzVirtualEntityResponse clientResponse
          = this.entityDataApi.getVirtualEntityByRecordIds(
          clientParamIds,
          clientRecordList,
          detailLvl,
          featMode,
          withFeatureStats,
          withInternalFeatures,
          forceMinimal,
          withRaw);

      long after = System.nanoTime();

      SzVirtualEntityResponse response
          = jsonCopy(clientResponse, SzVirtualEntityResponse.class);

      validateVirtualEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          forceMinimal,
          detailLevel,
          featureMode,
          withFeatureStats == null ? false : withFeatureStats,
          withInternalFeatures == null ? false : withInternalFeatures,
          expectedRecordCount,
          recordIds,
          expectedFeatureCounts,
          primaryFeatureValues,
          duplicateFeatureValues,
          expectedDataValues,
          expectedOtherDataValues,
          after - before);
    });
  }

  private StringBuilder buildVirtualEntityQueryString(
      StringBuilder       sb,
      List<String>        recordParamsList,
      StringBuilder       arrayBuilder,
      Set<SzRecordId>     recordIds,
      int                 arrayCount,
      Boolean             withRaw,
      Boolean             forceMinimal,
      SzDetailLevel       detailLevel,
      SzFeatureMode       featureMode,
      Boolean             withFeatureStats,
      Boolean             withInternalFeatures)
  {
    String prefix = "?";
    if (arrayCount < recordIds.size()) {
      int count = 0;
      for (SzRecordId recordId : recordIds) {
        if (count++ < arrayCount) continue;
        String encodedId = recordId.toString();
        recordParamsList.add(encodedId);
        sb.append(prefix).append("r=")
            .append(this.urlEncode(encodedId));
        prefix = "&";
      }
    }
    if (arrayCount > 0) {
      int count = 0;
      JsonArrayBuilder jab = Json.createArrayBuilder();
      for (SzRecordId recordId : recordIds) {
        jab.add(JsonUtilities.parseJsonObject(recordId.toString()));
        if (++count >= arrayCount) break;
      }
      JsonArray jsonArray = jab.build();
      String arrayParam = JsonUtilities.toJsonText(jsonArray);

      if (jsonArray.size() == 0 && arrayCount > 0) {
        System.err.println(
            "***** JSON ARRAY SIZE / ARRAY COUNT: " + jsonArray.size()
                + " / " + arrayCount);
      }
      arrayBuilder.append(arrayParam);
      sb.append(prefix).append("records=")
          .append(this.urlEncode(arrayParam));
      prefix = "&";
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
    if (withRaw != null) {
      sb.append(prefix).append("withRaw=").append(withRaw);
    }
    return sb;
  }

  protected static class ClientRecordId
      extends com.senzing.gen.api.model.SzRecordIdentifier
  {
    private com.senzing.gen.api.model.SzRecordId recordId;

    public ClientRecordId(com.senzing.gen.api.model.SzRecordId recordId)
    {
      this.recordId = recordId;
    }

    public ClientRecordId(SzRecordId recordId) {
      this.recordId = new com.senzing.gen.api.model.SzRecordId();
      this.recordId.setId(recordId.getRecordId());
      this.recordId.setSrc(recordId.getDataSourceCode());
    }

    @JsonValue
    public com.senzing.gen.api.model.SzRecordId getValue() {
      return this.recordId;
    }

    public String toString() {
      try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.recordId);

      } catch (JsonProcessingException exception) {
        throw new RuntimeException(exception);
      }
    }
  }

  protected static class ClientRecordIds
      extends com.senzing.gen.api.model.SzRecordIdentifiers
  {
    private List<com.senzing.gen.api.model.SzRecordId> recordIds;

    public ClientRecordIds(List<com.senzing.gen.api.model.SzRecordId> recordIds)
    {
      this.recordIds = recordIds;
    }

    @JsonValue
    public List<com.senzing.gen.api.model.SzRecordId> getValue() {
      return this.recordIds;
    }

    public String toString() {
      try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.recordIds);

      } catch (JsonProcessingException exception) {
        throw new RuntimeException(exception);
      }
    }
  }

  protected com.senzing.gen.api.model.SzRecordIdentifier toClientId(
      SzRecordId recordId)
  {
    if (recordId == null) return null;

    Class<? extends com.senzing.gen.api.model.SzEntityIdentifier> target;

    return new ClientRecordId(recordId);
  }

  protected com.senzing.gen.api.model.SzRecordIdentifiers toClientIds(
      Collection<SzRecordId> recordIds)
  {
    if (recordIds == null || recordIds.size() == 0) return null;
    List<com.senzing.gen.api.model.SzRecordId> clientIds
        = new ArrayList<>(recordIds.size());

    for (SzRecordId recordId: recordIds) {
      clientIds.add(((ClientRecordId)
          this.toClientId(recordId)).getValue());
    }
    return new ClientRecordIds(clientIds);
  }

  protected List<com.senzing.gen.api.model.SzRecordIdentifier> toClientIdList(
      Collection<SzRecordId> recordIds)
  {
    if (recordIds == null) return null;

    List<com.senzing.gen.api.model.SzRecordIdentifier> clientIds
        = new ArrayList<>(recordIds.size());

    for (SzRecordId recordId: recordIds) {
      ClientRecordId clientId = (ClientRecordId) this.toClientId(recordId);
      clientIds.add(clientId);
    }
    return clientIds;
  }

  public List<Arguments> getHowEntityParameters() {
    List<Arguments> result = new LinkedList<>();

    List<Set<SzRecordId>> recordSets = list(
        set(ABC123, MNO345, STU901),  // Joe Schmoe
        set(DEF456, PQR678, XYZ234),  // Joanne Smith
        set(GHI789, STU234),          // John Doe
        set(JKL012, XYZ456));         // Jane Doe

    Boolean[] booleanVariants = { null, Boolean.TRUE, Boolean.FALSE };

    for (Set<SzRecordId> set : recordSets) {
      for (SzRecordId recordId : set) {
        for (Boolean withRaw : booleanVariants) {
          result.add(arguments(recordId,
                               withRaw,
                               set.size(),
                               set));
        }
      }
    }
    return result;
  }

  @MethodSource("getHowEntityParameters")
  @ParameterizedTest
  public void howEntityByRecordIdTest(
      SzRecordId      recordId,
      Boolean         withRaw,
      Integer         expectedRecordCount,
      Set<SzRecordId> expectedRecordIds)
  {
    this.performTest(() -> {
      String testInfo = "recordId=[ " + recordId
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/");
      sb.append(this.urlEncode(recordId.getDataSourceCode()));
      sb.append("/records/");
      sb.append(this.urlEncode(recordId.getRecordId()));
      sb.append("/entity/how");
      if (withRaw != null) {
        sb.append("?withRaw=").append(withRaw);
      }

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzHowEntityResponse response
          = this.howRelatedServices.howEntityByRecordId(
              recordId.getDataSourceCode(),
              recordId.getRecordId(),
              (withRaw != null ? withRaw : false),
              uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      validateHowEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          expectedRecordCount,
          expectedRecordIds,
          after - before);
    });

  }

  @MethodSource("getHowEntityParameters")
  @ParameterizedTest
  public void howEntityByRecordIdViaHttpTest(
      SzRecordId      recordId,
      Boolean         withRaw,
      Integer         expectedRecordCount,
      Set<SzRecordId> expectedRecordIds)
  {
    this.performTest(() -> {
      String testInfo = "recordId=[ " + recordId
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/");
      sb.append(this.urlEncode(recordId.getDataSourceCode()));
      sb.append("/records/");
      sb.append(this.urlEncode(recordId.getRecordId()));
      sb.append("/entity/how");
      if (withRaw != null) {
        sb.append("?withRaw=").append(withRaw);
      }

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();

      SzHowEntityResponse response = this.invokeServerViaHttp(
          GET, uriText, SzHowEntityResponse.class);

      response.concludeTimers();
      long after = System.nanoTime();

      validateHowEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          expectedRecordCount,
          expectedRecordIds,
          after - before);
    });

  }

  @MethodSource("getHowEntityParameters")
  @ParameterizedTest
  public void howEntityByRecordIdViaJavaClientTest(
      SzRecordId      recordId,
      Boolean         withRaw,
      Integer         expectedRecordCount,
      Set<SzRecordId> expectedRecordIds)
  {
    this.performTest(() -> {
      String testInfo = "recordId=[ " + recordId
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/");
      sb.append(this.urlEncode(recordId.getDataSourceCode()));
      sb.append("/records/");
      sb.append(this.urlEncode(recordId.getRecordId()));
      sb.append("/entity/how");
      if (withRaw != null) {
        sb.append("?withRaw=").append(withRaw);
      }

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();

      com.senzing.gen.api.model.SzHowEntityResponse clientResponse
          = this.entityDataApi.howEntityByRecordID(
              recordId.getDataSourceCode(),
              recordId.getRecordId(),
              withRaw);

      long after = System.nanoTime();

      SzHowEntityResponse response
          = jsonCopy(clientResponse, SzHowEntityResponse.class);

      validateHowEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          expectedRecordCount,
          expectedRecordIds,
          after - before);
    });
  }

  @MethodSource("getHowEntityParameters")
  @ParameterizedTest
  public void howEntityByEntityIdTest(
      SzRecordId      recordId,
      Boolean         withRaw,
      Integer         expectedRecordCount,
      Set<SzRecordId> expectedRecordIds)
  {
    this.performTest(() -> {
      String testInfo = "recordId=[ " + recordId
          + " ], withRaw=[ " + withRaw + " ]";

      long entityId = this.getEntityIdForRecordId(recordId);

      StringBuilder sb = new StringBuilder();
      sb.append("entities/").append(entityId).append("/how");
      if (withRaw != null) {
        sb.append("?withRaw=").append(withRaw);
      }

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzHowEntityResponse response
          = this.howRelatedServices.howEntityByEntityId(
              entityId,
              (withRaw != null ? withRaw : false),
              uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      validateHowEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          expectedRecordCount,
          expectedRecordIds,
          after - before);
    });

  }

  @MethodSource("getHowEntityParameters")
  @ParameterizedTest
  public void howEntityByEntityIdViaHttpTest(
      SzRecordId      recordId,
      Boolean         withRaw,
      Integer         expectedRecordCount,
      Set<SzRecordId> expectedRecordIds)
  {
    this.performTest(() -> {
      String testInfo = "recordId=[ " + recordId
          + " ], withRaw=[ " + withRaw + " ]";

      long entityId = this.getEntityIdForRecordId(recordId);

      StringBuilder sb = new StringBuilder();
      sb.append("entities/").append(entityId).append("/how");
      if (withRaw != null) {
        sb.append("?withRaw=").append(withRaw);
      }

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();

      SzHowEntityResponse response = this.invokeServerViaHttp(
          GET, uriText, SzHowEntityResponse.class);

      response.concludeTimers();
      long after = System.nanoTime();

      validateHowEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          expectedRecordCount,
          expectedRecordIds,
          after - before);
    });

  }

  @MethodSource("getHowEntityParameters")
  @ParameterizedTest
  public void howEntityByEntityIdViaJavaClientTest(
      SzRecordId      recordId,
      Boolean         withRaw,
      Integer         expectedRecordCount,
      Set<SzRecordId> expectedRecordIds)
  {
    this.performTest(() -> {
      String testInfo = "recordId=[ " + recordId
          + " ], withRaw=[ " + withRaw + " ]";

      long entityId = this.getEntityIdForRecordId(recordId);

      StringBuilder sb = new StringBuilder();
      sb.append("entities/").append(entityId).append("/how");
      if (withRaw != null) {
        sb.append("?withRaw=").append(withRaw);
      }

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();

      com.senzing.gen.api.model.SzHowEntityResponse clientResponse
          = this.entityDataApi.howEntityByEntityID(entityId, withRaw);

      long after = System.nanoTime();

      SzHowEntityResponse response
          = jsonCopy(clientResponse, SzHowEntityResponse.class);

      validateHowEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          withRaw,
          expectedRecordCount,
          expectedRecordIds,
          after - before);
    });

  }

  protected void validateHowEntityResponse(
      String              testInfo,
      SzHowEntityResponse response,
      SzHttpMethod        httpMethod,
      String              selfLink,
      Boolean             withRaw,
      Integer             expectedRecordCount,
      Set<SzRecordId>     expectedRecordIds,
      long                maxDuration)
  {
    validateBasics(testInfo,
                   response,
                   httpMethod,
                   selfLink,
                   maxDuration);

    SzHowEntityResult result = response.getData();

    assertNotNull(result, "Response data is null: " + testInfo);

    if (withRaw != null && withRaw) {
      validateRawDataMap(testInfo,
                         response.getRawData(),
                         true,
                         "HOW_RESULTS");
      Object howResult = ((Map) response.getRawData()).get("HOW_RESULTS");
      validateRawDataMap(testInfo,
                         howResult,
                         true,
                         "RESOLUTION_STEPS", "FINAL_STATE");
      Object steps = ((Map) howResult).get("RESOLUTION_STEPS");
      validateRawDataMapArray(testInfo,
                              steps,
                              true,
                              "STEP",
                              "VIRTUAL_ENTITY_1",
                              "VIRTUAL_ENTITY_2",
                              "INBOUND_VIRTUAL_ENTITY_ID",
                              "RESULT_VIRTUAL_ENTITY_ID",
                              "MATCH_INFO");
      Object finalState = ((Map) howResult).get("FINAL_STATE");
      validateRawDataMap(testInfo,
                         finalState,
                         true,
                         "NEED_REEVALUATION",
                         "VIRTUAL_ENTITIES");
    }

    // check if we need to validate the records
    if (expectedRecordCount != null || expectedRecordIds != null) {
      // create a set of record ID's
      Set<SzRecordId> recordIds = new LinkedHashSet<>();

      // get all the records from the final states
      List<SzVirtualEntity> finalStates
          = response.getData().getFinalStates();

      // iterate over the virtual entities
      for (SzVirtualEntity virtualEntity : finalStates) {
        // get the records
        Set<SzVirtualEntityRecord> records
            = virtualEntity.getRecords();

        // for each record, build an SzRecordId
        for (SzVirtualEntityRecord record : records) {
          SzRecordId recordId = SzRecordId.FACTORY.create(
              record.getDataSource(), record.getRecordId());
          recordIds.add(recordId);
        }
      }

      if (expectedRecordCount != null) {
        assertEquals(expectedRecordCount, recordIds.size(),
                     "Unexpected number of records: " + testInfo);
      }

      if (expectedRecordIds != null) {
        Set<SzRecordId> extras = new LinkedHashSet<>(recordIds);
        extras.removeAll(expectedRecordIds);
        Set<SzRecordId> missing = new LinkedHashSet<>(expectedRecordIds);
        missing.removeAll(recordIds);

        assertEquals(expectedRecordIds, recordIds,
                     "Unexpected records: extras=[ " + extras
                     + " ], missing=[ " + missing + " ]");
      }
    }

    // verify the traversal of the tree
    Map<String, SzResolutionStep> steps = result.getResolutionSteps();
    List<SzVirtualEntity> virtualEntities = new LinkedList<>();
    virtualEntities.addAll(result.getFinalStates());
    while (virtualEntities.size() > 0) {
      SzVirtualEntity virtualEntity = virtualEntities.remove(0);
      String virtualEntityId = virtualEntity.getVirtualEntityId();
      SzResolutionStep step = steps.get(virtualEntityId);
      boolean found = (step != null);
      if (virtualEntity.isSingleton()) {
        assertFalse(found,
                   "Resolution step was found for a singleton virtual "
                       + "entity.  virtualEntityId=[ " + virtualEntityId
                       + " ], virtualEntity=[ " + virtualEntity + " ]");
      } else {
        assertTrue(found,
                    "No resolution step found for non-singleton virtual "
                        + "entity.  virtualEntityId=[ " + virtualEntityId
                        + " ], virtualEntity=[ " + virtualEntity + " ]");
      }
      if (step != null) {
        virtualEntities.add(step.getInboundVirtualEntity());
        virtualEntities.add(step.getCandidateVirtualEntity());
      }
    }

    if (withRaw != null && withRaw) {
      Map<String, String> inboundMap = new LinkedHashMap<>();
      Map<String, String> rawInboundMap = new LinkedHashMap<>();
      for (SzResolutionStep step : result.getResolutionSteps().values()) {
        inboundMap.put(step.getResolvedVirtualEntityId(),
                       step.getInboundVirtualEntity().getVirtualEntityId());
      }
      Map rawData   = (Map) response.getRawData();
      Map rawResult = (Map) rawData.get("HOW_RESULTS");
      Collection rawSteps = (Collection) rawResult.get("RESOLUTION_STEPS");
      for (Object step : rawSteps) {
        Map stepMap = (Map) step;
        String resultId   = (String) stepMap.get("RESULT_VIRTUAL_ENTITY_ID");
        String inboundId  = (String) stepMap.get("INBOUND_VIRTUAL_ENTITY_ID");
        rawInboundMap.put(resultId, inboundId);
      }

      assertEquals(rawInboundMap, inboundMap,
                   "Inbound virtual entities of steps do not match "
                       + "the raw data.  expected=[ " + rawInboundMap
                       + " ], actual=[ " + inboundMap + " ]");
    }
  }

}
