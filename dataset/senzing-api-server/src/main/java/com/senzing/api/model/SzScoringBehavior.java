package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzScoringBehaviorImpl;

/**
 * Describes the scoring behavior for features of a given feature type.
 */
@JsonDeserialize(using=SzScoringBehavior.Factory.class)
public interface SzScoringBehavior {
  /**
   * Returns the code for the scoring behavior.
   *
   * @return The code for the scoring behavior.
   */
  String getCode();

  /**
   * Gets the {@link SzScoringFrequency} for the scoring behavior.
   *
   * @return The {@link SzScoringFrequency} for the scoring behavior.
   */
  SzScoringFrequency getFrequency();

  /**
   * Checks if the scoring behavior is exclusive.
   *
   * @return <tt>true</tt> if the scoring behavior is exclusive,
   *         otherwise <tt>false</tt>
   */
  boolean isExclusive();

  /**
   * Checks if the scoring behavior is stable.
   *
   * @return <tt>true</tt> if the scoring behavior is stable,
   *         otherwise <tt>false</tt>
   */
  boolean isStable();

  /**
   * A {@link ModelProvider} for instances of {@link SzScoringBehavior}.
   */
  interface Provider extends ModelProvider<SzScoringBehavior> {
    /**
     * Constructs with the specified {@link SzScoringFrequency}, exclusivity
     * and stability flags.
     *
     * @param frequency The {@link SzScoringFrequency} for the scoring behavior.
     * @param exclusive <tt>true</tt> if the feature value is considered
     *                  exclusive, and <tt>false</tt> if not.
     * @param stable <tt>true</tt> if the feature value is considered stable,
     *               and <tt>false</tt> if not.
     */
    SzScoringBehavior create(SzScoringFrequency frequency,
                             boolean            exclusive,
                             boolean            stable);

  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzScoringBehavior} that produces instances of
   * {@link SzScoringBehaviorImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzScoringBehavior>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzScoringBehavior.class, SzScoringBehaviorImpl.class);
    }

    @Override
    public SzScoringBehavior create(SzScoringFrequency frequency,
                                    boolean            exclusive,
                                    boolean            stable)
    {
      return new SzScoringBehaviorImpl(frequency, exclusive, stable);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for
   * {@link SzScoringBehavior}.
   */
  class Factory extends ModelFactory<SzScoringBehavior, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzScoringBehavior.class);
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
     * Constructs with the specified {@link SzScoringFrequency}, exclusivity
     * and stability flags.
     *
     * @param frequency The {@link SzScoringFrequency} for the scoring behavior.
     * @param exclusive <tt>true</tt> if the feature value is considered
     *                  exclusive, and <tt>false</tt> if not.
     * @param stable <tt>true</tt> if the feature value is considered stable,
     *               and <tt>false</tt> if not.
     */
    public SzScoringBehavior create(SzScoringFrequency frequency,
                                    boolean            exclusive,
                                    boolean            stable)
    {
      return this.getProvider().create(frequency, exclusive, stable);
    }

  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the specified text as an {@link SzScoringBehavior}.
   *
   * @param text The text to be parsed.
   *
   * @return The {@link SzScoringBehavior} for the text.
   *
   * @throws IllegalArgumentException If the specified text does not match
   *                                  a known scoring behavior.
   */
  static SzScoringBehavior parse(String text)
      throws IllegalArgumentException {
    String origText = text;
    text = text.trim().toUpperCase();
    SzScoringFrequency frequency = null;
    for (SzScoringFrequency freq : SzScoringFrequency.values()) {
      if (text.startsWith(freq.code())) {
        frequency = freq;
        break;
      }
    }
    if (frequency == null) return null;

    // check if there is remaining text
    int length = frequency.code().length();
    text = (length < text.length()) ? text.substring(length) : "";
    boolean exclusive = false;
    boolean stable = false;

    switch (text) {
      case "ES":
        stable = true;
      case "E":
        exclusive = true;
        break;
      case "":
        exclusive = false;
        stable = false;
        break;
      default:
        throw new IllegalArgumentException(
            "Unrecognized scoring behavior: " + origText);
    }

    // create the instance
    return SzScoringBehavior.FACTORY.create(frequency, exclusive, stable);
  }

  /**
   * Computers the scoring behavior code from the specified {@link
   * SzScoringFrequency}, exclusivity flag and stability flag.
   *
   * @param frequency The {@link SzScoringFrequency} for the scoring behavior.
   * @param exclusive <tt>true</tt> if the feature value is considered
   *                  exclusive, and <tt>false</tt> if not.
   * @param stable <tt>true</tt> if the feature value is considered stable,
   *               and <tt>false</tt> if not.
   *
   * @return The scoring behavior code.
   */
  static String computeCode(SzScoringFrequency  frequency,
                            boolean             exclusive,
                            boolean             stable)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(frequency.code());

    if (exclusive) sb.append("E");
    if (stable)    sb.append("S");

    return sb.toString();
  }
}
