package com.senzing.datagen;

import javax.json.JsonObjectBuilder;
import java.util.Objects;
import java.util.Set;

/**
 * Provides an abstract implementation of {@link GeneratedFeature}.
 */
public abstract class AbstractGeneratedFeature implements GeneratedFeature {
  /**
   * The associated {@link FeatureType}.
   */
  private FeatureType featureType;

  /**
   * The {@link RecordType} for the feature.
   */
  private RecordType recordType;

  /**
   * Constructs with the specified {@link FeatureType} and {@link RecordType}.
   *
   * @param featureType The associated {@link FeatureType}.
   *
   * @param recordType The {@link RecordType} for the feature.
   */
  protected AbstractGeneratedFeature(FeatureType featureType,
                                     RecordType  recordType)
  {
    this.featureType = featureType;
    this.recordType  = recordType;
  }

  /**
   * Implemented to return the appropriate {@link FeatureType}.
   *
   * @return The appropriate {@link FeatureType}.
   */
  public FeatureType getFeatureType() {
    return this.featureType;
  }

  /**
   * Implemented to return the {@link RecordType} with which the instance
   * was constructed.
   *
   * @return The {@link RecordType} that this feature was generated for.
   */
  public RecordType getRecordType() {
    return this.recordType;
  }

  /**
   * Gets the value for the specified {@linkplain GeneratedAttributeType
   * attribute type} that is of this feature.
   *
   * @param attributeType The non-null {@link GeneratedAttributeType} for the
   *                      value being requested.
   *
   * @throws IllegalArgumentException If the specified parameter is not
   *                                  associated with this feature.
   * @throws NullPointerException If the specified parameter is <tt>null</tt>.
   */
  public final String getValue(GeneratedAttributeType attributeType) {
    // check for null
    Objects.requireNonNull(
        attributeType, "Attribute type cannot be null");

    // check the feature type
    if (this.getFeatureType() != attributeType.getFeatureType()) {
      throw new IllegalArgumentException(
          "The specified attribute type (" + attributeType
              + ") is for the wrong feature type ("
              + attributeType.getFeatureType() + ").  Expected feature type: "
              + this.getFeatureType());
    }

    // switch on the value type
    switch (attributeType.getValueType()) {
      case FULL_VALUE:
        return this.getFullValue();
      case PART_VALUE:
        return this.doGetValue(attributeType);
      case USAGE_TYPE:
        throw new IllegalArgumentException(
            "Cannot get the USAGE_TYPE value from the feature: "
                + attributeType);
      default:
        throw new IllegalStateException(
            "Unhandled attribute value type: " + attributeType.getValueType());
    }
  }

  /**
   * Internal method that returns the value for the specified {@link
   * GeneratedAttributeType} after it has been validated.
   *
   * @param attrType The {@link GeneratedAttributeType} for the value being
   *                 requested.
   *
   * @return The attribute value.
   */
  protected abstract String doGetValue(GeneratedAttributeType attrType);

  /**
   * Validates that the specified {@link UsageType} is applicable to the
   * {@link FeatureType} and {@link RecordType} associated with this feature.
   *
   * @param usageType The {@link UsageType} to validate.
   *
   * @throws IllegalArgumentException If the specified {@link UsageType} is not
   *                                  valid for use with this feature.
   */
  private void validateUsageType(UsageType usageType)
    throws IllegalArgumentException
  {
    if (usageType == null) return;
    FeatureType featureType = this.getFeatureType();
    RecordType  recordType  = this.getRecordType();
    if (!usageType.appliesTo(featureType)) {
      throw new IllegalArgumentException(
          "Invalid usage type for a " + this.getFeatureType()
              + " feature: " + usageType);
    }
    if (!usageType.appliesTo(recordType)) {
      throw new IllegalArgumentException(
          "Invalid usage type for a " + recordType
              + " record type: " + usageType);
    }
  }

  /**
   * Implemented to validate the specified {@link UsageType} and
   * then add the full property value to the builder with the appropriate
   * property name.  If a {@link UsageType} is specified it will either be
   * used as a prefix to the full property name or added as a separate JSON
   * property using the {@link GeneratedAttributeType.ValueType#USAGE_TYPE}
   * attribute, depending on the specified "prefixed" boolean parameter.
   *
   * @param builder The {@link JsonObjectBuilder} to add to.
   * @param usageType The {@link UsageType} to add the feature with or
   *                  <tt>null</tt> if no usage type should be used.
   * @param prefixed <tt>true</tt> if the usage type (if any) should be a prefix
   *                 to the JSON property names that are added, and
   *                 <tt>false</tt> if it should be added as its own property.
   * @return The specified {@link JsonObjectBuilder}.
   */
  public JsonObjectBuilder addFull(JsonObjectBuilder  builder,
                                   UsageType          usageType,
                                   boolean            prefixed)
  {
    this.validateUsageType(usageType);

    // get the property name (there should be one)
    GeneratedAttributeType fullProp
        = GeneratedAttributeType.fullValueInstance(this.getFeatureType(),
                                                   this.getRecordType());

    // check if we are prefixing the property name with the usage type
    String prop = ((usageType == null || !prefixed) ? "" : usageType + "_")
        + fullProp;

    // add to the builder
    builder.add(prop, this.toString());

    // now check if we need to add the usage type
    if (usageType != null && !prefixed) {
      GeneratedAttributeType usageTypeProp
          = GeneratedAttributeType.usageTypeInstance(this.getFeatureType());
      builder.add(usageTypeProp.toString(), usageType.toString());
    }

    // return the builder
    return builder;
  }

  /**
   * Adds the feature to the specified {@link JsonObjectBuilder} with the
   * optionally specified {@link UsageType} with the {@link
   * GeneratedAttributeType.ValueType#PART_VALUE} property names.  If a
   * {@link UsageType} is specified it will either be used as a prefix to
   * value property names or added as a separate JSON property using the
   * {@link GeneratedAttributeType.ValueType#USAGE_TYPE} attribute, depending
   * on the specified "prefixed" boolean parameter.
   *
   * @param builder The {@link JsonObjectBuilder} to add to.
   * @param usageType The {@link UsageType} to add the feature with or
   *                  <tt>null</tt> if no usage type should be used.
   * @param prefixed <tt>true</tt> if the usage type (if any) should be a prefix
   *                 to the JSON property names that are added, and
   *                 <tt>false</tt> if it should be added as its own property.
   * @return The specified {@link JsonObjectBuilder}.
   */
  public JsonObjectBuilder addParts(JsonObjectBuilder builder,
                                    UsageType         usageType,
                                    boolean           prefixed)
  {
    this.validateUsageType(usageType);

    Set<GeneratedAttributeType> attrTypes
        = GeneratedAttributeType.partValuesInstances(
            this.getFeatureType(), this.getRecordType());

    // iterates over the attribute types and adds the values for each one
    for (GeneratedAttributeType attrType: attrTypes) {
      String prop = ((usageType == null || !prefixed) ? "" : usageType + "_")
          + attrType;
      builder.add(prop, this.getValue(attrType));
    }

    if (usageType != null && !prefixed) {
      GeneratedAttributeType usageTypeProp
          = GeneratedAttributeType.usageTypeInstance(this.getFeatureType());
      builder.add(usageTypeProp.toString(), usageType.toString());
    }

    // add to the builder
    return builder;
  }
}
