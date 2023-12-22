package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourceDescriptorsImpl;
import com.senzing.util.JsonUtilities;

import javax.json.*;
import java.util.*;

/**
 * Used to represent a {@link List} of zero or more {@link SzDataSourceDescriptor}
 * instances.
 *
 */
@JsonDeserialize(using=SzDataSourceDescriptors.Factory.class)
public interface SzDataSourceDescriptors {
  /**
   * Checks if all the {@link SzDataSourceDescriptor} instances contained are of the
   * same type (e.g.: either {@link SzEntityId} or {@link SzRecordId}).
   *
   * @return <tt>true</tt> if the {@link SzDataSourceDescriptor} instances are
   *         of the same type otherwise <tt>false</tt>.
   */
  default boolean isHomogeneous() {
    Boolean expectCodes = null;
    Class<? extends SzDataSourceDescriptor> c = null;
    for (SzDataSourceDescriptor i : this.getDescriptors()) {
      boolean isCode = (i instanceof SzDataSourceCode);
      if (expectCodes == null) {
        expectCodes = isCode;
        continue;
      }
      if (expectCodes != isCode) return false;
    }
    return true;
  }

  /**
   * Checks if there are no entity descriptors specified for this instance.
   *
   * @return <tt>true</tt> if no entity descriptors are specified, otherwise
   *         <tt>false</tt>.
   */
  default boolean isEmpty() {
    List<SzDataSourceDescriptor> list = this.getDescriptors();
    return (list == null || list.size() == 0);
  }

  /**
   * Returns the number of entity descriptors.
   *
   * @return The number of entity descriptors.
   */
  default int getCount() {
    List<SzDataSourceDescriptor> list = this.getDescriptors();
    return (list == null ? 0 : list.size());
  }

  /**
   * Returns the unmodifiable {@link List} of {@link SzDataSourceDescriptor}
   * instances that were specified.
   *
   * @return The unmodifiable {@link List} of {@link SzDataSourceDescriptor}
   *         instances that were specified.
   */
  List<SzDataSourceDescriptor> getDescriptors();

  /**
   * A {@link ModelProvider} for instances of {@link SzDataSourceDescriptors}.
   */
  interface Provider extends ModelProvider<SzDataSourceDescriptors> {
    /**
     * Constructs an instance with no {@link SzDataSourceDescriptor} instances.
     */
    SzDataSourceDescriptors create();

    /**
     * Constructs an instance with a single {@link SzDataSourceDescriptor}
     * instance.
     *
     * @param identifier The single non-null {@link SzDataSourceDescriptor}
     *                   instance.
     *
     * @throws NullPointerException If the specified parameter is null.
     */
    SzDataSourceDescriptors create(SzDataSourceDescriptor identifier)
        throws NullPointerException;

    /**
     * Constructs with the specified {@link Collection} of {@link
     * SzDataSourceDescriptor} instances.  The specified {@link Collection} will be
     * copied.
     *
     * @param descriptors The non-null {@link Collection} of {@link
     *                    SzDataSourceDescriptor} instances.
     *
     * @throws NullPointerException If the specified parameter is null.
     */
    SzDataSourceDescriptors create(
        Collection<? extends SzDataSourceDescriptor> descriptors)
        throws NullPointerException;
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSourceDescriptor} that produces instances of {@link
   * SzDataSourceDescriptorsImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDataSourceDescriptors>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSourceDescriptors.class, SzDataSourceDescriptorsImpl.class);
    }

    @Override
    public SzDataSourceDescriptors create() {
      return new SzDataSourceDescriptorsImpl();
    }

    @Override
    public SzDataSourceDescriptors create(SzDataSourceDescriptor identifier)
        throws NullPointerException
    {
      return new SzDataSourceDescriptorsImpl(identifier);
    }

    @Override
    public SzDataSourceDescriptors create(
        Collection<? extends SzDataSourceDescriptor> descriptors)
        throws NullPointerException
    {
      return new SzDataSourceDescriptorsImpl(descriptors);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzDataSourceDescriptors}.
   */
  class Factory extends ModelFactory<SzDataSourceDescriptors, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSourceDescriptors.class);
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
     * Constructs an instance with no {@link SzDataSourceDescriptor} instances.
     */
    public SzDataSourceDescriptors create() {
      return this.getProvider().create();
    }

    /**
     * Constructs an instance with a single {@link SzDataSourceDescriptor}
     * instance.
     *
     * @param identifier The single non-null {@link SzDataSourceDescriptor}
     *                   instance.
     *
     * @throws NullPointerException If the specified parameter is null.
     */
    public SzDataSourceDescriptors create(SzDataSourceDescriptor identifier)
        throws NullPointerException
    {
      return this.getProvider().create(identifier);
    }

    /**
     * Constructs with the specified {@link Collection} of {@link
     * SzDataSourceDescriptor} instances.  The specified {@link Collection} will be
     * copied.
     *
     * @param descriptors The non-null {@link Collection} of {@link
     *                    SzDataSourceDescriptor} instances.
     *
     * @throws NullPointerException If the specified parameter is null.
     */
    public SzDataSourceDescriptors create(
        Collection<? extends SzDataSourceDescriptor> descriptors)
        throws NullPointerException
    {
      return this.getProvider().create(descriptors);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the specified text as a {@link List} of homogeneous
   * {@link SzDataSourceDescriptor} instances.
   *
   * @param text The text to parse.
   *
   * @return The {@link SzDataSourceDescriptors} instance representing the {@link
   *         List} of {@link SzDataSourceDescriptor} instances.
   */
  static SzDataSourceDescriptors valueOf(String text) {
    if (text != null) text = text.trim();
    int               length  = (text == null) ? 0 : text.length();
    char              first   = (length == 0) ? 0 : text.charAt(0);
    char              last    = (length <= 1) ? 0 : text.charAt(length-1);

    // check if no descriptors
    if (length == 0) {
      // no descriptors
      return SzDataSourceDescriptors.FACTORY.create();
    }

    // check if it looks like a JSON array
    if (first == '[' && last == ']') {
      try {
        return parseAsJsonArray(text);

      } catch (RuntimeException e) {
        // ignore
      }
    }

    // try to convert it to a JSON array
    if (first != '[' && last != ']') {
      // check if it is a quoted string or a JSON object
      if ((first == '"' && last == '"') || (first == '{' && last == '}')) {
        // if its already a quoted string or object, surround it in brackets
        try {
          return parseAsJsonArray("[" + text + "]");

        } catch (RuntimeException e) {
          // ignore
        }
      }

      // if we get here then assume it is an unquoted string representing a
      // data source code and build a JSON string array
      JsonArrayBuilder jab = Json.createArrayBuilder();
      jab.add(text);
      String jsonText = JsonUtilities.toJsonText(jab);
      return parseAsJsonArray(jsonText);
    }

    // if we get here then check for a failure
    throw new IllegalArgumentException(
        "Unable to interpret the text as a list of data source descriptors: "
        + text);
  }

  /**
   * Parses the specified text as a JSON array.
   *
   * @param text The text to parse
   * @return The {@link SzDataSourceDescriptors} that was parsed.
   */
  private static SzDataSourceDescriptors parseAsJsonArray(String text) {
    // it appears we have a JSON array of entity descriptors
    JsonArray jsonArray = JsonUtilities.parseJsonArray(text);
    List<SzDataSourceDescriptor> descriptors
        = new ArrayList<>(jsonArray.size());
    JsonValue.ValueType valueType = null;
    for (JsonValue value : jsonArray) {
      JsonValue.ValueType vt = value.getValueType();
      SzDataSourceDescriptor descriptor;
      switch (vt) {
        case STRING:
          descriptor = SzDataSourceCode.FACTORY.create(
              ((JsonString) value).getString());
          break;

        case OBJECT:
          descriptor = SzDataSource.parse((JsonObject) value);
          break;

        default:
          throw new IllegalArgumentException(
              "Unexpected element in entity descriptor array: valueType=[ "
                  + valueType + " ], value=[ " + value + " ]");
      }
      descriptors.add(descriptor);
    }

    // make the list unmodifiable
    return SzDataSourceDescriptors.FACTORY.create(
        Collections.unmodifiableList(descriptors));
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
        SzDataSourceDescriptors descriptors = SzDataSourceDescriptors.valueOf(arg);
        System.out.println(descriptors.getDescriptors());

      } catch (Exception e) {
        e.printStackTrace(System.out);
      }

    }
  }
}
