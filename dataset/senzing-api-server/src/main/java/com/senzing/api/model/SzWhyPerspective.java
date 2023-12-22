package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzWhyPerspectiveImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Collection;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Describes the perspective used in evaluating why an entity resolved or why
 * two records may or may not resolve.  The answer to "why" is dependent on
 * which "record" you are comparing against the other "records".  Internally,
 * it is not always based on "record" because multiple records that are
 * effectively identical collapse into a single perspective.
 */
@JsonDeserialize(using=SzWhyPerspective.Factory.class)
public interface SzWhyPerspective {
  /**
   * Gets the internal ID uniquely identifying this perspective from others
   * in the complete "why" response.
   *
   * @return The internal ID uniquely identifying this perspective from others
   * in the complete "why" response.
   */
  @JsonInclude(NON_NULL)
  Long getInternalId();

  /**
   * Sets the internal ID uniquely identifying this perspective from others
   * in the complete "why" response.
   *
   * @param internalId The internal ID uniquely identifying this perspective
   *                   from others in the complete "why" response.
   */
  void setInternalId(Long internalId);

  /**
   * Gets the associated entity ID for the perspective.
   *
   * @return The associated entity ID for the perspective.
   */
  @JsonInclude(NON_NULL)
  Long getEntityId();

  /**
   * Sets the associated entity ID for the perspective.
   *
   * @param entityId The associated entity ID for the perspective.
   */
  void setEntityId(Long entityId);

  /**
   * Gets the <b>unmodifiable</b> {@link Set} of {@link SzFocusRecordId}
   * instances identifying the focus records for this perspective.
   *
   * @return The <b>unmodifiable</b> {@link Set} of {@link SzFocusRecordId}
   *         instances identifying the focus records for this perspective.
   */
  @JsonInclude(NON_EMPTY)
  Set<SzFocusRecordId> getFocusRecords();

  /**
   * Adds the specified {@link SzFocusRecordId} to the {@link Set} of focus
   * records.
   *
   * @param focusRecord The {@link SzFocusRecordId} to add to the focus records.
   */
  void addFocusRecord(SzFocusRecordId focusRecord);

  /**
   * Sets the {@link Set} of {@link SzFocusRecordId} instances identifying the
   * focus records for this perspective.
   *
   * @param focusRecords The {@link Collection} of {@link SzFocusRecordId}
   *                     instances identifying the focus records for this
   *                     perspective.
   */
  void setFocusRecords(Collection<SzFocusRecordId> focusRecords);

  /**
   * A {@link ModelProvider} for instances of {@link SzWhyPerspective}.
   */
  interface Provider extends ModelProvider<SzWhyPerspective> {
    /**
     * Creates a new instance of {@link SzWhyPerspective}.
     *
     * @return The new instance of {@link SzWhyPerspective}
     */
    SzWhyPerspective create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzWhyPerspective} that produces instances of
   * {@link SzWhyPerspectiveImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzWhyPerspective>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzWhyPerspective.class, SzWhyPerspectiveImpl.class);
    }

    @Override
    public SzWhyPerspective create() {
      return new SzWhyPerspectiveImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzWhyPerspective}.
   */
  class Factory extends ModelFactory<SzWhyPerspective, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzWhyPerspective.class);
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
     * Creates a new instance of {@link SzWhyPerspective}.
     * @return The new instance of {@link SzWhyPerspective}.
     */
    public SzWhyPerspective create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzWhyPerspective}.
   *
   * @param jsonObject The {@link JsonObject} describing the record using the
   *                   native API JSON format.
   *
   * @return The created instance of {@link SzWhyPerspective}.
   */
  static SzWhyPerspective parseWhyPerspective(JsonObject jsonObject) {
    return parseWhyPerspective(jsonObject, "");
  }

  /**
   * Parses the native API JSON to build an instance of {@link
   * SzWhyPerspective}.
   *
   * @param jsonObject The {@link JsonObject} describing the perspective using
   *                   the native API JSON format.
   *
   * @param suffix The suffix to apply to the native JSON keys.
   *
   * @return The created instance of {@link SzWhyPerspective}.
   */
  static SzWhyPerspective parseWhyPerspective(JsonObject jsonObject,
                                                     String     suffix)
  {
    Long internalId = JsonUtilities.getLong(jsonObject,"INTERNAL_ID" + suffix);

    Long entityId = JsonUtilities.getLong(jsonObject,"ENTITY_ID" + suffix);

    JsonArray jsonArr = jsonObject.getJsonArray("FOCUS_RECORDS" + suffix);

    Collection<SzFocusRecordId> focusRecords
        = SzFocusRecordId.parseFocusRecordIdList(jsonArr);

    SzWhyPerspective perspective = SzWhyPerspective.FACTORY.create();

    perspective.setInternalId(internalId);
    perspective.setEntityId(entityId);
    perspective.setFocusRecords(focusRecords);

    return perspective;
  }

}
