package com.senzing.datagen;

import java.util.*;

import static com.senzing.datagen.FeatureType.*;
import static com.senzing.datagen.RecordType.*;

/**
 * Enumerates the various attribute types that can be generated.
 */
public enum GeneratedAttributeType {
  /**
   * The full name attribute type.
   */
  NAME_FULL(NAME, EnumSet.of(PERSON), ValueType.FULL_VALUE),

  /**
   * The full organization name attribute type.
   */
  NAME_ORG(NAME, EnumSet.of(BUSINESS, ORGANIZATION), ValueType.FULL_VALUE),

  /**
   * The first name attribute type.
   */
  NAME_FIRST(NAME, EnumSet.of(PERSON), ValueType.PART_VALUE),

  /**
   * The last name attribute type.
   */
  NAME_LAST(NAME, EnumSet.of(PERSON), ValueType.PART_VALUE),

  /**
   * The name type attribute for usage type.
   */
  NAME_TYPE(NAME, EnumSet.allOf(RecordType.class), ValueType.USAGE_TYPE),

  /**
   * The full address attribute type.
   */
  ADDR_FULL(ADDRESS, EnumSet.allOf(RecordType.class), ValueType.FULL_VALUE),

  /**
   * The first address line attribute type.
   */
  ADDR_LINE1(ADDRESS, EnumSet.allOf(RecordType.class), ValueType.PART_VALUE),

  /**
   * The address city attribute type.
   */
  ADDR_CITY(ADDRESS, EnumSet.allOf(RecordType.class), ValueType.PART_VALUE),

  /**
   * The address state attribute type.
   */
  ADDR_STATE(ADDRESS, EnumSet.allOf(RecordType.class), ValueType.PART_VALUE),

  /**
   * The address postal code attribute type.
   */
  ADDR_POSTAL_CODE(ADDRESS,
                   EnumSet.allOf(RecordType.class),
                   ValueType.PART_VALUE),

  /**
   * The address type attribute for usage type.
   */
  ADDR_TYPE(ADDRESS, EnumSet.allOf(RecordType.class), ValueType.USAGE_TYPE),

  /**
   * The phone number attribute type.
   */
  PHONE_NUMBER(PHONE, EnumSet.allOf(RecordType.class), ValueType.FULL_VALUE),

  /**
   * The address type attribute for usage type.
   */
  PHONE_TYPE(PHONE, EnumSet.allOf(RecordType.class), ValueType.USAGE_TYPE),

  /**
   * The email address attribute type.
   */
  EMAIL_ADDRESS(EMAIL, EnumSet.allOf(RecordType.class), ValueType.FULL_VALUE),

  /**
   * The address type attribute for usage type.
   */
  EMAIL_TYPE(EMAIL, EnumSet.allOf(RecordType.class), ValueType.USAGE_TYPE),

  /**
   * The date of birth attribute.
   */
  DATE_OF_BIRTH(BIRTH_DATE, EnumSet.of(PERSON), ValueType.FULL_VALUE);

  /**
   * Enumerates the types of values that can be represented by an instance
   * of {@link GeneratedAttributeType}.
   */
  public enum ValueType {
    /**
     * The attribute type represents the full value of the attribute.
     */
    FULL_VALUE,

    /**
     * The attribute type represents a component part of the value for the
     * attribute.
     */
    PART_VALUE,

    /**
     * The attribute type is used to describe the usage type for the feature
     * but does not hold an actual value.
     */
    USAGE_TYPE;
  }

  /**
   * The associated {@link FeatureType}.
   */
  private FeatureType featureType;

  /**
   * The {@link EnumSet} of {@link RecordType} instances to which this
   * attribute type applies.
   */
  private EnumSet<RecordType> recordTypes;

  /**
   * Indicates what the value of an attribute of this type represents.
   */
  private ValueType valueType;

  /**
   * Constructs with the specified {@link FeatureType}, {@link RecordType}
   * and flag indicating if the attribute type represents the full value.
   *
   * @param featureType The associated {@link FeatureType}.
   * @param recordTypes The {@link EnumSet} of {@link RecordType} instances to
   *                    which this applies.
   * @param valueType Indicates what a value of this attribute type represents.
   */
  GeneratedAttributeType(FeatureType         featureType,
                         EnumSet<RecordType> recordTypes,
                         ValueType           valueType)
  {
    this.featureType  = featureType;
    this.recordTypes  = recordTypes;
    this.valueType    = valueType;
  }

  /**
   * Gets the associated {@link FeatureType}.
   *
   * @return The associated {@link FeatureType}.
   */
  public FeatureType getFeatureType() {
    return this.featureType;
  }

  /**
   * Gets the {@link EnumSet} of {@link RecordType} instances that this
   * attribute type is applicable to.
   *
   * @return The {@link EnumSet} of {@link RecordType} instances that this
   *         attribute type is applicable to.
   */
  public EnumSet<RecordType> getRecordTypes() {
    return this.recordTypes;
  }

  /**
   * Gets the {@link ValueType} describing what the value of an attribute of
   * this type represents (either the full value, component part value or
   * usage type)
   */
  public ValueType getValueType() {
    return this.valueType;
  }

  /**
   * The lookup key for instances of this class.
   */
  private static class LookupKey {
    private FeatureType featureType;
    private RecordType recordType;

    private LookupKey(FeatureType featureType,
                      RecordType  recordType)
    {
      this.featureType = featureType;
      this.recordType  = recordType;
    }

    public boolean equals(Object object) {
      if (object == null) return false;
      if (this == object) return true;
      if (this.getClass() != object.getClass()) return false;
      LookupKey key = (LookupKey) object;
      return ((this.featureType == key.featureType)
              && (this.recordType == key.recordType));
    }

    public int hashCode() {
      return Objects.hash(this.featureType, this.recordType);
    }
  }

  /**
   * The {@link Map} for looking up the {@link ValueType#FULL_VALUE} instance by
   * {@link FeatureType} and {@link RecordType}.
   */
  private static final Map<LookupKey, GeneratedAttributeType> FULL_LOOKUP_MAP;

  /**
   * The {@link Map} for looking up the {@link ValueType#USAGE_TYPE} instance by
   * {@link FeatureType}.
   */
  private static final Map<FeatureType, GeneratedAttributeType> TYPE_LOOKUP_MAP;

  /**
   * The {@link Map} for looking up the {@link Set} of {@link
   * ValueType#PART_VALUE} instances by {@link FeatureType} and {@link
   * RecordType}.
   */
  private static final Map<LookupKey, Set<GeneratedAttributeType>>
      PARTS_LOOKUP_MAP;

  static {
    Map<LookupKey, GeneratedAttributeType>      fullMap = new LinkedHashMap<>();
    Map<FeatureType, GeneratedAttributeType>    typeMap = new LinkedHashMap<>();
    Map<LookupKey, Set<GeneratedAttributeType>> partMap = new LinkedHashMap<>();

    try {
      for (GeneratedAttributeType attrType : GeneratedAttributeType.values()) {
        FeatureType         featType    = attrType.getFeatureType();
        EnumSet<RecordType> recordTypes = attrType.getRecordTypes();
        ValueType           valueType   = attrType.getValueType();

        // iterate over the record types
        for (RecordType recordType : recordTypes) {
          LookupKey key = new LookupKey(featType, recordType);

          // check if a full value or part value
          switch (valueType) {
            case FULL_VALUE:
              if (fullMap.containsKey(key)) {
                throw new IllegalStateException(
                    "Cannot have more than one FULL_VALUE instance for the same "
                        + "feature type and record type.  featureType=[ " +
                        featType + " ], recordType=[ " + recordType
                        + " ], instance1=[ " + fullMap.get(key)
                        + " ], instance2=[ " + attrType + " ]");
              }
              fullMap.put(key, attrType);
              break;
            case PART_VALUE:
              Set<GeneratedAttributeType> set = partMap.get(key);
              if (set == null) {
                set = new LinkedHashSet<>();
                partMap.put(key, set);
              }
              set.add(attrType);
              break;

            case USAGE_TYPE:
              if (typeMap.containsKey(featType)
                  && !typeMap.get(featType).equals(attrType))
              {
                throw new IllegalStateException(
                    "Cannot have more than one USAGE_TYPE instance for the same "
                        + "feature type.  featureType=[ " +
                        featType + " ], instance1=[ " + typeMap.get(featType)
                        + " ], instance2=[" + attrType + " ]");
              }
              typeMap.put(featType, attrType);
              break;
            default:
              throw new IllegalStateException(
                  "Unhandled ValueType.  valueType=[ " + valueType
                      + " ], attrType=[ " + attrType + " ]");
          }
        }
      }

      // make the sets in the part map unmodifiable
      Iterator<Map.Entry<LookupKey, Set<GeneratedAttributeType>>> iter
          = partMap.entrySet().iterator();
      while (iter.hasNext()) {
        // get the next entry
        Map.Entry<LookupKey,Set<GeneratedAttributeType>> entry = iter.next();

        // get the set value
        Set<GeneratedAttributeType> set = entry.getValue();

        // make the set unmodifiable
        set = Collections.unmodifiableSet(set);
        entry.setValue(set);
      }

      // iterate over the entries in the full map
      fullMap.entrySet().forEach(entry -> {
        LookupKey key = entry.getKey();
        partMap.putIfAbsent(key, Collections.singleton(entry.getValue()));
      });

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      // set the lookup map
      FULL_LOOKUP_MAP   = Collections.unmodifiableMap(fullMap);
      PARTS_LOOKUP_MAP  = Collections.unmodifiableMap(partMap);
      TYPE_LOOKUP_MAP   = Collections.unmodifiableMap(typeMap);
    }
  }

  /**
   * Gets the {@link GeneratedAttributeType} instance that represents the full
   * value for the specified {@link FeatureType} and {@link RecordType}.
   *
   * @param featureType The {@link FeatureType} for the desired attribute types.
   * @param recordType The {@link RecordType} for the generated record.
   *
   * @return The {@link GeneratedAttributeType} instance that represents the
   *         full value for the specified {@link FeatureType} and {@link
   *         RecordType}.
   */
  public static GeneratedAttributeType fullValueInstance(
      FeatureType   featureType,
      RecordType    recordType)
  {
    LookupKey key = new LookupKey(featureType, recordType);
    return FULL_LOOKUP_MAP.get(key);
  }

  /**
   * Gets the {@link GeneratedAttributeType} instance that represents the usage
   * type value for the specified {@link FeatureType} and {@link RecordType}.
   *
   * @param featureType The {@link FeatureType} for the desired attribute types.
   *
   * @return The {@link GeneratedAttributeType} instance that represents the
   *         full value for the specified {@link FeatureType} and {@link
   *         RecordType}.
   */
  public static GeneratedAttributeType usageTypeInstance(
      FeatureType featureType)
  {
    return TYPE_LOOKUP_MAP.get(featureType);
  }

  /**
   * Gets the {@link Set} of {@link GeneratedAttributeType} instances that
   * represent the component part values for the specified {@link FeatureType}
   * and {@link RecordType}.  This returns a singleton set if there is only
   * a full-value instance for the specified {@link FeatureType} and {@link
   * RecordType}.
   *
   * @param featureType The {@link FeatureType} for the desired attribute types.
   * @param recordType The {@link RecordType} for the generated record.
   *
   * @return The {@link Set} of {@link GeneratedAttributeType} values for the
   *         specified parameters.
   */
  public static Set<GeneratedAttributeType> partValuesInstances(
      FeatureType   featureType,
      RecordType    recordType)
  {
    LookupKey key = new LookupKey(featureType, recordType);
    Set<GeneratedAttributeType> set = PARTS_LOOKUP_MAP.get(key);
    return (set != null) ? set : Collections.emptySet();
  }
}
