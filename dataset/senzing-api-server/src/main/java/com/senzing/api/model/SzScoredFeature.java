package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzScoredFeatureImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

/**
 * Describes a feature value that has been scored.
 */
@JsonDeserialize(using=SzScoredFeature.Factory.class)
public interface SzScoredFeature {
  /**
   * Gets the feature ID for the scored feature.
   *
   * @return The feature ID for the scored feature.
   */
  Long getFeatureId();

  /**
   * Sets the feature ID for the scored feature.
   *
   * @param featureId The feature ID for the scored feature.
   */
  void setFeatureId(Long featureId);

  /**
   * Gets the feature type for the scored feature.
   *
   * @return The feature type for the scored feature.
   */
  String getFeatureType();

  /**
   * Sets the feature type for the scored feature.
   *
   * @param featureType The feature type for the scored feature.
   */
  void setFeatureType(String featureType);

  /**
   * Gets the feature value for the scored feature.
   *
   * @return The feature value for the scored feature.
   */
  String getFeatureValue();

  /**
   * Sets the feature value for the scored feature.
   *
   * @param featureValue The feature value for the scored feature.
   */
  void setFeatureValue(String featureValue);

  /**
   * Gets the usage type for the scored feature.
   *
   * @return The usage type for the scored feature.
   */
  @JsonInclude(NON_EMPTY)
  String getUsageType();

  /**
   * Sets the usage type for the scored feature.
   *
   * @param usageType The usage type for the scored feature.
   */
  void setUsageType(String usageType);

  /**
   * A {@link ModelProvider} for instances of {@link SzScoredFeature}.
   */
  interface Provider extends ModelProvider<SzScoredFeature> {
    /**
     * Creates an uninitialized instnace of {@link SzScoredFeature}.
     */
    SzScoredFeature create();

    /**
     * Creates an instance of {@Link SzScoredFeatures} initialized with the
     * specified parameters.
     *
     * @param featureId The feature ID.
     * @param featureType The feature type.
     * @param featureValue The value for the feautre.
     * @param usageType The usage type associated with the feature.
     */
    SzScoredFeature create(Long   featureId,
                           String featureType,
                           String featureValue,
                           String usageType);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzScoredFeature} that produces instances of
   * {@link SzScoredFeatureImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzScoredFeature>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzScoredFeature.class, SzScoredFeatureImpl.class);
    }

    @Override
    public SzScoredFeature create() {
      return new SzScoredFeatureImpl();
    }

    @Override
    public SzScoredFeature create(Long   featureId,
                           String featureType,
                           String featureValue,
                           String usageType) {
      return new SzScoredFeatureImpl(
          featureId, featureType, featureValue, usageType);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzScoredFeature}.
   */
  class Factory extends ModelFactory<SzScoredFeature, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzScoredFeature.class);
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
     * Creates an uninitialized instnace of {@link SzScoredFeature}.
     */
    public SzScoredFeature create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance of {@Link SzScoredFeatures} initialized with the
     * specified parameters.
     *
     * @param featureId The feature ID.
     * @param featureType The feature type.
     * @param featureValue The value for the feautre.
     * @param usageType The usage type associated with the feature.
     */
    public SzScoredFeature create(Long   featureId,
                                  String featureType,
                                  String featureValue,
                                  String usageType)
    {
      return this.getProvider().create(
          featureId, featureType, featureValue, usageType);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzScoredFeature}.
   *
   * @param jsonObject The {@link JsonObject} describing the perspective using
   *                   the native API JSON format.
   *
   * @param prefix The prefix to apply to the native JSON keys.
   *
   * @param featureType The feature type for the {@link SzCandidateKey}
   *                    instances.
   *
   * @return The created instance of {@link SzWhyPerspective}.
   */
  static SzScoredFeature parseScoredFeature(JsonObject jsonObject,
                                            String     prefix,
                                            String     featureType)
  {
    Long featureId = JsonUtilities.getLong(jsonObject, prefix + "FEAT_ID");

    String value = (jsonObject.containsKey(prefix + "FEAT"))
      ? jsonObject.getString(prefix + "FEAT")
      : JsonUtilities.getString(jsonObject, prefix + "FEAT_DESC");

    String usage = JsonUtilities.getString(
        jsonObject,prefix + "FEAT_USAGE_TYPE");

    SzScoredFeature result = SzScoredFeature.FACTORY.create(featureId,
                                                            featureType,
                                                            value,
                                                            usage);

    return result;
  }

}
