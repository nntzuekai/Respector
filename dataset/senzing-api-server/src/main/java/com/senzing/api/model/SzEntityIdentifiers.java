package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzEntityIdentifiersImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;

/**
 * Used to represent a {@link List} of zero or more {@link SzEntityIdentifier}
 * instances.
 *
 */
@JsonDeserialize(using=SzEntityIdentifiers.Factory.class)
public interface SzEntityIdentifiers {
  /**
   * Checks if all the {@link SzEntityIdentifier} instances contained are of the
   * same type (e.g.: either {@link SzEntityId} or {@link SzRecordId}).
   *
   * @return <tt>true</tt> if the {@link SzEntityIdentifier} instances are
   *         of the same type otherwise <tt>false</tt>.
   */
  default boolean isHomogeneous() {
    Class<? extends SzEntityIdentifier> c = null;
    for (SzEntityIdentifier i : this.getIdentifiers()) {
      if (c == null) {
        c = i.getClass();
        continue;
      }
      if (c != i.getClass()) return false;
    }
    return true;
  }

  /**
   * Checks if there are no entity identifiers specified for this instance.
   *
   * @return <tt>true</tt> if no entity identifiers are specified, otherwise
   *         <tt>false</tt>.
   */
  default boolean isEmpty() {
    List<SzEntityIdentifier> identifiers = this.getIdentifiers();
    return (identifiers == null || identifiers.size() == 0);
  }

  /**
   * Returns the number of entity identifiers.
   *
   * @return The number of entity identifiers.
   */
  default int getCount() {
    List<SzEntityIdentifier> identifiers = this.getIdentifiers();
    return (identifiers == null ? 0 : identifiers.size());
  }

  /**
   * Returns the unmodifiable {@link List} of {@link SzEntityIdentifier}
   * instances that were specified.
   *
   * @return The unmodifiable {@link List} of {@link SzEntityIdentifier}
   *         instances that were specified.
   */
  List<SzEntityIdentifier> getIdentifiers();

  /**
   * A {@link ModelProvider} for instances of {@link SzEntityIdentifiers}.
   */
  interface Provider extends ModelProvider<SzEntityIdentifiers> {
    /**
     * Constructs an instance with no {@link SzEntityIdentifier} instances.
     */
    SzEntityIdentifiers create();

    /**
     * Constructs an instance with a single {@link SzEntityIdentifier}
     * instance.
     *
     * @param identifier The single non-null {@link SzEntityIdentifier}
     *                   instance.
     *
     * @throws NullPointerException If the specified parameter is null.
     */
    SzEntityIdentifiers create(SzEntityIdentifier identifier)
        throws NullPointerException;

    /**
     * Constructs with the specified {@link Collection} of {@link
     * SzEntityIdentifier} instances.  The specified {@link Collection} will be
     * copied.
     *
     * @param identifiers The non-null {@link Collection} of {@link
     *                    SzEntityIdentifier} instances.
     *
     * @throws NullPointerException If the specified parameter is null.
     */
    SzEntityIdentifiers create(
        Collection<? extends SzEntityIdentifier> identifiers)
        throws NullPointerException;
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzEntityIdentifier} that produces instances of {@link
   * SzEntityIdentifiersImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzEntityIdentifiers>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzEntityIdentifiers.class, SzEntityIdentifiersImpl.class);
    }

    @Override
    public SzEntityIdentifiers create() {
      return new SzEntityIdentifiersImpl();
    }

    @Override
    public SzEntityIdentifiers create(SzEntityIdentifier identifier)
        throws NullPointerException
    {
      return new SzEntityIdentifiersImpl(identifier);
    }

    @Override
    public SzEntityIdentifiers create(
        Collection<? extends SzEntityIdentifier> identifiers)
        throws NullPointerException
    {
      return new SzEntityIdentifiersImpl(identifiers);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzEntityIdentifiers}.
   */
  class Factory extends ModelFactory<SzEntityIdentifiers, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzEntityIdentifiers.class);
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
     * Constructs an instance with no {@link SzEntityIdentifier} instances.
     */
    public SzEntityIdentifiers create() {
      return this.getProvider().create();
    }

    /**
     * Constructs an instance with a single {@link SzEntityIdentifier}
     * instance.
     *
     * @param identifier The single non-null {@link SzEntityIdentifier}
     *                   instance.
     *
     * @throws NullPointerException If the specified parameter is null.
     */
    public SzEntityIdentifiers create(SzEntityIdentifier identifier)
        throws NullPointerException
    {
      return this.getProvider().create(identifier);
    }

    /**
     * Constructs with the specified {@link Collection} of {@link
     * SzEntityIdentifier} instances.  The specified {@link Collection} will be
     * copied.
     *
     * @param identifiers The non-null {@link Collection} of {@link
     *                    SzEntityIdentifier} instances.
     *
     * @throws NullPointerException If the specified parameter is null.
     */
    public SzEntityIdentifiers create(
        Collection<? extends SzEntityIdentifier> identifiers)
        throws NullPointerException
    {
      return this.getProvider().create(identifiers);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the specified text as a {@link List} of homogeneous
   * {@link SzEntityIdentifier} instances.
   *
   * @param text The text to parse.
   *
   * @return The {@link SzEntityIdentifiers} instance representing the {@link
   *         List} of {@link SzEntityIdentifier} instances.
   */
  static SzEntityIdentifiers valueOf(String text) {
    if (text != null) text = text.trim();
    int               length  = (text == null) ? 0 : text.length();
    char              first   = (length == 0) ? 0 : text.charAt(0);
    char              last    = (length <= 1) ? 0 : text.charAt(length-1);

    // check if no identifiers
    if (length == 0) {
      // no identifiers
      return SzEntityIdentifiers.FACTORY.create();
    }

    // check if it looks like a JSON array
    if (first == '[' && last == ']') {
      try {
        return parseAsJsonArray(text);

      } catch (RuntimeException e) {
        // ignore
      }
    }

    // check if we have yet to find the identifiers
    if (first == '{' && last == '}') {
      try {
        // it appears we have a JSON object for a single entity identifier
        JsonObject jsonObject = JsonUtilities.parseJsonObject(text);
        SzRecordId recordId = SzRecordId.parse(jsonObject);
        return SzEntityIdentifiers.FACTORY.create(recordId);

      } catch (RuntimeException e) {
        // ignore
      }
    }

    // check if we have a comma-separated list of entity IDs
    if (text.matches("(-?[\\d]+\\s*,\\s*)+-?[\\d]+")) {
      String[] tokens = text.split("\\s*,\\s*");
      List<SzEntityIdentifier> identifiers = new ArrayList<>(tokens.length);
      for (String token : tokens) {
        token = token.trim();
        identifiers.add(SzEntityId.valueOf(token));
      }
      identifiers = Collections.unmodifiableList(identifiers);
      return SzEntityIdentifiers.FACTORY.create(identifiers);
    }

    // try to convert it to a JSON array
    if (first != '[' && last != ']') {
      try {
        return parseAsJsonArray("[" + text + "]");

      } catch (RuntimeException e) {
        // ignore
      }
    }

    // try to parse as delimited records -- this assumes data source codes
    // and record IDs do not contain commas
    try {
      return parseAsDelimitedTokens(text);

    } catch (RuntimeException e) {
      // ignore
    }

    // if we get here then check for a failure
    throw new IllegalArgumentException(
        "Unable to interpret the text as a list of entity identifiers: "
        + text);
  }

  /**
   * Parses the specified text as a JSON array.
   *
   * @param text The text to parse
   * @return The {@link SzEntityIdentifiers} that was parsed.
   */
  private static SzEntityIdentifiers parseAsJsonArray(String text) {
    // it appears we have a JSON array of entity identifiers
    JsonArray jsonArray = JsonUtilities.parseJsonArray(text);
    List<SzEntityIdentifier> identifiers
        = new ArrayList<>(jsonArray.size());
    JsonValue.ValueType valueType = null;
    for (JsonValue value : jsonArray) {
      JsonValue.ValueType vt = value.getValueType();
      SzEntityIdentifier identifier = null;
      switch (vt) {
        case NUMBER:
          identifier = SzEntityId.FACTORY.create(
              ((JsonNumber) value).longValue());
          break;

        case OBJECT:
          identifier = SzRecordId.parse((JsonObject) value);
          break;

        default:
          throw new IllegalArgumentException(
              "Unexpected element in entity identifier array: valueType=[ "
                  + valueType + " ], value=[ " + value + " ]");
      }
      identifiers.add(identifier);
    }

    // make the list unmodifiable
    return SzEntityIdentifiers.FACTORY.create(
        Collections.unmodifiableList(identifiers));
  }

  /**
   * Parses the specified text as comma-delimited tokens to build an instance
   * of {@link SzEntityIdentifiers}.
   *
   * @param text The text to be parsed.
   * @return The {@link SzEntityIdentifiers} that was parsed.
   */
  private static SzEntityIdentifiers parseAsDelimitedTokens(String text) {
    String[]      rawTokens = text.split("\\s*,\\s*");
    List<String>  tokens    = new ArrayList<>(rawTokens.length);

    String jsonStart = null;
    for (String rawToken : rawTokens) {
      String tok = rawToken.trim();
      // check if we are starting a JSON token
      if (jsonStart == null && tok.startsWith("{") && tok.endsWith("\""))
      {
        // looks like JSON
        jsonStart = rawToken;
        continue;
      }

      // check if we are completing a JSON token
      if (jsonStart != null && tok.startsWith("\"") && tok.endsWith("}")) {
        try {
          String jsonText = jsonStart + "," + rawToken;
          JsonObject obj = JsonUtilities.parseJsonObject(jsonText);

          tokens.add(jsonText);

        } catch (Exception e) {
          // not JSON
          tokens.add(jsonStart);
          tokens.add(rawToken);

        } finally {
          jsonStart = null;
        }
        continue;
      }

      // otherwise just take the raw token as-is
      if (jsonStart != null) tokens.add(jsonStart);
      tokens.add(rawToken);
      jsonStart = null;
    }

    List<SzEntityIdentifier> identifiers = new ArrayList<>(tokens.size());
    for (String token : tokens) {
      token = token.trim();
      identifiers.add(SzEntityIdentifier.valueOf(token));
    }
    identifiers = Collections.unmodifiableList(identifiers);
    return SzEntityIdentifiers.FACTORY.create(identifiers);
  }

  /**
   * Test main function.
   */
  static void main(String[] args) {
    for (String arg : args) {
      System.out.println();
      System.out.println("- - - - - - - - - - - - - - - - - - - - - ");
      System.out.println("PARSING: " + arg);
      try {
        SzEntityIdentifiers identifiers = SzEntityIdentifiers.valueOf(arg);
        System.out.println(identifiers.getIdentifiers());

      } catch (Exception e) {
        e.printStackTrace(System.out);
      }

    }
  }
}
