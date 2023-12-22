package com.senzing.datagen;

import java.util.*;

import static com.senzing.datagen.FeatureType.*;
import static com.senzing.datagen.RecordType.*;

/**
 * Enumerates the various usage types used in data generation.
 */
public enum UsageType {
  /**
   * The HOME usage type applies to addresses, phone numbers and email
   * addresses.
   */
  HOME(EnumSet.of(ADDRESS, PHONE, EMAIL), EnumSet.of(PERSON)),

  /**
   * The WORK usage type applies to addresses, phone numbers and email.
   */
  WORK(EnumSet.of(ADDRESS, PHONE, EMAIL), EnumSet.of(PERSON)),

  /**
   * The BUSINESS usage type applies to addresses, phone numbers and email,
   * BUT has special meaning for addresses indicating an organization that
   * will not merge with another if they have different BUSINESS addresses.
   */
  BUSINESS(EnumSet.of(ADDRESS, PHONE, EMAIL), EnumSet.of(RecordType.BUSINESS)),

  /**
   * The MOBILE usage type for phone number features.
   */
  MOBILE(EnumSet.of(PHONE), EnumSet.of(PERSON)),

  /**
   * The FAX usage type for FAX phone number features.
   */
  FAX(EnumSet.of(PHONE), EnumSet.allOf(RecordType.class)),

  /**
   * Main PRIMARY usage type for all feature types.
   */
  PRIMARY(EnumSet.allOf(FeatureType.class), EnumSet.allOf(RecordType.class)),

  /**
   * Main AKA usage type for name features.
   */
  AKA(EnumSet.of(NAME), EnumSet.of(PERSON)),

  /**
   * Main DBA usage type for name features.
   */
  DBA(EnumSet.of(NAME), EnumSet.of(ORGANIZATION, RecordType.BUSINESS)),

  /**
   * Main ALT usage type for all feature types.
   */
  ALT(EnumSet.allOf(FeatureType.class), EnumSet.allOf(RecordType.class)),

  /**
   * Main ALT_2 usage type for all feature types.
   */
  ALT_2(EnumSet.allOf(FeatureType.class), EnumSet.allOf(RecordType.class)),

  /**
   * Main MAIN usage type for all feature types.
   */
  MAIN(EnumSet.allOf(FeatureType.class), EnumSet.allOf(RecordType.class)),

  /**
   * The MAIDEN usage type for name features.
   */
  MAIDEN(EnumSet.of(NAME), EnumSet.of(PERSON));

  /**
   * The applicable feature types for the usage type.
   */
  private EnumSet<FeatureType> featureTypes;

  /**
   * The applicable record types for the usage type.
   */
  private EnumSet<RecordType> recordTypes;

  /**
   * Default constructor for instances that apply to all feature types.
   */
  UsageType() {
    this(EnumSet.allOf(FeatureType.class), EnumSet.allOf(RecordType.class));
  }

  /**
   * Constructs with flags indicating the applicability of the usage type.
   * @param featureTypes The feature types that the usage type is applicable to.
   * @param recordTypes The record types that the usage type is applicable to.
   */
  UsageType(EnumSet<FeatureType> featureTypes, EnumSet<RecordType> recordTypes)
  {
    this.featureTypes = featureTypes;
    this.recordTypes  = recordTypes;
  }

  /**
   * The map of {@link FeatureType} to {@link List} values containing
   * applicable {@link UsageType} instances.
   */
  private static final Map<FeatureType, List<UsageType>> FEATURE_TYPE_MAP;

  /**
   * The map of {@link RecordType} to {@link List} values containing
   * applicable {@link UsageType} instances.
   */
  private static final Map<RecordType, List<UsageType>> RECORD_TYPE_MAP;

  /**
   * Setup the feature type map.
   */
  static {
    Map<FeatureType, List<UsageType>> featureTypeMap = new LinkedHashMap<>();
    Map<RecordType, List<UsageType>> recordTypeMap = new LinkedHashMap<>();

    try {
      UsageType[] usageTypes = UsageType.values();
      for (FeatureType featureType : FeatureType.values()) {
        List<UsageType> list = new ArrayList<>(usageTypes.length);
        for (UsageType usageType : usageTypes) {
          if (usageType.appliesTo(featureType)) {
            list.add(usageType);
          }
        }
        list = Collections.unmodifiableList(list);
        featureTypeMap.put(featureType, list);
      }

      for (RecordType recordType : RecordType.values()) {
        List<UsageType> list = new ArrayList<>(usageTypes.length);
        for (UsageType usageType : usageTypes) {
          if (usageType.appliesTo(recordType)) {
            list.add(usageType);
          }
        }
        list = Collections.unmodifiableList(list);
        recordTypeMap.put(recordType, list);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      FEATURE_TYPE_MAP = Collections.unmodifiableMap(featureTypeMap);
      RECORD_TYPE_MAP = Collections.unmodifiableMap(recordTypeMap);
    }
  }


  /**
   * Checks if this usage type applies to the specified {@link FeatureType}.
   *
   * @param featureType The {@link FeatureType} to check for applicability.
   *
   * @return <tt>true</tt> if this applies to specified {@link FeatureType},
   *         otherwise <tt>false</tt>.
   */
  public boolean appliesTo(FeatureType featureType) {
    return this.featureTypes.contains(featureType);
  }

  /**
   * Checks if this usage type applies to the specified {@link RecordType}.
   *
   * @param recordType The {@link RecordType} to check for applicability.
   *
   * @return <tt>true</tt> if this applies to specified {@link RecordType},
   *         otherwise <tt>false</tt>.
   */
  public boolean appliesTo(RecordType recordType) {
    return this.recordTypes.contains(recordType);
  }

  /**
   * Returns an {@link EnumSet} of {@link UsageType} instances for the
   * specified {@link FeatureType} containing at most the specified maximum
   * count.
   *
   * @param featureType The non-null applicable {@link FeatureType}.
   * @param recordType The non-null applicable {@link RecordType}.
   * @param maxCount The non-negative maximum count.
   */
  public static EnumSet<UsageType> usageTypesFor(FeatureType  featureType,
                                                 RecordType   recordType,
                                                 int          maxCount) {
    return usageTypesFor(featureType, recordType, maxCount, false);
  }

  /**
   * Returns an {@link EnumSet} of {@link UsageType} instances for the
   * specified {@link FeatureType} containing at most the specified maximum
   * count.
   *
   * @param featureType The non-null applicable {@link FeatureType}.
   * @param recordType The non-null applicable {@link RecordType}.
   * @param maxCount The non-negative maximum count.
   * @param nullIfOne <tt>true</tt> if <tt>null</tt> should be returned if only
   *                  one (1) {@link UsageType} would be in the resultant
   *                  {@link Set} and <tt>false</tt> if the {@link Set} of one
   *                  should be returned.
   */
  public static EnumSet<UsageType> usageTypesFor(FeatureType  featureType,
                                                 RecordType   recordType,
                                                 int          maxCount,
                                                 boolean      nullIfOne)
  {
    Objects.requireNonNull(featureType, "Feature type cannot be null");
    Objects.requireNonNull(recordType, "Record type cannot be null");
    if (maxCount < 0) {
      throw new IllegalArgumentException(
          "The specified maximum count cannot be negative: " + maxCount);
    }
    List<UsageType> list1 = new ArrayList<>(FEATURE_TYPE_MAP.get(featureType));
    List<UsageType> list2 = RECORD_TYPE_MAP.get(recordType);
    list1.retainAll(list2);
    int upperBound = Math.min(list1.size(), maxCount);
    EnumSet<UsageType> result = (upperBound == 0)
        ? EnumSet.noneOf(UsageType.class)
        : EnumSet.copyOf(list1.subList(0, upperBound));
    if (result.size() == 1 && nullIfOne) return null;
    return result;
  }
}
