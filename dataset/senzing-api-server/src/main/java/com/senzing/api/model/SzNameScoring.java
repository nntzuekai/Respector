package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzNameScoringImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Describes the various scoring values between two name feature values.
 */
@JsonDeserialize(using=SzNameScoring.Factory.class)
public interface SzNameScoring {
  /**
   * Gets the full name score if one exists.  This method returns <tt>null</tt>
   * if there is no full name score.
   *
   * @return The full name score, or <tt>null</tt> if there is no full name
   *         score.
   */
  @JsonInclude(NON_EMPTY)
  Integer getFullNameScore();

  /**
   * Sets the full name score if one exists.  Set the value to <tt>null</tt>
   * if there is no full name score.
   *
   * @param score The full name score, or <tt>null</tt> if there is no full
   *              name score.
   */
  void setFullNameScore(Integer score);

  /**
   * Gets the surname score if one exists.  This method returns <tt>null</tt>
   * if there is no surname score.
   *
   * @return The surname score, or <tt>null</tt> if there is no surname score.
   */
  @JsonInclude(NON_EMPTY)
  Integer getSurnameScore();

  /**
   * Sets the surname score if one exists.  Set the value to <tt>null</tt>
   * if there is no surname score.
   *
   * @param score The surname score, or <tt>null</tt> if there is no surname
   *              score.
   */
  void setSurnameScore(Integer score);

  /**
   * Gets the given name score if one exists.  This method returns <tt>null</tt>
   * if there is no given name score.
   *
   * @return The given name score, or <tt>null</tt> if there is no given name
   *         score.
   */
  @JsonInclude(NON_EMPTY)
  Integer getGivenNameScore();

  /**
   * Sets the given name score if one exists.  Set the value to <tt>null</tt>
   * if there is no given name score.
   *
   * @param score The given name score, or <tt>null</tt> if there is no
   *              given name score.
   */
  void setGivenNameScore(Integer score);

  /**
   * Gets the generation match score if one exists.  This method returns
   * <tt>null</tt> if there is no generation match score.
   *
   * @return The generation match score, or <tt>null</tt> if there is no
   *         generation match score.
   */
  @JsonInclude(NON_EMPTY)
  Integer getGenerationScore();

  /**
   * Sets the generation match score if one exists.  Set the value to
   * <tt>null</tt> if there is no generation match score.
   *
   * @param score The generation match score, or <tt>null</tt> if there is no
   *              generation match score.
   */
  void setGenerationScore(Integer score);

  /**
   * Gets the organization name score if one exists.  This method returns
   * <tt>null</tt> if there is no organization name score.
   *
   * @return The organization name score, or <tt>null</tt> if there is no
   *         organization name score.
   */
  @JsonInclude(NON_EMPTY)
  Integer getOrgNameScore();

  /**
   * Sets the organization name score if one exists.  Set the value to
   * <tt>null</tt> if there is no organization name score.
   *
   * @param score The organization name score, or <tt>null</tt> if there is no
   *              organization name score.
   */
  void setOrgNameScore(Integer score);

  /**
   * Converts the value into the best value for a "full score".  This looks for
   * values in the follow fields and returns them if not <tt>null</tt> in the
   * following order of precedence:
   * <ol>
   *   <li>{@link #getOrgNameScore()}</li>
   *   <li>{@link #getFullNameScore()}</li>
   *   <li>{@link #getSurnameScore()}</li>
   *   <li>{@link #getGivenNameScore()}</li>
   * </ol>
   *
   * @return The "best" {@link Integer} value for the full score.
   */
  default Integer asFullScore() {
    if (this.getOrgNameScore()    != null)  return this.getOrgNameScore();
    if (this.getFullNameScore()   != null)  return this.getFullNameScore();
    if (this.getSurnameScore()    != null)  return this.getSurnameScore();
    if (this.getGivenNameScore()  != null)  return this.getGivenNameScore();
    return null;
  }

  /**
   * A {@link ModelProvider} for instances of {@link SzNameScoring}.
   */
  interface Provider extends ModelProvider<SzNameScoring> {
    /**
     * Creates a new instance of {@link SzNameScoring}.
     *
     * @return The new instance of {@link SzNameScoring}
     */
    SzNameScoring create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzNameScoring} that produces instances of
   * {@link SzNameScoringImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzNameScoring>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzNameScoring.class, SzNameScoringImpl.class);
    }

    @Override
    public SzNameScoring create() {
      return new SzNameScoringImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzNameScoring}.
   */
  class Factory extends ModelFactory<SzNameScoring, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzNameScoring.class);
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
     * Creates a new instance of {@link SzNameScoring}.
     * @return The new instance of {@link SzNameScoring}.
     */
    public SzNameScoring create() {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the {@link SzNameScoring} from a {@link JsonObject} describing JSON
   * for the Senzing native API format for a name score to create a new
   * instance.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzFeatureScore} that was created.
   */
  static SzNameScoring parseNameScoring(JsonObject jsonObject) {
    Integer fnScore  = JsonUtilities.getInteger(jsonObject, "GNR_FN");
    Integer snScore  = JsonUtilities.getInteger(jsonObject, "GNR_SN");
    Integer gnScore  = JsonUtilities.getInteger(jsonObject, "GNR_GN");
    Integer genScore = JsonUtilities.getInteger(jsonObject, "GENERATION_MATCH");
    Integer orgScore = JsonUtilities.getInteger(jsonObject, "GNR_ON");

    // check if there are no name scoring values (we do this before converting
    // negative values to null -- checking for missing JSON properties)
    if ((fnScore == null) && (snScore == null) && (gnScore == null)
        && (genScore == null) && (orgScore == null)) {
      return null;
    }

    // check for negative values and set to null if found
    if (fnScore < 0)  fnScore = null;
    if (snScore < 0)  snScore = null;
    if (gnScore < 0)  gnScore = null;
    if (genScore < 0) genScore = null;
    if (orgScore < 0) orgScore = null;

    // construct the result
    SzNameScoring result = SzNameScoring.FACTORY.create();

    // set the score values
    result.setFullNameScore(fnScore);
    result.setSurnameScore(snScore);
    result.setGivenNameScore(gnScore);
    result.setGenerationScore(genScore);
    result.setOrgNameScore(orgScore);

    // return the result
    return result;
  }
}
