package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzDataSourceRecordSummaryImpl;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a record summary by data source.
 */
@JsonDeserialize(using=SzDataSourceRecordSummary.Factory.class)
public interface SzDataSourceRecordSummary {
  /**
   * Returns the associated data source.
   *
   * @return The associated data source.
   */
  String getDataSource();

  /**
   * Sets the associated data source.
   *
   * @param dataSource The data source for the summary.
   */
  void setDataSource(String dataSource);

  /**
   * Returns the record count for the summary.
   *
   * @return The record count for the summary.
   */
  int getRecordCount();

  /**
   * Sets the record count for the summary.
   *
   * @param recordCount The number of records in the entity from the
   *                    data source.
   */
  void setRecordCount(int recordCount);

  /**
   * Returns an unmodifiable {@link List} of the top record IDs.
   *
   * @return An unmodifiable {@link List} of the top record IDs.
   */
  List<String> getTopRecordIds();

  /**
   * Sets the top record IDs to the specified {@link List} of record IDs.
   *
   * @param topRecordIds The top record IDs for the data source.
   */
  void setTopRecordIds(List<String> topRecordIds);

  /**
   * Adds a record ID to the {@link List} of top record IDs for the summary.
   *
   * @param recordId The record ID to add to the list of top record IDs.
   */
  void addTopRecordId(String recordId);

  /**
   * A {@link ModelProvider} for instances of {@link SzDataSourceRecordSummary}.
   */
  interface Provider extends ModelProvider<SzDataSourceRecordSummary> {
    /**
     * Creates a new instance of {@link SzDataSourceRecordSummary}.
     *
     * @return The new instance of {@link SzDataSourceRecordSummary}
     */
    SzDataSourceRecordSummary create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzDataSourceRecordSummary} that produces instances of {@link
   * SzDataSourceRecordSummaryImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzDataSourceRecordSummary>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzDataSourceRecordSummary.class, SzDataSourceRecordSummaryImpl.class);
    }

    @Override
    public SzDataSourceRecordSummary create() {
      return new SzDataSourceRecordSummaryImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link
   * SzDataSourceRecordSummary}.
   */
  class Factory extends ModelFactory<SzDataSourceRecordSummary, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzDataSourceRecordSummary.class);
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
     * Creates a new instance of {@link SzDataSourceRecordSummary}.
     * @return The new instance of {@link SzDataSourceRecordSummary}.
     */
    public SzDataSourceRecordSummary create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a list of {@link SzDataSourceRecordSummary} instances from native API JSON
   * format and populates the specified {@link List} or creates a new {@link
   * List} if the specified {@link List} is <tt>null</tt>.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new list
   *             should be created.
   *
   * @param jsonArray The {@link JsonArray} describing the list of record
   *                  summaries in the Senzing native API JSON format.
   *
   * @return The specified {@link List} that was populated or the new
   *         {@link List} that was created.
   */
  static List<SzDataSourceRecordSummary> parseRecordSummaryList(
      List<SzDataSourceRecordSummary> list,
      JsonArray                       jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseRecordSummary(null, jsonObject));
    }
    return list;
  }

  /**
   * Parses the native API JSON and creates or populates an
   * {@link SzDataSourceRecordSummary}.
   *
   * @param summary The summary to populate or <tt>null</tt> if a new
   *                instance should be created.
   *
   * @param jsonObject The {@link JsonObject} to parse.
   *
   * @return The {@link SzDataSourceRecordSummary} that was specified or created.
   */
  static SzDataSourceRecordSummary parseRecordSummary(
      SzDataSourceRecordSummary summary,
      JsonObject                jsonObject)
  {
    if (summary == null) summary = SzDataSourceRecordSummary.FACTORY.create();

    String dataSource  = jsonObject.getString("DATA_SOURCE");
    int    recordCount = jsonObject.getJsonNumber("RECORD_COUNT").intValue();

    summary.setDataSource(dataSource);
    summary.setRecordCount(recordCount);

    return summary;
  }
}
