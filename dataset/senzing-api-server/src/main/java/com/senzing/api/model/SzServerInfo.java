package com.senzing.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzServerInfoImpl;

/**
 * Describes the server features and state.
 */
@JsonDeserialize(using=SzServerInfo.Factory.class)
public interface SzServerInfo {
  /**
   * Gets the number of Senzing worker threads pooled for handling requests.
   *
   * @return The number of Senzing worker threads pooled for handling requests.
   */
  int getConcurrency();

  /**
   * Sets the number of Senzing worker threads pooled for handling requests.
   *
   * @param concurrency The number of Senzing worker threads pooled for
   *                    handling requests.
   */
  void setConcurrency(int concurrency);

  /**
   * The active configuration ID being used by the API server.  This
   * is still available if the server was started with a static file
   * configuration via the `G2CONFIGFILE` initialization property.
   *
   * @return The active configuration ID being used b the API server.
   */
  long getActiveConfigId();

  /**
   * The active configuration ID being used by the API server.  This
   * is still available if the server was started with a static file
   * configuration via the `G2CONFIGFILE` initialization property.
   *
   * @param activeConfigId The active configuration ID being used by the
   *                       API server.
   */
  void setActiveConfigId(long activeConfigId);

  /**
   * Checks whether or not the server will automatically pickup the latest
   * default configuration if it changes.
   *
   * @return <tt>true</tt> if the server will automatically pickup the latest
   *         default configuration if it changes, and <tt>false</tt> if the
   *         configuration is static and the server will not recognize changes.
   */
  boolean isDynamicConfig();

  /**
   * Sets whether or not the server will automatically pickup the latest
   * default configuration if it changes.
   *
   * @param dynamicConfig <tt>true</tt> if the server will automatically pickup
   *                      the latest default configuration if it changes, and
   *                      <tt>false</tt> if the configuration is static and the
   *                      server will not recognize changes.
   */
  void setDynamicConfig(boolean dynamicConfig);

  /**
   * Checks whether or not the server was started in read-only mode.  If in
   * read-only mode then operations that modify the repository (e.g.: loading
   * records or configuring new data sources) are not allowed.
   *
   * @return <tt>true</tt> if the server was started in read-only mode,
   *         and <tt>false</tt> if write operations are allowed.
   */
  boolean isReadOnly();

  /**
   * Sets whether or not the server was started in read-only mode.  If in
   * read-only mode then operations that modify the repository (e.g.: loading
   * records or configuring new data sources) are not allowed.
   *
   * @param readOnly <tt>true</tt> if the server was started in read-only mode,
   *                 and <tt>false</tt> if write operations are allowed.
   */
  void setReadOnly(boolean readOnly);

  /**
   * Checks whether or not admin features are enabled.  If admin features are
   * not enabled then the configuration cannot be modified.
   *
   * @return <tt>true</tt> if admin features are enabled, otherwise
   *         <tt>false</tt>.
   */
  boolean isAdminEnabled();

  /**
   * Sets whether or not admin features are enabled.  If admin features are
   * not enabled then the configuration cannot be modified.
   *
   * @param adminEnabled <tt>true</tt> if admin features are enabled, otherwise
   *                     <tt>false</tt>.
   */
  void setAdminEnabled(boolean adminEnabled);

  /**
   * Gets the maximum number of bytes for both text and binary web sockets
   * messages.
   *
   * @return The maximum number of bytes for both text and binary web sockets
   *         messages.
   */
  int getWebSocketsMessageMaxSize();

  /**
   * Sets the maximum number of bytes for both text and binary web sockets
   * messages.
   *
   * @param webSocketsMessageMaxSize The maximum number of bytes for both text
   *                                 and binary web sockets messages.
   */
  void setWebSocketsMessageMaxSize(int webSocketsMessageMaxSize);

  /**
   * Checks if an asynchronous INFO queue has been configured for load,
   * reevaluate and delete operations.
   *
   * @return <tt>true</tt> if an asynchronous INFO queue has been configured,
   *         otherwise <tt>false</tt>.
   */
  boolean isInfoQueueConfigured();

  /**
   * Sets whether or not an asynchronous INFO queue has been configured for
   * load, reevaluate and delete operations.
   *
   * @param configured <tt>true</tt> if an asynchronous INFO queue has been
   *                   configured, and <tt>false</tt> if not.
   */
  void setInfoQueueConfigured(boolean configured);

  /**
   * A {@link ModelProvider} for instances of {@link SzServerInfo}.
   */
  interface Provider extends ModelProvider<SzServerInfo> {
    /**
     * Creates a new instance of {@link SzServerInfo}.
     *
     * @return The new instance of {@link SzServerInfo}
     */
    SzServerInfo create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzServerInfo} that produces instances of {@link SzServerInfoImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzServerInfo>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzServerInfo.class, SzServerInfoImpl.class);
    }

    @Override
    public SzServerInfo create() {
      return new SzServerInfoImpl();
    }
  }

    /**
   * Provides a {@link ModelFactory} implementation for {@link SzServerInfo}.
   */
  class Factory extends ModelFactory<SzServerInfo, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzServerInfo.class);
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
     * Creates a new instance of {@link SzServerInfo}.
     * @return The new instance of {@link SzServerInfo}.
     */
    public SzServerInfo create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
