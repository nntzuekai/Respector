package com.senzing.datagen;

import javax.json.JsonObjectBuilder;

/**
 * Common interface for generated features.
 */
public interface GeneratedFeature {
  /**
   * Gets the {@link FeatureType} for this feature.
   *
   * @return The {@link FeatureType} for this feature.
   */
  FeatureType getFeatureType();

  /**
   * Gets the {@link RecordType} describing the type of record that this
   * feature was generated for.
   *
   * @return The {@link RecordType} that this feature was generated for.
   */
  RecordType getRecordType();

  /**
   * Gets the full value for this feature.
   * @return The full value for this feature.
   */
  default String getFullValue() {
    return this.toString();
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
  String getValue(GeneratedAttributeType attributeType);

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
  JsonObjectBuilder addFull(JsonObjectBuilder builder,
                            UsageType         usageType,
                            boolean           prefixed);

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
  JsonObjectBuilder addParts(JsonObjectBuilder  builder,
                             UsageType          usageType,
                             boolean            prefixed);
}
