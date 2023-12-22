package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.gen.api.invoker.ApiClient;
import com.senzing.gen.api.services.EntityDataApi;
import com.senzing.repomgr.RepositoryManager;
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
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static com.senzing.api.model.SzDetailLevel.*;
import static com.senzing.api.model.SzFeatureMode.*;
import static com.senzing.api.model.SzHttpMethod.GET;
import static com.senzing.api.services.ResponseValidators.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(Lifecycle.PER_CLASS)
public class WhyServicesTest extends AbstractServiceTest {
  private static final long RANDOM_SEED = 3456789012L;

  private static final String PASSENGERS = "PASSENGERS";
  private static final String CUSTOMERS = "CUSTOMERS";
  private static final String VIPS = "VIPS";

  private static final String COMPANIES = "COMPANIES";
  private static final String EMPLOYEES = "EMPLOYEES";
  private static final String CONTACTS = "CONTACTS";

  private static final SzRecordId ABC123
      = SzRecordId.FACTORY.create(PASSENGERS, "ABC123");
  private static final SzRecordId DEF456
      = SzRecordId.FACTORY.create(PASSENGERS, "DEF456");
  private static final SzRecordId GHI789
      = SzRecordId.FACTORY.create(PASSENGERS, "GHI789");
  private static final SzRecordId JKL012
      = SzRecordId.FACTORY.create(PASSENGERS, "JKL012");
  private static final SzRecordId MNO345
      = SzRecordId.FACTORY.create(CUSTOMERS, "MNO345");
  private static final SzRecordId PQR678
      = SzRecordId.FACTORY.create(CUSTOMERS, "PQR678");
  private static final SzRecordId ABC567
      = SzRecordId.FACTORY.create(CUSTOMERS, "ABC567");
  private static final SzRecordId DEF890
      = SzRecordId.FACTORY.create(CUSTOMERS, "DEF890");
  private static final SzRecordId STU901
      = SzRecordId.FACTORY.create(VIPS, "STU901");
  private static final SzRecordId XYZ234
      = SzRecordId.FACTORY.create(VIPS, "XYZ234");
  private static final SzRecordId GHI123
      = SzRecordId.FACTORY.create(VIPS, "GHI123");
  private static final SzRecordId JKL456
      = SzRecordId.FACTORY.create(VIPS, "JKL456");

  private static final List<SzRecordId> RECORD_IDS;

  private static final SzRecordId COMPANY_1
      = SzRecordId.FACTORY.create(COMPANIES, "COMPANY_1");
  private static final SzRecordId COMPANY_2
      = SzRecordId.FACTORY.create(COMPANIES, "COMPANY_2");
  private static final SzRecordId EMPLOYEE_1
      = SzRecordId.FACTORY.create(EMPLOYEES, "EMPLOYEE_1");
  private static final SzRecordId EMPLOYEE_2
      = SzRecordId.FACTORY.create(EMPLOYEES, "EMPLOYEE_2");
  private static final SzRecordId EMPLOYEE_3
      = SzRecordId.FACTORY.create(EMPLOYEES, "EMPLOYEE_3");
  private static final SzRecordId CONTACT_1
      = SzRecordId.FACTORY.create(CONTACTS, "CONTACT_1");
  private static final SzRecordId CONTACT_2
      = SzRecordId.FACTORY.create(CONTACTS, "CONTACT_2");
  private static final SzRecordId CONTACT_3
      = SzRecordId.FACTORY.create(CONTACTS, "CONTACT_3");
  private static final SzRecordId CONTACT_4
      = SzRecordId.FACTORY.create(CONTACTS, "CONTACT_4");

  private static final List<SzRecordId> RELATED_RECORD_IDS;

  static {
    List<SzRecordId> recordIds = new ArrayList<>(12);
    List<SzRecordId> relatedIds = new ArrayList<>(9);

    try {
      recordIds.add(ABC123);
      recordIds.add(DEF456);
      recordIds.add(GHI789);
      recordIds.add(JKL012);
      recordIds.add(MNO345);
      recordIds.add(PQR678);
      recordIds.add(ABC567);
      recordIds.add(DEF890);
      recordIds.add(STU901);
      recordIds.add(XYZ234);
      recordIds.add(GHI123);
      recordIds.add(JKL456);

      relatedIds.add(COMPANY_1);
      relatedIds.add(COMPANY_2);
      relatedIds.add(EMPLOYEE_1);
      relatedIds.add(EMPLOYEE_2);
      relatedIds.add(EMPLOYEE_3);
      relatedIds.add(CONTACT_1);
      relatedIds.add(CONTACT_2);
      relatedIds.add(CONTACT_3);
      relatedIds.add(CONTACT_4);

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      RECORD_IDS = Collections.unmodifiableList(recordIds);
      RELATED_RECORD_IDS = Collections.unmodifiableList(relatedIds);
    }
  }

  private WhyServices whyServices;
  private EntityDataServices entityDataServices;
  private EntityDataApi entityDataApi;

  @BeforeAll
  public void initializeEnvironment() {
    this.beginTests();
    this.initializeTestEnvironment();
    this.whyServices = new WhyServices();
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
    dataSources.add("CUSTOMERS");
    dataSources.add("VIPS");
    dataSources.add("COMPANIES");
    dataSources.add("EMPLOYEES");
    dataSources.add("CONTACTS");

    File passengerFile = this.preparePassengerFile();
    File customerFile = this.prepareCustomerFile();
    File vipFile = this.prepareVipFile();

    File companyFile = this.prepareCompanyFile();
    File employeeFile = this.prepareEmployeeFile();
    File contactFile = this.prepareContactFile();

    customerFile.deleteOnExit();
    passengerFile.deleteOnExit();
    vipFile.deleteOnExit();
    companyFile.deleteOnExit();
    employeeFile.deleteOnExit();
    contactFile.deleteOnExit();

    RepositoryManager.configSources(repoDirectory,
                                    dataSources,
                                    true);

    RepositoryManager.loadFile(repoDirectory,
                               passengerFile,
                               PASSENGERS,
                               true);

    RepositoryManager.loadFile(repoDirectory,
                               customerFile,
                               CUSTOMERS,
                               true);

    RepositoryManager.loadFile(repoDirectory,
                               vipFile,
                               VIPS,
                               true);

    RepositoryManager.loadFile(repoDirectory,
                               companyFile,
                               COMPANIES,
                               true);

    RepositoryManager.loadFile(repoDirectory,
                               employeeFile,
                               EMPLOYEES,
                               true);

    RepositoryManager.loadFile(repoDirectory,
                               contactFile,
                               CONTACTS,
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

  private File prepareCustomerFile() {
    String[] headers = {
        "RECORD_ID", "NAME_FIRST", "NAME_LAST", "MOBILE_PHONE_NUMBER",
        "HOME_PHONE_NUMBER", "ADDR_FULL", "DATE_OF_BIRTH"};

    String[][] customers = {
        {MNO345.getRecordId(), "Bill", "Wright", "702-444-2121", "702-123-4567",
            "101 Main Street, Las Vegas, NV 89101", "22-AUG-1981"},
        {PQR678.getRecordId(), "Craig", "Smith", "212-555-1212", "702-888-3939",
            "451 Dover Street, Las Vegas, NV 89108", "17-NOV-1982"},
        {ABC567.getRecordId(), "Kim", "Long", "702-246-8024", "702-135-7913",
            "451 Dover Street, Las Vegas, NV 89108", "24-OCT-1976"},
        {DEF890.getRecordId(), "Kathy", "Osborne", "702-444-2121", "702-111-2222",
            "707 Seventh Ave, Las Vegas, NV 89143", "27-JUL-1981"}
    };

    return this.prepareJsonArrayFile("test-customers-", headers, customers);
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

  private File prepareCompanyFile() {
    JsonArrayBuilder jab = Json.createArrayBuilder();
    JsonObjectBuilder job = Json.createObjectBuilder();
    job.add("RECORD_ID", COMPANY_1.getRecordId());
    job.add("DATA_SOURCE", COMPANY_1.getDataSourceCode());
    job.add("NAME_ORG", "Acme Corporation");
    JsonArrayBuilder relJab = Json.createArrayBuilder();
    JsonObjectBuilder relJob = Json.createObjectBuilder();
    relJob.add("REL_ANCHOR_DOMAIN", "EMPLOYER_ID");
    relJob.add("REL_ANCHOR_KEY", "ACME_CORP_KEY");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_ANCHOR_DOMAIN", "CORP_HIERARCHY");
    relJob.add("REL_ANCHOR_KEY", "ACME_CORP_KEY");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_POINTER_DOMAIN", "CORP_HIERARCHY");
    relJob.add("REL_POINTER_KEY", "COYOTE_SOLUTIONS_KEY");
    relJob.add("REL_POINTER_ROLE", "ULTIMATE_PARENT");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_POINTER_DOMAIN", "CORP_HIERARCHY");
    relJob.add("REL_POINTER_KEY", "COYOTE_SOLUTIONS_KEY");
    relJob.add("REL_POINTER_ROLE", "PARENT");
    relJab.add(relJob);
    job.add("RELATIONSHIP_LIST", relJab);

    jab.add(job);
    job = Json.createObjectBuilder();
    job.add("RECORD_ID", COMPANY_2.getRecordId());
    job.add("DATA_SOURCE", COMPANY_2.getDataSourceCode());
    job.add("NAME_ORG", "Coyote Solutions");
    relJab = Json.createArrayBuilder();
    relJob = Json.createObjectBuilder();
    relJob.add("REL_ANCHOR_DOMAIN", "EMPLOYER_ID");
    relJob.add("REL_ANCHOR_KEY", "COYOTE_SOLUTIONS_KEY");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_ANCHOR_DOMAIN", "CORP_HIERARCHY");
    relJob.add("REL_ANCHOR_KEY", "COYOTE_SOLUTIONS_KEY");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_POINTER_DOMAIN", "CORP_HIERARCHY");
    relJob.add("REL_POINTER_KEY", "ACME_CORP_KEY");
    relJob.add("REL_POINTER_ROLE", "SUBSIDIARY");
    relJab.add(relJob);
    job.add("RELATIONSHIP_LIST", relJab);
    jab.add(job);

    return this.prepareJsonFile("test-companies-", jab.build());
  }

  private File prepareEmployeeFile() {
    JsonArrayBuilder jab = Json.createArrayBuilder();
    JsonObjectBuilder job = Json.createObjectBuilder();
    job.add("RECORD_ID", EMPLOYEE_1.getRecordId());
    job.add("DATA_SOURCE", EMPLOYEE_1.getDataSourceCode());
    job.add("NAME_FULL", "Jeff Founder");
    JsonArrayBuilder relJab = Json.createArrayBuilder();
    JsonObjectBuilder relJob = Json.createObjectBuilder();
    relJob.add("REL_ANCHOR_DOMAIN", "EMPLOYEE_NUM");
    relJob.add("REL_ANCHOR_KEY", "1");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_POINTER_DOMAIN", "EMPLOYER_ID");
    relJob.add("REL_POINTER_KEY", "ACME_CORP_KEY");
    relJob.add("REL_POINTER_ROLE", "EMPLOYED_BY");
    relJab.add(relJob);
    job.add("RELATIONSHIP_LIST", relJab);
    jab.add(job);

    job = Json.createObjectBuilder();
    job.add("RECORD_ID", EMPLOYEE_2.getRecordId());
    job.add("DATA_SOURCE", EMPLOYEE_2.getDataSourceCode());
    job.add("NAME_FULL", "Jane Leader");
    relJab = Json.createArrayBuilder();
    relJob = Json.createObjectBuilder();
    relJob.add("REL_ANCHOR_DOMAIN", "EMPLOYEE_NUM");
    relJob.add("REL_ANCHOR_KEY", "2");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_POINTER_DOMAIN", "EMPLOYEE_NUM");
    relJob.add("REL_POINTER_KEY", "1");
    relJob.add("REL_POINTER_ROLE", "MANAGED_BY");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_POINTER_DOMAIN", "EMPLOYER_ID");
    relJob.add("REL_POINTER_KEY", "ACME_CORP_KEY");
    relJob.add("REL_POINTER_ROLE", "EMPLOYED_BY");
    relJab.add(relJob);
    job.add("RELATIONSHIP_LIST", relJab);
    jab.add(job);

    job = Json.createObjectBuilder();
    job.add("RECORD_ID", EMPLOYEE_3.getRecordId());
    job.add("DATA_SOURCE", EMPLOYEE_3.getDataSourceCode());
    job.add("NAME_FULL", "Joe Workman");
    relJab = Json.createArrayBuilder();
    relJob = Json.createObjectBuilder();
    relJob.add("REL_ANCHOR_DOMAIN", "EMPLOYEE_NUM");
    relJob.add("REL_ANCHOR_KEY", "6");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_POINTER_DOMAIN", "EMPLOYEE_NUM");
    relJob.add("REL_POINTER_KEY", "2");
    relJob.add("REL_POINTER_ROLE", "MANAGED_BY");
    relJab.add(relJob);
    relJob = Json.createObjectBuilder();
    relJob.add("REL_POINTER_DOMAIN", "EMPLOYER_ID");
    relJob.add("REL_POINTER_KEY", "ACME_CORP_KEY");
    relJob.add("REL_POINTER_ROLE", "EMPLOYED_BY");
    relJab.add(relJob);
    job.add("RELATIONSHIP_LIST", relJab);
    jab.add(job);

    return this.prepareJsonFile("test-employees-", jab.build());
  }

  private File prepareContactFile() {
    JsonArrayBuilder jab = Json.createArrayBuilder();
    JsonObjectBuilder job = Json.createObjectBuilder();
    job.add("RECORD_ID", CONTACT_1.getRecordId());
    job.add("DATA_SOURCE", CONTACT_1.getDataSourceCode());
    job.add("NAME_FULL", "Richard Couples");
    job.add("PHONE_NUMBER", "718-949-8812");
    job.add("ADDR_FULL", "10010 WOODLAND AVE; ATLANTA, GA 30334");
    JsonArrayBuilder relJab = Json.createArrayBuilder();
    JsonObjectBuilder relJob = Json.createObjectBuilder();
    relJob.add("RELATIONSHIP_TYPE", "SPOUSE");
    relJob.add("RELATIONSHIP_KEY", "SPOUSES-1-2");
    relJob.add("RELATIONSHIP_ROLE", "WIFE");
    relJab.add(relJob);
    job.add("RELATIONSHIP_LIST", relJab);
    jab.add(job);

    job = Json.createObjectBuilder();
    job.add("RECORD_ID", CONTACT_2.getRecordId());
    job.add("DATA_SOURCE", CONTACT_2.getDataSourceCode());
    job.add("NAME_FULL", "Brianna Couples");
    job.add("PHONE_NUMBER", "718-949-8812");
    job.add("ADDR_FULL", "10010 WOODLAND AVE; ATLANTA, GA 30334");
    relJab = Json.createArrayBuilder();
    relJob = Json.createObjectBuilder();
    relJob.add("RELATIONSHIP_TYPE", "SPOUSE");
    relJob.add("RELATIONSHIP_KEY", "SPOUSES-1-2");
    relJob.add("RELATIONSHIP_ROLE", "HUSBAND");
    relJab.add(relJob);
    job.add("RELATIONSHIP_LIST", relJab);
    jab.add(job);

    job = Json.createObjectBuilder();
    job.add("RECORD_ID", CONTACT_3.getRecordId());
    job.add("DATA_SOURCE", CONTACT_3.getDataSourceCode());
    job.add("NAME_FULL", "Samuel Strong");
    job.add("PHONE_NUMBER", "312-889-3340");
    job.add("ADDR_FULL", "10010 LAKE VIEW RD; SPRINGFIELD, MO 65807");
    jab.add(job);

    job = Json.createObjectBuilder();
    job.add("RECORD_ID", CONTACT_4.getRecordId());
    job.add("DATA_SOURCE", CONTACT_4.getDataSourceCode());
    job.add("NAME_FULL", "Melissa Powers");
    job.add("PHONE_NUMBER", "312-885-4236");
    job.add("ADDR_FULL", "10010 LAKE VIEW RD; SPRINGFIELD, MO 65807");
    jab.add(job);

    return this.prepareJsonFile("test-contacts-", jab.build());
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
        MINIMAL,
        WITH_DUPLICATES,
        false,
        false,
        uriInfo);

    SzEntityData data = response.getData();

    SzResolvedEntity entity = data.getResolvedEntity();

    return entity.getEntityId();
  }

  private List<Arguments> getWhyEntityParameters() {
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

    List<SzRecordId> recordIds = RECORD_IDS;

    Random prng = new Random(RANDOM_SEED);

    List<List<Boolean>> booleanCombos = getBooleanVariants(5);
    Collections.shuffle(booleanCombos, prng);
    Iterator<List<Boolean>> booleansIter  = circularIterator(booleanCombos);

    List<List> optionCombos = generateCombinations(detailLevels, featureModes);
    Collections.shuffle(optionCombos, prng);
    Iterator<List> optionsIter = circularIterator(optionCombos);

    int loopCount
        = Math.max(booleanCombos.size(), optionCombos.size()) * 15
        / recordIds.size();

    int totalCount = loopCount * recordIds.size();

    List<Arguments> result = new ArrayList<>(totalCount);

    recordIds.forEach(recordId -> {
      for (int index = 0; index < loopCount; index++) {
        List<Boolean> booleansList          = booleansIter.next();
        List          optsList              = optionsIter.next();

        Boolean       forceMinimal          = booleansList.get(0);
        Boolean       withFeatureStats      = booleansList.get(1);
        Boolean       withInternalFeatures  = booleansList.get(2);
        Boolean       withRelationships     = booleansList.get(3);
        Boolean       withRaw               = booleansList.get(4);

        SzDetailLevel detailLevel           = (SzDetailLevel) optsList.get(0);
        SzFeatureMode featureMode           = (SzFeatureMode) optsList.get(1);

        Object[] args = {
            recordId,
            forceMinimal,
            detailLevel,
            featureMode,
            withFeatureStats,
            withInternalFeatures,
            withRelationships,
            withRaw
        };

        result.add(arguments(args));
      }
    });

    return result;
  }

  private List<Arguments> getWhyEntitiesParameters() {
    Boolean[] booleanVariants = {null, true, false};
    Boolean[] asRecordVariants = {true, false};
    List<Boolean> booleanVariantList = Arrays.asList(booleanVariants);
    List<Boolean> asRecordVariantList = Arrays.asList(asRecordVariants);

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

    List<List> optionCombos = generateCombinations(asRecordVariantList,
                                                   detailLevels,
                                                   featureModes);
    Collections.shuffle(optionCombos, prng);
    Iterator<List> optionsIter = circularIterator(optionCombos);

    // get the record ID combinations
    List<List> recordIdCombos = generateCombinations(
        RELATED_RECORD_IDS, RELATED_RECORD_IDS);

    // pare down the combinations
    Iterator<List> iter = recordIdCombos.iterator();
    while (iter.hasNext()) {
      List list = (List) iter.next();
      SzRecordId arg0 = (SzRecordId) list.get(0);
      SzRecordId arg1 = (SzRecordId) list.get(1);

      // thin the list out to reduce the number of tests
      int index1 = RELATED_RECORD_IDS.indexOf(arg0);
      int index2 = RELATED_RECORD_IDS.indexOf(arg1);
      if (Math.abs(index2 - index1) > 4) {
        iter.remove();
      }
    }

    int loopCount
        = Math.max(booleanCombos.size(), optionCombos.size()) * 25
        / recordIdCombos.size();

    int totalCount = loopCount * recordIdCombos.size();

    List<Arguments> result = new ArrayList<>(totalCount);

    recordIdCombos.forEach(recordIds -> {
      for (int index = 0; index < loopCount; index++) {
        List<Boolean> booleansList          = booleansIter.next();
        List          optsList              = optionsIter.next();

        Boolean       forceMinimal          = booleansList.get(0);
        Boolean       withFeatureStats      = booleansList.get(1);
        Boolean       withInternalFeatures  = booleansList.get(2);
        Boolean       withRelationships     = booleansList.get(3);
        Boolean       withRaw               = booleansList.get(4);

        Boolean       asRecord              = (Boolean) optsList.get(0);
        SzDetailLevel detailLevel           = (SzDetailLevel) optsList.get(1);
        SzFeatureMode featureMode           = (SzFeatureMode) optsList.get(2);

        Object[] args = {
            recordIds.get(0),
            recordIds.get(1),
            asRecord,
            forceMinimal,
            detailLevel,
            featureMode,
            withFeatureStats,
            withInternalFeatures,
            withRelationships,
            withRaw
        };

        result.add(arguments(args));
      }
    });

    return result;
  }

  private List<Arguments> getWhyRecordsParameters() {
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
    List<List<Boolean>> booleanCombos = getBooleanVariants(5);
    Collections.shuffle(booleanCombos, prng);
    Iterator<List<Boolean>> booleansIter = circularIterator(booleanCombos);

    List<List> optionCombos = generateCombinations(detailLevels,
                                                   featureModes);
    Collections.shuffle(optionCombos, prng);
    Iterator<List> optionsIter = circularIterator(optionCombos);

    List<List> recordIdCombos = generateCombinations(RECORD_IDS, RECORD_IDS);

    int loopCount
        = Math.max(booleanCombos.size(), optionCombos.size()) * 25
        / recordIdCombos.size();

    int totalCount = loopCount * recordIdCombos.size();

    List<Arguments> result = new ArrayList<>(totalCount);


    recordIdCombos.forEach(recordIds -> {
      for (int index = 0; index < loopCount; index++) {
        List<Boolean> booleansList          = booleansIter.next();
        List          optsList              = optionsIter.next();

        Boolean       forceMinimal          = booleansList.get(0);
        Boolean       withFeatureStats      = booleansList.get(1);
        Boolean       withInternalFeatures  = booleansList.get(2);
        Boolean       withRelationships     = booleansList.get(3);
        Boolean       withRaw               = booleansList.get(4);

        SzDetailLevel detailLevel           = (SzDetailLevel) optsList.get(0);
        SzFeatureMode featureMode           = (SzFeatureMode) optsList.get(1);

        Object[] args = {
            recordIds.get(0),
            recordIds.get(1),
            forceMinimal,
            detailLevel,
            featureMode,
            withFeatureStats,
            withInternalFeatures,
            withRelationships,
            withRaw
        };

        result.add(arguments(args));
      }
    });

    return result;
  }

  private StringBuilder buildWhyEntityQueryString(
      StringBuilder sb,
      Boolean       forceMinimal,
      SzDetailLevel detailLevel,
      SzFeatureMode featureMode,
      Boolean       withFeatureStats,
      Boolean       withInternalFeatures,
      Boolean       withRelationships,
      Boolean       withRaw)
  {
    String prefix = "?";
    if (withRelationships != null) {
      sb.append(prefix).append("withRelationships=").append(withRelationships);
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
    if (detailLevel != null) {
      sb.append(prefix).append("detailLevel=").append(detailLevel);
      prefix = "&";
    }
    if (featureMode != null) {
      sb.append(prefix).append("featureMode=").append(featureMode);
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

  private StringBuilder buildWhyEntitiesQueryString(
      StringBuilder       sb,
      SzEntityIdentifier  entity1,
      SzEntityIdentifier  entity2,
      Boolean             forceMinimal,
      SzDetailLevel       detailLevel,
      SzFeatureMode       featureMode,
      Boolean             withFeatureStats,
      Boolean             withInternalFeatures,
      Boolean             withRelationships,
      Boolean             withRaw)
  {
    try {
      sb.append("?entity1=").append(
          URLEncoder.encode(entity1.toString(), "UTF-8"));
      sb.append("&entity2=").append(
          URLEncoder.encode(entity2.toString(), "UTF-8"));

    } catch (UnsupportedEncodingException cannotHappen) {
      throw new IllegalStateException("UTF-8 Encoding is not support");
    }

    if (withRelationships != null) {
      sb.append("&withRelationships=").append(withRelationships);
    }
    if (withFeatureStats != null) {
      sb.append("&withFeatureStats=").append(withFeatureStats);
    }
    if (withInternalFeatures != null) {
      sb.append("&withInternalFeatures=").append(withInternalFeatures);
    }
    if (detailLevel != null) {
      sb.append("&detailLevel=").append(detailLevel);
    }
    if (featureMode != null) {
      sb.append("&featureMode=").append(featureMode);
    }
    if (forceMinimal != null) {
      sb.append("&forceMinimal=").append(forceMinimal);
    }
    if (withRaw != null) {
      sb.append("&withRaw=").append(withRaw);
    }
    return sb;
  }

  private StringBuilder buildWhyRecordsQueryString(
      StringBuilder sb,
      SzRecordId    recordId1,
      SzRecordId    recordId2,
      Boolean       forceMinimal,
      SzDetailLevel detailLevel,
      SzFeatureMode featureMode,
      Boolean       withFeatureStats,
      Boolean       withInternalFeatures,
      Boolean       withRelationships,
      Boolean       withRaw)
  {
    try {
      sb.append("?dataSource1=").append(
          URLEncoder.encode(recordId1.getDataSourceCode(), "UTF-8"));

      sb.append("&recordId1=").append(
          URLEncoder.encode(recordId1.getRecordId(), "UTF-8"));

      sb.append("&dataSource2=").append(
          URLEncoder.encode(recordId2.getDataSourceCode(), "UTF-8"));

      sb.append("&recordId2=").append(
          URLEncoder.encode(recordId2.getRecordId(), "UTF-8"));

      if (withRelationships != null) {
        sb.append("&withRelationships=").append(withRelationships);
      }
      if (withFeatureStats != null) {
        sb.append("&withFeatureStats=").append(withFeatureStats);
      }
      if (withInternalFeatures != null) {
        sb.append("&withInternalFeatures=").append(withInternalFeatures);
      }
      if (detailLevel != null) {
        sb.append("&detailLevel=").append(detailLevel);
      }
      if (featureMode != null) {
        sb.append("&featureMode=").append(featureMode);
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
  @MethodSource("getWhyEntitiesParameters")
  public void whyEntitiesTest(SzRecordId    recordId1,
                              SzRecordId    recordId2,
                              boolean       asRecordIds,
                              Boolean       forceMinimal,
                              SzDetailLevel detailLevel,
                              SzFeatureMode featureMode,
                              Boolean       withFeatureStats,
                              Boolean       withInternalFeatures,
                              Boolean       withRelationships,
                              Boolean       withRaw)
  {
    this.performTest(() -> {
      String testInfo = "recordId1=[ " + recordId1
          + " ], recordId2=[ " + recordId2
          + " ], asRecordIds=[ " + asRecordIds
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";


      StringBuilder sb = new StringBuilder();
      sb.append("why/entities");

      Long entityId1 = getEntityIdForRecordId(recordId1);
      Long entityId2 = getEntityIdForRecordId(recordId2);

      SzEntityIdentifier entityIdent1 = (asRecordIds) ? recordId1
          : SzEntityId.FACTORY.create(entityId1);

      SzEntityIdentifier entityIdent2 = (asRecordIds) ? recordId2
          : SzEntityId.FACTORY.create(entityId2);

      buildWhyEntitiesQueryString(sb,
                                  entityIdent1,
                                  entityIdent2,
                                  forceMinimal,
                                  detailLevel,
                                  featureMode,
                                  withFeatureStats,
                                  withInternalFeatures,
                                  withRelationships,
                                  withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzWhyEntitiesResponse response = this.whyServices.whyEntities(
          entityIdent1.toString(),
          entityIdent2.toString(),
          (forceMinimal == null ? false : forceMinimal),
          (detailLevel == null ? VERBOSE : detailLevel),
          (featureMode == null ? WITH_DUPLICATES : featureMode),
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          (withRaw == null ? false : withRaw),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      this.validateWhyEntitiesResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId1,
          recordId2,
          entityId1,
          entityId2,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyEntitiesParameters")
  public void whyEntitiesViaHttpTest(SzRecordId     recordId1,
                                     SzRecordId     recordId2,
                                     boolean        asRecordIds,
                                     Boolean        forceMinimal,
                                     SzDetailLevel  detailLevel,
                                     SzFeatureMode  featureMode,
                                     Boolean        withFeatureStats,
                                     Boolean        withInternalFeatures,
                                     Boolean        withRelationships,
                                     Boolean        withRaw)
  {
    this.performTest(() -> {
      String testInfo = "recordId1=[ " + recordId1
          + " ], recordId2=[ " + recordId2
          + " ], asRecordIds=[ " + asRecordIds
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";


      StringBuilder sb = new StringBuilder();
      sb.append("why/entities");

      Long entityId1 = getEntityIdForRecordId(recordId1);
      Long entityId2 = getEntityIdForRecordId(recordId2);

      SzEntityIdentifier entityIdent1 = (asRecordIds) ? recordId1
          : SzEntityId.FACTORY.create(entityId1);

      SzEntityIdentifier entityIdent2 = (asRecordIds) ? recordId2
          : SzEntityId.FACTORY.create(entityId2);

      buildWhyEntitiesQueryString(sb,
                                  entityIdent1,
                                  entityIdent2,
                                  forceMinimal,
                                  detailLevel,
                                  featureMode,
                                  withFeatureStats,
                                  withInternalFeatures,
                                  withRelationships,
                                  withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzWhyEntitiesResponse response = this.invokeServerViaHttp(
          GET, uriText, SzWhyEntitiesResponse.class);

      response.concludeTimers();
      long after = System.nanoTime();

      this.validateWhyEntitiesResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId1,
          recordId2,
          entityId1,
          entityId2,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyEntitiesParameters")
  public void whyEntitiesViaJavaClientTest(SzRecordId     recordId1,
                                           SzRecordId     recordId2,
                                           boolean        asRecordIds,
                                           Boolean        forceMinimal,
                                           SzDetailLevel  detailLevel,
                                           SzFeatureMode  featureMode,
                                           Boolean        withFeatureStats,
                                           Boolean        withInternalFeatures,
                                           Boolean        withRelationships,
                                           Boolean        withRaw)
  {
    this.performTest(() -> {
      String testInfo = "recordId1=[ " + recordId1
          + " ], recordId2=[ " + recordId2
          + " ], asRecordIds=[ " + asRecordIds
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";


      StringBuilder sb = new StringBuilder();
      sb.append("why/entities");

      Long entityId1 = getEntityIdForRecordId(recordId1);
      Long entityId2 = getEntityIdForRecordId(recordId2);

      SzEntityIdentifier entityIdent1 = (asRecordIds) ? recordId1
          : SzEntityId.FACTORY.create(entityId1);

      SzEntityIdentifier entityIdent2 = (asRecordIds) ? recordId2
          : SzEntityId.FACTORY.create(entityId2);

      buildWhyEntitiesQueryString(sb,
                                  entityIdent1,
                                  entityIdent2,
                                  forceMinimal,
                                  detailLevel,
                                  featureMode,
                                  withFeatureStats,
                                  withInternalFeatures,
                                  withRelationships,
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

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzWhyEntitiesResponse clientResponse
          = this.entityDataApi.whyEntities(
              entityIdent1.toString(),
              entityIdent2.toString(),
              withRelationships,
              withFeatureStats,
              withInternalFeatures,
              clientDetailLevel,
              clientFeatureMode,
              forceMinimal,
              withRaw);

      long after = System.nanoTime();

      SzWhyEntitiesResponse response = jsonCopy(clientResponse,
                                                SzWhyEntitiesResponse.class);

      this.validateWhyEntitiesResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId1,
          recordId2,
          entityId1,
          entityId2,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @Test
  public void whyEntitiesBadRecordIdTest() {
    this.performTest(() -> {
      StringBuilder sb = new StringBuilder();
      sb.append("why/entities");

      SzRecordId recordId1 = SzRecordId.FACTORY.create(COMPANIES,
                                                       "DOES_NOT_EXIST");
      SzRecordId recordId2 = COMPANY_1;

      buildWhyEntitiesQueryString(sb,
                                  recordId1,
                                  recordId2,
                                  false,
                                  MINIMAL,
                                  REPRESENTATIVE,
                                  false,
                                  false,
                                  false,
                                  false);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      try {
      SzWhyEntitiesResponse response = this.whyServices.whyEntities(
          recordId1.toString(),
          recordId2.toString(),
          false,
          SzDetailLevel.MINIMAL,
          SzFeatureMode.REPRESENTATIVE,
          false,
          false,
          false,
          false,
          uriInfo);

        fail("Expected entity for dataSource \"" + recordId1.getDataSourceCode()
                 + "\" and record ID \"" + recordId1.getRecordId()
                 + "\" to NOT be found");

      } catch (BadRequestException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();

        validateBasics(response, 400, GET, uriText, after - before);
      }
    });
  }

  @Test
  public void whyEntitiesBadRecordIdViaHttpTest() {
    this.performTest(() -> {
      StringBuilder sb = new StringBuilder();
      sb.append("why/entities");

      SzRecordId recordId1 = SzRecordId.FACTORY.create(COMPANIES,
                                                       "DOES_NOT_EXIST");
      SzRecordId recordId2 = COMPANY_1;

      buildWhyEntitiesQueryString(sb,
                                  recordId1,
                                  recordId2,
                                  false,
                                  MINIMAL,
                                  REPRESENTATIVE,
                                  false,
                                  false,
                                  false,
                                  false);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          GET, uriText, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(
          response, 400, GET, uriText, after - before);
    });

  }

  @Test
  public void whyEntitiesBadDataSourceTest() {
    this.performTest(() -> {
      StringBuilder sb = new StringBuilder();
      sb.append("why/entities");

      SzRecordId recordId1 = SzRecordId.FACTORY.create("DOES_NOT_EXIST",
                                                       "ABC123");
      SzRecordId recordId2 = COMPANY_1;

      buildWhyEntitiesQueryString(sb,
                                  recordId1,
                                  recordId2,
                                  false,
                                  VERBOSE,
                                  REPRESENTATIVE,
                                  false,
                                  false,
                                  false,
                                  false);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      try {
        SzWhyEntitiesResponse response = this.whyServices.whyEntities(
            recordId1.toString(),
            recordId2.toString(),
            false,
            VERBOSE,
            REPRESENTATIVE,
            false,
            false,
            false,
            false,
            uriInfo);

        fail("Expected entity for dataSource \"" + recordId1.getDataSourceCode()
                 + "\" and record ID \"" + recordId1.getRecordId()
                 + "\" to NOT be found");

      } catch (BadRequestException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();

        validateBasics(
            response, 400, GET, uriText, after - before);
      }
    });
  }

  @Test
  public void whyEntitiesBadDataSourceViaHttpTest() {
    this.performTest(() -> {
      StringBuilder sb = new StringBuilder();
      sb.append("why/entities");

      SzRecordId recordId1 = SzRecordId.FACTORY.create("DOES_NOT_EXIST",
                                                       "ABC123");
      SzRecordId recordId2 = COMPANY_1;

      buildWhyEntitiesQueryString(sb,
                                  recordId1,
                                  recordId2,
                                  false,
                                  VERBOSE,
                                  REPRESENTATIVE,
                                  false,
                                  false,
                                  false,
                                  false);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          GET, uriText, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(
          response, 400, GET, uriText, after - before);
    });
  }

  @Test
  public void whyEntitiesBadEntityIdTest() {
    this.performTest(() -> {
      StringBuilder sb = new StringBuilder();
      sb.append("why/entities");

      SzEntityId entityId1 = SzEntityId.FACTORY.create(100000000L);
      SzEntityId entityId2 = SzEntityId.FACTORY.create(100000001L);

      buildWhyEntitiesQueryString(sb,
                                  entityId1,
                                  entityId2,
                                  false,
                                  VERBOSE,
                                  REPRESENTATIVE,
                                  false,
                                  false,
                                  false,
                                  false);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      try {
        SzWhyEntitiesResponse response = this.whyServices.whyEntities(
            entityId1.toString(),
            entityId2.toString(),
            false,
            VERBOSE,
            REPRESENTATIVE,
            false,
            false,
            false,
            false,
            uriInfo);

        fail("Expected entity for entity ID \"" + entityId1
                 + "\" and entity ID \"" + entityId2
                 + "\" to NOT be found");

      } catch (BadRequestException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();

        validateBasics(
            response, 400, GET, uriText, after - before);
      }
    });
  }

  @Test
  public void whyEntitiesBadEntityIdViaHttpTest() {
    this.performTest(() -> {
      StringBuilder sb = new StringBuilder();
      sb.append("why/entities");

      SzEntityId entityId1 = SzEntityId.FACTORY.create(100000000L);
      SzEntityId entityId2 = SzEntityId.FACTORY.create(100000001L);

      buildWhyEntitiesQueryString(sb,
                                  entityId1,
                                  entityId2,
                                  false,
                                  VERBOSE,
                                  REPRESENTATIVE,
                                  false,
                                  false,
                                  false,
                                  false);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzErrorResponse response = this.invokeServerViaHttp(
          GET, uriText, SzErrorResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(
          response, 400, GET, uriText, after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyEntityParameters")
  public void whyEntityByRecordIdTest(SzRecordId    recordId,
                                      Boolean       forceMinimal,
                                      SzDetailLevel detailLevel,
                                      SzFeatureMode featureMode,
                                      Boolean       withFeatureStats,
                                      Boolean       withInternalFeatures,
                                      Boolean       withRelationships,
                                      Boolean       withRaw)
  {
    this.performTest(() -> {
      String testInfo = "recordId=[ " + recordId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(urlEncode(recordId.getDataSourceCode()))
          .append("/records/").append(urlEncode(recordId.getRecordId()))
          .append("/entity/why");

      buildWhyEntityQueryString(sb,
                                forceMinimal,
                                detailLevel,
                                featureMode,
                                withFeatureStats,
                                withInternalFeatures,
                                withRelationships,
                                withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzWhyEntityResponse response = this.whyServices.whyEntityByRecordId(
          recordId.getDataSourceCode(),
          recordId.getRecordId(),
          (forceMinimal == null ? false : forceMinimal),
          (detailLevel == null ? VERBOSE : detailLevel),
          (featureMode == null ? WITH_DUPLICATES : featureMode),
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          (withRaw == null ? false : withRaw),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      this.validateWhyEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId,
          null,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyEntityParameters")
  public void whyEntityByRecordIdViaHttpTest(
      SzRecordId    recordId,
      Boolean       forceMinimal,
      SzDetailLevel detailLevel,
      SzFeatureMode featureMode,
      Boolean       withFeatureStats,
      Boolean       withInternalFeatures,
      Boolean       withRelationships,
      Boolean       withRaw)
  {
    this.performTest(() -> {
      String testInfo = "recordId=[ " + recordId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(urlEncode(recordId.getDataSourceCode()))
          .append("/records/").append(urlEncode(recordId.getRecordId()))
          .append("/entity/why");

      buildWhyEntityQueryString(sb,
                                forceMinimal,
                                detailLevel,
                                featureMode,
                                withFeatureStats,
                                withInternalFeatures,
                                withRelationships,
                                withRaw);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzWhyEntityResponse response = this.invokeServerViaHttp(
          GET, uriText, SzWhyEntityResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      this.validateWhyEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId,
          null,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyEntityParameters")
  public void whyEntityByRecordIdViaJavaClientTest(
      SzRecordId    recordId,
      Boolean       forceMinimal,
      SzDetailLevel detailLevel,
      SzFeatureMode featureMode,
      Boolean       withFeatureStats,
      Boolean       withInternalFeatures,
      Boolean       withRelationships,
      Boolean       withRaw)
  {
    this.performTest(() -> {
      String testInfo = "recordId=[ " + recordId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("data-sources/").append(urlEncode(recordId.getDataSourceCode()))
          .append("/records/").append(urlEncode(recordId.getRecordId()))
          .append("/entity/why");

      buildWhyEntityQueryString(sb,
                                forceMinimal,
                                detailLevel,
                                featureMode,
                                withFeatureStats,
                                withInternalFeatures,
                                withRelationships,
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

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzWhyEntityResponse clientResponse
          = this.entityDataApi.whyEntityByRecordID(
              recordId.getDataSourceCode(),
              recordId.getRecordId(),
              withRelationships,
              withFeatureStats,
              withInternalFeatures,
              clientDetailLevel,
              clientFeatureMode,
              forceMinimal,
              withRaw);
      long after = System.nanoTime();

      SzWhyEntityResponse response = jsonCopy(clientResponse,
                                              SzWhyEntityResponse.class);

      this.validateWhyEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId,
          null,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyEntityParameters")
  public void whyEntityByEntityIdTest(SzRecordId    recordId,
                                      Boolean       forceMinimal,
                                      SzDetailLevel detailLevel,
                                      SzFeatureMode featureMode,
                                      Boolean       withFeatureStats,
                                      Boolean       withInternalFeatures,
                                      Boolean       withRelationships,
                                      Boolean       withRaw)
  {
    this.performTest(() -> {
      long entityId = this.getEntityIdForRecordId(recordId);

      String testInfo = "recordId=[ " + recordId
          + " ], entityId=[ " + entityId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("entities/").append(entityId).append("/why");

      buildWhyEntityQueryString(sb,
                                forceMinimal,
                                detailLevel,
                                featureMode,
                                withFeatureStats,
                                withInternalFeatures,
                                withRelationships,
                                withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzWhyEntityResponse response = this.whyServices.whyEntityByRecordId(
          recordId.getDataSourceCode(),
          recordId.getRecordId(),
          (forceMinimal == null ? false : forceMinimal),
          (detailLevel == null ? VERBOSE : detailLevel),
          (featureMode == null ? WITH_DUPLICATES : featureMode),
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          (withRaw == null ? false : withRaw),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      this.validateWhyEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId,
          null,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyEntityParameters")
  public void whyEntityByEntityIdViaHttpTest(
      SzRecordId    recordId,
      Boolean       forceMinimal,
      SzDetailLevel detailLevel,
      SzFeatureMode featureMode,
      Boolean       withFeatureStats,
      Boolean       withInternalFeatures,
      Boolean       withRelationships,
      Boolean       withRaw)
  {
    this.performTest(() -> {
      long entityId = this.getEntityIdForRecordId(recordId);

      String testInfo = "recordId=[ " + recordId
          + " ], entityId=[ " + entityId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("entities/").append(entityId).append("/why");

      buildWhyEntityQueryString(sb,
                                forceMinimal,
                                detailLevel,
                                featureMode,
                                withFeatureStats,
                                withInternalFeatures,
                                withRelationships,
                                withRaw);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzWhyEntityResponse response = this.invokeServerViaHttp(
          GET, uriText, SzWhyEntityResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      this.validateWhyEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId,
          null,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyEntityParameters")
  public void whyEntityByEntityIdTestViaJavaClient(
      SzRecordId    recordId,
      Boolean       forceMinimal,
      SzDetailLevel detailLevel,
      SzFeatureMode featureMode,
      Boolean       withFeatureStats,
      Boolean       withInternalFeatures,
      Boolean       withRelationships,
      Boolean       withRaw)
  {
    this.performTest(() -> {
      long entityId = this.getEntityIdForRecordId(recordId);

      String testInfo = "recordId=[ " + recordId
          + " ], entityId=[ " + entityId
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("entities/").append(entityId).append("/why");

      buildWhyEntityQueryString(sb,
                                forceMinimal,
                                detailLevel,
                                featureMode,
                                withFeatureStats,
                                withInternalFeatures,
                                withRelationships,
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

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzWhyEntityResponse clientResponse
          = this.entityDataApi.whyEntityByEntityID(
          entityId,
          withRelationships,
          withFeatureStats,
          withInternalFeatures,
          clientDetailLevel,
          clientFeatureMode,
          forceMinimal,
          withRaw);
      long after = System.nanoTime();

      SzWhyEntityResponse response = jsonCopy(clientResponse,
                                              SzWhyEntityResponse.class);

      this.validateWhyEntityResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId,
          null,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  public void validateWhyEntityResponse(
      String              testInfo,
      SzWhyEntityResponse response,
      SzHttpMethod        httpMethod,
      String              selfLink,
      SzRecordId          recordId,
      Long                entityId,
      boolean             forceMinimal,
      SzDetailLevel       detailLevel,
      SzFeatureMode       featureMode,
      boolean             withFeatureStats,
      boolean             withInternalFeatures,
      boolean             withRelationships,
      Boolean             withRaw,
      long                maxDuration)
  {
    if (testInfo != null && selfLink != null) {
      testInfo = testInfo + ", selfLink=[ " + selfLink + " ]";
    }
    validateBasics(testInfo, response, httpMethod, selfLink, maxDuration);

    List<SzWhyEntityResult> whyResults = response.getData().getWhyResults();
    List<SzEntityData> entities = response.getData().getEntities();

    assertNotNull(whyResults, "Why results list is null: " + testInfo);
    assertNotNull(entities, "Entities list is null: " + testInfo);

    Set<SzFocusRecordId> perspectiveIds = (recordId == null) ? null
        : new LinkedHashSet<>();

    for (SzWhyEntityResult result : whyResults) {
      if (entityId != null) {
        assertEquals(entityId, result.getPerspective().getEntityId(),
                     "Unexpected entity ID in perspective: "
                         + testInfo);
      }
      if (recordId != null) {
        for (SzFocusRecordId focusId : result.getPerspective().getFocusRecords()) {
          perspectiveIds.add(focusId);
        }
      }

      // check the why result why key
      SzWhyMatchInfo matchInfo = result.getMatchInfo();
      this.validateMatchInfo(testInfo, matchInfo);
    }

    SzFocusRecordId focusRecordId = SzFocusRecordId.FACTORY.create(
        recordId.getDataSourceCode(), recordId.getRecordId());

    if (recordId != null) {
      assertTrue(perspectiveIds.contains(focusRecordId),
                 "No perspective from requested record ID (" + recordId
                     + "): " + testInfo);
    }

    if (entityId != null) {
      Set<Long> entityIds = new LinkedHashSet<>();
      for (SzEntityData entityData : entities) {
        entityIds.add(entityData.getResolvedEntity().getEntityId());
      }
      assertTrue(entityIds.contains(entityId),
                 "Requested entity ID (" + entityId
                     + ") not represented in returned entities ("
                     + entityIds + "): " + testInfo);
    }

    if (recordId != null && detailLevel != SUMMARY) {
      Set<SzRecordId> recordIds = new LinkedHashSet<>();
      for (SzEntityData entityData : entities) {
        SzResolvedEntity entity = entityData.getResolvedEntity();
        for (SzEntityRecord record : entity.getRecords()) {
          recordIds.add(SzRecordId.FACTORY.create(record.getDataSource(),
                                                  record.getRecordId()));
        }
      }
      assertTrue(recordIds.contains(recordId),
                 "Requested record ID (" + recordId
                     + ") not represented in returned entities ("
                     + recordIds + "): " + testInfo);
    }

    Map<Long, SzResolvedEntity> entityMap = new LinkedHashMap<>();
    entities.forEach(entityData -> {
      SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();
      entityMap.put(resolvedEntity.getEntityId(), resolvedEntity);
    });

    // verify the with relationships
    if (withRelationships) {
      for (SzEntityData entityData : entities) {
        List<SzRelatedEntity> relatedList = entityData.getRelatedEntities();
        assertNotEquals(0, relatedList.size(),
                        "Expected at least one related entity with "
                            + "withRelationships set to true: " + testInfo);

      }
    } else {
      for (SzEntityData entityData : entities) {
        List<SzRelatedEntity> relatedList = entityData.getRelatedEntities();
        assertFalse(relatedList != null && relatedList.size() > 0,
                    "Got unexpected related entities ("
                        + relatedList.size()
                        + ") when withRelationships set to false: " + testInfo);
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
                     !withRelationships,
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
                         "WHY_RESULTS", "ENTITIES");

      Object rawResults = ((Map) response.getRawData()).get("WHY_RESULTS");

      validateRawDataMapArray(testInfo,
                              rawResults,
                              true,
                              "INTERNAL_ID",
                              "ENTITY_ID",
                              "FOCUS_RECORDS",
                              "MATCH_INFO");

      Object rawEntities = ((Map) response.getRawData()).get("ENTITIES");

      if (withRelationships) {
        validateRawDataMapArray(testInfo,
                                rawEntities,
                                true,
                                "RESOLVED_ENTITY",
                                "RELATED_ENTITIES");
      } else {
        validateRawDataMapArray(testInfo,
                                rawEntities,
                                true,
                                "RESOLVED_ENTITY");
      }

      for (Object entity : ((Collection) rawEntities)) {
          validateRawDataMap(
              testInfo,
              ((Map) entity).get("RESOLVED_ENTITY"),
              false,
              rawEntityKeys(forceMinimal, detailLevel, featureMode));
      }
    }
  }

  public void validateWhyEntitiesResponse(
      String                testInfo,
      SzWhyEntitiesResponse response,
      SzHttpMethod          httpMethod,
      String                selfLink,
      SzRecordId            recordId1,
      SzRecordId            recordId2,
      Long                  entityId1,
      Long                  entityId2,
      boolean               forceMinimal,
      SzDetailLevel         detailLevel,
      SzFeatureMode         featureMode,
      boolean               withFeatureStats,
      boolean               withInternalFeatures,
      boolean               withRelationships,
      Boolean               withRaw,
      long                  maxDuration)
  {
    if (testInfo != null && selfLink != null) {
      testInfo = testInfo + ", selfLink=[ " + selfLink + " ]";
    }

    validateBasics(testInfo, response, httpMethod, selfLink, maxDuration);

    SzWhyEntitiesResult whyResult = response.getData().getWhyResult();
    List<SzEntityData> entities = response.getData().getEntities();

    assertNotNull(whyResult, "Why result is null: " + testInfo);
    assertNotNull(entities, "Entities list is null: " + testInfo);

    assertTrue((entities.size() > 0), "No entities in entity list");

    Set<Long> entityIds = new LinkedHashSet<>();
    Set<SzRecordId> recordIds = new LinkedHashSet<>();
    for (SzEntityData entityData : entities) {
      SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();
      entityIds.add(resolvedEntity.getEntityId());
      for (SzMatchedRecord record : resolvedEntity.getRecords()) {
        recordIds.add(SzRecordId.FACTORY.create(record.getDataSource(),
                                                record.getRecordId()));
      }
    }

    if (entityId1 != null) {
      assertEquals(entityId1, whyResult.getEntityId1(),
                   "Unexpected first entity ID in why result: "
                       + testInfo);

      assertTrue(entityIds.contains(entityId1),
                 "First entity ID (" + entityId1 + ") not found "
                     + "entities list (" + entityIds + "): " + testInfo);
    }

    if (entityId2 != null) {
      assertEquals(entityId2, whyResult.getEntityId2(),
                   "Unexpected second entity ID in why result: "
                       + testInfo);

      assertTrue(entityIds.contains(entityId2),
                 "Second entity ID (" + entityId2 + ") not found "
                     + "entities list (" + entityIds + "): " + testInfo);
    }

    if (recordId1 != null && detailLevel != SUMMARY) {
      assertTrue(recordIds.contains(recordId1),
                 "First record ID (" + recordId1 + ") not present in "
                     + "record IDs of returned entities (" + recordIds + "): "
                     + testInfo);
    }
    if (recordId2 != null && detailLevel != SUMMARY) {
      assertTrue(recordIds.contains(recordId2),
                 "Second record ID (" + recordId2 + ") not present in "
                     + "record IDs of returned entities (" + recordIds + "): "
                     + testInfo);
    }

    // check the why result why key
    SzWhyMatchInfo matchInfo = whyResult.getMatchInfo();
    this.validateMatchInfo(testInfo, matchInfo);

    Map<Long, SzResolvedEntity> entityMap = new LinkedHashMap<>();
    entities.forEach(entityData -> {
      SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();
      entityMap.put(resolvedEntity.getEntityId(), resolvedEntity);
    });

    // verify the with relationships
    if (withRelationships) {
      for (SzEntityData entityData : entities) {
        List<SzRelatedEntity> relatedList = entityData.getRelatedEntities();
        assertNotEquals(0, relatedList.size(),
                        "Expected at least one related entity with "
                            + "withRelationships set to true: " + testInfo);

      }
    } else {
      for (SzEntityData entityData : entities) {
        List<SzRelatedEntity> relatedList = entityData.getRelatedEntities();
        assertFalse(relatedList != null && relatedList.size() > 0,
                    "Got unexpected related entities ("
                        + relatedList.size()
                        + ") when withRelationships set to false: " + testInfo);
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
                     !withRelationships,
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
                         "WHY_RESULTS", "ENTITIES");

      Object rawResults = ((Map) response.getRawData()).get("WHY_RESULTS");

      validateRawDataMapArray(testInfo,
                              rawResults,
                              true,
                              "ENTITY_ID",
                              "ENTITY_ID_2",
                              "MATCH_INFO");

      Object rawEntities = ((Map) response.getRawData()).get("ENTITIES");

      if (withRelationships) {
        validateRawDataMapArray(testInfo,
                                rawEntities,
                                true,
                                "RESOLVED_ENTITY",
                                "RELATED_ENTITIES");
      } else {
        validateRawDataMapArray(testInfo,
                                rawEntities,
                                true,
                                "RESOLVED_ENTITY");
      }

      for (Object entity : ((Collection) rawEntities)) {
          validateRawDataMap(
              testInfo,
              ((Map) entity).get("RESOLVED_ENTITY"),
              false,
              rawEntityKeys(forceMinimal, detailLevel, featureMode));
      }
    }
  }

  @ParameterizedTest
  @MethodSource("getWhyRecordsParameters")
  public void whyRecordsTest(SzRecordId     recordId1,
                             SzRecordId     recordId2,
                             Boolean        forceMinimal,
                             SzDetailLevel  detailLevel,
                             SzFeatureMode  featureMode,
                             Boolean        withFeatureStats,
                             Boolean        withInternalFeatures,
                             Boolean        withRelationships,
                             Boolean        withRaw)
  {
    this.performTest(() -> {
      String testInfo = "recordId1=[ " + recordId1
          + " ], recordId2=[ " + recordId2
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("why/records");

      buildWhyRecordsQueryString(sb,
                                 recordId1,
                                 recordId2,
                                 forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 withRelationships,
                                 withRaw);

      String uriText = this.formatServerUri(sb.toString());
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();

      SzWhyRecordsResponse response = this.whyServices.whyRecords(
          recordId1.getDataSourceCode(),
          recordId1.getRecordId(),
          recordId2.getDataSourceCode(),
          recordId2.getRecordId(),
          (forceMinimal == null ? false : forceMinimal),
          (detailLevel == null ? VERBOSE : detailLevel),
          (featureMode == null ? WITH_DUPLICATES : featureMode),
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          (withRaw == null ? false : withRaw),
          uriInfo);

      response.concludeTimers();
      long after = System.nanoTime();

      this.validateWhyRecordsResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId1,
          recordId2,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyRecordsParameters")
  public void whyRecordsViaHttpTest(
      SzRecordId    recordId1,
      SzRecordId    recordId2,
      Boolean       forceMinimal,
      SzDetailLevel detailLevel,
      SzFeatureMode featureMode,
      Boolean       withFeatureStats,
      Boolean       withInternalFeatures,
      Boolean       withRelationships,
      Boolean       withRaw)
  {
    this.performTest(() -> {
      String testInfo = "recordId1=[ " + recordId1
          + " ], recordId2=[ " + recordId2
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("why/records");

      buildWhyRecordsQueryString(sb,
                                 recordId1,
                                 recordId2,
                                 forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 withRelationships,
                                 withRaw);

      String uriText = this.formatServerUri(sb.toString());

      long before = System.nanoTime();
      SzWhyRecordsResponse response = this.invokeServerViaHttp(
          GET, uriText, SzWhyRecordsResponse.class);
      response.concludeTimers();
      long after = System.nanoTime();

      this.validateWhyRecordsResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId1,
          recordId2,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  @ParameterizedTest
  @MethodSource("getWhyRecordsParameters")
  public void whyRecordsTestViaJavaClient(
      SzRecordId    recordId1,
      SzRecordId    recordId2,
      Boolean       forceMinimal,
      SzDetailLevel detailLevel,
      SzFeatureMode featureMode,
      Boolean       withFeatureStats,
      Boolean       withInternalFeatures,
      Boolean       withRelationships,
      Boolean       withRaw)
  {
    this.performTest(() -> {
      String testInfo = "recordId1=[ " + recordId1
          + " ], recordId2=[ " + recordId2
          + " ], forceMinimal=[ " + forceMinimal
          + " ], detailLevel=[ " + detailLevel
          + " ], featureMode=[ " + featureMode
          + " ], withFeatureStats=[ " + withFeatureStats
          + " ], withInternalFeatures=[ " + withInternalFeatures
          + " ], withRelationships=[ " + withRelationships
          + " ], withRaw=[ " + withRaw + " ]";

      StringBuilder sb = new StringBuilder();
      sb.append("why/records");

      buildWhyRecordsQueryString(sb,
                                 recordId1,
                                 recordId2,
                                 forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 withRelationships,
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

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzWhyRecordsResponse clientResponse
          = this.entityDataApi.whyRecords(recordId1.getDataSourceCode(),
                                          recordId1.getRecordId(),
                                          recordId2.getDataSourceCode(),
                                          recordId2.getRecordId(),
                                          withRelationships,
                                          withFeatureStats,
                                          withInternalFeatures,
                                          clientDetailLevel,
                                          clientFeatureMode,
                                          forceMinimal,
                                          withRaw);
      long after = System.nanoTime();

      SzWhyRecordsResponse response = jsonCopy(clientResponse,
                                               SzWhyRecordsResponse.class);

      this.validateWhyRecordsResponse(
          testInfo,
          response,
          GET,
          uriText,
          recordId1,
          recordId2,
          (forceMinimal == null ? false : forceMinimal),
          detailLevel,
          featureMode,
          (withFeatureStats == null ? true : withFeatureStats),
          (withInternalFeatures == null ? true : withInternalFeatures),
          (withRelationships == null ? false : withRelationships),
          withRaw,
          after - before);
    });
  }

  public void validateWhyRecordsResponse(
      String                testInfo,
      SzWhyRecordsResponse  response,
      SzHttpMethod          httpMethod,
      String                selfLink,
      SzRecordId            recordId1,
      SzRecordId            recordId2,
      boolean               forceMinimal,
      SzDetailLevel         detailLevel,
      SzFeatureMode         featureMode,
      boolean               withFeatureStats,
      boolean               withInternalFeatures,
      boolean               withRelationships,
      Boolean               withRaw,
      long                  maxDuration)
  {
    if (testInfo != null && selfLink != null) {
      testInfo = testInfo + ", selfLink=[ " + selfLink + " ]";
    }

    validateBasics(testInfo, response, httpMethod, selfLink, maxDuration);

    SzWhyRecordsResult whyResult = response.getData().getWhyResult();
    List<SzEntityData> entities = response.getData().getEntities();

    assertNotNull(whyResult, "Why result is null: " + testInfo);
    assertNotNull(entities, "Entities list is null: " + testInfo);

    Set<SzRecordId> perspectiveIds = new LinkedHashSet<>();

    Set<SzFocusRecordId> recordIds1
        = whyResult.getPerspective1().getFocusRecords();
    Set<SzFocusRecordId> recordIds2
        = whyResult.getPerspective2().getFocusRecords();

    SzFocusRecordId focusRecord1 = SzFocusRecordId.FACTORY.create(
        recordId1.getDataSourceCode(), recordId1.getRecordId());
    SzFocusRecordId focusRecord2 = SzFocusRecordId.FACTORY.create(
        recordId2.getDataSourceCode(), recordId2.getRecordId());

    assertTrue(recordIds1.contains(focusRecord1),
               "Perspective 1 focus records (" + recordIds1
                   + ") does not contain first record ID ("
                   + recordId1 + "): " + testInfo);

    assertTrue(recordIds2.contains(focusRecord2),
               "Perspective 2 focus records (" + recordIds2
                   + ") does not contain first record ID ("
                   + recordId2 + "): " + testInfo);


    Set<SzRecordId> recordIds = new LinkedHashSet<>();
    for (SzEntityData entityData : entities) {
      SzResolvedEntity entity = entityData.getResolvedEntity();
      for (SzEntityRecord record : entity.getRecords()) {
        recordIds.add(SzRecordId.FACTORY.create(record.getDataSource(),
                                                record.getRecordId()));
      }
    }

    if (detailLevel != SUMMARY) {
      assertTrue(recordIds.contains(recordId1),
                 "First requested record ID (" + recordId1
                     + ") not represented in returned entities ("
                     + recordIds + "): " + testInfo);

      assertTrue(recordIds.contains(recordId2),
                 "Second requested record ID (" + recordId2
                     + ") not represented in returned entities ("
                     + recordIds + "): " + testInfo);
    }

    Map<Long, SzResolvedEntity> entityMap = new LinkedHashMap<>();
    entities.forEach(entityData -> {
      SzResolvedEntity resolvedEntity = entityData.getResolvedEntity();
      entityMap.put(resolvedEntity.getEntityId(), resolvedEntity);
    });

    // verify the with relationships
    if (withRelationships) {
      for (SzEntityData entityData : entities) {
        List<SzRelatedEntity> relatedList = entityData.getRelatedEntities();
        assertNotEquals(0, relatedList.size(),
                        "Expected at least one related entity with "
                            + "withRelationships set to true: " + testInfo);

      }
    } else {
      for (SzEntityData entityData : entities) {
        List<SzRelatedEntity> relatedList = entityData.getRelatedEntities();
        assertFalse(relatedList != null && relatedList.size() > 0,
                    "Got unexpected related entities ("
                        + relatedList.size()
                        + ") when withRelationships set to false: " + testInfo);
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
                     !withRelationships,
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
                         "WHY_RESULTS", "ENTITIES");

      Object rawResults = ((Map) response.getRawData()).get("WHY_RESULTS");

      validateRawDataMapArray(testInfo,
                              rawResults,
                              true,
                              "INTERNAL_ID",
                              "ENTITY_ID",
                              "FOCUS_RECORDS",
                              "INTERNAL_ID_2",
                              "ENTITY_ID_2",
                              "FOCUS_RECORDS_2",
                              "MATCH_INFO");

      Object rawEntities = ((Map) response.getRawData()).get("ENTITIES");

      if (withRelationships) {
        validateRawDataMapArray(testInfo,
                                rawEntities,
                                true,
                                "RESOLVED_ENTITY",
                                "RELATED_ENTITIES");
      } else {
        validateRawDataMapArray(testInfo,
                                rawEntities,
                                true,
                                "RESOLVED_ENTITY");
      }

      for (Object entity : ((Collection) rawEntities)) {
        validateRawDataMap(
            testInfo,
            ((Map) entity).get("RESOLVED_ENTITY"),
            false,
            rawEntityKeys(forceMinimal, detailLevel, featureMode));
      }
    }
  }

  private void validateMatchInfo(String testInfo, SzWhyMatchInfo matchInfo)
  {
    String whyKey = matchInfo.getWhyKey();

    Set<String> expectedTokens = new LinkedHashSet<>();
    for (List<SzFeatureScore> scores : matchInfo.getFeatureScores().values())
    {
      for (SzFeatureScore score: scores) {
        if (score.getScoringBucket() == SzScoringBucket.SAME) {
          expectedTokens.add("+" + score.getFeatureType());
        }
      }
    }

    for (SzDisclosedRelation relation : matchInfo.getDisclosedRelations()) {
      String relToken = "+" + relation.getDomain();
      expectedTokens.add(relToken);
      // if the WHY_KEY is constructed correctly for a REL_ANCHOR/REL_POINTER
      // and there are relationship roles then the why-key token for the
      // relationship will be followed by an open parentheses
      relToken = relToken + "(";

      // check if we have a REL_ANCHOR/REL_POINTER or REL_LINK relationship
      if ((whyKey.indexOf(relToken) >= 0)
          || relation.getDirection() != SzRelationDirection.BIDIRECTIONAL)
      {
        // NOTE: all REL_LINK relationships are bidirectional so if not
        // bidirectional then we are dealing with a REL_ANCHOR/REL_POINTER
        for (String role : relation.getRoles1()) {
          expectedTokens.add(role);
        }
        for (String role : relation.getRoles2()) {
          expectedTokens.add(role);
        }
      }
    }

    for (String token : expectedTokens) {
      assertTrue((whyKey.indexOf(token) >= 0),
                  "Missing expected token (" + token
                      + ") from why key (" + whyKey + "): " + testInfo);
    }
  }
}
