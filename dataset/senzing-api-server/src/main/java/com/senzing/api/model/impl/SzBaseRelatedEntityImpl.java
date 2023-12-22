package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.senzing.api.model.SzBaseRelatedEntity;
import com.senzing.api.model.SzResolvedEntity;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;
import java.util.Optional;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Provides a default implementation of the {@link SzBaseRelatedEntity}
 * functionality.
 */
public abstract class SzBaseRelatedEntityImpl
    extends SzResolvedEntityImpl implements SzBaseRelatedEntity
{
  /**
   * The match level.
   */
  private Integer matchLevel;

  /**
   * The match key for the relationship.
   */
  private String matchKey;

  /**
   * The resolution rule code.
   */
  private String resolutionRuleCode;

  /**
   * Default constructor.
   */
  public SzBaseRelatedEntityImpl() {
    this.matchLevel         = null;
    this.matchKey           = null;
    this.resolutionRuleCode = null;
  }

  /**
   * Gets the underlying match level from the entity resolution between the
   * entities.
   *
   * @return The underlying match level from the entity resolution between the
   *         entities.
   */
  @JsonInclude(NON_NULL)
  public Integer getMatchLevel() {
    return this.matchLevel;
  }

  /**
   * Sets the underlying match level from the entity resolution between the
   * entities.
   *
   * @param matchLevel The underlying match level from the entity resolution
   *                   between the entities.
   */
  public void setMatchLevel(Integer matchLevel) {
    this.matchLevel = matchLevel;
  }

  /**
   * Gets the underlying match key from the entity resolution between
   * the entities.
   *
   * @return The underlying match key from the entity resolution between
   *         the entities.
   */
  @JsonInclude(NON_NULL)
  public String getMatchKey() {
    return matchKey;
  }

  /**
   * Sets the underlying match key from the entity resolution between
   * the entities.
   *
   * @param matchKey The underlying match key from the entity resolution
   *                 between the entities.
   */
  public void setMatchKey(String matchKey) {
    this.matchKey = matchKey;
  }

  /**
   * Gets the underlying resolution rule code from the entity resolution
   * between the entities.
   *
   * @return The underlying resolution rule code from the entity resolution
   *         between the entities.
   */
  @JsonInclude(NON_NULL)
  public String getResolutionRuleCode() {
    return resolutionRuleCode;
  }

  /**
   * Sets the underlying resolution rule code from the entity resolution
   * between the entities.
   *
   * @param code The underlying resolution rule code from the entity resolution
   *             between the entities.
   */
  public void setResolutionRuleCode(String code) {
    this.resolutionRuleCode = code;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "{" +
        "matchLevel=" + matchLevel +
        ", matchKey='" + matchKey + '\'' +
        ", resolutionRuleCode='" + resolutionRuleCode + '\'' +
        ", super=" + super.toString() +
        '}';
  }
}
