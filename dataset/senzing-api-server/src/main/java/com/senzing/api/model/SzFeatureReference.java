package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzFeatureReferenceImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes a feature for an entity.
 */
@JsonDeserialize(using= SzFeatureReference.Factory.class)
public interface SzFeatureReference {
  /**
   * Gets the internal ID for the entity feature.
   *
   * @return The internal ID for the entity feature.
   */
  @JsonInclude(NON_NULL)
  Long getInternalId();

  /**
   * Sets the internal ID for the entity feature.
   *
   * @param internalId The internal ID for the entity feature.
   */
  void setInternalId(Long internalId);

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
   * A {@link ModelProvider} for instances of {@link SzFeatureReference}.
   */
  interface Provider extends ModelProvider<SzFeatureReference> {
    /**
     * Creates a new instance of {@link SzFeatureReference}.
     *
     * @return The new instance of {@link SzFeatureReference}
     */
    SzFeatureReference create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzFeatureReference} that produces instances of {@link
   * SzFeatureReferenceImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzFeatureReference>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzFeatureReference.class, SzFeatureReferenceImpl.class);
    }

    @Override
    public SzFeatureReference create() {
      return new SzFeatureReferenceImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzFeatureReference}.
   */
  class Factory extends ModelFactory<SzFeatureReference, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzFeatureReference.class);
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
     * Creates a new instance of {@link SzFeatureReference}.
     * @return The new instance of {@link SzFeatureReference}.
     */
    public SzFeatureReference create()
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
   * @param list The {@link List} of {@link SzFeatureReference} instances to
   *             populate, or <tt>null</tt> if a new {@link List}
   *             should be created.
   *
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   *
   * @return The populated (or created) {@link List} of {@link
   *         SzFeatureReference} instances.
   */
  static List<SzFeatureReference> parseFeatureReferenceList(
      List<SzFeatureReference>  list,
      JsonArray                 jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseFeatureReference(null, jsonObject));
    }
    return list;
  }

  /**
   * Parses the entity feature from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity feature and populates
   * the specified {@link SzFeatureReference} or creates a new instance.
   *
   * @param feature The {@link SzFeatureReference} instance to populate, or
   *                <tt>null</tt> if a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @return The populated (or created) {@link SzFeatureReference}.
   */
  static SzFeatureReference parseFeatureReference(SzFeatureReference feature,
                                                  JsonObject      jsonObject)
  {
    if (feature == null) feature = SzFeatureReference.FACTORY.create();

    long   libFeatId   = jsonObject.getJsonNumber("LIB_FEAT_ID").longValue();
    String usageType   = JsonUtilities.getString(jsonObject, "USAGE_TYPE");

    feature.setInternalId(libFeatId);
    feature.setUsageType(usageType);

    return feature;
  }
}
