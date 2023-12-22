package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzWhyMatchInfo;
import com.senzing.api.model.SzWhyEntitiesResult;

/**
 * Describes why an entity resolved.
 */
@JsonDeserialize
public class SzWhyEntitiesResultImpl implements SzWhyEntitiesResult {
  /**
   * The entity ID for the first entity.
   */
  private Long entityId1;

  /**
   * The entity ID for the second entity.
   */
  private Long entityId2;

  /**
   * The {@link SzWhyMatchInfo} providing the details of the result.
   */
  private SzWhyMatchInfo matchInfo;

  /**
   * Default constructor.
   */
  public SzWhyEntitiesResultImpl() {
    this.entityId1  = null;
    this.entityId2  = null;
    this.matchInfo  = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getEntityId1() {
    return this.entityId1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntityId1(Long entityId) {
    this.entityId1 = entityId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getEntityId2() {
    return this.entityId2;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEntityId2(Long entityId) {
    this.entityId2 = entityId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzWhyMatchInfo getMatchInfo() {
    return this.matchInfo;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setMatchInfo(SzWhyMatchInfo matchInfo) {
    this.matchInfo = matchInfo;
  }
}
