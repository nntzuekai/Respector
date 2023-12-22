package com.senzing.api.services;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.senzing.api.model.*;
import com.senzing.nativeapi.NativeApiFactory;
import com.senzing.nativeapi.replay.ReplayNativeApiProvider;
import com.senzing.api.server.SzApiServer;
import com.senzing.api.server.SzApiServerOptions;
import com.senzing.g2.engine.*;
import com.senzing.repomgr.RepositoryManager;
import com.senzing.util.AccessToken;
import com.senzing.util.JsonUtilities;
import com.senzing.util.LoggingUtilities;
import org.junit.jupiter.params.provider.Arguments;

import javax.json.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import static com.senzing.io.IOUtilities.*;
import static org.junit.jupiter.api.Assumptions.*;
import static com.senzing.util.LoggingUtilities.*;
import static com.senzing.repomgr.RepositoryManager.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Provides an abstract base class for services tests that will create a
 * Senzing repository and startup the API server configured to use that
 * repository.  It also provides hooks to load the repository with data.
 */
public abstract class AbstractServiceTest {
  /**
   * Flag to control whether or not debug logging should be enabled when
   * running the tests.
   */
  public static final Boolean DEBUG_LOGGING
      = Boolean.valueOf("" + System.getProperty("com.senzing.api.test.debug"));

  /**
   * The replay provider to use.
   */
  private static final ReplayNativeApiProvider REPLAY_PROVIDER
      = new ReplayNativeApiProvider();

  /**
   * Whether or not the Senzing native API is available and the G2 native
   * library could be loaded.
   */
  private static final boolean NATIVE_API_AVAILABLE;

  /**
   * Message to display when the Senzing API is not available and the tests
   * are being skipped.
   */
  private static final String NATIVE_API_UNAVAILABLE_MESSAGE;

  static {
    G2Product productApi = null;
    StringWriter sw = new StringWriter();
    try {
      PrintWriter pw = new PrintWriter(sw);
      pw.println();
      pw.println("Skipping Tests: The Senzing Native API is NOT available.");
      pw.println("Check that SENZING_DIR environment variable is properly defined.");
      pw.println("Alternatively, you can run maven (mvn) with -Dsenzing.dir=[path].");
      pw.println();

      try {
        productApi = new G2ProductJNI();
      } catch (Throwable ignore) {
        // do nothing
      }

      NativeApiFactory.installProvider(REPLAY_PROVIDER);

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      NATIVE_API_AVAILABLE = (productApi != null);
      NATIVE_API_UNAVAILABLE_MESSAGE = sw.toString();
    }
  }

  /**
   * The time of the last progress log.
   */
  private long progressLogTimestamp = -1L;

  /**
   * The API Server being used to run the tests.
   */
  private SzApiServer server;

  /**
   * The repository directory used to run the tests.
   */
  private File repoDirectory;

  /**
   * The access token to use for privileged access to created objects.
   */
  private AccessToken accessToken;

  /**
   * The access token to use to unregister the API provider.
   */
  private AccessToken providerToken;

  /**
   * Whether or not the repository has been created.
   */
  private boolean repoCreated = false;

  /**
   * The number of tests that failed for this instance.
   */
  private int failureCount = 0;

  /**
   * The number of tests that succeeded for this instance.
   */
  private int successCount = 0;

  /**
   * The default configuration ID.
   */
  private Long bootstrapConfigId = null;

  /**
   * The default configuration ID.
   */
  private Long initialConfigId = null;

  /**
   * The map of default data sources.
   */
  private Map<String, SzDataSource> defaultDataSources = Collections.emptyMap();

  /**
   * The map of default attribute types.
   */
  private Map<String, SzAttributeType> defaultAttributeTypes
      = Collections.emptyMap();

  /**
   * The map of the initial data sources (after the repository is prepared).
   */
  private Map<String, SzDataSource> initialDataSources = Collections.emptyMap();

  /**
   * The map of initial attribute types (after the repository is prepared).
   */
  private Map<String, SzAttributeType> initialAttributeTypes
      = Collections.emptyMap();

  /**
   * The access token to deregister the current test suite.
   */
  private AccessToken replayTestToken = null;

  /**
   * Creates a temp repository directory.
   *
   * @param prefix The directory name prefix for the temp repo directory.
   *
   * @return The {@link File} representing the directory.
   */
  private static File createTempRepoDirectory(String prefix) {
    try {
      File    targetDir     = null;
      String  buildDirProp  = System.getProperty("project.build.directory");
      if (buildDirProp != null) {
        targetDir = new File(buildDirProp);
      } else {
        String workingDir = System.getProperty("user.dir");
        File currentDir = new File(workingDir);
        targetDir = new File(currentDir, "target");
      }

      boolean forceTempRepos = false;
      String prop = System.getProperty("senzing.test.tmp.repos");
      if (prop != null && prop.toLowerCase().trim().equals("true")) {
        forceTempRepos = true;
      }

      // check if we have a target directory (i.e.: maven build)
      if (forceTempRepos || !targetDir.exists()) {
        // if no target directory then use the temp directory
        return Files.createTempDirectory(prefix).toFile();
      }

      // if we have a target directory then use it as a parent for our test repo
      File testRepoDir = new File(targetDir, "test-repos");
      if (!testRepoDir.exists()) {
        testRepoDir.mkdirs();
      }

      // create a temp directory inside the test repo directory
      return Files.createTempDirectory(testRepoDir.toPath(), prefix).toFile();

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Protected default constructor.
   */
  protected AbstractServiceTest() {
    this(null);
  }

  /**
   * Protected constructor allowing the derived class to specify the
   * location for the entity respository.
   *
   * @param repoDirectory The directory in which to include the entity
   *                      repository.
   */
  protected AbstractServiceTest(File repoDirectory) {
    if (repoDirectory == null) {
      String repoPrefix = "sz-repo-" + this.getClass().getSimpleName() + "-";
      repoDirectory = createTempRepoDirectory(repoPrefix);
    }
    this.server = null;
    this.repoDirectory = repoDirectory;
    this.accessToken = new AccessToken();
    this.providerToken = null;
  }

  /**
   * Signals the beginning of the current test suite.
   *
   * @return <tt>true</tt> if replaying previous results and <tt>false</tt>
   *         if using the live API.
   */
  protected boolean beginTests() {
    this.replayTestToken = REPLAY_PROVIDER.beginTests(this.getClass());
    return REPLAY_PROVIDER.isReplaying();
  }

  /**
   * Signals the end of the current test suite.
   */
  protected void endTests() {
    try {
      File testCacheZip = REPLAY_PROVIDER.getTestCacheZip();
      REPLAY_PROVIDER.endTests(this.replayTestToken);
      this.replayTestToken = null;
      if (this.getFailureCount() > 0 && REPLAY_PROVIDER.isCacheStale()) {
        System.out.println();
        System.out.println("**********************");
        System.out.println("** WARNING: DEPENDENCIES HAVE CHANGED");
        System.out.println("** CACHED TEST RESULTS MAY BE INVALID");
        System.out.println("** " + testCacheZip);
        System.out.println("**********************");
        System.out.println();
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Returns the base URL for validating tests responses.
   *
   * @return The base URL for validating test responses.
   */
  protected String getBaseUri() {
    StringBuilder sb = new StringBuilder();
    sb.append("http://localhost:");
    if (this.server != null) {
      sb.append(this.server.getHttpPort());
    } else {
      sb.append("2080");
    }
    sb.append("/");
    return sb.toString();
  }

  /**
   * Creates an absolute URI for the relative URI provided.  For example, if
   * <tt>"license"</tt> was passed as the parameter then
   * <tt>"http://localhost:[port]/license"</tt> will be returned where
   * <tt>"[port]"</tt> is the port number of the currently running server, if
   * running, and is <tt>"2080"</tt> (the default port) if not running.
   *
   * @param relativeUri The relative URI to build the absolute URI from.
   * @return The absolute URI for localhost on the current port.
   */
  protected String formatServerUri(String relativeUri) {
    return this.formatServerUri(relativeUri, null);
  }

  /**
   * Creates an absolute URI for the relative URI provided.  For example, if
   * <tt>"license"</tt> was passed as the parameter then
   * <tt>"http://localhost:[port]/license"</tt> will be returned where
   * <tt>"[port]"</tt> is the port number of the currently running server, if
   * running, and is <tt>"2080"</tt> (the default port) if not running.
   *
   * @param basePath The relative URI to build the absolute URI from.
   * @param queryParams The optional query parameters to append.
   * @return The absolute URI for localhost on the current port.
   */
  protected String formatServerUri(String basePath, Map<String, ?> queryParams)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("http://localhost:");
    if (this.server != null) {
      sb.append(this.server.getHttpPort());
    } else {
      sb.append("2080");
    }
    if (basePath.startsWith(sb.toString())) {
      sb.delete(0, sb.length());
      sb.append(basePath);
      return basePath;
    } else {
      sb.append("/" + basePath);
    }

    if (queryParams != null && queryParams.size() > 0) {
      String initialPrefix = basePath.contains("?") ? "&" : "?";
      int initialLength = sb.length();

      queryParams.entrySet().forEach(entry -> {
        String key = entry.getKey();
        Object value = entry.getValue();
        Collection values = null;
        if (value instanceof Collection) {
          values = (Collection) value;
        } else {
          values = Collections.singletonList(value);
        }
        try {
          key = URLEncoder.encode(key, "UTF-8");
          for (Object val : values) {
            if (val == null) return;
            String textValue = val.toString();
            textValue = URLEncoder.encode(textValue, "UTF-8");
            sb.append((sb.length() == initialLength) ? initialPrefix : "&");
            sb.append(key).append("=").append(textValue);
          }
        } catch (UnsupportedEncodingException cannotHappen) {
          throw new RuntimeException(cannotHappen);
        }
      });
    }

    return sb.toString();

  }

  /**
   * Checks that the Senzing Native API is available and if not causes the
   * test or tests to be skipped.
   *
   * @return <tt>true</tt> if the native API's are available, otherwise
   * <tt>false</tt>
   */
  protected boolean assumeNativeApiAvailable() {
    assumeTrue(checkNativeApiAvailable(), NATIVE_API_UNAVAILABLE_MESSAGE);
    return checkNativeApiAvailable();
  }

  /**
   * Checks if the native API is directly available or is being replayed from
   * a cache.
   *
   * @return <tt>true</tt> if the native API functions can be called, otherwise
   *         <tt>false</tt>.
   */
  protected boolean checkNativeApiAvailable() {
    return NATIVE_API_AVAILABLE || REPLAY_PROVIDER.isReplaying();
  }

  /**
   * This method can typically be called from a method annotated with
   * "@BeforeClass".  It will create a Senzing entity repository and
   * initialize and start the Senzing API Server.
   */
  protected void initializeTestEnvironment() {
    if (!NATIVE_API_AVAILABLE && !REPLAY_PROVIDER.isReplaying()) return;
    String moduleName = this.getModuleName("RepoMgr (create)");
    RepositoryManager.setThreadModuleName(moduleName);
    boolean concluded = false;
    try {
      Configuration config = RepositoryManager.createRepo(
          this.getRepositoryDirectory(), true);
      this.repoCreated = true;

      this.processDefaultConfig(config);

      this.prepareRepository();

      RepositoryManager.conclude();
      concluded = true;

      // initialize the server
      this.initializeServer();

      // process the post-initialization config
      this.processInitialConfig();

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Error e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
       if (!concluded) RepositoryManager.conclude();
       RepositoryManager.clearThreadModuleName();
    }
  }

  /**
   * Process the default config.
   */
  private void processDefaultConfig(Configuration config) {
    this.defaultDataSources     = new LinkedHashMap<>();
    this.defaultAttributeTypes  = new LinkedHashMap<>();
    this.processConfig(config.getConfigJson(),
                       this.defaultDataSources,
                       this.defaultAttributeTypes);

    this.defaultDataSources
        = Collections.unmodifiableMap(this.defaultDataSources);

    this.defaultAttributeTypes
        = Collections.unmodifiableMap(this.defaultAttributeTypes);

    this.bootstrapConfigId = config.getConfigId();
  }

  /**
   * Processes the initial configuration after the server starts up.
   *
   */
  private void processInitialConfig() {
    G2Engine engineApi = this.server.getEngineApi();
    Configuration config = this.server.executeInThread(() -> {
      StringBuffer sb = new StringBuffer();
      int returnCode = engineApi.exportConfig(sb);
      if (returnCode != 0) {
        throw new IllegalStateException(
            formatError("G2Engine.exportConfig()", engineApi));
      }

      JsonObject configJson = JsonUtilities.parseJsonObject(sb.toString());

      Result<Long> result = new Result<>();
      returnCode = engineApi.getActiveConfigID(result);
      if (returnCode != 0) {
        throw new IllegalStateException(
            formatError("G2Engine.getActiveConfigID()", engineApi));
      }
      return new Configuration(result.getValue(), configJson);
    });

    this.initialDataSources     = new LinkedHashMap<>();
    this.initialAttributeTypes  = new LinkedHashMap<>();

    this.processConfig(config.getConfigJson(),
                       this.initialDataSources,
                       this.initialAttributeTypes);

    // make the maps unmodifiable
    this.initialDataSources
        = Collections.unmodifiableMap(this.initialDataSources);
    this.initialAttributeTypes
        = Collections.unmodifiableMap(this.initialAttributeTypes);

    this.initialConfigId = config.getConfigId();

    // fire the post initialization callback
    this.doPostServerInitialization(this.server,
                                    config.getConfigId(),
                                    config.getConfigJson());
  }

  /**
   * Sets the default configuration to the initial configuration ID and
   * then ensures the configuration is current.
   *
   */
  protected void revertToInitialConfig() {
    if (this.initialConfigId == null) {
      throw new IllegalStateException(
          "Cannot revert to the initial config before initialization.");
    }
    G2ConfigMgr configMgrApi = this.server.getConfigMgrApi();
    if (configMgrApi == null) {
      throw new IllegalStateException(
          "Cannot revert the configuration if the server is pinned to a "
          + "specific configuration ID.");
    }
    int returnCode = configMgrApi.setDefaultConfigID(this.initialConfigId);
    if (returnCode != 0) {
      throw new IllegalStateException(
          formatError("G2ConfigMgr.setDefaultConfigID()",
                      configMgrApi));
    }
    Boolean result = this.server.ensureConfigCurrent();
    if (result == null) {
      throw new IllegalStateException(
          "Unable to revert the configuration due to an error.");
    }
  }

  /**
   * Process the specified config and puts the data sources and attribute types
   * in the specified maps.
   */
  private void processConfig(JsonObject                   config,
                             Map<String, SzDataSource>    dataSourceMap,
                             Map<String, SzAttributeType> attributeTypeMap)
  {
    config = config.getJsonObject("G2_CONFIG");

    if (dataSourceMap != null) {
      // get the data sources
      JsonArray jsonArray = config.getJsonArray("CFG_DSRC");
      for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
        String  dataSourceCode  = jsonObject.getString("DSRC_CODE");
        int     dataSourceId    = jsonObject.getInt("DSRC_ID");
        SzDataSource dataSource = SzDataSource.FACTORY.create(dataSourceCode, dataSourceId);
        dataSourceMap.put(dataSource.getDataSourceCode(), dataSource);
      }
    }

    if (attributeTypeMap != null) {
      JsonArray jsonArray = config.getJsonArray("CFG_ATTR");
      for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
        SzAttributeType attributeType
            = SzAttributeType.parseAttributeType(null, jsonObject);
        attributeTypeMap.put(attributeType.getAttributeCode(), attributeType);
      }
    }
  }

  /**
   * Return the unmodifiable {@link Map} of {@link String} data source code
   * keys to {@link SzDataSource} values describing the data sources
   * included in the default base template configuration.
   *
   * @return The unmodifiable {@link Map} of default data sources.
   */
  protected Map<String, SzDataSource> getDefaultDataSources() {
    return this.defaultDataSources;
  }

  /**
   * Return the unmodifiable {@link Map} of {@link String} attribute tyoe code
   * keys to {@link SzAttributeType} values describing the attribute types
   * included in the default base template configuration.
   *
   * @return The unmodifiable {@link Map} of default attribute types.
   */
  protected Map<String, SzAttributeType> getDefaultAttributeTypes() {
    return this.defaultAttributeTypes;
  }

  /**
   * Gets the unmodifiable {@link Map} of {@link String} data source code
   * keys to {@link SzDataSource} values describing the data sources that
   * were configured when the server was first initialized (after the
   * repository has been prepared).
   *
   * @return The unmodifiable {@link Map} of {@link String} data source code
   *         keys to {@link SzDataSource} values describing the initial data
   *         sources.
   */
  protected Map<String, SzDataSource> getInitialDataSources() {
    return this.initialDataSources;
  }

  /**
   * Gets the unmodifiable {@link Map} of {@link String} attribute type code
   * keys to {@link SzAttributeType} values describing the attribute types that
   * were configured when the server was first initialized (after the
   * repository has been prepared).
   *
   * @return The unmodifiable {@link Map} of {@link String} attribute type code
   *         keys to {@link SzAttributeType} values describing the initial
   *         attribute types.
   */
  protected Map<String, SzAttributeType> getInitialAttributeTypes() {
    return this.initialAttributeTypes;
  }

  /**
   * Called after the API Server is first started (not on reinitialization) to
   * handle any test suite initialization that requires the use of the server
   * or the initial configuration.
   *
   * @param provider The {@link SzApiProvider}.
   * @param initialConfigId The initial configuration ID.
   * @param initialConfig The {@link JsonObject} describing the inital
   *                      configuration.
   */
  protected void doPostServerInitialization(SzApiProvider provider,
                                            long          initialConfigId,
                                            JsonObject    initialConfig)
  {
    // do nothing
  }

  /**
   * This method can typically be called from a method annotated with
   * "@AfterClass".  It will shutdown the server and delete the entity
   * repository that was created for the tests if there are no test failures
   * recorded via {@link #incrementFailureCount()}.
   */
  protected void teardownTestEnvironment() {
    int failureCount = this.getFailureCount();
    teardownTestEnvironment((failureCount == 0));
  }

  /**
   * This method can typically be called from a method annotated with
   * "@AfterClass".  It will shutdown the server and optionally delete
   * the entity repository that was created for the tests.
   *
   * @param deleteRepository <tt>true</tt> if the test repository should be
   *                         deleted, otherwise <tt>false</tt>
   */
  protected void teardownTestEnvironment(boolean deleteRepository) {
    // destroy the server
    if (this.server != null) this.destroyServer();

    String preserveProp = System.getProperty("senzing.test.preserve.repos");
    if (preserveProp != null) preserveProp = preserveProp.trim().toLowerCase();
    boolean preserve = (preserveProp != null && preserveProp.equals("true"));

    // cleanup the repo directory
    if (this.repoCreated && deleteRepository && !preserve
        && this.repoDirectory.exists() && this.repoDirectory.isDirectory()) {
      try {
        // delete the repository
        Files.walk(this.repoDirectory.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    // for good measure
    if (NATIVE_API_AVAILABLE || REPLAY_PROVIDER.isReplaying()) {
      RepositoryManager.conclude();
    }
  }

  /**
   * Returns the {@link File} identifying the repository directory used for
   * the test.  This can be specified in the constructor, but if not specified
   * is a newly created temporary directory.
   */
  protected File getRepositoryDirectory() {
    return this.repoDirectory;
  }

  /**
   * Override this function to prepare the repository by configuring
   * data sources or loading records.  By default this function does nothing.
   * The repository directory can be obtained via {@link
   * #getRepositoryDirectory()}.
   */
  protected void prepareRepository() {
    // do nothing
  }

  /**
   * Purges the repository via the {@link SzApiServer}. This reinitializes and
   * recreates the worker thread pool.
   *
   */
  protected void livePurgeRepository() {
    this.server.purgeRepository();
  }

  /**
   * Stops the server if it is running and purges the repository.  After
   * purging the server is restarted.
   */
  protected void purgeRepository() {
    this.purgeRepository(true);
  }

  /**
   * Stops the server if it is running and purges the repository.  After
   * purging the server is <b>optionally</b> restarted.  You may not want to
   * restart the server if you intend to load more records into via the
   * {@link RepositoryManager} before restarting.
   *
   * @param restartServer <tt>true</tt> to restart the server and <tt>false</tt>
   *                      if you intend to restart it manually.
   * @see #restartServer()
   */
  protected void purgeRepository(boolean restartServer) {
    boolean running = (this.server != null);
    if (running) this.destroyServer();
    String moduleName = this.getModuleName("RepoMgr (purge)");
    RepositoryManager.setThreadModuleName(moduleName);
    try {
      RepositoryManager.purgeRepo(this.repoDirectory);
    } finally {
      RepositoryManager.clearThreadModuleName();
    }
    if (running && restartServer) {
      this.initializeServer();
    } else {
      RepositoryManager.conclude();
    }
  }

  /**
   * Prompts the reinitializer thread to ensure the config is current if the
   * server has a reinitializer thread running.  This does nothing if there is
   * no reinitializer thread.
   */
  protected void requestConfigRefreshCheck() {
    if (this.server == null) return;
    this.server.requestConfigRefreshCheck();
  }

  /**
   * Restarts the server.  If the server is already running it is shutdown
   * first and then started.  If not running it is started up.  This cannot
   * be called prior to the repository being created.
   */
  protected void restartServer() {
    if (!this.repoCreated) {
      throw new IllegalStateException(
          "Cannnot restart server prior to calling initializeTestEnvironment()");
    }
    RepositoryManager.conclude();
    this.destroyServer();
    this.initializeServer();
  }

  /**
   * Internal method for shutting down and destroying the server.  This method
   * has no effect if the server is not currently initialized.
   */
  private void destroyServer() {
    if (this.server == null) {
      System.err.println("WARNING: Server was not running at destroy");
      return;
    }
    SzApiProvider.Factory.uninstallProvider(this.providerToken);
    this.server.shutdown(this.accessToken);
    this.server.join();
    this.server = null;
  }

  /**
   * Returns the port that the server should bind to.  By default this returns
   * <tt>null</tt> to indicate that any available port can be used for the
   * server.  Override to use a specific port.
   *
   * @return The port that should be used in initializing the server, or
   * <tt>null</tt> if any available port is fine.
   */
  protected Integer getServerPort() {
    return null;
  }

  /**
   * Retuns the {@link InetAddress} used to initialize the server.  By default
   * this returns the address obtained for <tt>"127.0.0.1"</tt>.  Override this
   * to change the address.  Return <tt>null</tt> if all available interfaces
   * should be bound to.
   *
   * @return The {@link InetAddress} for initializing the server, or
   * <tt>null</tt> if the server should bind to all available network
   * interfaces.
   */
  protected InetAddress getServerAddress() {
    try {
      return InetAddress.getByName("127.0.0.1");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the concurrency with which to initialize the server.  By default
   * this returns one (1).  Override to use a different concurrency.
   *
   * @return The concurrency with which to initialize the server.
   */
  protected int getServerConcurrency() {
    return 1;
  }

  /**
   * Gets the version number of the REST API server implementation.
   *
   * @return The version number of the REST API server implementation.
   */
  protected String getApiServerVersion() {
    if (this.server == null) return null;
    return this.server.getApiProviderVersion();
  }

  /**
   * Gets the version number of the REST API specification implemented by
   * the underlying server.
   *
   * @return The version number of the REST API specification implemented by
   *         the underlying server.
   */
  protected String getRestApiVersion() {
    if (this.server == null) return null;
    return this.server.getRestApiVersion();
  }

  /**
   * Gets the version for the underlying runtime native Senzing API.
   *
   * @return The version for the underlying runtime native Senzing API.
   */
  protected String getNativeApiVersion() {
    if (this.server == null) {
      return null;
    }
    String result = this.server.getNativeApiVersion();
    return result;
  }

  /**
   * Gets the build version for the underlying runtime native Senzing API.
   *
   * @return The build version for the underlying runtime native Senzing API.
   */
  protected String getNativeApiBuildVersion() {
    if (this.server == null) return null;
    return this.server.getNativeApiBuildVersion();
  }

  /**
   * Gets the build number for the underlying runtime native Senzing API.
   *
   * @return The build number for the underlying runtime native Senzing API.
   */
  protected String getNativeApiBuildNumber() {
    if (this.server == null) return null;
    return this.server.getNativeApiBuildNumber();
  }

  /**
   * Gets the build date for the underlying runtime native Senzing API.
   *
   * @return The build date for the underlying runtime native Senzing API.
   */
  protected Date getNativeApiBuildDate() {
    if (this.server == null) return null;
    return this.server.getNativeApiBuildDate();
  }

  /**
   * Gets the configuration compatibility version for the underlying runtime
   * native Senzing API.
   *
   * @return The configuration compatibility version for the underlying runtime
   *         native Senzing API.
   */
  protected String getConfigCompatibilityVersion() {
    if (this.server == null) return null;
    return this.server.getConfigCompatibilityVersion();
  }

  /**
   * Returns the module name with which to initialize the server.  By default
   * this returns <tt>"Test API Server"</tt>.  Override to use a different
   * module name.
   *
   * param suffix The optional suffix to append to the module name.
   *
   * @return The module name with which to initialize the server.
   */
  protected String getModuleName(String suffix) {
    if (suffix != null && suffix.trim().length() > 0) {
      suffix = " - " + suffix.trim();
    } else {
      suffix = "";
    }
    return this.getClass().getSimpleName() + suffix;
  }

  /**
   * Checks whether or not the server should be initialized in verbose mode.
   * By default this is <tt>true</tt>.  Override to set to <tt>false</tt>.
   *
   * @return <tt>true</tt> if the server should be initialized in verbose mode,
   * otherwise <tt>false</tt>.
   */
  protected boolean isVerbose() {
    return false;
  }

  /**
   * Checks whether or not the server should reduce the number of messges sent
   * as feedback to standard output.  By default this is <tt>true</tt> for
   * auto tests.  Override to set to <tt>false</tt>.
   *
   * @return <tt>true</tt> if the server should be initialized in verbose mode,
   * otherwise <tt>false</tt>.
   */
  protected boolean isQuiet() {
    return true;
  }

  /**
   * Sets the desired options for the {@link SzApiServer} during server
   * initialization.
   *
   * @param options The {@link SzApiServerOptions} to initialize.
   */
  protected void initializeServerOptions(SzApiServerOptions options) {
    options.setHttpPort(0);
    options.setBindAddress(this.getServerAddress());
    options.setConcurrency(this.getServerConcurrency());
    options.setModuleName(this.getModuleName("Test API Server"));
    options.setVerbose(this.isVerbose());
    options.setQuiet(this.isQuiet());
    options.setDebugLogging(DEBUG_LOGGING);
    options.setAutoRefreshPeriod(-1L); // refresh on demand for auto-tests
    options.setStatsInterval(0L); // don't log stats for tests
    options.setSkippingStartupPerformance(true); // no sense in doing perf check
  }

  /**
   * Internal method for initializing the server.
   */
  private void initializeServer() {
    RepositoryManager.conclude();

    if (this.server != null) {
      this.destroyServer();
    }

    try {
      File        repoDirectory = this.getRepositoryDirectory();
      File        initJsonFile  = new File(repoDirectory, "g2-init.json");
      String      initJsonText  = readTextFileAsString(initJsonFile, "UTF-8");
      JsonObject  initJson      = JsonUtilities.parseJsonObject(initJsonText);

      System.out.println("Initializing with initialization file: "
                         + initJsonFile);

      SzApiServerOptions options = new SzApiServerOptions(initJson);
      this.initializeServerOptions(options);

      this.server = new SzApiServer(this.accessToken, options);

      this.providerToken = SzApiProvider.Factory.installProvider(server);

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Increments the failure count and returns the new failure count.
   * @return The new failure count.
   */
  protected int incrementFailureCount() {
    this.failureCount++;
    this.conditionallyLogCounts(false);
    return this.failureCount;
  }

  /**
   * Increments the success count and returns the new success count.
   * @return The new success count.
   */
  protected int incrementSuccessCount() {
    this.successCount++;
    this.conditionallyLogCounts(false);
    return this.successCount;
  }

  /**
   * Conditionally logs the progress of the tests.
   *
   * @param complete <tt>true</tt> if tests are complete for this class,
   *                 otherwise <tt>false</tt>.
   */
  protected void conditionallyLogCounts(boolean complete) {
    int successCount = this.getSuccessCount();
    int failureCount = this.getFailureCount();

    long now = System.currentTimeMillis();
    long lapse = (this.progressLogTimestamp > 0L)
        ? (now - this.progressLogTimestamp) : 0L;

    if (complete || (lapse > 30000L)) {
      System.out.println(this.getClass().getSimpleName()
                         + (complete ? " Complete: " : " Progress: ")
                         + successCount + " (succeeded) / " + failureCount
                         + " (failed)");
      this.progressLogTimestamp = now;
    }
    if (complete) {
      System.out.println();
    }
    if (this.progressLogTimestamp < 0L) {
      this.progressLogTimestamp = now;
    }
  }

  /**
   * Returns the current failure count.  The failure count is incremented via
   * {@link #incrementFailureCount()}.
   *
   * @return The current success count.
   */
  protected int getFailureCount() {
    return this.failureCount;
  }

  /**
   * Returns the current success count.  The success count is incremented via
   * {@link #incrementSuccessCount()}.
   *
   * @return The current success count.
   */
  protected int getSuccessCount() {
    return this.successCount;
  }

  /**
   * Wrapper function for performing a test that will first check if
   * the native API is available via {@link #assumeNativeApiAvailable()} and
   * then record a success or failure.
   *
   * @param testFunction The {@link Runnable} to execute.
   */
  protected void performTest(Runnable testFunction) {
    this.performTest(true, testFunction);
  }

  /**
   * Wrapper function for performing a test that will first optionally check if
   * the native API is available {@link #assumeNativeApiAvailable()} and then
   * record a success or failure.
   *
   * @param testFunction The {@link Runnable} to execute.
   */
  protected void performTest(boolean requireNativeApi, Runnable testFunction) {
    if (requireNativeApi) this.assumeNativeApiAvailable();
    boolean success = false;
    try {
      testFunction.run();
      success = true;

    } catch (Error|RuntimeException e) {
      e.printStackTrace();
      System.err.flush();
      if ("true".equals(System.getProperty("com.senzing.api.test.fastFail"))) {
        try {
          Thread.sleep(5000L);
        } catch (InterruptedException ignore) {
          // do nothing
        }
        System.exit(1);
      }
      throw e;
    } finally {
      if (!success) this.incrementFailureCount();
      else this.incrementSuccessCount();
    }
  }

  /**
   * URL encodes the specified text using UTF-8 character encoding and traps
   * the {@link UnsupportedEncodingException} that is not actually possible with
   * UTF-8 encoding specified.
   *
   * @param text The text to encode.
   *
   * @return The URL-encoded text.
   */
  protected static String urlEncode(String text) {
    try {
      return URLEncoder.encode(text, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a proxy {@link UriInfo} to simulate a {@link UriInfo} when directly
   * calling the API services functions.
   *
   * @param selfLink The simulated request URI.
   *
   * @return The proxied {@link UriInfo} object using the specified URI.
   */
  protected UriInfo newProxyUriInfo(String selfLink) {
    try {
      final URI uri = new URI(selfLink);
      final URI baseUri = new URI(this.getBaseUri());

      InvocationHandler handler = (p, m, a) -> {
        switch (m.getName()) {
          case "getRequestUri":
            return uri;
          case "getBaseUri":
            return baseUri;
          default:
            throw new UnsupportedOperationException(
                "Operation not implemented on proxy UriInfo");
        }
      };

      ClassLoader loader = this.getClass().getClassLoader();
      Class[] classes = {UriInfo.class};

      return (UriInfo) Proxy.newProxyInstance(loader, classes, handler);

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a proxy {@link UriInfo} to simulate a {@link UriInfo} when directly
   * calling the API services functions.
   *
   * @param selfLink The simulated request URI.
   * @param queryParams The {@link MultivaluedMap} describing the query params.
   *
   * @return The proxied {@link UriInfo} object using the specified URI.
   */
  protected UriInfo newProxyUriInfo(String                        selfLink,
                                    MultivaluedMap<String,String> queryParams)
  {
    try {
      final URI uri = new URI(selfLink);
      final URI baseUri = new URI(this.getBaseUri());

      InvocationHandler handler = (p, m, a) -> {
        switch (m.getName()) {
          case "getRequestUri":
            return uri;
          case "getBaseUri":
            return baseUri;
          case "getQueryParameters":
            return queryParams;
          default:
            throw new UnsupportedOperationException(
                "Operation not implemented on proxy UriInfo");
        }
      };

      ClassLoader loader = this.getClass().getClassLoader();
      Class[] classes = {UriInfo.class};

      return (UriInfo) Proxy.newProxyInstance(loader, classes, handler);

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Invoke an operation on the currently running API server over HTTP.
   *
   * @param httpMethod    The HTTP method to use.
   * @param uri           The relative or absolute URI (optionally including query params)
   * @param responseClass The class of the response.
   * @param <T>           The response type.
   * @return
   */
  protected <T> T invokeServerViaHttp(
      SzHttpMethod httpMethod,
      String uri,
      Class<T> responseClass) {
    return this.invokeServerViaHttp(
        httpMethod, uri, null, null, responseClass);
  }

  /**
   * Invoke an operation on the currently running API server over HTTP.
   *
   * @param httpMethod    The HTTP method to use.
   * @param uri           The relative or absolute URI (optionally including query params)
   * @param queryParams   The optional map of query parameters.
   * @param bodyContent   The object to be converted to JSON for body content.
   * @param responseClass The class of the response.
   * @param <T>           The response type.
   * @return
   */
  protected <T> T invokeServerViaHttp(
      SzHttpMethod httpMethod,
      String uri,
      Map<String, ?> queryParams,
      Object bodyContent,
      Class<T> responseClass)
  {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JodaModule());
    try {
      uri = this.formatServerUri(uri, queryParams);

      String jsonContent = null;
      if (bodyContent != null) {
        jsonContent = objectMapper.writeValueAsString(bodyContent);
      }

      URL url = new URL(uri);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(httpMethod.toString());
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Accept-Charset", "utf-8");
      if (jsonContent != null) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
        osw.write(jsonContent);
        osw.flush();
        byte[] bytes = baos.toByteArray();
        int length = bytes.length;
        conn.addRequestProperty("Content-Length", "" + length);
        conn.addRequestProperty("Content-Type",
                                "application/json; charset=utf-8");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(bytes);
        os.flush();
      }

      int responseCode = conn.getResponseCode();
      boolean errorResponse = (responseCode < 200 || responseCode >= 300);
      InputStream is = (!errorResponse)
          ? conn.getInputStream() : conn.getErrorStream();
      InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      BufferedReader br = new BufferedReader(isr);
      StringBuilder sb = new StringBuilder();
      for (int nextChar = br.read(); nextChar >= 0; nextChar = br.read()) {
        sb.append((char) nextChar);
      }

      String responseJson = sb.toString();

      // check if a string is expected
      if (responseClass == String.class) {
        return (T) responseJson;
      }

      T result = null;
      try {
        result = objectMapper.readValue(responseJson, responseClass);

      } catch (Exception e) {
        // check if we have an unexpected error response
        if (errorResponse && !responseClass.equals(SzErrorResponse.class)) {
          System.err.println("UNEXPECTED ERROR RESPONSE: ");
          System.err.println(responseJson);
          System.err.println();
          throw new IllegalStateException(
              "UNEXPECTED ERROR RESPONSE: " + responseJson);
        }

        // log the deserialization exception
        System.err.println("DESERIALIZING RESPONSE JSON: ");
        System.err.println(responseJson);
        System.err.println();
        throw e;
      }
      return result;

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Invoke an operation on the currently running API server over HTTP.
   *
   * @param httpMethod    The HTTP method to use.
   * @param uri           The relative or absolute URI (optionally including query params)
   * @param queryParams   The optional map of query parameters.
   * @param contentType   The content type for the body.
   * @param bodyContent   The raw body content data.
   * @param responseClass The class of the response.
   * @param <T>           The response type.
   * @return
   */
  protected <T> T invokeServerViaHttp(
      SzHttpMethod httpMethod,
      String uri,
      Map<String, ?> queryParams,
      String contentType,
      byte[] bodyContent,
      Class<T> responseClass)
  {
    ByteArrayInputStream bodyStream = new ByteArrayInputStream(bodyContent);
    Long contentLength = new Long(bodyContent.length);
    return this.invokeServerViaHttp(httpMethod,
                                    uri,
                                    queryParams,
                                    contentType,
                                    contentLength,
                                    bodyStream,
                                    responseClass);
  }

  /**
   * Invoke an operation on the currently running API server over HTTP.
   *
   * @param httpMethod    The HTTP method to use.
   * @param uri           The relative or absolute URI (optionally including query params)
   * @param queryParams   The optional map of query parameters.
   * @param contentType   The content type for the body.
   * @param contentLength The optional content length.
   * @param bodyStream    The raw body content data.
   * @param responseClass The class of the response.
   * @param <T>           The response type.
   * @return
   */
  protected <T> T invokeServerViaHttp(
      SzHttpMethod httpMethod,
      String uri,
      Map<String, ?> queryParams,
      String contentType,
      Long contentLength,
      InputStream bodyStream,
      Class<T> responseClass)
  {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JodaModule());
    try {
      if (!uri.toLowerCase().startsWith("http://")) {
        uri = this.formatServerUri(uri);
      }
      if (queryParams != null && queryParams.size() > 0) {
        String initialPrefix = uri.contains("?") ? "&" : "?";

        StringBuilder sb = new StringBuilder();
        queryParams.entrySet().forEach(entry -> {
          String key = entry.getKey();
          Object value = entry.getValue();
          Collection values = null;
          if (value instanceof Collection) {
            values = (Collection) value;
          } else {
            values = Collections.singletonList(value);
          }
          try {
            key = URLEncoder.encode(key, "UTF-8");
            for (Object val : values) {
              if (val == null) return;
              String textValue = val.toString();
              textValue = URLEncoder.encode(textValue, "UTF-8");
              sb.append((sb.length() == 0) ? initialPrefix : "&");
              sb.append(key).append("=").append(textValue);
            }
          } catch (UnsupportedEncodingException cannotHappen) {
            throw new RuntimeException(cannotHappen);
          }
        });
        uri = uri + sb.toString();
      }

      URL url = new URL(uri);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(httpMethod.toString());
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Accept-Charset", "utf-8");
      if (bodyStream != null) {
        if (contentLength != null) {
          conn.addRequestProperty("Content-Length", "" + contentLength);
        }
        conn.addRequestProperty("Content-Type", contentType);
        conn.setDoOutput(true);
        OutputStream os = new BufferedOutputStream(conn.getOutputStream(),
                                                   8192);
        for (int byteRead = bodyStream.read();
             byteRead >= 0;
             byteRead = bodyStream.read()) {
          os.write(byteRead);
        }
        os.flush();
      }

      int responseCode = conn.getResponseCode();
      InputStream is = (responseCode >= 200 && responseCode < 300)
          ? conn.getInputStream() : conn.getErrorStream();
      InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      BufferedReader br = new BufferedReader(isr);
      StringBuilder sb = new StringBuilder();
      for (int nextChar = br.read(); nextChar >= 0; nextChar = br.read()) {
        sb.append((char) nextChar);
      }

      String responseJson = sb.toString();

      // check if a string is expected
      if (responseClass == String.class) {
        return (T) responseJson;
      }

      T result = null;
      try {
        result = objectMapper.readValue(responseJson, responseClass);
      } catch (Exception e) {
        System.out.println("DESERIALIZING RESPONSE JSON TO "
                               + responseClass.getSimpleName() + ": ");
        System.out.println(responseJson);
        System.out.println();
        throw e;
      }
      return result;

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Quotes the specified text as a quoted string for a CSV value or header.
   *
   * @param text The text to be quoted.
   * @return The quoted text.
   */
  protected String csvQuote(String text) {
    if (text.indexOf("\"") < 0 && text.indexOf("\\") < 0) {
      return "\"" + text + "\"";
    }
    char[] textChars = text.toCharArray();
    StringBuilder sb = new StringBuilder(text.length() * 2);
    for (char c : textChars) {
      if (c == '"' || c == '\\') {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb.toString();
  }

  /**
   * Creates a CSV temp file with the specified headers and records.
   *
   * @param filePrefix The prefix for the temp file name.
   * @param headers    The CSV headers.
   * @param records    The one or more records.
   * @return The {@link File} that was created.
   */
  protected File prepareCSVFile(String filePrefix,
                                String[] headers,
                                String[]... records) {
    // check the arguments
    int count = headers.length;
    for (int index = 0; index < records.length; index++) {
      String[] record = records[index];
      if (record.length != count) {
        throw new IllegalArgumentException(
            "The header and records do not all have the same number of "
                + "elements.  expected=[ " + count + " ], received=[ "
                + record.length + " ], index=[ " + index + " ]");
      }
    }

    try {
      File csvFile = File.createTempFile(filePrefix, ".csv");

      // populate the file as a CSV
      try (FileOutputStream fos = new FileOutputStream(csvFile);
           OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
           PrintWriter pw = new PrintWriter(osw)) {
        String prefix = "";
        for (String header : headers) {
          pw.print(prefix);
          pw.print(csvQuote(header));
          prefix = ",";
        }
        pw.println();
        pw.flush();

        for (String[] record : records) {
          prefix = "";
          for (String value : record) {
            pw.print(prefix);
            pw.print(csvQuote(value));
            prefix = ",";
          }
          pw.println();
          pw.flush();
        }
        pw.flush();

      }

      return csvFile;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Creates a JSON array temp file with the specified headers and records.
   *
   * @param filePrefix The prefix for the temp file name.
   * @param headers    The CSV headers.
   * @param records    The one or more records.
   * @return The {@link File} that was created.
   */
  protected File prepareJsonArrayFile(String filePrefix,
                                      String[] headers,
                                      String[]... records) {
    // check the arguments
    int count = headers.length;
    for (int index = 0; index < records.length; index++) {
      String[] record = records[index];
      if (record.length != count) {
        throw new IllegalArgumentException(
            "The header and records do not all have the same number of "
                + "elements.  expected=[ " + count + " ], received=[ "
                + record.length + " ], index=[ " + index + " ]");
      }
    }

    try {
      File jsonFile = File.createTempFile(filePrefix, ".json");

      // populate the file with a JSON array
      try (FileOutputStream fos = new FileOutputStream(jsonFile);
           OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8")) {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        JsonObjectBuilder job = Json.createObjectBuilder();
        for (String[] record : records) {
          for (int index = 0; index < record.length; index++) {
            String key = headers[index];
            String value = record[index];
            job.add(key, value);
          }
          jab.add(job);
        }

        String jsonText = JsonUtilities.toJsonText(jab);
        osw.write(jsonText);
        osw.flush();
      }

      return jsonFile;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a JSON temp file with the specified headers and records.
   *
   * @param filePrefix The prefix for the temp file name.
   * @param headers    The CSV headers.
   * @param records    The one or more records.
   * @return The {@link File} that was created.
   */
  protected File prepareJsonFile(String filePrefix,
                                 String[] headers,
                                 String[]... records) {
    // check the arguments
    int count = headers.length;
    for (int index = 0; index < records.length; index++) {
      String[] record = records[index];
      if (record.length != count) {
        throw new IllegalArgumentException(
            "The header and records do not all have the same number of "
                + "elements.  expected=[ " + count + " ], received=[ "
                + record.length + " ], index=[ " + index + " ]");
      }
    }

    try {
      File jsonFile = File.createTempFile(filePrefix, ".json");

      // populate the file as one JSON record per line
      try (FileOutputStream fos = new FileOutputStream(jsonFile);
           OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
           PrintWriter pw = new PrintWriter(osw)) {
        for (String[] record : records) {
          JsonObjectBuilder job = Json.createObjectBuilder();
          for (int index = 0; index < record.length; index++) {
            String key = headers[index];
            String value = record[index];
            job.add(key, value);
          }
          String jsonText = JsonUtilities.toJsonText(job);
          pw.println(jsonText);
          pw.flush();
        }
        pw.flush();
      }

      return jsonFile;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a JSON-lines temp file with the specified headers and records.
   *
   * @param filePrefix The prefix for the temp file name.
   * @param jsonArray The {@link JsonArray} describing the records.
   * @return The {@link File} that was created.
   */
  protected File prepareJsonArrayFile(String filePrefix, JsonArray jsonArray) {
    try {
      File jsonFile = File.createTempFile(filePrefix, ".json");

      // populate the file as one JSON record per line
      try (FileOutputStream fos = new FileOutputStream(jsonFile);
           OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
           PrintWriter pw = new PrintWriter(osw))
      {
        String jsonText = JsonUtilities.toJsonText(jsonArray, true);
        pw.println(jsonText);
        pw.flush();
      }

      return jsonFile;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a JSON-lines temp file with the specified headers and records.
   *
   * @param filePrefix The prefix for the temp file name.
   * @param jsonArray The {@link JsonArray} describing the records.
   * @return The {@link File} that was created.
   */
  protected File prepareJsonFile(String filePrefix, JsonArray jsonArray) {
    try {
      File jsonFile = File.createTempFile(filePrefix, ".json");

      // populate the file as one JSON record per line
      try (FileOutputStream fos = new FileOutputStream(jsonFile);
           OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
           PrintWriter pw = new PrintWriter(osw)) {
        for (JsonObject record: jsonArray.getValuesAs(JsonObject.class)) {
          String jsonText = JsonUtilities.toJsonText(record);
          pw.println(jsonText);
          pw.flush();
        }
        pw.flush();
      }

      return jsonFile;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the contents of the JSON init file as a {@link String}.
   *
   * @return The contents of the JSON init file as a {@link String}.
   */
  protected String readInitJsonFile() {
    try {
      File    repoDir       = this.getRepositoryDirectory();
      File    initJsonFile  = new File(repoDir, "g2-init.json");

      return readTextFileAsString(initJsonFile, "UTF-8");

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String formatTestInfo(String uriText, String bodyContent)
  {
    return "uriText=[ " + uriText + " ], bodyContent=[ " + bodyContent + " ]";
  }

  /**
   * Generats option combos for the specified variants.
   *
   * @param variants
   */
  protected static List<List> generateCombinations(List... variants) {
    // determine the total number of combinations
    int comboCount = 1;
    for (List v : variants) {
      comboCount *= v.size();
    }

    // determine the intervals for each variant
    List<Integer> intervals = new ArrayList<>(variants.length);
    // iterate over the variants
    for (int index = 0; index < variants.length; index++) {
      // default the interval count to one (1)
      int intervalCount = 1;

      // loop over the remaining variants after the current
      for (int index2 = index+1; index2 < variants.length; index2++) {
        // multiply the interval count by the remaining variant sizes
        intervalCount *= variants[index2].size();
      }

      // add the interval count
      intervals.add(intervalCount);
    }

    ArrayList<List> optionCombos = new ArrayList<>(comboCount);
    for (int comboIndex = 0; comboIndex < comboCount; comboIndex++) {
      List optionCombo = new ArrayList<>(variants.length);

      for (int index = 0; index < variants.length; index++) {
        int interval = intervals.get(index);
        int valueIndex = (comboIndex / interval) % variants[index].size();
        optionCombo.add(variants[index].get(valueIndex));
      }

      optionCombos.add(optionCombo);
    }

    return optionCombos;
  }

  /**
   * Converts the source object to JSON and then parses the JSON to the
   * specified target class.
   *
   * @param source The source object to copy.
   * @param targetClass The target class to parse the JSON as.
   * @return The target object that was parsed.
   */
  public static <S, T> T jsonCopy(S source, Class<T> targetClass) {
    String jsonText = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JodaModule());
      StringWriter sw = new StringWriter();
      objectMapper.writeValue(sw, source);
      jsonText = sw.toString();
      return objectMapper.readValue(jsonText, targetClass);

    } catch (RuntimeException e) {
      if (jsonText != null) {
        System.err.println("JSON TEXT: " + jsonText);
        e.printStackTrace();
//        try {
//          File current = new File(System.getProperty("user.dir"));
//          File target  = new File(current, "target");
//          File file = File.createTempFile("error-", ".txt", target);
//          try (FileOutputStream   fos = new FileOutputStream(file);
//          OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8);
//               PrintWriter        pw  = new PrintWriter(osw))
//          {
//            JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonText);
//            pw.println(JsonUtilities.toJsonText(jsonObject, true));
//            pw.println();
//            e.printStackTrace(pw);
//            pw.flush();
//          }
//        } catch (IOException exception) {
//          // ignore
//        }
      }
      throw e;
    } catch (Exception e) {
      if (jsonText != null) {
        System.err.println("JSON TEXT: " + jsonText);
        e.printStackTrace();
//        try {
//          File current = new File(System.getProperty("user.dir"));
//          File target  = new File(current, "target");
//          File file = File.createTempFile("error-", ".txt", target);
//          try (FileOutputStream   fos = new FileOutputStream(file);
//               OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8);
//               PrintWriter        pw  = new PrintWriter(osw))
//          {
//            JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonText);
//            pw.println(JsonUtilities.toJsonText(jsonObject, true));
//            pw.println();
//            e.printStackTrace(pw);
//            pw.flush();
//          }
//        } catch (IOException exception) {
//          // ignore
//        }
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts each source object in the specified {@link Collection} to JSON and
   * then parses the JSON to the specified target class and populates the
   * target collection.
   *
   * @param source The {@link Collection} of source objects to copy.
   * @param targetClass The target class to parse the JSON as.
   * @return The {@link Collection} containing target objects that were parsed.
   */
  public static <S, T, C extends Collection<T>> C jsonCopy(
      Collection<S> source,
      Class<T>      targetClass,
      Class<C>      collectionClass)
  {
    try {
      C target = collectionClass.newInstance();

      source.forEach(s -> {
        T t = jsonCopy(s, targetClass);
        target.add(t);
      });

      return target;

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts each source object in the specified {@link Map} to JSON and
   * then parses the JSON to the specified target class and populates the
   * target {@link Map}.
   *
   * @param source The {@link Map} of source objects to copy.
   * @param targetClass The target class to parse the JSON as.
   * @return The {@link Map} containing the target objects that were parsed.
   */
  public static <S, T, K> Map<K, T> jsonCopy(
      Map<K, S>     source,
      Class<T>      targetClass)
  {
    try {
      Map<K, T> target = new LinkedHashMap<K, T>();

      source.entrySet().forEach(entry -> {
        K key = entry.getKey();
        S s   = entry.getValue();
        T t   = jsonCopy(s, targetClass);
        target.put(key, t);
      });

      return target;

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse JSON text as the specified target class.
   * @param jsonText The JSON text to parse.
   * @param targetClass The target class to parse to.
   */
  public static <T> T jsonParse(String jsonText, Class<T> targetClass) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JodaModule());
      return objectMapper.readValue(jsonText, targetClass);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Serves as a method source for a test method with a withRaw parameter.
   *
   * @return The list of arguments for the boolean withRaw options.
   */
  protected List<Arguments> getWithRawVariants() {
    List<Arguments> result = new LinkedList<>();
    Boolean[] booleanVariants = {null, true, false};
    for (Boolean withRaw: booleanVariants) {
      Object[] argArray = new Object[1];
      argArray[0] = withRaw;
      result.add(arguments(argArray));
    }
    return result;
  }

  /**
   * Gets the variant possible values of booleans for the specified
   * parameter count.  This includes <tt>null</tt> values.
   *
   * @param paramCount The number of boolean parameters.
   * @return The {@link List} of parameter value lists.
   */
  protected static List<List<Boolean>> getBooleanVariants(int paramCount) {
    Boolean[] booleanValues = { null, true, false };
    int variantCount = (int) Math.ceil(Math.pow(3, paramCount));
    List<List<Boolean>> variants = new ArrayList<>(variantCount);

    // iterate over the variants
    for (int index1 = 0; index1 < variantCount; index1++) {
      // create the parameter list
      List<Boolean> params = new ArrayList<>(paramCount);

      // iterate over the parameter slots
      for (int index2 = 0; index2 < paramCount; index2++) {
        int repeat = (int) Math.ceil(Math.pow(3, index2));
        int valueIndex = ( index1 / repeat ) % booleanValues.length;
         params.add(booleanValues[valueIndex]);
      }

      // add the combinatorial variant
      variants.add(params);
    }
    return variants;
  }

  /**
   * Utility class for circular iteration.
   */
  private static class CircularIterator<T> implements Iterator<T> {
    private Collection<T> collection = null;
    private Iterator<T> iterator = null;
    private CircularIterator(Collection<T> collection) {
      this.collection = collection;
      this.iterator = this.collection.iterator();
    }
    public boolean hasNext() {
      return (this.collection.size() > 0);
    }
    public T next() {
      if (!this.iterator.hasNext()) {
        this.iterator = this.collection.iterator();
      }
      return this.iterator.next();
    }
    public void remove() {
      throw new UnsupportedOperationException(
          "Cannot remove from a circular iterator.");
    }
  }

  /**
   * Returns an iterator that iterates over the specified {@link Collection}
   * in a circular fashion.
   *
   * @param collection The {@link Collection} to iterate over.
   * @return The circular {@link Iterator}.
   */
  protected static <T> Iterator<T> circularIterator(Collection<T> collection) {
    return new CircularIterator<>(collection);
  }
}
