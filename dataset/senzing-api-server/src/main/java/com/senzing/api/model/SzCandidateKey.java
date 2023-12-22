package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzCandidateKeyImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a candidate key that triggered the scoring of two entities.
 */
@JsonDeserialize(using=SzCandidateKey.Factory.class)
public interface SzCandidateKey {
  /**
   * Gets the identifier for the candidate feature.
   *
   * @return The identifier for the candidate feature.
   */
  Long getFeatureId();

  /**
   * Sets the identifier for the candidate feature.
   *
   * @param featureId The identifier for the candidate feature.
   */
  void setFeatureId(Long featureId);

  /**
   * Gets the feature type for the candidate feature.
   *
   * @return The feature type for the candidate feature.
   */
  String getFeatureType();

  /**
   * Sets the feature type for the candidate feature.
   *
   * @param featureType The feature type for the candidate feature.
   */
  void setFeatureType(String featureType);

  /**
   * Gets the feature value for the candidate feature.
   *
   * @return The feature value for the candidate feature.
   */
  String getFeatureValue();

  /**
   * Sets the feature value for the candidate feature.
   *
   * @param featureValue The feature value for the candidate feature.
   */
  void setFeatureValue(String featureValue);

    /**
   * A {@link ModelProvider} for instances of {@link SzCandidateKey}.
   */
  interface Provider extends ModelProvider<SzCandidateKey> {
    /**
     * Creates a new instance of {@link SzCandidateKey}.
     *
     * @return The new instance of {@link SzCandidateKey}
     */
    SzCandidateKey create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzCandidateKey} that produces instances of {@link SzCandidateKeyImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzCandidateKey>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzCandidateKey.class, SzCandidateKeyImpl.class);
    }

    @Override
    public SzCandidateKey create() {
      return new SzCandidateKeyImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzCandidateKey}.
   */
  class Factory extends ModelFactory<SzCandidateKey, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzCandidateKey.class);
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
     * Creates a new instance of {@link SzCandidateKey}.
     * @return The new instance of {@link SzCandidateKey}.
     */
    public SzCandidateKey create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the {@link SzCandidateKey} from a {@link JsonObject} describing JSON
   * for the Senzing native API format for a candidate key to create a new
   * instance.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @param featureType The feature type for the {@link SzCandidateKey}.
   *
   * @return The {@link SzCandidateKey} that was created.
   */
  static SzCandidateKey parseCandidateKey(JsonObject  jsonObject,
                                          String      featureType)
  {
    Long    featureId     = JsonUtilities.getLong(jsonObject, "FEAT_ID");
    String  featureValue  = JsonUtilities.getString(jsonObject, "FEAT_DESC");

    SzCandidateKey result = SzCandidateKey.FACTORY.create();

    result.setFeatureId(featureId);
    result.setFeatureType(featureType);
    result.setFeatureValue(featureValue);

    return result;
  }

  /**
   * Parses and populates a {@link List} of {@link SzCandidateKey} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for candidate keys to create new
   * instances.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @param featureType The feature type for the {@link SzCandidateKey}
   *                    instances.
   *
   * @return The {@link List} of {@link SzCandidateKey} instances that were
   *         populated.
   */
  static List<SzCandidateKey> parseCandidateKeyList(JsonArray jsonArray,
                                                    String    featureType)
  {
    return parseCandidateKeyList(null, jsonArray, featureType);
  }

  /**
   * Parses and populates a {@link List} of {@link SzCandidateKey} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for candidate keys to create new
   * instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @param featureType The feature type for the {@link SzCandidateKey}
   *                    instances.
   *
   * @return The {@link List} of {@link SzCandidateKey} instances that were
   *         populated.
   */
  static List<SzCandidateKey> parseCandidateKeyList(
      List<SzCandidateKey>  list,
      JsonArray             jsonArray,
      String                featureType)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseCandidateKey(jsonObject, featureType));
    }

    return list;
  }
}
