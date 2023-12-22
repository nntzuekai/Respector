package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzServerInfo;

/**
 * Provides the default implementation of {@link SzServerInfo}.
 */
@JsonDeserialize
public class SzServerInfoImpl implements SzServerInfo  {
  /**
   * The server concurrency.
   */
  private int concurrency;

  /**
   * The active config ID being used by the server or <tt>null</tt>
   */
  private long activeConfigId;

  /**
   * Whether or not the server will automatically pickup the latest
   * default configuration if it changes.
   */
  private boolean dynamicConfig;

  /**
   * Whether or not the server was started in read-only mode.  If in
   * read-only mode then operations that modify the repository (e.g.:
   * loading records or configuring new data sources) are not allowed.
   */
  private boolean readOnly;

  /**
   * Whether or not admin features are enabled.  If admin features are
   * not enabled then the configuration cannot be modified.
   */
  private boolean adminEnabled;

  /**
   * The maximum size for web sockets messages.
   */
  private int webSocketsMessageMaxSize;

  /**
   * Whether or not an asynchronous INFO queue has been configured.
   */
  private boolean infoQueueConfigured;

  /**
   * Default constructor.
   */
  public SzServerInfoImpl() {
    this.concurrency              = 0;
    this.activeConfigId           = 0;
    this.dynamicConfig            = false;
    this.readOnly                 = false;
    this.adminEnabled             = false;
    this.webSocketsMessageMaxSize = 0;
    this.infoQueueConfigured      = false;
  }

  /**
   * Gets the number of Senzing worker threads pooled for handling requests.
   *
   * @return The number of Senzing worker threads pooled for handling requests.
   */
  @Override
  public int getConcurrency() {
    return this.concurrency;
  }

  /**
   * Sets the number of Senzing worker threads pooled for handling requests.
   *
   * @param concurrency The number of Senzing worker threads pooled for
   *                    handling requests.
   */
  @Override
  public void setConcurrency(int concurrency) {
    this.concurrency = concurrency;
  }

  /**
   * The active configuration ID being used by the API server.  This
   * is still available if the server was started with a static file
   * configuration via the `G2CONFIGFILE` initialization property.
   *
   * @return The active configuration ID being used b the API server.
   */
  @Override
  public long getActiveConfigId() {
    return this.activeConfigId;
  }

  /**
   * The active configuration ID being used by the API server.  This
   * is still available if the server was started with a static file
   * configuration via the `G2CONFIGFILE` initialization property.
   *
   * @param activeConfigId The active configuration ID being used by the
   *                       API server.
   */
  @Override
  public void setActiveConfigId(long activeConfigId) {
    this.activeConfigId = activeConfigId;
  }

  /**
   * Checks whether or not the server will automatically pickup the latest
   * default configuration if it changes.
   *
   * @return <tt>true</tt> if the server will automatically pickup the latest
   *         default configuration if it changes, and <tt>false</tt> if the
   *         configuration is static and the server will not recognize changes.
   */
  @Override
  public boolean isDynamicConfig() {
    return this.dynamicConfig;
  }

  /**
   * Sets whether or not the server will automatically pickup the latest
   * default configuration if it changes.
   *
   * @param dynamicConfig <tt>true</tt> if the server will automatically pickup
   *                      the latest default configuration if it changes, and
   *                      <tt>false</tt> if the configuration is static and the
   *                      server will not recognize changes.
   */
  @Override
  public void setDynamicConfig(boolean dynamicConfig) {
    this.dynamicConfig = dynamicConfig;
  }

  /**
   * Checks whether or not the server was started in read-only mode.  If in
   * read-only mode then operations that modify the repository (e.g.: loading
   * records or configuring new data sources) are not allowed.
   *
   * @return <tt>true</tt> if the server was started in read-only mode,
   *         and <tt>false</tt> if write operations are allowed.
   */
  @Override
  public boolean isReadOnly() {
    return this.readOnly;
  }

  /**
   * Sets whether or not the server was started in read-only mode.  If in
   * read-only mode then operations that modify the repository (e.g.: loading
   * records or configuring new data sources) are not allowed.
   *
   * @param readOnly <tt>true</tt> if the server was started in read-only mode,
   *                 and <tt>false</tt> if write operations are allowed.
   */
  @Override
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  /**
   * Checks whether or not admin features are enabled.  If admin features are
   * not enabled then the configuration cannot be modified.
   *
   * @return <tt>true</tt> if admin features are enabled, otherwise
   *         <tt>false</tt>.
   */
  @Override
  public boolean isAdminEnabled() {
    return this.adminEnabled;
  }

  /**
   * Sets whether or not admin features are enabled.  If admin features are
   * not enabled then the configuration cannot be modified.
   *
   * @param adminEnabled <tt>true</tt> if admin features are enabled, otherwise
   *                     <tt>false</tt>.
   */
  @Override
  public void setAdminEnabled(boolean adminEnabled) {
    this.adminEnabled = adminEnabled;
  }

  /**
   * Gets the maximum number of bytes for both text and binary web sockets
   * messages.
   *
   * @return The maximum number of bytes for both text and binary web sockets
   *         messages.
   */
  @Override
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
  @Override
  public void setWebSocketsMessageMaxSize(int webSocketsMessageMaxSize) {
    this.webSocketsMessageMaxSize = webSocketsMessageMaxSize;
  }

  /**
   * Checks if an asynchronous INFO queue has been configured for load,
   * reevaluate and delete operations.
   *
   * @return <tt>true</tt> if an asynchronous INFO queue has been configured,
   *         otherwise <tt>false</tt>.
   */
  @Override
  public boolean isInfoQueueConfigured() {
    return this.infoQueueConfigured;
  }

  /**
   * Sets whether or not an asynchronous INFO queue has been configured for
   * load, reevaluate and delete operations.
   *
   * @param configured <tt>true</tt> if an asynchronous INFO queue has been
   *                   configured, and <tt>false</tt> if not.
   */
  @Override
  public void setInfoQueueConfigured(boolean configured) {
    this.infoQueueConfigured = configured;
  }

}
