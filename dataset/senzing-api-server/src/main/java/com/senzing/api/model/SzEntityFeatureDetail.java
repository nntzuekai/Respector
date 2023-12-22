package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityFeatureDetailImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes the details of an entity feature value, optionally including
 * statistics if they have been requested.
 */
@JsonDeserialize(using=SzEntityFeatureDetail.Factory.class)
public interface SzEntityFeatureDetail {
  /**
   * Gets the internal ID for the feature value.
   *
   * @return The internal ID for the feature value.
   */
  Long getInternalId();

  /**
   * Sets the internal ID for the feature value.
   *
   * @param internalId The internal ID for the feature value.
   */
  void setInternalId(Long internalId);

  /**
   * Gets the actual feature value.
   *
   * @return The actual feature value.
   */
  String getFeatureValue();

  /**
   * Sets the actual feature value.
   *
   * @param featureValue The actual feature value.
   */
  void setFeatureValue(String featureValue);

  /**
   * Gets the {@link SzEntityFeatureStatistics} describing the statistics for
   * the feature value.  This returns <tt>null</tt> if the statistics were not
   * requested.
   *
   * @return The {@link SzEntityFeatureStatistics} describing the statistics
   *         for the feature value, or <tt>null</tt> if the statistics were not
   *         requested.
   */
  @JsonInclude(NON_NULL)
  SzEntityFeatureStatistics getStatistics();

  /**
   * Sets the {@link SzEntityFeatureStatistics} describing the statistics for
   * the feature value.  Set this to <tt>null</tt> if the statistics were not
   * requested.
   *
   * @param statistics The {@link SzEntityFeatureStatistics} describing the
   *                   statistics for the feature value, or <tt>null</tt> if
   *                   the statistics were not requested.
   */
  void setStatistics(SzEntityFeatureStatistics statistics);

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityFeatureDetail}.
   */
  interface Provider extends ModelProvider<SzEntityFeatureDetail> {
    /**
     * Creates a new instance of {@link SzEntityFeatureDetail}.
     *
     * @return The new instance of {@link SzEntityFeatureDetail}
     */
    SzEntityFeatureDetail create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityFeatureDetail} that produces instances of {@link
   * SzEntityFeatureDetailImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityFeatureDetail>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityFeatureDetail.class, SzEntityFeatureDetailImpl.class);
    }

    @Override
    public SzEntityFeatureDetail create() {
      return new SzEntityFeatureDetailImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzEntityFeatureDetail}.
   */
  class Factory extends ModelFactory<SzEntityFeatureDetail, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityFeatureDetail.class);
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
     * Creates a new instance of {@link SzEntityFeatureDetail}.
     * @return The new instance of {@link SzEntityFeatureDetail}.
     */
    public SzEntityFeatureDetail create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API Senzing JSON to populate (or create and populate) an
   * instance of {@link SzEntityFeatureDetail}.
   *
   * @param detail The {@link SzEntityFeatureDetail} to populate, or
   *               <tt>null</tt> if a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzEntityFeatureDetail} that was parsed.
   */
  static SzEntityFeatureDetail parseEntityFeatureDetail(
      SzEntityFeatureDetail detail,
      JsonObject            jsonObject)
  {
    if (detail == null) detail = SzEntityFeatureDetail.FACTORY.create();

    Long    internalId  = JsonUtilities.getLong(jsonObject, "LIB_FEAT_ID");
    String  value       = JsonUtilities.getString(jsonObject,  "FEAT_DESC");

    SzEntityFeatureStatistics statistics
        = SzEntityFeatureStatistics.parseEntityFeatureStatistics(jsonObject);

    detail.setInternalId(internalId);
    detail.setFeatureValue(value);
    detail.setStatistics(statistics);

    return detail;
  }

  /**
   * Parses the native API Senzing JSON to populate (or create and populate) a
   * {@link List} of {@link SzEntityFeatureDetail} instances.
   *
   * @param list The {@link List} of {@link SzEntityFeatureDetail} to populate,
   *             or <tt>null</tt> if a new list should be created.
   *
   * @param jsonArray The {@link JsonArray} of {@link JsonObject} instances to
   *                  parse.
   *
   * @return The {@link SzEntityFeatureDetail} that was parsed.
   */
  static List<SzEntityFeatureDetail> parseEntityFeatureDetailList(
      List<SzEntityFeatureDetail> list,
      JsonArray                   jsonArray)
  {
    if (jsonArray == null) return null;

    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }

    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseEntityFeatureDetail(null, jsonObject));
    }

    return list;
  }
}
