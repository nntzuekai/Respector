package com.senzing.configmgr;

import com.senzing.cmdline.*;
import com.senzing.nativeapi.NativeApiFactory;
import com.senzing.g2.engine.*;
import com.senzing.nativeapi.InstallLocations;
import com.senzing.io.IOUtilities;
import com.senzing.util.JsonUtilities;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.util.*;

import static com.senzing.io.IOUtilities.readTextFileAsString;
import static com.senzing.util.LoggingUtilities.*;
import static com.senzing.util.OperatingSystemFamily.RUNTIME_OS_FAMILY;
import static com.senzing.configmgr.ConfigManagerOption.*;
import static com.senzing.cmdline.CommandLineUtilities.*;

/**
 * Utlity class for managing the Senzing configurations.
 */
public class ConfigurationManager {
  private static final InstallLocations INSTALL_LOCATIONS
      = InstallLocations.findLocations();

  private static final File INSTALL_DIR = (INSTALL_LOCATIONS == null)
      ? null : INSTALL_LOCATIONS.getInstallDirectory();

  private static final G2ConfigMgr CONFIG_MGR_API;

  private static String initializationKey = null;

  private static final int CONFIG_NOT_FOUND_ERROR_CODE = 7221;

  static {
    G2ConfigMgr configMgrApi  = null;
    try {
      configMgrApi = NativeApiFactory.createConfigMgrApi();

    } catch (Exception e) {
      File libPath = new File(INSTALL_DIR, "lib");
      e.printStackTrace();
      System.err.println();
      switch (RUNTIME_OS_FAMILY) {
        case WINDOWS:
          System.err.println("Failed to load native G2.dll library.");
          System.err.println(
              "Check PATH environment variable for " + libPath);
          break;
        case MAC_OS:
          System.err.println("Failed to load native libG2.so library");
          System.err.println(
              "Check DYLD_LIBRARY_PATH environment variable for: ");
          System.err.println("     - " + libPath);
          System.err.println("     - " + (new File(libPath, "macos")));
          break;
        case UNIX:
          System.err.println("Failed to load native libG2.so library");
          System.err.println(
              "Check LD_LIBRARY_PATH environment variable for: ");
          System.err.println("     - " + libPath);
          System.err.println("     - " + (new File(libPath, "debian")));
          break;
        default:
          // do nothing
      }
      throw new ExceptionInInitializerError(e);

    } finally {
      CONFIG_MGR_API = configMgrApi;
    }
  }

  private static final String JAR_FILE_NAME;

  private static final String JAR_BASE_URL;

  static {
    String jarBaseUrl   = null;
    String jarFileName  = null;

    try {
      Class<ConfigurationManager> cls = ConfigurationManager.class;

      String url = cls.getResource(
          cls.getSimpleName() + ".class").toString();

      if (url.indexOf(".jar") >= 0) {
        int index = url.lastIndexOf(
            cls.getName().replace(".", "/") + ".class");
        jarBaseUrl = url.substring(0, index);

        index = jarBaseUrl.lastIndexOf("!");
        if (index >= 0) {
          url = url.substring(0, index);
          index = url.lastIndexOf("/");

          if (index >= 0) {
            jarFileName = url.substring(index + 1);
          }

          // url = url.substring(0, index);
          // index = url.indexOf("/");
          // PATH_TO_JAR = url.substring(index);
        }
      }

    } finally {
      JAR_BASE_URL  = jarBaseUrl;
      JAR_FILE_NAME = jarFileName;
    }
  }

  /**
   * Parses the command line arguments and returns a {@link Map} of those
   * arguments.  This will throw an exception if invalid command line arguments
   * are provided.
   *
   * @param args The arguments to parse.
   * @param deprecationWarnings The {@link List} to populate with any
   *                            deprecation warnings that might be generated,
   *                            or <code>null</code> if the caller is not
   *                            interested.
   * @return The {@link Map} of options to their values.
   * @throws CommandLineException If command line arguments are invalid.
   */
  private static Map<CommandLineOption, Object> parseCommandLine(
      String[]                      args,
      List<DeprecatedOptionWarning> deprecationWarnings)
      throws CommandLineException
  {
    Map<CommandLineOption, CommandLineValue>
        optionValues = CommandLineUtilities.parseCommandLine(
            ConfigManagerOption.class,
            args,
            PARAMETER_PROCESSOR,
            deprecationWarnings);

    // create a result map
    Map<CommandLineOption, Object> result = new LinkedHashMap<>();

    // iterate over the option values and handle them
    CommandLineUtilities.processCommandLine(optionValues, result);

    // return the result
    return result;
  }

  /**
   * Exits and prints the message associated with the specified exception.
   */
  private static void exitOnError(Throwable t) {
    System.err.println(t.getMessage());
    System.exit(1);
  }

  /**
   * @return
   */
  public static String getUsageString(boolean full) {
    // check if called from the RepositoryManager.main() directly
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    pw.println();
    Class<ConfigurationManager> cls = ConfigurationManager.class;
    if (checkClassIsMain(cls)) {
      pw.println("USAGE: java -cp " + JAR_FILE_NAME + " "
                     + cls.getName() + " <options>");
    } else {
      pw.println("USAGE: java -jar " + JAR_FILE_NAME + " --configmgr <options>");
    }
    pw.println();
    pw.println();
    if (!full) {
      pw.flush();
      return sw.toString();
    }
    pw.print(multilineFormat(
        "<options> includes: ",
        "   --help",
        "        Should be the first and only option if provided.",
        "        Displays a complete usage message describing all options.",
        "",
        "   --list-configs",
        "        Also --listConfigs.  List the available configurations and their IDs.",
        "        This requires one of the following options be specified:",
        "",
        "   --get-default-config",
        "        Also --getDefaultConfig.  Gets the default configuration ID from the",
        "        repository and prints it",
        "",
        "   --set-default-config",
        "        Also --setDefaultConfig.  Sets the default configuration ID in the",
        "        repository to the specified ID.",
        "",
        "   --export-config [output-file-path]",
        "        Also --exportConfig.  Exports the configuration specified by the",
        "        --config-id option to the specified file.  If the --config-id option is",
        "        is not specified then the current default configuration is exported.",
        "        If no default configuration then an error message will be displayed.",
        "        This accepts an optional parameter representing the file to export the",
        "        config to, if not provided the configuration is written to stdout.",
        "",
        "   --import-config <config-file-path> [description]",
        "        Also --importConfig.  Imports the configuration contained in the",
        "        specified JSON configuration file and outputs the configuration ID for",
        "        the imported configuration.  The optional second parameter specifies a",
        "        description for the imported configuration.",
        "",
        "   --migrate-ini <ini-file-path> [init-json-file]",
        "        Also --migrateIni.  Migrates the specified INI file to JSON",
        "        initialization parameters and imports any referenced configuration file",
        "        and sets it as the default configuration.  If a different configuration",
        "        is already configured as the default then it is left in place and",
        "        warning is displayed.  The first parameter is the path to the INI file,",
        "        a second option parameter specifies the file path to the write the JSON",
        "        initialization parameters to, if not provided then they are written to",
        "        stdout.",
        "",
        "   --init-file <json-init-file>",
        "        Also -initFile.  The path to the file containing the initialization JSON",
        "        text to use initializing Senzing and connecting to the Senzing",
        "        repository.  This can be used with the following options:",
        formatUsageOptionsList(
            "          ".length(),
            LIST_CONFIGS, GET_DEFAULT_CONFIG_ID, SET_DEFAULT_CONFIG_ID,
            EXPORT_CONFIG, IMPORT_CONFIG),
        "   --init-env-var <environment-variable-name>",
        "        Also -initEnvVar.  The environment variable from which to extract the",
        "        JSON initialization text to use for initializing Senzing and connecting",
        "        to the Senzing repository.  This can be used with the following options:",
        formatUsageOptionsList(
            "           ".length(),
            LIST_CONFIGS, GET_DEFAULT_CONFIG_ID, SET_DEFAULT_CONFIG_ID,
            EXPORT_CONFIG, IMPORT_CONFIG),
        "   --init-json <json-init-text>",
        "        Also -initJson.  The initialization JSON text to use for initializing",
        "        Senzing and connecting to the Senzing repository.  This can be used",
        "        with the following options:",
        formatUsageOptionsList(
            "          ".length(),
            LIST_CONFIGS, GET_DEFAULT_CONFIG_ID, SET_DEFAULT_CONFIG_ID,
            EXPORT_CONFIG, IMPORT_CONFIG),
        "        *** SECURITY WARNING: If the JSON text contains a password and it is",
        "        provided as a command line option then it may be visible to other users",
        "        ~via process monitoring.",
        "",
        "   --verbose [true|false]",
        "        Also -verbose.  If specified then initialize in verbose mode.  The",
        "        true/false parameter is optional, if not specified then true is assumed.",
        "        If specified as false then it is the same as omitting the option.",
        "",
        "   --config-id <config-id>",
        "        Also -configId.  Use with the --export-config and --set-default-config",
        "        options to specify the ID of the configuration to use."));
    pw.flush();
    sw.flush();

    return sw.toString();
  }


  /**
   * Initializing the Senzing Configuration Manager API and returns
   * <tt>true</tt> if initialized by this call and <tt>false</tt> if already
   * initialized with the same initialization JSON and verbose setting.  If
   * initialized with different settings, the {@link #destroyApi()}
   * method is called first and then initialization proceeds with the new
   * settings.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @return <tt>true</tt> if initialized by this call and <tt>false</tt> if
   *         already initialized with the same settings.
   */
  public static synchronized boolean initApi(JsonObject  initJson,
                                             boolean     verbose)
  {
    return initApi(initJson, verbose, false);
  }

  /**
   * Initializing the Senzing Configuration Manager API and returns
   * <tt>true</tt> if initialized by this call and <tt>false</tt> if already
   * initialized with the same initialization JSON and verbose setting.  If
   * initialized with different settings, the {@link #destroyApi(boolean)}
   * method is called first and then initialization proceeds with the new
   * settings.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param silent <tt>false</tt> if output should be written to stdout, and
   *               <tt>true</tt> if not.
   * @return <tt>true</tt> if initialized by this call and <tt>false</tt> if
   *         already initialized with the same settings.
   */
  public static synchronized boolean initApi(JsonObject  initJson,
                                             boolean     verbose,
                                             boolean     silent)
  {
    String jsonText = JsonUtilities.toJsonText(initJson);
    String initKey  = "" + verbose + ":" + jsonText;
    if (initializationKey != null) {
      if (initializationKey.equals(initKey)) return false;
      destroyApi();
    }
    String moduleName = ConfigurationManager.class.getName();
    int returnCode = CONFIG_MGR_API.init(moduleName, jsonText, verbose);
    if (returnCode != 0) {
      String errorMsg = formatError(
          "G2ConfigMgr.init()", CONFIG_MGR_API);
      if (!silent) {
        System.err.println("Failed to initialize G2ConfigMgr");
        System.err.println(errorMsg);
      }
      throw new RuntimeException(errorMsg);
    }
    initializationKey = initKey;
    return true;
  }

  /**
   * Calls the destroy function on the API and returns <tt>true</tt> if
   * destroyed by this call or <tt>false</tt> if already destroyed.
   */
  public static synchronized boolean destroyApi() {
    return destroyApi(false);
  }

  /**
   * Calls the destroy function on the API and returns <tt>true</tt> if
   * destroyed by this call or <tt>false</tt> if already destroyed.
   *
   * @param silent <tt>false</tt> if output should be written to stdout, and
   *               <tt>true</tt> if not.
   */
  public static synchronized boolean destroyApi(boolean silent) {
    if (initializationKey == null) return false;
    int returnCode = CONFIG_MGR_API.destroy();
    if (returnCode != 0) {
      String errorMsg = formatError(
          "G2ConfigMgr.destroy()", CONFIG_MGR_API);
      if (!silent) {
        System.err.println("Failed to destroy G2ConfigMgr");
        System.err.println(errorMsg);
      }
      throw new RuntimeException(errorMsg);
    }
    return true;
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    Map<CommandLineOption, Object>  options = null;
    List<DeprecatedOptionWarning>   warnings = new LinkedList<>();
    try {
      options = parseCommandLine(args, warnings);

      for (DeprecatedOptionWarning warning: warnings) {
        System.out.println(warning);
        System.out.println();
      }

    } catch (CommandLineException e) {
      System.out.println(e.getMessage());

      System.out.println(ConfigurationManager.getUsageString(true));
      System.exit(1);

    } catch (Exception e) {
      if (!isLastLoggedException(e))
      {
        e.printStackTrace();
      }
      System.exit(1);
    }

    if (options.containsKey(HELP)) {
      System.out.println(ConfigurationManager.getUsageString(true));
      System.exit(0);
    }

    try {
      boolean verbose = false;
      // check if verbose
      if (options.containsKey(VERBOSE)) {
        verbose = (Boolean) options.get(VERBOSE);
      }

      if (options.containsKey(MIGRATE_INI_FILE)) {
        File[] files = (File[]) options.get(MIGRATE_INI_FILE);
        File iniFile = files[0];
        File outputFile = (files.length > 1 ? files[1] : null);
        migrateFromIniFile(iniFile, verbose, outputFile);
        return;
      }

      JsonObject  initJson  = null;
      // determine the init JSON
      initJson = (JsonObject) options.get(INIT_FILE);
      if (initJson == null) {
        initJson = (JsonObject) options.get(INIT_ENV_VAR);
      }
      if (initJson == null) {
        initJson = (JsonObject) options.get(INIT_JSON);
      }

      if (options.containsKey(LIST_CONFIGS)) {
        listConfigs(initJson, verbose);

      } else if (options.containsKey(GET_DEFAULT_CONFIG_ID)) {
        getDefaultConfigId(initJson, verbose);

      } else if (options.containsKey(SET_DEFAULT_CONFIG_ID)) {
        Long configId = (Long) options.get(CONFIG_ID);
        setDefaultConfigId(initJson, verbose, configId);

      } else if (options.containsKey(EXPORT_CONFIG)) {
        Long configId   = (Long) options.get(CONFIG_ID);
        File outputFile = (File) options.get(EXPORT_CONFIG);
        exportConfig(initJson, verbose, configId, outputFile);

      } else if (options.containsKey(IMPORT_CONFIG)) {
        Object[] params = (Object[]) options.get(IMPORT_CONFIG);
        File configFile = (File) params[0];
        String description = null;
        if (params.length > 1) description = (String) params[1];
        importConfig(initJson, verbose, configFile, description);
      }

    } catch (Exception e) {
      if (!isLastLoggedException(e)) e.printStackTrace();
    }
  }

  /**
   * Lists the configs for the repository described by the specified
   * initialization {@link JsonObject} and using the specified verbose flag
   * for initialization.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @return The {@link JsonArray} describing the configurations.
   */
  public static JsonArray listConfigs(JsonObject initJson, boolean verbose) {
    return listConfigs(initJson, verbose, false);
  }

  /**
   * Lists the configs for the repository described by the specified
   * initialization {@link JsonObject} and using the specified verbose flag
   * for initialization.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param silent <tt>false</tt> if output should be written to stdout, and
   *               <tt>true</tt> if not.
   * @return The {@link JsonArray} describing the configurations.
   */
  public static synchronized JsonArray listConfigs(JsonObject  initJson,
                                                   boolean     verbose,
                                                   boolean     silent)
  {
    boolean initialized = initApi(initJson, verbose);
    try {
      StringBuffer sb = new StringBuffer();
      int returnCode = CONFIG_MGR_API.getConfigList(sb);
      if (returnCode != 0) {
        String errorMsg = formatError(
            "G2ConfigMgr.getConfigList()", CONFIG_MGR_API);
        if (!silent) {
          System.err.println("Failed to get the config list via getConfigList()");
          System.err.println(errorMsg);
        }
        throw new RuntimeException(errorMsg);
      }

      String jsonText = sb.toString();
      JsonObject  jsonObj = JsonUtilities.parseJsonObject(jsonText);
      JsonArray   jsonArr = JsonUtilities.getJsonArray(jsonObj,"CONFIGS");
      if (jsonArr == null) {
        throw new IllegalStateException(
            "Could not find CONFIGS element in getConfigList() result");
      }
      if (!silent) {
        // output a header for the table of configurations
        StringBuilder line = new StringBuilder();
        line.append("TIMESTAMP");
        while (line.length() < 25) line.append(" ");
        line.append("CONFIGURATION ID");
        while (line.length() < 48) line.append(" ");
        line.append("COMMENTS/DESCRIPTION");
        System.out.println(line.toString());
        line.delete(0, line.length());
        while (line.length() < 24) line.append("-");
        line.append(" ");
        while (line.length() < 47) line.append("-");
        line.append(" ");
        while (line.length() < 80) line.append("-");
        System.out.println(line.toString());

        // iterate over the configurations and output each
        for (JsonObject elem : jsonArr.getValuesAs(JsonObject.class)) {
          Long    configId  = JsonUtilities.getLong(elem, "CONFIG_ID");
          String  timeStamp = JsonUtilities.getString(elem, "SYS_CREATE_DT");
          String  comments  = JsonUtilities.getString(elem, "CONFIG_COMMENTS");
          if (line.length() > 0) line.delete(0, line.length());
          line.append(timeStamp);
          while (line.length() < 25) line.append(" ");
          line.append("" + configId);
          while (line.length() < 48) line.append(" ");
          line.append(comments);
          System.out.println(line.toString());
        }
      }
      return jsonArr;

    } finally {
      if (initialized) destroyApi();
    }
  }

  /**
   * Gets the default configuration ID for the repository described by the
   * specified initialization {@link JsonObject} and using the specified verbose
   * flag for initialization.  This method returns <tt>null</tt> if the
   * repository has no default configuration.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @return The default configuration ID or <tt>null</tt> if the repository
   *         has no default configuration.
   */
  public static Long getDefaultConfigId(JsonObject initJson, boolean verbose) {
    return getDefaultConfigId(initJson, verbose, false);
  }

  /**
   * Gets the default configuration ID for the repository described by the
   * specified initialization {@link JsonObject} and using the specified verbose
   * flag for initialization.  This method returns <tt>null</tt> if the
   * repository has no default configuration.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param silent <tt>false</tt> if output should be written to stdout, and
   *               <tt>true</tt> if not.
   * @return The default configuration ID or <tt>null</tt> if the repository
   *         has no default configuration.
   */
  public static synchronized Long getDefaultConfigId(JsonObject  initJson,
                                                     boolean     verbose,
                                                     boolean     silent)
  {
    boolean initialized = initApi(initJson, verbose);
    try {
      Result<Long> result = new Result<>();
      int returnCode = CONFIG_MGR_API.getDefaultConfigID(result);
      if (returnCode != 0) {
        String errorMsg = formatError(
            "G2ConfigMgr.getDefaultConfigID()", CONFIG_MGR_API);
        if (!silent) {
          System.err.println("Failed to get the default configuration ID");
          System.err.println(errorMsg);
        }
        throw new RuntimeException(errorMsg);
      }

      Long configId = result.getValue();
      if (!silent) {
        System.out.println(
            "DEFAULT CONFIGURATION ID: "
            + ((configId == null) ? "** NONE **" : configId.toString()));
      }

      // return null if not found
      return (configId == null || configId == 0) ? null : configId;

    } finally {
      if (initialized) destroyApi();
    }

  }

  /**
   * Sets the default configuration ID for the repository described by the
   * specified initialization {@link JsonObject} and using the specified verbose
   * flag for initialization.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param configId The configuration ID to set as a default for the
   *                 repository.
   * @return <tt>true</tt> if the specified configuration ID was found and
   *         set as the default configuration, or <tt>false</tt> if not found.
   */
  public static boolean setDefaultConfigId(JsonObject initJson,
                                           boolean    verbose,
                                           long       configId)
  {
    return setDefaultConfigId(initJson, verbose, configId, false);
  }

  /**
   * Sets the default configuration ID for the repository described by the
   * specified initialization {@link JsonObject} and using the specified verbose
   * flag for initialization.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param configId The configuration ID to set as a default for the
   *                 repository.
   * @param silent <tt>false</tt> if output should be written to stdout, and
   *               <tt>true</tt> if not.
   * @return <tt>true</tt> if the specified configuration ID was found and
   *         set as the default configuration, or <tt>false</tt> if not found.
   */
  public static synchronized boolean setDefaultConfigId(JsonObject  initJson,
                                                        boolean     verbose,
                                                        long        configId,
                                                        boolean     silent)
  {
    boolean initialized = initApi(initJson, verbose);
    try {
      int returnCode = CONFIG_MGR_API.setDefaultConfigID(configId);
      if (returnCode == CONFIG_NOT_FOUND_ERROR_CODE) return false;
      if (returnCode != 0) {
        String errorMsg = formatError(
            "G2ConfigMgr.setDefaultConfigID()", CONFIG_MGR_API);
        if (!silent) {
          System.err.println("Failed to set the default configuration ID: "
                             + configId);
          System.err.println(errorMsg);
        }
        throw new RuntimeException(errorMsg);
      }

      if (!silent) {
        System.out.println("Default configuration set to " + configId);
      }
      return true;

    } finally {
      if (initialized) destroyApi();
    }
  }

  /**
   * Exports the configuration for the specified configuration ID or the default
   * configuration if the specified configuration ID is <tt>null</tt>.  If the
   * specified configuration ID is <tt>null</tt> and the repository has no
   * default configuration then <tt>null</tt> is returned.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param configId The configuration ID of the configuration to export, or
   *                 <tt>null</tt> if the default configuration should be
   *                 exported.
   * @param outputFile The file to export the configuration to.
   *
   * @return The {@link JsonObject} describing the configuration that was
   *         exported, or <tt>null</tt> if the configuration ID was
   *         <tt>null</tt> and the repository has no default configuration.
   */
  public static JsonObject exportConfig(JsonObject  initJson,
                                        boolean     verbose,
                                        Long        configId,
                                        File        outputFile)
  {
    return exportConfig(initJson, verbose, configId, outputFile, false);
  }

  /**
   * Exports the configuration for the specified configuration ID or the default
   * configuration if the specified configuration ID is <tt>null</tt>.  If the
   * specified configuration ID is <tt>null</tt> and the repository has no
   * default configuration then <tt>null</tt> is returned.  The configuration
   * is returned as a {@link JsonObject} and will be written to a file as well
   * as UTF-8 encoded JSON text if the specified output file is not
   * <tt>null</tt>.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param configId The configuration ID of the configuration to export, or
   *                 <tt>null</tt> if the default configuration should be
   *                 exported.
   * @param outputFile The file to export the configuration to, or <tt>null</tt>
   *                   if the configuration should just be returned and not
   *                   written to a file.
   * @param silent <tt>false</tt> if output should be written to stdout, and
   *               <tt>true</tt> if not.
   *
   * @return The {@link JsonObject} describing the configuration that was
   *         exported, or <tt>null</tt> if the configuration ID was
   *         <tt>null</tt> and the repository has no default configuration.
   */
  public static JsonObject exportConfig(JsonObject  initJson,
                                        boolean     verbose,
                                        Long        configId,
                                        File        outputFile,
                                        boolean     silent)
  {
    boolean initialized = initApi(initJson, verbose);
    try {
      int returnCode;
      // if we don't have a config ID then get the default config ID
      if (configId == null) {
        Result<Long> result = new Result<>();
         returnCode = CONFIG_MGR_API.getDefaultConfigID(result);
         if (returnCode != 0) {
           String errorMsg = formatError(
               "G2ConfigMgr.getDefaultConfigID()", CONFIG_MGR_API);
           if (!silent) {
             System.err.println("Failed to get the default configuration ID");
             System.err.println(errorMsg);
           }
         }
         configId = result.getValue();
      }
      // if we still don't have a config ID then return null
      if (configId == null || configId == 0) {
        if (!silent) {
          System.err.println("Unable to export.  No default configuration "
                             + "found in repository.");
        }
        return null;
      }

      StringBuffer sb = new StringBuffer();
      returnCode = CONFIG_MGR_API.getConfig(configId, sb);
      if (returnCode == CONFIG_NOT_FOUND_ERROR_CODE) {
        if (!silent) {
          System.err.println("Unable to export.  Configuration not found for "
                             + "configuration ID: " + configId);
        }
      }
      if (returnCode != 0) {
        String errorMsg = formatError(
            "G2ConfigMgr.getConfig()", CONFIG_MGR_API);
        if (!silent) {
          System.err.println("Failed to export configuration for configuration "
                             + "ID: " + configId);
          System.err.println(errorMsg);
        }
        throw new RuntimeException(errorMsg);
      }

      // get the JSON text and parse it to be sure it parses as JSON
      String      jsonText  = sb.toString();
      JsonObject  configObj = JsonUtilities.parseJsonObject(jsonText);
      jsonText = JsonUtilities.toJsonText(configObj, true);

      // write the text to the specified file
      if (outputFile != null) {
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8")) {
          osw.write(jsonText);
          osw.flush();

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        if (!silent) {
          System.out.println("Configuration exported to file: " + outputFile);
        }
      } else {
        if (!silent) {
          System.out.println(
              multilineFormat(
                  "Exported configuration:",
                  "--------------------------------------------",
                  "", jsonText, ""));
        }
      }

      // return the configuration object
      return configObj;

    } finally {
      if (initialized) destroyApi();
    }
  }

  /**
   * Imports the configuration contained in the specified file and returns the
   * configuration ID of the imported configuration.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param configFile The file containing the configuration to import.
   * @param description The comment/description to associate with the
   *                    configuration.
   * @return The configuration ID of the imported configuration.
   */
  public static long importConfig(JsonObject  initJson,
                                  boolean     verbose,
                                  File        configFile,
                                  String      description)
  {
    return importConfig(initJson, verbose, configFile, description, false);
  }

  /**
   * Imports the configuration contained in the specified file and returns the
   * configuration ID of the imported configuration.
   *
   * @param initJson The {@link JsonObject} describing the initialization
   *                 parameters for the Senzing repository.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param configFile The file containing the configuration to import.
   * @param description The comment/description to associate with the
   *                    configuration.
   * @param silent <tt>false</tt> if output should be written to stdout, and
   *               <tt>true</tt> if not.
   *
   * @return The configuration ID of the imported configuration.
   */
  public static long importConfig(JsonObject  initJson,
                                  boolean     verbose,
                                  File        configFile,
                                  String      description,
                                  boolean     silent)
  {
    boolean initialized = initApi(initJson, verbose);
    try {
      // read the JSON text
      String jsonText = IOUtilities.readTextFileAsString(configFile, "UTF-8");

      // make sure the file parses as JSON
      try {
        JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonText);
        jsonObject.getJsonObject("G2_CONFIG");

      } catch (Exception e) {
        if (!silent) {
          System.err.println(
              "File does not contain a valid configuration: " + configFile);
        }
        throw new IllegalArgumentException(
                "The file does not contain a valid configuration: "
                    + configFile);

      }

      if (description == null) {
        description = "Imported from " + configFile + " on " + (new Date());
      }
      // add the config
      Result<Long> result = new Result<>();
      int returnCode = CONFIG_MGR_API.addConfig(jsonText, description, result);
      if (returnCode != 0) {
        String errorMsg = formatError(
            "G2ConfigMgr.addConfig()", CONFIG_MGR_API);
        if (!silent) {
          System.err.println("Failed to import configuration from file: "
                             + configFile);
          System.err.println(errorMsg);
        }
        throw new RuntimeException(errorMsg);
      }

      // get the config ID
      long configId = result.getValue();

      if (!silent) {
        System.out.println("Added configuration with ID: " + configId);
      }

      // return the configuration ID
      return configId;

    } catch (IOException e) {
      throw new RuntimeException(e);

    } finally {
      if (initialized) destroyApi();
    }
  }

  /**
   * Migrates the specified INI file to the specified initialization JSON
   * parameters file using the specified verbose flag when initializing Senzing.
   * This method returns the {@link MigrationResult} describing the result of
   * the migration.
   *
   * @param iniFile The legacy INI file.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param initJsonOutFile The output file for the initialization JSON
   *                        parameters.
   * @return The {@link MigrationResult} describing the result of the migration.
   */
  public static MigrationResult migrateFromIniFile(File     iniFile,
                                                   boolean  verbose,
                                                   File     initJsonOutFile)
  {
    return migrateFromIniFile(iniFile, verbose, initJsonOutFile, false);
  }

  /**
   * Migrates the specified INI file to the specified initialization JSON
   * parameters file using the specified verbose flag when initializing Senzing.
   * This method returns the {@link MigrationResult} describing the result of
   * the migration.
   *
   * @param iniFile The legacy INI file.
   * @param verbose <tt>true</tt> to initialize in verbose, otherwise
   *                <tt>false</tt>.
   * @param initJsonOutFile The output file for the initialization JSON
   *                        parameters.
   * @param silent <tt>false</tt> if output should be written to stdout, and
   *               <tt>true</tt> if not.
   * @return The {@link MigrationResult} describing the result of the migration.
   */
  public static MigrationResult migrateFromIniFile(File     iniFile,
                                                   boolean  verbose,
                                                   File     initJsonOutFile,
                                                   boolean  silent)
  {
    if (!silent) {
      System.out.println("Migrating INI file: " + iniFile);
    }
    Long        importedConfigId  = null;
    Long        defaultConfigId   = null;
    JsonObject  initJson          = JsonUtilities.iniToJson(iniFile);
    File        configFile        = null;
    JsonObject sqlSection = JsonUtilities.getJsonObject(initJson, "SQL");
    if (sqlSection != null) {
      String filePath = JsonUtilities.getString(sqlSection, "G2CONFIGFILE");
      if (filePath != null) configFile = new File(filePath);
    }

    // strip the G2CONFIGFILE from the initialization parameters
    if (configFile != null) {
      // create a builder from the original SQL section and remove G2CONFIGFILE
      JsonObjectBuilder sqlBuilder = Json.createObjectBuilder(sqlSection);
      sqlBuilder.remove("G2CONFIGFILE");

      // create an object builder and remove the SQL section
      JsonObjectBuilder job = Json.createObjectBuilder(initJson);
      job.remove("SQL");

      // restore the modified SQL section to the root builder
      job.add("SQL", sqlBuilder);

      // rebuild the init JSON without the G2CONFIGFILE
      initJson = job.build();
    }

    boolean initialized = initApi(initJson, verbose);
    try {
      int returnCode;
      if (configFile != null) {
        if (!silent) {
          System.out.println("Found G2CONFIGFILE setting in INI file: "
                             + configFile);
        }
        try {
          String description = "Imported when migrating INI (" + iniFile
              + ") from " + configFile;

          Result<Long> result = new Result<>();

          String jsonText = readTextFileAsString(configFile, "UTF-8");

          returnCode = CONFIG_MGR_API.addConfig(jsonText, description, result);
          if (returnCode != 0) {
            String errorMsg = formatError(
                "G2ConfigMgr.addConfig()", CONFIG_MGR_API);
            if (!silent) {
              System.err.println("Failed to add configuration file from INI: "
                                 + configFile);
              System.err.println(errorMsg);
            }
            throw new RuntimeException(errorMsg);
          }

          importedConfigId = result.getValue();

          if (!silent) {
            System.out.println("Imported configuration with ID: "
                               + importedConfigId);
          }

        } catch (IOException e) {
          if (!silent) {
            System.err.println("Failed to read configuration file from INI: "
                                   + configFile);
          }
          throw new RuntimeException(e);
        }
      } else if (!silent) {
        System.out.println("No G2CONFIGFILE setting found in INI file.");
      }

      // if we imported a config then we would like to make it the default
      if (importedConfigId != null && importedConfigId != 0) {

        // first check if existing default config -- do not override it
        Result<Long> result = new Result<>();
        returnCode = CONFIG_MGR_API.getDefaultConfigID(result);
        if (returnCode != 0) {
          String errorMsg = formatError(
              "G2ConfigMgr.getDefaultConfigID()", CONFIG_MGR_API);
          if (!silent) {
            System.err.println("Failed to check for existing default "
                               + "configuration while migrating INI file: "
                               + iniFile);
            System.err.println(errorMsg);
          }
          throw new RuntimeException(errorMsg);
        }

        defaultConfigId = result.getValue();
        // if no existing default config then set the default config ID
        if (defaultConfigId == null || defaultConfigId == 0) {
          returnCode = CONFIG_MGR_API.setDefaultConfigID(importedConfigId);
          if (returnCode != 0) {
            String errorMsg = formatError(
                "G2ConfigMgr.setDefaultConfigID()", CONFIG_MGR_API);
            if (!silent) {
              System.err.println("Failed to set the default configuration to "
                                 + importedConfigId
                                 + " while migrating INI file: " + iniFile);
              System.err.println(errorMsg);
            }
            throw new RuntimeException(errorMsg);
          }
          if (!silent) {
            System.out.println("Set default configuration to configuration ID: "
                               + importedConfigId);
          }
          defaultConfigId = importedConfigId;

        } else if (!silent) {
          if (defaultConfigId.longValue() == importedConfigId.longValue()) {
            System.out.println("Imported configuration already configured as "
                  + "default for the repository: " + defaultConfigId);

          } else {
            System.out.println("Repository already has default configuration: "
                                   + defaultConfigId);
            System.out.println(
                "Cowardly refusing to override default configuration with "
                  + importedConfigId);
            System.out.println(
                "You may manually set the default configuration with the "
                    + SET_DEFAULT_CONFIG_ID.getCommandLineFlag() + " option.");
          }
        }
      }

      // now its time to output the init JSON
      String initJsonText = JsonUtilities.toJsonText(initJson);
      if (initJsonOutFile != null) {
        try (FileOutputStream   fos = new FileOutputStream(initJsonOutFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8"))
        {
          osw.write(initJsonText);
          osw.flush();
          if (!silent) {
            System.out.println("Created file: " + initJsonOutFile);
          }
        } catch (IOException e) {
          if (!silent) {
            System.err.println("Failed to write initialization JSON to file: "
                               + initJsonOutFile);
            System.out.println(
                multilineFormat(
                    "--------------------------------------------",
                    "", initJsonText, ""));
          }
          throw new RuntimeException(e);
        }
      } else if (!silent) {
        System.out.println(
            multilineFormat(
                "--------------------------------------------",
                "", initJsonText, ""));
      }

      // return the configuration ID of the imported configuration
      return new MigrationResult(initJson, importedConfigId, defaultConfigId);

    } finally {
      if (initialized) destroyApi();
    }

  }
}
