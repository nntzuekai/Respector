package com.senzing.api.services;

import com.senzing.api.model.SzServerInfo;
import com.senzing.api.model.SzVersionInfo;
import com.senzing.g2.engine.*;
import com.senzing.util.AccessToken;
import com.senzing.util.SemanticVersion;
import com.senzing.util.WorkerThreadPool;

import java.lang.invoke.StringConcatException;
import java.util.Date;
import java.util.Set;

/**
 * This interface abstracts the various functions required by the API services
 * to run.
 */
public interface SzApiProvider {
  /**
   * Until we come up with a better method, use a simple factory to make
   * the global provider instance available.
   */
  class Factory {
    /**
     * The installed {@link SzApiProvider}.
     */
    private static SzApiProvider PROVIDER = null;

    /**
     * The {@link AccessToken} to authorizing uninstalling the provider.
     */
    private static AccessToken ACCESS_TOKEN = null;

    /**
     * Install a provider.  This fails if a provider is already installed.
     *
     * @param provider The non-null provider to install.
     *
     * @throws NullPointerException If the specified parameter is <tt>null</tt>.
     *
     * @throws IllegalStateException If a provider is already installed.
     */
    public static synchronized AccessToken installProvider(SzApiProvider provider) {
      if (provider == null) {
        throw new NullPointerException(
            "The specified provider cannot be null.");
      }
      if (PROVIDER != null) {
        throw new IllegalStateException(
            "An SzApiProvider is already installed: "
            + PROVIDER.getClass().getName());
      }
      PROVIDER = provider;
      ACCESS_TOKEN = new AccessToken();
      return ACCESS_TOKEN;
    }

    /**
     * Uninstalls the provider.  This does nothing if no provider is installed.
     *
     * @param token The {@link AccessToken} with which the provider was
     *              installed.
     *
     * @throws IllegalStateException If the specifid token is not the expected
     *                               token.
     */
    public static synchronized void uninstallProvider(AccessToken token)
      throws IllegalStateException
    {
      if (ACCESS_TOKEN != null && ACCESS_TOKEN != token) {
        throw new IllegalStateException(
            "The specified access token was not the expected access token.");
      }
      PROVIDER = null;
      ACCESS_TOKEN = null;
    }

    /**
     * Returns the installed {@link SzApiProvider}.  If no provider is installed
     * then an exception is thrown.
     *
     * @return The installed {@link SzApiProvider}.
     *
     * @throws IllegalStateException If no provider is installed.
     */
    public static synchronized SzApiProvider getProvider()
      throws IllegalStateException
    {
      if (PROVIDER == null) {
        IllegalStateException e = new IllegalStateException(
            "No SzApiProvider has been installed.");
        e.printStackTrace();
        throw e;
      }
      return PROVIDER;
    }
  }

  /**
   * Returns a description for the provider.  This is useful for inclusion
   * in the meta data for each response.
   *
   * @return A description for the provider.
   */
  String getDescription();

  /**
   * Returns the associated {@link G2Product} API implementation.
   *
   * @return The associated {@link G2Product} API implementation.
   */
  G2Product getProductApi();

  /**
   * Returns the associated {@link G2Engine} API implementation.
   *
   * @return The associated {@link G2Engine} API implementation.
   */
  G2Engine getEngineApi();

  /**
   * Returns the associated {@link G2Config} API implementation.
   *
   * @return The associated {@link G2Config} API implementation.
   */
  G2Config getConfigApi();

  /**
   * Returns the associated {@link G2ConfigMgr} API implementation.
   * This returns <tt>null</tt> if the configuration is not automatically
   * being picked up as the current default configuration.
   *
   * @return The associated {@link G2ConfigMgr} API implementation, or
   *         <tt>null</tt> if the configuration is not automatically
   *         being picked up as the current default configuration.
   */
  G2ConfigMgr getConfigMgrApi();

  /**
   * Returns the associated {@link G2Diagnostic} API implementation.
   *
   * @return The associated {@link G2Diagnostic} API implementation.
   */
  G2Diagnostic getDiagnosticApi();

  /**
   * Gets the version number of the REST API provider implementation.
   *
   * @return The version number of the REST API provider implementation.
   */
  String getApiProviderVersion();

  /**
   * Gets the version number of the REST API specification implemented by
   * this provider.
   *
   * @return The version number of the REST API specification implemented by
   *         this provider.
   */
  String getRestApiVersion();

  /**
   * Gets the version for the underlying runtime native Senzing API.
   *
   * @return The version for the underlying runtime native Senzing API.
   */
  String getNativeApiVersion();

  /**
   * Gets the build version for the underlying runtime native Senzing API.
   *
   * @return The build version for the underlying runtime native Senzing API.
   */
  String getNativeApiBuildVersion();

  /**
   * Gets the build number for the underlying runtime native Senzing API.
   *
   * @return The build number for the underlying runtime native Senzing API.
   */
  String getNativeApiBuildNumber();

  /**
   * Gets the build date for the underlying runtime native Senzing API.
   *
   * @return The build date for the underlying runtime native Senzing API.
   */
  Date getNativeApiBuildDate();

  /**
   * Gets the configuration compatibility version for the underlying runtime
   * native Senzing API.
   *
   * @return The configuration compatibility version for the underlying runtime
   *         native Senzing API.
   */
  String getConfigCompatibilityVersion();

  /**
   * Returns the concurrency for the API provider.  This is the number
   * of initialized threads to do work against the Senzing repository.
   * This method returns
   *
   * @return The maximum concurrency for the API provider, or zero (0) if
   *         the number of threads is unknown.
   */
  int getConcurrency();

  /**
   * Returns the base path for the REST API.  Typically this is <tt>"/"</tt>,
   * but it may be a sub-path.
   *
   * @return The base path for the REST API.
   */
  String getBasePath();

  /**
   * Gets the maximum number of bytes for both text and binary web sockets
   * messages.
   *
   * @return The maximum number of bytes for both text and binary web sockets
   *         messages.
   */
  int getWebSocketsMessageMaxSize();

  /**
   * Executes the specified task with the proper thread for utilizing the
   * various G2 API implementations.
   *
   * @param task The Task to execute.
   * @param <T> The return value for the task.
   * @param <E> The exception type that may be thrown by the task.
   * @return Returns an instance of type <tt>T</tt> as obtained from the
   *         specified task.
   * @throws E If the specified task fails with an exception.
   */
  <T, E extends Exception> T executeInThread(WorkerThreadPool.Task<T, E> task)
      throws E;

  /**
   * Called before beginning an operation that may require a prolonged amount of
   * time to complete.  If this returns <tt>null</tt> then the caller should
   * <b>not</b> continue with the operation.  If this returns an {@link
   * AccessToken} instance then the operation is authorized to continue and the
   * caller should call {@link #concludeProlongedOperation(AccessToken)} with
   * the result when the operation is complete.
   *
   * @return The {@link AccessToken} that authorizes the operation or
   *         <tt>null</tt> if the operation is not authorized.
   */
  AccessToken authorizeProlongedOperation();

  /**
   * Called when completing a prolonged operation that was previously
   * {@linkplain #authorizeProlongedOperation() authorized}.  The specified
   * {@link AccessToken} cannot be null.
   *
   * @param token The {@link AccessToken} from the previous {@linkplain
   *              #authorizeProlongedOperation() authorization}.
   *
   * @throws NullPointerException If the specified {@link AccessToken} is
   *                              <tt>null</tt>.
   * @throws IllegalArgumentException If the specified {@link AccessToken} is
   *                                  not recognized from a previous
   *                                  authorization.
   */
  void concludeProlongedOperation(AccessToken token);

  /**
   * Gets the <b>unmodifiable</b> {@Link Set} of Data Source codes that
   * are configured.
   *
   * @param expectedDataSources The zero or more data source codes that the
   *                            caller expects to exist.
   *
   * @return The <b>unmodifiable</b> {@Link Set} of Data Source codes that
   *         are configured.
   */
  Set<String> getDataSources(String... expectedDataSources);

  /**
   * Gets the attribute class associated with a feature type code.
   *
   * @param featureType The feature type code.
   *
   * @return The attribute class for the specified feature type, or
   *         <tt>null</tt> if the specified feature type code is <tt>null</tt>
   *         or not recognized.
   */
  String getAttributeClassForFeature(String featureType);

  /**
   * Checks if the API is running in read-only mode.
   *
   * @return <tt>true</tt> if only read-only endpoints should be allowed,
   *         otherwise <tt>false</tt>.
   */
  boolean isReadOnly();

  /**
   * Checks if the API is running with admin features enabled.
   *
   * @return <tt>true</tt> if admin features are enabled, otherwise
   *         <tt>false</tt>.
   */
  boolean isAdminEnabled();

  /**
   * Checks if there is an info message sink configured.
   *
   * @return <tt>true</tt> if an info message sink is configured, and
   *         <tt>false</tt> if none is configured.
   */
  boolean hasInfoSink();

  /**
   * Gets the {@link SzMessageSink} for sending info messages when records are
   * loaded, deleted or re-evaluated.  This returns <tt>null</tt> if an info
   * queue is not configured.
   *
   * @return The {@link SzMessageSink} for sending the info messages, or
   *         <tt>null</tt> if none is configured.
   */
  SzMessageSink acquireInfoSink();

  /**
   * Releases the specified {@link SzMessageSink} back to the provider when
   * done using it.
   *
   * @param sink The {@link SzMessageSink} to release.
   */
  void releaseInfoSink(SzMessageSink sink);
}
