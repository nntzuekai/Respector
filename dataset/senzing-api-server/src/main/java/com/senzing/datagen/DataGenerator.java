package com.senzing.datagen;

import com.senzing.cmdline.*;
import com.senzing.configmgr.ConfigurationManager;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static com.senzing.cmdline.CommandLineUtilities.*;
import static com.senzing.datagen.RecordType.*;
import static com.senzing.datagen.FeatureType.*;
import static com.senzing.datagen.UsageType.*;
import static com.senzing.datagen.FeatureDensity.*;
import static com.senzing.util.LoggingUtilities.*;
import static com.senzing.datagen.DataGeneratorOption.*;
import static com.senzing.io.IOUtilities.*;

/**
 * Provides functionality to generate synthetic data.
 */
public class DataGenerator {
  private static final List<List<String>> GIVEN_NAMES;

  private static final List<String> SURNAMES;

  private static final List<List<String>> BUSINESS_PATTERNS;

  private static final List<List<String>> BUSINESS_NAMES;

  private static final List<List<String>> ORG_PATTERNS;

  private static final List<String> EMAIL_DOMAINS;

  private static final List<String> AREA_CODES;

  private static final List<String> STREET_NAMES;

  private static final List<String> STREET_SUFFIXES;

  private static final List<String> STREET_PREFIXES;

  private static final List<String> CITY_PREFIXES;

  private static final List<String> BUSINESS_USER_NAMES;

  private static final List<String> ORG_USER_NAMES;

  private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  private static final Random PRNG = new Random(
      (((long) "LJGSALJHGSDASD".hashCode() << 32)
          | ((long) "IUYTCKUWJFGS".hashCode() & 0xFFFFFFFFL)));

  private static int nextInt(Random prng, int bound) {
    synchronized (prng) {
      return prng.nextInt(bound);
    }
  }

  private static boolean nextBoolean(Random prng, double probability) {
    synchronized (prng) {
      return (prng.nextDouble() <= probability);
    }
  }

  private static <T> T nextItem(Random prng, List<T> list) {
    return list.get(nextInt(prng, list.size()));
  }

  private static <T> T nextItem(Random prng, T[] array) {
    return array[nextInt(prng, array.length)];
  }

  /**
   * Enumerates the various ways to format a user name from a name.
   */
  private enum UserNameFormat {
    FULL_NAME,
    FULL_NAME_WITH_DOT,
    FULL_NAME_REVERSE,
    FULL_NAME_REVERSE_WITH_DOT,
    INITIAL_THEN_SURNAME,
    SURNAME_THEN_INITIAL;

    public String generate(GeneratedName name) {
      return this.generate(name.getGivenName(), name.getSurname());
    }

    public String generate(String givenName, String surname) {
      switch (this) {
        case FULL_NAME:
          return givenName + surname;
        case FULL_NAME_WITH_DOT:
          return givenName + "." + surname;
        case FULL_NAME_REVERSE:
          return surname + givenName;
        case FULL_NAME_REVERSE_WITH_DOT:
          return surname + "." + givenName;
        case INITIAL_THEN_SURNAME:
          return givenName.substring(0, 1) + surname;
        case SURNAME_THEN_INITIAL:
          return surname + givenName.substring(0, 1);
        default:
          throw new IllegalStateException(
              "Unhandled UserNameFormat: " + this);
      }
    }
  }

  private static final List<UserNameFormat> USER_NAME_FORMATS
      = Collections.unmodifiableList(Arrays.asList(UserNameFormat.values()));

  private static List<String> readData(String fileName) {
    List<String> list = new LinkedList<>();
    Class<DataGenerator> dataGeneratorClass = DataGenerator.class;
    try (InputStream is = dataGeneratorClass.getResourceAsStream(fileName);
         InputStreamReader isr = new InputStreamReader(is, "UTF-8");
         BufferedReader br = new BufferedReader(isr))
    {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        line = line.trim();
        if (line.length() == 0 || line.startsWith("#")) continue;
        list.add(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // return an array list so it is fast to do sub lists and random access
    return new ArrayList<>(list);
  }

  private static List<List<String>> readListData(String fileName) {
    List<List<String>> result = new LinkedList<>();
    for (String items : readData(fileName)) {
      String[]      tokens  = items.split(",");
      List<String>  list    = new ArrayList<>(tokens.length);
      for (String name: tokens) {
        name = name.trim();
        list.add(name);
      }
      list = Collections.unmodifiableList(list);
      result.add(list);
    }
    return result;
  }

  static {
    List<List<String>>  givenNames      = null;
    List<String>        surnames        = null;
    List<List<String>>  bizNames        = null;
    List<List<String>>  bizPatterns     = null;
    List<List<String>>  orgPatterns     = null;
    List<String>        emailDomains    = null;
    List<String>        areaCodes       = null;
    List<String>        cityPrefixes    = null;
    List<String>        streetNames     = null;
    List<String>        bizUserNames    = null;
    List<String>        orgUserNames    = null;
    List<String>        streetSuffixes  = null;
    List<String>        streetPrefixes  = null;

    try {
      // get the given names (and related names)
      givenNames = readListData("given-names.txt");
      surnames = readData("surnames.txt");
      bizPatterns = readListData("biz-patterns.txt");
      bizNames = readListData("biz-names.txt");
      orgPatterns = readListData("org-patterns.txt");
      emailDomains = readData("email-domains.txt");
      areaCodes = readData("area-codes.txt");
      cityPrefixes = readData("city-prefixes.txt");
      streetNames = readData("street-names.txt");
      bizUserNames = readData("biz-users.txt");
      orgUserNames = readData("org-users.txt");
      streetPrefixes = readData("street-prefixes.txt");
      streetSuffixes = readData("street-suffixes.txt");

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      GIVEN_NAMES         = Collections.unmodifiableList(givenNames);
      SURNAMES            = Collections.unmodifiableList(surnames);
      BUSINESS_PATTERNS   = Collections.unmodifiableList(bizPatterns);
      BUSINESS_NAMES      = Collections.unmodifiableList(bizNames);
      ORG_PATTERNS        = Collections.unmodifiableList(orgPatterns);
      EMAIL_DOMAINS       = Collections.unmodifiableList(emailDomains);
      AREA_CODES          = Collections.unmodifiableList(areaCodes);
      CITY_PREFIXES       = Collections.unmodifiableList(cityPrefixes);
      STREET_NAMES        = Collections.unmodifiableList(streetNames);
      STREET_PREFIXES     = Collections.unmodifiableList(streetPrefixes);
      STREET_SUFFIXES     = Collections.unmodifiableList(streetSuffixes);
      BUSINESS_USER_NAMES = Collections.unmodifiableList(bizUserNames);
      ORG_USER_NAMES      = Collections.unmodifiableList(orgUserNames);
    }
  }

  /**
   * Converts a name to a domain name.
   */
  private static String nameToDomain(String name) {
    return name.replaceAll("'", "")
        .replaceAll("\\.", "")
        .replace(' ', '-')
        .replace('&', 'N')
        .toLowerCase() + ".com";
  }

  /**
   * The psuedo random number generator used for this instance.
   */
  private Random prng;

  /**
   * Constructs the instance with the specified seed.
   *
   * @param seed The random number seed for this instance.
   */
  public DataGenerator(long seed) {
    this.prng = new Random(seed);
  }

  private int nextInt(int bound) {
    return nextInt(this.prng, bound);
  }

  private boolean nextBoolean(double probability) {
    return nextBoolean(this.prng, probability);
  }

  private <T> T nextItem(List<T> list) {
    return nextItem(this.prng, list);
  }

  private <T> T nextItem(T[] array) {
    return nextItem(this.prng, array);
  }

  /**
   * Adds a single name with no usage type to the specified {@link
   * JsonObjectBuilder}.  If the {@link RecordType} is {@link RecordType#PERSON}
   * then the names are personal names, otherwise they are organization names.
   *
   * @param builder The {@link JsonObjectBuilder} to add the feature to.
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param fullValue <tt>true</tt> if the name should be added as a full
   *                  value and <tt>false</tt> if the name parts should be
   *                  added individually.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   * @return The {@link GeneratedName} describing the name that was added.
   */
  public GeneratedName addName(JsonObjectBuilder builder,
                               RecordType        recordType,
                               boolean           fullValue,
                               boolean           flatten)
  {
    Map<UsageType, GeneratedName> names = this.addNames(
        builder, recordType, null, GUARANTEED, fullValue, flatten);
    return names.get(null);
  }

  /**
   * Adds names of the specified usage types to the specified {@link
   * JsonObjectBuilder}.  If the {@link RecordType} is {@link RecordType#PERSON}
   * then the names are personal names, otherwise they are organization names.
   *
   * @param builder The {@link JsonObjectBuilder} to add the feature to.
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param usageTypes The usage types for the names or <tt>null</tt>
   *                   if only one name with no usage type should be generated.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   * @param fullValues <tt>true</tt> if the addresses should be added as full
   *                   values and <tt>false</tt> if the address parts should be
   *                   added individually.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   * @return The {@link Map} of {@link UsageType} keys to the associated {@link
   *         GeneratedName} value that was generated for that usage type.
   */
  public Map<UsageType, GeneratedName> addNames(
      JsonObjectBuilder   builder,
      RecordType          recordType,
      Set<UsageType>      usageTypes,
      FeatureDensity      featureDensity,
      boolean             fullValues,
      boolean             flatten)
  {
    Map<UsageType, GeneratedName> names
        = this.generateNames(recordType, usageTypes, featureDensity);

    this.addFeatures(builder, NAME, names, fullValues, flatten);

    return names;
  }

  /**
   * Generates names as if for the same record with the {@link UsageType}'s in
   * the specified {@link Set} (or a single name with no {@link UsageType}
   * if the specified {@link Set} is <tt>null</tt>) and returns a {@link
   * Map} of {@link UsageType} keys to the respective {@link GeneratedName}
   * value.  If the {@link RecordType} is {@link RecordType#PERSON} then the
   * names are personal names, otherwise they are organization names.
   *
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param usageTypes The usage types for the names or <tt>null</tt>
   *                   if only one name with no usage type should be generated.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   * @return The {@link Map} of {@link UsageType} keys to the associated {@link
   *         GeneratedName} value that was generated for that usage type.
   */
  public Map<UsageType, GeneratedName> generateNames(
      RecordType        recordType,
      Set<UsageType>    usageTypes,
      FeatureDensity    featureDensity)
  {
    featureDensity = (featureDensity == null) ? GUARANTEED : featureDensity;

    Set<UsageType> usageTypeSet = (usageTypes == null)
        ? Collections.singleton(null) : usageTypes;

    Map<UsageType,GeneratedName> result = new LinkedHashMap<>();

    List<String>        gnames        = null;
    List<String>        snames        = null;
    List<String>        names         = null;
    int                 distinctCount = 0;
    if (recordType == PERSON) {
      gnames = this.nextItem(GIVEN_NAMES);
      snames = (gnames.size() == 1 || (gnames.size() < 3 && this.nextInt(10) == 9))
          ? SURNAMES : Collections.singletonList(this.nextItem(SURNAMES));
      distinctCount = (gnames.size() * snames.size());
    } else {
      names = (recordType == ORGANIZATION)
          ? this.nextOrgNameList() : this.nextBizNameList();
      distinctCount = names.size();
    }

    for (UsageType usageType: usageTypeSet) {
      double probability = featureDensity.probability(result.size());
      if (!this.nextBoolean(probability)) continue;

      GeneratedName name;
      do {
        // generate the name
        name = (recordType == PERSON)
            ? new GeneratedName(this.nextItem(gnames), this.nextItem(snames))
            : new GeneratedName(this.nextItem(names),
                                (recordType == RecordType.BUSINESS));

      } while (distinctCount > result.size() && result.values().contains(name));

      result.put(usageType, name);
    }

    return result;
  }

  /**
   *
   */
  private class OrgPatterns {
    private int subCount;
    private String[] patterns;
  }

  /**
   *
   */
  private OrgPatterns processOrgPatterns(List<String> patternList) {
    String[] patterns = new String[patternList.size()];
    patterns = patternList.toArray(patterns);

    int     index   = patterns[0].indexOf(":");
    String  prefix  = patterns[0].substring(0, index);

    patterns[0] = patterns[0].substring(index+1);

    String[]  range = prefix.split("-");
    int       min   = Integer.parseInt(range[0].trim());
    int       max   = Integer.parseInt(range[1].trim());
    int       diff  = max - min;
    OrgPatterns result = new OrgPatterns();
    result.subCount = (diff == 0) ? min : min + nextInt(this.prng, diff);
    result.patterns = patterns;

    return result;
  }

  /**
   *
   */
  private String nextOrgEmailDomain() {
    List<String>  patternList = nextItem(this.prng, ORG_PATTERNS);
    OrgPatterns   orgPatterns = processOrgPatterns(patternList);

    String        pattern     = nextItem(orgPatterns.patterns);
    int           count       = orgPatterns.subCount;

    String sub = null;
    StringBuilder sb = new StringBuilder();
    for (int index2 = 0; index2 < count; index2++) {
      String ownerName = nextItem(this.prng, SURNAMES);
      String domainPart = (count == 1) ? ownerName : ownerName.substring(0,1);
      sb.append(domainPart.toLowerCase());
    }
    sub = sb.toString();

    // add the domain for the first one
    return nameToDomain(pattern.replaceAll("_", sub));
  }

  /**
   * Generates a list of organization name variants that are close to one
   * another.
   */
  private List<String> nextOrgNameList() {
    List<String>  patternList = nextItem(this.prng, ORG_PATTERNS);
    OrgPatterns   orgPatterns = processOrgPatterns(patternList);

    String sub = null;
    List<String> orgs = new ArrayList<>(orgPatterns.patterns.length);
    for (String pattern: orgPatterns.patterns) {
      // for the first pattern we need to handle the prefix
      if (sub == null) {
        String        sep   = "";
        StringBuilder sb    = new StringBuilder();
        int           count = orgPatterns.subCount;

        for (int index2 = 0; index2 < count; index2++) {
          String ownerName = nextItem(this.prng, SURNAMES);
          sb.append(sep).append(ownerName);
          sep = (index2 == (count - 2)) ? " & " : ", ";
        }
        sub = sb.toString();
      }
      String orgName = pattern.replaceAll(
          "_", (pattern.indexOf("_ &") < 0)
              ? sub : sub.replaceAll(" & ", ", "));

      orgs.add(orgName);
    }
    return orgs;
  }

  /**
   * Generates a list of business name variants that are close to one another.
   */
  private List<String> nextBizNameList() {
    int approxGenCount = GIVEN_NAMES.size() * BUSINESS_PATTERNS.size() * 3;
    int cannedCount = BUSINESS_NAMES.size();
    if (this.nextInt(approxGenCount+cannedCount) < cannedCount) {
      return this.nextItem(BUSINESS_NAMES);
    }
    List<String>  givenNames  = this.nextItem(GIVEN_NAMES);
    List<String>  patterns    = this.nextItem(BUSINESS_PATTERNS);
    List<String>  nameList    = new ArrayList<>(patterns.size());
    String        givenName   = nextItem(this.prng, givenNames);
    while (givenName.length() == 1) {
      givenName = nextItem(this.prng, givenNames);
    }
    for (String pattern: patterns) {
      pattern = pattern.toUpperCase();
      String sub = "_";
      if ((givenName.toUpperCase().endsWith("S") && pattern.indexOf("_'S")>=0))
      {
        givenName = givenName + "'";
        sub       = "_'S";
      }

      String bizName = pattern.replaceAll(sub, givenName);
      nameList.add(bizName);
    }
    return nameList;
  }

  /**
   *
   */
  private List<String> nextPhoneList() {
    String areaCode = nextItem(AREA_CODES);
    List<String> list = new ArrayList<>(50);
    for (int index1 = 0; index1 < 50; index1++) {
      String prefix = "";
      for (int index2 = 0; index2 < 3; index2++) {
        int digit = nextInt(this.prng, (index2 == 0) ? 9 : 10)
            + ((index2 == 0) ? 1 : 0);
        prefix = prefix + digit;
      }
      String exchange = "";
      for (int index2 = 0; index2 < 4; index2++) {
        exchange = exchange + nextInt(this.prng, 10);
      }
      list.add("(" + areaCode + ") " + prefix + "-" + exchange);
    }
    return list;
  }

  /**
   *
   */
  private List<String> nextAddressList() {
    String cityPrefix = this.nextItem(CITY_PREFIXES);

    List<String> list = new ArrayList<>(50);
    for (int index1 = 0; index1 < 50; index1++) {
      // setup the city, state and zip
      String cityStateZip = cityPrefix + nextInt(this.prng, 7)
          + nextInt(this.prng, 10);

      // get the street number
      String streetNumber = "";
      for (int index2 = 0; index2 < 4; index2++) {
        int digit = nextInt(this.prng, (index2 == 0) ? 9 : 10)
            + ((index2 == 0) ? 1 : 0);
        streetNumber = streetNumber + digit;
      }
      String street = streetNumber + " " + this.nextItem(STREET_PREFIXES)
          + " " + this.nextItem(STREET_NAMES) + " "
          + this.nextItem(STREET_SUFFIXES);

      // add the address
      list.add(street + "; " + cityStateZip);
    }
    return list;
  }

  /**
   * Adds a single full address with no usage type to the specified
   * {@link JsonObjectBuilder}.
   *
   * @param builder The {@link JsonObjectBuilder} to add the feature to.
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param fullValue <tt>true</tt> if the addresses should be added as full
   *                   values and <tt>false</tt> if the address parts should be
   *                   added individually.
   * @param fullValue <tt>true</tt> if the addresses should be added as full
   *                  values and <tt>false</tt> if the address parts should be
   *                  added individually.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   * @return The {@link GeneratedAddress} describing the address that was added.
   */
  public GeneratedAddress addAddress(JsonObjectBuilder  builder,
                                     RecordType         recordType,
                                     boolean            fullValue,
                                     boolean            flatten)
  {
    Map<UsageType, GeneratedAddress> addresses = this.addAddresses(
        builder, recordType,null, GUARANTEED, fullValue, flatten);
    return addresses.get(null);
  }

  /**
   * Adds full addresses of the specified usage types to the specified
   * {@link JsonObjectBuilder}.
   *
   * @param builder The {@link JsonObjectBuilder} to add the feature to.
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param usageTypes The usage types for the addresses or <tt>null</tt>
   *                   if only one address with no usage type should be generated.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   * @param fullValues <tt>true</tt> if the addresses should be added as full
   *                   values and <tt>false</tt> if the address parts should be
   *                   added individually.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   * @return The {@link Map} of {@link UsageType} keys to the associated {@link
   *         GeneratedAddress} value that was generated for that usage type.
   *
   * @return The {@link List} of addresses that were added.
   */
  public Map<UsageType, GeneratedAddress> addAddresses(
      JsonObjectBuilder   builder,
      RecordType          recordType,
      Set<UsageType>      usageTypes,
      FeatureDensity      featureDensity,
      boolean             fullValues,
      boolean             flatten)
  {
    Map<UsageType, GeneratedAddress> addresses
        = this.generateAddresses(recordType, usageTypes, featureDensity);

    this.addFeatures(builder, ADDRESS, addresses, fullValues, flatten);

    return addresses;
  }

  /**
   * Generates addresses as if for the same record with the {@link UsageType}'s
   * in the specified {@link Set} (or a single address with no {@link
   * UsageType} if the specified {@link Set} is <tt>null</tt>) and returns
   * a {@link Map} of {@link UsageType} keys to the respective {@link
   * GeneratedAddress} value.
   *
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param usageTypes The usage types for the addresses or <tt>null</tt>
   *                   if only one address with no usage type should be generated.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   * @return The {@link Map} of {@link UsageType} keys to the associated {@link
   *         GeneratedAddress} value that was generated for that usage type.
   */
  public Map<UsageType, GeneratedAddress> generateAddresses(
      RecordType      recordType,
      Set<UsageType>  usageTypes,
      FeatureDensity  featureDensity)
  {
    featureDensity = (featureDensity == null) ? GUARANTEED : featureDensity;

    Set<UsageType> usageTypeSet = (usageTypes == null)
        ? Collections.singleton(null)  : usageTypes;

    Map<UsageType, GeneratedAddress> result = new LinkedHashMap<>();

    List<String> addressList = this.nextAddressList();

    for (UsageType usageType: usageTypeSet) {
      double probability = featureDensity.probability(result.size());
      if (!this.nextBoolean(probability)) continue;

      String addressText = this.nextItem(addressList);
      GeneratedAddress address = new GeneratedAddress(recordType, addressText);
      result.put(usageType, address);
    }

    return result;
  }

  /**
   * Adds a single phone number with no usage type to the specified
   * {@link JsonObjectBuilder}.
   *
   * @param builder The {@link JsonObjectBuilder} to add the feature to.
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   * @return The {@link GeneratedPhone} describing the phone number that was
   *         added.
   */
  public GeneratedPhone addPhoneNumber(JsonObjectBuilder builder,
                                       RecordType        recordType,
                                       boolean           flatten)
  {
    return this.addPhoneNumbers(
        builder, recordType,null, GUARANTEED, flatten).get(null);
  }

  /**
   * Adds phone numbers of the specified usage types to the specified
   * {@link JsonObjectBuilder}.
   *
   * @param builder The {@link JsonObjectBuilder} to add the feature to.
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param usageTypes The usage types for the phone numbers or <tt>null</tt>
   *                   if only one phone number with no usage type should be
   *                   added.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   * @return The {@link List} of phone numbers that were added.
   */
  public Map<UsageType, GeneratedPhone> addPhoneNumbers(
      JsonObjectBuilder builder,
      RecordType        recordType,
      Set<UsageType>    usageTypes,
      FeatureDensity    featureDensity,
      boolean           flatten)
  {
    Map<UsageType,GeneratedPhone> phones
        = this.generatePhoneNumbers(recordType, usageTypes, featureDensity);

    this.addFeatures(builder, PHONE, phones, true, flatten);

    return phones;
  }

  /**
   * Generates phone numbers as if for the same record with the {@link
   * UsageType}'s in the specified {@link Set} (or a single phone number
   * with no {@link UsageType} if the specified {@link Set} is
   * <tt>null</tt>) and returns a {@link Map} of {@link UsageType} keys to the
   * respective {@link GeneratedPhone} value.
   *
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param usageTypes The usage types for the phone numbers or <tt>null</tt>
   *                   if only one phone number with no usage type should be
   *                   generated.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   * @return The {@link Map} of {@link UsageType} keys to the associated {@link
   *         GeneratedPhone} value that was generated for that usage type.
   */
  public Map<UsageType, GeneratedPhone> generatePhoneNumbers(
      RecordType        recordType,
      Set<UsageType>    usageTypes,
      FeatureDensity    featureDensity)
  {
    featureDensity = (featureDensity == null) ? GUARANTEED : featureDensity;

    Set<UsageType> usageTypeSet = (usageTypes == null)
        ? Collections.singleton(null)  : usageTypes;

    Map<UsageType, GeneratedPhone> result = new LinkedHashMap<>();

    List<String> phoneNumbers = this.nextPhoneList();

    for (UsageType usageType: usageTypeSet) {
      double probability = featureDensity.probability(result.size());
      if (!this.nextBoolean(probability)) continue;

      String phoneText = this.nextItem(phoneNumbers);
      GeneratedPhone phone = new GeneratedPhone(recordType, phoneText);
      result.put(usageType, phone);
    }

    return result;
  }

  /**
   * Adds a single email address with no usage type to the specified
   * {@link JsonObjectBuilder}.
   *
   * @param builder The {@link JsonObjectBuilder} to add the feature to.
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param name The {@link GeneratedName} to use to create the user name.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   * @return The {@link GeneratedEmail} describing the email address that
   *         was added.
   */
  public GeneratedEmail addEmailAddress(JsonObjectBuilder builder,
                                        RecordType        recordType,
                                        GeneratedName     name,
                                        boolean           flatten)
  {
    List<GeneratedName> names = Collections.singletonList(name);
    Map<UsageType, GeneratedEmail> map = this.addEmailAddresses(
        builder, recordType, names,null, GUARANTEED, flatten);
    return map.get(null);
  }

  /**
   * Adds email addresses of the specified usage types to the specified
   * {@link JsonObjectBuilder}.  Depending on the specified {@link RecordType}
   * either the user names OR email domains will be based on the specified
   * {@link Collection} of names.
   *
   * @param builder The {@link JsonObjectBuilder} to add the properties to.
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param names The {@link List} of {@link GeneratedName} instances to base the email
   *              address user names off.
   * @param usageTypes The usage types for the email addresses or <tt>null</tt>
   *                   if only one email address with no usage type should be
   *                   added.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   *
   * @return The {@link Map} of {@link UsageType} keys to the associated {@link
   *         GeneratedEmail} value that was generated for that usage type.
   */
  public Map<UsageType, GeneratedEmail> addEmailAddresses(
      JsonObjectBuilder         builder,
      RecordType                recordType,
      Collection<GeneratedName> names,
      Set<UsageType>            usageTypes,
      FeatureDensity            featureDensity,
      boolean                   flatten)
  {
    Map<UsageType, GeneratedEmail> emails
        = this.generateEmailAddresses(
            recordType, names, usageTypes, featureDensity);

    this.addFeatures(builder, EMAIL, emails, true, flatten);

    return emails;
  }

  /**
   * Generates email addresses as if for the same record with the {@link
   * UsageType}'s in the specified {@link Set} (or a single email address
   * with no {@link UsageType} if the specified {@link Set} is
   * <tt>null</tt>) and returns a {@link Map} of {@link UsageType} keys to the
   * respective {@link GeneratedEmail} value.  The specified {@link Collection}
   * of {@link GeneratedName} values is used to determine the user names or
   * email domains for the email address (depending on the record type).
   *
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param names The {@link Collection} of {@link GeneratedName} instances to
   *              base the email address user names or email domains off of
   *              (depending on the record type).
   * @param usageTypes The usage types for the email addresses or <tt>null</tt>
   *                   if only one email address with no usage type should be
   *                   generated.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   * @return The {@link Map} of {@link UsageType} keys to the associated {@link
   *         GeneratedEmail} value that was generated for that usage type.
   */
  public Map<UsageType,GeneratedEmail> generateEmailAddresses(
      RecordType                recordType,
      Collection<GeneratedName> names,
      Set<UsageType>            usageTypes,
      FeatureDensity            featureDensity)
  {
    featureDensity = (featureDensity == null) ? GUARANTEED : featureDensity;

    // require the names
    if (usageTypes == null || usageTypes.size() > 0) {
      Objects.requireNonNull(names, "The collection of names cannot be null");
      if (names.size() == 0) {
        throw new IllegalArgumentException("The collection of names cannot be empty");
      }
    }

    // create a list of the names
    List<GeneratedName> nameList = new ArrayList<>(names);

    Set<UsageType> usageTypeSet = (usageTypes == null)
        ? Collections.singleton(null)  : usageTypes;

    Map<UsageType, GeneratedEmail> result = new LinkedHashMap<>();

    List<String> usedDomains = new ArrayList<>(usageTypeSet.size());

    for (UsageType usageType: usageTypeSet) {
      double probability = featureDensity.probability(result.size());
      if (!this.nextBoolean(probability)) continue;

      GeneratedName name = (recordType == PERSON)
          ? this.nextItem(nameList) : nameList.get(0);
      String          userName    = null;
      String          emailDomain = null;
      switch (recordType) {
        case PERSON:
          UserNameFormat userFormat = this.nextItem(USER_NAME_FORMATS);
          userName = userFormat.generate(name).toLowerCase();
          do {
            emailDomain = (usageType == WORK) ? this.nextOrgEmailDomain()
                : this.nextItem(EMAIL_DOMAINS);

          } while (usedDomains.contains(emailDomain));
          usedDomains.add(emailDomain);
          break;
        case ORGANIZATION:
          emailDomain = nameToDomain(name.toString());
          userName = this.nextItem(ORG_USER_NAMES);
          break;
        case BUSINESS:
          emailDomain = nameToDomain(name.toString());
          userName = this.nextItem(BUSINESS_USER_NAMES);
          break;
        default:
          throw new IllegalArgumentException(
              "Unhandled record type: " + recordType);
      }

      String emailAddress = userName + "@" + emailDomain;
      GeneratedEmail email = new GeneratedEmail(recordType, emailAddress);

      result.put(usageType, email);
    }
    return result;
  }

  /**
   * Adds email addresses of the specified usage types to the specified
   * {@link JsonObjectBuilder}.  Depending on the specified {@link RecordType}
   * either the user names OR email domains will be based on the specified
   * {@link Collection} of names.
   *
   * @param builder The {@link JsonObjectBuilder} to add the properties to.
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   *
   * @return The {@link Map} of {@link UsageType} keys to the associated {@link
   *         GeneratedEmail} value that was generated for that usage type.
   */
  public GeneratedBirthDate addBirthDate(
      JsonObjectBuilder         builder,
      RecordType                recordType,
      FeatureDensity            featureDensity)
  {
    GeneratedBirthDate birthDate
        = this.generateBirthDate(recordType, featureDensity);

    this.addFeatures(builder, BIRTH_DATE,
                     Collections.singletonMap(null, birthDate),
                     true, true);

    return birthDate;
  }

  /**
   * Generates a birth date within the last 90 years with younger and older
   * birthdates being less likely than those between 18 and 70 years old.
   *
   * @param recordType The non-null {@link RecordType} describing the type of
   *                   record being created.
   * @param featureDensity The {@link FeatureDensity} describing how to densely
   *                       the generated values should be populated.
   * @return The {@link GeneratedBirthDate} value that was generated.
   */
  public GeneratedBirthDate generateBirthDate(RecordType      recordType,
                                              FeatureDensity  featureDensity)
  {
    // check the record type
    GeneratedAttributeType attrType
        = GeneratedAttributeType.fullValueInstance(BIRTH_DATE, recordType);
    if (attrType == null) {
      throw new IllegalArgumentException(
          "Unsupported record type for the BIRTH_DATE feature: " + recordType);
    }

    featureDensity = (featureDensity == null) ? GUARANTEED : featureDensity;

    double probability = featureDensity.probability(0);
    if (!this.nextBoolean(probability)) return null;

    LocalDate today       = LocalDate.now();

    int       rand1       = this.nextInt(100);
    int       rand2       = this.nextInt(60) + 20;
    int       rand3       = this.nextInt(20) + 30;
    int       yearsAgo    = (rand1 + rand2 + rand3) / 3;

    Month     month       = Month.of(this.nextInt(12) + 1);
    int       dayOfMonth  = this.nextInt(month.length(false)) + 1;
    int       year        = today.getYear() - yearsAgo;

    return new GeneratedBirthDate(month.getValue(), dayOfMonth, year);
  }

  /**
   * Utility method for adding the generated feature to either the specified
   * {@link JsonObjectBuilder} or {@link JsonArrayBuilder} as a new {@link
   * JsonObjectBuilder}.
   */
  private void addFeatures(
      JsonObjectBuilder                           builder,
      FeatureType                                 featureType,
      Map<UsageType, ? extends GeneratedFeature>  featureMap,
      boolean                                     fullValues,
      boolean                                     flatten)
  {
    // if no plural property then flatten since there is no other choice
    if (featureType.getPluralProperty() == null) flatten = true;

    // if not flattening then create an array builder
    JsonArrayBuilder jab = (flatten) ? null : Json.createArrayBuilder();

    int[] addCount = { 0 };

    final boolean flattened = flatten;
    featureMap.entrySet().forEach(entry -> {
      UsageType usageType = entry.getKey();
      GeneratedFeature feature = entry.getValue();
      if (feature == null) return;

      if (flattened) {
        if (fullValues) {
          feature.addFull(builder, usageType,true);
        } else {
          feature.addParts(builder, usageType, true);
        }

      } else {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (fullValues) {
          feature.addFull(job, usageType, false);
        } else {
          feature.addParts(job, usageType, false);
        }
        addCount[0]++;
        jab.add(job);
      }
    });

    // add the nested array if not flattening the JSON
    if (!flatten && addCount[0] > 0) {
      builder.add(featureType.getPluralProperty(), jab);
    }
  }

  /**
   * Generates a single record with the specified {@link RecordType},
   * optional record ID, optional data source, and optional feature generation
   * {@link Map} of {@link FeatureType} keys indicating the feature types of
   * each feature that should be generated and {@link Set} values containing
   * {@link UsageType} instances to indicate how many features of that type
   * should be generated and with which usage types with <tt>null</tt> values
   * being interpretted as a single feature of that type with no {@link
   * UsageType}.
   *
   * @param recordType The {@link RecordType} describing the type of record
   *                   being generated.
   * @param recordId The optional record ID, or <tt>null</tt> to omit the
   *                 record ID from the generated record.
   * @param dataSource The optional data source, or <tt>null</tt> to omit the
   *                   data source from the generated record.
   * @param featureGenMap The {@link Map} of {@link FeatureType} keys to {@link
   *                      Set} values of {@link UsageType} describing the
   *                      features to be added to the record, or <tt>null</tt>
   *                      if no features.
   * @param fullValues <tt>true</tt> if the features should be added as full
   *                   values and <tt>false</tt> if the feature parts should be
   *                   added individually.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   */
  public JsonObjectBuilder generateRecord(
      RecordType                        recordType,
      String                            recordId,
      String                            dataSource,
      Map<FeatureType, Set<UsageType>>  featureGenMap,
      Map<FeatureType, FeatureDensity>  featureDensityMap,
      boolean                           fullValues,
      boolean                           flatten)
  {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    if (featureDensityMap == null) featureDensityMap = Collections.emptyMap();

    // check if we need to add a record ID
    if (recordId != null) {
      builder.add("RECORD_ID", recordId);
    }

    // check if a data source was specified
    if (dataSource != null) {
      builder.add("DATA_SOURCE", dataSource);
    }

    // end early if the feature generation map is null
    if (featureGenMap == null) return builder;

    Map<UsageType, GeneratedName> names = null;

    // check if generating names
    if (featureGenMap.containsKey(NAME)) {
      Set<UsageType> nameTypes = featureGenMap.get(NAME);

      FeatureDensity density = featureDensityMap.get(NAME);

      // add the names for the specified usage types
      names = this.addNames(
          builder, recordType, nameTypes, density, fullValues, flatten);
    }

    if (featureGenMap.containsKey(BIRTH_DATE)) {
      // check if birth date is applicable to the record type
      GeneratedAttributeType attrType
          = GeneratedAttributeType.fullValueInstance(BIRTH_DATE, recordType);
      if (attrType == null) {
        throw new IllegalArgumentException(
            "Unsupported record type for the BIRTH_DATE feature: "
                + recordType);
      }
      Set<UsageType> birthDateTypes = featureGenMap.get(BIRTH_DATE);

      FeatureDensity density = featureDensityMap.get(BIRTH_DATE);

      // add the names for the specified usage types
      this.addBirthDate(builder, recordType, density);
    }

    // check if generating addresses
    if (featureGenMap.containsKey(ADDRESS)) {
      Set<UsageType> addressTypes = featureGenMap.get(ADDRESS);

      FeatureDensity density = featureDensityMap.get(ADDRESS);

      // add the addresses for the specified usage types
      this.addAddresses(
          builder, recordType, addressTypes, density, fullValues, flatten);
    }

    // check if generating phone numbers
    if (featureGenMap.containsKey(PHONE)) {
      Set<UsageType> phoneTypes = featureGenMap.get(PHONE);

      FeatureDensity density = featureDensityMap.get(PHONE);

      // add the phone numbers for the specified usage types
      this.addPhoneNumbers(
          builder, recordType, phoneTypes, density, flatten);
    }

    // check if generating email addresses
    if (featureGenMap.containsKey(EMAIL)) {
      Set<UsageType> emailTypes = featureGenMap.get(EMAIL);

      // check if we do not have names
      if (names == null) {
        // generate a least one name
        names = this.generateNames(recordType, null, GUARANTEED);
      }

      FeatureDensity density = featureDensityMap.get(EMAIL);

      // add the email addresses for the specified usage types
      this.addEmailAddresses(builder,
                             recordType,
                             names.values(),
                             emailTypes,
                             density,
                             flatten);
    }

    // return the specified builder
    return builder;
  }

  /**
   * Generates the next record ID with a random alphabetic prefix and using the
   * specified record index as the suffix.
   *
   * @param recordIndex The record index.
   * @param recordCount The total number of records being generated.
   */
  private String nextRecordId(
      RecordType recordType, int recordIndex, int recordCount)
  {
    try {
      int    digits   = Math.max(((int) Math.log10((double) recordCount)) + 1, 4);
      int    base     = (int) Math.pow(10, digits);

      // make a prefix out of the record type
      String prefix = Base64.getEncoder().encodeToString(
              recordType.toString().getBytes(UTF_8))
          .replaceAll("=", "").toUpperCase();
      if (prefix.length() > 8) {
        prefix = prefix.substring(0, 8);
      }

      // now append an alphabetic prefix
      int alphaIndex = this.nextInt(ALPHABET.length() - 3);
      prefix += "-" + ALPHABET.substring(alphaIndex, alphaIndex + 3)
          + "-" + (this.nextInt(100000) + 100000) + "-";

      // now attach the record index
      return (prefix + (base + recordIndex));

    } catch (UnsupportedEncodingException cannotHappen) {
      throw new IllegalStateException("UTF-8 encoding not supported");
    }
  }

  /**
   * Generates the specified number of records with the specified {@link
   * RecordType}, {@link List} of data sources and optional feature generation
   * {@link Map} of {@link FeatureType} keys indicating the feature types of
   * each feature that should be generated and {@link Set} values containing
   * {@link UsageType} instances to indicate how many features of that type
   * should be generated and with which usage types with <tt>null</tt> values
   * being interpreted as a single feature of that type with no {@link
   * UsageType}.
   *
   * @param recordType The {@link RecordType} describing the type of record
   *                   being generated.
   * @param recordCount The non-negative number of records to generate.
   * @param dataSource The optional data source for each record, or
   *                   <tt>null</tt> to omit the data source from the generated
   *                   records.
   * @param featureGenMap The {@link Map} of {@link FeatureType} keys to {@link
   *                      Set} values of {@link UsageType} describing the
   *                      features to be added to the record, or <tt>null</tt>
   *                      if no features.
   * @param featureDensityMap The {@link Map} of {@link FeatureType} keys to
   *                          {@link FeatureDensity} values describing the
   *                          density for each feature.
   * @param fullValues <tt>true</tt> if the features should be added as full
   *                   values and <tt>false</tt> if the feature parts should be
   *                   added individually.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   */
  public void generateRecords(
      RecordHandler                     recordHandler,
      RecordType                        recordType,
      int                               recordCount,
      boolean                           includeRecordIds,
      String                            dataSource,
      Map<FeatureType, Set<UsageType>>  featureGenMap,
      Map<FeatureType, FeatureDensity>  featureDensityMap,
      boolean                           fullValues,
      boolean                           flatten)
  {
    if (featureDensityMap == null) featureDensityMap = Collections.emptyMap();

    // check the record count
    if (recordCount < 0) {
      throw new IllegalArgumentException(
          "The specified record count cannot be negative: " + recordCount);
    }
    // check if no records
    if (recordCount == 0) return;

    // iterate and create the records
    for (int index = 0; index < recordCount; index++) {
      // get the next record ID if neccessary
      String recordId = null;
      if (includeRecordIds) {
        recordId = this.nextRecordId(recordType, index,  recordCount);
      }

      // build the record
      JsonObjectBuilder record = this.generateRecord(recordType,
                                                     recordId,
                                                     dataSource,
                                                     featureGenMap,
                                                     featureDensityMap,
                                                     fullValues,
                                                     flatten);

      recordHandler.handle(record);
    }
  }

  /**
   * Generates the specified number of records with the specified {@link
   * RecordType}, {@link List} of data sources and optional feature generation
   * {@link Map} of {@link FeatureType} keys indicating the feature types of
   * each feature that should be generated and {@link Set} values containing
   * {@link UsageType} instances to indicate how many features of that type
   * should be generated and with which usage types with <tt>null</tt> values
   * being interpreted as a single feature of that type with no {@link
   * UsageType}.
   *
   * @param recordType The {@link RecordType} describing the type of record
   *                   being generated.
   * @param recordCount The non-negative number of records to generate.
   * @param dataSources The optional {@link List} of data sources from which to
   *                    randomly select a data source for each record, or
   *                    <tt>null</tt> to omit the data source from the generated
   *                    records.
   * @param featureGenMap The {@link Map} of {@link FeatureType} keys to {@link
   *                      Set} values of {@link UsageType} describing the
   *                      features to be added to the record, or <tt>null</tt>
   *                      if no features.
   * @param featureDensityMap The {@link Map} of {@link FeatureType} keys to
   *                          {@link FeatureDensity} values describing the
   *                          density for each feature.
   * @param fullValues <tt>true</tt> if the features should be added as full
   *                   values and <tt>false</tt> if the feature parts should be
   *                   added individually.
   * @param flatten <tt>true</tt> if flat JSON should be used and
   *                <tt>false</tt> if features should be added to sub-arrays
   *                based on the feature name.
   */
  public void generateRecords(
      RecordHandler                     recordHandler,
      RecordType                        recordType,
      int                               recordCount,
      boolean                           includeRecordIds,
      List<String>                      dataSources,
      Map<FeatureType, Set<UsageType>>  featureGenMap,
      Map<FeatureType, FeatureDensity>  featureDensityMap,
      boolean                           fullValues,
      boolean                           flatten)
  {
    // check the record count
    if (recordCount < 0) {
      throw new IllegalArgumentException(
          "The specified record count cannot be negative: " + recordCount);
    }

    if (featureDensityMap == null) featureDensityMap = Collections.emptyMap();

    // check if no records
    if (recordCount == 0) return;

    // iterate and create the records
    for (int index = 0; index < recordCount; index++) {
      // get the next record ID if neccessary
      String recordId = null;
      if (includeRecordIds) {
        recordId = this.nextRecordId(recordType, index, recordCount);
      }

      // get the next data source
      String dataSource = (dataSources == null || dataSources.size() == 0)
          ? null : this.nextItem(dataSources);

      // build the record
      JsonObjectBuilder record = this.generateRecord(recordType,
                                                     recordId,
                                                     dataSource,
                                                     featureGenMap,
                                                     featureDensityMap,
                                                     fullValues,
                                                     flatten);

      recordHandler.handle(record);
    }
  }

  /**
   *
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    Map<CommandLineOption, Object>  options = null;
    List<DeprecatedOptionWarning>   warnings = new LinkedList<>();
    try {
      options = parseCommandLine(args, warnings);

      for (DeprecatedOptionWarning warning: warnings) {
        System.out.println(warning);
        System.out.println();
      }

    } catch (CommandLineException e) {
      System.out.println(e.getMessage());

      System.out.println(DataGenerator.getUsageString(true));
      System.exit(1);

    } catch (Exception e) {
      if (!isLastLoggedException(e))
      {
        e.printStackTrace();
      }
      System.exit(1);
    }

    if (options.containsKey(HELP)) {
      System.out.println(DataGenerator.getUsageString(true));
      System.exit(0);
    }

    Integer personCount = (Integer) options.get(PERSON_COUNT);
    Integer orgCount    = (Integer) options.get(ORGANIZATION_COUNT);
    Integer bizCount    = (Integer) options.get(BUSINESS_COUNT);

    Set<RecordType> recordTypes = new LinkedHashSet<>();
    int totalCount = 0;
    if (personCount != null) {
      recordTypes.add(PERSON);
      totalCount += personCount.intValue();
    }
    if (orgCount != null) {
      recordTypes.add(ORGANIZATION);
      totalCount += orgCount.intValue();
    }
    if (bizCount != null) {
      recordTypes.add(RecordType.BUSINESS);
      totalCount += bizCount.intValue();
    }

    Map<RecordType, Integer> recordCounts = new LinkedHashMap<>();
    if (personCount != null)  recordCounts.put(PERSON,  personCount);
    if (orgCount != null)     recordCounts.put(ORGANIZATION, orgCount);
    if (bizCount != null)     recordCounts.put(RecordType.BUSINESS, bizCount);

    // check if total count is zero
    if (totalCount == 0) {
      System.err.println("At least one of the "
         + PERSON_COUNT.getCommandLineFlag() + ", "
         + ORGANIZATION_COUNT.getCommandLineFlag() + " and "
         + BUSINESS_COUNT.getCommandLineFlag() + " options must be specified.");
      System.exit(1);
    }

    // get the data sources
    List<String> personSources = (List<String>) options.get(PERSON_SOURCES);
    List<String> orgSources = (List<String>) options.get(ORGANIZATION_SOURCES);
    List<String> bizSources = (List<String>) options.get(BUSINESS_SOURCES);

    boolean includeSources
        = ((personSources != null && personSources.size() > 0)
           || (orgSources != null && orgSources.size() > 0)
           || (bizSources != null && bizSources.size() > 0));

    Map<RecordType, List<String>> recordSources = new LinkedHashMap<>();
    recordSources.put(PERSON, personSources);
    recordSources.put(ORGANIZATION, orgSources);
    recordSources.put(RecordType.BUSINESS, bizSources);

    // get the random number seed (if any)
    Long seed = (Long) options.get(SEED);
    if (seed == null) seed = 0L;
    Random prng = new Random(seed);

    // determine the default feature counts
    int defaultMaxNames       = prng.nextInt(2) + 1;
    int defaultMaxBirthDates  = 1;
    int defaultMaxAddresses   = prng.nextInt(3) + 1;
    int defaultMaxPhones      = prng.nextInt(3) + 1;
    int defaultMaxEmails      = prng.nextInt(3) + 1;

    // check if setting all defaults to zero (0)
    if (Boolean.TRUE.equals(options.get(DEFAULT_NO_FEATURES))) {
      defaultMaxNames       = 0;
      defaultMaxBirthDates  = 0;
      defaultMaxAddresses   = 0;
      defaultMaxPhones      = 0;
      defaultMaxEmails      = 0;
    }

    // get the maximum name count
    Integer maxNames = (Integer) options.get(MAX_NAME_COUNT);
    if (maxNames == null) maxNames = defaultMaxNames;

    // get the maximum birth date count
    Integer maxBirthDates = (Integer) options.get(MAX_BIRTH_DATE_COUNT);
    if (maxBirthDates == null) maxBirthDates = defaultMaxBirthDates;

    // get the maximum address count
    Integer maxAddresses = (Integer) options.get(MAX_ADDRESS_COUNT);
    if (maxAddresses == null) maxAddresses = defaultMaxAddresses;

    // get the maximum phone count
    Integer maxPhones = (Integer) options.get(MAX_PHONE_COUNT);
    if (maxPhones == null) maxPhones = defaultMaxPhones;

    // get the maximum email count
    Integer maxEmails = (Integer) options.get(MAX_EMAIL_COUNT);
    if (maxEmails == null) maxEmails = defaultMaxEmails;

    Map<FeatureType, FeatureDensity> densityMap = new LinkedHashMap<>();

    // get the name density
    FeatureDensity nameDensity = (FeatureDensity) options.get(NAME_DENSITY);
    if (nameDensity == null) nameDensity = FIRST_THEN_SPARSE;

    // get the birth date density
    FeatureDensity birthDateDensity
        = (FeatureDensity) options.get(BIRTH_DATE_DENSITY);
    if (birthDateDensity == null) birthDateDensity = COMMON;

    // get the address density
    FeatureDensity addrDensity = (FeatureDensity) options.get(ADDRESS_DENSITY);
    if (addrDensity == null) addrDensity = COMMON;

    // get the phone density
    FeatureDensity phoneDensity = (FeatureDensity) options.get(PHONE_DENSITY);
    if (phoneDensity == null) phoneDensity = COMMON;

    // get the phone density
    FeatureDensity emailDensity = (FeatureDensity) options.get(EMAIL_DENSITY);
    if (emailDensity == null) emailDensity = COMMON;

    // setup the density map
    densityMap.put(NAME, nameDensity);
    densityMap.put(BIRTH_DATE, birthDateDensity);
    densityMap.put(ADDRESS, addrDensity);
    densityMap.put(PHONE, phoneDensity);
    densityMap.put(EMAIL, emailDensity);

    // create the data generator
    DataGenerator dataGenerator = new DataGenerator(seed);

    // get the overwrite option
    Boolean withRecordIds = Boolean.TRUE.equals(options.get(WITH_RECORD_IDS));
    Boolean overwrite     = Boolean.TRUE.equals(options.get(OVERWRITE));
    Boolean flatten       = Boolean.TRUE.equals(options.get(FLATTEN));
    Boolean fullValues    = Boolean.TRUE.equals(options.get(FULL_VALUES));
    Boolean prettyPrint   = Boolean.TRUE.equals(options.get(PRETTY_PRINT));

    Map<RecordType, Map<FeatureType, Set<UsageType>>> featureGenMaps
        = new LinkedHashMap<>();

    for (RecordType recordType: recordTypes) {
      Map<FeatureType, Set<UsageType>> featureGenMap = new LinkedHashMap<>();

      featureGenMap.put(
          NAME, usageTypesFor(NAME, recordType, maxNames, true));

      GeneratedAttributeType attrType
          = GeneratedAttributeType.fullValueInstance(BIRTH_DATE, recordType);
      if (attrType != null) {
        featureGenMap.put(
            BIRTH_DATE,
            usageTypesFor(BIRTH_DATE, recordType, maxBirthDates, true));
      }

      featureGenMap.put(
          ADDRESS,
          usageTypesFor(ADDRESS, recordType, maxAddresses, true));
      featureGenMap.put(
          PHONE,
          usageTypesFor(PHONE, recordType, maxPhones, true));
      featureGenMap.put(
          EMAIL,
          usageTypesFor(EMAIL, recordType, maxEmails, true));

      featureGenMaps.put(recordType, featureGenMap);
    }

    // create the union feature gen map
    Map<FeatureType, Set<UsageType>> aggregateFeatureGenMap
        = new LinkedHashMap<>();
    featureGenMaps.values().forEach(map -> {
      map.entrySet().forEach(entry -> {
        FeatureType     featureType = entry.getKey();
        Set<UsageType>  usageTypes  = entry.getValue();

        Set<UsageType> set = aggregateFeatureGenMap.get(featureType);
        if (set == null) {
          set = new LinkedHashSet<>();
          aggregateFeatureGenMap.put(featureType, set);
        }
        if (usageTypes != null) set.addAll(usageTypes);
        else set.add(null);
      });
    });

    // get the output files
    File csvFile        = (File) options.get(CSV_FILE);
    File jsonLinesFile  = (File) options.get(JSON_LINES_FILE);
    File jsonFile       = (File) options.get(JSON_FILE);

    List<RecordHandler> recordHandlers = new ArrayList<>();

    // create the CSV handler
    if (csvFile != null) {
      // check if file exists
      if (csvFile.exists() && !overwrite) {
        System.err.println(
            multilineFormat(
                "File cannot be overwritten unless the -overwrite option is specified:",
                "       " + csvFile));
      }
      try {
        FileOutputStream fos = new FileOutputStream(csvFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        Writer writer = new OutputStreamWriter(bos, UTF_8);

        CSVRecordHandler csvRecordHandler = new CSVRecordHandler(
            writer,
            withRecordIds,
            includeSources,
            aggregateFeatureGenMap,
            recordTypes,
            fullValues);

        recordHandlers.add(csvRecordHandler);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // create the JSON lines handler
    if (jsonLinesFile != null) {
      // check if file exists
      if (jsonLinesFile.exists() && !overwrite) {
        System.err.println(
            multilineFormat(
                "File cannot be overwritten unless the -overwrite option is specified:",
                "       " + jsonLinesFile));
      }
      try {
        FileOutputStream fos = new FileOutputStream(jsonLinesFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        Writer writer = new OutputStreamWriter(bos, UTF_8);

        JsonLinesRecordHandler jsonLinesHandler
            = new JsonLinesRecordHandler(writer);

        recordHandlers.add(jsonLinesHandler);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // create the JSON array handler
    if (jsonFile != null) {
      // check if file exists
      if (jsonFile.exists() && !overwrite) {
        System.err.println(
            multilineFormat(
                "File cannot be overwritten unless the -overwrite option is specified:",
                "       " + jsonFile));
      }
    }
    boolean stdout = (recordHandlers.size() == 0 && jsonFile == null);
    if (jsonFile != null || recordHandlers.size() == 0) {
      try {
        OutputStream fos = (jsonFile != null)
            ? new FileOutputStream(jsonFile) : nonClosingWrapper(System.out);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        Writer writer = new OutputStreamWriter(bos, UTF_8);

        JsonArrayRecordHandler jsonHandler
            = new JsonArrayRecordHandler(writer,
                                         (prettyPrint || jsonFile == null));

        recordHandlers.add(jsonHandler);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    CompoundRecordHandler recordHandler
        = new CompoundRecordHandler(recordHandlers);

    try {
      for (RecordType recordType: recordTypes) {
        dataGenerator.generateRecords(recordHandler,
                                      recordType,
                                      recordCounts.get(recordType),
                                      withRecordIds,
                                      recordSources.get(recordType),
                                      featureGenMaps.get(recordType),
                                      densityMap,
                                      fullValues,
                                      flatten);
      }

    } catch (Exception e) {
      e.printStackTrace();

    } finally {
      recordHandler.close();
      if (stdout) System.out.println();
    }
  }

  /**
   * Parses the command line arguments and returns a {@link Map} of those
   * arguments.  This will throw an exception if invalid command line arguments
   * are provided.
   *
   * @param args The arguments to parse.
   * @param deprecationWarnings The {@link List} to populate with any
   *                            deprecation warnings that might be generated,
   *                            or <code>null</code> if the caller is not
   *                            interested.
   * @return The {@link Map} of options to their values.
   * @throws CommandLineException If command line arguments are invalid.
   */
  private static Map<CommandLineOption, Object> parseCommandLine(
      String[]                      args,
      List<DeprecatedOptionWarning> deprecationWarnings)
      throws CommandLineException
  {
    Map<CommandLineOption, CommandLineValue> optionValues
        = CommandLineUtilities.parseCommandLine(
        DataGeneratorOption.class,
        args,
        DataGeneratorOption.PARAMETER_PROCESSOR,
        deprecationWarnings);

    // create a result map
    Map<CommandLineOption, Object> result = new LinkedHashMap<>();

    // iterate over the option values and handle them
    CommandLineUtilities.processCommandLine(optionValues, result);

    // return the result
    return result;
  }

  /**
   * Exits and prints the message associated with the specified exception.
   */
  private static void exitOnError(Throwable t) {
    System.err.println(t.getMessage());
    System.exit(1);
  }

  /**
   * Generates the usage string.
   *
   * @param full <tt>true</tt> if the full help message should be generated,
   *             and <tt>false</tt> if a shortened message should be generated.
   *
   * @return The usage string.
   */
  public static String getUsageString(boolean full) {
    // check if called from the RepositoryManager.main() directly
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    pw.println();
    Class<DataGenerator> cls = DataGenerator.class;
    if (checkClassIsMain(cls)) {
      pw.println("USAGE: java -cp " + JAR_FILE_NAME + " "
                     + cls.getName() + " <options>");
    } else {
      pw.println("USAGE: java -jar " + JAR_FILE_NAME + " --datagen <options>");
    }
    pw.println();
    pw.println();
    if (!full) {
      pw.flush();
      return sw.toString();
    }
    pw.print(multilineFormat(
        "<options> includes: ",
        "   --help",
        "        Should be the first and only option if provided.",
        "        Displays a complete usage message describing all options.",
        "",
        "   -personCount <count>",
        "        The number of person records to generate.",
        "",
        "   -orgCount <count>",
        "        The number of generic organization records to generate.",
        "",
        "   -bizCount <count>",
        "        The number of business organization records to generate.",
        "        Businesses are typically stores or offices as opposed to legal ",
        "        entities like corporations or foundations.",
        "",
        "   -personSources <data-source-1> [data-source-2 ... data-source-n]",
        "        The data sources to randomly choose from when generating the person",
        "        records.  If not specified then the records will be generated without",
        "        the DATA_SOURCE property.  This option requires that the -personCount",
        "        option has been specified.",
        "",
        "   -orgSources <data-source-1> [data-source-2 ... data-source-n]",
        "        The data sources to randomly choose from when generating the",
        "        organization records.  If not specified then the records will be",
        "        generated without the DATA_SOURCE property.",
        "        NOTE: This option requires that the -orgCount option has also been",
        "        specified.",
        "",
        "   -bizSources <data-source-1> [data-source-2 ... data-source-n]",
        "        The data sources to randomly choose from when generating the",
        "        business organization records.  If not specified then the records",
        "         will be generated without the DATA_SOURCE property.",
        "        NOTE: This option requires that the -bizCount option has also been",
        "        specified.",
        "",
        "   -maxNames <count>",
        "        Specifies the maximum number of names to generate for the records.",
        "        If not specified, then this a random number between 1 and 2.",
        "",
        "   -maxBirthDates <count>",
        "        Specifies the maximum number of birth dates to generate for the records.",
        "        If not specified, then this a random number between 0 and 1.",
        "",
        "   -maxAddresses <count>",
        "        Specifies the maximum number of addresses to generate for the records.",
        "        If not specified, then this a random number between 1 and 3.",
        "",
        "   -maxPhones <count>",
        "        Specifies the maximum number of addresses to generate for the records.",
        "        If not specified, then this a random number between 1 and 3.",
        "",
        "   -maxEmails <count>",
        "        Specifies the maximum number of email addresses to generate for the",
        "        records.  If not specified, then this a random number between 1 and 3.",
        "",
        "   -defaultNoFeatures",
        "        If specified then the default value for the maximum number of generated",
        "        features for each feature type is zero and must be overridden by specifying",
        "        the \"-max____\" option for that feature type.",
        "",
        "   -nameDensity <density>",
        "        Specifies the density of the name features that are generated.",
        "        The possible density values are:",
        "          - guaranteed          : all values generated",
        "          - first-then-common   : first guaranteed, others likely",
        "          - first-then-uncommon : first guaranteed, others less likely",
        "          - first-then-sparse   : first guaranteed, others unlikely",
        "          - common              : values are likely, none guaranteed",
        "          - uncommon            : values are less likely, none guaranteed",
        "          - sparse              : all values are unlikely",
        "        NOTE: The default value for names is \"first-then-sparse\".",
        "",
        "   -birthDateDensity <density>",
        "        Specifies the density of the birth date features that are generated.",
        "        The possible density values are the same as the -nameDensity option.",
        "        The default value is \"common\".",
        "",
        "   -addressDensity <density>",
        "        Specifies the density of the address features that are generated.",
        "        The possible density values are the same as the -nameDensity option.",
        "        The default value is \"common\".",
        "",
        "   -phoneDensity <density>",
        "        Specifies the density of the phone features that are generated.",
        "        The possible density values are the same as the -nameDensity option.",
        "        The default value is \"common\".",
        "",
        "   -emailDensity <density>",
        "        Specifies the density of the email features that are generated.",
        "        The possible density values are the same as the -nameDensity option.",
        "        The default value is \"common\".",
        "",
        "   -seed <random-seed>",
        "        Specifies long integer random seed to use.  The seed can be any ",
        "        integer number that can be represented as a 64-bit integer.",
        "",
        "   -withRecordIds",
        "        If specified then the records will be generated with the RECORD_ID",
        "        property with a generated record ID.",
        "",
        "   -fullValues",
        "        If specified then name and address features will be generated as single",
        "        full-value properties using NAME_FULL and ADDR_FULL, respectively.",
        "",
        "   -flatten",
        "        If specified then usage types will be prefixes on attribute property",
        "        names and will otherwise appear as distinct TYPE properties such as",
        "        NAME_TYPE, ADDR_TYPE, PHONE_TYPE and EMAIL_TYPE.",
        "",
        "   -csvFile <file-path>",
        "        If specified then the record are written to the specified CSV file.",
        "        An existing file will not be overwritten unless the -overwrite option",
        "        is specified.",
        "",
        "   -jsonLinesFile <file-path>",
        "        If specified then the record are written to the specified JSON-lines",
        "        file.  An existing file will not be overwritten unless the -overwrite",
        "        option is specified.",
        "",
        "   -jsonFile <file-path>",
        "        If specified then the record are written to the specified JSON file as",
        "        a JSON array of JSON objects.  An existing file will not be overwritten",
        "        unless the -overwrite option is specified.",
        "",
        "   -overwrite",
        "        If specified with the -csvFile, -jsonLinesFile and/or -jsonFile options",
        "        then an existing file will be overwritten with new content.",
        "",
        "   -prettyPrint",
        "        If specified with the -jsonFile option then the JSON will be pretty",
        "        printed.  This option does NOT affect the -jsonLinesFile output.",
        "",
        "   -configId <config-id>",
        "        Use with the -export and -setDefaultId options to specify the ID of",
        "        the configuration to use."));
    pw.flush();
    sw.flush();

    return sw.toString();
  }

}
