package com.senzing.api.server;

import com.senzing.cmdline.CommandLineOption;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;
import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.senzing.api.server.SzApiServerConstants.*;
import static com.senzing.api.server.SzApiServerOption.*;

/**
 * Describes the options to be set when constructing an instance of
 * {@link SzApiServer}.
 *
 */
public class SzApiServerOptions {
  private int         httpPort                  = DEFAULT_PORT;
  private InetAddress bindAddress               = null;
  private int         httpsPort                 = DEFAULT_SECURE_PORT;
  private File        keyStoreFile              = null;
  private String      keyStorePassword          = null;
  private String      keyAlias                  = null;
  private File        clientKeyStoreFile        = null;
  private String      clientKeyStorePassword    = null;
  private String      urlBasePath               = null;
  private int         concurrency               = DEFAULT_CONCURRENCY;
  private int         httpConcurrency           = DEFAULT_HTTP_CONCURRENCY;
  private String      moduleName                = DEFAULT_MODULE_NAME;
  private boolean     verbose                   = false;
  private boolean     quiet                     = false;
  private boolean     readOnly                  = false;
  private boolean     adminEnabled              = false;
  private boolean     skipStartupPerf           = false;
  private boolean     skipEnginePriming         = false;
  private boolean     debugLogging              = false;
  private long        statsInterval             = DEFAULT_STATS_INTERVAL;
  private String      allowedOrigins            = null;
  private Long        configId                  = null;
  private Integer     webSocketsMessageMaxSize  = null;
  private Long        autoRefreshPeriod         = null;
  private JsonObject  jsonInit                  = null;
  private String      kafkaInfoServers          = null;
  private String      kafkaInfoGroupId          = null;
  private String      kafkaInfoTopic            = null;
  private String      rabbitInfoUser            = null;
  private String      rabbitInfoPassword        = null;
  private String      rabbitInfoHost            = null;
  private Integer     rabbitInfoPort            = null;
  private String      rabbitInfoVHost           = null;
  private String      rabbitInfoExchange        = null;
  private String      rabbitInfoRoutingKey      = null;
  private String      sqsInfoUrl                = null;

  /**
   * Constructs with the native Senzing JSON initialization parameters as a
   * {@link JsonObject}.
   *
   * @param jsonInit The JSON initialization parameters.
   */
  public SzApiServerOptions(JsonObject jsonInit) {
    Objects.requireNonNull(jsonInit,
                           "JSON init parameters cannot be null");
    this.jsonInit = jsonInit;
  }

  /**
   * Constructs with the native Senzing JSON initialization parameters as JSON
   * text.
   *
   * @param jsonInitText The JSON initialization parameters as JSON text.
   */
  public SzApiServerOptions(String jsonInitText) {
    this(JsonUtilities.parseJsonObject(jsonInitText));
  }

  /**
   * Returns the {@link JsonObject} describing the initialization parameters
   * for the Senzing engine.
   *
   * @return The {@link JsonObject} describing the initialization parameters
   *         for the Senzing engine.
   */
  public JsonObject getJsonInitParameters() {
    return this.jsonInit;
  }

  /**
   * Returns the HTTP port to bind to.  Zero (0) is returned if binding to
   * a random available port.  This is initialized to the {@linkplain
   * SzApiServerConstants#DEFAULT_PORT default port number} if not explicitly
   * set.
   *
   * @return The HTTP port to bind to or zero (0) if the server will bind
   *         to a random available port.
   */
  public int getHttpPort() {
    return this.httpPort;
  }

  /**
   * Sets the HTTP port to bind to.  Use zero to bind to a random port and
   * <tt>null</tt> to bind to the {@linkplain SzApiServerConstants#DEFAULT_PORT
   * default port}.
   *
   * @param port The HTTP port to bind to, zero (0) if the server should bind
   *             to a random port and <tt>null</tt> if server should bind to
   *             the default port.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setHttpPort(Integer port) {
    this.httpPort = (port != null) ? port : DEFAULT_PORT;
    return this;
  }

  /**
   * Returns the HTTPS port to bind to.  Zero (0) is returned if binding to
   * a random available port.  This is initialized to the {@linkplain
   * SzApiServerConstants#DEFAULT_SECURE_PORT default secure port number} if
   * not explicitly set.
   *
   * @return The HTTPS port to bind to or zero (0) if the server will bind
   *         to a random available port.
   */
  public int getHttpsPort() {
    return this.httpsPort;
  }

  /**
   * Sets the HTTPS port to bind to.  Use zero to bind to a random port and
   * <tt>null</tt> to bind to the {@linkplain
   * SzApiServerConstants#DEFAULT_SECURE_PORT default port}.
   *
   * @param port The HTTPS port to bind to, zero (0) if the server should bind
   *             to a random port and <tt>null</tt> if server should bind to
   *             the default secure port.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setHttpsPort(Integer port) {
    this.httpsPort = (port != null) ? port : DEFAULT_SECURE_PORT;
    return this;
  }

  /**
   * Gets the {@link InetAddress} for the address that the server will bind
   * to.  If this returns <tt>null</tt> then the loopback address is to be used.
   *
   * @return The {@link InetAddress} for the address that the server will
   *         bind, or <tt>null</tt> then the loopback address is to be used.
   */
  public InetAddress getBindAddress() {
    return this.bindAddress;
  }

  /**
   * Sets the {@link InetAddress} for the address that the server will bind
   * to.  Set to <tt>null</tt> to bind to the loopback address.
   *
   * @param addr The {@link InetAddress} for the address that the server will
   *             bind, or <tt>null</tt> if the loopback address is to be used.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setBindAddress(InetAddress addr) {
    this.bindAddress = addr;
    return this;
  }

  /**
   * Gets the {@link File} for the key store for HTTPS support.  This returns
   * <tt>null</tt> if HTTPS is not supported.
   *
   * @return The {@link File} for the key store for HTTPS support.
   */
  public File getKeyStoreFile() {
    return this.keyStoreFile;
  }

  /**
   * Sets the {@link File} for the key store for HTTPS support.  Set this to
   * <tt>null</tt> if HTTPS is not supported.
   *
   * @param keyStoreFile The {@link File} for the key store for HTTPS support.
   */
  public void setKeyStoreFile(File keyStoreFile) {
    this.keyStoreFile = keyStoreFile;
  }

  /**
   * Gets the password for decrypting the {@linkplain #getKeyStoreFile() key
   * store file}.  This returns <tt>null</tt> if HTTPS is not supported.
   *
   * @return The password for decrypting the key store file or <tt>null</tt>
   *         if HTTPS is not supported.
   */
  public String getKeyStorePassword() {
    return this.keyStorePassword;
  }

  /**
   * Sets the password for decrypting the {@linkplain #getKeyStoreFile() key
   * store file}.  Set this to <tt>null</tt> if HTTPS is not supported.
   *
   * @param password The password for decrypting the key store file or
   *                 <tt>null</tt> if HTTPS is not supported.
   */
  public void setKeyStorePassword(String password) {
    this.keyStorePassword = password;
  }

  /**
   * Gets the alias for the server key to use from the {@linkplain
   * #getKeyStoreFile() key store file}.  This returns <tt>null</tt> if
   * HTTPS is not supported <b>or</b> if HTTPS is supported, but the {@linkplain
   * #getKeyStoreFile() key store file} only contains a single key and it is
   * therefore not required.
   *
   * @return The alias for the server key to use from the {@linkplain
   *         #getKeyStoreFile() key store file}, or <tt>null</tt> if not
   *         specified.
   */
  public String getKeyAlias() {
    return this.keyAlias;
  }

  /**
   * Sets the alias for the server key to use from the {@linkplain
   * #getKeyStoreFile() key store file}.  Set this to <tt>null</tt> if HTTPS
   * is not supported <b>or</b> if HTTPS is supported, but the {@linkplain
   * #getKeyStoreFile() key store file} only contains a single key and it is
   * therefore not required.
   *
   * @param keyAlias The alias for the server key to use from the {@linkplain
   *                 #getKeyStoreFile() key store file}, or <tt>null</tt> if
   *                 HTTPS is not supported or the alias is not needed.
   */
  public void setKeyAlias(String keyAlias) {
    this.keyAlias = keyAlias;
  }

  /**
   * Gets the {@link File} for the client key store for SSL client
   * authentication.  This returns <tt>null</tt> if SSL client authentication
   * is not required.
   *
   * @return The {@link File} for the client key store for SSL client
   *         authentication or <tt>null</tt> if SSL client authentication is
   *         not required.
   */
  public File getClientKeyStoreFile() {
    return this.clientKeyStoreFile;
  }

  /**
   * Sets the {@link File} for the client key store for SSL client
   * authentication.  Set this to <tt>null</tt> if SSL client authentication is
   * not required.
   *
   * @param keyStoreFile The {@link File} for the client key store for SSL
   *                     client authentication or <tt>null</tt> if SSL client
   *                     authentication is not required.
   */
  public void setClientKeyStoreFile(File keyStoreFile) {
    this.clientKeyStoreFile = keyStoreFile;
  }

  /**
   * Gets the password for decrypting the {@linkplain #getClientKeyStoreFile()
   * client key store file}.  This returns <tt>null</tt> if SSL client
   * authentication is not required.
   *
   * @return The password for decrypting the client key store file or
   *         <tt>null</tt> if SSL client authentication is not required.
   */
  public String getClientKeyStorePassword() {
    return this.clientKeyStorePassword;
  }

  /**
   * Sets the password for decrypting the {@linkplain #getClientKeyStoreFile()
   * client key store file}.  Set this to <tt>null</tt> if SSL client
   * authentication is not required.
   *
   * @param password The password for decrypting the client key store file or
   *                 <tt>null</tt> if SSL client authentication is not required.
   */
  public void setClientKeyStorePassword(String password) {
    this.clientKeyStorePassword = password;
  }

  /**
   * Gets the URL base path to use for API server.
   *
   * @return The URL base path to use for the API server.
   */
  public String getUrlBasePath() {
    return this.urlBasePath;
  }

  /**
   * Sets the URL base path to use for the API Server.
   *
   * @param urlBasePath The URL base path.
   */
  public void setUrlBasePath(String urlBasePath) {
    this.urlBasePath = urlBasePath;
  }

  /**
   * Gets the number of threads that the server will create for the engine.
   * If the value has not {@linkplain #setConcurrency(Integer) explicitly set}
   * then {@link SzApiServerConstants#DEFAULT_CONCURRENCY} is returned.
   *
   * @return The number of threads that the server will create for the engine.
   */
  public int getConcurrency() {
    return this.concurrency;
  }

  /**
   * Sets the number of threads that the server will create for the engine.
   * Set to <tt>null</tt> to use the {@linkplain
   * SzApiServerConstants#DEFAULT_CONCURRENCY default number of threads}.
   *
   * @param concurrency The number of threads to create for the engine, or
   *                    <tt>null</tt> for the default number of threads.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setConcurrency(Integer concurrency) {
    this.concurrency = (concurrency != null)
        ? concurrency : DEFAULT_CONCURRENCY;
    return this;
  }

  /**
   * Gets the maximum number of threads that will be used for the web server
   * thread pool.  If the value is not {@linkplain #setHttpConcurrency(Integer)
   * explicitly set} then {@link SzApiServerConstants#DEFAULT_HTTP_CONCURRENCY}
   * is returned.
   *
   * @return The maximum number of threads for the web server thread pool.
   */
  public int getHttpConcurrency() {
    return this.httpConcurrency;
  }

  /**
   * Sets the maximum number of threads that will be used for the web server
   * thread pool.  Set to <tt>null</tt> to use the {@linkplain
   * SzApiServerConstants#DEFAULT_HTTP_CONCURRENCY default number of threads}.
   * If set less than {@link SzApiServerConstants#MINIMUM_HTTP_CONCURRENCY} then
   * an {@link IllegalArgumentException} is thrown.
   *
   * @param concurrency The maximum number of threads for the web server thread
   *                    pool, or <tt>null</tt> for the default number of
   *                    threads.
   *
   * @return A reference to this instance.
   *
   * @throws IllegalArgumentException If the specified concurrency is less than
   *                                  {@link SzApiServerConstants#MINIMUM_HTTP_CONCURRENCY}.
   */
  public SzApiServerOptions setHttpConcurrency(Integer concurrency) {
    concurrency = (concurrency!=null) ? concurrency : DEFAULT_HTTP_CONCURRENCY;
    if (concurrency < MINIMUM_HTTP_CONCURRENCY) {
      throw new IllegalArgumentException(
          "The specified HTTP concurrency cannot be less than "
          + MINIMUM_HTTP_CONCURRENCY);
    }
    this.httpConcurrency = concurrency;
    return this;
  }

  /**
   * Gets the module name to initialize with.  If <tt>null</tt> is returned
   * then {@link SzApiServerConstants#DEFAULT_MODULE_NAME} is used.
   *
   * @return The module name to initialize with, or <tt>null</tt> is returned
   *         then {@link SzApiServerConstants#DEFAULT_MODULE_NAME} is used.
   */
  public String getModuleName() {
    return this.moduleName;
  }

  /**
   * Sets the module name to initialize with.  Set to <tt>null</tt> if the
   * default value of {@link SzApiServerConstants#DEFAULT_MODULE_NAME} is to be
   * used.
   *
   * @param moduleName The module name to bind to, or <tt>null</tt> then the
   *                   {@link SzApiServerConstants#DEFAULT_MODULE_NAME} is used.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setModuleName(String moduleName) {
    this.moduleName = moduleName;
    return this;
  }

  /**
   * Checks whether or not to initialize the Senzing API's in verbose mode.
   * If the verbosity has not been {@linkplain #setVerbose(boolean)
   * explicitly set} then <tt>false</tt> is returned.
   *
   * @return <tt>true</tt> if the native Senzing API's should be initialized
   *         in verbose mode, otherwise <tt>false</tt>.
   */
  public boolean isVerbose() {
    return this.verbose;
  }

  /**
   * Sets whether or not to initialize the Senzing API's in verbose mode.
   *
   * @param verbose <tt>true</tt> if the native Senzing API's should be
   *                initialized in verbose mode, otherwise <tt>false</tt>.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setVerbose(boolean verbose) {
    this.verbose = verbose;
    return this;
  }

  /**
   * Checks whether or not the API server should forbid access to operations
   * that would modify the entity repository and allow only read operations.
   * If the read-only restriction has not been {@linkplain
   * #setReadOnly(boolean) explicitly set} then <tt>false</tt> is returned.
   *
   * @return <tt>true</tt> if the API server should only allow read
   *         operations, and <tt>false</tt> if all operations are allowed.
   */
  public boolean isReadOnly() {
    return this.readOnly;
  }

  /**
   * Sets whether or not the API server should forbid access to operations
   * that would modify the entity repository and allow only read operations.
   *
   * @param readOnly <tt>true</tt> if the API server should forbid write
   *                 operations, and <tt>false</tt> if all operations are
   *                 allowed.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }

  /**
   * Checks whether or not the API server should allow access to admin
   * operations.  If the admin features have not been {@linkplain
   * #setAdminEnabled(boolean) explicitly enabled} then <tt>false</tt> is
   * returned.
   *
   * @return <tt>true</tt> if the API server should only allow admin
   *         operations, and <tt>false</tt> if admin operations are forbidden.
   */
  public boolean isAdminEnabled() {
    return this.adminEnabled;
  }

  /**
   * Sets whether or not the API server should allow access to admin operations.
   *
   * @param adminEnabled <tt>true</tt> if the API server should allow admin
   *                     operations, and <tt>false</tt> if admin operations
   *                     are forbidden.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setAdminEnabled(boolean adminEnabled) {
    this.adminEnabled = adminEnabled;
    return this;
  }

  /**
   * Checks whether or not the API server should reduce the number of messages
   * sent to standard output.  This applies to messages specific to the API
   * server and NOT messages generated by the underlying native API (especially
   * if the API is initialized in verbose mode).
   *
   * @return <tt>true</tt> if the API server should reduce the number of
   *         messages sent to standard output, otherwise <tt>false</tt>
   */
  public boolean isQuiet() {
    return this.quiet;
  }

  /**
   * Sets whether or not the API server should reduce the number of messages
   * sent to standard output.  This applies to messages specific to the API
   * server and NOT messages generated by the underlying native API (especially
   * if the API is initialized in verbose mode).
   *
   * @param quiet <tt>true</tt> if the API server should reduce the number of
   *              messages sent to standard output, otherwise <tt>false</tt>
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setQuiet(boolean quiet) {
    this.quiet = quiet;
    return this;
  }

  /**
   * Checks whether or not debug logging is enabled.  If debug has not been
   * {@linkplain #setDebugLogging(boolean) explicitly enabled} then
   * <tt>false</tt> is returned.
   *
   * @return <tt>true</tt> if debug logging should be enabled, otherwise
   *         <tt>false</tt>.
   */
  public boolean isDebugLogging() {
    return this.debugLogging;
  }

  /**
   * Sets whether or debug logging should be enabled.
   *
   * @param debugLogging <tt>true</tt> if debug logging should be enabled,
   *                     otherwise <tt>false</tt>.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setDebugLogging(boolean debugLogging) {
    this.debugLogging = debugLogging;
    return this;
  }

  /**
   * Returns the CORS Access-Control-Allow-Origin header to use for responses
   * from all HTTP REST API endpoints.  This returns <tt>null</tt> if the
   * CORS Access-Control-Allow-Origin header is to be omitted from the HTTP
   * responses.
   *
   * @return The CORS Access-Control-Allow-Origin header to use for responses
   *         from all HTTP REST API endpoints, or <tt>null</tt> if the header
   *         is to be omitted.
   */
  public String getAllowedOrigins() {
    return this.allowedOrigins;
  }

  /**
   * Sets the CORS Access-Control-Allow-Origin header to use for responses
   * from all HTTP REST API endpoints.  Set this to <tt>null</tt> if the
   * CORS Access-Control-Allow-Origin header is to be omitted from the HTTP
   * responses.
   *
   * @param allowOriginHeader The CORS Access-Control-Allow-Origin header to
   *                          use for responses from all HTTP REST API
   *                          endpoints, or <tt>null</tt> if the header
   *                          is to be omitted.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setAllowedOrigins(String allowOriginHeader) {
    this.allowedOrigins = allowOriginHeader;
    return this;
  }

  /**
   * Gets the explicit configuration ID with which to initialize the Senzing
   * native engine API.  This method returns <tt>null</tt> if the API server
   * should use the current default configuration ID from the entity
   * repository.  This method returns <tt>null</tt> if the value has not been
   * {@linkplain #setConfigurationId(Long) explicitly set}.
   *
   * @return The explicit configuration ID with which to initialize the
   *         Senzing native engine API, or <tt>null</tt> if the API server
   *         should use the current default configuration ID from the entity
   *         repository.
   */
  public Long getConfigurationId() {
    return this.configId;
  }

  /**
   * Sets the explicit configuration ID with which to initialize the Senzing
   * native engine API.  Set the value to <tt>null</tt> if the API server
   * should use the current default configuration ID from the entity
   * repository.
   *
   * @param configId The explicit configuration ID with which to initialize
   *                 the Senzing native engine API, or <tt>null</tt> if the
   *                 API server should use the current default configuration
   *                 ID from the entity repository.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setConfigurationId(Long configId) {
    this.configId = configId;
    return this;
  }

  /**
   * Returns the auto refresh period which is positive to indicate a number of
   * seconds to delay, zero if auto-refresh is disabled, and negative to
   * indicate that the auto refresh thread should run but refreshes will be
   * requested manually (used for testing).
   *
   * @return The auto refresh period.
   */
  public Long getAutoRefreshPeriod() {
    return this.autoRefreshPeriod;
  }

  /**
   * Sets the configuration auto refresh period.  Set the value to <tt>null</tt>
   * if the API server should use {@link
   * SzApiServerConstants#DEFAULT_CONFIG_REFRESH_PERIOD}.
   *
   * @param autoRefreshPeriod The number of seconds to automatically
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setAutoRefreshPeriod(Long autoRefreshPeriod) {
    this.autoRefreshPeriod = autoRefreshPeriod;
    return this;
  }

  /**
   * Gets the minimum time interval for logging stats.  This is the minimum
   * period between logging of stats assuming the API Server is performing
   * operations that will affect stats (i.e.: activities pertaining to entity
   * scoring).  If the API Server is idle or active, but not performing entity
   * scoring activities then stats logging will be delayed until activities are
   * performed that will affect stats.  If the returned interval is zero (0)
   * then stats logging will be suppressed.
   *
   * @return The interval for logging stats, or zero (0) if stats logging is
   *         suppressed.
   */
  public long getStatsInterval() {
    return this.statsInterval;
  }

  /**
   * Sets the minimum interval for logging stats.  This is the minimum
   * period between logging of stats assuming the API Server is performing
   * operations that will affect stats (i.e.: activities pertaining to entity
   * scoring).  If the API Server is idle or active, but not performing entity
   * scoring activities then stats logging will be delayed until activities are
   * performed that will affect stats.  If the specified value is zero (0)
   * then stats logging will be suppressed.  If the specified value is less-than
   * zero (0) then the value will be set to zero (0).
   *
   * @param statsInterval The stats interval, or a non-positive number (e.g.:
   *                      zero) to suppress logging stats.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setStatsInterval(long statsInterval) {
    this.statsInterval = (statsInterval < 0L) ? 0L : statsInterval;
    return this;
  }

  /**
   * Gets the maximum number of bytes for both text and binary web sockets
   * messages.
   *
   * @return The maximum number of bytes for both text and binary web sockets
   *         messages.
   */
  public int getWebSocketsMessageMaxSize() {
    return this.webSocketsMessageMaxSize;
  }

  /**
   * Sets the maximum number of bytes for both text and binary web sockets
   * messages.
   *
   * @param webSocketsMessageMaxSize The maximum number of bytes for both text
   *                                 and binary web sockets messages.
   */
  public void setWebSocketsMessageMaxSize(int webSocketsMessageMaxSize) {
    this.webSocketsMessageMaxSize = webSocketsMessageMaxSize;
  }

  /**
   * Checks whether or not the API server should skip the performance check that
   * is performed at startup.
   *
   * @return <tt>true</tt> if the API server should skip the performance
   *         check performed at startup, and <tt>false</tt> if not.
   */
  public boolean isSkippingStartupPerformance() {
    return this.skipStartupPerf;
  }

  /**
   * Sets whether or not the API server should skip the performance check that
   * is performed at startup.
   *
   * @param skipping <tt>true</tt> if the API server should skip the performance
   *                 check performed at startup, and <tt>false</tt> if not.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setSkippingStartupPerformance(boolean skipping) {
    this.skipStartupPerf = skipping;
    return this;
  }

  /**
   * Checks whether or not the API server should skip priming the engine on
   * startup.
   *
   * @return <tt>true</tt> if the API server should skip priming the engine on
   *         startup, and <tt>false</tt> if not.
   */
  public boolean isSkippingEnginePriming() {
    return this.skipEnginePriming;
  }

  /**
   * Sets whether or not the API server should skip the priming the engine on
   * startup.
   *
   * @param skipping <tt>true</tt> if the API server should skip priming the
   *                 engine on startup, and <tt>false</tt> if not.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setSkippingEnginePriming(boolean skipping) {
    this.skipEnginePriming = skipping;
    return this;
  }

  /**
   * Returns the Kafka bootstrap servers to connect to for the "info" queue.
   * This is part of the info queue configuration to push "info" messages when
   * records are loaded or deleted or entities are reevaluated.
   *
   * @return The Kafka boostrap servers for the "info" queue.
   */
  public String getKafkaInfoBootstrapServers() {
    return this.kafkaInfoServers;
  }

  /**
   * Sets the Kafka server to connect to for the "info" queue.  This is part
   * of the info queue configuration to push "info" messages when records are
   * loaded or deleted or entities are reevaluated.
   *
   * @param servers The Kafka bootstrap servers for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setKafkaInfoBootstrapServers(String servers)
  {
    this.kafkaInfoServers = servers;
    return this;
  }

  /**
   * Returns the Kafka group ID to for the "info" queue.  This is part of the
   * info queue configuration to push "info" messages when records are loaded
   * or deleted or entities are reevaluated.
   *
   * @return The Kafka group ID for the "info" queue.
   */
  public String getKafkaInfoGroupId() {
    return this.kafkaInfoGroupId;
  }

  /**
   * Sets the Kafka group ID to for the "info" queue.  This is part of the info
   * queue configuration to push "info" messages when records are loaded or
   * deleted or entities are reevaluated.
   *
   * @param groupId The Kafka group ID for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setKafkaInfoGroupId(String groupId) {
    this.kafkaInfoGroupId = groupId;
    return this;
  }

  /**
   * Returns the Kafka topic for the "info" queue.  This is part of the info
   * queue configuration to push "info" messages when records are loaded or
   * deleted or entities are reevaluated.
   *
   * @return The Kafka topic for the "info" queue.
   */
  public String getKafkaInfoTopic() {
    return this.kafkaInfoTopic;
  }

  /**
   * Sets the Kafka topic for the "info" queue.  This is part of the info
   * queue configuration to push "info" messages when records are loaded or
   * deleted or entities are reevaluated.
   *
   * @param topic The Kafka topic for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setKafkaInfoTopic(String topic) {
    this.kafkaInfoTopic = topic;
    return this;
  }

  /**
   * Returns the RabbitMQ user for the "info" queue.  This is part of the info
   * queue configuration to push "info" messages when records are loaded or
   * deleted or entities are reevaluated.
   *
   * @return The RabbitMQ user for the "info" queue.
   */
  public String getRabbitInfoUser() {
    return this.rabbitInfoUser;
  }

  /**
   * Sets the RabbitMQ user for the "info" queue.  This is part of the info
   * queue configuration to push "info" messages when records are loaded or
   * deleted or entities are reevaluated.
   *
   * @param user The RabbitMQ user for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setRabbitInfoUser(String user) {
    this.rabbitInfoUser = user;
    return this;
  }

  /**
   * Returns the RabbitMQ password for the "info" queue.  This is part of the
   * info queue configuration to push "info" messages when records are loaded
   * or deleted or entities are reevaluated.
   *
   * @return The RabbitMQ password for the "info" queue.
   */
  public String getRabbitInfoPassword() {
    return this.rabbitInfoPassword;
  }

  /**
   * Sets the RabbitMQ password for the "info" queue.  This is part of the info
   * queue configuration to push "info" messages when records are loaded or
   * deleted or entities are reevaluated.
   *
   * @param password The RabbitMQ password for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setRabbitInfoPassword(String password) {
    this.rabbitInfoPassword = password;
    return this;
  }

  /**
   * Returns the RabbitMQ host for the "info" queue.  This is part of the
   * info queue configuration to push "info" messages when records are loaded
   * or deleted or entities are reevaluated.
   *
   * @return The RabbitMQ host for the "info" queue.
   */
  public String getRabbitInfoHost() {
    return this.rabbitInfoHost;
  }

  /**
   * Sets the RabbitMQ host for the "info" queue.  This is part of the info
   * queue configuration to push "info" messages when records are loaded or
   * deleted or entities are reevaluated.
   *
   * @param host The RabbitMQ host for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setRabbitInfoHost(String host) {
    this.rabbitInfoHost = host;
    return this;
  }

  /**
   * Returns the RabbitMQ port for the "info" queue.  This is part of the
   * info queue configuration to push "info" messages when records are loaded
   * or deleted or entities are reevaluated.
   *
   * @return The RabbitMQ port for the "info" queue.
   */
  public Integer getRabbitInfoPort() {
    return this.rabbitInfoPort;
  }

  /**
   * Sets the RabbitMQ port for the "info" queue.  This is part of the info
   * queue configuration to push "info" messages when records are loaded or
   * deleted or entities are reevaluated.
   *
   * @param port The RabbitMQ port for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setRabbitInfoPort(Integer port) {
    this.rabbitInfoPort = port;
    return this;
  }

  /**
   * Returns the RabbitMQ virtual host for the "info" queue.  This is part of
   * the info queue configuration to push "info" messages when records are
   * loaded or deleted or entities are reevaluated.
   *
   * @return The RabbitMQ virtual host for the "info" queue.
   */
  public String getRabbitInfoVirtualHost() {
    return this.rabbitInfoVHost;
  }

  /**
   * Sets the RabbitMQ virtual host for the "info" queue.  This is part of the
   * info queue configuration to push "info" messages when records are loaded
   * or deleted or entities are reevaluated.
   *
   * @param virtualHost The RabbitMQ virtual host for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setRabbitInfoVirtualHost(String virtualHost) {
    this.rabbitInfoVHost = virtualHost;
    return this;
  }

  /**
   * Returns the RabbitMQ exchange for the "info" queue.  This is part of
   * the info queue configuration to push "info" messages when records are
   * loaded or deleted or entities are reevaluated.
   *
   * @return The RabbitMQ exchange for the "info" queue.
   */
  public String getRabbitInfoExchange() {
    return this.rabbitInfoExchange;
  }

  /**
   * Sets the RabbitMQ exchange for the "info" queue.  This is part of the
   * info queue configuration to push "info" messages when records are loaded
   * or deleted or entities are reevaluated.
   *
   * @param exchange The RabbitMQ exchange for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setRabbitInfoExchange(String exchange) {
    this.rabbitInfoExchange = exchange;
    return this;
  }

  /**
   * Returns the RabbitMQ routing key for the "info" queue.  This is part of
   * the info queue configuration to push "info" messages when records are
   * loaded or deleted or entities are reevaluated.
   *
   * @return The RabbitMQ routing key for the "info" queue.
   */
  public String getRabbitInfoRoutingKey() {
    return this.rabbitInfoRoutingKey;
  }

  /**
   * Sets the RabbitMQ routing key for the "info" queue.  This is part of the
   * info queue configuration to push "info" messages when records are loaded
   * or deleted or entities are reevaluated.
   *
   * @param routingKey The RabbitMQ routing key for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setRabbitInfoRoutingKey(String routingKey) {
    this.rabbitInfoRoutingKey = routingKey;
    return this;
  }

  /**
   * Returns the SQS URL for the "info" queue.  This is part of
   * the info queue configuration to push "info" messages when records are
   * loaded or deleted or entities are reevaluated.
   *
   * @return The SQS URL for the "info" queue.
   */
  public String getSqsInfoUrl() {
    return sqsInfoUrl;
  }

  /**
   * Sets the SQS URL for the "info" queue.  This is part of the info queue
   * configuration to push "info" messages when records are loaded or deleted
   * or entities are reevaluated.
   *
   * @param url The SQS URL for the "info" queue.
   *
   * @return A reference to this instance.
   */
  public SzApiServerOptions setSqsInfoUrl(String url) {
    this.sqsInfoUrl = url;
    return this;
  }

  /**
   * Creates a {@link Map} of {@link CommandLineOption} keys to {@link Object}
   * values for initializing an {@link SzApiServer} instance.
   *
   * @return The {@link Map} of {@link CommandLineOption} keys to {@link Object}
   *         values for initializing an {@link SzApiServer} instanc
   */
  protected Map<CommandLineOption, Object> buildOptionsMap() {
    Map<CommandLineOption, Object> map = new HashMap<>();
    put(map, HTTP_PORT,                    this.getHttpPort());
    put(map, BIND_ADDRESS,                 this.getBindAddress());
    put(map, HTTPS_PORT,                   this.getHttpsPort());
    put(map, KEY_STORE,                    this.getKeyStoreFile());
    put(map, KEY_STORE_PASSWORD,           this.getKeyStorePassword());
    put(map, KEY_ALIAS,                    this.getKeyAlias());
    put(map, CLIENT_KEY_STORE,             this.getClientKeyStoreFile());
    put(map, CLIENT_KEY_STORE_PASSWORD,    this.getClientKeyStorePassword());
    put(map, URL_BASE_PATH,                this.getUrlBasePath());
    put(map, CONCURRENCY,                  this.getConcurrency());
    put(map, HTTP_CONCURRENCY,             this.getHttpConcurrency());
    put(map, MODULE_NAME,                  this.getModuleName());
    put(map, VERBOSE,                      this.isVerbose());
    put(map, QUIET,                        this.isQuiet());
    put(map, DEBUG_LOGGING,                this.isDebugLogging());
    put(map, READ_ONLY,                    this.isReadOnly());
    put(map, ENABLE_ADMIN,                 this.isAdminEnabled());
    put(map, ALLOWED_ORIGINS,              this.getAllowedOrigins());
    put(map, CONFIG_ID,                    this.getConfigurationId());
    put(map, INIT_JSON,                    this.getJsonInitParameters());
    put(map, AUTO_REFRESH_PERIOD,          this.getAutoRefreshPeriod());
    put(map, STATS_INTERVAL,               this.getStatsInterval());
    put(map, SKIP_STARTUP_PERF,            this.isSkippingStartupPerformance());
    put(map, SKIP_ENGINE_PRIMING,          this.isSkippingEnginePriming());
    put(map, KAFKA_INFO_BOOTSTRAP_SERVER,  this.getKafkaInfoBootstrapServers());
    put(map, KAFKA_INFO_GROUP,             this.getKafkaInfoGroupId());
    put(map, KAFKA_INFO_TOPIC,             this.getKafkaInfoTopic());
    put(map, RABBIT_INFO_USER,             this.getRabbitInfoUser());
    put(map, RABBIT_INFO_PASSWORD,         this.getRabbitInfoPassword());
    put(map, RABBIT_INFO_HOST,             this.getRabbitInfoHost());
    put(map, RABBIT_INFO_PORT,             this.getRabbitInfoPort());
    put(map, RABBIT_INFO_VIRTUAL_HOST,     this.getRabbitInfoVirtualHost());
    put(map, RABBIT_INFO_EXCHANGE,         this.getRabbitInfoExchange());
    put(map, RABBIT_INFO_ROUTING_KEY,      this.getRabbitInfoRoutingKey());
    put(map, SQS_INFO_URL,                 this.getSqsInfoUrl());
    return map;
  }

  /**
   * Utility method to only put non-null values in the specified {@link Map}
   * with the specified {@link SzApiServerOption} key and {@link Object} value.
   *
   * @param map The {@link Map} to put the key-value pair into.
   * @param option The {@link SzApiServerOption} key.
   * @param value The {@link Object} value.
   */
  private static void put(Map<CommandLineOption, Object>  map,
                          SzApiServerOption               option,
                          Object                          value)
  {
    if (value != null) {
      map.put(option, value);
    }
  }
}
