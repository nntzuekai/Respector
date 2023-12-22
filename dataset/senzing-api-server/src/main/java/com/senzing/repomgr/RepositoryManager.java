package com.senzing.repomgr;

import com.senzing.api.model.SzDataSource;
import com.senzing.cmdline.*;
import com.senzing.nativeapi.NativeApiFactory;
import com.senzing.g2.engine.*;
import com.senzing.nativeapi.InstallLocations;
import com.senzing.io.RecordReader;
import com.senzing.util.JsonUtilities;

import javax.json.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.senzing.util.LoggingUtilities.*;
import static com.senzing.util.OperatingSystemFamily.*;
import static java.nio.file.StandardCopyOption.*;
import static com.senzing.io.IOUtilities.*;
import static com.senzing.cmdline.CommandLineUtilities.*;
import static com.senzing.repomgr.RepoManagerOption.*;
import static com.senzing.io.RecordReader.Format.*;

public class RepositoryManager {
  private static final File INSTALL_DIR;

  private static final File CONFIG_DIR;

  private static final File RESOURCE_DIR;

  private static final File SUPPORT_DIR;

  private static final File TEMPLATES_DIR;

  private static final G2Engine ENGINE_API;

  private static final G2Config CONFIG_API;

  private static final G2ConfigMgr CONFIG_MGR_API;

  private static final Set<String> EXCLUDED_TEMPLATE_FILES;

  private static final ThreadLocal<String> THREAD_MODULE_NAME
      = new ThreadLocal<>();

  private static String baseInitializedWith = null;
  private static String engineInitializedWith = null;

  static {
    try {
      Set<String> set = new LinkedHashSet<>();
      set.add("G2Module.ini".toLowerCase());
      set.add("G2Project.ini".toLowerCase());
      set.add("G2C.db".toLowerCase());
      set.add("g2config.json".toLowerCase());
      EXCLUDED_TEMPLATE_FILES = Collections.unmodifiableSet(set);

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);
    }
  }

  static {
    try {
      InstallLocations locations = InstallLocations.findLocations();

      if (locations != null) {
        INSTALL_DIR   = locations.getInstallDirectory();
        CONFIG_DIR    = locations.getConfigDirectory();
        SUPPORT_DIR   = locations.getSupportDirectory();
        RESOURCE_DIR  = locations.getResourceDirectory();
        TEMPLATES_DIR = locations.getTemplatesDirectory();
      } else {
        INSTALL_DIR   = null;
        CONFIG_DIR    = null;
        SUPPORT_DIR   = null;
        RESOURCE_DIR  = null;
        TEMPLATES_DIR = null;
      }

      G2Engine    engineApi     = null;
      G2Config    configApi     = null;
      G2ConfigMgr configMgrApi  = null;
      try {
        engineApi     = NativeApiFactory.createEngineApi();
        configApi     = NativeApiFactory.createConfigApi();
        configMgrApi  = NativeApiFactory.createConfigMgrApi();

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
        ENGINE_API      = engineApi;
        CONFIG_API      = configApi;
        CONFIG_MGR_API  = configMgrApi;
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);
    }
  }

  private static final String JAR_FILE_NAME;

  private static final String JAR_BASE_URL;

  // private static final String PATH_TO_JAR;

  static {
    String jarBaseUrl   = null;
    String jarFileName  = null;

    try {
      Class<RepositoryManager> cls = RepositoryManager.class;

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

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      JAR_BASE_URL  = jarBaseUrl;
      JAR_FILE_NAME = jarFileName;
    }
  }

  /**
   * Copies the config template files from the template directory to the
   * specified configuration directory (creating the directory if necessary).
   *
   * @param templateDir The template directory to get the templates from.
   * @param configDir The config directory to copy the files to.
   * @throws IOException If an I/O failure occurs.
   */
  public static void copyConfigFiles(File   templateDir,
                                     File   configDir)
    throws IOException
  {
    if (templateDir != null) {
      File[] templateFiles = templateDir.listFiles(
          f -> !f.getName().endsWith(".template") && !f.isDirectory()
              && (!EXCLUDED_TEMPLATE_FILES.contains(f.getName().toLowerCase())));

      if (templateFiles.length > 0)
      {
        if (!configDir.exists()) {
          configDir.mkdirs();
        }
        for (File templateFile : templateFiles) {
          File targetFile = new File(configDir, templateFile.getName());
          copyFile(templateFile, targetFile);
        }
      }
    }
  }

  /**
   * Describes a repository configuration.
   *
   */
  public static class Configuration {
    private long configId;
    private JsonObject configJson;
    public Configuration(long configId, JsonObject configJson) {
      this.configId   = configId;
      this.configJson = configJson;
    }

    /**
     * Returns the configuration ID.
     * @return The configuration ID.
     */
    public long getConfigId() {
      return this.configId;
    }

    /**
     * Returns the configuration JSON as a {@link JsonObject}.
     * @return The {@link JsonObject} describing the configuration.
     */
    public JsonObject getConfigJson() {
      return this.configJson;
    }
  }

  /**
   * Parses the command line arguments and returns a {@link Map} of those
   * arguments.
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
    Map<CommandLineOption, CommandLineValue> optionValues =
        CommandLineUtilities.parseCommandLine(
            RepoManagerOption.class,
            args,
            RepoManagerOption.PARAMETER_PROCESSOR,
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
   * Use this method in conjunction with {@link #clearThreadModuleName()} to
   * provide a specific module name for the repository manager to use when
   * initializing the G2 API's.
   *
   * @param moduleName The module name to initialize with, or <tt>null</tt>
   *                   to do the equivalent of clearing the name.
   *
   */
  public static void setThreadModuleName(String moduleName) {
    RepositoryManager.THREAD_MODULE_NAME.set(moduleName);
  }

  /**
   * Clears any previously set thread module name.  This method should be called
   * in a "finally" block.
   */
  public static void clearThreadModuleName() {
    RepositoryManager.setThreadModuleName(null);
  }

  /**
   * @return
   */
  public static String getUsageString(boolean full) {
    // check if called from the RepositoryManager.main() directly
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    pw.println();
    Class<RepositoryManager> cls = RepositoryManager.class;
    if (checkClassIsMain(cls)) {
      pw.println("USAGE: java -cp " + JAR_FILE_NAME + " "
                 + cls.getName() + " <options>");
    } else {
      pw.println("USAGE: java -jar " + JAR_FILE_NAME + " --repomgr <options>");
    }
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
        "   --create-repo <repository-directory-path>",
        "        Creates a new Senzing repository at the specified path.",
        "",
        "   --purge-repo",
        "        Purges the Senzing repository at the specified path.",
        "",
        "   --config-sources <data-source-1> [data-source-2 ... data-source-n]",
        "        Configures the specified data sources for the repository",
        "        specified by the -repo option.",
        "",
        "   --load-file <source-file>",
        "        Loads the records in the specified source CSV or JSON file.",
        "        Records are loaded to the repository specified by the -repo option.",
        "        Use the -dataSource option to specify or override a data source for",
        "        the records.",
        "",
        "   --add-record <json-record>",
        "        Loads the specified JSON record provided on the command line.",
        "        The record is loaded to the repository specified by the -repo option.",
        "        Use the -dataSource option to specify or override the data source for ",
        "        the record.",
        "",
        "   --repo <repository-directory-path>",
        "        Specifies the directory path to the repository to use when performing",
        "        other operations such as:",
        formatUsageOptionsList(
            "           ".length(),
            PURGE_REPO, CONFIG_SOURCES, LOAD_FILE, ADD_RECORD),
        "   --data-source <data-source>",
        "        Specifies a data source to use when loading records.  If the records",
        "        already have a DATA_SOURCE property then this will override that value.",
        "",
        "   --verbose",
        "        If provided then Senzing will be initialized in verbose mode"));
    pw.flush();
    sw.flush();

    return sw.toString();
  }

  /**
   * @param args
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    Map<CommandLineOption, Object> options = null;
    List<DeprecatedOptionWarning> warnings = new LinkedList<>();
    try {
      options = parseCommandLine(args, warnings);

      for (DeprecatedOptionWarning warning: warnings) {
        System.out.println(warning);
        System.out.println();
      }

    } catch (CommandLineException e) {
      System.out.println(e.getMessage());

      System.out.println(RepositoryManager.getUsageString(true));
      System.exit(1);

    } catch (Exception e) {
      if (!isLastLoggedException(e))
      {
        e.printStackTrace();
      }
      System.exit(1);
    }

    if (options.containsKey(RepoManagerOption.HELP)) {
      System.out.println(RepositoryManager.getUsageString(true));
      System.exit(0);
    }

    File repository = (File) options.get(REPOSITORY);
    String dataSource = (String) options.get(DATA_SOURCE);
    Boolean verbose = (Boolean) options.get(VERBOSE);
    if (verbose == null) verbose = Boolean.FALSE;

    try {
      // check if we are creating a repo
      if (options.containsKey(CREATE_REPO)) {
        File directory = (File) options.get(CREATE_REPO);
        createRepo(directory);
      } else if (options.containsKey(PURGE_REPO)) {
        try {
          purgeRepo(repository, verbose);
        } finally {
          destroyApis();
        }

      } else if (options.containsKey(RepoManagerOption.LOAD_FILE)) {
        File sourceFile = (File) options.get(RepoManagerOption.LOAD_FILE);
        try {
          loadFile(repository, verbose, sourceFile, dataSource);
        } finally {
          destroyApis();
        }

      } else if (options.containsKey(RepoManagerOption.ADD_RECORD)) {
        String jsonRecord = (String) options.get(RepoManagerOption.ADD_RECORD);
        try {
          addRecord(repository, verbose, jsonRecord, dataSource);
        } finally {
          destroyApis();
        }

      } else if (options.containsKey(RepoManagerOption.CONFIG_SOURCES)) {
        Set<String> dataSources = (Set<String>) options.get(RepoManagerOption.CONFIG_SOURCES);
        try {
          configSources(repository, verbose, dataSources);
        } finally {
          destroyApis();
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates a new Senzing SQLite repository from the default repository data.
   *
   * @param directory The directory at which to create the repository.
   *
   * @return The {@link Configuration} describing the initial configuration.
   */
  public static Configuration createRepo(File directory) {
    return createRepo(directory, false);
  }

  /**
   * Creates a new Senzing SQLite repository from the default repository data.
   *
   * @param directory The directory at which to create the repository.
   *
   * @return The {@link Configuration} describing the initial configuration.
   */
  public static Configuration createRepo(File directory, boolean silent) {
    JsonObject resultConfig = null;
    Long resultConfigId = null;
    try {
      directory = directory.getCanonicalFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (directory.exists()) {
      if (!directory.isDirectory()) {
        throw new IllegalArgumentException(
            "Repository directory exists and is not a directory: " + directory);
      }
      if (directory.listFiles().length > 0) {
        throw new IllegalArgumentException(
            "Repository directory exists and is not empty: "
                + directory + " / " + directory.listFiles()[0]);
      }
    }
    try {
      directory.mkdirs();
      File repoConfigDir = new File(directory, "etc");
      copyConfigFiles(TEMPLATES_DIR, repoConfigDir);

      // find the template DB file
      File templateDB = (TEMPLATES_DIR != null)
          ? new File(TEMPLATES_DIR, "G2C.db")
          : new File(SUPPORT_DIR, "G2C.db");
      if (!templateDB.exists()) {
        templateDB = new File(SUPPORT_DIR, "G2C.db");
      }

      if (templateDB.exists()) {
        // copy the file
        copyFile(templateDB, new File(directory, "G2C.db"));
        copyFile(templateDB, new File(directory, "G2_RES.db"));
        copyFile(templateDB, new File(directory, "G2_LIB_FEAT.db"));
      } else {
        // handle running in mock replay mode (no installation)
        touchFile(new File(directory, "G2C.db"));
        touchFile(new File(directory, "G2_RES.db"));
        touchFile(new File(directory, "G2_LIB_FEAT.db"));
      }

      // define the license path
      File licensePath = null;

      // check if there is a license file in the installation
      InstallLocations installLocations = InstallLocations.findLocations();
      if (installLocations != null) {
        File installDir = installLocations.getInstallDirectory();
        File etcDir = new File(installDir, "etc");
        licensePath = new File(etcDir, "g2.lic");
      }

      // if no existing license then set a license path in the repo directory
      if (licensePath == null || !licensePath.exists()) {
        licensePath = new File(directory, "g2.lic");
      }

      String fileSep = System.getProperty("file.separator");
      String sqlitePrefix = "sqlite3://na:na@" + directory.toString() + fileSep;

      File jsonInitFile = new File(directory, "g2-init.json");
      JsonObjectBuilder builder = Json.createObjectBuilder();
      JsonObjectBuilder subBuilder = Json.createObjectBuilder();
      if (SUPPORT_DIR != null) {
        subBuilder.add("SUPPORTPATH", SUPPORT_DIR.toString());
      }
      if (RESOURCE_DIR != null) {
        subBuilder.add("RESOURCEPATH", RESOURCE_DIR.toString());
      }
      if (repoConfigDir != null) {
        subBuilder.add("CONFIGPATH", repoConfigDir.toString());
      } else if (CONFIG_DIR != null) {
        subBuilder.add("CONFIGPATH", CONFIG_DIR.toString());
      }
      subBuilder.add("LICENSEFILE", licensePath.getCanonicalPath());
      builder.add("PIPELINE", subBuilder);

      subBuilder = Json.createObjectBuilder();
      subBuilder.add("BACKEND", "HYBRID");
      subBuilder.add("CONNECTION", sqlitePrefix + "G2C.db");
      builder.add("SQL", subBuilder);

      subBuilder = Json.createObjectBuilder();
      subBuilder.add("RES_FEAT", "C1");
      subBuilder.add("RES_FEAT_EKEY", "C1");
      subBuilder.add("RES_FEAT_LKEY", "C1");
      subBuilder.add("RES_FEAT_STAT", "C1");
      subBuilder.add("LIB_FEAT", "C2");
      subBuilder.add("LIB_FEAT_HKEY", "C2");
      builder.add("HYBRID", subBuilder);

      subBuilder = Json.createObjectBuilder();
      subBuilder.add("CLUSTER_SIZE", "1");
      subBuilder.add("DB_1", sqlitePrefix + "G2_RES.db");
      builder.add("C1", subBuilder);

      subBuilder = Json.createObjectBuilder();
      subBuilder.add("CLUSTER_SIZE", "1");
      subBuilder.add("DB_1", sqlitePrefix + "G2_LIB_FEAT.db");
      builder.add("C2", subBuilder);

      JsonObject  initJson      = builder.build();
      String      initJsonText  = JsonUtilities.toJsonText(initJson, true);

      try (FileOutputStream   fos = new FileOutputStream(jsonInitFile);
           OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8"))
      {
        osw.write(initJsonText);
        osw.flush();
      }

      // setup the initial configuration
      initBaseApis(directory, false);
      try {
        Result<Long> configIdResult = new Result<>();
        int returnCode = CONFIG_API.create(configIdResult);
        if (returnCode != 0) {
          System.err.println("RETURN CODE: " + returnCode);
          String msg = logError("G2Config.create()", CONFIG_API);
          throw new IllegalStateException(msg);
        }
        long configId = configIdResult.getValue();
        StringBuffer sb = new StringBuffer();
        returnCode = CONFIG_API.save(configId, sb);
        if (returnCode != 0) {
          String msg = logError("G2Config.save()", CONFIG_API);
          throw new IllegalStateException(msg);
        }
        CONFIG_API.close(configId);

        String configJsonText = sb.toString();

        resultConfig = JsonUtilities.parseJsonObject(configJsonText);

        Result<Long> result = new Result<>();
        returnCode = CONFIG_MGR_API.addConfig(configJsonText,
                                              "Initial Config",
                                              result);
        if (returnCode != 0) {
          String msg = logError("G2ConfigMgr.addConfig()",
                                CONFIG_MGR_API);
          throw new IllegalStateException(msg);
        }

        resultConfigId = result.getValue();
        returnCode = CONFIG_MGR_API.setDefaultConfigID(resultConfigId);
        if (returnCode != 0) {
          String msg = logError("G2ConfigMgr.setDefaultConfigID()",
                                CONFIG_MGR_API);
          throw new IllegalStateException(msg);
        }

      } finally {
        destroyBaseApis();
      }

      if (!silent) {
        System.out.println("Entity repository created at: " + directory);
      }

    } catch (IOException e) {
      e.printStackTrace();
      deleteRecursively(directory);
      throw new RuntimeException(e);
    }
    return new Configuration(resultConfigId, resultConfig);
  }

  private static void copyFile(File source, File target)
    throws IOException
  {
    Files.copy(source.toPath(), target.toPath(), COPY_ATTRIBUTES);
  }

  private static void deleteRecursively(File directory) {
    try {
      Files.walk(directory.toPath())
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);

    } catch (IOException e) {
      System.err.println("Failed to delete directory: " + directory);
    }

  }

  private static synchronized void initBaseApis(File repository, boolean verbose)
  {
    try {
      String moduleName = THREAD_MODULE_NAME.get();
      if (moduleName == null) moduleName = "G2 Repository Manager";
      String initializer = verbose + ":" + repository.getCanonicalPath();
      if (baseInitializedWith == null || !baseInitializedWith.equals(initializer))
      {
        if (baseInitializedWith != null) {
          destroyBaseApis();
        }
        File iniJsonFile = new File(repository, "g2-init.json");
        String initJsonText = readTextFileAsString(iniJsonFile, "UTF-8");
        int returnCode = CONFIG_API.init(moduleName, initJsonText, verbose);
        if (returnCode != 0) {
          logError("G2Config.init()", CONFIG_API);
          return;
        }
        returnCode = CONFIG_MGR_API.init(moduleName, initJsonText, verbose);
        if (returnCode != 0) {
          CONFIG_API.destroy();
          logError("G2ConfigMgr.init()", CONFIG_MGR_API);
          return;
        }
        baseInitializedWith = initializer;
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static synchronized void initApis(File repository, boolean verbose) {
    try {
      String moduleName = THREAD_MODULE_NAME.get();
      if (moduleName == null) moduleName = "G2 Repository Manager";

      initBaseApis(repository, verbose);

      String initializer = verbose + ":" + repository.getCanonicalPath();
      if (engineInitializedWith == null
          || !engineInitializedWith.equals(initializer))
      {
        if (engineInitializedWith != null) {
          destroyApis();
        }
        File iniJsonFile = new File(repository, "g2-init.json");
        String initJsonText = readTextFileAsString(iniJsonFile, "UTF-8");
        int returnCode = ENGINE_API.init(moduleName, initJsonText, verbose);
        if (returnCode != 0) {
          destroyBaseApis();
          logError("G2Engine.init()", ENGINE_API);
          return;
        }
        engineInitializedWith = initializer;
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static synchronized void destroyBaseApis() {
    if (baseInitializedWith != null) {
      CONFIG_API.destroy();
      CONFIG_MGR_API.destroy();
      baseInitializedWith = null;
    }
  }


  private static synchronized void destroyApis() {
    if (engineInitializedWith != null) {
      ENGINE_API.destroy();
      engineInitializedWith = null;
    }
    destroyBaseApis();
  }

  /**
   * Shuts down the repository manager after use to ensure the native
   * Senzing API destroy() functions are called.
   */
  public static void conclude() {
    destroyApis();
  }

  private static String logError(String operation, G2Fallible fallible) {
    String errorMsg = formatError(operation, fallible, true);
    System.err.println();
    System.err.println(errorMsg);
    System.err.println();
    return errorMsg;
  }

  private static Set<String> getDataSources() {
    Result<Long> configId = new Result<>();
    try {
      return getDataSources(configId);

    } finally {
      if (configId.getValue() != null) {
        CONFIG_API.close(configId.getValue());
      }
    }
  }

  private static Long loadActiveConfig() {
    StringBuffer sb = new StringBuffer();
    int returnCode = ENGINE_API.exportConfig(sb);
    if (returnCode != 0) {
      logError("G2Engine.exportConfig()", ENGINE_API);
      return null;
    }
    Result<Long> configHandleResult = new Result<>();
    returnCode = CONFIG_API.load(sb.toString(), configHandleResult);
    if (returnCode != 0) {
      logError("G2Config.load()", CONFIG_API);
      return null;
    }
    long handle = configHandleResult.getValue();
    return handle;
  }

  private static Set<String> getDataSources(Result<Long> configId) {
    Long handle = loadActiveConfig();
    if (handle == null) return null;
    configId.setValue(handle);
    return getDataSources(handle);
  }

  private static Set<String> getDataSources(long configId) {
    StringBuffer sb = new StringBuffer();
    int returnCode = CONFIG_API.listDataSources(configId, sb);
    if (returnCode != 0) {
      logError("G2Config.listDataSources()", CONFIG_API);
      return null;
    }

    Set<String> existingSet = new LinkedHashSet<>();

    // parse the raw data
    JsonObject jsonObject = JsonUtilities.parseJsonObject(sb.toString());
    JsonArray jsonArray = jsonObject.getJsonArray("DATA_SOURCES");
    for (JsonObject dataSource : jsonArray.getValuesAs(JsonObject.class)) {
      existingSet.add(JsonUtilities.getString(dataSource, "DSRC_CODE"));
    }

    return existingSet;
  }

  /**
   * Purges the repository that resides at the specified repository directory.
   *
   * @param repository The directory for the repository.
   */
  public static void purgeRepo(File repository) {
    purgeRepo(repository, false);
  }

  /**
   * Purges the repository that resides at the specified repository directory.
   *
   * @param repository The directory for the repository.
   *
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   */
  public static void purgeRepo(File repository, boolean verbose) {
    purgeRepo(repository, verbose, false);
  }

  /**
   * Purges the repository that resides at the specified repository directory.
   *
   * @param repository The directory for the repository.
   *
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   *
   * @param silent <tt>true</tt> if no feedback should be given to the user
   *               upon completion, otherwise <tt>false</tt>
   */
  public static void purgeRepo(File repository, boolean verbose, boolean silent)
  {
    initApis(repository, verbose);
    int result = ENGINE_API.purgeRepository();
    if (result != 0) {
      logError("G2Engine.purgeRepository()", ENGINE_API);
    } else if (!silent) {
      System.out.println();
      System.out.println("Repository purged: " + repository);
      System.out.println();
    }
  }

  /**
   * Loads a single CSV or JSON file to the repository -- optionally setting
   * the data source for all the records.  NOTE: if the records in the file do
   * not have a defined DATA_SOURCE then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param sourceFile The source file to load (JSON or CSV).
   * @param dataSource The data source to use for loading the records.
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean loadFile(File   repository,
                                 File   sourceFile,
                                 String dataSource)
  {
    return loadFile(repository,
                    false,
                    sourceFile,
                    dataSource,
                    null,
                    null,
                    false);
  }

  /**
   * Loads a single CSV or JSON file to the repository -- optionally setting
   * the data source for all the records.  NOTE: if the records in the file do
   * not have a defined DATA_SOURCE then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param sourceFile The source file to load (JSON or CSV).
   * @param dataSource The data source to use for loading the records.
   * @param silent <tt>true</tt> if no feedback should be given to the user
   *               upon completion, otherwise <tt>false</tt>
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean loadFile(File     repository,
                                 File     sourceFile,
                                 String   dataSource,
                                 boolean  silent)
  {
    return loadFile(repository,
                    false,
                    sourceFile,
                    dataSource,
                    null,
                    null,
                    silent);
  }

  /**
   * Loads a single CSV or JSON file to the repository -- optionally setting
   * the data source for all the records.  NOTE: if the records in the file do
   * not have a defined DATA_SOURCE then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param sourceFile The source file to load (JSON or CSV).
   * @param dataSource The data source to use for loading the records.
   * @param loadedCount The output parameter for the number successfully loaded.
   * @param failedCount The output parameter for the number that failed to load.
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean loadFile(File             repository,
                                 File             sourceFile,
                                 String           dataSource,
                                 Result<Integer>  loadedCount,
                                 Result<Integer>  failedCount)
  {
    return loadFile(repository,
                    false,
                    sourceFile,
                    dataSource,
                    loadedCount,
                    failedCount,
                    false);
  }

  /**
   * Loads a single CSV or JSON file to the repository -- optionally setting
   * the data source for all the records.  NOTE: if the records in the file do
   * not have a defined DATA_SOURCE then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param sourceFile The source file to load (JSON or CSV).
   * @param dataSource The data source to use for loading the records.
   * @param loadedCount The output parameter for the number successfully loaded.
   * @param failedCount The output parameter for the number that failed to load.
   * @param silent <tt>true</tt> if no feedback should be given to the user
   *               upon completion, otherwise <tt>false</tt>
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean loadFile(File             repository,
                                 File             sourceFile,
                                 String           dataSource,
                                 Result<Integer>  loadedCount,
                                 Result<Integer>  failedCount,
                                 boolean          silent)
  {
    return loadFile(repository,
                    false,
                    sourceFile,
                    dataSource,
                    loadedCount,
                    failedCount,
                    silent);
  }

  /**
   * Loads a single CSV or JSON file to the repository -- optionally setting
   * the data source for all the records.  NOTE: if the records in the file do
   * not have a defined DATA_SOURCE then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   * @param sourceFile The source file to load (JSON or CSV).
   * @param dataSource The data source to use for loading the records.
   *
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean loadFile(File     repository,
                                 boolean  verbose,
                                 File     sourceFile,
                                 String   dataSource)
  {
    return loadFile(repository,
                    verbose,
                    sourceFile,
                    dataSource,
                    null,
                    null,
                    false);
  }

  /**
   * Loads a single CSV or JSON file to the repository -- optionally setting
   * the data source for all the records.  NOTE: if the records in the file do
   * not have a defined DATA_SOURCE then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   * @param sourceFile The source file to load (JSON or CSV).
   * @param dataSource The data source to use for loading the records.
   * @param silent <tt>true</tt> if no feedback should be given to the user
   *               upon completion, otherwise <tt>false</tt>
   *
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean loadFile(File     repository,
                                 boolean  verbose,
                                 File     sourceFile,
                                 String   dataSource,
                                 boolean  silent)
  {
    return loadFile(repository,
                    verbose,
                    sourceFile,
                    dataSource,
                    null,
                    null,
                    silent);
  }

  /**
   * Loads a single CSV or JSON file to the repository -- optionally setting
   * the data source for all the records.  NOTE: if the records in the file do
   * not have a defined DATA_SOURCE then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   * @param sourceFile The source file to load (JSON or CSV).
   * @param dataSource The data source to use for loading the records.
   * @param loadedCount The output parameter for the number successfully loaded.
   * @param failedCount The output parameter for the number that failed to load.
   *
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean loadFile(File             repository,
                                 boolean          verbose,
                                 File             sourceFile,
                                 String           dataSource,
                                 Result<Integer>  loadedCount,
                                 Result<Integer>  failedCount)
  {
    return loadFile(repository,
                    verbose,
                    sourceFile,
                    dataSource,
                    loadedCount,
                    failedCount,
                    false);
  }

  /**
   * Loads a single CSV or JSON file to the repository -- optionally setting
   * the data source for all the records.  NOTE: if the records in the file do
   * not have a defined DATA_SOURCE then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   * @param sourceFile The source file to load (JSON or CSV).
   * @param dataSource The data source to use for loading the records.
   * @param loadedCount The output parameter for the number successfully loaded.
   * @param failedCount The output parameter for the number that failed to load.
   *
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean loadFile(File             repository,
                                 boolean          verbose,
                                 File             sourceFile,
                                 String           dataSource,
                                 Result<Integer>  loadedCount,
                                 Result<Integer>  failedCount,
                                 boolean          silent)
  {
    String normalizedFileName = sourceFile.toString().toUpperCase();
    if ((!normalizedFileName.endsWith(".JSON"))
        && (!normalizedFileName.endsWith(".CSV"))) {
      throw new IllegalArgumentException(
          "File must be a CSV or JSON file: " + sourceFile);
    }

    initApis(repository, verbose);

    final Integer ZERO = 0;
    if (loadedCount != null) loadedCount.setValue(ZERO);
    if (failedCount != null) failedCount.setValue(ZERO);
    if (dataSource != null) dataSource = dataSource.toUpperCase();

    Set<String> dataSources = getDataSources();
    // check if the data source is configured
    if (dataSource != null && !dataSources.contains(dataSource)) {
      if (!addDataSource(repository, dataSource, verbose)) return false;
      dataSources.add(dataSource);
    }

    RecordReader recordReader = null;
    // check the file type
    if (normalizedFileName.endsWith(".JSON")) {
      recordReader = provideJsonRecords(sourceFile, dataSource);
    } else if (normalizedFileName.endsWith(".CSV")) {
      recordReader = provideCsvRecords(sourceFile, dataSource);
    }
    if (recordReader == null) {
      return false;
    }

    String loadId = (new Date()).toString();
    int loaded = 0;
    int failed = 0;
    int loadedInterval = 100;
    int failedInterval = 100;
    PrintStream printStream = System.err;
    try {
      for (JsonObject record = recordReader.readRecord();
           (record != null);
           record = recordReader.readRecord())
      {
        String recordId = JsonUtilities.getString(record, "RECORD_ID");
        String recordSource = JsonUtilities.getString(record, "DATA_SOURCE");
        if (recordSource == null) {
          System.err.println();
          System.err.println(
              "If records in the file do not have a DATA_SOURCE then "
                  + RepoManagerOption.DATA_SOURCE.getCommandLineFlag()
                  + " is required.");
          return false;
        }

        if (!dataSources.contains(recordSource)) {
          if (!addDataSource(repository, recordSource, verbose)) return false;
          dataSources.add(recordSource);
        }

        StringBuffer sb = new StringBuffer();
        String jsonRecord = JsonUtilities.toJsonText(record);

        int returnCode
            = (recordId != null)
            ? ENGINE_API.addRecord(dataSource, recordId, jsonRecord, loadId)
            : ENGINE_API.addRecordWithReturnedRecordID(dataSource,
                                                       sb,
                                                       jsonRecord,
                                                       loadId);
        if (returnCode == 0) {
          loaded++;
          loadedInterval = doLoadFeedback(
              "Loaded so far", loaded, loadedInterval, loaded, failed, silent);

        } else {
          failed++;
          if (failed == 1 || ((failed % failedInterval) == 0)) {
            logError("G2Engine.addRecord()", ENGINE_API);
          }
          failedInterval = doLoadFeedback(
              "Loaded so far", failed, failedInterval, loaded, failed, silent);
        }
      }
      doLoadFeedback(
          "Loaded all records", loaded, 0, loaded, failed, silent);
      processRedos(silent);
      printStream = (silent) ? null : System.out;

      return true;

    } finally {
      if (loaded > 0 || failed > 0) {
        if (printStream != null) {
          printStream.println();
          printStream.println("Loaded records from file:");
          printStream.println("     Repository  : " + repository);
          printStream.println("     File        : " + sourceFile);
          if (dataSource != null) {
            printStream.println("     Data Source : " + dataSource);
          }
          printStream.println("     Load Count  : " + loaded);
          printStream.println("     Fail Count  : " + failed);
          printStream.println();
        }
      }
      // set the counts
      if (failedCount != null) failedCount.setValue(failed);
      if (loadedCount != null) loadedCount.setValue(loaded);
    }
  }

  private static int processRedos(boolean silent) {
    int loaded = 0;
    int failed = 0;
    try {
      // process redos
      int loadedInterval = 100;
      int failedInterval = 100;
      long originalCount = ENGINE_API.countRedoRecords();
      if (originalCount == 0) return 0;
      if (originalCount > 0) {
        System.out.println();
        System.out.println("Found redos to process: " + originalCount);
        System.out.println();
      }
      for (int count = 0; ENGINE_API.countRedoRecords() > 0; count++) {
        StringBuffer sb = new StringBuffer();
        int returnCode = ENGINE_API.processRedoRecord(sb);
        if (returnCode != 0) {
          logError("G2Engine.processRedoRecord()", ENGINE_API);
          failed++;
          failedInterval = doLoadFeedback(
              "Redo's so far", failed, failedInterval, loaded, failed, silent);
        } else {
          loaded++;
          loadedInterval = doLoadFeedback(
              "Redo's so far", loaded, loadedInterval, loaded, failed, silent);
        }
        if (count > (originalCount*5)) {
          System.err.println();
          System.err.println("Processing redo's not converging -- giving up.");
          System.err.println();
          return count;
        }
      }
      System.out.println();
      System.out.println("Processed all redos (succeeded / failed): "
                         + loaded + " / " + failed);
      System.out.println();

      return loaded;

    } catch (Exception ignore) {
      System.err.println();
      System.err.println("IGNORING EXCEPTION DURING REDOS:");
      ignore.printStackTrace();
      System.err.println();
      return loaded;
    }

  }

  private static int doLoadFeedback(String  prefix,
                                    int     count,
                                    int     interval,
                                    int     loaded,
                                    int     failed,
                                    boolean silent)
  {
    if (count > (interval * 10)) {
      interval *= 10;
    }
    if ((count > 0) && ((interval == 0) || (count % interval) == 0)) {
      if (!silent) {
        System.out.println(prefix + " (succeeded / failed): "
                           + loaded + " / " + failed);
      }
    }
    return interval;
  }

  private static boolean addDataSource(File     repository,
                                       String   dataSource,
                                       boolean  verbose)
  {
    // add the data source and reinitialize
    Configuration config = configSources(repository,
                                         Collections.singleton(dataSource),
                                         verbose);
    if (config == null) return false;
    destroyApis();
    initApis(repository, verbose);
    return true;
  }

  private static RecordReader provideJsonRecords(File    sourceFile,
                                                 String  dataSource)
  {
    RecordReader recordReader = null;
    // check if we have a real JSON array
    try {
      FileInputStream    fis = new FileInputStream(sourceFile);
      InputStreamReader  isr = new InputStreamReader(fis, "UTF-8");
      BufferedReader     br  = new BufferedReader(isr);

      recordReader = new RecordReader(br, dataSource);

      RecordReader.Format format = recordReader.getFormat();
      if (format != JSON && format != JSON_LINES) {
        System.err.println();
        System.err.println(
            "JSON file does not contain JSON or JSON-lines formatted records");
        System.err.println();
        return null;
      }

    } catch (IOException e) {
      e.printStackTrace();
      System.err.println();
      System.err.println("Failed to read file: " + sourceFile);
      System.err.println();
      return null;
    }

    // return the record reader
    return recordReader;
  }

  private static RecordReader provideCsvRecords(File   sourceFile,
                                                String dataSource)
  {
    RecordReader recordReader = null;
    // check if we have a real JSON array
    try {
      FileInputStream    fis = new FileInputStream(sourceFile);
      InputStreamReader  isr = new InputStreamReader(fis, "UTF-8");
      BufferedReader     br  = new BufferedReader(isr);

      recordReader = new RecordReader(CSV, br, dataSource);

    } catch (IOException e) {
      e.printStackTrace();
      System.err.println();
      System.err.println("Failed to read file: " + sourceFile);
      System.err.println();
      return null;
    }

    // return the record reader
    return recordReader;
  }

  /**
   * Loads a single JSON record to the repository -- optionally setting
   * the data source for the record.  NOTE: if the specified record does not
   * have a DATA_SOURCE property then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param jsonRecord The JSON record to load.
   * @param dataSource The data source to use for loading the record.
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean addRecord(File    repository,
                                  String  jsonRecord,
                                  String  dataSource)
  {
    return addRecord(repository, false, jsonRecord, dataSource, false);
  }

  /**
   * Loads a single JSON record to the repository -- optionally setting
   * the data source for the record.  NOTE: if the specified record does not
   * have a DATA_SOURCE property then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param jsonRecord The JSON record to load.
   * @param dataSource The data source to use for loading the record.
   *
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean addRecord(File    repository,
                                  boolean verbose,
                                  String  jsonRecord,
                                  String  dataSource)
  {
    return addRecord(repository, verbose, jsonRecord, dataSource, false);
  }

  /**
   * Loads a single JSON record to the repository -- optionally setting
   * the data source for the record.  NOTE: if the specified record does not
   * have a DATA_SOURCE property then the specified data source is required.
   *
   * @param repository The directory for the repository.
   * @param jsonRecord The JSON record to load.
   * @param dataSource The data source to use for loading the record.
   *
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean addRecord(File    repository,
                                  String  jsonRecord,
                                  String  dataSource,
                                  boolean silent)
  {
    return addRecord(repository, false, jsonRecord, dataSource, silent);
  }


  /**
   * Loads a single JSON record to the repository -- optionally setting
   * the data source for the record.  NOTE: if the specified
   * record does not have a DATA_SOURCE property then the specified data source
   * is required.
   *
   * @param repository The directory for the repository.
   * @param jsonRecord The JSON record to load.
   * @param dataSource The data source to use for loading the record.
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   *
   * @return <tt>true</tt> if successful, otherwise <tt>false</tt>
   */
  public static boolean addRecord(File     repository,
                                  boolean  verbose,
                                  String   jsonRecord,
                                  String   dataSource,
                                  boolean  silent)
  {
    initApis(repository, verbose);

    Set<String> dataSources = getDataSources();
    JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonRecord);

    if (dataSource == null) {
      dataSource = JsonUtilities.getString(jsonObject, "DATA_SOURCE");
    }
    if (dataSource == null) {
      System.err.println();
      System.err.println("ERROR: Could not determine data source for record.");
      System.err.println();
      return false;
    }

    // check if the data source is configured
    dataSource = dataSource.toUpperCase();
    if (!dataSources.contains(dataSource)) {
      if (!addDataSource(repository, dataSource, verbose)) return false;
      dataSources.add(dataSource);
    }

    String recordId = JsonUtilities.getString(jsonObject, "RECORD_ID");
    StringBuffer sb = new StringBuffer();
    String loadId = (new Date()).toString();
    int returnCode
        = (recordId != null)
        ? ENGINE_API.addRecord(dataSource, recordId, jsonRecord, loadId)
        : ENGINE_API.addRecordWithReturnedRecordID(dataSource,
                                                   sb,
                                                   jsonRecord,
                                                   loadId);
    if (returnCode != 0) {
      logError("G2Engine.addRecord"
                   + ((recordId == null) ? "WithReturnedRecordId()" : "()"),
               ENGINE_API);
      return false;
    }

    processRedos(silent);

    if (!silent) {
      System.out.println();
      System.out.println("Added record to " + dataSource + " data source: ");
      System.out.println(jsonRecord);
      System.out.println();
    }

    return true;
  }

  /**
   * Updates the configuration to be the configuration in the specified {@link
   * JsonObject}.
   *
   * @param repository The directory for the repository.
   * @param configJson The {@link JsonObject} describing the configuration.
   * @param comment The comment to associate with the config.
   *
   * @return The {@link Configuration} describing the new configuration or
   *         <tt>null</tt> if the operation failed.
   */
  public static Configuration updateConfig(File       repository,
                                           JsonObject configJson,
                                           String     comment)
  {
    return updateConfig(repository, configJson, comment, false);
  }

  /**
   * Updates the configuration to be the configuration in the specified {@link
   * JsonObject}.
   *
   * @param repository The directory for the repository.
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   * @param configJson The {@link JsonObject} describing the configuration.
   * @param comment The comment to associate with the config.
   *
   * @return The {@link Configuration} describing the new configuration or
   *         <tt>null</tt> if the operation failed.
   */
  public static Configuration updateConfig(File       repository,
                                           boolean    verbose,
                                           JsonObject configJson,
                                           String     comment)
  {
    return updateConfig(repository, verbose, configJson, comment, false);
  }

  /**
   * Updates the configuration to be the configuration in the specified {@link
   * JsonObject}.
   *
   * @param repository The directory for the repository.
   * @param configJson The {@link JsonObject} describing the configuration.
   * @param comment The comment to associate with the config.
   * @param silent <tt>true</tt> if no feedback should be given to the user
   *               upon completion, otherwise <tt>false</tt>
   *
   * @return The {@link Configuration} describing the new configuration or
   *         <tt>null</tt> if the operation failed.
   */
  public static Configuration updateConfig(File       repository,
                                           JsonObject configJson,
                                           String     comment,
                                           boolean    silent)
  {
    return updateConfig(repository, false, configJson, comment, silent);
  }

  /**
   * Updates the configuration to be the configuration in the specified {@link
   * JsonObject}.
   *
   * @param repository The directory for the repository.
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   * @param configJson The {@link JsonObject} describing the configuration.
   * @param comment The comment to associate with the config.
   * @param silent <tt>true</tt> if no feedback should be given to the user
   *               upon completion, otherwise <tt>false</tt>
   *
   * @return The {@link Configuration} describing the new configuration or
   *         <tt>null</tt> if the operation failed.
   */
  public static Configuration updateConfig(File         repository,
                                           boolean      verbose,
                                           JsonObject   configJson,
                                           String       comment,
                                           boolean      silent)
  {
    initApis(repository, verbose);
    Long        resultConfigId  = null;
    JsonObject  resultConfig    = null;

    Result<Long> configId = new Result<>();
    int returnCode = 0;
    String configJsonText = JsonUtilities.toJsonText(configJson);
    Result<Long> result = new Result<>();
    returnCode = CONFIG_MGR_API.addConfig(configJsonText, comment, result);
    if (returnCode != 0) {
      logError("G2ConfigMgr.addConfig()", CONFIG_MGR_API);
      return null;
    }
    resultConfigId = result.getValue();
    returnCode = CONFIG_MGR_API.setDefaultConfigID(resultConfigId);
    if (returnCode != 0) {
      logError("G2ConfigMgr.setDefaultConfigID()", CONFIG_MGR_API);
      return null;
    }

    StringBuffer sb = new StringBuffer();
    returnCode = CONFIG_MGR_API.getConfig(resultConfigId, sb);
    if (returnCode != 0) {
      logError("G2ConfigMgr.getConfig()", CONFIG_MGR_API);
      return null;
    }

    // parse the configuration
    resultConfig = JsonUtilities.parseJsonObject(sb.toString());

    if (!silent) {
      System.out.println();
      System.out.println("Added config and set as default: " + resultConfigId);
      System.out.println();
    }

    destroyApis();
    initApis(repository, verbose);

    // check if the result config ID is not set (usually means that all the
    // data sources to be added already existed)
    if (resultConfigId == null) {
      return getDefaultConfig();
    }

    return new Configuration(resultConfigId, resultConfig);
  }


  /**
   * Configures the specified data sources for the specified repository
   * if not already configured.
   *
   * @param repository The directory for the repository.
   * @param dataSources The {@link Set} of data source codes.
   *
   * @return The {@link Configuration} describing the new configuration or
   *         <tt>null</tt> if the operation failed.
   */
  public static Configuration configSources(File         repository,
                                            Set<String>  dataSources)
  {
    return configSources(repository, dataSources, false);
  }

  /**
   * Configures the specified data sources for the specified repository
   * if not already configured.
   *
   * @param repository The directory for the repository.
   * @param dataSources The {@link Set} of data source codes.
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   *
   * @return The {@link Configuration} describing the new configuration or
   *         <tt>null</tt> if the operation failed.
   */
  public static Configuration configSources(File         repository,
                                            boolean      verbose,
                                            Set<String>  dataSources)
  {
    return configSources(repository, verbose, dataSources, false);
  }

  /**
   * Configures the specified data sources for the specified repository
   * if not already configured.
   *
   * @param repository The directory for the repository.
   * @param dataSources The {@link Set} of data source codes.
   * @param silent <tt>true</tt> if no feedback should be given to the user
   *               upon completion, otherwise <tt>false</tt>
   *
   * @return The {@link Configuration} describing the new configuration or
   *         <tt>null</tt> if the operation failed.
   */
  public static Configuration configSources(File         repository,
                                            Set<String>  dataSources,
                                            boolean      silent)
  {
    return configSources(repository, false, dataSources, silent);
  }

  /**
   * Configures the specified data sources for the specified repository
   * if not already configured.
   *
   * @param repository The directory for the repository.
   * @param verbose <tt>true</tt> for verbose API logging, otherwise
   *                <tt>false</tt>
   * @param dataSources The {@link Set} of data source codes.
   * @param silent <tt>true</tt> if no feedback should be given to the user
   *               upon completion, otherwise <tt>false</tt>
   *
   * @return The {@link Configuration} describing the new configuration or
   *         <tt>null</tt> if the operation failed.
   */
  public static Configuration configSources(File         repository,
                                            boolean      verbose,
                                            Set<String>  dataSources,
                                            boolean      silent)
  {
    initApis(repository, verbose);
    Long        resultConfigId  = null;
    JsonObject  resultConfig    = null;

    Result<Long> configId = new Result<>();
    int returnCode = 0;
    try {
      Set<String> existingSet = getDataSources(configId);

      Map<String, Boolean> dataSourceActions = new LinkedHashMap<>();
      Set<String> addedDataSources = new LinkedHashSet<>();
      int addedCount = 0;
      for (String dataSourceCode : dataSources) {
        if (existingSet.contains(dataSourceCode)) {
          dataSourceActions.put(dataSourceCode, false);
          continue;
        }
        StringBuffer sb = new StringBuffer();
        SzDataSource dataSource = SzDataSource.FACTORY.create(dataSourceCode);
        returnCode = CONFIG_API.addDataSource(
            configId.getValue(), dataSource.toNativeJson(), sb);
        if (returnCode != 0) {
          logError("G2Config.addDataSource()", CONFIG_API);
          return null;
        }
        dataSourceActions.put(dataSourceCode, true);
        addedDataSources.add(dataSourceCode);
        addedCount++;
      }

      if (addedCount > 0) {
        String comment = buildAddedComment(
            "Added data sources: ", addedDataSources);

        Result<Long> configIdResult = new Result<>();
        resultConfig = addConfigAndSetDefault(
            configId.getValue(), comment, configIdResult);

        if (resultConfig == null) return null;

        // get the result config and its ID for the result
        resultConfigId  = configIdResult.getValue();
      }

      if (!silent) {
        System.out.println();
        System.out.println("Ensured specified data sources are configured.");
        System.out.println("     Repository   : " + repository);
        System.out.println("     Data Sources : ");
        dataSourceActions.entrySet().forEach(entry -> {
          System.out.println(
              "          - " + entry.getKey()
              + " (" + ((entry.getValue()) ? "added" : "preconfigured") + ")");
        });
        System.out.println();
      }

      if (addedCount > 0) {
        destroyApis();
        initApis(repository, verbose);
      }

    } finally {
      if (configId.getValue() != null) {
        CONFIG_API.close(configId.getValue());
      }
    }

    // check if the result config ID is not set (usually means that all the
    // data sources to be added already existed)
    if (resultConfigId == null) {
      return getDefaultConfig();
    }

    return new Configuration(resultConfigId, resultConfig);
  }

  /**
   * Builds a comment for adding config objects.
   */
  private static String buildAddedComment(String prefix, Set<String> addedSet) {
    String comment;
    if (addedSet.size() == 1) {
      comment = prefix + addedSet.iterator().next();
    } else {
      StringBuilder commentSB = new StringBuilder();
      commentSB.append(prefix);
      Iterator<String> iter = addedSet.iterator();
      String sep = "";
      while (iter.hasNext()) {
        String code = iter.next();
        commentSB.append(sep).append(code);
        sep = iter.hasNext() ? ", " : " and ";
      }
      comment = commentSB.toString();
    }
    return comment;
  }

  /**
   * Adds the config associated witht he specified handle using the specified
   * comment and returns the {@link JsonObject} for the config along with
   * setting the config's ID in the specified result parameter.
   */
  private static JsonObject addConfigAndSetDefault(
      long configHandle, String comment, Result<Long> resultConfig)
  {
    // write the modified config to a string buffer
    StringBuffer sb = new StringBuffer();
    int returnCode = CONFIG_API.save(configHandle, sb);
    if (returnCode != 0) {
      logError("G2Config.save()", CONFIG_API);
      return null;
    }

    Result<Long> result = new Result<>();
    String configJsonText = sb.toString();
    returnCode = CONFIG_MGR_API.addConfig(configJsonText, comment, result);
    if (returnCode != 0) {
      logError("G2ConfigMgr.addConfig()", CONFIG_MGR_API);
      return null;
    }

    returnCode = CONFIG_MGR_API.setDefaultConfigID(result.getValue());
    if (returnCode != 0) {
      logError("G2ConfigMgr.setDefaultConfigID()", CONFIG_MGR_API);
      return null;
    }

    // get the result config and its ID for the result
    resultConfig.setValue(result.getValue());
    return JsonUtilities.parseJsonObject(configJsonText);
  }

  /**
   * Gets the {@link JsonObject} describing the current default config as well
   * as setting the default config's ID in the specified result parameter.
   */
  private static Configuration getDefaultConfig() {
    Result<Long> result = new Result<>();
    int returnCode = CONFIG_MGR_API.getDefaultConfigID(result);
    if (returnCode != 0) {
      logError("G2ConfigMgr.getDefaultConfigID()", CONFIG_MGR_API);
      return null;
    }
    StringBuffer sb = new StringBuffer();
    returnCode = CONFIG_MGR_API.getConfig(result.getValue(), sb);
    if (returnCode != 0) {
      logError("G2ConfigMgr.getConfig()", CONFIG_MGR_API);
      return null;
    }
    JsonObject config = JsonUtilities.parseJsonObject(sb.toString());
    return new Configuration(result.getValue(), config);
  }

}
