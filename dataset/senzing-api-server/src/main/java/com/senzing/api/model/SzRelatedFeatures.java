package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzRelatedFeaturesImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Described two features that matched each other to create a relationship
 * (typically a disclosed relationship).
 */
@JsonDeserialize(using=SzRelatedFeatures.Factory.class)
public interface SzRelatedFeatures {
  /**
   * Gets the relationship feature belonging to the first entity that was
   * matched to create the relationship.
   *
   * @return The relationship feature belonging to the first entity that was
   *         matched to create the relationship.
   */
  SzScoredFeature getFeature1();

  /**
   * Sets the relationship feature belonging to the first entity that was
   * matched to create the relationship.
   *
   * @param feature The relationship feature belonging to the first entity that
   *                was matched to create the relationship.
   */
  void setFeature1(SzScoredFeature feature);

  /**
   * Gets the relationship feature belonging to the second entity that was
   * matched to create the relationship.
   *
   * @return The relationship feature belonging to the first entity that was
   *         matched to create the relationship.
   */
  SzScoredFeature getFeature2();

  /**
   * Sets the relationship feature belonging to the second entity that was
   * matched to create the relationship.
   *
   * @param feature The relationship feature belonging to the first entity that
   *                was matched to create the relationship.
   */
  void setFeature2(SzScoredFeature feature);

  /**
   * A {@link ModelProvider} for instances of {@link SzRelatedFeatures}.
   */
  interface Provider extends ModelProvider<SzRelatedFeatures> {
    /**
     * Creates a new instance of {@link SzRelatedFeatures}.
     *
     * @return The new instance of {@link SzRelatedFeatures}
     */
    SzRelatedFeatures create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzRelatedFeatures} that produces instances of
   * {@link SzRelatedFeaturesImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzRelatedFeatures>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzRelatedFeatures.class, SzRelatedFeaturesImpl.class);
    }

    @Override
    public SzRelatedFeatures create() {
      return new SzRelatedFeaturesImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzRelatedFeatures}.
   */
  class Factory extends ModelFactory<SzRelatedFeatures, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzRelatedFeatures.class);
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
     * Creates a new instance of {@link SzRelatedFeatures}.
     * @return The new instance of {@link SzRelatedFeatures}.
     */
    public SzRelatedFeatures create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzRelatedFeatures}.
   *
   * @param jsonObject The {@link JsonObject} describing the features using
   *                   the native API JSON format.
   *
   * @param featureType The feature tyoe for the first feature.
   *
   * @return The created instance of {@link SzRelatedFeatures}.
   */
  static SzRelatedFeatures parseRelatedFeatures(JsonObject jsonObject,
                                                String     featureType)
  {
    SzScoredFeature feature = SzScoredFeature.parseScoredFeature(
        jsonObject, "", featureType);

    String linkedType = JsonUtilities.getString(jsonObject, "LINKED_FEAT_TYPE");

    SzScoredFeature linkedFeature = SzScoredFeature.parseScoredFeature(
        jsonObject, "LINKED_", linkedType);

    SzRelatedFeatures result = SzRelatedFeatures.FACTORY.create();

    result.setFeature1(feature);
    result.setFeature2(linkedFeature);

    return result;
  }

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzRelatedFeatures}.
   *
   * @param jsonArray The {@link JsonArray} describing the features list using
   *                  the native API JSON format.
   *
   * @param featureType The feature tyoe for the first feature.
   *
   * @return The created instance of {@link SzRelatedFeatures}.
   */
  static List<SzRelatedFeatures> parseRelatedFeatures(
      JsonArray jsonArray, String featureType) {
    return parseRelatedFeatures(null, jsonArray, featureType);
  }

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzRelatedFeatures}.
   *
   * @param jsonArray The {@link JsonArray} describing the features list using
   *                  the native API JSON format.
   *
   * @param featureType The feature tyoe for the first feature.
   *
   * @return The created instance of {@link SzRelatedFeatures}.
   */
  static List<SzRelatedFeatures> parseRelatedFeatures(
      List<SzRelatedFeatures> list,  JsonArray jsonArray, String featureType)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseRelatedFeatures(jsonObject, featureType));
    }

    return list;
  }

}
