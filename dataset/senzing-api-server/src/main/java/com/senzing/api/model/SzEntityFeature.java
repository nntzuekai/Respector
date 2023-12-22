package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityFeatureImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Describes a feature for an entity.
 */
@JsonDeserialize(using=SzEntityFeature.Factory.class)
public interface SzEntityFeature {
  /**
   * Gets the internal ID for the primary feature value.
   *
   * @return The internal ID for the primary feature value.
   */
  @JsonInclude(NON_NULL)
  Long getPrimaryId();

  /**
   * Sets the internal ID for the primary feature value.
   *
   * @param primaryId The internal ID for the primary feature value.
   */
  void setPrimaryId(Long primaryId);

  /**
   * Gets the primary value for the feature.
   *
   * @return The primary value for the feature.
   */
  @JsonInclude(NON_NULL)
  String getPrimaryValue();

  /**
   * Sets the primary value for the feature.
   *
   * @param primaryValue The primary value for the feature.
   */
  void setPrimaryValue(String primaryValue);

  /**
   * Gets the usage type for the feature.
   *
   * @return The usage type for the feature.
   */
  @JsonInclude(NON_NULL)
  String getUsageType();

  /**
   * Sets the usage type for the feature.
   *
   * @param usageType The usage type for the feature.
   */
  void setUsageType(String usageType);

  /**
   * Returns the <b>unmodifiable</b> {@link Set} of duplicate values for the
   * entity.
   *
   * @return The <b>unmodifiable</b> {@link Set} of duplicate values for the
   *         entity.
   */
  @JsonInclude(NON_EMPTY)
  Set<String> getDuplicateValues();

  /**
   * Sets the duplicate values list for the entity.
   *
   * @param duplicateValues The list of duplicate values.
   */
  void setDuplicateValues(Collection<String> duplicateValues);

  /**
   * Adds to the duplicate value list for the record.
   *
   * @param value The duplicate value to add to the duplicate value list.
   */
  void addDuplicateValue(String value);

  /**
   * Gets the <b>unmodifiable</b> {@link List} of {@link SzEntityFeatureDetail}
   * instances describing the details of each of the clustered feature values
   * for this feature.
   *
   * @return The <b>unmodifiable</b> {@link List} of {@link
   *         SzEntityFeatureDetail} instances describing the details of each of
   *         the clustered feature values for this feature.
   */
  @JsonInclude(NON_EMPTY)
  List<SzEntityFeatureDetail> getFeatureDetails();

  /**
   * Sets the {@link List} of {@link SzEntityFeatureDetail} instances describing
   * the details of each of the clustered feature values for this feature.
   *
   * @param details The {@link Collection} of {@linkSzEntityFeatureDetail}
   *                instances describing the details of each of the clustered
   *                feature values for this feature.
   */
  void setFeatureDetails(Collection<SzEntityFeatureDetail> details);

  /**
   * Adds the specified {@link SzEntityFeatureDetail} instance to the {@link
   * List} of feature details.
   *
   * @param featureDetail The {@link SzEntityFeatureDetail} instance to add to
   *                      the list of feature details.
   */
  void addFeatureDetail(SzEntityFeatureDetail featureDetail);

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityFeature}.
   */
  interface Provider extends ModelProvider<SzEntityFeature> {
    /**
     * Creates a new instance of {@link SzEntityFeature}.
     *
     * @return The new instance of {@link SzEntityFeature}
     */
    SzEntityFeature create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityFeature} that produces instances of {@link SzEntityFeatureImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityFeature>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityFeature.class, SzEntityFeatureImpl.class);
    }

    @Override
    public SzEntityFeature create() {
      return new SzEntityFeatureImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzEntityFeature}.
   */
  class Factory extends ModelFactory<SzEntityFeature, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityFeature.class);
    }

    /**
     * Constructs with the default provider.  This constructor is private and
     * is used for the master singleton instance.
     * @param defaultProvider The default provider.
     */
    private Factory(Provider defaultProvider) {
      super(defaultProvider);
    }

    /**
     * Creates a new instance of {@link SzEntityFeature}.
     * @return The new instance of {@link SzEntityFeature}.
     */
    public SzEntityFeature create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a list of entity features from a {@link JsonArray} describing a
   * JSON array in the Senzing native API format for entity features and
   * populates the specified {@link List} or creates a new {@link List}.
   *
   * @param list The {@link List} of {@link SzEntityFeature} instances to
   *             populate, or <tt>null</tt> if a new {@link List}
   *             should be created.
   *
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   *
   * @return The populated (or created) {@link List} of {@link SzEntityFeature}
   *         instances.
   */
  static List<SzEntityFeature> parseEntityFeatureList(
      List<SzEntityFeature> list,
      JsonArray             jsonArray)
  {
    if (list == null) {
      list = new ArrayList<SzEntityFeature>(jsonArray.size());
    }
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseEntityFeature(null, jsonObject));
    }
    return list;
  }

  /**
   * Parses the entity feature from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity feature and populates
   * the specified {@link SzEntityFeature} or creates a new instance.
   *
   * @param feature The {@link SzEntityFeature} instance to populate, or
   *                <tt>null</tt> if a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @return The populated (or created) {@link SzEntityFeature}.
   */
  static SzEntityFeature parseEntityFeature(SzEntityFeature feature,
                                            JsonObject      jsonObject)
  {
    if (feature == null) feature = SzEntityFeature.FACTORY.create();

    String featureDesc = jsonObject.getString("FEAT_DESC");
    long   libFeatId   = jsonObject.getJsonNumber("LIB_FEAT_ID").longValue();
    String usageType   = JsonUtilities.getString(jsonObject, "USAGE_TYPE");

    feature.setPrimaryId(libFeatId);
    feature.setPrimaryValue(featureDesc);
    feature.setUsageType(usageType);

    JsonArray featureValues = jsonObject.getJsonArray("FEAT_DESC_VALUES");

    List<SzEntityFeatureDetail> details
        = SzEntityFeatureDetail.parseEntityFeatureDetailList(
            null, featureValues);

    for (SzEntityFeatureDetail detail: details) {
      long valueId = detail.getInternalId();
      if (valueId != libFeatId) {
        feature.addDuplicateValue(detail.getFeatureValue());
      }
      feature.addFeatureDetail(detail);
    }

    return feature;
  }
}
