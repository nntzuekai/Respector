package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourceImpl;
import com.senzing.util.JsonUtilities;

import javax.json.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Describes a data source in its entirety.
 */
@JsonDeserialize(using=SzDataSource.Factory.class)
public interface SzDataSource extends SzDataSourceDescriptor {
  /**
   * Gets the data source code for the data source.
   *
   * @return The data source code for the data source.
   */
  String getDataSourceCode();

  /**
   * Sets the data source code for the data source.
   *
   * @param code The data source code for the data source.
   */
  void setDataSourceCode(String code);

  /**
   * Return the data source ID associated with the data source.
   *
   * @return The data source ID associated with the data source.
   */
  Integer getDataSourceId();

  /**
   * Sets the data source ID associated with the data source.
   *
   * @param dataSourceId The data source ID associated with the data source.
   */
  void setDataSourceId(Integer dataSourceId);

  /**
   * A {@link ModelProvider} for instances of {@link SzDataSource}.
   */
  interface Provider extends ModelProvider<SzDataSource> {
    /**
     * Constructs an uninitialized instance.
     */
    SzDataSource create();

    /**
     * Constructs with the specified data source code and a <tt>null</tt>
     * data source ID.
     *
     * @param dataSourceCode The data source code for the data source.
     */
    SzDataSource create(String dataSourceCode);

    /**
     * Constructs with the specified data source code and data source ID.
     *
     * @param dataSourceCode The data source code for the data source.
     * @param dataSourceId The data source ID for the data source, or
     *                     <tt>null</tt> if the data source ID is not
     *                     specified.
     */
    SzDataSource create(String dataSourceCode, Integer dataSourceId);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSource} that produces instances of {@link SzDataSourceImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDataSource>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSource.class, SzDataSourceImpl.class);
    }

    @Override
    public SzDataSource create() {
      return new SzDataSourceImpl();
    }

    @Override
    public SzDataSource create(String dataSourceCode) {
      return new SzDataSourceImpl(dataSourceCode);
    }

    @Override
    public SzDataSource create(String dataSourceCode, Integer dataSourceId) {
      return new SzDataSourceImpl(dataSourceCode, dataSourceId);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzDataSource}.
   */
  class Factory extends ModelFactory<SzDataSource, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSource.class);
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
     * Constructs an uninitialized instance.
     */
    public SzDataSource create() {
      return this.getProvider().create();
    }

    /**
     * Constructs with the specified data source code and a <tt>null</tt>
     * data source ID.
     *
     * @param dataSourceCode The data source code for the data source.
     */
    public SzDataSource create(String dataSourceCode) {
      return this.getProvider().create(dataSourceCode);
    }

    /**
     * Constructs with the specified data source code and data source ID.
     *
     * @param dataSourceCode The data source code for the data source.
     * @param dataSourceId The data source ID for the data source, or
     *                     <tt>null</tt> if the data source ID is not
     *                     specified.
     */
    public SzDataSource create(String dataSourceCode, Integer dataSourceId) {
      return this.getProvider().create(dataSourceCode, dataSourceId);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the specified JSON text for the data source.
   *
   * @param text The JSON text to parse.
   * @return The {@link SzDataSource} that was created.
   */
  static SzDataSource valueOf(String text) {
    try {
      JsonObject jsonObject = JsonUtilities.parseJsonObject(text.trim());

      return SzDataSource.parse(jsonObject);

    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid data source: " + text);
    }
  }

  /**
   * Parses the specified {@link JsonObject} as a data source.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzDataSource} that was created.
   */
  static SzDataSource parse(JsonObject jsonObject) {
    String code = JsonUtilities.getString(jsonObject, "dataSourceCode");
    if (code == null) {
      code = JsonUtilities.getString(jsonObject, "DSRC_CODE");
    }
    Integer id = JsonUtilities.getInteger(jsonObject, "dataSourceId");
    if (id == null) {
      id = JsonUtilities.getInteger(jsonObject, "DSRC_ID");
    }
    return SzDataSource.FACTORY.create(code, id);
  }

  /**
   * Parses a JSON array of the engine API JSON to create or populate a
   * {@link List} of {@link SzDataSource} instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new
   *             {@link List} should be created.
   *
   * @param jsonArray The {@link JsonArray} of {@link JsonObject} instances
   *                  to parse from the engine API.
   *
   * @return The specified (or newly created) {@link List} of 
   *         {@link SzDataSource} instances.
   */
  static List<SzDataSource> parseDataSourceList(
      List<SzDataSource>  list,
      JsonArray           jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseDataSource(null, jsonObject));
    }
    return list;
  }

  /**
   * Parses the engine API JSON to create an instance of {@link SzDataSource}.
   *
   * @param dataSource The {@link SzDataSource} object to initialize or
   *                   <tt>null</tt> if a new one should be created.
   *
   * @param jsonObject The {@link JsonObject} to parse from the engine API.
   *
   * @return The specified (or newly created) {@link SzDataSource}
   */
  static SzDataSource parseDataSource(SzDataSource dataSource,
                                      JsonObject   jsonObject)
  {
    if (dataSource == null) dataSource = SzDataSource.FACTORY.create();

    String  code  = JsonUtilities.getString(jsonObject, "DSRC_CODE");
    Integer id    = JsonUtilities.getInteger(jsonObject, "DSRC_ID");

    if (code == null) {
      throw new IllegalArgumentException(
          "Could not find the DSRC_CODE property");
    }
    if (id == null) {
      throw new IllegalArgumentException(
          "Could not find the DSRC_ID property");
    }

    dataSource.setDataSourceCode(code);
    dataSource.setDataSourceId(id);

    return dataSource;
  }

  /**
   * Returns a reference to this instance.
   *
   * @return The a reference to this instance.
   */
  default SzDataSource toDataSource() {
    return this;
  }

  /**
   * Adds the JSON properties to the specified {@link JsonObjectBuilder} to
   * describe this instance in its standard JSON format.
   *
   * @param builder The {@link JsonObjectBuilder} to add the properties.
   */
  default void buildJson(JsonObjectBuilder builder) {
    builder.add("dataSourceCode", this.getDataSourceCode());
    Integer sourceId = this.getDataSourceId();
    if (sourceId != null) {
      builder.add("dataSourceId", sourceId);
    }
  }

  /**
   * Converts this instance to a {@link JsonObject} representation of this
   * object as native JSON.
   *
   * @param builder The {@link JsonObjectBuilder} to add the JSON properties to.
   */
  default void buildNativeJson(JsonObjectBuilder builder) {
    builder.add("DSRC_CODE", this.getDataSourceCode());
    if (this.getDataSourceId() != null) {
      builder.add("DSRC_ID", this.getDataSourceId());
    }
  }
}
