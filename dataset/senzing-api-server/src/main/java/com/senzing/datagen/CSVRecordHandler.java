package com.senzing.datagen;

import com.senzing.util.JsonUtilities;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static com.senzing.datagen.GeneratedAttributeType.*;

/**
 * Provides a {@link RecordHandler} implementation to output records to a CSV.
 */
public class CSVRecordHandler implements RecordHandler {
  /**
   * Internal nested class to describe a column of the CSV.
   */
  private static class Column {
    private String headerName;

    private String mappedName;

    private GeneratedAttributeType attrType;

    private UsageType usageType;

    private Column(String headerName) {
      this(headerName, null, null);
    }

    private Column(String                 headerName,
                   GeneratedAttributeType attrType,
                   UsageType              usageType)
    {
      this(headerName, headerName, attrType, usageType);
    }

    private Column(String                 headerName,
                   String                 mappedName,
                   GeneratedAttributeType attrType,
                   UsageType              usageType)
    {
      this.headerName   = headerName;
      this.mappedName   = mappedName;
      this.attrType     = attrType;
      this.usageType    = usageType;
    }
  }

  /**
   * The {@link List} of columns.
   */
  private List<Column> columns;

  /**
   * The writer for directing the CSV text at.
   */
  private Writer writer;

  /**
   * The {@link CSVPrinter} for generating the CSV.
   */
  private CSVPrinter csvPrinter;

  /**
   *
   * @param writer The {@link Writer} to push the CSV text to.
   *
   * @param featureGenMap The feature generation map that typically was used
   *                      to generate the records and will be used to determine
   *                      the CSV columns.
   */
  public CSVRecordHandler(
      Writer                            writer,
      boolean                           includeRecordId,
      boolean                           includeDataSource,
      Map<FeatureType, Set<UsageType>>  featureGenMap,
      Set<RecordType>                   recordTypes,
      boolean                           fullValues)
    throws IOException
  {
    this.columns = new LinkedList<>();
    if (includeRecordId) {
      this.columns.add(new Column("RECORD_ID"));
    }

    if (includeDataSource) {
      this.columns.add(new Column("DATA_SOURCE"));
    }

    featureGenMap.entrySet().forEach(entry -> {
      FeatureType     featureType = entry.getKey();
      Set<UsageType>  usageTypes  = entry.getValue();

      // determine the attribute types
      Set<GeneratedAttributeType> attrTypes = new LinkedHashSet<>();
      for (RecordType recordType: recordTypes) {
        if (fullValues) {
          attrTypes.add(fullValueInstance(featureType, recordType));
        } else {
          attrTypes.addAll(partValuesInstances(featureType, recordType));
        }
      }

      // iterate over the usage types
      usageTypes.forEach(usageType -> {
        // iterate over the attribute types
        for (GeneratedAttributeType attrType: attrTypes) {
          String headerName = ((usageType == null) ? "" : usageType + "_")
              + attrType;

          Column column = new Column(headerName, attrType, usageType);

          this.columns.add(column);
        }
      });
    });

    // convert the columns list into an ArrayList
    this.columns = new ArrayList<>(this.columns);

    // get the headers
    String[] headers = new String[ this.columns.size() ];
    for (int index = 0; index < this.columns.size(); index++) {
      headers[index] = this.columns.get(index).mappedName;
    }

    // set the writer
    this.writer = writer;

    // create the printer
    this.csvPrinter = new CSVPrinter(
        this.writer, CSVFormat.DEFAULT.withHeader(headers));
  }

  /**
   * Handles a generated record that has been generated as part of the specified
   * {@link JsonObject}.
   *
   * @param recordBuilder The {@link JsonObject} describing the record.
   */
  public void handle(JsonObjectBuilder recordBuilder) {
    if (this.csvPrinter == null) {
      throw new IllegalStateException(
          "The handler has already been closed.");
    }

    // build the object
    JsonObject record = recordBuilder.build();

    // check if object is empty
    if (record.size() == 0) return;

    try {
      List<String> recordValues = new ArrayList<>(this.columns.size());
      for (Column column : this.columns) {
        recordValues.add(this.getFeatureValue(record, column));
      }
      this.csvPrinter.printRecord(recordValues);
      this.csvPrinter.flush();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the value for the specified {@link Column} from the record
   * described by the specified {@link JsonObject}.
   *
   * @param record The {@link JsonObject} describing the record.
   * @param column The {@link Column} value to retrieve.
   */
  private String getFeatureValue(JsonObject record, Column column) {
    GeneratedAttributeType attrType = column.attrType;

    FeatureType featureType = (attrType == null)
        ? null : attrType.getFeatureType();

    String pluralProp = (featureType == null)
        ? null : featureType.getPluralProperty();

    UsageType usageType = column.usageType;

    GeneratedAttributeType typeAttr
        = GeneratedAttributeType.usageTypeInstance(featureType);

    String typeProp   = (typeAttr == null) ? null : typeAttr.toString();

    String usageValue = (usageType == null) ? "" : usageType.toString();

    String attrProp   = (attrType == null) ? column.headerName
        : (usageValue + (usageType == null ? "" : "_") + attrType);

    String value = null;

    // look for a direct occurrence of the property (no nesting)
    if (record.containsKey(attrProp)) {
      // get the value directly (flat JSON)
      return record.getString(attrProp);

    }

    if ((attrProp != null) && record.containsKey(pluralProp)) {
      // get the array of values when they are nested
      JsonArray jsonArray = record.getJsonArray(pluralProp);

      // iterate over the values and look for the attribute property key
      for (JsonObject jsonObj: jsonArray.getValuesAs(JsonObject.class)) {
        // check if the property with usage prefix in the current JSON object
        if (jsonObj.containsKey(attrProp)) {
          // the JSON property exist with a prefixed usage type
          return jsonObj.getString(attrProp);

        } else if (jsonObj.containsKey(attrType.toString())) {
          // the JSON property exists without a prefix, confirm the usage type
          String usage = JsonUtilities.getString(jsonObj, typeProp, "");
          if (usage.trim().equalsIgnoreCase(usageValue)) {
            return jsonObj.getString(attrType.toString());
          }
        }
      }
    }

    // if we get here then return null
    return null;
  }

  /**
   * Completes the handling of the records.
   */
  public void close() {
    // flush the CSV printer
    if (this.csvPrinter != null) {
      try {
        this.csvPrinter.flush();
      } catch (Exception ignore) {
        // do nothing
      }
    }

    // flush the writer
    if (this.writer != null) {
      try {
        this.writer.flush();
      } catch (Exception ignore) {
        // do nothing
      }
    }

    // close the CSV printer
    if (this.csvPrinter != null) {
      try {
        this.csvPrinter.close();
      } catch (Exception ignore) {
        // do nothing
      }
      this.csvPrinter = null;
    }

    // close the writer
    if (this.writer != null) {
      try {
        this.writer.close();
      } catch (Exception ignore) {
        // do nothing
      }
      this.writer = null;
    }
  }
}
