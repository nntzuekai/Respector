package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzWhyMatchInfo;
import com.senzing.api.model.SzWhyEntityResult;
import com.senzing.api.model.SzWhyPerspective;

/**
 * Describes why an entity resolved.
 */
@JsonDeserialize
public class SzWhyEntityResultImpl implements SzWhyEntityResult {
  /**
   * The {@link SzWhyPerspective} identifying and describing the perspective
   * for this why result.
   */
  private SzWhyPerspective perspective;

  /**
   * The {@link SzWhyMatchInfo} providing the details of the result.
   */
  private SzWhyMatchInfo matchInfo;

  /**
   * Default constructor.
   */
  public SzWhyEntityResultImpl() {
    this.perspective  = null;
    this.matchInfo    = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SzWhyPerspective getPerspective() {
    return this.perspective;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setPerspective(SzWhyPerspective perspective) {
    this.perspective = perspective;
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
