package com.senzing.api.services;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senzing.api.model.*;
import com.senzing.api.model.impl.SzEntityPathImpl;
import com.senzing.gen.api.invoker.ApiClient;
import com.senzing.gen.api.services.EntityDataApi;
import com.senzing.gen.api.services.EntityGraphApi;
import com.senzing.repomgr.RepositoryManager;
import com.senzing.util.CollectionUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static com.senzing.api.model.SzDetailLevel.VERBOSE;
import static com.senzing.api.model.SzFeatureMode.NONE;
import static com.senzing.api.model.SzFeatureMode.WITH_DUPLICATES;
import static com.senzing.api.model.SzHttpMethod.GET;
import static com.senzing.util.CollectionUtilities.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static com.senzing.api.services.ResponseValidators.*;
import static java.util.Collections.*;

@TestInstance(Lifecycle.PER_CLASS)
public class EntityGraphServicesTest extends AbstractServiceTest {
  private static final long RANDOM_SEED = 2345678910L;

  private static final int DEFAULT_PATH_DEGREES = 3;
  private static final int DEFAULT_NETWORK_DEGREES = 3;
  private static final int DEFAULT_BUILD_OUT = 1;
  private static final int DEFAULT_MAX_ENTITIES = 1000;

  private static final String PASSENGERS = "PASSENGERS";
  private static final String EMPLOYEES  = "EMPLOYEES";
  private static final String VIPS       = "VIPS";

  private static final SzRecordId ABC123 = SzRecordId.FACTORY.create(
      PASSENGERS,"ABC123");
  private static final SzRecordId DEF456 = SzRecordId.FACTORY.create(
      PASSENGERS,"DEF456");
  private static final SzRecordId GHI789 = SzRecordId.FACTORY.create(
      PASSENGERS,"GHI789");
  private static final SzRecordId JKL012 = SzRecordId.FACTORY.create(
      PASSENGERS,"JKL012");
  private static final SzRecordId MNO345 = SzRecordId.FACTORY.create(
      EMPLOYEES,"MNO345");
  private static final SzRecordId PQR678 = SzRecordId.FACTORY.create(
      EMPLOYEES,"PQR678");
  private static final SzRecordId ABC567 = SzRecordId.FACTORY.create(
      EMPLOYEES,"ABC567");
  private static final SzRecordId DEF890 = SzRecordId.FACTORY.create(
      EMPLOYEES,"DEF890");
  private static final SzRecordId STU901 = SzRecordId.FACTORY.create(
      VIPS,"STU901");
  private static final SzRecordId XYZ234 = SzRecordId.FACTORY.create(
      VIPS,"XYZ234");
  private static final SzRecordId GHI123 = SzRecordId.FACTORY.create(
      VIPS,"GHI123");
  private static final SzRecordId JKL456 = SzRecordId.FACTORY.create(
      VIPS,"JKL456");

  private EntityGraphServices entityGraphServices;
  private EntityDataServices entityDataServices;
  private EntityGraphApi entityGraphApi;
  private EntityDataApi entityDataApi;

  @BeforeAll
  public void initializeEnvironment() {
    this.beginTests();
    this.initializeTestEnvironment();
    this.entityGraphServices  = new EntityGraphServices();
    this.entityDataServices   = new EntityDataServices();
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(this.formatServerUri(""));
    this.entityDataApi = new EntityDataApi(apiClient);
    this.entityGraphApi = new EntityGraphApi(apiClient);
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

    File passengerFile = this.preparePassengerFile();
    File employeeFile = this.prepareEmployeeFile();
    File vipFile = this.prepareVipFile();

    employeeFile.deleteOnExit();
    passengerFile.deleteOnExit();
    vipFile.deleteOnExit();

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
  }

  private File preparePassengerFile() {
    String[] headers = {
        "RECORD_ID", "NAME_FIRST", "NAME_LAST", "MOBILE_PHONE_NUMBER",
        "HOME_PHONE_NUMBER", "ADDR_FULL", "DATE_OF_BIRTH"};

    String[][] passengers = {
        {ABC123.getRecordId(), "Joe", "Schmoe", "702-555-1212", "702-777-2424",
            "101 Main Street, Las Vegas, NV 89101", "12-JAN-1981"},
        {DEF456.getRecordId(), "Joann", "Smith", "702-555-1212", "702-888-3939",
            "101 Fifth Ave, Las Vegas, NV 10018", "15-MAY-1983"},
        {GHI789.getRecordId(), "John", "Doe", "818-555-1313", "818-999-2121",
            "101 Fifth Ave, Las Vegas, NV 10018", "17-OCT-1978"},
        {JKL012.getRecordId(), "Jane", "Doe", "818-555-1313", "818-222-3131",
            "400 River Street, Pasadena, CA 90034", "23-APR-1974"}
    };
    return this.prepareCSVFile("test-passengers-", headers, passengers);
  }

  private File prepareEmployeeFile() {
    String[] headers = {
        "RECORD_ID", "NAME_FIRST", "NAME_LAST", "MOBILE_PHONE_NUMBER",
        "HOME_PHONE_NUMBER", "ADDR_FULL", "DATE_OF_BIRTH"};

    String[][] employees = {
        {MNO345.getRecordId(), "Bill", "Wright", "702-444-2121", "702-123-4567",
            "101 Main Street, Las Vegas, NV 89101", "22-AUG-1981"},
        {PQR678.getRecordId(), "Craig", "Smith", "212-555-1212", "702-888-3939",
            "451 Dover Street, Las Vegas, NV 89108", "17-NOV-1982"},
        {ABC567.getRecordId(), "Kim", "Long", "702-246-8024", "702-135-7913",
            "451 Dover Street, Las Vegas, NV 89108", "24-OCT-1976"},
        {DEF890.getRecordId(), "Kathy", "Osborne", "702-444-2121", "702-111-2222",
            "707 Seventh Ave, Las Vegas, NV 89143", "27-JUL-1981"}
    };

    return this.prepareJsonArrayFile("test-employees-", headers, employees);
  }

  private File prepareVipFile() {
    String[] headers = {
        "RECORD_ID", "NAME_FIRST", "NAME_LAST", "MOBILE_PHONE_NUMBER",
        "HOME_PHONE_NUMBER", "ADDR_FULL", "DATE_OF_BIRTH"};

    String[][] vips = {
        {STU901.getRecordId(), "Martha", "Wayne", "818-891-9292", "818-987-1234",
            "888 Sepulveda Blvd, Los Angeles, CA 90034", "27-NOV-1973"},
        {XYZ234.getRecordId(), "Jane", "Johnson", "702-333-7171", "702-123-9876",
            "400 River Street, Pasadena, CA 90034", "5-SEP-1975"},
        {GHI123.getRecordId(), "Martha", "Kent", "818-333-5757", "702-123-9876",
            "888 Sepulveda Blvd, Los Angeles, CA 90034", "17-OCT-1978"},
        {JKL456.getRecordId(), "Kelly", "Rogers", "702-333-7171", "702-789-6543",
            "707 Seventh Ave, Las Vegas, NV 89143", "5-FEB-1979"}
    };

    return this.prepareJsonFile("test-vips-", headers, vips);
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

  private static List<List> pathArgs(SzRecordId       fromRecord,
                                     SzRecordId       toRecord,
                                     Integer          maxDegrees,
                                     Set<SzRecordId>  avoid,
                                     Boolean          forbid,
                                     List<String>     sources,
                                     Integer          expectedPathLength,
                                     List<SzRecordId> expectedPath)
  {

    List<List> result = new LinkedList<>();

    // creates variants of the specified avoid parameter to exercise both
    // of the parameters that deal with avoiding entities
    List<List> avoidVariants = new LinkedList<>();
    if (avoid == null || avoid.size() == 0) {
      avoidVariants.add(list(null, null));
    } else if (avoid.size() == 1) {
      avoidVariants.add(list(avoid, null));
      avoidVariants.add(list(null, avoid));
    } else if (avoid.size() > 1) {
      avoidVariants.add(list(avoid, null));
      avoidVariants.add(list(null, avoid));

      int avoidCount = avoid.size();
      Set<SzRecordId> avoid1 = new LinkedHashSet<>();
      Set<SzRecordId> avoid2 = new LinkedHashSet<>();
      for (SzRecordId recordId : avoid) {
        if ((avoidCount % 2) == 0) {
          avoid1.add(recordId);
        } else {
          avoid2.add(recordId);
        }
        avoidCount--;
      }
      avoidVariants.add(list(avoid1, avoid2));
      avoidVariants.add(list(avoid2, avoid1));
    }

    // create variants on the forbid parameter to test default values
    List<Boolean> forbidVariants = new LinkedList<>();
    if (avoid == null || avoid.size() == 0) {
      // nothing to avoid so try all variants of forbid
      forbidVariants.add(null);
      forbidVariants.add(true);
      forbidVariants.add(false);

    } else if (forbid == null || forbid == false) {
      // not forbidding so try both "default" and false option
      forbidVariants.add(null);
      forbidVariants.add(false);
    } else {
      // forbid specifically requested so it is the only variant we will use
      forbidVariants.add(true);
    }

    // create variants on the max degrees parameter to test default values
    List<Integer> degreesVariants = new LinkedList<>();
    if (maxDegrees == null || maxDegrees == DEFAULT_PATH_DEGREES) {
      degreesVariants.add(null);
      degreesVariants.add(DEFAULT_PATH_DEGREES);
    } else {
      degreesVariants.add(maxDegrees);
    }

    avoidVariants.forEach(avoidVariant -> {
      degreesVariants.forEach(degreeVariant -> {
        forbidVariants.forEach(forbidVariant -> {
          result.add(
            list(fromRecord,
                 toRecord,
                 degreeVariant,
                 avoidVariant.get(0),
                 avoidVariant.get(1),
                 forbidVariant,
                 sources,
                 null,  // forceMinimal (7)
                 null,  // detailLevel (8)
                 null,  // featureMode (9)
                 null,  // withFeatureStats (10)
                 null,  // withInternalFeatures (11)
                 null,  // withRaw (12)
                 expectedPathLength,
                 expectedPath));
        });
      });
    });

    return result;
  }

  private List<Arguments> getEntityPathParameters() {

    List<List> baseArgs = new LinkedList<>();

    baseArgs.addAll(pathArgs(
        ABC123, DEF890, null, null, null,
        null, 3, list(ABC123, MNO345, DEF890)));

    baseArgs.addAll(pathArgs(
        ABC123, JKL456, null, null, null, null,
        4, list(ABC123, MNO345, DEF890, JKL456)));

    baseArgs.addAll(pathArgs(
        ABC123, JKL456, 2, null, null, null,
        -1, Collections.emptyList()));

    baseArgs.addAll(pathArgs(
        ABC123, JKL456, null, set(DEF890), null, null,
        4, list(ABC123, MNO345, DEF890, JKL456)));

    baseArgs.addAll(pathArgs(
        ABC123, JKL456, null, set(DEF890), true, null,
        -1, Collections.emptyList()));

    baseArgs.addAll(pathArgs(
        ABC123, JKL456, 10, set(DEF890), true, null,
        6, list(ABC123, DEF456, GHI789, JKL012, XYZ234, JKL456)));

    baseArgs.addAll(pathArgs(
        ABC123, JKL012, 10, null, null, list(EMPLOYEES),
        6, list(ABC123, MNO345, DEF890, JKL456, XYZ234, JKL012)));

    baseArgs.addAll(pathArgs(
        ABC123, JKL012, 10, null, null, list(VIPS),
        6, list(ABC123, MNO345, DEF890, JKL456, XYZ234, JKL012)));

    baseArgs.addAll(pathArgs(
        ABC123, JKL012, 10, null, null, list(EMPLOYEES, VIPS),
        6, list(ABC123, MNO345, DEF890, JKL456, XYZ234, JKL012)));

    // make a random-access version of the list
    baseArgs = new ArrayList<>(baseArgs);

    Boolean[] booleanVariants = {null, true, false};
    List<Boolean> booleanVariantList = Arrays.asList(booleanVariants);

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

    List<List> optionCombos = generateCombinations(detailLevels, featureModes);
    Collections.shuffle(optionCombos, prng);
    Iterator<List> optionsIter = circularIterator(optionCombos);

    int loopCount
        = Math.max(booleanCombos.size(), optionCombos.size()) * 15
        / baseArgs.size();

    int totalCount = loopCount * baseArgs.size();
    List<Arguments> result = new ArrayList<>(totalCount);

    baseArgs.forEach(baseArgList -> {
      for (int index = 0; index < loopCount; index++) {
        List optsList               = optionsIter.next();
        List<Boolean> booleansList  = booleansIter.next();

        SzDetailLevel detailLevel           = (SzDetailLevel) optsList.get(0);
        SzFeatureMode featureMode           = (SzFeatureMode) optsList.get(1);
        Boolean       forceMinimal          = booleansList.get(0);
        Boolean       withFeatureStats      = booleansList.get(1);
        Boolean       withInternalFeatures  = booleansList.get(2);
        Boolean       withRaw               = booleansList.get(3);

        Object[] argArray = baseArgList.toArray();
        argArray[7]   = forceMinimal;
        argArray[8]   = detailLevel;
        argArray[9]   = featureMode;
        argArray[10]  = withFeatureStats;
        argArray[11]  = withInternalFeatures;
        argArray[12]  = withRaw;

        result.add(arguments(argArray));
      }
    });

    return result;
  }

  private StringBuilder buildPathQueryString(
      StringBuilder         sb,
      SzEntityIdentifier    fromIdentifier,
      SzEntityIdentifier    toIdentifier,
      Integer               maxDegrees,
      SzEntityIdentifiers   avoidParam,
      SzEntityIdentifiers   avoidList,
      Boolean               forbidAvoided,
      List<String>          sourcesParam,
      Boolean               forceMinimal,
      SzDetailLevel         detailLevel,
      SzFeatureMode         featureMode,
      Boolean               withFeatureStats,
      Boolean               withInternalFeatures,
      Boolean               withRaw)
  {
    try {
      sb.append("?from=").append(
          URLEncoder.encode(fromIdentifier.toString(), "UTF-8"));
      sb.append("&to=").append(
          URLEncoder.encode(toIdentifier.toString(), "UTF-8"));

      if (maxDegrees != null) {
        sb.append("&maxDegrees=").append(maxDegrees);
      }
      if (avoidParam != null && !avoidParam.isEmpty()) {
        for (SzEntityIdentifier identifier : avoidParam.getIdentifiers()) {
          sb.append("&x=").append(
              URLEncoder.encode(identifier.toString(), "UTF-8"));
        }
      }
      if (avoidList != null && !avoidList.isEmpty()) {
        sb.append("&avoidEntities=").append(
            URLEncoder.encode(avoidList.toString(), "UTF-8"));
      }
      if (forbidAvoided != null) {
        sb.append("&forbidAvoided=").append(forbidAvoided);
      }
      if (sourcesParam != null && sourcesParam.size() > 0) {
        for (String value: sourcesParam) {
          sb.append("&s=").append(URLEncoder.encode(value, "UTF-8"));
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
      if (withRaw != null) {
        sb.append("&withRaw=").append(withRaw);
      }
      return sb;

    } catch (UnsupportedEncodingException cannotHappen) {
      throw new RuntimeException(cannotHappen);
    }
  }

  private Long asEntityId(SzEntityIdentifier identifier) {
    if (identifier == null) return null;
    if (identifier instanceof SzEntityId) {
      return ((SzEntityId) identifier).getValue();
    }
    return this.getEntityIdForRecordId((SzRecordId) identifier);
  }

  private List<Long> asEntityIds(
      Collection<? extends SzEntityIdentifier> identifiers)
  {
    return this.asEntityIds(SzEntityIdentifiers.FACTORY.create(identifiers));
  }

  private List<Long> asEntityIds(SzEntityIdentifiers identifiers) {
    if (identifiers == null || identifiers.isEmpty()) return null;

    List<Long> entityIds = new ArrayList<>(identifiers.getCount());
    for (SzEntityIdentifier identifier : identifiers.getIdentifiers()) {
      if (identifier instanceof SzEntityId) {
        entityIds.add(((SzEntityId) identifier).getValue());
      } else {
        entityIds.add(this.getEntityIdForRecordId((SzRecordId) identifier));
      }
    }
    return entityIds;
  }

  private SzEntityIdentifier normalizeIdentifier(SzRecordId recordId,
                                                 boolean    asEntityId)
  {
   if (recordId == null) return null;
   if (!asEntityId) return recordId;
   Long entityId = this.getEntityIdForRecordId(recordId);
   return SzEntityId.FACTORY.create(entityId);
  }

  private SzEntityIdentifiers normalizeIdentifiers(
      Collection<SzRecordId>  recordIds,
      boolean                 asEntityIds)
  {
    if (recordIds == null) return null;
    if (recordIds.size() == 0) return null;
    if (!asEntityIds) return SzEntityIdentifiers.FACTORY.create(recordIds);
    List<SzEntityId> entityIds = new ArrayList<>(recordIds.size());
    for (SzRecordId recordId : recordIds) {
      long entityId = this.getEntityIdForRecordId(recordId);
      entityIds.add(SzEntityId.FACTORY.create(entityId));
    }
    return SzEntityIdentifiers.FACTORY.create(entityIds);
  }

  private List<String> formatIdentifierParam(SzEntityIdentifiers identifiers)
  {
    if (identifiers == null || identifiers.isEmpty()) return null;
    List<String> result = new ArrayList<>(identifiers.getCount());
    for (SzEntityIdentifier identifier : identifiers.getIdentifiers()) {
      result.add(identifier.toString());
    }
    return result;
  }

  private String formatIdentifierList(SzEntityIdentifiers identifiers)
  {
    if (identifiers == null || identifiers.isEmpty()) return null;
    return identifiers.toString();
  }

  @ParameterizedTest
  @MethodSource("getEntityPathParameters")
  public void getPathByRecordIdTest(SzRecordId              fromRecordId,
                                    SzRecordId              toRecordId,
                                    Integer                 maxDegrees,
                                    Collection<SzRecordId>  avoidParam,
                                    Collection<SzRecordId>  avoidList,
                                    Boolean                 forbidAvoided,
                                    List<String>            sourcesParam,
                                    Boolean                 forceMinimal,
                                    SzDetailLevel           detailLevel,
                                    SzFeatureMode           featureMode,
                                    Boolean                 withFeatureStats,
                                    Boolean                 withInternalFeatures,
                                    Boolean                 withRaw,
                                    Integer                 expectedPathLength,
                                    List<SzRecordId>        expectedPath)
  {
    this.performTest(() -> {
      String testInfo = "fromRecord=[ " + fromRecordId
          + " ], toRecord=[ " + toRecordId
          + " ], maxDegrees=[ " + maxDegrees
          + " ], avoidParam=[ " + avoidParam
          + " ], avoidList=[ " + avoidList
          + " ], forbidAvoided=[ " + forbidAvoided
          + " ], sources=[ " + sourcesParam
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifier fromIdentifer
          = this.normalizeIdentifier(fromRecordId,false);

      SzEntityIdentifier toIdentifier
          = this.normalizeIdentifier(toRecordId,false);

      SzEntityIdentifiers avoidParamIds
          = this.normalizeIdentifiers(avoidParam,false);

      SzEntityIdentifiers avoidListIds
          = this.normalizeIdentifiers(avoidList,false);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-paths");

      buildPathQueryString(sb,
                           fromIdentifer,
                           toIdentifier,
                           maxDegrees,
                           avoidParamIds,
                           avoidListIds,
                           forbidAvoided,
                           sourcesParam,
                           forceMinimal,
                           detailLevel,
                           featureMode,
                           withFeatureStats,
                           withInternalFeatures,
                           withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzEntityPathResponse response = this.entityGraphServices.getEntityPath(
          fromIdentifer.toString(),
          toIdentifier.toString(),
          (maxDegrees == null ? DEFAULT_PATH_DEGREES : maxDegrees),
          formatIdentifierParam(avoidParamIds),
          formatIdentifierList(avoidListIds),
          (forbidAvoided == null ? false : forbidAvoided),
          sourcesParam,
          (forceMinimal == null ? false : forceMinimal),
          (detailLevel == null ? VERBOSE : detailLevel),
          (featureMode == null ? WITH_DUPLICATES : featureMode),
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          (withRaw == null ? false : withRaw),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      this.validateEntityPathResponse(
          testInfo,
          response,
          GET,
          uriText,
          fromIdentifer,
          toIdentifier,
          (maxDegrees != null ? maxDegrees : DEFAULT_PATH_DEGREES),
          avoidParamIds,
          avoidListIds,
          forbidAvoided,
          sourcesParam,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          withRaw,
          expectedPathLength,
          expectedPath,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityPathParameters")
  public void getPathByRecordIdViaHttpTest(
      SzRecordId              fromRecordId,
      SzRecordId              toRecordId,
      Integer                 maxDegrees,
      Collection<SzRecordId>  avoidParam,
      Collection<SzRecordId>  avoidList,
      Boolean                 forbidAvoided,
      List<String>            sourcesParam,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathLength,
      List<SzRecordId>        expectedPath)
  {
    this.performTest(() -> {
      String testInfo = "fromRecord=[ " + fromRecordId
          + " ], toRecord=[ " + toRecordId
          + " ], maxDegrees=[ " + maxDegrees
          + " ], avoidParam=[ " + avoidParam
          + " ], avoidList=[ " + avoidList
          + " ], forbidAvoided=[ " + forbidAvoided
          + " ], sources=[ " + sourcesParam
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifier fromIdentifer
          = this.normalizeIdentifier(fromRecordId,false);

      SzEntityIdentifier toIdentifier
          = this.normalizeIdentifier(toRecordId,false);

      SzEntityIdentifiers avoidParamIds
          = this.normalizeIdentifiers(avoidParam,false);

      SzEntityIdentifiers avoidListIds
          = this.normalizeIdentifiers(avoidList,false);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-paths");

      buildPathQueryString(sb,
                           fromIdentifer,
                           toIdentifier,
                           maxDegrees,
                           avoidParamIds,
                           avoidListIds,
                           forbidAvoided,
                           sourcesParam,
                           forceMinimal,
                           detailLevel,
                           featureMode,
                           withFeatureStats,
                           withInternalFeatures,
                           withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      SzEntityPathResponse response = this.invokeServerViaHttp(
          GET, uriText, SzEntityPathResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      this.validateEntityPathResponse(
          testInfo,
          response,
          GET,
          uriText,
          fromIdentifer,
          toIdentifier,
          (maxDegrees != null ? maxDegrees : DEFAULT_PATH_DEGREES),
          avoidParamIds,
          avoidListIds,
          forbidAvoided,
          sourcesParam,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          withRaw,
          expectedPathLength,
          expectedPath,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityPathParameters")
  public void getPathByRecordIdViaJavaClient(
      SzRecordId              fromRecordId,
      SzRecordId              toRecordId,
      Integer                 maxDegrees,
      Collection<SzRecordId>  avoidParam,
      Collection<SzRecordId>  avoidList,
      Boolean                 forbidAvoided,
      List<String>            sourcesParam,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathLength,
      List<SzRecordId>        expectedPath)
  {
    this.performTest(() -> {
      String testInfo = "fromRecord=[ " + fromRecordId
          + " ], toRecord=[ " + toRecordId
          + " ], maxDegrees=[ " + maxDegrees
          + " ], avoidParam=[ " + avoidParam
          + " ], avoidList=[ " + avoidList
          + " ], forbidAvoided=[ " + forbidAvoided
          + " ], sources=[ " + sourcesParam
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifier fromIdentifer
          = this.normalizeIdentifier(fromRecordId,false);

      SzEntityIdentifier toIdentifier
          = this.normalizeIdentifier(toRecordId,false);

      SzEntityIdentifiers avoidParamIds
          = this.normalizeIdentifiers(avoidParam,false);

      SzEntityIdentifiers avoidListIds
          = this.normalizeIdentifiers(avoidList,false);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-paths");

      buildPathQueryString(sb,
                           fromIdentifer,
                           toIdentifier,
                           maxDegrees,
                           avoidParamIds,
                           avoidListIds,
                           forbidAvoided,
                           sourcesParam,
                           forceMinimal,
                           detailLevel,
                           featureMode,
                           withFeatureStats,
                           withInternalFeatures,
                           withRaw);

      String uriText = this.formatServerUri(sb.toString());

      com.senzing.gen.api.model.SzDetailLevel clientDetailLevel
          = (detailLevel == null)
          ? null
          : com.senzing.gen.api.model.SzDetailLevel.valueOf(
              detailLevel.toString());

      com.senzing.gen.api.model.SzFeatureMode clientFeatureMode
          = (featureMode == null)
          ? null
          : com.senzing.gen.api.model.SzFeatureMode.valueOf(
              featureMode.toString());

      List<com.senzing.gen.api.model.SzEntityIdentifier> clientAvoidIds
          = this.toClientIdList(avoidParam, false);

      com.senzing.gen.api.model.SzEntityIdentifiers clientAvoidList
          = this.toClientIds(avoidList,  false);

      com.senzing.gen.api.model.SzEntityIdentifier clientFrom
          = this.toClientId(fromRecordId, false);

      com.senzing.gen.api.model.SzEntityIdentifier clientTo
          = this.toClientId(toRecordId, false);

      long before = System.nanoTime();

      com.senzing.gen.api.model.SzEntityPathResponse clientResponse
          = this.entityGraphApi.findEntityPath(clientFrom,
                                               clientTo,
                                               maxDegrees,
                                               clientAvoidIds,
                                               clientAvoidList,
                                               forbidAvoided,
                                               sourcesParam,
                                               clientDetailLevel,
                                               clientFeatureMode,
                                               withFeatureStats,
                                               withInternalFeatures,
                                               forceMinimal,
                                               withRaw);
      long after = System.nanoTime();

      SzEntityPathResponse response
          = jsonCopy(clientResponse, SzEntityPathResponse.class);

      this.validateEntityPathResponse(
          testInfo,
          response,
          GET,
          uriText,
          fromIdentifer,
          toIdentifier,
          (maxDegrees != null ? maxDegrees : DEFAULT_PATH_DEGREES),
          avoidParamIds,
          avoidListIds,
          forbidAvoided,
          sourcesParam,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          withRaw,
          expectedPathLength,
          expectedPath,
          after - before);
    });
  }


  @ParameterizedTest
  @MethodSource("getEntityPathParameters")
  public void getPathByEntityIdTest(SzRecordId              fromRecordId,
                                    SzRecordId              toRecordId,
                                    Integer                 maxDegrees,
                                    Collection<SzRecordId>  avoidParam,
                                    Collection<SzRecordId>  avoidList,
                                    Boolean                 forbidAvoided,
                                    List<String>            sourcesParam,
                                    Boolean                 forceMinimal,
                                    SzDetailLevel           detailLevel,
                                    SzFeatureMode           featureMode,
                                    Boolean                 withFeatureStats,
                                    Boolean                 withInternalFeatures,
                                    Boolean                 withRaw,
                                    Integer                 expectedPathLength,
                                    List<SzRecordId>        expectedPath)
  {
    this.performTest(() -> {
      String testInfo = "fromRecord=[ " + fromRecordId
          + " ], toRecord=[ " + toRecordId
          + " ], maxDegrees=[ " + maxDegrees
          + " ], avoidParam=[ " + avoidParam
          + " ], avoidList=[ " + avoidList
          + " ], forbidAvoided=[ " + forbidAvoided
          + " ], sources=[ " + sourcesParam
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifier fromIdentifer
          = this.normalizeIdentifier(fromRecordId,true);

      SzEntityIdentifier toIdentifier
          = this.normalizeIdentifier(toRecordId,true);

      SzEntityIdentifiers avoidParamIds
          = this.normalizeIdentifiers(avoidParam,true);

      SzEntityIdentifiers avoidListIds
          = this.normalizeIdentifiers(avoidList,true);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-paths");

      buildPathQueryString(sb,
                           fromIdentifer,
                           toIdentifier,
                           maxDegrees,
                           avoidParamIds,
                           avoidListIds,
                           forbidAvoided,
                           sourcesParam,
                           forceMinimal,
                           detailLevel,
                           featureMode,
                           withFeatureStats,
                           withInternalFeatures,
                           withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzEntityPathResponse response = this.entityGraphServices.getEntityPath(
          fromIdentifer.toString(),
          toIdentifier.toString(),
          (maxDegrees == null ? DEFAULT_PATH_DEGREES : maxDegrees),
          formatIdentifierParam(avoidParamIds),
          formatIdentifierList(avoidListIds),
          (forbidAvoided == null ? false : forbidAvoided),
          sourcesParam,
          (forceMinimal == null ? false : forceMinimal),
          (detailLevel == null ? VERBOSE : detailLevel),
          (featureMode == null ? WITH_DUPLICATES : featureMode),
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          (withRaw == null ? false : withRaw),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      this.validateEntityPathResponse(
          testInfo,
          response,
          GET,
          uriText,
          fromIdentifer,
          toIdentifier,
          (maxDegrees != null ? maxDegrees : DEFAULT_PATH_DEGREES),
          avoidParamIds,
          avoidListIds,
          forbidAvoided,
          sourcesParam,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          withRaw,
          expectedPathLength,
          expectedPath,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityPathParameters")
  public void getPathByEntityIdViaHttpTest(
      SzRecordId              fromRecordId,
      SzRecordId              toRecordId,
      Integer                 maxDegrees,
      Collection<SzRecordId>  avoidParam,
      Collection<SzRecordId>  avoidList,
      Boolean                 forbidAvoided,
      List<String>            sourcesParam,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathLength,
      List<SzRecordId>        expectedPath)
  {
    this.performTest(() -> {
      String testInfo = "fromRecord=[ " + fromRecordId
          + " ], toRecord=[ " + toRecordId
          + " ], maxDegrees=[ " + maxDegrees
          + " ], avoidParam=[ " + avoidParam
          + " ], avoidList=[ " + avoidList
          + " ], forbidAvoided=[ " + forbidAvoided
          + " ], sources=[ " + sourcesParam
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifier fromIdentifer
          = this.normalizeIdentifier(fromRecordId,true);

      SzEntityIdentifier toIdentifier
          = this.normalizeIdentifier(toRecordId,true);

      SzEntityIdentifiers avoidParamIds
          = this.normalizeIdentifiers(avoidParam,true);

      SzEntityIdentifiers avoidListIds
          = this.normalizeIdentifiers(avoidList,true);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-paths");

      buildPathQueryString(sb,
                           fromIdentifer,
                           toIdentifier,
                           maxDegrees,
                           avoidParamIds,
                           avoidListIds,
                           forbidAvoided,
                           sourcesParam,
                           forceMinimal,
                           detailLevel,
                           featureMode,
                           withFeatureStats,
                           withInternalFeatures,
                           withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      SzEntityPathResponse response = this.invokeServerViaHttp(
          GET, uriText, SzEntityPathResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      this.validateEntityPathResponse(
          testInfo,
          response,
          GET,
          uriText,
          fromIdentifer,
          toIdentifier,
          (maxDegrees != null ? maxDegrees : DEFAULT_PATH_DEGREES),
          avoidParamIds,
          avoidListIds,
          forbidAvoided,
          sourcesParam,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          withRaw,
          expectedPathLength,
          expectedPath,
          after - before);
    });
  }


  @ParameterizedTest
  @MethodSource("getEntityPathParameters")
  public void getPathByEntityIdViaJavaClientTest(
      SzRecordId              fromRecordId,
      SzRecordId              toRecordId,
      Integer                 maxDegrees,
      Collection<SzRecordId>  avoidParam,
      Collection<SzRecordId>  avoidList,
      Boolean                 forbidAvoided,
      List<String>            sourcesParam,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathLength,
      List<SzRecordId>        expectedPath)
  {
    this.performTest(() -> {
      String testInfo = "fromRecord=[ " + fromRecordId
          + " ], toRecord=[ " + toRecordId
          + " ], maxDegrees=[ " + maxDegrees
          + " ], avoidParam=[ " + avoidParam
          + " ], avoidList=[ " + avoidList
          + " ], forbidAvoided=[ " + forbidAvoided
          + " ], sources=[ " + sourcesParam
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifier fromIdentifer
          = this.normalizeIdentifier(fromRecordId,true);

      SzEntityIdentifier toIdentifier
          = this.normalizeIdentifier(toRecordId,true);

      SzEntityIdentifiers avoidParamIds
          = this.normalizeIdentifiers(avoidParam,true);

      SzEntityIdentifiers avoidListIds
          = this.normalizeIdentifiers(avoidList,true);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-paths");

      buildPathQueryString(sb,
                           fromIdentifer,
                           toIdentifier,
                           maxDegrees,
                           avoidParamIds,
                           avoidListIds,
                           forbidAvoided,
                           sourcesParam,
                           forceMinimal,
                           detailLevel,
                           featureMode,
                           withFeatureStats,
                           withInternalFeatures,
                           withRaw);

      com.senzing.gen.api.model.SzDetailLevel clientDetailLevel
          = (detailLevel == null)
          ? null
          : com.senzing.gen.api.model.SzDetailLevel.valueOf(
              detailLevel.toString());

      com.senzing.gen.api.model.SzFeatureMode clientFeatureMode
          = (featureMode == null)
          ? null
          : com.senzing.gen.api.model.SzFeatureMode.valueOf(
              featureMode.toString());

      String uriText = this.formatServerUri(sb.toString());
      List<com.senzing.gen.api.model.SzEntityIdentifier> clientAvoidIds
          = this.toClientIdList(avoidParam, true);

      com.senzing.gen.api.model.SzEntityIdentifiers clientAvoidList
          = this.toClientIds(avoidList,  true);

      com.senzing.gen.api.model.SzEntityIdentifier clientFrom
          = this.toClientId(fromRecordId, true);

      com.senzing.gen.api.model.SzEntityIdentifier clientTo
          = this.toClientId(toRecordId, true);

      long before = System.nanoTime();

      com.senzing.gen.api.model.SzEntityPathResponse clientResponse
          = this.entityGraphApi.findEntityPath(clientFrom,
                                               clientTo,
                                               maxDegrees,
                                               clientAvoidIds,
                                               clientAvoidList,
                                               forbidAvoided,
                                               sourcesParam,
                                               clientDetailLevel,
                                               clientFeatureMode,
                                               withFeatureStats,
                                               withInternalFeatures,
                                               forceMinimal,
                                               withRaw);
      long after = System.nanoTime();

      SzEntityPathResponse response
          = jsonCopy(clientResponse, SzEntityPathResponse.class);

      this.validateEntityPathResponse(
          testInfo,
          response,
          GET,
          uriText,
          fromIdentifer,
          toIdentifier,
          (maxDegrees != null ? maxDegrees : DEFAULT_PATH_DEGREES),
          avoidParamIds,
          avoidListIds,
          forbidAvoided,
          sourcesParam,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          withRaw,
          expectedPathLength,
          expectedPath,
          after - before);
    });
  }



  public void validateEntityPathResponse(
      String                testInfo,
      SzEntityPathResponse  response,
      SzHttpMethod          httpMethod,
      String                selfLink,
      SzEntityIdentifier    fromIdentifer,
      SzEntityIdentifier    toIdentifier,
      Integer               maxDegrees,
      SzEntityIdentifiers   avoidParam,
      SzEntityIdentifiers   avoidList,
      Boolean               forbidAvoided,
      List<String>          sourcesParam,
      Boolean               forceMinimal,
      SzDetailLevel         detailLevel,
      SzFeatureMode         featureMode,
      boolean               withFeatureStats,
      boolean               withInternalFeatures,
      Boolean               withRaw,
      Integer               expectedPathLength,
      List<SzRecordId>      expectedPath,
      long                  maxDuration)
  {
    validateBasics(testInfo, response, httpMethod, selfLink, maxDuration);

    SzEntityPathData pathData = response.getData();

    assertNotNull(pathData, "Response path data is null: " + testInfo);

    SzEntityPath entityPath = pathData.getEntityPath();

    assertNotNull(entityPath, "Entity path is null: " + testInfo);

    List<SzEntityData> entities = pathData.getEntities();

    assertNotNull(entities, "Entity list from path is null: " + testInfo);

    Long        fromEntityId    = this.asEntityId(fromIdentifer);
    Long        toEntityId      = this.asEntityId(toIdentifier);
    List<Long>  avoidParamIds   = this.asEntityIds(avoidParam);
    List<Long>  avoidListIds    = this.asEntityIds(avoidList);
    List<Long>  expectedPathIds = this.asEntityIds(expectedPath);

    assertEquals(fromEntityId, entityPath.getStartEntityId(),
                 "Unexpected path start point: " + testInfo);
    assertEquals(toEntityId, entityPath.getEndEntityId(),
                 "Unexpected path end point: " + testInfo);

    if (avoidParamIds != null && forbidAvoided != null && forbidAvoided) {
      for (Long entityId: entityPath.getEntityIds()) {
        if (avoidParamIds.contains(entityId)) {
          fail("Entity from avoidParam (" + entityId
                   + ") in path despite being forbidden: " + testInfo);
        }
      }
    }
    if (avoidListIds != null && forbidAvoided != null && forbidAvoided) {
      for (Long entityId: entityPath.getEntityIds()) {
        if (avoidListIds.contains(entityId)) {
          fail("Entity from avoidList (" + entityId
                   + ") in path despite being forbidden: " + testInfo);
        }
      }
    }
    Map<Long, SzResolvedEntity> entityMap = new LinkedHashMap<>();
    entities.forEach(entityData -> {
      SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();
      entityMap.put(resolvedEntity.getEntityId(), resolvedEntity);
    });

    if (sourcesParam != null && sourcesParam.size() > 0) {
      boolean sourcesSatisifed = false;
      for (Long entityId : entityPath.getEntityIds()) {
        if (entityId.equals(entityPath.getStartEntityId())) continue;
        if (entityId.equals(entityPath.getEndEntityId())) continue;
        SzResolvedEntity entity = entityMap.get(entityId);
        for (SzDataSourceRecordSummary summary : entity.getRecordSummaries()) {
          if (sourcesParam.contains(summary.getDataSource())) {
            sourcesSatisifed = true;
            break;
          }
        }
        if (sourcesSatisifed) break;
      }
      if (!sourcesSatisifed) {
        fail("Entity path does not contain required data sources: " + testInfo);
      }
    }

    if (expectedPathLength != null && expectedPathLength <= 0) {
      // expect that no path was found
      assertEquals(0, entityPath.getEntityIds().size(),
                   "Path unexpectedly found between entities: "
                       + testInfo);
    } else if (expectedPathLength != null) {
      // expect the path to be of a certain length
      String unexpectedPathMsg = this.formatUnexpectedPathMessage(
          expectedPath, entityPath.getEntityIds(), entityMap);
      assertEquals(expectedPathLength, entityPath.getEntityIds().size(),
                   "Path found of unexpected length: " + testInfo
                   + unexpectedPathMsg);
    }

    if (maxDegrees != null && maxDegrees < (entityPath.getEntityIds().size()-1))
    {
      String unexpectedPathMsg = this.formatUnexpectedPathMessage(
          expectedPath, entityPath.getEntityIds(), entityMap);
      fail("Entity path exceeds the maximum number of degrees of separation: "
           + testInfo + unexpectedPathMsg);
    }
    if (expectedPathIds != null) {
      if (!expectedPathIds.equals(entityPath.getEntityIds())) {
        String unexpectedPathMsg = this.formatUnexpectedPathMessage(
            expectedPath, entityPath.getEntityIds(), entityMap);
        fail("Path found does not match expected paths (" + testInfo + ")"
              + unexpectedPathMsg);
      }
    }

    for (SzEntityData entityData : entities) {
      SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();
      List<SzRelatedEntity> relatedEntities = entityData.getRelatedEntities();

      validateEntity(testInfo,
                     resolvedEntity,
                     relatedEntities,
                     forceMinimal,
                     detailLevel,
                     featureMode,
                     withFeatureStats,
                     withInternalFeatures,
                     null,
                     null,
                     false,
                     null,
                     true,
                     null,
                     null,
                     null,
                     null,
                     null);
    }

    if (withRaw != null && withRaw) {
      validateRawDataMap(testInfo,
                         response.getRawData(),
                         true,
                         "ENTITY_PATHS", "ENTITIES");

      Object rawPaths = ((Map) response.getRawData()).get("ENTITY_PATHS");

      validateRawDataMapArray(testInfo,
                              rawPaths,
                              true,
                              "START_ENTITY_ID",
                              "END_ENTITY_ID",
                              "ENTITIES");

      Object rawEntities = ((Map) response.getRawData()).get("ENTITIES");

      validateRawDataMapArray(testInfo,
                              rawEntities,
                              true,
                              "RESOLVED_ENTITY",
                              "RELATED_ENTITIES");

      for (Object entity : ((Collection) rawEntities)) {
        validateRawDataMap(
            testInfo,
            ((Map) entity).get("RESOLVED_ENTITY"),
            false,
            rawEntityKeys(forceMinimal, detailLevel, featureMode));
      }
    }
  }

  private String formatUnexpectedPathMessage(
      List<SzRecordId>            expectedPath,
      List<Long>                  actualPath,
      Map<Long, SzResolvedEntity> entityMap)
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    pw.println();
    if (expectedPath != null) {
      pw.println("EXPECTED PATH: ");
      expectedPath.forEach(recordId -> {
        Long entityId = this.getEntityIdForRecordId(recordId);
        pw.println("    " + entityId + " / " + recordId.getDataSourceCode()
                       + ":" + recordId.getRecordId());
      });
      pw.flush();
    }
    if (actualPath != null) {
      pw.println("ACTUAL PATH: ");
      actualPath.forEach(entityId -> {
        SzResolvedEntity entity = entityMap.get(entityId);
        pw.print("    " + entityId);
        entity.getRecords().forEach(record -> {
          pw.print(" / ");
          pw.print(record.getDataSource() + ":" + record.getRecordId());
        });
        pw.println();
        pw.flush();
      });
    }
    pw.flush();
    return sw.toString();
  }


  private static List<List> networkArgs(
      Collection<SzRecordId>  recordIds,
      Integer                 maxDegrees,
      Integer                 buildOut,
      Integer                 maxEntities,
      Integer                 expectedPathCount,
      List<List<SzRecordId>>  expectedPaths,
      Set<SzRecordId>         expectedEntities)
  {
    List<List> result = new LinkedList<>();

    // creates variants of the specified avoid parameter to exercise both
    // of the parameters that deal with avoiding entities
    List<List> recordVariants = new LinkedList<>();
    if (recordIds.size() == 1) {
      recordVariants.add(list(recordIds, null));
      recordVariants.add(list(null, recordIds));

    } else if (recordIds.size() > 1) {
      recordVariants.add(list(recordIds, null));
      recordVariants.add(list(null, recordIds));

      int recordCount = recordIds.size();
      Set<SzRecordId> records1 = new LinkedHashSet<>();
      Set<SzRecordId> records2 = new LinkedHashSet<>();
      for (SzRecordId recordId : recordIds) {
        if ((recordCount % 2) == 0) {
          records1.add(recordId);
        } else {
          records2.add(recordId);
        }
        recordCount--;
      }
      recordVariants.add(list(records1, records2));
      recordVariants.add(list(records2, records1));
    }

    // create variants on the max degrees parameter to test default values
    List<Integer> degreesVariants = new LinkedList<>();
    if (maxDegrees == null || maxDegrees == DEFAULT_NETWORK_DEGREES) {
      degreesVariants.add(null);
      degreesVariants.add(DEFAULT_NETWORK_DEGREES);
    } else {
      degreesVariants.add(maxDegrees);
    }

    // create variants on the build-out parameter to test default values
    List<Integer> buildOutVariants = new LinkedList<>();
    if (buildOut == null || buildOut == DEFAULT_BUILD_OUT) {
      buildOutVariants.add(null);
      buildOutVariants.add(DEFAULT_BUILD_OUT);
    } else {
      buildOutVariants.add(buildOut);
    }

    // create variants on the max entities parameter to test default values
    List<Integer> maxEntitiesVariants = new LinkedList<>();
    if (maxEntities == null || maxEntities == DEFAULT_MAX_ENTITIES) {
      maxEntitiesVariants.add(null);
      maxEntitiesVariants.add(DEFAULT_MAX_ENTITIES);
    } else {
      maxEntitiesVariants.add(maxEntities);
    }
    int maxEntityCount = (maxEntities != null)
                       ? maxEntities : DEFAULT_MAX_ENTITIES;
    if (expectedEntities != null) {
      int[] addlVariants = {
          expectedEntities.size(),
          expectedEntities.size() - 1,
          expectedEntities.size() / 2,
          expectedEntities.size() * 2
      };
      for (int variant : addlVariants) {
        if (!maxEntitiesVariants.contains(variant)) {
          maxEntitiesVariants.add(variant);
        }
      }
    }

    recordVariants.forEach(recordVariant -> {
      degreesVariants.forEach(degreeVariant -> {
        buildOutVariants.forEach(buildOutVariant -> {
          maxEntitiesVariants.forEach(maxEntitiesVariant -> {
            result.add(
                list(recordVariant.get(0),
                     recordVariant.get(1),
                     degreeVariant,
                     buildOutVariant,
                     maxEntitiesVariant,
                     null,  // forceMinimal (5)
                     null,  // detailLevel (6)
                     null,  // featureMode (7)
                     null,  // withFeatureStats (8)
                     null,  // withInternalFeatures (9)
                     null,  // withRaw (10)
                     expectedPathCount,
                     expectedPaths,
                     expectedEntities));
          });
        });
      });
    });

    return result;
  }

  private List<Arguments> getEntityNetworkParameters() {

    List<List> baseArgs = new LinkedList<>();

    baseArgs.addAll(networkArgs(
        set(ABC123), 1, 0, null,
        0, list(), set(ABC123)));

    baseArgs.addAll(networkArgs(
        set(ABC123), 1, 1, null,
        0, list(), set(ABC123,DEF456,MNO345)));

    baseArgs.addAll(networkArgs(
        set(ABC123,JKL456), 1, 0, null,
        1, list(list(null, ABC123, JKL456)),
        set(ABC123,JKL456)));

    baseArgs.addAll(networkArgs(
        set(ABC123,JKL456), 3, 0, null,
        1,
        list(list(ABC123,MNO345,DEF890,JKL456)),
        set(ABC123,MNO345,DEF890,JKL456)));

    baseArgs.addAll(networkArgs(
        set(ABC123,ABC567,JKL456), 3, 0, null,
        3,
        list(list(ABC123,MNO345,DEF890,JKL456),
             list(ABC123,DEF456,PQR678,ABC567),
             list(null, ABC567, JKL456)),
        set(ABC123,MNO345,DEF890,JKL456,DEF456,PQR678,ABC567)));

    baseArgs.addAll(networkArgs(
        set(ABC567,GHI123,MNO345), 0, 1, null,
        3,
        list(list(null, ABC567, GHI123),
             list(null, ABC567, MNO345),
             list(null, GHI123, MNO345)),
        set(ABC567,GHI123,MNO345,PQR678,XYZ234,STU901,ABC123,DEF890)));

    baseArgs.addAll(networkArgs(
        set(ABC123,DEF456), 0, 1, null,
        1,
        list(list(null, ABC123, DEF456)),
        set(ABC123,DEF456,MNO345,GHI789,PQR678)));

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

    // convert to an array list for random access
    baseArgs = new ArrayList<>(baseArgs);

    Random prng = new Random(RANDOM_SEED);
    List<List<Boolean>> booleanCombos = getBooleanVariants(4);
    Collections.shuffle(booleanCombos, prng);
    Iterator<List<Boolean>> booleansIter = circularIterator(booleanCombos);

    List<List> optionCombos = generateCombinations(detailLevels, featureModes);
    Collections.shuffle(optionCombos, prng);
    Iterator<List> optionsIter = circularIterator(optionCombos);

    int loopCount
        = Math.max(booleanCombos.size(), optionCombos.size()) * 15
        / baseArgs.size();

    int totalCount = loopCount * baseArgs.size();
    List<Arguments> result = new ArrayList<>(totalCount);

    baseArgs.forEach(baseArgList -> {
      for (int index = 0; index < loopCount; index++) {
        List          optsList      = optionsIter.next();
        List<Boolean> booleansList  = booleansIter.next();

        SzDetailLevel detailLevel           = (SzDetailLevel) optsList.get(0);
        SzFeatureMode featureMode           = (SzFeatureMode) optsList.get(1);
        Boolean       forceMinimal          = booleansList.get(0);
        Boolean       withFeatureStats      = booleansList.get(1);
        Boolean       withInternalFeatures  = booleansList.get(2);
        Boolean       withRaw               = booleansList.get(3);

        Object[] argArray = baseArgList.toArray();
        argArray[5] = forceMinimal;
        argArray[6] = detailLevel;
        argArray[7] = featureMode;
        argArray[8] = withFeatureStats;
        argArray[9] = withInternalFeatures;
        argArray[10] = withRaw;

        result.add(arguments(argArray));
      }
    });

    return result;

  }

  private StringBuilder buildNetworkQueryString(
      StringBuilder         sb,
      SzEntityIdentifiers   entitiesParam,
      SzEntityIdentifiers   entityList,
      Integer               maxDegrees,
      Integer               buildOut,
      Integer               maxEntities,
      Boolean               forceMinimal,
      SzDetailLevel         detailLevel,
      SzFeatureMode         featureMode,
      Boolean               withFeatureStats,
      Boolean               withInternalFeatures,
      Boolean               withRaw)
  {
    try {
      String prefix = "?";
      if (entitiesParam != null && !entitiesParam.isEmpty()) {
        for (SzEntityIdentifier identifier : entitiesParam.getIdentifiers()) {
          sb.append(prefix).append("e=").append(
              URLEncoder.encode(identifier.toString(), "UTF-8"));
          prefix = "&";
        }
      }
      if (entityList != null && !entityList.isEmpty()) {
        sb.append(prefix).append("entities=").append(
            URLEncoder.encode(entityList.toString(), "UTF-8"));
      }
      if (maxDegrees != null) {
        sb.append("&maxDegrees=").append(maxDegrees);
      }
      if (buildOut != null) {
        sb.append("&buildOut=").append(buildOut);
      }
      if (maxEntities != null) {
        sb.append("&maxEntities=").append(maxEntities);
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
      if (withRaw != null) {
        sb.append("&withRaw=").append(withRaw);
      }
      return sb;

    } catch (UnsupportedEncodingException cannotHappen) {
      throw new RuntimeException(cannotHappen);
    }
  }

  @ParameterizedTest
  @MethodSource("getEntityNetworkParameters")
  public void getNetworkByRecordIdTest(
      Collection<SzRecordId>  entityParam,
      Collection<SzRecordId>  entityList,
      Integer                 maxDegrees,
      Integer                 buildOut,
      Integer                 maxEntities,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathCount,
      List<List<SzRecordId>>  expectedPaths,
      Set<SzRecordId>         expectedEntities)
  {
    this.performTest(() -> {
      String testInfo = "entityParam=[ " + entityParam
          + " ], entityList=[ " + entityList
          + " ], maxDegrees=[ " + maxDegrees
          + " ], buildOut=[ " + buildOut
          + " ], maxEntities=[ " + maxEntities
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifiers entityParamIds
          = this.normalizeIdentifiers(entityParam,false);

      SzEntityIdentifiers entityListIds
          = this.normalizeIdentifiers(entityList,false);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-networks");

      buildNetworkQueryString(sb,
                              entityParamIds,
                              entityListIds,
                              maxDegrees,
                              buildOut,
                              maxEntities,
                              forceMinimal,
                              detailLevel,
                              featureMode,
                              withFeatureStats,
                              withInternalFeatures,
                              withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzEntityNetworkResponse response
          = this.entityGraphServices.getEntityNetwork(
          formatIdentifierParam(entityParamIds),
          formatIdentifierList(entityListIds),
          (maxDegrees == null   ? DEFAULT_NETWORK_DEGREES : maxDegrees),
          (buildOut == null     ? DEFAULT_BUILD_OUT : buildOut),
          (maxEntities == null  ? DEFAULT_MAX_ENTITIES : maxEntities),
          (forceMinimal == null ? false : forceMinimal),
          (detailLevel == null ? VERBOSE : detailLevel),
          (featureMode == null  ? WITH_DUPLICATES : featureMode),
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          (withRaw == null      ? false : withRaw),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      this.validateEntityNetworkResponse(
          testInfo,
          response,
          GET,
          uriText,
          entityParamIds,
          entityListIds,
          (maxDegrees != null) ? maxDegrees : DEFAULT_NETWORK_DEGREES,
          (buildOut != null) ? buildOut : DEFAULT_BUILD_OUT,
          (maxEntities != null) ? maxEntities : DEFAULT_MAX_ENTITIES,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null) ? false : withFeatureStats,
          (withInternalFeatures == null) ? false : withInternalFeatures,
          withRaw,
          expectedPathCount,
          expectedPaths,
          expectedEntities,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityNetworkParameters")
  public void getNetworkByRecordIdViaHttpTest(
      Collection<SzRecordId>  entityParam,
      Collection<SzRecordId>  entityList,
      Integer                 maxDegrees,
      Integer                 buildOut,
      Integer                 maxEntities,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathCount,
      List<List<SzRecordId>>  expectedPaths,
      Set<SzRecordId>         expectedEntities)
  {
    this.performTest(() -> {
      String testInfo = "entityParam=[ " + entityParam
          + " ], entityList=[ " + entityList
          + " ], maxDegrees=[ " + maxDegrees
          + " ], buildOut=[ " + buildOut
          + " ], maxEntities=[ " + maxEntities
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifiers entityParamIds
          = this.normalizeIdentifiers(entityParam,false);

      SzEntityIdentifiers entityListIds
          = this.normalizeIdentifiers(entityList,false);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-networks");

      buildNetworkQueryString(sb,
                              entityParamIds,
                              entityListIds,
                              maxDegrees,
                              buildOut,
                              maxEntities,
                              forceMinimal,
                              detailLevel,
                              featureMode,
                              withFeatureStats,
                              withInternalFeatures,
                              withRaw);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();

      SzEntityNetworkResponse response = this.invokeServerViaHttp(
          GET, uriText, SzEntityNetworkResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      this.validateEntityNetworkResponse(
          testInfo,
          response,
          GET,
          uriText,
          entityParamIds,
          entityListIds,
          (maxDegrees != null) ? maxDegrees : DEFAULT_NETWORK_DEGREES,
          (buildOut != null) ? buildOut : DEFAULT_BUILD_OUT,
          (maxEntities != null) ? maxEntities : DEFAULT_MAX_ENTITIES,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null) ? false : withFeatureStats,
          (withInternalFeatures == null) ? false : withInternalFeatures,
          withRaw,
          expectedPathCount,
          expectedPaths,
          expectedEntities,
          after - before);
    });
  }

  protected static class ClientRecordId
      extends com.senzing.gen.api.model.SzEntityIdentifier
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
      extends com.senzing.gen.api.model.SzEntityIdentifiers
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

  protected static class ClientEntityIds
      extends com.senzing.gen.api.model.SzEntityIdentifiers
  {
    private List<Long> entityIds;

    public ClientEntityIds(List<Long> entityIds) {
      this.entityIds = entityIds;
    }

    @JsonValue
    public List<Long> getValue() {
      return this.entityIds;
    }

    public String toString() {
      try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.entityIds);

      } catch (JsonProcessingException exception) {
        throw new RuntimeException(exception);
      }
    }
  }

  /**
   * Implements {@link com.senzing.gen.api.model.SzEntityIdentifier}
   */
  protected static class ClientEntityId
      extends com.senzing.gen.api.model.SzEntityIdentifier
  {
    private Long entityId;

    public ClientEntityId(Long entityId) {
      this.entityId = entityId;
    }

    public ClientEntityId(SzEntityId entityId) {
      this.entityId = entityId.getValue();
    }

    @JsonValue
    public Long getValue() {
      return this.entityId;
    }

    public String toString() {
      try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.entityId);

      } catch (JsonProcessingException exception) {
        throw new RuntimeException(exception);
      }
    }
  }

  protected com.senzing.gen.api.model.SzEntityIdentifier toClientId(
      SzRecordId recordId, boolean asEntityId)
  {
    if (recordId == null) return null;
    SzEntityIdentifier id = this.normalizeIdentifier(recordId, asEntityId);

    Class<? extends com.senzing.gen.api.model.SzEntityIdentifier> target;

    if (asEntityId) {
      SzEntityId entityId = (SzEntityId) id;
      return new ClientEntityId(entityId);
    } else {
      return new ClientRecordId(recordId);
    }
  }

  protected com.senzing.gen.api.model.SzEntityIdentifiers toClientIds(
      Collection<SzRecordId> recordIds, boolean asEntityId)
  {
    if (recordIds == null) return null;
    SzEntityIdentifiers ids = this.normalizeIdentifiers(recordIds, asEntityId);

    if (asEntityId) {
      List<Long> entityIds = new ArrayList<>(ids.getCount());
      for (SzEntityIdentifier id: ids.getIdentifiers()) {
        entityIds.add(((SzEntityId) id).getValue());
      }
      return new ClientEntityIds(entityIds);
    } else {
      List<com.senzing.gen.api.model.SzRecordId> clientIds
          = new ArrayList<>(ids.getCount());
      for (SzEntityIdentifier id: ids.getIdentifiers()) {
        clientIds.add(((ClientRecordId)
            this.toClientId((SzRecordId) id, false)).getValue());
      }
      return new ClientRecordIds(clientIds);
    }
  }

  protected List<com.senzing.gen.api.model.SzEntityIdentifier>
    toClientIdList(Collection<SzRecordId> recordIds, boolean asEntityId)
  {
    if (recordIds == null) return null;
    SzEntityIdentifiers ids = this.normalizeIdentifiers(recordIds, asEntityId);

    if (asEntityId) {
      List<com.senzing.gen.api.model.SzEntityIdentifier> entityIds = new ArrayList<>(ids.getCount());
      for (SzEntityIdentifier id: ids.getIdentifiers()) {
        entityIds.add(new ClientEntityId(((SzEntityId) id).getValue()));
      }
      return entityIds;

    } else {
      List<com.senzing.gen.api.model.SzEntityIdentifier> clientIds
          = new ArrayList<>(ids.getCount());
      for (SzEntityIdentifier id: ids.getIdentifiers()) {
        ClientRecordId clientId = (ClientRecordId)
            this.toClientId((SzRecordId) id, false);
        clientIds.add(clientId);
      }
      return clientIds;
    }
  }

  @ParameterizedTest
  @MethodSource("getEntityNetworkParameters")
  public void getNetworkByRecordIdViaJavaClientTest(
      Collection<SzRecordId>  entityParam,
      Collection<SzRecordId>  entityList,
      Integer                 maxDegrees,
      Integer                 buildOut,
      Integer                 maxEntities,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathCount,
      List<List<SzRecordId>>  expectedPaths,
      Set<SzRecordId>         expectedEntities)
  {
    this.performTest(() -> {
      String testInfo = "entityParam=[ " + entityParam
          + " ], entityList=[ " + entityList
          + " ], maxDegrees=[ " + maxDegrees
          + " ], buildOut=[ " + buildOut
          + " ], maxEntities=[ " + maxEntities
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifiers entityParamIds
          = this.normalizeIdentifiers(entityParam,false);

      SzEntityIdentifiers entityListIds
          = this.normalizeIdentifiers(entityList,false);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-networks");

      buildNetworkQueryString(sb,
                              entityParamIds,
                              entityListIds,
                              maxDegrees,
                              buildOut,
                              maxEntities,
                              forceMinimal,
                              detailLevel,
                              featureMode,
                              withFeatureStats,
                              withInternalFeatures,
                              withRaw);

      String uriText = this.formatServerUri(sb.toString());

      com.senzing.gen.api.model.SzDetailLevel clientDetailLevel
          = (detailLevel == null)
          ? null
          : com.senzing.gen.api.model.SzDetailLevel.valueOf(
              detailLevel.toString());

      com.senzing.gen.api.model.SzFeatureMode clientFeatureMode
          = (featureMode == null)
          ? null
          : com.senzing.gen.api.model.SzFeatureMode.valueOf(
              featureMode.toString());

      List<com.senzing.gen.api.model.SzEntityIdentifier> clientParamIds
          = this.toClientIdList(entityParam, false);

      com.senzing.gen.api.model.SzEntityIdentifiers clientEntityList
          = this.toClientIds(entityList, false);

      long before = System.nanoTime();

      com.senzing.gen.api.model.SzEntityNetworkResponse clientResponse
        = this.entityGraphApi.findEntityNetwork(clientParamIds,
                                                clientEntityList,
                                                maxDegrees,
                                                buildOut,
                                                maxEntities,
                                                clientDetailLevel,
                                                clientFeatureMode,
                                                withFeatureStats,
                                                withInternalFeatures,
                                                forceMinimal,
                                                withRaw);

      long after = System.nanoTime();

      SzEntityNetworkResponse response
          = jsonCopy(clientResponse, SzEntityNetworkResponse.class);

      this.validateEntityNetworkResponse(
          testInfo,
          response,
          GET,
          uriText,
          entityParamIds,
          entityListIds,
          (maxDegrees != null) ? maxDegrees : DEFAULT_NETWORK_DEGREES,
          (buildOut != null) ? buildOut : DEFAULT_BUILD_OUT,
          (maxEntities != null) ? maxEntities : DEFAULT_MAX_ENTITIES,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null) ? false : withFeatureStats,
          (withInternalFeatures == null) ? false : withInternalFeatures,
          withRaw,
          expectedPathCount,
          expectedPaths,
          expectedEntities,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityNetworkParameters")
  public void getNetworkByEntityIdTest(
      Collection<SzRecordId>  entityParam,
      Collection<SzRecordId>  entityList,
      Integer                 maxDegrees,
      Integer                 buildOut,
      Integer                 maxEntities,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathCount,
      List<List<SzRecordId>>  expectedPaths,
      Set<SzRecordId>         expectedEntities)
  {
    this.performTest(() -> {
      String testInfo = "entityParam=[ " + entityParam
          + " ], entityList=[ " + entityList
          + " ], maxDegrees=[ " + maxDegrees
          + " ], buildOut=[ " + buildOut
          + " ], maxEntities=[ " + maxEntities
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifiers entityParamIds
          = this.normalizeIdentifiers(entityParam,true);

      SzEntityIdentifiers entityListIds
          = this.normalizeIdentifiers(entityList,true);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-networks");

      buildNetworkQueryString(sb,
                              entityParamIds,
                              entityListIds,
                              maxDegrees,
                              buildOut,
                              maxEntities,
                              forceMinimal,
                              detailLevel,
                              featureMode,
                              withFeatureStats,
                              withInternalFeatures,
                              withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzEntityNetworkResponse response
          = this.entityGraphServices.getEntityNetwork(
          formatIdentifierParam(entityParamIds),
          formatIdentifierList(entityListIds),
          (maxDegrees == null   ? DEFAULT_NETWORK_DEGREES : maxDegrees),
          (buildOut == null     ? DEFAULT_BUILD_OUT : buildOut),
          (maxEntities == null  ? DEFAULT_MAX_ENTITIES : maxEntities),
          (forceMinimal == null ? false : forceMinimal),
          (detailLevel == null ? VERBOSE : detailLevel),
          (featureMode == null  ? WITH_DUPLICATES : featureMode),
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          (withRaw == null      ? false : withRaw),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      this.validateEntityNetworkResponse(
          testInfo,
          response,
          GET,
          uriText,
          entityParamIds,
          entityListIds,
          (maxDegrees != null) ? maxDegrees : DEFAULT_NETWORK_DEGREES,
          (buildOut != null) ? buildOut : DEFAULT_BUILD_OUT,
          (maxEntities != null) ? maxEntities : DEFAULT_MAX_ENTITIES,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null) ? false : withFeatureStats,
          (withInternalFeatures == null) ? false : withInternalFeatures,
          withRaw,
          expectedPathCount,
          expectedPaths,
          expectedEntities,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityNetworkParameters")
  public void getNetworkByEntityIdViaHttpTest(
      Collection<SzRecordId>  entityParam,
      Collection<SzRecordId>  entityList,
      Integer                 maxDegrees,
      Integer                 buildOut,
      Integer                 maxEntities,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathCount,
      List<List<SzRecordId>>  expectedPaths,
      Set<SzRecordId>         expectedEntities)
  {
    this.performTest(() -> {
      String testInfo = "entityParam=[ " + entityParam
          + " ], entityList=[ " + entityList
          + " ], maxDegrees=[ " + maxDegrees
          + " ], buildOut=[ " + buildOut
          + " ], maxEntities=[ " + maxEntities
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifiers entityParamIds
          = this.normalizeIdentifiers(entityParam,true);

      SzEntityIdentifiers entityListIds
          = this.normalizeIdentifiers(entityList,true);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-networks");

      buildNetworkQueryString(sb,
                              entityParamIds,
                              entityListIds,
                              maxDegrees,
                              buildOut,
                              maxEntities,
                              forceMinimal,
                              detailLevel,
                              featureMode,
                              withFeatureStats,
                              withInternalFeatures,
                              withRaw);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();

      SzEntityNetworkResponse response = this.invokeServerViaHttp(
          GET, uriText, SzEntityNetworkResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      this.validateEntityNetworkResponse(
          testInfo,
          response,
          GET,
          uriText,
          entityParamIds,
          entityListIds,
          (maxDegrees != null) ? maxDegrees : DEFAULT_NETWORK_DEGREES,
          (buildOut != null) ? buildOut : DEFAULT_BUILD_OUT,
          (maxEntities != null) ? maxEntities : DEFAULT_MAX_ENTITIES,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          withRaw,
          expectedPathCount,
          expectedPaths,
          expectedEntities,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getEntityNetworkParameters")
  public void getNetworkByEntityIdViaJavaClientTest(
      Collection<SzRecordId>  entityParam,
      Collection<SzRecordId>  entityList,
      Integer                 maxDegrees,
      Integer                 buildOut,
      Integer                 maxEntities,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      Boolean                 withFeatureStats,
      Boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathCount,
      List<List<SzRecordId>>  expectedPaths,
      Set<SzRecordId>         expectedEntities)
  {
    this.performTest(() -> {
      String testInfo = "entityParam=[ " + entityParam
          + " ], entityList=[ " + entityList
          + " ], maxDegrees=[ " + maxDegrees
          + " ], buildOut=[ " + buildOut
          + " ], maxEntities=[ " + maxEntities
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRaw=[ " + withRaw + " ]";

      SzEntityIdentifiers entityParamIds
          = this.normalizeIdentifiers(entityParam,true);

      SzEntityIdentifiers entityListIds
          = this.normalizeIdentifiers(entityList,true);

      StringBuilder sb = new StringBuilder();
      sb.append("entity-networks");

      buildNetworkQueryString(sb,
                              entityParamIds,
                              entityListIds,
                              maxDegrees,
                              buildOut,
                              maxEntities,
                              forceMinimal,
                              detailLevel,
                              featureMode,
                              withFeatureStats,
                              withInternalFeatures,
                              withRaw);

      String uriText = this.formatServerUri(sb.toString());

      com.senzing.gen.api.model.SzDetailLevel clientDetailLevel
          = (detailLevel == null)
          ? null
          : com.senzing.gen.api.model.SzDetailLevel.valueOf(
              detailLevel.toString());

      com.senzing.gen.api.model.SzFeatureMode clientFeatureMode
          = (featureMode == null)
          ? null
          : com.senzing.gen.api.model.SzFeatureMode.valueOf(
              featureMode.toString());

      List<com.senzing.gen.api.model.SzEntityIdentifier> clientParamIds
          = this.toClientIdList(entityParam, true);

      com.senzing.gen.api.model.SzEntityIdentifiers clientEntityList
          = this.toClientIds(entityList, true);

      long before = System.nanoTime();

      com.senzing.gen.api.model.SzEntityNetworkResponse clientResponse
          = this.entityGraphApi.findEntityNetwork(clientParamIds,
                                                  clientEntityList,
                                                  maxDegrees,
                                                  buildOut,
                                                  maxEntities,
                                                  clientDetailLevel,
                                                  clientFeatureMode,
                                                  withFeatureStats,
                                                  withInternalFeatures,
                                                  forceMinimal,
                                                  withRaw);

      long after = System.nanoTime();

      SzEntityNetworkResponse response
          = jsonCopy(clientResponse, SzEntityNetworkResponse.class);

      this.validateEntityNetworkResponse(
          testInfo,
          response,
          GET,
          uriText,
          entityParamIds,
          entityListIds,
          (maxDegrees != null) ? maxDegrees : DEFAULT_NETWORK_DEGREES,
          (buildOut != null) ? buildOut : DEFAULT_BUILD_OUT,
          (maxEntities != null) ? maxEntities : DEFAULT_MAX_ENTITIES,
          forceMinimal,
          detailLevel,
          featureMode,
          (withFeatureStats == null ? false : withFeatureStats),
          (withInternalFeatures == null ? false : withInternalFeatures),
          withRaw,
          expectedPathCount,
          expectedPaths,
          expectedEntities,
          after - before);
    });
  }

  private void validateEntityNetworkResponse(
      String                  testInfo,
      SzEntityNetworkResponse response,
      SzHttpMethod            httpMethod,
      String                  selfLink,
      SzEntityIdentifiers     entityParam,
      SzEntityIdentifiers     entityList,
      Integer                 maxDegrees,
      Integer                 buildOut,
      Integer                 maxEntities,
      Boolean                 forceMinimal,
      SzDetailLevel           detailLevel,
      SzFeatureMode           featureMode,
      boolean                 withFeatureStats,
      boolean                 withInternalFeatures,
      Boolean                 withRaw,
      Integer                 expectedPathCount,
      List<List<SzRecordId>>  expectedPaths,
      Set<SzRecordId>         expectedEntities,
      long                    maxDuration)
  {
    selfLink = this.formatServerUri(selfLink);

    // determine how many entities were requested
    Set<SzEntityIdentifier> entityIdentifiers = new LinkedHashSet<>();
    if (entityParam != null) {
      entityIdentifiers.addAll(entityParam.getIdentifiers());
    }
    if (entityList != null) {
      entityIdentifiers.addAll(entityList.getIdentifiers());
    }
    int entityCount = entityIdentifiers.size();

    validateBasics(testInfo, response, httpMethod, selfLink, maxDuration);

    SzEntityNetworkData networkData = response.getData();

    assertNotNull(networkData,
                  "Response network data is null: " + testInfo);

    List<SzEntityPath> entityPaths = networkData.getEntityPaths();

    assertNotNull(entityPaths, "Entity path list is null: " + testInfo);

    // remove the self-paths when only a single entity is requested
    if (entityCount == 1) {
      List<SzEntityPath> list = new ArrayList<>(entityPaths.size());
      for (SzEntityPath path : entityPaths) {
        if (path.getStartEntityId() == path.getEndEntityId()) {
          continue;
        }
        list.add(path);
      }
      entityPaths = list;
    }

    List<SzEntityData> entities = networkData.getEntities();

    assertNotNull(entities,
                  "Entity list from network is null: " + testInfo);

    List<Long>  entityParamIds  = this.asEntityIds(entityParam);
    List<Long>  entityListIds   = this.asEntityIds(entityList);

    Set<Long> pathEntityIds = new HashSet<>();
    for (SzEntityPath entityPath : entityPaths) {
      pathEntityIds.add(entityPath.getStartEntityId());
      pathEntityIds.add(entityPath.getEndEntityId());
      for (Long entityId : entityPath.getEntityIds()) {
        pathEntityIds.add(entityId);
      }
    }

    List<SzEntityPath>  expectedPathList  = null;
    IdentityHashMap<SzEntityPath, List<SzRecordId>> epLookup = null;
    if (expectedPaths != null) {
      epLookup = new IdentityHashMap<>();
      expectedPathList = new ArrayList<>(expectedPaths.size());
      for (List<SzRecordId> expectedPath : expectedPaths) {
        List<Long> entityIds = null;
        List<Long> pathIds = null;
        if (expectedPath.get(0) == null) {
          entityIds = this.asEntityIds(
              expectedPath.subList(1, expectedPath.size()));
          pathIds = emptyList();
        } else {
          entityIds = this.asEntityIds(expectedPath);
          pathIds = entityIds;
        }

        if (entityIds != null) {
          long entityId1 = entityIds.get(0).longValue();
          long entityId2 = entityIds.get(entityIds.size() - 1).longValue();
          long fromId = (entityId1 < entityId2) ? entityId1 : entityId2;
          long toId = (entityId1 < entityId2) ? entityId2 : entityId1;

          SzEntityPath entityPath = new SzEntityPathImpl(fromId, toId, pathIds);
          expectedPathList.add(entityPath);
          epLookup.put(entityPath, expectedPath);
        }
      }
    }

    Set<Long> allExpectedEntities = new LinkedHashSet<>();
    if (entityParamIds != null) allExpectedEntities.addAll(entityParamIds);
    if (entityListIds != null)  allExpectedEntities.addAll(entityListIds);

    for (long fromEntityId : allExpectedEntities) {
      for (long toEntityId : allExpectedEntities) {
        if (fromEntityId == toEntityId) continue;
        boolean found = false;
        for (SzEntityPath entityPath : entityPaths) {
          long start  = entityPath.getStartEntityId();
          long end    = entityPath.getEndEntityId();
          if (((start == fromEntityId) && (end == toEntityId))
              || ((start == toEntityId) && (end == fromEntityId)))
          {
            found = true;
            break;
          }
        }
        if (!found) {
          fail("Missing entity path between " + fromEntityId + " and "
               + toEntityId + ": " + entityPaths + " / " + testInfo);
        }

      }
    }

    List<Long> expectedEntityIds = this.asEntityIds(expectedEntities);
    if (expectedEntityIds != null) {
      allExpectedEntities.addAll(expectedEntityIds);

      for (SzEntityPath entityPath : entityPaths) {
        for (Long entityId : entityPath.getEntityIds()) {
          if (!expectedEntityIds.contains(entityId)) {
            fail("Unexpected entity found on entity (" + entityId
                 + ") path entity path (" + entityPath.getEntityIds()
                 + "): " + testInfo);
          }
        }
      }
    }

    Map<Long, SzResolvedEntity> entityMap = new LinkedHashMap<>();
    entities.forEach(entityData -> {
      SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();
      entityMap.put(resolvedEntity.getEntityId(), resolvedEntity);
    });

    // augment the path entity IDs for single entities with no path to others
    if (entityParamIds != null) pathEntityIds.addAll(entityParamIds);
    if (entityListIds != null) pathEntityIds.addAll(entityListIds);
    if (maxEntities != null
        && entityMap.size() > Math.max(maxEntities,pathEntityIds.size()))
    {
      fail("The number of entity details (" + entityMap.size()
           + ") exceeded the max entities (" + maxEntities
           +  " / " + pathEntityIds.size() + "): " + testInfo);
    }

    int maxEntityCount = (maxEntities != null)
        ? maxEntities : DEFAULT_MAX_ENTITIES;
    if (allExpectedEntities.size() < maxEntityCount) {
      for (Long entityId : allExpectedEntities) {
        if (!entityMap.containsKey(entityId)) {
          fail("Missing entity details for entity " + entityId + ": " + testInfo);
        }
      }
    } else if (expectedEntities != null) {
      int foundCount = 0;
      for (Long entityId: allExpectedEntities) {
        if (entityMap.containsKey(entityId)) foundCount++;
      }
      if (foundCount < maxEntityCount) {
        fail("Only found " + foundCount + " entity details for "
             + allExpectedEntities.size() + " expected entities with "
             + maxEntityCount + " max entities: " + testInfo);
      }
    }

    if (expectedPathCount != null) {
      assertEquals(expectedPathCount, entityPaths.size(),
                   "Unexpected number of paths found: "
                       + entityPaths + " / " + testInfo);
    }

    if (maxDegrees != null) {
      for (SzEntityPath entityPath : entityPaths) {
        if (maxDegrees < (entityPath.getEntityIds().size() - 1)) {
          String unexpectedPathMsg = this.formatUnexpectedPathMessage(
              null, entityPath.getEntityIds(), entityMap);
          fail("Entity path exceeds the maximum number of degrees of "
                   + "separation: " + testInfo + unexpectedPathMsg);
        }
      }
    }
    if (expectedPathList != null) {
      for (SzEntityPath expectedPath : expectedPathList) {
        boolean found = false;
        for (SzEntityPath entityPath : entityPaths) {
          if (expectedPath.equals(entityPath)) {
            found = true;
            break;
          }
        }
        if (!found) {
          String unexpectedPathMsg = this.formatUnexpectedPathMessage(
              epLookup.get(expectedPath), null, entityMap);
          fail("Expected path not found (" + testInfo + ")"
                   + unexpectedPathMsg + "\n\nexpectedPaths=[ "
                   + expectedPathList + " ]\n\nactualPaths=[ "
                   + entityPaths + " ]");
        }
      }
      for (SzEntityPath entityPath : entityPaths) {
        boolean found = false;
        for (SzEntityPath expectedPath : expectedPathList) {
          if (expectedPath.equals(entityPath)) {
            found = true;
            break;
          }
        }
        if (!found) {
          String unexpectedPathMsg = this.formatUnexpectedPathMessage(
              null, entityPath.getEntityIds(), entityMap);
          fail("Unexpected path found (" + testInfo + ")"
                   + unexpectedPathMsg + "\n\nexpectedPaths=[ "
                   + expectedPathList + " ]\n\nactualPaths=[ "
                   + entityPaths + " ]");
        }
      }
    }

    for (SzEntityData entityData : entities) {
      SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();
      List<SzRelatedEntity> relatedEntities = entityData.getRelatedEntities();

      validateEntity(testInfo,
                     resolvedEntity,
                     relatedEntities,
                     forceMinimal,
                     detailLevel,
                     featureMode,
                     withFeatureStats,
                     withInternalFeatures,
                     null,
                     null,
                     false,
                     null,
                     true,
                     null,
                     null,
                     null,
                     null,
                     null);
    }

    if (withRaw != null && withRaw) {
      if (maxEntities == null || expectedEntities.size() < maxEntities) {
        validateRawDataMap(testInfo,
                           response.getRawData(),
                           true,
                           "ENTITY_PATHS", "ENTITIES");
      } else {
        validateRawDataMap(testInfo,
                           response.getRawData(),
                           true,
                           "ENTITY_PATHS",
                           "ENTITIES",
                           "MAX_ENTITY_LIMIT_REACHED");
      }

      Object rawPaths = ((Map) response.getRawData()).get("ENTITY_PATHS");

      validateRawDataMapArray(testInfo,
                              rawPaths,
                              true,
                              "START_ENTITY_ID",
                              "END_ENTITY_ID",
                              "ENTITIES");

      Object rawEntities = ((Map) response.getRawData()).get("ENTITIES");

      validateRawDataMapArray(testInfo,
                              rawEntities,
                              true,
                              "RESOLVED_ENTITY",
                              "RELATED_ENTITIES");

      for (Object entity : ((Collection) rawEntities)) {
          validateRawDataMap(
              testInfo,
              ((Map) entity).get("RESOLVED_ENTITY"),
              false,
              rawEntityKeys(forceMinimal, detailLevel, featureMode));
      }
    }
  }

}
