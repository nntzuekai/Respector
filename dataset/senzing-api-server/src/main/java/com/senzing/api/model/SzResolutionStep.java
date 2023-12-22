package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzResolutionStepImpl;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.senzing.util.JsonUtilities.*;
import static com.senzing.util.JsonUtilities.getJsonObject;

/**
 * Describes a virtual entity.
 */
@JsonDeserialize(using= SzResolutionStep.Factory.class)
public interface SzResolutionStep {
  /**
   * Gets the step number for this step indicating the order of this step
   * relative to other steps if the steps were flattened to be linear.  However,
   * the non-linear nature of entity resolution means that the ordering of the
   * steps is only relevant within a single branch of the resolution tree.
   *
   * @return The step number for this step.
   */
  int getStepNumber();

  /**
   * Sets the step number for this step indicating the order of this step
   * relative to other steps if the steps were flattened to be linear.  However,
   * the non-linear nature of entity resolution means that the ordering of the
   * steps is only relevant within a single branch of the resolution tree.
   *
   * @param stepNumber The step number for this step.
   */
  void setStepNumber(int stepNumber);

  /**
   * Gets the {@link SzVirtualEntity} describing the inbound virtual entity
   * for this resolution step.
   *
   * @return The {@link SzVirtualEntity} describing the inbound virtual entity
   *         for this resolution step.
   */
  SzVirtualEntity getInboundVirtualEntity();

  /**
   * Sets the {@link SzVirtualEntity} describing the inbound virtual entity
   * for this resolution step.
   *
   * @param virtualEntity The {@link SzVirtualEntity} describing the inbound
   *                      virtual entity for this resolution step.
   */
  void setInboundVirtualEntity(SzVirtualEntity virtualEntity);

  /**
   * Gets the {@link SzVirtualEntity} describing the candidate virtual entity
   * for this resolution step.
   *
   * @return The {@link SzVirtualEntity} describing the candidate virtual entity
   *         for this resolution step.
   */
  SzVirtualEntity getCandidateVirtualEntity();

  /**
   * Sets the {@link SzVirtualEntity} describing the candidate virtual entity
   * for this resolution step.
   *
   * @param virtualEntity The {@link SzVirtualEntity} describing the candidate
   *                      virtual entity for this resolution step.
   */
  void setCandidateVirtualEntity(SzVirtualEntity virtualEntity);

  /**
   * Gets the {@link SzHowMatchInfo} describing how the {@linkplain
   * #getInboundVirtualEntity() inbound virtual entity} and {@linkplain
   * #getCandidateVirtualEntity() candidate virtual entity} were resolved to
   * create the virtual entity identified by the {@linkplain
   * #getResolvedVirtualEntityId() resolved virtual entity ID}.
   *
   * @return The {@link SzHowMatchInfo} describing how the virtual entities
   *         were resolved.
   */
  SzHowMatchInfo getMatchInfo();

  /**
   * Sets the {@link SzHowMatchInfo} describing how the {@linkplain
   * #getInboundVirtualEntity() inbound virtual entity} and {@linkplain
   * #getCandidateVirtualEntity() candidate virtual entity} were resolved to
   * create the virtual entity identified by the {@linkplain
   * #getResolvedVirtualEntityId() resolved virtual entity ID}.
   *
   * @param matchInfo The {@link SzHowMatchInfo} describing how the virtual
   *                  entities were resolved.
   */
  void setMatchInfo(SzHowMatchInfo matchInfo);

  /**
   * Gets the virtual entity ID for the resultant virtual entity that was
   * resolved in this step from the {@linkplain #getInboundVirtualEntity()
   * inbound virtual entity} and {@linkplain #getCandidateVirtualEntity()
   * candidate virtual entity}.
   *
   * @return The virtual entity ID for the resultant virtual entity.
   */
  String getResolvedVirtualEntityId();

  /**
   * Sets the virtual entity ID for the resultant virtual entity that was
   * resolved in this step from the {@linkplain #getInboundVirtualEntity()
   * inbound virtual entity} and {@linkplain #getCandidateVirtualEntity()
   * candidate virtual entity}.
   *
   * @param virtualEntityId The virtual entity ID for the resultant virtual
   *                        entity.
   */
  void setResolvedVirtualEntityId(String virtualEntityId);

  /**
   * A {@link ModelProvider} for instances of {@link SzResolutionStep}.
   */
  interface Provider extends ModelProvider<SzResolutionStep> {
    /**
     * Creates an instance with a step number of zero (0).
     *
     * @return The {@link SzResolutionStep} that was created.
     */
    SzResolutionStep create();
    
    /**
     * Creates an instance with the specified step number.
     *
     * @param stepNumber The step number for the step being created.
     *
     * @return The {@link SzResolutionStep} that was created.
     */
    SzResolutionStep create(int stepNumber);
  }

  /**
   * Implements a default {@link Provider} for {@link SzResolutionStep} that
   * produces instances of {@link SzResolutionStepImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzResolutionStep>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzResolutionStep.class, SzResolutionStepImpl.class);
    }

    @Override
    public SzResolutionStep create() {
      return new SzResolutionStepImpl();
    }

    @Override
    public SzResolutionStep create(int stepNumber)
    {
      return new SzResolutionStepImpl(stepNumber);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzResolutionStep}.
   */
  class Factory extends ModelFactory<SzResolutionStep, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzResolutionStep.class);
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
     * Creates an instance with a step number of zero (0).
     *
     * @return The {@link SzResolutionStep} that was created.
     */
    public SzResolutionStep create() {
      return this.getProvider().create();
    }

    /**
     * Creates an instance with the specified step number.
     *
     * @param stepNumber The step number for the step being created.
     *
     * @return The {@link SzResolutionStep} that was created.
     */
    public SzResolutionStep create(int stepNumber) {
      return this.getProvider().create(stepNumber);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the Senzing native API JSON described by the specified {@link
   * JsonObject} as an instance of {@link SzResolutionStep}.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzResolutionStep} that was created.
   */
  static SzResolutionStep parseResolutionStep(JsonObject jsonObject)
  {
    int stepNumber = getInteger(jsonObject, "STEP");
    SzResolutionStep step = SzResolutionStep.FACTORY.create(stepNumber);
    
    JsonObject entityObj = getJsonObject(jsonObject, "VIRTUAL_ENTITY_1");
    SzVirtualEntity entity1 = SzVirtualEntity.parseVirtualEntity(entityObj);

    entityObj = getJsonObject(jsonObject, "VIRTUAL_ENTITY_2");
    SzVirtualEntity entity2 = SzVirtualEntity.parseVirtualEntity(entityObj);

    String inboundId = getString(jsonObject, "INBOUND_VIRTUAL_ENTITY_ID");

    if (inboundId.equals(entity1.getVirtualEntityId())) {
      step.setInboundVirtualEntity(entity1);
      step.setCandidateVirtualEntity(entity2);
    } else if (inboundId.equals(entity2.getVirtualEntityId())) {
      step.setInboundVirtualEntity(entity2);
      step.setCandidateVirtualEntity(entity1);
    } else {
      throw new IllegalArgumentException(
          "The INBOUND_VIRTUAL_ENTITY_ID property has a virtual entity ID ("
          + inboundId + ") that matches neither of the virtual entities.  "
          + "virtualEntity1=[ " + entity1 + " ], virtualEntity2=[ " + entity2
          + " ], rawJson=[ " + toJsonText(jsonObject) + " ]");
    }

    JsonObject matchObj = getJsonObject(jsonObject, "MATCH_INFO");
    step.setMatchInfo(SzHowMatchInfo.parseMatchInfo(matchObj));

    String resolvedId = getString(jsonObject, "RESULT_VIRTUAL_ENTITY_ID");
    step.setResolvedVirtualEntityId(resolvedId);

    // return the step
    return step;
  }

  /**
   * Parses and populates a {@link List} of {@link SzResolutionStep} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzResolutionStep} instances that were
   *         populated.
   */
  static List<SzResolutionStep> parseResolutionStepList(
      JsonArray jsonArray)
  {
    return parseResolutionStepList(null, jsonArray);
  }

  /**
   * Parses and populates a {@link List} of {@link SzResolutionStep} instances
   * from a {@link JsonArray} of {@link JsonObject} instances describing JSON
   * for the Senzing native API format for record ID to create new instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   *
   * @param jsonArray The {@link JsonArray} to parse.
   *
   * @return The {@link List} of {@link SzResolutionStep} instances that were
   *         populated.
   */
  static List<SzResolutionStep> parseResolutionStepList(
      List<SzResolutionStep>  list,
      JsonArray               jsonArray)
  {
    // construct the list
    if (list == null) {
      // construct an array list
      list = new ArrayList<>(jsonArray.size());
    }

    // parse the records for each object
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseResolutionStep(jsonObject));
    }

    // return the list
    return list;
  }
}
