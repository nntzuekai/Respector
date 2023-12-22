package com.senzing.configmgr;

import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;
import static com.senzing.util.LoggingUtilities.*;

/**
 * Describes the result from migrating an INI file to a JSON initialization
 * parameters and importing the corresponding configuration (if any) as the
 * default configuration.
 */
public class MigrationResult {
  /**
   * The {@link JsonObject} representing the initialization parameters migrated
   * from the INI file.
   */
  private JsonObject initJson;

  /**
   * The configuration ID of the imported configuration referenced in the INI
   * file, or <tt>null</tt> if the INI file did not have a "G2CONFIGFILE"
   * setting.
   */
  private Long importedConfigId;

  /**
   * The configuration ID of the default configuration in the repository, or
   * <tt>null</tt> if the repository has no default configuration.
   */
  private Long defaultConfigId;

  /**
   * Constructs with the {@link JsonObject} initialization parameters, the
   * {@link Long} configuration ID for the imported configuration and flag
   * indicating if the imported configuration is set as the default
   * configuration.
   *
   * @param initJson The {@link JsonObject} representing the initialization
   *                 parameters migrated from the INI file.
   * @param importedConfigId The configuration ID of the imported configuration
   *                         referenced in the INI file, or <tt>null</tt> if the
   *                         INI file did not have a "G2CONFIGFILE" setting.
   * @param defaultConfigId The configuration ID of the default configuration
   *                        in the repository, or <tt>null</tt> if the
   *                        repository has no default configuration.
   */
  public MigrationResult(JsonObject initJson,
                         Long       importedConfigId,
                         Long       defaultConfigId)
  {
    this.initJson         = initJson;
    this.importedConfigId = importedConfigId;
    this.defaultConfigId  = defaultConfigId;
  }

  /**
   * Returns the {@link JsonObject} representing the initialization parameters
   * migrated from the INI file.  If the INI file had a "G2CONFIGFILE" setting
   * then it is stripped from the result.
   *
   * @return The {@link JsonObject} representing the initialization parameters
   *         migrated from the INI file.
   */
  public JsonObject getInitJson() {
    return this.initJson;
  }

  /**
   * Returns the configuration ID of the imported configuration referenced in
   * the INI file, or <tt>null</tt> if the INI file did not have a
   * "G2CONFIGFILE" setting.
   *
   * @return The configuration ID of the imported configuration referenced in
   *         the INI file, or <tt>null</tt> if the INI file did not have a
   *         "G2CONFIGFILE" setting.
   */
  public Long getImportedConfigId() {
    return this.importedConfigId;
  }

  /**
   * Returns the configuration ID of the default configuration in the repository
   * or <tt>null</tt> if the repository has no default configuration.
   *
   * @return The configuration ID of the default configuration in the repository
   *         or <tt>null</tt> if the repository has no default configuration.
   */
  public Long getDefaultConfigId() {
    return this.defaultConfigId;
  }

  /**
   * Converts this instance to a diagnostic {@link String}.
   *
   * @return A {@link String} representation of this instance.
   */
  public String toString() {
    return multilineFormat(
        "Imported Configuration : "
            + (this.importedConfigId == null ? "NONE" : this.importedConfigId),
        "Default Configuration  : "
            + (this.defaultConfigId == null ? "NONE" : this.defaultConfigId),
        "Initialization JSON    : ",
        "-----------------------------------------------------------",
        JsonUtilities.toJsonText(this.initJson));
  }
}

