package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourceCodeImpl;

import javax.json.JsonObjectBuilder;

/**
 * Describes a data source code to identify an data source.
 */
@JsonDeserialize(using=SzDataSourceCode.Factory.class)
public interface SzDataSourceCode extends SzDataSourceDescriptor {
  /**
   * Return the data source code identifying the data source.
   *
   * @return The data source code identifying the data source.
   */
  String getValue();

  /**
   * A {@link ModelProvider} for instances of {@link SzDataSourceCode}.
   */
  interface Provider extends ModelProvider<SzDataSourceCode> {
    /**
     * Constructs with the specified data source code.
     *
     * @param dataSourceCode The data source code for the data source.
     */
    SzDataSourceCode create(String dataSourceCode);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSourceCode} that produces instances of {@link SzDataSourceCodeImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDataSourceCode>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSourceCode.class, SzDataSourceCodeImpl.class);
    }

    @Override
    public SzDataSourceCode create(String dataSourceCode) {
      return new SzDataSourceCodeImpl(dataSourceCode);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzDataSourceCode}.
   */
  class Factory extends ModelFactory<SzDataSourceCode, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSourceCode.class);
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
     * Constructs with the specified data source code.
     *
     * @param dataSourceCode The data source code for the data source.
     */
    public SzDataSourceCode create(String dataSourceCode) {
      return this.getProvider().create(dataSourceCode);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses text as a data source code.  The specified text is trimmed of
   * leading and trailing white space and converted to upper case.  If the
   * specified text is enclosed in double quotes, they are stripped off.
   *
   * @param text The to parse.
   *
   * @return The {@link SzDataSourceCode} that was created.
   *
   * @throws NullPointerException If the specified text is <tt>null</tt>.
   */
  static SzDataSourceCode valueOf(String text) throws NullPointerException {
    if (text.length() > 2 && text.startsWith("\"") && text.endsWith("\"")) {
      text = text.substring(1, text.length() - 1);
    }
    return FACTORY.create(text);
  }

  /**
   * Converts this instance to an instance of {@link SzDataSource}
   * which completely describes a data source with the same
   * data source code and a <tt>null</tt> data source ID.
   *
   * @return The {@link SzDataSource} describing the data source.
   */
  default SzDataSource toDataSource() {
    return SzDataSource.FACTORY.create(this.getValue());
  }

  /**
   * Adds the JSON properties to the specified {@link JsonObjectBuilder} to
   * describe this instance in its standard JSON format.
   *
   * @param builder The {@link JsonObjectBuilder} to add the properties.
   */
  default void buildJson(JsonObjectBuilder builder) {
    builder.add("dataSourceCode", this.getValue());
  }

  /**
   * Implemented to add the <tt>"DSRC_CODE"</tt> field to the specified
   * {@link JsonObjectBuilder}.
   *
   * @param builder The {@link JsonObjectBuilder} to add the properties to.
   */
  default void buildNativeJson(JsonObjectBuilder builder) {
    builder.add("DSRC_CODE", this.getValue());
  }
}
