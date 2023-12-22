package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzHowEntityResultImpl;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;

import static com.senzing.api.model.SzVirtualEntityRecord.parseVirtualEntityRecordList;
import static com.senzing.util.JsonUtilities.*;

/**
 * Describes a virtual entity.
 */
@JsonDeserialize(using=SzHowEntityResult.Factory.class)
public interface SzHowEntityResult {
  /**
   * Gets the {@link List} of {@link SzVirtualEntity} instances describing
   * the possible final states for the resolution.  If there is more than one
   * final state then the entity should be reevaluated.
   * 
   * @return The {@link List} of {@link SzVirtualEntity} instances describing
   *         the possible states for the entity resolution.
   */
  List<SzVirtualEntity> getFinalStates();

  /**
   * Adds the specified {@link SzVirtualEntity} to the {@link List} of possible
   * final states for the entity resolution.  If there is more than one final
   * state then the entity should be reevaluated.
   *
   * @param finalState The {@link SzVirtualEntity} to add to the {@link List}
   *                   of possible final states.
   */
  void addFinalState(SzVirtualEntity finalState);

  /**
   * Sets the {@link List} of {@link SzVirtualEntity} instances describing
   * the possible final states for the entithy resolution.  If there is more
   * than one final state then the entity should be reevaluated.
   *
   * @param finalStates The {@link Collection} of {@link SzVirtualEntity}
   *                    instances describing the possible final states for the
   *                    entity resolution.
   */
  void setFinalStates(Collection<SzVirtualEntity> finalStates);

  /**
   * Gets the {@link Map} of {@link String} virtual entity ID keys to {@link
   * SzResolutionStep} values describing to the resolution steps in which the
   * respective identified virtual entity was resolved.
   *
   * <b>NOTE:</b> Any {@linkplain SzVirtualEntity#isSingleton() singleton}
   * virtual entities in the {@link SzResolutionStep} values will not have their
   * keys in this {@link Map} since they represent individual records that were
   * not entity resolved.
   *
   * @return The {@link Map} of {@link String} virtual entity ID keys to {@link
   *         SzResolutionStep} values describing to the resolution steps in
   *         which the respective identified virtual entity was resolved.
   */
  Map<String, SzResolutionStep> getResolutionSteps();

  /**
   * Adds the specified {@link SzResolutionStep} to the {@link Map} of
   * resolution steps for resolving the entity.  The result from
   * {@link SzResolutionStep#getResolvedVirtualEntityId()} is used as the key
   * in the {@link Map} with the specified parameter used as the value.
   *
   * @param step The {@link SzResolutionStep} to add to the {@link Map} of
   *             resolution steps for resolving the entity.
   */
  void addResolutionStep(SzResolutionStep step);

  /**
   * Sets the {@link SzResolutionStep} instances for this instance to those
   * in the specified {@link Collection} of {@link SzResolutionStep} instances,
   * using the {@link SzResolutionStep} instances as {@link Map} values and the
   * result from {@link SzResolutionStep#getResolvedVirtualEntityId()} as the
   * respective key in the {@link Map}.
   *
   * @param steps The {@link Collection} of {@link SzVirtualEntity}
   *                    instances describing the possible final states for the
   *                    entity resolution.
   */
  void setResolutionSteps(Collection<SzResolutionStep> steps);

  /**
   * A {@link ModelProvider} for instances of {@link SzHowEntityResult}.
   */
  interface Provider extends ModelProvider<SzHowEntityResult> {
    /**
     * Creates an uninitialized instance with no arguments.
     *
     * @return The {@link SzHowEntityResult} that was created.
     */
    SzHowEntityResult create();
  }

  /**
   * Implements a default {@link Provider} for {@link SzHowEntityResult} that
   * produces instances of {@link SzHowEntityResultImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzHowEntityResult>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzHowEntityResult.class, SzHowEntityResultImpl.class);
    }

    @Override
    public SzHowEntityResult create() {
      return new SzHowEntityResultImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzHowEntityResult}.
   */
  class Factory extends ModelFactory<SzHowEntityResult, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzHowEntityResult.class);
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
     * Creates an uninitialized instance with no arguments.
     *
     * @return The {@link SzHowEntityResult} that was created.
     */
    public SzHowEntityResult create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the {@link SzHowEntityResult} from a {@link JsonObject} describing
   * JSON for the Senzing native API format for how result to create a new
   * instance.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzHowEntityResult} that was created.
   */
  static SzHowEntityResult parseHowEntityResult(JsonObject jsonObject)
  {
    // get the final states array
    JsonObject finalStateObj = getJsonObject(jsonObject, "FINAL_STATE");

    JsonArray jsonArray = getJsonArray(finalStateObj, "VIRTUAL_ENTITIES");

    // parse the final states
    List<SzVirtualEntity> finalStates
        = SzVirtualEntity.parseVirtualEntityList(jsonArray);

    // get the resolution steps array
    jsonArray = getJsonArray(jsonObject, "RESOLUTION_STEPS");

    // parse the resolution steps
    List<SzResolutionStep> steps
        = SzResolutionStep.parseResolutionStepList(jsonArray);

    // construct the record
    SzHowEntityResult result = SzHowEntityResult.FACTORY.create();

    // set the result fields
    result.setFinalStates(finalStates);
    result.setResolutionSteps(steps);

    // return the result
    return result;
  }

  /**
   * Parses and populates a {@link List} of {@link SzHowEntityResult} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzHowEntityResult} instances that were
   *         populated.
   */
  static List<SzHowEntityResult> parseHowEntityResultList(JsonArray jsonArray)
  {
    return parseHowEntityResultList(null, jsonArray);
  }

  /**
   * Parses and populates a {@link List} of {@link SzHowEntityResult} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzHowEntityResult} instances that were
   *         populated.
   */
  static List<SzHowEntityResult> parseHowEntityResultList(
      List<SzHowEntityResult> list,
      JsonArray               jsonArray)
  {
    // construct the list
    if (list == null) {
      // construct an array list
      list = new ArrayList<>(jsonArray.size());
    }

    // parse the records for each object
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseHowEntityResult(jsonObject));
    }

    // return the list
    return list;
  }
}
