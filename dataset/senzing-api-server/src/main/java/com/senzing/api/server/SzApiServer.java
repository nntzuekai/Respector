package com.senzing.api.server;

import java.io.*;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.senzing.api.BuildInfo;
import com.senzing.api.server.mq.SzMessagingEndpoint;
import com.senzing.api.server.mq.SzMessagingEndpointFactory;
import com.senzing.api.services.SzMessageSink;
import com.senzing.api.model.SzVersionInfo;
import com.senzing.api.websocket.WebSocketFilter;
import com.senzing.cmdline.*;
import com.senzing.nativeapi.EngineStatsLoggingHandler;
import com.senzing.nativeapi.NativeApiFactory;
import com.senzing.api.services.SzApiProvider;
import com.senzing.api.model.SzLicenseInfo;
import com.senzing.configmgr.ConfigurationManager;
import com.senzing.g2.engine.*;
import com.senzing.repomgr.RepositoryManager;
import com.senzing.util.JsonUtilities;
import com.senzing.util.LoggingUtilities;
import com.senzing.util.WorkerThreadPool;
import com.senzing.util.AccessToken;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;

import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.json.*;
import javax.servlet.DispatcherType;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;

import static com.senzing.api.server.SzApiServerOption.*;
import static com.senzing.util.WorkerThreadPool.Task;
import static com.senzing.cmdline.CommandLineUtilities.*;
import static com.senzing.util.LoggingUtilities.*;
import static com.senzing.api.server.SzApiServerConstants.*;
import static com.senzing.cmdline.CommandLineSource.*;
import static javax.ws.rs.core.MediaType.*;

/**
 * Implements the Senzing REST API specification in Java in an HTTP server.
 *
 */
public class SzApiServer implements SzApiProvider {
  /**
   * The description of the server: {@value}.
   */
  public static final String SERVER_DESCRIPTION = "Senzing REST API Server";

  /**
   * The resource file for the service endpoint classes.  The value of this is
   * {@value}.
   */
  public static final String SERVICES_RESOURCE_FILE
      = "service-endpoints.properties";

  /**
   * The resource file for the Web Socket endpoint classes.  The value of this
   * is {@value}.
   */
  public static final String WEB_SOCKETS_RESOURCE_FILE
      = "web-socket-endpoints.properties";

  /**
   * The GZIP inflate buffer size.
   */
  private static final int GZIP_INFLATE_BUFFER_SIZE = 8192;

  /**
   * The HTTP output buffer size for HTTP connections.
   */
  private static final int HTTP_OUTPUT_BUFFER_SIZE = 128 * 1024;

  /**
   * The HTTP output aggregation size for HTTP connections.
   */
  private static final int HTTP_OUTPUT_AGGREGATION_SIZE
      = HTTP_OUTPUT_BUFFER_SIZE / 4;

  /**
   * The HTTP request header size for HTTP connections.
   */
  private static final int HTTP_REQUEST_HEADER_SIZE = 32 * 1024;

  /**
   * The HTTP response header size for HTTP connections.
   */
  private static final int HTTP_RESPONSE_HEADER_SIZE = 32 * 1024;

  /**
   * The HTTP header cache size for HTTP connections.
   */
  private static final int HTTP_HEADER_CACHE_SIZE = 4 * 1024;

  /**
   * The period to avoid over-logging of the diagnostics.
   */
  private static final long DIAGNOSTIC_PERIOD = (1000L * 60L * 45L);

  /**
   * Set the timeout for web socket reads.
   */
  public static final long WEB_SOCKET_TIMEOUT = 1000 * 60 * 60 * 24;

  /**
   * The date-time pattern for the build number.
   */
  private static final String BUILD_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss z";

  /**
   * The time zone used for the time component of the build number.
   */
  private static final ZoneId BUILD_ZONE = ZoneId.of("America/Los_Angeles");

  /**
   * The {@link DateTimeFormatter} for interpretting the build number as a
   * LocalDateTime instance.
   */
  private static final DateTimeFormatter BUILD_DATE_FORMATTER
      = DateTimeFormatter.ofPattern(BUILD_DATE_PATTERN).withZone(BUILD_ZONE);

  /**
   * The number of milliseconds to provide advance warning of an expiring
   * license.
   */
  private static final long EXPIRATION_WARNING_PERIOD
      = 1000L * 60L * 60L * 24L * 30L;

  private static final String JAR_FILE_NAME = getJarName(SzApiServer.class);

  private static final String JAR_BASE_URL = getJarBaseUrl(SzApiServer.class);

  private static SzApiServer INSTANCE = null;

  private static AccessToken PROVIDER_TOKEN = null;

  private enum ConfigType {
    FILE_PATH(false, true),
    MANAGED(true, false),
    BOTH(true, true);

    private boolean managed;
    private boolean filePath;

    ConfigType(boolean managed, boolean filePath) {
      this.managed = managed;
      this.filePath = filePath;
    }

    public boolean isManaged() {
      return this.managed;
    }

    public boolean isFilePath() {
      return this.filePath;
    }
  }

  public static synchronized SzApiServer getInstance() {
    if (INSTANCE == null) {
      throw new IllegalStateException("SzApiServer instance not initialized");
    }
    return INSTANCE;
  }

  /**
   * The access token to use call privileged functions on the server.
   * This may be <tt>null</tt> if no privileged functions may be performed.
   */
  protected AccessToken accessToken = null;

  /**
   * The HTTP port for the server.
   */
  protected Integer httpPort;

  /**
   * The URL base path.
   */
  protected String basePath = "/";

  /**
   * The {@link InetAddress} for the server.
   */
  protected InetAddress ipAddr;

  /**
   * The HTTPS port.
   */
  protected Integer httpsPort;

  /**
   * The key store file for the server key.
   */
  protected File keyStoreFile;

  /**
   * The password for the key store file.
   */
  protected String keyStorePassword;

  /**
   * The alias for the server key or <tt>null</tt> if there is only one key
   * in the key store file.
   */
  protected String keyAlias;

  /**
   * The client key store file for SSL client authentication.
   */
  protected File clientKeyStoreFile;

  /**
   * The password for the client key store file for SSL client authentication.
   */
  protected String clientKeyStorePassword;

  /**
   * The concurrency for the engine (e.g.: the number of threads in the
   * engine thread pool).
   */
  protected int concurrency;

  /**
   * The maximum number of threads for the HTTP server thread pool.
   */
  protected int httpConcurrency;

  /**
   * The {@link G2Config} config API.
   */
  protected G2Config configApi;

  /**
   * The {@link G2Product} product API.
   */
  protected G2Product productApi;

  /**
   * The {@link G2Engine} engine API.
   */
  protected G2Engine engineApi;

  /**
   * The {@link G2Diagnostic} diagnostics API.
   */
  protected G2Diagnostic diagnosticApi;

  /**
   * The {@link SzMessagingEndpoint} to use for asynchronous info messages.
   */
  protected SzMessagingEndpoint infoEndpoint;

  /**
   * The {@link G2EngineRetryHandler} backing the proxied
   * retry version of {@link G2Engine}.
   */
  protected G2EngineRetryHandler engineRetryHandler = null;

  /**
   * The {@link G2Engine} engine API instance wrapper that will automatically
   * retry some methods if the configuration is stale.
   */
  protected G2Engine retryEngineApi = null;

  /**
   * The {@link G2ConfigMgr} configuration manager API.
   */
  protected G2ConfigMgr configMgrApi;

  /**
   * The set of configured data sources.
   */
  protected Set<String> dataSources;

  /**
   * The {@link Map} of FTYPE_CODE values to ATTR_CLASS values from the config.
   */
  protected Map<String, String> featureToAttrClassMap;

  /**
   * The {@link Map} of ATTR_CODE values to ATTR_CLASS values from the config.
   */
  protected Map<String, String> attrCodeToAttrClassMap;

  /**
   * The Jetty Server.
   */
  protected Server jettyServer;

  /**
   * The {@link FileMonitor} thread that monitors when to shutdown.
   */
  protected FileMonitor fileMonitor;

  /**
   * The module name for initializing the API.
   */
  protected String moduleName;

  /**
   * Whether or not the API should be initialized in verbose mode.
   */
  protected boolean verbose;

  /**
   * Whether or not the API server should reduce the number of feedback
   * messages it sends to standard out.
   */
  protected boolean quiet;

  /**
   * The INI {@link File} for initializing the API.
   */
  protected File iniFile;

  /**
   * The JSON describing the initialization parameters.
   */
  protected JsonObject initJson;

  /**
   * The forced config ID to use for initialiaation -- this disables the
   * ability to detect config changes and reload.
   */
  protected Long configId = null;

  /**
   * The period for checking and automatically refreshing the active
   * configuration if necessary.
   */
  protected Long autoRefreshPeriod = null;

  /**
   * Indicates where the config resides.
   */
  protected ConfigType configType;

  /**
   * Indicates if only read operations should be allowed.
   */
  protected boolean readOnly;

  /**
   * Indicates if admin operations are enabled.
   */
  protected boolean adminEnabled;

  /**
   * The minimum period of time between logging of stats.  If this is zero (0)
   * then stats will not be logged.  The time is a minimum because stats will
   * not be logged if no operations are taking place within the process that are
   * affecting entity scoring.
   */
  protected long statsInterval = DEFAULT_STATS_INTERVAL;

  /**
   * Flag indicating if the performance check should be skipped on startup.
   */
  protected boolean skipStartupPerf = false;

  /**
   * CORS Access-Control-Allow-Origin for all endpoints on the server.
   */
  protected String allowedOrigins;

  /**
   * The {@link WorkerThreadPool} for executing Senzing API calls.
   */
  protected WorkerThreadPool workerThreadPool;

  /**
   * The {@link Set} of {@link AccessToken} instances for authorized
   * prolonged operations.
   */
  protected final Set<AccessToken> prolongedAuthSet = new LinkedHashSet<>();

  /**
   * The {@link Reinitializer} to periodically check if the configuration
   * has changed.
   */
  protected Reinitializer reinitializer = null;

  /**
   * The monitor object to use while waiting for the server to shutdown.
   */
  protected final Object joinMonitor = new Object();

  /**
   * The monitor object to use to synchronize for data dependent on
   * reinitialization.
   */
  protected final Object reinitMonitor = new Object();

  /**
   * Flag indicating if the server has been shutdown.
   */
  protected boolean completed = false;

  /**
   * The timestamp from the last diagnostics run.
   */
  protected long lastDiagnosticsRun = -1L;

  /**
   * The {@link SzVersionInfo} describing the version.
   */
  protected SzVersionInfo versionInfo = null;

  /**
   * The read-write lock to use to prevent simultaneous request handling and
   * purging.
   */
  protected final ReadWriteLock purgeLock = new ReentrantReadWriteLock();

  /**
   * The {@link Map} of Web Socket implementation classes to the {@link String}
   * path endpoints.
   */
  protected Map<Class, String> webSocketClasses = null;

  /**
   * A general purpose object to use for synchronized locks.
   */
  protected final Object monitor = new Object();

  /**
   * The {@link ServletContextHandler} for the Jetty server.
   */
  protected ServletContextHandler servletContext = null;

  /**
   * Returns the HTTP port for this instance.
   *
   * @return The HTTP port for this instance.
   */
  public int getHttpPort() {
    this.assertNotShutdown();
    return this.httpPort;
  }

  /**
   * Returns the {@link InetAddress} for the IP address interface to which
   * the server is bound.
   *
   * @return The {@link InetAddress} for the IP address interface to which
   * the server is bound.
   */
  public InetAddress getIPAddress() {
    this.assertNotShutdown();
    return this.ipAddr;
  }

  /**
   * Returns the configured API module name.
   *
   * @return The configured API module name.
   */
  public String getApiModuleName() {
    this.assertNotShutdown();
    return this.moduleName;
  }

  /**
   * Returns the configured verbose flag for the API initialization.
   *
   * @return The configured verbose flag for the API initialization.
   */
  public boolean isApiVerbose() {
    this.assertNotShutdown();
    return this.verbose;
  }

  /**
   * Implemented to return the name of the server given by
   * {@link #SERVER_DESCRIPTION}.
   *
   * @return The description of the server.
   */
  @Override
  public String getDescription() {
    return SERVER_DESCRIPTION;
  }

  /**
   * Returns the initialized {@link G2Product} API interface.
   *
   * @return The initialized {@link G2Product} API interface.
   */
  @Override
  public G2Product getProductApi() {
    this.assertNotShutdown();
    return this.productApi;
  }

  /**
   * Returns the initialized {@link G2Config} API interface.
   *
   * @return The initialized {@link G2Config} API interface.
   */
  @Override
  public G2Config getConfigApi() {
    this.assertNotShutdown();
    return this.configApi;
  }

  /**
   * Returns the initialized {@link G2Engine} API interface.
   *
   * @return The initialized {@link G2Engine} API interface.
   */
  @Override
  public G2Engine getEngineApi() {
    this.assertNotShutdown();
    return (this.retryEngineApi == null)
        ? this.engineApi : this.retryEngineApi;
  }

  /**
   * Returns the initialized {@link G2ConfigMgr} API interface, or <tt>null</tt>
   * if the configuration is not automatically being picked up as the current
   * default configuration.
   *
   * @return The {@link G2ConfigMgr} API interface, or <tt>null</tt> if the
   * configuration is not automatically being picked up as the current
   * default configuration.
   */
  @Override
  public G2ConfigMgr getConfigMgrApi() {
    return this.configMgrApi;
  }

  /**
   * Returns the initialized {@link G2Diagnostic} API interface.
   *
   * @return The initialized {@link G2Diagnostic} API interface.
   */
  @Override
  public G2Diagnostic getDiagnosticApi() {
    this.assertNotShutdown();
    return this.diagnosticApi;
  }

  /**
   * Checks whether or not only read operations are being allowed.
   *
   * @return <tt>true</tt> if only read operations are allowed, and
   * <tt>false</tt> if all operations are being supported.
   */
  @Override
  public boolean isReadOnly() {
    this.assertNotShutdown();
    return this.readOnly;
  }

  /**
   * Checks whether or not admin operations are enabled.
   *
   * @return <tt>true</tt> if admin operations are enabled, and
   * <tt>false</tt> if admin operations are disabled.
   */
  @Override
  public boolean isAdminEnabled() {
    this.assertNotShutdown();
    return this.adminEnabled;
  }

  /**
   * Returns the minimum time interval for logging stats.  This is the minimum
   * period between logging of stats assuming the API Server is performing
   * operations that will affect stats (i.e.: activities pertaining to entity
   * scoring).  If the API Server is idle or active, but not performing entity
   * scoring activities then stats logging will be delayed until activities are
   * performed that will affect stats.  If the returned interval is zero (0)
   * then stats logging will be suppressed.
   *
   * @return The interval for logging stats, or zero (0) if stats logging is
   * suppressed.
   */
  public long getStatsInterval() {
    return this.statsInterval;
  }

  /**
   * Checks if we are skipping the performance check on startup.
   *
   * @return <tt>true</tt> if skipping the performance check, and
   * <tt>false</tt> otherwise.
   */
  public boolean isSkippingStartupPerformance() {
    return this.skipStartupPerf;
  }

  /**
   * Returns the number of worker threads initialized to do work against
   * the Senzing repository.
   *
   * @return The number of worker threads initialized to do work against
   * the Senzing repository.
   */
  @Override
  public int getConcurrency() {
    return this.workerThreadPool.size();
  }

  @Override
  public String getBasePath() {
    return this.basePath;
  }

  @Override
  public int getWebSocketsMessageMaxSize() {
    return WEB_SOCKETS_MESSAGE_MAX_SIZE;
  }

  @Override
  public boolean hasInfoSink() {
    return (this.infoEndpoint != null);
  }

  @Override
  public SzMessageSink acquireInfoSink() {
    return (this.infoEndpoint == null) ? null
        : this.infoEndpoint.acquireMessageSink();
  }

  @Override
  public void releaseInfoSink(SzMessageSink sink) {
    if (this.infoEndpoint == null) {
      throw new IllegalStateException(
          "No load message endpoint exists for releasing the sink");
    }
    this.infoEndpoint.releaseMessageSink(sink);
  }

  @Override
  public String getApiProviderVersion() {
    if (this.versionInfo == null) return null;
    return this.versionInfo.getApiServerVersion();
  }

  @Override
  public String getRestApiVersion() {
    if (this.versionInfo == null) return null;
    return this.versionInfo.getRestApiVersion();
  }

  @Override
  public String getNativeApiVersion() {
    if (this.versionInfo == null) return null;
    return this.versionInfo.getNativeApiVersion();
  }

  @Override
  public String getNativeApiBuildVersion() {
    if (this.versionInfo == null) return null;
    return this.versionInfo.getNativeApiBuildVersion();
  }

  @Override
  public String getNativeApiBuildNumber() {
    if (this.versionInfo == null) return null;
    return this.versionInfo.getNativeApiBuildNumber();
  }

  @Override
  public Date getNativeApiBuildDate() {
    if (this.versionInfo == null) return null;
    return this.versionInfo.getNativeApiBuildDate();
  }

  @Override
  public String getConfigCompatibilityVersion() {
    if (this.versionInfo == null) return null;
    return this.versionInfo.getConfigCompatibilityVersion();
  }

  /**
   * Evaluates the configuration and populates the {@link Set} of
   * data sources and maps mapping f-type code to attribute class and
   * attribute code to attribute class.
   *
   * @param config       The {@link JsonObject} describing the config.
   * @param dataSources  The {@link Set} of data sources to populate.
   * @param ftypeCodeMap The {@link Map} of f-type codes to attribute classes
   *                     to populate.
   * @param attrCodeMap  The {@link Map} of attribute code to attribute classes
   *                     to populate.
   */
  private static void evaluateConfig(JsonObject config,
                                     Set<String> dataSources,
                                     Map<String, String> ftypeCodeMap,
                                     Map<String, String> attrCodeMap) {
    // get the data sources from the config
    JsonValue jsonValue = config.getValue("/G2_CONFIG/CFG_DSRC");
    JsonArray jsonArray = jsonValue.asJsonArray();

    for (JsonValue val : jsonArray) {
      JsonObject dataSource = val.asJsonObject();
      String dsrcCode = dataSource.getString("DSRC_CODE").toUpperCase();
      dataSources.add(dsrcCode);
    }

    // get the attribute types from the config
    jsonValue = config.getValue("/G2_CONFIG/CFG_ATTR");
    jsonArray = jsonValue.asJsonArray();

    for (JsonValue val : jsonArray) {
      JsonObject cfgAttr = val.asJsonObject();
      String attrCode = cfgAttr.getString("ATTR_CODE").toUpperCase();
      String ftypeCode = cfgAttr.getString("FTYPE_CODE").toUpperCase();
      String attrClass = cfgAttr.getString("ATTR_CLASS").toUpperCase();

      String ac = attrCodeMap.get(attrCode);
      if (ac != null && !ac.equals(attrClass)) {
        System.err.println(
            "*** WARNING : Multiple attribute classes for ATTR_CODE: "
                + attrCode + " ( " + ac + " / " + attrClass + " )");
      } else {
        attrCodeMap.put(attrCode, attrClass);
      }

      ac = ftypeCodeMap.get(ftypeCode);
      if (ac != null && !ac.equals(attrClass)) {
        System.err.println(
            "*** WARNING : Multiple attribute classes for FTYPE_CODE: "
                + ftypeCode + " ( " + ac + " / " + attrClass + " )");
      } else {
        ftypeCodeMap.put(ftypeCode, attrClass);
      }
    }
  }

  /**
   * Returns the unmodifiable {@link Set} of configured data source codes.
   *
   * @param expectedDataSources The zero or more data source codes that the
   *                            caller expects to exist.
   * @return The unmodifiable {@link Set} of configured data source codes.
   */
  public Set<String> getDataSources(String... expectedDataSources) {
    synchronized (this.reinitMonitor) {
      this.assertNotShutdown();
      for (String dataSource : expectedDataSources) {
        if (!this.dataSources.contains(dataSource)) {
          this.ensureConfigCurrent(false);
          break;
        }
      }
      return this.dataSources;
    }
  }

  /**
   * Returns the attribute class (<tt>ATTR_CLASS</tt>) associated with the
   * specified feature name (<tt>FTYPE_CODE</tt>).
   *
   * @param featureName The feature name from the configuration to lookup the
   *                    attribute class.
   * @return The attribute class associated with the specified f-type code.
   */
  public String getAttributeClassForFeature(String featureName) {
    synchronized (this.reinitMonitor) {
      this.assertNotShutdown();
      if (!this.featureToAttrClassMap.containsKey(featureName)) {
        this.ensureConfigCurrent(false);
      }
      return this.featureToAttrClassMap.get(featureName);
    }
  }

  /**
   * Returns the attribute class (<tt>ATTR_CLASS</tt>) associated with the
   * specified attribute code (<tt>ATTR_CODE</tt>).
   *
   * @param attrCode The attribute code from the configuration to lookup the
   *                 attribute class.
   * @return The attribute class associated with the specified attribute code.
   */
  public String getAttributeClassForAttributeCode(String attrCode) {
    synchronized (this.reinitMonitor) {
      this.assertNotShutdown();
      if (!this.attrCodeToAttrClassMap.containsKey(attrCode)) {
        this.ensureConfigCurrent(false);
      }
      return this.attrCodeToAttrClassMap.get(attrCode);
    }
  }

  /**
   *
   */
  private static String buildErrorMessage(String heading,
                                          Integer errorCode,
                                          String errorMessage) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    if (heading != null) pw.println(heading);
    if (errorCode != null) {
      pw.println("ERROR CODE: " + errorCode);
      pw.println();
    }
    if (errorMessage != null) {
      pw.println(errorMessage);
      pw.println();
    }
    pw.flush();
    return sw.toString();
  }

  /**
   *
   */
  private static void exitOnError(Throwable t) {
    System.err.println(t.getMessage());
    System.exit(1);
  }

  /**
   * Parses the {@link SzApiServer} command line arguments and produces a
   * {@link Map} of {@link CommandLineOption} keys to {@link Object} command
   * line values.
   *
   * @param args The arguments to parse.
   * @return The {@link Map} describing the command-line arguments.
   */
  protected static Map<CommandLineOption, Object> parseCommandLine(
      String[] args, List<DeprecatedOptionWarning> deprecationWarnings)
    throws CommandLineException
  {
    Map<CommandLineOption, CommandLineValue> optionValues
        = CommandLineUtilities.parseCommandLine(
            SzApiServerOption.class,
            args,
            SzApiServerOption.PARAMETER_PROCESSOR,
            deprecationWarnings);

    // iterate over the option values and handle them
    JsonObjectBuilder job = Json.createObjectBuilder();
    job.add("message", "Startup Options");

    StringBuilder sb = new StringBuilder();

    Map<CommandLineOption, Object> result = new LinkedHashMap<>();

    CommandLineUtilities.processCommandLine(optionValues, result, job, sb);

    // log the options
    if (!optionValues.containsKey(HELP) && !optionValues.containsKey(VERSION)) {
      System.out.println(
          "[" + (new Date()) + "] Senzing API Server: " + sb.toString());
    }

    // check the ports
    CommandLineValue httpPortValue = optionValues.get(HTTP_PORT);
    CommandLineValue keyStoreValue = optionValues.get(KEY_STORE);
    if (httpPortValue.getSource() == DEFAULT && keyStoreValue != null) {
      result.remove(HTTP_PORT);
    }

    // return the result
    return result;
  }

  /**
   * Returns a formatted string describing the version details of the
   * API Server.
   *
   * @return A formatted string describing the version details.
   */
  protected static String getVersionString() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    printJarVersion(pw);
    printSenzingVersions(pw);
    return sw.toString();
  }

  /**
   * Prints the JAR version header for the version string to the specified
   * {@link PrintWriter}.
   *
   * @param pw The {@link PrintWriter} to print the version information to.
   */
  protected static void printJarVersion(PrintWriter pw) {
    pw.println("[ " + JAR_FILE_NAME + " version "
                   + BuildInfo.MAVEN_VERSION + " ]");
  }

  /**
   * Prints the Senzing version information to the specified {@link
   * PrintWriter}.
   *
   * @param pw The {@link PrintWriter} to print the version information to.
   */
  protected static void printSenzingVersions(PrintWriter pw) {
    // use G2Product API without "init()" for now
    G2Product productApi = NativeApiFactory.createProductApi();
    String jsonText = productApi.version();
    JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonText);
    SzVersionInfo info = SzVersionInfo.parseVersionInfo(null, jsonObject);

    String formattedBuildDate
        = BUILD_DATE_FORMATTER.format(
        Instant.ofEpochMilli(info.getNativeApiBuildDate().getTime()));

    pw.println(" - REST API Server Version      : " + info.getApiServerVersion());
    pw.println(" - REST Specification Version   : " + info.getRestApiVersion());
    pw.println(" - Senzing Native API Version   : " + info.getNativeApiVersion());
    pw.println(" - Senzing Native Build Version : " + info.getNativeApiBuildVersion());
    pw.println(" - Senzing Native Build Number  : " + info.getNativeApiBuildNumber());
    pw.println(" - Senzing Native Build Date    : " + formattedBuildDate);
    pw.println(" - Config Compatibility Version : " + info.getConfigCompatibilityVersion());
    pw.flush();
  }

  /**
   * Prints the introduction to the usage message to the specified {@link
   * PrintWriter}.
   *
   * @param pw The {@link PrintWriter} to write the usage introduction to.
   */
  protected static void printUsageIntro(PrintWriter pw) {
    pw.println(multilineFormat(
        "java -jar " + JAR_FILE_NAME + " <options>",
        "",
        "<options> includes: ",
        ""));
  }

  /**
   * Prints the usage for the standard options to the specified {@link
   * PrintWriter}.
   *
   * @param pw The {@link PrintWriter} to write the standard options usage.
   */
  protected static void printStandardOptionsUsage(PrintWriter pw) {
    pw.println(multilineFormat(
        "[ Standard Options ]",
        "   --help",
        "        Also -help.  Should be the first and only option if provided.",
        "        Causes this help message to be displayed.",
        "        NOTE: If this option is provided, the server will not start.",
        "",
        "   --version",
        "        Also -version.  Should be the first and only option if provided.",
        "        Causes the version of the G2 REST API Server to be displayed.",
        "        NOTE: If this option is provided, the server will not start.",
        "",
        "   --read-only [true|false]",
        "        Also -readOnly.  Disables functions that would modify the entity",
        "        repository data, causing those functions to return a 403 Forbidden",
        "        response.  The true/false parameter is optional, if not specified",
        "        then true is assumed.  If specified as false then it is the same as",
        "        omitting the option with the exception that omission falls back to the",
        "        environment variable setting whereas an explicit false overrides any",
        "        environment variable.  NOTE: this option will not only disable loading",
        "        data to the entity repository, but will also disable modifications to",
        "        the configuration even if the --enable-admin option is provided.",
        "        --> VIA ENVIRONMENT: " + READ_ONLY.getEnvironmentVariable(),
        "",
        "   --enable-admin [true|false]",
        "        Also -enableAdmin.  Enables administrative functions via the API",
        "        server.  Administrative functions include those that would modify",
        "        the active configuration (e.g.: adding data sources, entity types,",
        "        or entity classes).  The true/false parameter is optional, if not",
        "        specified then true is assumed.  If specified as false then it is",
        "        the same as omitting the option with the exception that omission",
        "        falls back to the environment variable setting whereas an explicit",
        "        false overrides any environment variable.  If not specified then",
        "        administrative functions will return a 403 Forbidden response.",
        "        --> VIA ENVIRONMENT: " + ENABLE_ADMIN.getEnvironmentVariable(),
        "",
        "   --http-port <port-number>",
        "        Also -httpPort.  Sets the port for HTTP communication.  If not",
        "        specified, then the default port (" + DEFAULT_PORT + ") is used.",
        "        Specify 0 for a randomly selected available port number.  This",
        "        option cannot be specified if SSL client authentication is configured.",
        "        --> VIA ENVIRONMENT: " + HTTP_PORT.getEnvironmentVariable(),
        "",
        "   --bind-addr <ip-address|loopback|all>",
        "        Also -bindAddr.  Sets the bind address for HTTP communication.  If not",
        "        provided the bind address defaults to the loopback address.",
        "        --> VIA ENVIRONMENT: " + BIND_ADDRESS.getEnvironmentVariable(),
        "",
        "   --url-base-path <base-path>",
        "        Also -urlBasePath.  Sets the URL base path for the API Server.",
        "        --> VIA ENVIRONMENT: " + URL_BASE_PATH.getEnvironmentVariable(),
        "",
        "   --allowed-origins <url-domain>",
        "        Also -allowedOrigins.  Sets the CORS Access-Control-Allow-Origin header",
        "        for all endpoints.  There is no default value.  If not specified then",
        "        the Access-Control-Allow-Origin is not included with responses.",
        "        --> VIA ENVIRONMENT: " + ALLOWED_ORIGINS.getEnvironmentVariable(),
        "",
        "   --concurrency <thread-count>",
        "        Also -concurrency.  Sets the number of threads available for executing ",
        "        Senzing API functions (i.e.: the number of engine threads).",
        "        If not specified, then this defaults to "
            + DEFAULT_CONCURRENCY + ".",
        "        --> VIA ENVIRONMENT: " + CONCURRENCY.getEnvironmentVariable(),
        "",
        "   --http-concurrency <thread-count>",
        "        Also -httpConcurrency.  Sets the maximum number of threads available",
        "        for the HTTP server.  The single parameter to this option should be",
        "        a positive integer.  If not specified, then this defaults to "
            + DEFAULT_HTTP_CONCURRENCY + ".  If",
        "        the specified thread count is less than " + MINIMUM_HTTP_CONCURRENCY
            + " then an error is reported",
        "        --> VIA ENVIRONMENT: " + HTTP_CONCURRENCY.getEnvironmentVariable(),
        "",
        "   --module-name <module-name>",
        "        Also -moduleName.  The module name to initialize with.  If not",
        "        specified, then the module name defaults to \""
            + DEFAULT_MODULE_NAME + "\".",
        "        --> VIA ENVIRONMENT: " + MODULE_NAME.getEnvironmentVariable(),
        "",
        "   --ini-file <ini-file-path>",
        "        Also -iniFile.  The path to the Senzing INI file to with which to",
        "        initialize.",
        "        EXAMPLE: -iniFile /etc/opt/senzing/G2Module.ini",
        "        --> VIA ENVIRONMENT: " + INI_FILE.getEnvironmentVariable(),
        "",
        "   --init-file <json-init-file>",
        "        Also -initFile.  The path to the file containing the JSON text to",
        "        use for Senzing initialization.",
        "        EXAMPLE: -initFile ~/senzing/g2-init.json",
        "        --> VIA ENVIRONMENT: " + INIT_FILE.getEnvironmentVariable(),
        "",
        "   --init-env-var <environment-variable-name>",
        "        Also -initEnvVar.  The environment variable from which to extract",
        "        the JSON text to use for Senzing initialization.",
        "        *** SECURITY WARNING: If the JSON text contains a password",
        "        then it may be visible to other users via process monitoring.",
        "        EXAMPLE: -initEnvVar SENZING_INIT_JSON",
        "        --> VIA ENVIRONMENT: " + INIT_ENV_VAR.getEnvironmentVariable(),
        "",
        "   --init-json <json-init-text>",
        "        Also -initJson.  The JSON text to use for Senzing initialization.",
        "        *** SECURITY WARNING: If the JSON text contains a password",
        "        then it may be visible to other users via process monitoring.",
        "        EXAMPLE: -initJson \"{\"PIPELINE\":{ ... }}\"",
        "        --> VIA ENVIRONMENT: " + INIT_JSON.getEnvironmentVariable(),
        "                             "
            + INIT_JSON.getEnvironmentFallbacks().iterator().next()
            + " (fallback)",
        "",
        "   --config-id <config-id>",
        "        Also -configId.  Use with the -iniFile, -initFile, -initEnvVar or",
        "        -initJson options to force a specific configuration ID to use for",
        "        initialization.",
        "        --> VIA ENVIRONMENT: " + CONFIG_ID.getEnvironmentVariable(),
        "",
        "   --auto-refresh-period <positive-integer-seconds|0|negative-integer>",
        "        Also -autoRefreshPeriod.  If leveraging the default configuration",
        "        stored in the database, this is used to specify how often the API",
        "        server should background check that the current active config is the",
        "        same as the current default config, and if different reinitialize",
        "        with the current default config.  If zero is specified, then the",
        "        auto-refresh is disabled and it will only occur when a requested",
        "        configuration element is not found in the current active config.",
        "        Specifying a negative integer is allowed but is used to enable a",
        "        check and conditional refresh only when manually requested",
        "        (programmatically).  NOTE: This is option ignored if auto-refresh is",
        "        disabled because the config was specified via the G2CONFIGFILE init",
        "        option or if --config-id has been specified to lock to a specific",
        "        configuration.",
        "        --> VIA ENVIRONMENT: " + AUTO_REFRESH_PERIOD.getEnvironmentVariable(),
        "",
        "   --stats-interval <milliseconds>",
        "        Also -statsInterval.  The minimum number of milliseconds between",
        "        logging of stats.  This is minimum because stats logging is suppressed",
        "        if the API Server is idle or active but not performing activities",
        "        pertaining to entity scoring.  In such cases, stats logging is delayed",
        "        until an activity pertaining to entity scoring is performed.  By",
        "        default this is set to the millisecond equivalent of 15 minutes.  If",
        "        zero (0) is specified then the logging of stats will be suppressed.",
        "        --> VIA ENVIRONMENT: " + STATS_INTERVAL.getEnvironmentVariable(),
        "",
        "   --skip-startup-perf [true|false]",
        "        Also -skipStartupPerf.  If specified then the performance check on",
        "        startup is skipped.  The true/false parameter is optional, if not",
        "        specified then true is assumed.  If specified as false then it is the",
        "        same as omitting the option with the exception that omission falls back",
        "        to the environment variable setting whereas an explicit false overrides",
        "        any environment variable.",
        "        --> VIA ENVIRONMENT: " + SKIP_STARTUP_PERF.getEnvironmentVariable(),
        "",
        "   --skip-engine-priming [true|false]",
        "        Also -skipEnginePriming.  If specified then the API Server will not",
        "        prime the engine on startup.  The true/false parameter is optional, if",
        "        not specified then true is assumed.  If specified as false then it is",
        "        the same as omitting the option with the exception that omission falls",
        "        back to the environment variable setting whereas an explicit false",
        "        overrides any environment variable.",
        "        --> VIA ENVIRONMENT: " + SKIP_ENGINE_PRIMING.getEnvironmentVariable(),
        "",
        "   --verbose [true|false]",
        "        Also -verbose.  If specified then initialize in verbose mode.  The",
        "        true/false parameter is optional, if not specified then true is assumed.",
        "        If specified as false then it is the same as omitting the option with",
        "        the exception that omission falls back to the environment variable",
        "        setting whereas an explicit false overrides any environment variable.",
        "        --> VIA ENVIRONMENT: " + VERBOSE.getEnvironmentVariable(),
        "",
        "   --quiet [true|false]",
        "        Also -quiet.  If specified then the API server reduces the number of",
        "        messages provided as feedback to standard output.  This applies only to",
        "        messages generated by the API server and not by the underlying API",
        "        which can be quite prolific if --verbose is provided.  The true/false",
        "        parameter is optional, if not specified then true is assumed.  If",
        "        specified as false then it is the same as omitting the option with",
        "        the exception that omission falls back to the environment variable",
        "        setting whereas an explicit false overrides any environment variable.",
        "        --> VIA ENVIRONMENT: " + QUIET.getEnvironmentVariable(),
        "",
        "   --debug [true|false]",
        "        Also -debug.  If specified then debug logging is enabled.  The",
        "        true/false parameter is optional, if not specified then true is assumed.",
        "        If specified as false then it is the same as omitting the option with",
        "        the exception that omission falls back to the environment variable",
        "        setting whereas an explicit false overrides any environment variable.",
        "        --> VIA ENVIRONMENT: " + DEBUG_LOGGING.getEnvironmentVariable(),
        "",
        "   --monitor-file <file-path>",
        "        Also -monitorFile.  Specifies a file whose timestamp is monitored to",
        "        determine when to shutdown.",
        "        --> VIA ENVIRONMENT: " + MONITOR_FILE.getEnvironmentVariable(),
        ""));
  }

  /**
   * Prints the SSL-related options usage to the specified {@link PrintWriter}.
   *
   * @param pw The {@link PrintWriter} to write the info-queue options usage.
   */
  protected static void printSslOptionsUsage(PrintWriter pw) {
    pw.println(multilineFormat(
        "[ HTTPS / SSL Options ]",
        "   The following options pertain to HTTPS / SSL configuration.  The ",
        "   " + KEY_STORE.getCommandLineFlag() + " and "
            + KEY_STORE_PASSWORD.getCommandLineFlag() + " options are the minimum required",
        "   options to enable HTTPS / SSL communication.  If HTTPS / SSL communication",
        "   is enabled, then HTTP communication is disabled UNLESS the "
            + HTTP_PORT.getCommandLineFlag(),
        "   option is specified.  However, if client SSL authentication is configured",
        "   via the " + CLIENT_KEY_STORE.getCommandLineFlag() + " and "
            + CLIENT_KEY_STORE_PASSWORD.getCommandLineFlag() + " options then",
        "   enabling HTTP communication via the " + HTTP_PORT.getCommandLineFlag()
            + " option is prohibited.",
        "",
        "   --https-port <port-number>",
        "        Also -httpsPort.  Sets the port for secure HTTPS communication.",
        "        While the default HTTPS port is " + DEFAULT_SECURE_PORT + " if not specified,",
        "        HTTPS is only enabled if the " + KEY_STORE.getCommandLineFlag() + " option is specified.",
        "        Specify 0 for a randomly selected available port number.",
        "        --> VIA ENVIRONMENT: " + HTTPS_PORT.getEnvironmentVariable(),
        "",
        "   --key-store <path-to-pkcs12-keystore-file>",
        "        Also -keyStore.  Specifies the key store file that holds the private",
        "        key that the sever uses to identify itself for HTTPS communication.",
        "        --> VIA ENVIRONMENT: " + KEY_STORE.getEnvironmentVariable(),
        "",
        "   --key-store-password <password>",
        "        Also -keyStorePassword.  Specifies the password for decrypting the",
        "        key store file specified with the " + KEY_STORE.getCommandLineFlag() + " option.",
        "        --> VIA ENVIRONMENT: " + KEY_STORE_PASSWORD.getEnvironmentVariable(),
        "",
        "   --key-alias <server-key-alias>",
        "        Also -keyAlias.  Optionally specifies the alias for the private server",
        "        key in the specified key store.  If not specified, then the first key",
        "        found in the specified key store is used.",
        "        --> VIA ENVIRONMENT: " + KEY_ALIAS.getEnvironmentVariable(),
        "",
        "   --client-key-store <path-to-pkcs12-keystore-file>",
        "        Also -clientKeyStore.  Specifies the key store file that holds the",
        "        public keys of those clients that are authorized to connect.  If this",
        "        option is specified then SSL client authentication is required and",
        "        the " + HTTP_PORT.getCommandLineFlag() + " option is forbidden.",
        "        --> VIA ENVIRONMENT: " + CLIENT_KEY_STORE.getEnvironmentVariable(),
        "",
        "   --client-key-store-password <password>",
        "        Also -clientKeyStorePassword.  Specifies the password for decrypting",
        "        the key store file specified with the " + CLIENT_KEY_STORE.getCommandLineFlag() + " option.",
        "        --> VIA ENVIRONMENT: " + CLIENT_KEY_STORE_PASSWORD.getEnvironmentVariable(),
        ""));
  }

  /**
   * Prints the info-queue options usage to the specified {@link PrintWriter}.
   *
   * @param pw The {@link PrintWriter} to write the info-queue options usage.
   */
  protected static void printInfoQueueOptionsUsage(PrintWriter pw) {
    pw.println(multilineFormat(
        "[ Asynchronous Info Queue Options ]",
        "   The following options pertain to configuring an asynchronous message",
        "   queue on which to send \"info\" messages generated when records are",
        "   loaded, deleted or entities are re-evaluated.  At most one such queue",
        "   can be configured.  If an \"info\" queue is configured then every load,",
        "   delete and re-evaluate operation is performed with the variant to",
        "   generate an info message.  The info messages that are sent on the queue",
        "   (or topic) are the relevant \"raw data\" JSON segments.",
        "",
        "   --sqs-info-url <url>",
        "        Also -sqsInfoUrl.  Specifies an Amazon SQS queue URL as the info queue.",
        "        --> VIA ENVIRONMENT: " + SQS_INFO_URL.getEnvironmentVariable(),
        "",
        "   --rabbit-info-host <hostname>",
        "        Also -rabbitInfoHost.  Used to specify the hostname for connecting to",
        "        RabbitMQ as part of specifying a RabbitMQ info queue.",
        "        --> VIA ENVIRONMENT: " + RABBIT_INFO_HOST.getEnvironmentVariable(),
        "                             "
            + RABBIT_INFO_HOST.getEnvironmentFallbacks().iterator().next()
            + " (fallback)",
        "",
        "   --rabbit-info-port <port>",
        "        Also -rabbitInfoPort.  Used to specify the port number for connecting",
        "        to RabbitMQ as part of specifying a RabbitMQ info queue.",
        "        --> VIA ENVIRONMENT: " + RABBIT_INFO_PORT.getEnvironmentVariable(),
        "                             "
            + RABBIT_INFO_PORT.getEnvironmentFallbacks().iterator().next()
            + " (fallback)",
        "",
        "   --rabbit-info-user <user name>",
        "        Also -rabbitInfoUser.  Used to specify the user name for connecting to",
        "        RabbitMQ as part of specifying a RabbitMQ info queue.",
        "        --> VIA ENVIRONMENT: " + RABBIT_INFO_USER.getEnvironmentVariable(),
        "                             "
            + RABBIT_INFO_USER.getEnvironmentFallbacks().iterator().next()
            + " (fallback)",
        "",
        "   --rabbit-info-password <password>",
        "        Also -rabbitInfoPassword.  Used to specify the password for connecting",
        "        to RabbitMQ as part of specifying a RabbitMQ info queue.",
        "        --> VIA ENVIRONMENT: " + RABBIT_INFO_PASSWORD.getEnvironmentVariable(),
        "                             "
            + RABBIT_INFO_PASSWORD.getEnvironmentFallbacks().iterator().next()
            + " (fallback)",
        "",
        "   --rabbit-info-virtual-host <virtual host>",
        "        Also -rabbitInfoVirtualHost.  Used to specify the virtual host for",
        "        connecting to RabbitMQ as part of specifying a RabbitMQ info queue.",
        "        --> VIA ENVIRONMENT: " + RABBIT_INFO_VIRTUAL_HOST.getEnvironmentVariable(),
        "                             "
            + RABBIT_INFO_VIRTUAL_HOST.getEnvironmentFallbacks().iterator().next()
            + " (fallback)",
        "",
        "   --rabbit-info-exchange <exchange>",
        "        Also -rabbitInfoExchange.  Used to specify the exchange for connecting",
        "        to RabbitMQ as part of specifying a RabbitMQ info queue.",
        "        --> VIA ENVIRONMENT: " + RABBIT_INFO_EXCHANGE.getEnvironmentVariable(),
        "                             "
            + RABBIT_INFO_EXCHANGE.getEnvironmentFallbacks().iterator().next()
            + " (fallback)",
        "",
        "   --rabbit-info-routing-key <routing key>",
        "        Also -rabbitInfoRoutingKey.  Used to specify the routing key for",
        "        connecting to RabbitMQ as part of specifying a RabbitMQ info queue.",
        "        --> VIA ENVIRONMENT: " + RABBIT_INFO_ROUTING_KEY.getEnvironmentVariable(),
        "",
        "   --kafka-info-bootstrap-server <bootstrap servers>",
        "        Also -kafkaInfoBootstrapServer.  Used to specify the bootstrap servers",
        "        for connecting to Kafka as part of specifying a Kafka info topic.",
        "        --> VIA ENVIRONMENT: " + KAFKA_INFO_BOOTSTRAP_SERVER.getEnvironmentVariable(),
        "                             "
            + KAFKA_INFO_BOOTSTRAP_SERVER.getEnvironmentFallbacks().iterator().next()
            + " (fallback)",
        "",
        "   --kafka-info-group <group id>",
        "        Also -kafkaInfoGroupId.  Used to specify the group ID for connecting to",
        "        Kafka as part of specifying a Kafka info topic.",
        "        --> VIA ENVIRONMENT: " + KAFKA_INFO_GROUP.getEnvironmentVariable(),
        "                             "
            + KAFKA_INFO_GROUP.getEnvironmentFallbacks().iterator().next()
            + " (fallback)",
        "",
        "   --kafka-info-topic <topic>",
        "        Also -kafkaInfoTopic.  Used to specify the topic name for connecting to",
        "        Kafka as part of specifying a Kafka info topic.",
        "        --> VIA ENVIRONMENT: " + KAFKA_INFO_TOPIC.getEnvironmentVariable(),
        ""));
  }

  /**
   * Prints the advanced options usage to the specified {@link PrintWriter}.
   *
   * @param pw The {@link PrintWriter} to write the advanced options usage.
   */
  protected static void printAdvancedOptionsUsage(PrintWriter pw) {
    pw.println(multilineFormat(
        "[ Advanced Options ]",
        "   --config-mgr [config manager options]...",
        "        Also --configmgr.  Should be the first option if provided.  All",
        "        subsequent options are interpreted as configuration manager options.",
        "        If this option is specified by itself then a help message on",
        "        configuration manager options will be displayed.",
        "        NOTE: If this option is provided, the server will not start."));
  }

  /**
   * @return
   */
  public static String getUsageString() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    pw.println();
    printUsageIntro(pw);
    printStandardOptionsUsage(pw);
    printSslOptionsUsage(pw);
    printInfoQueueOptionsUsage(pw);
    printAdvancedOptionsUsage(pw);

    pw.println();
    pw.flush();
    sw.flush();

    return sw.toString();
  }

  /**
   * Outputs the specified {@link String} unless running in quiet mode.
   *
   * @param message
   */
  protected void echo(String message) {
    if (this.quiet) return;
    System.out.println(message);
  }

  /**
   * Gets the {@link List} of {@link Class} instances containing the endpoints
   * to be added to the REST API Server.  The returned {@link List} may be
   * modified directly.
   *
   * @return The {@link List} of {@link Class} instances containing the
   * endpoints to be added to the REST API Server.
   */
  protected List<Class> getServicesClassList() {
    try {
      // get the properties file
      Properties serviceProps = new Properties();
      serviceProps.load(
          SzApiServer.class.getResourceAsStream(SERVICES_RESOURCE_FILE));

      // create the result list
      List<Class> result = new ArrayList<>(serviceProps.size());

      // loop through the keys and treat them as class names
      for (Object key : serviceProps.keySet()) {
        String className = key.toString();
        Class c = Class.forName(className);
        result.add(c);
      }

      // return the list
      return result;

    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Filters the specified {@link List} of {@link Class} instances to remove
   * classes that are base classes to others.  The derived classes will contain
   * the same methods and annotations as the base ones and therefore the base
   * ones are redundant.
   *
   * @param classes The {@link List} of classes.
   * @return The newly created filtered {@link List} of classes or the specified
   * parameter if no classes were filtered.
   */
  @SuppressWarnings("unchecked")
  protected List<Class> filterBaseServiceClasses(List<Class> classes) {
    // create the result
    Set<Class> removeSet = new HashSet<>();

    // loop through the classes
    for (Class c1 : classes) {
      // check if the class is an interface
      if (c1.isInterface()) {
        System.err.println(
            "WARNING: Interface found in services class list: " + c1);
        removeSet.add(c1);
        continue; // there should be no interfaces
      }

      // loop through the other classes
      for (Class c2 : classes) {
        // if the same class then skip this one
        if (c1 == c2) continue;

        // check if the second class is assignable to the first
        if (c1.isAssignableFrom(c2)) {
          removeSet.add(c1);
          break;
        }
      }
    }

    // check if none are to be removed
    if (removeSet.size() == 0) return classes;

    // create the result list
    List<Class> result = new ArrayList<>(classes.size() - removeSet.size());
    for (Class c : classes) {
      if (!removeSet.contains(c)) {
        result.add(c);
      }
    }
    return result;
  }

  /**
   * Adds the Jersey servlet to the specified {@link ServletContextHandler}
   * using the specified package name, path and init order.
   *
   * @param context   The context to add the servlet to.
   * @param path      The path to bind the servlet to.
   * @param initOrder The init order for the servlet.
   */
  protected void addJerseyServlet(ServletContextHandler context,
                                  String path,
                                  int initOrder) {
    ServletHolder jerseyServlet = context.addServlet(
        org.glassfish.jersey.servlet.ServletContainer.class, path);

    jerseyServlet.setInitOrder(initOrder);

    jerseyServlet.setInitParameter(
        "jersey.config.server.provider.packages",
        "org.codehaus.jackson.jaxrs;"
            + "org.glassfish.jersey.media.multipart");

    // get the class list and filter it
    List<Class> classList = this.getServicesClassList();
    classList = this.filterBaseServiceClasses(classList);
    StringBuilder sb = new StringBuilder();
    for (Class c : classList) {
      sb.append(c.getName()).append(";");
    }
    sb.append("org.glassfish.jersey.media.multipart.MultiPartFeature");

    jerseyServlet.setInitParameter(
        "jersey.config.server.provider.classnames", sb.toString());

    jerseyServlet.setInitParameter(
        "jersey.api.json.POJOMappingFeature", "true");
  }

  /**
   * This method is called to install any custom model providers for the model
   * factories.  The default implementation does nothing.
   */
  protected void installModelProviders() {
    // do nothing
  }

  /**
   * Adds the specified zero or more web socket classes to the specified
   * {@link ServletContextHandler}.
   *
   * @param context       The {@link ServletContextHandler} to add the web socket
   *                      endpoints to.
   * @param socketClasses The zero or more Web Socket endpoint classes to add
   *                      to the context.
   */
  protected static void addWebSocketHandler(ServletContextHandler context,
                                            Class... socketClasses) {
    final int maxSize = WEB_SOCKETS_MESSAGE_MAX_SIZE;
    WebSocketServerContainerInitializer.configure(
        context,
        ((servletContext, wsContainer) ->
        {
          wsContainer.setDefaultMaxTextMessageBufferSize(maxSize);
          wsContainer.setDefaultMaxBinaryMessageBufferSize(maxSize);
          wsContainer.setDefaultMaxSessionIdleTimeout(WEB_SOCKET_TIMEOUT);
          for (Class socketClass : socketClasses) {
            wsContainer.addEndpoint(socketClass);
          }
        }));
  }

  /**
   * @param context
   * @param path
   * @param viaHost
   * @param preserveHost
   * @param hostHeader
   * @param initOrder
   */
  protected static void addProxyServlet(ServletContextHandler context,
                                        String path,
                                        String viaHost,
                                        boolean preserveHost,
                                        String hostHeader,
                                        int initOrder) {
    ServletHolder proxyServlet = context.addServlet(
        org.eclipse.jetty.proxy.ProxyServlet.class, path);

    proxyServlet.setInitOrder(initOrder);

    proxyServlet.setInitParameter("viaHost", viaHost);

    proxyServlet.setInitParameter("preserveHost", "" + preserveHost);

    if (hostHeader != null) {
      proxyServlet.setInitParameter("hostHeader", hostHeader);
    }
  }

  /**
   * Creates a new instance of {@link SzApiServer} from the specified options.
   *
   * @param options The {@link Map} of {@link CommandLineOption} keys to
   *                {@link Object} values.
   * @return The created instance of {@link SzApiServer}.
   * @throws Exception If a failure occurs.
   */
  private static SzApiServer build(Map<CommandLineOption, Object> options)
      throws Exception {
    return new SzApiServer(options);
  }

  /**
   * Handles execution from the command line.
   *
   * @param args The command line arguments.
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    commandLineStart(args,
                     SzApiServer::parseCommandLine,
                     SzApiServer::getUsageString,
                     SzApiServer::getVersionString,
                     SzApiServer::build);
  }

  /**
   * Starts an instance of the server from the command line using the specified
   * arguments and the specified function helpers.
   *
   * @param args           The command-line arguments.
   * @param cmdLineParser  The {@link CommandLineParser} to parse the command
   *                       line arguments.
   * @param usageMessage   The {@link Supplier} for the usage message.
   * @param versionMessage The {@link Supplier} for the version message.
   * @param serverBuilder  The {@link CommandLineBuilder} to create the server
   *                       instance from the command line options.
   * @throws Exception
   */
  protected static void commandLineStart(
      String[]                        args,
      CommandLineParser               cmdLineParser,
      Supplier<String>                usageMessage,
      Supplier<String>                versionMessage,
      CommandLineBuilder<SzApiServer> serverBuilder)
      throws Exception
  {
    if (args.length > 0 && (args[0].equals("--repomgr")
        || args[0].equals("--repo-mgr"))) {
      String[] args2 = shiftArguments(args, 1);
      RepositoryManager.main(args2);
      return;
    }
    if (args.length > 0 && (args[0].equals("--configmgr")
        || args[0].equals("--config-mgr"))) {
      String[] args2 = shiftArguments(args, 1);
      ConfigurationManager.main(args2);
      return;
    }

    Map<CommandLineOption, Object>  options   = null;
    List<DeprecatedOptionWarning>   warnings  = new LinkedList<>();
    try {
      options = cmdLineParser.parseCommandLine(args, warnings);

      for (DeprecatedOptionWarning warning: warnings) {
        System.out.println(warning);
        System.out.println();
      }

    } catch (CommandLineException e) {
      System.out.println(e.getMessage());

      System.err.println();
      System.err.println(
          "Try the " + HELP.getCommandLineFlag() + " option for help.");
      System.err.println();
      System.exit(1);

    } catch (Exception e) {
      if (!isLastLoggedException(e)) {
        System.err.println();
        System.err.println(e.getMessage());
        System.err.println();
        e.printStackTrace();
      }
      System.exit(1);
    }

    if (options.containsKey(HELP)) {
      System.out.println(usageMessage.get());
      System.exit(0);
    }
    if (options.containsKey(VERSION)) {
      System.out.println();
      System.out.println(versionMessage.get());
      System.exit(0);
    }

    System.out.println("os.arch        = " + System.getProperty("os.arch"));
    System.out.println("os.name        = " + System.getProperty("os.name"));
    System.out.println("user.dir       = " + System.getProperty("user.dir"));
    System.out.println("user.home      = " + System.getProperty("user.home"));
    System.out.println("java.io.tmpdir = " + System.getProperty("java.io.tmpdir"));

    try {
      if (!SzApiServer.initialize(options, serverBuilder)) {
        System.err.println("FAILED TO INITIALIZE");
        System.exit(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
      exitOnError(e);
    }

    final SzApiServer server = SzApiServer.getInstance();

    final DateTimeFormatter formatter
        = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withZone(ZoneId.systemDefault());

    Thread thread = new Thread(() -> {
      G2Product product = server.getProductApi();
      while (!server.isShutdown()) {
        try {
          String license = product.license();
          JsonObject jsonObject = JsonUtilities.parseJsonObject(license);
          SzLicenseInfo licenseInfo
              = SzLicenseInfo.parseLicenseInfo(null, jsonObject);

          Date expirationDate = licenseInfo.getExpirationDate();

          if (expirationDate != null) {
            long now = System.currentTimeMillis();

            Instant expiration = expirationDate.toInstant();

            long diff = expirationDate.getTime() - now;

            // check if within the expiration warning period
            if (diff < 0L) {
              System.err.println(
                  "WARNING: License expired -- was valid through "
                      + formatter.format(expiration) + ".");

            } else if (diff < EXPIRATION_WARNING_PERIOD) {
              System.err.println(
                  "WARNING: License expiring soon -- valid through "
                      + formatter.format(expiration) + ".");
            }
          }
          try {
            // sleep for six hours
            Thread.sleep(1000 * 60 * 60 * 6);
          } catch (InterruptedException ignore) {
            // do nothing
          }

        } catch (Exception failure) {
          break;
        }
      }
    });
    thread.start();
    server.join();
  }

  /**
   * @param options
   * @return
   */
  protected static boolean initialize(
      Map<CommandLineOption, Object> options,
      CommandLineBuilder<? extends SzApiServer> serverBuilder) {
    if (options.containsKey(HELP)) {
      System.out.println(SzApiServer.getUsageString());
      return false;
    }
    if (options.containsKey(VERSION)) {
      System.out.println();
      System.out.println(SzApiServer.getVersionString());
      return false;
    }
    synchronized (SzApiServer.class) {
      if (SzApiServer.INSTANCE != null) {
        throw new IllegalStateException("Server already initialized!");
      }

      try {
        SzApiServer.INSTANCE = serverBuilder.build(options);
        SzApiServer.PROVIDER_TOKEN
            = SzApiProvider.Factory.installProvider(SzApiServer.INSTANCE);

      } catch (RuntimeException e) {
        throw e;

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return true;
  }

  /**
   * Constructs with the specified parameters.
   *
   * @param httpPort    The HTTP port to bind to.  Use zero to bind to a random
   *                    port and <tt>null</tt> to bind to the default port.
   * @param bindAddress The {@link InetAddress} for the address to bind to.
   *                    If <tt>null</tt> then the loopback address is used.
   * @param concurrency The number of threads to create for the engine, or
   *                    <tt>null</tt> for the default number of threads.
   * @param moduleName  The module name to bind to.  If <tt>null</tt> then the
   *                    {@link SzApiServerConstants#DEFAULT_MODULE_NAME} is used.
   * @param iniFile     The non-null {@link File} with which to initialize.
   * @param verbose     Whether or not to initialize as verbose or not.  If
   *                    <tt>null</tt> then the default setting is invoked.
   * @throws Exception If a failure occurs.
   * @deprecated Use {@link #SzApiServer(SzApiServerOptions)} instead.
   */
  public SzApiServer(Integer httpPort,
                     InetAddress bindAddress,
                     Integer concurrency,
                     String moduleName,
                     File iniFile,
                     Boolean verbose)
      throws Exception {
    this(null,
         httpPort,
         bindAddress,
         concurrency,
         moduleName,
         iniFile,
         verbose);
  }

  /**
   * Constructs with the specified parameters.
   *
   * @param accessToken The {@link AccessToken} for later accessing privileged
   *                    functions.
   * @param httpPort    The HTTP port to bind to.  Use zero to bind to a random
   *                    port and <tt>null</tt> to bind to the default port.
   * @param bindAddress The {@link InetAddress} for the address to bind to.
   *                    If <tt>null</tt> then the loopback address is used.
   * @param concurrency The number of threads to create for the engine, or
   *                    <tt>nul</tt> for the default number of threads.
   * @param moduleName  The module name to bind to.  If <tt>null</tt> then the
   *                    {@link SzApiServerConstants#DEFAULT_MODULE_NAME} is used.
   * @param iniFile     The non-null {@link File} with which to initialize.
   * @param verbose     Whether or not to initialize as verbose or not.  If
   *                    <tt>null</tt> then the default setting is invoked.
   * @throws Exception If a failure occurs.
   * @deprecated Use {@link #SzApiServer(SzApiServerOptions)} instead.
   */
  public SzApiServer(AccessToken accessToken,
                     Integer httpPort,
                     InetAddress bindAddress,
                     Integer concurrency,
                     String moduleName,
                     File iniFile,
                     Boolean verbose)
      throws Exception {
    this(accessToken,
         buildOptionsMap(httpPort,
                         bindAddress,
                         concurrency,
                         moduleName,
                         iniFile,
                         verbose));
  }

  /**
   * Constructs an instance of {@link SzApiServer} with the specified {@link
   * SzApiServerOptions} instance.
   *
   * @param options The {@link SzApiServerOptions} instance with which to
   *                construct the API server instance.
   * @throws Exception If a failure occurs.
   */
  public SzApiServer(SzApiServerOptions options)
      throws Exception {
    this(options.buildOptionsMap());
  }

  /**
   * Constructs with the specified parameters.
   *
   * @param options The options with which to initialize.
   * @throws Exception If a failure occurs.
   */
  protected SzApiServer(Map<CommandLineOption, Object> options)
      throws Exception {

    this(null, options);
  }

  /**
   * Constructs an instance of {@link SzApiServer} with the specified {@link
   * SzApiServerOptions} instance.
   *
   * @param accessToken The {@link AccessToken} for later accessing privileged
   *                    functions.
   * @param options     The {@link SzApiServerOptions} instance with which to
   *                    construct the API server instance.
   * @throws Exception If a failure occurs.
   */
  public SzApiServer(AccessToken accessToken, SzApiServerOptions options)
      throws Exception {
    this(accessToken, options.buildOptionsMap());
  }

  /**
   * Internal method to build an options map.
   */
  protected static Map<CommandLineOption, Object> buildOptionsMap(
      Integer httpPort,
      InetAddress bindAddress,
      Integer concurrency,
      String moduleName,
      File iniFile,
      Boolean verbose) {
    Map<CommandLineOption, Object> map = new HashMap<>();
    if (httpPort != null) map.put(HTTP_PORT, httpPort);
    if (bindAddress != null) map.put(BIND_ADDRESS, bindAddress);
    if (concurrency != null) map.put(CONCURRENCY, concurrency);
    if (moduleName != null) map.put(MODULE_NAME, moduleName);
    if (iniFile != null) map.put(INI_FILE, iniFile);
    if (verbose != null) map.put(VERBOSE, verbose);
    return map;
  }

  /**
   * Constructs with the specified parameters.
   *
   * @param token The {@link AccessToken} for later accessing privileged
   *              functions.
   *
   * @param options The options with which to initialize.
   *
   * @throws Exception If a failure occurs.
   */
  protected SzApiServer(AccessToken                     token,
                        Map<CommandLineOption, Object>  options)
      throws Exception
  {
    this(token, options, true);
  }

  /**
   * Constructs with the specified parameters.
   *
   * @param token The {@link AccessToken} for later accessing privileged
   *              functions.
   *
   * @param options The options with which to initialize.
   *
   * @param startServer <tt>true</tt> if the server should be started, otherwise
   *                    <tt>false</tt>.
   *
   * @throws Exception If a failure occurs.
   */
  @SuppressWarnings("deprecation")
  protected SzApiServer(AccessToken                     token,
                        Map<CommandLineOption, Object>  options,
                        boolean                         startServer)
      throws Exception
  {
    this.accessToken = token;

    // check the debug flag
    Boolean debug = false;
    if (options.containsKey(DEBUG_LOGGING)) {
      debug = (Boolean) options.get(DEBUG_LOGGING);
    }
    if (Boolean.TRUE.equals(debug)) {
      System.setProperty(DEBUG_SYSTEM_PROPERTY, Boolean.TRUE.toString());
    }

    this.httpPort = null;
    if (options.containsKey(HTTP_PORT)) {
      this.httpPort = (Integer) options.get(HTTP_PORT);
    }

    this.ipAddr = InetAddress.getLoopbackAddress();
    if (options.containsKey(BIND_ADDRESS)) {
      this.ipAddr = (InetAddress) options.get(BIND_ADDRESS);
    }

    if (options.containsKey(URL_BASE_PATH)) {
      this.basePath = (String) options.get(URL_BASE_PATH);
    }

    this.concurrency = DEFAULT_CONCURRENCY;
    if (options.containsKey(CONCURRENCY)) {
      this.concurrency = (Integer) options.get(CONCURRENCY);
    }

    this.httpConcurrency = DEFAULT_HTTP_CONCURRENCY;
    if (options.containsKey(HTTP_CONCURRENCY)) {
      this.httpConcurrency = (Integer) options.get(HTTP_CONCURRENCY);
    }

    this.moduleName = DEFAULT_MODULE_NAME;
    if (options.containsKey(MODULE_NAME)) {
      this.moduleName = (String) options.get(MODULE_NAME);
    }

    this.verbose = false;
    if (options.containsKey(VERBOSE)) {
      this.verbose = (Boolean) options.get(VERBOSE);
    }

    this.quiet = false;
    if (options.containsKey(QUIET)) {
      this.quiet = (Boolean) options.get(QUIET);
    }

    this.readOnly = false;
    if (options.containsKey(READ_ONLY)) {
      this.readOnly = (Boolean) options.get(READ_ONLY);
    }

    this.adminEnabled = false;
    if (options.containsKey(ENABLE_ADMIN)) {
      this.adminEnabled = (Boolean) options.get(ENABLE_ADMIN);
    }

    this.statsInterval = DEFAULT_STATS_INTERVAL;
    if (options.containsKey(STATS_INTERVAL)) {
      this.statsInterval
          = (Long) options.get(STATS_INTERVAL);
    }

    this.skipStartupPerf = false;
    if (options.containsKey(SKIP_STARTUP_PERF)) {
      this.skipStartupPerf
          = (Boolean) options.get(SKIP_STARTUP_PERF);
    }

    // determine the init JSON
    this.initJson = (JsonObject) options.get(INIT_FILE);
    if (this.initJson == null) {
      this.initJson = (JsonObject) options.get(INIT_ENV_VAR);
    }
    if (this.initJson == null) {
      this.initJson = (JsonObject) options.get(INIT_JSON);
    }
    if (this.initJson == null) {
      this.iniFile = (File) options.get(INI_FILE);
      if (this.iniFile != null) {
        this.initJson = JsonUtilities.iniToJson(this.iniFile);
      }
    }

    this.configId = (Long) options.get(CONFIG_ID);

    // validate the init JSON
    this.configType = getConfigType(this.moduleName, this.initJson);
    if (this.configType == null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println();
      pw.println("**** ABORTING ****");
      pw.println("No Senzing configuration found in the repository or in the INI properties.");
      pw.println();
      pw.println("The preferred method of specifying the configuration is by setting the default");
      pw.println("configuration in the repository.");
      pw.println();
      pw.println("Having the default configuration in the repository has several advantages:");
      pw.println("   - The configuration can be centrally managed");
      pw.println("   - All instances of the Senzing API Server can use the same configuration");
      pw.println("   - The Senzing API Server can detect changes and automatically reinitialize.");
      pw.println();
      pw.println("If you want to start with an alternate configuration use the -configId option.");
      pw.println();
      pw.flush();

      String msg = sw.toString();
      System.err.println(msg);
      throw new IllegalStateException(msg);
    }

    if (this.iniFile == null && this.configType.isFilePath()) {
      File configPath = extractIniConfigPath(this.initJson);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println();
      pw.println("**** ABORTING ****");
      pw.println("Cannot specify the G2CONFIGFILE property in the JSON initialization parameters:");
      pw.println("--> " + configPath);
      pw.println();
      pw.println("Using G2CONFIGFILE is only allowed with the deprecated -iniFile option");
      pw.println("and only then if there is no default configuration in the repository.");
      pw.println();
      pw.println("Having the default configuration in the repository has several advantages:");
      pw.println("   - The configuration can be centrally managed");
      pw.println("   - All instances of the Senzing API Server can use the same configuration");
      pw.println("   - The Senzing API Server can detect changes and automatically reinitialize.");
      pw.println();
      pw.println("If you want to start with an alternate configuration use the -configId option.");
      pw.println();
      pw.flush();

      String msg = sw.toString();
      System.err.println(msg);
      throw new IllegalStateException(msg);

    } else if (this.configType == ConfigType.BOTH) {
      File configPath = extractIniConfigPath(this.initJson);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println();
      pw.println("**** ABORTING ****");
      pw.println("Cannot specify the G2CONFIGFILE property in the INI file:");
      pw.println("--> " + configPath);
      pw.println();
      pw.println("Using G2CONFIGFILE property is only allowed if there is no default");
      pw.println("configuration in the repository.");
      pw.println();
      pw.println("Having the default configuration in the repository has several advantages:");
      pw.println("   - The configuration can be centrally managed");
      pw.println("   - All instances of the Senzing API Server can use the same configuration");
      pw.println("   - The Senzing API Server can detect changes and automatically reinitialize.");
      pw.println();
      pw.println("If you want to start with an alternate configuration use the -configId option.");
      pw.println();
      pw.flush();
      // we have INI specified from file and database
      String msg = sw.toString();

      System.err.println(msg);
      throw new IllegalStateException(msg);

    } else if (this.configType == ConfigType.FILE_PATH && this.configId != null) {
      File configPath = extractIniConfigPath(this.initJson);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println();
      pw.println("**** ABORTING ****");
      pw.println("Ambiguous configuration.  Cannot specify the -configId option if the");
      pw.println("G2CONFIGFILE property was specified in the INI file:");
      pw.println("--> -configId " + configId);
      pw.println("--> G2CONFIGFILE=" + configPath);
      pw.println();
      pw.println("It is recommended to not use the G2CONFIGFILE property and move the ");
      pw.println("configuration to the repository.");
      pw.println();
      pw.println("Having the configuration in the repository has several advantages:");
      pw.println("   - The configuration can be centrally managed");
      pw.println("   - All instances of the Senzing API Server can use the same configuration");
      pw.println("   - The Senzing API Server can detect changes and automatically reinitialize.");
      pw.println();
      pw.flush();
      // we have INI specified from file and database
      String msg = sw.toString();

      System.err.println(msg);
      throw new IllegalStateException(msg);

    } else if (this.configType == ConfigType.FILE_PATH) {
      File configPath = extractIniConfigPath(this.initJson);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println();
      pw.println("**** DEPRECATION WARNING ****");
      pw.println("Using configuration from the G2CONFIGFILE property in the INI file:");
      pw.println("--> " + configPath);
      pw.println();
      pw.println("Using G2CONFIGFILE property is only allowed if there is no default");
      pw.println("configuration in the repository.");
      pw.println();
      pw.println("Moving the default configuration to the repository has several advantages:");
      pw.println("   - The configuration can be centrally managed");
      pw.println("   - All instances of the Senzing API Server can use the same configuration");
      pw.println("   - The Senzing API Server can detect changes and automatically reinitialize.");
      pw.println();
      pw.flush();
      String msg = sw.toString();
      System.err.println(msg);
    }

    Map<String, Map<String, Object>> optionGroups = new LinkedHashMap<>();

    // organize options into option groups
    options.forEach((option, optionValue) -> {
      // check if its an API server option
      if (optionValue == null) return;
      if (!(option instanceof SzApiServerOption)) return;
      SzApiServerOption serverOption = (SzApiServerOption) option;
      String groupName = serverOption.getGroupName();
      if (groupName == null) return;
      Map<String, Object> groupMap = optionGroups.get(groupName);
      if (groupMap == null) {
        groupMap = new LinkedHashMap<>();
        optionGroups.put(groupName, groupMap);
      }
      String groupProp = serverOption.getGroupPropertyKey();
      groupMap.put(groupProp, optionValue);
    });

    // count the number of specified info queues
    Map<String, Object> infoQueueProps = null;
    for (String key : INFO_QUEUE_GROUPS) {
      if (!optionGroups.containsKey(key)) continue;
      infoQueueProps = optionGroups.get(key);
      break;
    }

    // build the info endpoint
    this.infoEndpoint = (infoQueueProps == null) ? null
        : SzMessagingEndpointFactory.createEndpoint(infoQueueProps,
                                                    this.concurrency);

    this.autoRefreshPeriod = (Long) options.get(AUTO_REFRESH_PERIOD);
    if (this.autoRefreshPeriod != null) {
      this.autoRefreshPeriod *= 1000;
    }

    this.allowedOrigins = (String) options.get(ALLOWED_ORIGINS);

    this.initNativeApis();

    String versionJsonText = this.productApi.version();
    JsonObject versionJson = JsonUtilities.parseJsonObject(versionJsonText);
    this.versionInfo = SzVersionInfo.parseVersionInfo(null, versionJson);

    this.workerThreadPool
        = new WorkerThreadPool(this.getClass().getName(), this.concurrency);

    this.echo("Created Senzing engine thread pool with " + this.concurrency
                  + " thread(s).");

    if (this.configMgrApi != null) {
      // check if the auto refresh period is null
      if (this.autoRefreshPeriod == null) {
        this.autoRefreshPeriod = DEFAULT_CONFIG_REFRESH_PERIOD;
      }
      if (!this.autoRefreshPeriod.equals(0)) {
        this.reinitializer = new Reinitializer(this.configMgrApi,
                                               this.engineApi,
                                               this);
      }
    }

    this.initializeConfigData();

    // prime the engine unless told mot to
    Boolean skipPriming = (Boolean)
        options.get(SzApiServerOption.SKIP_ENGINE_PRIMING);
    if (!Boolean.TRUE.equals(skipPriming)) {
      // prime the engine
      long start = System.currentTimeMillis();
      System.out.println("Priming engine....");
      int returnCode = this.engineApi.primeEngine();
      long end = System.currentTimeMillis();
      System.out.println("Primed engine: " + (end - start) + "ms");

      if (returnCode != 0) {
        throw new IllegalStateException(
            formatError("G2Engine.primeEngine()", this.engineApi));
      }
    } else {
      System.out.println("Engine priming deferred.");
    }

    // setup a servlet context handler
    this.servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
    this.servletContext.setContextPath(this.basePath);

    // add web socket filters first
    EnumSet<DispatcherType> requestDispatch
        = EnumSet.of(DispatcherType.REQUEST);
    this.getWebSocketClasses().forEach((implClass, path) -> {
      // check if we have a pre-flight OnUpgrade annotation
      WebSocketFilter filter = WebSocketFilter.createIfOnUpgrade(implClass);
      if (filter != null) {
        FilterHolder filterHolder = new FilterHolder(filter);
        this.servletContext.addFilter(filterHolder, path, requestDispatch);
      }
    });

    // configure web sockets
    ServerContainer container
        = WebSocketServerContainerInitializer.configureContext(
            this.servletContext);

    this.getWebSocketClasses().forEach((implClass, path) -> {
      try {
        ServerEndpointConfig config = ServerEndpointConfig.Builder.create(
            implClass, path).build();
        container.addEndpoint(config);

      } catch (DeploymentException e) {
        throw new RuntimeException(e);
      }
    });

    // check if debugging requests
    if (LoggingUtilities.isDebugLogging()) {
      this.servletContext.addFilter(DebugRequestFilter.class, "/*", requestDispatch);
    }


    // diagnose requests with errors
    this.servletContext.addFilter(DiagnoseRequestFilter.class, "/*", requestDispatch);

    // check if we have to respond with the allowed origins header
    if (this.allowedOrigins != null) {
      FilterHolder filterHolder = this.servletContext.addFilter(CrossOriginFilter.class, "/*", requestDispatch);
      filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, this.allowedOrigins);
      filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,PUT,DELETE,PATCH,HEAD,OPTIONS");
      filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "*");
      //filterHolder.setInitParameter(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, "10"); // for testing to see OPTIONS requests
    }

    // find how this class was loaded so we can find the path to the static content
    ClassLoader loader = SzApiServer.class.getClassLoader();
    String url = JAR_BASE_URL + "com/senzing/webapps/";
    this.servletContext.setResourceBase(url);

    RewriteHandler rewriteHandler = new RewriteHandler();

    rewriteHandler.setHandler(this.servletContext);

    GzipHandler gzipHandler = new GzipHandler();
    gzipHandler.setIncludedMethods("GET", "POST", "PUT", "DELETE");
    gzipHandler.setInflateBufferSize(GZIP_INFLATE_BUFFER_SIZE);
    gzipHandler.setIncludedMimeTypes(
        APPLICATION_JSON, TEXT_HTML, TEXT_PLAIN, TEXT_XML);
    gzipHandler.setHandler(rewriteHandler);

    // create our server (TODO: add connectors for HTTP + HTTPS)
    ThreadPool threadPool = new QueuedThreadPool(this.httpConcurrency);
    this.jettyServer = new Server(threadPool);

    this.httpsPort              = (Integer) options.get(HTTPS_PORT);
    this.keyStoreFile           = (File) options.get(KEY_STORE);
    this.keyStorePassword       = (String) options.get(KEY_STORE_PASSWORD);
    this.keyAlias               = (String) options.get(KEY_ALIAS);
    this.clientKeyStoreFile     = (File) options.get(CLIENT_KEY_STORE);
    this.clientKeyStorePassword = (String)
        options.get(CLIENT_KEY_STORE_PASSWORD);

    // setup the HTTP configuration
    HttpConfiguration httpConfig  = new HttpConfiguration();
    httpConfig.setOutputBufferSize(HTTP_OUTPUT_BUFFER_SIZE);
    httpConfig.setOutputAggregationSize(HTTP_OUTPUT_AGGREGATION_SIZE);
    httpConfig.setRequestHeaderSize(HTTP_REQUEST_HEADER_SIZE);
    httpConfig.setResponseHeaderSize(HTTP_RESPONSE_HEADER_SIZE);
    httpConfig.setHeaderCacheSize(HTTP_HEADER_CACHE_SIZE);

    if (this.keyStoreFile != null) {
      HttpConfiguration httpsConfig = null;
      httpConfig.setSecureScheme("https");
      httpConfig.setSecurePort(this.httpsPort);

      httpsConfig = new HttpConfiguration(httpConfig);
      httpsConfig.addCustomizer(new SecureRequestCustomizer());

      SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
      sslContextFactory.setKeyStoreType("PKCS12");
      sslContextFactory.setKeyStorePath(this.keyStoreFile.getCanonicalPath());
      sslContextFactory.setKeyStorePassword(this.keyStorePassword);
      if (this.keyAlias != null) {
        sslContextFactory.setCertAlias(this.keyAlias);
      }
      if (this.clientKeyStoreFile != null) {
        sslContextFactory.setNeedClientAuth(true);
        sslContextFactory.setTrustStoreType("PKCS12");
        sslContextFactory.setTrustStorePath(this.clientKeyStoreFile.toString());
        sslContextFactory.setTrustStorePassword(this.clientKeyStorePassword);
      }

      if (this.httpPort != null) {
        ServerConnector httpConnector = new ServerConnector(
            this.jettyServer, new HttpConnectionFactory(httpConfig));
        httpConnector.setPort(this.httpPort);
        this.jettyServer.addConnector(httpConnector);
      }

      ServerConnector httpsConnector = new ServerConnector(
          this.jettyServer,
          new SslConnectionFactory(sslContextFactory,
                                   HttpVersion.HTTP_1_1.asString()),
          new HttpConnectionFactory(httpsConfig));
      httpsConnector.setPort(this.httpsPort);
      this.jettyServer.addConnector(httpsConnector);

    } else {
      if (this.httpPort == null) this.httpPort = DEFAULT_PORT;
      InetSocketAddress inetAddr
          = new InetSocketAddress(this.ipAddr, this.httpPort);
      ServerConnector connector = new ServerConnector(
          this.jettyServer, new HttpConnectionFactory(httpConfig));
      connector.setHost(inetAddr.getHostName());
      connector.setPort(inetAddr.getPort());
      this.jettyServer.setConnectors(new Connector[]{connector});
    }

    this.fileMonitor = null;
    if (options.containsKey(MONITOR_FILE)) {
      this.fileMonitor = (FileMonitor) options.get(MONITOR_FILE);
    }

    this.jettyServer.setHandler(gzipHandler);

    LifeCycleListener lifeCycleListener
        = new LifeCycleListener(this.getDescription(),
                                this.jettyServer,
                                this.httpPort,
                                (this.keyStoreFile == null)
                                    ? null : this.httpsPort,
                                this.basePath,
                                this.ipAddr,
                                this.fileMonitor);
    this.jettyServer.addLifeCycleListener(lifeCycleListener);
    int initOrder = 0;
    String apiPath = "/*";

    //addProxyServlet(context, "/www/*", "https://www.senzing.com", false,
    //                "www.senzing.com", initOrder++);

    this.addJerseyServlet(this.servletContext, apiPath, initOrder++);

    Map<Class, String> webSocketMap = this.getWebSocketClasses();
    Class[] webSocketClasses = new Class[webSocketMap.keySet().size()];
    webSocketClasses = webSocketMap.keySet().toArray(webSocketClasses);
    addWebSocketHandler(this.servletContext, webSocketClasses);

    ServletHolder rootHolder = new ServletHolder("default", DefaultServlet.class);
    rootHolder.setInitParameter("dirAllowed", "false");
    this.servletContext.addServlet(rootHolder, "/");

    if (!this.isSkippingStartupPerformance()) {
      this.logDiagnostics();
    }

    // install any custom model providers
    this.installModelProviders();


    if (startServer) this.startHttpServer(options);
  }

  /**
   * Starts the HTTP server to service requests.
   *
   * @param options The startup options.
   */
  protected void startHttpServer(Map<CommandLineOption, Object> options)
    throws Exception
  {
    try {
      this.jettyServer.start();
      Connector[] connectors = this.jettyServer.getConnectors();
      if (connectors.length == 1) {
        if (this.httpPort == null) {
          this.httpsPort  = ((ServerConnector) connectors[0]).getLocalPort();
        } {
          this.httpPort   = ((ServerConnector) connectors[0]).getLocalPort();
        }
      } else if (connectors.length > 1) {
        this.httpPort   = ((ServerConnector) connectors[0]).getLocalPort();
        this.httpsPort  = ((ServerConnector) connectors[1]).getLocalPort();
      }

      if (options.containsKey(MONITOR_FILE)) {
        this.fileMonitor.initialize(this.httpPort, this.httpsPort);
        this.fileMonitor.start();
      }
    } catch (Exception e) {
      this.shutdown();
      throw e;
    }

    // create a thread to monitor for server termination
    Thread thread = new Thread(() -> {
      try {
        if (this.fileMonitor != null) {
          this.echo("********************************************** ");
          this.echo("    MONITORING FILE FOR SHUTDOWN SIGNAL");
          this.echo("********************************************** ");
          this.fileMonitor.join();
          this.echo("********************************************** ");
          this.echo("    RECEIVED SHUTDOWN SIGNAL");
          this.echo("********************************************** ");
          try {
            if (this.reinitializer != null) this.reinitializer.complete();
            this.servletContext.stop();
            this.jettyServer.stop();
            this.jettyServer.join();
            this.joinReinitializer();

          } catch (Exception e) {
            e.printStackTrace();
          }

        } else {
          this.jettyServer.join();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);

      } finally {
        this.shutdown();
      }
    });
    thread.start();
  }

  /**
   * Purges the repository and recreates the worker thread pool.
   *
   * This should only be done when the API server has no pending requests.
   *
   */
  public synchronized void purgeRepository() {
    this.purgeLock.writeLock().lock();
    try {
      G2Engine engine = this.getEngineApi();
      int returnCode = engine.purgeRepository();
      if (returnCode != 0) {
        throw new IllegalStateException(
            formatError("G2Engine.purgeRepository()", engineApi));
      }
      this.workerThreadPool.close(true);
      this.workerThreadPool
          = new WorkerThreadPool(this.getClass().getName(), this.concurrency);

    } finally {
      this.purgeLock.writeLock().unlock();
    }
  }

  /**
   * Returns the configuration auto-refresh period to use.
   *
   * @return The configuration auto-refresh period to use.
   *
   */
  public long getConfigAutoRefreshPeriod() {
    return ((this.autoRefreshPeriod == null)
            ? DEFAULT_CONFIG_REFRESH_PERIOD : this.autoRefreshPeriod);
  }

  /**
   * Returns the {@link File} object describing the G2 configuration file path
   * obtained from the G2CONFIGFILE property of the specified initialization
   * parameters.
   *
   * @param initJson The initialization parameters represented as JSON.
   *
   * @return The {@link File} object describing the G2 configuration file path
   *         obtained from the G2CONFIGFILE initialization parameter, or
   *         <tt>null</tt> if the G2CONFIGFILE parameter is not present.
   */
  protected static File extractIniConfigPath(JsonObject initJson) {
    JsonObject sqlSection = JsonUtilities.getJsonObject(initJson, "SQL");
    if (sqlSection == null) return null;
    String configFile = JsonUtilities.getString(sqlSection, "G2CONFIGFILE");
    if (configFile == null) return null;
    return new File(configFile);
  }

  /**
   * Returns a new {@link JsonObject} which mirrors the specified {@link
   * JsonObject} but does not contain the "G2CONFIGFILE" property in the
   * "SQL" section (if one exists).  If no such property (or section) exists
   * then the specified object is returned as-is.
   *
   * @param initJson The {@link JsonObject} representation of the INI parameters.
   *
   * @return The {@link JsonObject} representation of the INI parameters, sans
   *         any reference to "G2CONFIGFILE" property.
   */
  protected static JsonObject stripIniConfigPath(JsonObject initJson) {
    JsonObject sqlSection = JsonUtilities.getJsonObject(initJson, "SQL");
    if (sqlSection == null) return initJson;
    String configFile = JsonUtilities.getString(sqlSection, "G2CONFIGFILE");
    if (configFile == null) return initJson;

    // create an object builder and remove the SQL section
    JsonObjectBuilder job = Json.createObjectBuilder(initJson);
    job.remove("SQL");

    // create a builder from the original SQL section and remove G2CONFIGFILE
    JsonObjectBuilder sqlBuilder = Json.createObjectBuilder(sqlSection);
    sqlBuilder.remove("G2CONFIGFILE");

    // restore the modified SQL section to the root builder
    job.add("SQL", sqlBuilder);

    // return the built object
    return job.build();
  }

  /**
   * Checks if the specified INI JSON contains a JSON configuration file path
   * and if so verifies the JSON configuration against the default config and
   * if they do not match, cowardly refuses to startup.
   *
   * @param moduleName The module name to use when initializing.
   *
   * @param initJson The {@link JsonObject} representation of the Senzing
   *                intitialization parameters.
   *
   * @return Zero (0) if no configuration file is provided by the INI, One (1)
   *         if a configuration file is provided and no default configuration
   *         is configured and negative one (-1) if a configuration file is
   *         provided and there is a default configuration configured.
   */
  protected static ConfigType getConfigType(String      moduleName,
                                            JsonObject  initJson)
  {
    if (moduleName == null) {
      moduleName = SzApiServer.class.getSimpleName();
    }
    moduleName +=  " (getConfigType)";
    boolean configInIni = false;
    JsonObject sqlSection = JsonUtilities.getJsonObject(initJson, "SQL");
    if (sqlSection != null) {
      String configFilePath = JsonUtilities.getString(sqlSection, "G2CONFIGFILE");
      configInIni = (configFilePath != null);
    }

    if (configInIni) {
      initJson = stripIniConfigPath(initJson);
    }
    String initJsonText = JsonUtilities.toJsonText(initJson);

    boolean configInRepo = false;
    G2ConfigMgr configMgr = NativeApiFactory.createConfigMgrApi();
    int returnCode = configMgr.init(moduleName, initJsonText, false);
    if (returnCode != 0) {
      String msg = multilineFormat(
          "Failed to initialize with specified initialization parameters.",
          "",
          formatError("G2ConfigMgr.init", configMgr)
          + "Initialization parameters:",
          "",
          (initJsonText.length() > 80
            ? initJsonText.substring(0, 64) + " ... [truncated]"
            : initJsonText),
          "");

      throw new RuntimeException(msg);
    }
    try {
      Result<Long> defaultConfigResult = new Result<>();
      configMgr.getDefaultConfigID(defaultConfigResult);
      Long defaultConfigId = defaultConfigResult.getValue();
      if (defaultConfigId != null && defaultConfigId.longValue() != 0) {
        configInRepo = true;
      }
    } finally {
      configMgr.destroy();
    }
    if (configInIni && configInRepo) {
      return ConfigType.BOTH;

    } else if (configInIni) {
      return ConfigType.FILE_PATH;

    } else if (configInRepo) {
      return ConfigType.MANAGED;

    } else {
      return null;
    }
  }

  /**
   * Initializes the native Senzing API's for this instance.
   */
  protected void initNativeApis()
  {
    String  initJsonText  = JsonUtilities.toJsonText(this.initJson);
    String  iniFilePath   = null;
    if (this.iniFile != null) {
      try {
        iniFilePath = this.iniFile.getCanonicalPath();

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    this.productApi = NativeApiFactory.createProductApi();
    int initResult = this.productApi.init(
        this.moduleName, initJsonText, this.verbose);

    if (initResult < 0) {
      throw new RuntimeException(buildErrorMessage(
          "Failed to initialize G2Product API.",
          this.productApi.getLastExceptionCode(),
          this.productApi.getLastException()));
    }

    this.diagnosticApi = NativeApiFactory.createDiagnosticApi();
    initResult = this.diagnosticApi.init(
        this.moduleName, initJsonText, this.verbose);
    if (initResult < 0) {
      throw new RuntimeException(buildErrorMessage(
          "Failed to initialize G2Diagnostic API.",
          this.diagnosticApi.getLastExceptionCode(),
          this.diagnosticApi.getLastException()));
    }

    this.configApi = NativeApiFactory.createConfigApi();
    initResult = this.configApi.init(
        this.moduleName, initJsonText, this.verbose);
    if (initResult < 0) {
      throw new RuntimeException(buildErrorMessage(
          "Failed to initialize G2Config API.",
          this.configApi.getLastExceptionCode(),
          this.configApi.getLastException()));
    }

    // init the engine API
    this.engineApi = NativeApiFactory.createEngineApi();

    if (this.configType.isManaged() && this.configId != null) {
      // config ID is hard coded and config is in the repository
      initResult = this.engineApi.initWithConfigID(
          this.moduleName, initJsonText, this.configId, this.verbose);

    } else {
      // config is in the repository
      initResult = this.engineApi.init(
          this.moduleName, initJsonText, this.verbose);
    }

    if (initResult < 0) {
      throw new RuntimeException(buildErrorMessage(
          "Failed to initialize G2Engine API",
          this.engineApi.getLastExceptionCode(),
          this.engineApi.getLastException()));
    }

    // initialize the config manager API
    if (this.configType.isManaged() && this.configId == null) {
      this.configMgrApi = NativeApiFactory.createConfigMgrApi();
      initResult = this.configMgrApi.init(this.moduleName, initJsonText, this.verbose);

      if (initResult < 0) {
        throw new RuntimeException(buildErrorMessage(
            "Failed to initialize G2ConfigMgr API",
            this.configMgrApi.getLastExceptionCode(),
            this.configMgrApi.getLastException()));
      }

      Class[]     interfaces  = { G2Engine.class };
      ClassLoader classLoader = this.getClass().getClassLoader();

      // check if logging stats
      if (this.getStatsInterval() > 0L) {
        // proxy the engine API to log stats
        EngineStatsLoggingHandler statsHandler
            = new EngineStatsLoggingHandler(this.engineApi,
                                            this.getStatsInterval(),
                                            System.out);
        this.engineApi = (G2Engine) Proxy.newProxyInstance(
            classLoader, interfaces, statsHandler);
      }


      this.engineRetryHandler
          = new G2EngineRetryHandler(this.engineApi, this);
      this.retryEngineApi = (G2Engine) Proxy.newProxyInstance(
          classLoader, interfaces, this.engineRetryHandler);
    }
  }

  /**
   *
   */
  protected void assertNotShutdown() {
    if (this.isShutdown()) {
      throw new IllegalStateException(
          "This API server instance is already shutdown.");
    }
  }

  /**
   * Checks if this instance has been shutdown.
   *
   * @return <tt>true</tt> if this instance has been shutdown, otherwise
   *         <tt>false</tt>.
   */
  public boolean isShutdown() {
    synchronized (this.joinMonitor) {
      return this.completed;
    }
  }

  /**
   * Shuts down this instance if the specified {@link AccessToken} matches
   * the token with which this instance was constructed.
   *
   * @param token The {@link AccessToken} for privileged access.
   *
   * @throws UnsupportedOperationException If this instance was not constructed
   *                                       with an {@link AccessToken}.
   *
   * @throws IllegalArgumentException If the specified {@link AccessToken} does
   *                                  not match the one with which this instance
   *                                  was constructed.
   */
  public void shutdown(AccessToken token) {
    if (this.accessToken == null) {
      throw new UnsupportedOperationException(
          "This instance has no access token, so this operation is not "
          + "supported for this instance.");
    }
    if (this.accessToken != token) {
      throw new IllegalArgumentException(
          "The specified AccessToken does not provide access to shutdown.");
    }
    this.shutdown();
  }

  /**
   * Internal method for handling cleanup on shutdown.
   */
  protected void shutdown() {
    if (this.jettyServer != null) {
      synchronized (this.jettyServer) {
        try {
          this.jettyServer.destroy();
        } catch (Exception e) {
          // ignore
        }
      }
    }

    // shutdown the file monitor
    this.joinFileMonitor();

    // shutdown the reinitializer
    this.joinReinitializer();

    // uninitialize
    synchronized (SzApiServer.class) {
      if (SzApiServer.INSTANCE == this) {
        SzApiServer.INSTANCE = null;
        try {
          SzApiProvider.Factory.uninstallProvider(SzApiServer.PROVIDER_TOKEN);
        } finally {
          SzApiServer.PROVIDER_TOKEN = null;
        }
      }
    }

    synchronized (this.joinMonitor) {
      this.engineApi.destroy();
      this.configApi.destroy();
      if (this.configMgrApi != null) {
        this.configMgrApi.destroy();
      }
      this.diagnosticApi.destroy();
      this.productApi.destroy();
      this.completed = true;
      this.joinMonitor.notifyAll();
    }
  }

  /**
   * Executes the specified task within a thread pool managed by the
   * {@link SzApiServer} instance.
   *
   * @param task The task to execute.
   *
   * @return The result from the specified {@link Task}.
   *
   * @throws Exception If the task has a failure.
   */
  public <T, E extends Exception> T executeInThread(Task<T, E> task)
    throws E
  {
    this.purgeLock.readLock().lock();
    try {
      return this.workerThreadPool.execute(task);

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw e;

    } finally {
      this.purgeLock.readLock().unlock();
    }
  }

  @Override
  public AccessToken authorizeProlongedOperation() {
    synchronized (this.prolongedAuthSet) {
      // get the number of concurrent prolonged operations
      int prolongedCount = this.prolongedAuthSet.size();

      // check if too many concurrent prolonged operations and return null if so
      if (prolongedCount >= ((this.httpConcurrency - 4) / 2)) return null;

      // create the access token
      AccessToken accessToken = new AccessToken();

      // store it in the set
      this.prolongedAuthSet.add(accessToken);

      // return it to authorize the operation
      return accessToken;
    }
  }

  @Override
  public void concludeProlongedOperation(AccessToken token) {
    // check for null
    Objects.requireNonNull(token, "The specified token cannot be null");

    // synchronize
    synchronized (this.prolongedAuthSet) {
      // check if unrecognized
      if (!this.prolongedAuthSet.remove(token)) {
        throw new IllegalArgumentException(
            "The specified token is not recognized");
      }
    }
  }

  /**
   * Waits for the API server to complete.
   *
   */
  public void join() {
    synchronized (this.joinMonitor) {
      while (!this.completed) {
        try {
          this.joinMonitor.wait(20000L);
        } catch (InterruptedException ignore) {
          // ignore the exception
        }
      }
    }
    this.joinReinitializer();
    this.joinFileMonitor();
  }

  /**
   * Shuts down and joins with the reinitializer (if any)
   */
  protected void joinReinitializer() {
    if (this.reinitializer != null) {
      this.reinitializer.complete();
      while (this.reinitializer.isAlive()) {
        try {
          this.reinitializer.join();
        } catch (InterruptedException ignore) {
          // do nothing
        }
      }
    }
  }

  /**
   * Shuts down and joins with the file monitor (if any)
   */
  protected void joinFileMonitor() {
    if (this.fileMonitor != null) {
      this.fileMonitor.signalShutdown();
      while (this.fileMonitor.isAlive()) {
        try {
          this.fileMonitor.join();
        } catch (InterruptedException ignore) {
          // do nothing
        }
      }
    }
  }

  /**
   * Prompts the reinitializer thread to check the config is current and
   * refresh it.  This is primarily used for testing the reinitializer thread.
   * This will wait until the request completes before returning.  If there is
   * no reinitializer thread then this method has no effect.
   *
   * <b>NOTE:</b> This differs from {@link #ensureConfigCurrent()} in that it
   * performs the task through the reinitializer thread if it exists and does
   * nothing if there is no reinitializer thread.
   */
  public void requestConfigRefreshCheck() {
    if (this.reinitializer == null) return;
    this.reinitializer.requestRefresh();
  }

  /**
   * Checks if the engine's active config is stale and if so reinitializes
   * with the new configuration.
   *
   * @return <tt>true</tt> if the configuration was updated, <tt>false</tt> if
   *         the configuration was already current and <tt>null</tt> if an
   *         error occurred in attempting to ensure it is current.
   */
  public Boolean ensureConfigCurrent() {
    return this.ensureConfigCurrent(false);
  }

  /**
   * Checks if the engine's active config is stale and if so reinitializes
   * with the new configuration.
   *
   * @param pauseWorkers <tt>true</tt> if the worker threads should be paused
   *                     before reinitialization and <tt>false</tt> if not.
   *
   * @return <tt>true</tt> if the configuration was updated, <tt>false</tt> if
   *         the configuration was already current and <tt>null</tt> if an
   *         error occurred in attempting to ensure it is current.
   */
  protected Boolean ensureConfigCurrent(boolean pauseWorkers) {
    this.purgeLock.readLock().lock();
    try {
      // if not capable of reinitialization then return alse
      if (this.configMgrApi == null) return false;

      Long defaultConfigId = null;
      try {
        defaultConfigId = this.getNewConfigurationID();
      } catch (IllegalStateException e) {
        // if we get an exception then return null
        return null;
      }
      if (defaultConfigId == null) return false;

      this.echo("Detected configuration change.");
      AccessToken pauseToken = null;

      // we can pause all workers before reinitializing or just let the underlying
      // G2Engine API handle the mutual exclusion issues
      if (pauseWorkers) {
        this.echo("Pausing API server....");
        pauseToken = this.workerThreadPool.pause();
      }

      int returnCode;
      G2ConfigMgr configMgrApi = this.getConfigMgrApi();
      // once we get here we just need to reinitialize
      synchronized (configMgrApi) {
        try {
          if (pauseWorkers) {
            this.echo("Paused API server.");
          }

          // double-check on the configuration ID
          try {
            defaultConfigId = this.getNewConfigurationID();
          } catch (IllegalStateException e) {
            return null;
          }

          // check if the default config ID has already been updated
          if (defaultConfigId == null) {
            this.echo("API Server Engine API already reinitialized.");
            return true;
          }
          this.echo("Reinitializing with config: " + defaultConfigId);

          // reinitialize with the default config ID
          returnCode = this.engineApi.reinit(defaultConfigId);
          if (returnCode != 0) {
            String errorMsg = formatError(
                "G2Engine.reinit", this.engineApi);
            System.err.println("Failed to reinitialize with config ID ("
                                   + defaultConfigId + "): " + errorMsg);
            return null;
          }

          // reinitialize the cached configuration data
          this.initializeConfigData();

          // return true to indicate we reinitialized
          return true;

        } finally {
          if (pauseWorkers) {
            this.workerThreadPool.resume(pauseToken);
            this.echo("Resumed API server.");
          }
        }
      }

    } finally {
      this.purgeLock.readLock().unlock();
    }
  }

  /**
   * Returns the new configuration ID if the configuration ID has changed,
   * otherwise returns <tt>null</tt>.
   *
   * @return The new configuration ID if the configuration ID has changed,
   *         otherwise returns <tt>null</tt>.
   *
   * @throws IllegalStateException If an engine failure occurs.
   */
  protected Long getNewConfigurationID() throws IllegalStateException {
    G2ConfigMgr configMgrApi = this.getConfigMgrApi();

    // get the active configuration ID
    Result<Long> result = new Result<>();
    int returnCode = this.engineApi.getActiveConfigID(result);

    // check the return code
    if (returnCode != 0) {
      String errorMsg = formatError(
          "G2Engine.getActiveConfigID", this.engineApi);
      System.err.println("Failed to get active config ID: " + errorMsg);
      throw new IllegalStateException(errorMsg);
    }

    // extract the active config ID
    long activeConfigId = result.getValue();

    // synchronize since G2ConfigMgr API is not thread-safe
    synchronized (configMgrApi) {
      // get the default configuration ID
      returnCode = configMgrApi.getDefaultConfigID(result);

      // check the return code
      if (returnCode != 0) {
        String errorMsg = formatError(
            "G2ConfigMgr.getDefaultConfigID", configMgrApi);
        System.err.println("Failed to get default config ID: " + errorMsg);
        throw new IllegalStateException(errorMsg);
      }
    }

    // extract the default config ID
    long defaultConfigId = result.getValue();

    // check if they differ
    if (activeConfigId == defaultConfigId) return null;

    // return the new default config ID
    return defaultConfigId;
  }

  /**
   * Initializes the configuration data cached by this instance. This is done
   * on startup and on reinitialization.
   */
  protected void initializeConfigData() {
    synchronized (this.reinitMonitor) {
      StringBuffer sb = new StringBuffer();
      this.engineApi.exportConfig(sb);

      JsonObject config = JsonUtilities.parseJsonObject(sb.toString());

      Set<String>         dataSourceSet   = new LinkedHashSet<>();
      Map<String,String>  ftypeCodeMap    = new LinkedHashMap<>();
      Map<String,String>  attrCodeMap     = new LinkedHashMap<>();

      this.evaluateConfig(config,
                          dataSourceSet,
                          ftypeCodeMap,
                          attrCodeMap);

      this.dataSources            = Collections.unmodifiableSet(dataSourceSet);
      this.featureToAttrClassMap  = Collections.unmodifiableMap(ftypeCodeMap);
      this.attrCodeToAttrClassMap = Collections.unmodifiableMap(attrCodeMap);
    }
  }

  /**
   * Logs diagnostics.
   */
  protected void logDiagnostics() {
    long now = System.currentTimeMillis();
    if ((now - this.lastDiagnosticsRun) <= DIAGNOSTIC_PERIOD) return;
    boolean firstRun = (this.lastDiagnosticsRun < 0L);
    this.lastDiagnosticsRun = now;

    StringWriter  sw = new StringWriter();
    PrintWriter   pw = new PrintWriter(sw);

    pw.println();
    pw.println("=============================================================");

    if (firstRun) {
      NumberFormat numFormat = new DecimalFormat("#.##");
      double totalMem = ((double) this.diagnosticApi.getTotalSystemMemory());
      totalMem /= (1024.0 * 1024.0 * 1024.0);
      double availMem = ((double) this.diagnosticApi.getAvailableMemory());
      availMem /= (1024.0 * 1024.0 * 1024.0);

      int physicalCores = this.diagnosticApi.getPhysicalCores();
      int logicalCores = this.diagnosticApi.getLogicalCores();

      pw.println("TOTAL SYSTEM MEMORY     : "
                     + numFormat.format(totalMem) + "GB");
      pw.println("AVAILABLE SYSTEM MEMORY : "
                     + numFormat.format(availMem) + "GB");
      pw.println("PHYSICAL CORE COUNT     : " + physicalCores);
      pw.println("LOGICAL CORE COUNT      : " + logicalCores);
    }

    pw.println("=============================================================");
    StringBuffer sb = new StringBuffer();
    int result = this.diagnosticApi.checkDBPerf(3, sb);
    if (result == 0) {
      String jsonText = sb.toString();

      pw.println("DATABASE PERF CHECK RESULT: ");
      pw.println("----------------------------");
      JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonText);
      jsonObject.forEach((key, value) -> {
        pw.println(key + " = " + value);
      });

      JsonObject jsonObj = JsonUtilities.parseJsonObject(jsonText);
      long insertCount = jsonObj.getInt("numRecordsInserted");
      long insertTimeMS = jsonObj.getJsonNumber("insertTime").longValue();
      double insertTimeSec = ((double) insertTimeMS) / 1000.0;

      double txnPerSecond = ((double) insertCount) / insertTimeSec;

      pw.println("transactionsPerSecond = " + txnPerSecond);
      pw.println("=============================================================");
      pw.println();

    } else {
      pw.println("****** DATABASE PERF CHECK FAILED: " + result);
      pw.println("****** ERROR CODE : "
                     + this.diagnosticApi.getLastExceptionCode());
      pw.println("****** ERROR      : "
                     + this.diagnosticApi.getLastException());
    }
    System.out.println(sw.toString());
  }

  /**
   * Obtains the list of Web Socket implementations from the resource properties
   * file by the name given by {@link #WEB_SOCKETS_RESOURCE_FILE}.
   *
   * @return The {@link Map} of {@link Class} objects to URL paths that they
   *         should be mapped to.
   */
  protected Map<Class, String> getWebSocketClasses() {
    synchronized (this.monitor) {
      // check the web socket classes is already initialized
      if (this.webSocketClasses != null) return this.webSocketClasses;

      // if not then initialize
      Map<Class, String> webSocketMap = new LinkedHashMap<>();
      this.populateWebSocketClasses(
          webSocketMap,
          SzApiServer.class.getResourceAsStream(WEB_SOCKETS_RESOURCE_FILE));

      // set the field once complete
      this.webSocketClasses = webSocketMap;
      return this.webSocketClasses;
    }
  }

  /**
   * Reads the specified {@link InputStream} as a {@link Properties}
   * files and populates the specified {@link Map} with the {@link Class}
   * keys and path values found in the properties.
   *
   * @param webSocketMap The {@link Map} to populate.
   * @param propertyStream The {@link InputStream} with the properties.
   */
  protected void populateWebSocketClasses(Map<Class, String>  webSocketMap,
                                          InputStream         propertyStream)
  {
    // if not then initialize
    try {
      Properties webSocketProps = new Properties();
      webSocketProps.load(propertyStream);

      webSocketProps.forEach((className, path) -> {
        try {
          webSocketMap.put(Class.forName(className.toString()),
                           path.toString());

        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      });

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
