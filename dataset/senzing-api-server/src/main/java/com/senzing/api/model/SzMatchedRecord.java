package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzMatchedRecordImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Describes an entity record that has matched another record in a resolved
 * entity.
 */
@JsonDeserialize(using=SzMatchedRecord.Factory.class)
public interface SzMatchedRecord extends SzEntityRecord {
  /**
   * Gets the match level for how this record matched against the first record
   * in the resolved entity.
   *
   * @return The match level for how this record matched against the first
   *         record in the resolved entity.
   */
  @JsonInclude(NON_EMPTY)
  Integer getMatchLevel();

  /**
   * Sets the match level for how this record matched against the first record
   * in the resolved entity.
   *
   * @param matchLevel The match level for how this record matched against the
   *                   first record in the resolved entity.
   */
  void setMatchLevel(Integer matchLevel);

  /**
   * Gets the match key for how this record matched against the first record
   * in the resolved entity.
   *
   * @return The match key for how this record matched against the first record
   *         in the resolved entity.
   */
  @JsonInclude(NON_EMPTY)
  String getMatchKey();

  /**
   * Sets the match key for how this record matched against the first record
   * in the resolved entity.
   *
   * @param matchKey The match key for how this record matched against the
   *                 first record in the resolved entity.
   */
  void setMatchKey(String matchKey);

  /**
   * Gets the resolution rule code for how this record matched against the
   * first record in the resolved entity.
   *
   * @return The resolution rule code for how this record matched against the
   *         first record in the resolved entity.
   */
  @JsonInclude(NON_EMPTY)
  String getResolutionRuleCode();

  /**
   * Sets the resolution rule code for how this record matched against the
   * first record in the resolved entity.
   *
   * @param resolutionRuleCode The resolution rule code for how this record
   *                           matched against the first record in the
   *                           resolved entity.
   */
  void setResolutionRuleCode(String resolutionRuleCode);

  /**
   * A {@link ModelProvider} for instances of {@link SzMatchedRecord}.
   */
  interface Provider extends ModelProvider<SzMatchedRecord> {
    /**
     * Creates a new instance of {@link SzMatchedRecord}.
     *
     * @return The new instance of {@link SzMatchedRecord}
     */
    SzMatchedRecord create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzMatchedRecord} that produces instances of {@link SzMatchedRecordImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzMatchedRecord>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzMatchedRecord.class, SzMatchedRecordImpl.class);
    }

    @Override
    public SzMatchedRecord create() {
      return new SzMatchedRecordImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzMatchedRecord}.
   */
  class Factory extends ModelFactory<SzMatchedRecord, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzMatchedRecord.class);
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
     * Creates a new instance of {@link SzMatchedRecord}.
     * @return The new instance of {@link SzMatchedRecord}.
     */
    public SzMatchedRecord create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses the native JSON to construct/populate a {@link List}
   * of {@link SzMatchedRecord} instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new {@link
   *             List} should be created.
   * @param jsonArray The {@link JsonArray} describing the native JSON array.
   *
   * @return The specified (or constructed) {@link List} of {@link
   *         SzMatchedRecord} instances.
   */
  static List<SzMatchedRecord> parseMatchedRecordList(
      List<SzMatchedRecord>  list,
      JsonArray             jsonArray)
  {
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      if (list == null) {
        list = new ArrayList<>(jsonArray.size());
      }
      list.add(parseMatchedRecord(null, jsonObject));
    }
    if (list != null) {
      list = Collections.unmodifiableList(list);
    } else {
      list = Collections.emptyList();
    }
    return list;
  }

  /**
   * Parses the native API JSON to build an populate or create an instance of
   * {@link SzMatchedRecord}.
   *
   * @param record The {@link SzMatchedRecord} to populate or <tt>null</tt> if
   *               a new instance should be created.
   *
   * @param jsonObject The {@link JsonObject} describing the record using the
   *                   native API JSON format.
   *
   * @return The populated (or created) instance of {@link SzMatchedRecord}
   */
  static SzMatchedRecord parseMatchedRecord(SzMatchedRecord record,
                                            JsonObject      jsonObject)
  {
    if (record == null) record = SzMatchedRecord.FACTORY.create();

    // populate from the base class
    SzEntityRecord.parseEntityRecord(record, jsonObject);

    // now get the match fields
    String  matchKey    = JsonUtilities.getString(jsonObject, "MATCH_KEY");
    Integer matchLevel  = JsonUtilities.getInteger(jsonObject, "MATCH_LEVEL");
    String  ruleCode    = JsonUtilities.getString(jsonObject,"ERRULE_CODE");

    record.setMatchKey(matchKey);
    record.setMatchLevel(matchLevel);
    record.setResolutionRuleCode(ruleCode);

    return record;
  }
}
