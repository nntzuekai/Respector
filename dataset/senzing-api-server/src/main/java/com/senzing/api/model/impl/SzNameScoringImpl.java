package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzNameScoring;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Provides a default implementation of {@link SzNameScoring}.
 */
@JsonDeserialize
public class SzNameScoringImpl implements SzNameScoring {
  /**
   * The full name score, or <tt>null</tt> if no full name score.
   */
  private Integer fullNameScore;

  /**
   * The surname score, or <tt>null</tt> if no surname score.
   */
  private Integer surnameScore;

  /**
   * The given name score, or <tt>null</tt> if no given name score.
   */
  private Integer givenNameScore;

  /**
   * The generation match score, or <tt>null</tt> if no generation match score.
   */
  private Integer generationScore;

  /**
   * The organization name score, or <tt>null</tt> if no organization name
   * score.
   */
  private Integer orgNameScore;

  /**
   * Default constructor.
   */
  public SzNameScoringImpl() {
    this.fullNameScore    = null;
    this.surnameScore     = null;
    this.givenNameScore   = null;
    this.generationScore  = null;
    this.orgNameScore     = null;
  }

  /**
   * Gets the full name score if one exists.  This method returns <tt>null</tt>
   * if there is no full name score.
   *
   * @return The full name score, or <tt>null</tt> if there is no full name
   *         score.
   */
  @JsonInclude(NON_EMPTY)
  public Integer getFullNameScore() {
    return fullNameScore;
  }

  /**
   * Sets the full name score if one exists.  Set the value to <tt>null</tt>
   * if there is no full name score.
   *
   * @param score The full name score, or <tt>null</tt> if there is no full
   *              name score.
   */
  public void setFullNameScore(Integer score) {
    this.fullNameScore = score;
  }

  /**
   * Gets the surname score if one exists.  This method returns <tt>null</tt>
   * if there is no surname score.
   *
   * @return The surname score, or <tt>null</tt> if there is no surname score.
   */
  @JsonInclude(NON_EMPTY)
  public Integer getSurnameScore() {
    return surnameScore;
  }

  /**
   * Sets the surname score if one exists.  Set the value to <tt>null</tt>
   * if there is no surname score.
   *
   * @param score The surname score, or <tt>null</tt> if there is no surname
   *              score.
   */
  public void setSurnameScore(Integer score) {
    this.surnameScore = score;
  }

  /**
   * Gets the given name score if one exists.  This method returns <tt>null</tt>
   * if there is no given name score.
   *
   * @return The given name score, or <tt>null</tt> if there is no given name
   *         score.
   */
  @JsonInclude(NON_EMPTY)
  public Integer getGivenNameScore() {
    return givenNameScore;
  }

  /**
   * Sets the given name score if one exists.  Set the value to <tt>null</tt>
   * if there is no given name score.
   *
   * @param score The given name score, or <tt>null</tt> if there is no
   *              given name score.
   */
  public void setGivenNameScore(Integer score) {
    this.givenNameScore = score;
  }

  /**
   * Gets the generation match score if one exists.  This method returns
   * <tt>null</tt> if there is no generation match score.
   *
   * @return The generation match score, or <tt>null</tt> if there is no
   *         generation match score.
   */
  @JsonInclude(NON_EMPTY)
  public Integer getGenerationScore() {
    return generationScore;
  }

  /**
   * Sets the generation match score if one exists.  Set the value to
   * <tt>null</tt> if there is no generation match score.
   *
   * @param score The generation match score, or <tt>null</tt> if there is no
   *              generation match score.
   */
  public void setGenerationScore(Integer score) {
    this.generationScore = score;
  }

  /**
   * Gets the organization name score if one exists.  This method returns
   * <tt>null</tt> if there is no organization name score.
   *
   * @return The organization name score, or <tt>null</tt> if there is no
   *         organization name score.
   */
  @JsonInclude(NON_EMPTY)
  public Integer getOrgNameScore() {
    return orgNameScore;
  }

  /**
   * Sets the organization name score if one exists.  Set the value to
   * <tt>null</tt> if there is no organization name score.
   *
   * @param score The organization name score, or <tt>null</tt> if there is no
   *              organization name score.
   */
  public void setOrgNameScore(Integer score) {
    this.orgNameScore = score;
  }

  @Override
  public String toString() {
    return "SzNameScoring{" +
        "fullNameScore=" + fullNameScore +
        ", surnameScore=" + surnameScore +
        ", givenNameScore=" + givenNameScore +
        ", generationScore=" + generationScore +
        ", orgNameScore=" + orgNameScore +
        '}';
  }
}
