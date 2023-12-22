package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.BuildInfo;
import com.senzing.api.model.SzHttpMethod;
import com.senzing.api.model.SzMeta;
import com.senzing.api.services.SzApiProvider;
import com.senzing.util.Timers;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * Provides a default implementation of {@link SzMeta}.
 */
@JsonDeserialize
public class SzMetaImpl implements SzMeta {
  /**
   * The description of the server / provider that produced the response.
   */
  private String server;

  /**
   * The HTTP method that was executed.
   */
  private SzHttpMethod httpMethod;

  /**
   * The HTTP response code.
   */
  private int httpStatusCode;

  /**
   * The timings for the operation.
   */
  private Timers timers;

  /**
   * The cached {@link Map} of timings which once initialized is not modified.
   */
  private Map<String,Long> timings;

  /**
   * The timestamp associated with the response.
   */
  private Date timestamp;

  /**
   * The server version number for the response.
   */
  private String version;

  /**
   * The Senzing REST API version implemented by the server.
   */
  private String restApiVersion;

  /**
   * The version for the underlying runtime native Senzing API
   */
  private String nativeApiVersion = null;

  /**
   * The build version for the underlying runtime native Senzing API.
   */
  private String nativeApiBuildVersion = null;

  /**
   * The build number for the underlying runtime native Senzing API.
   */
  private String nativeApiBuildNumber = null;

  /**
   * The build date associated with the underlying runtime native API.
   */
  private Date nativeApiBuildDate = null;

  /**
   * The configuration compatibility version for the underlying runtime
   * native Senzing API.
   */
  private String configCompatibilityVersion = null;

  /**
   * Default constructor for reconstructing from JSON.
   */
  protected SzMetaImpl() {
    this.server         = null;
    this.httpMethod     = null;
    this.httpStatusCode = 0;
    this.timestamp      = null;
    this.timers         = null;
    this.timings        = null;
    this.version        = null;
    this.restApiVersion = null;
  }

  /**
   * Constructs with the specified HTTP method.
   *
   * @param httpMethod The HTTP method with which to construct.
   *
   * @param httpStatusCode The HTTP response code.
   */
  public SzMetaImpl(SzHttpMethod httpMethod, int httpStatusCode, Timers timers) {
    this.httpMethod     = httpMethod;
    this.httpStatusCode = httpStatusCode;
    this.timestamp      = new Date();
    this.timers         = timers;
    this.timings        = null;
    this.version        = BuildInfo.MAVEN_VERSION;
    this.restApiVersion = BuildInfo.REST_API_VERSION;

    SzApiProvider prov = null;
    try {
      prov = SzApiProvider.Factory.getProvider();
    } catch (IllegalStateException ignore) {
      // ignore the exception
    }
    if (prov != null) {
      this.server                     = prov.getDescription();
      this.nativeApiVersion           = prov.getNativeApiVersion();
      this.nativeApiBuildVersion      = prov.getNativeApiBuildVersion();
      this.nativeApiBuildNumber       = prov.getNativeApiBuildNumber();
      this.nativeApiBuildDate         = prov.getNativeApiBuildDate();
      this.configCompatibilityVersion = prov.getConfigCompatibilityVersion();
    }
  }

  /**
   * Gets the description of the server/provider that produced the response.
   *
   * @return The description of the server/provider that produced the response.
   */
  @Override
  public String getServer() { return this.server; }

  /**
   * The HTTP method for the REST request.
   *
   * @return HTTP method for the REST request.
   */
  @Override
  public SzHttpMethod getHttpMethod() {
    return this.httpMethod;
  }

  /**
   * The HTTP response status code for the REST request.
   *
   * @return The HTTP response status code for the REST request.
   */
  @Override
  public int getHttpStatusCode() { return this.httpStatusCode; }

  /**
   * Returns the timestamp that the request was completed.
   *
   * @return The timestamp that the request was completed.
   */
  @Override
  public Date getTimestamp() {
    return this.timestamp;
  }

  /**
   * Returns the build version of the server implementation.
   *
   * @return The build version of the server implementation.
   */
  @Override
  public String getVersion() { return this.version; }

  /**
   * Returns the Senzing REST API version implemented by the server.
   *
   * @return The Senzing REST API version implemented by the server.
   */
  @Override
  public String getRestApiVersion() { return this.restApiVersion; }

  /**
   * Gets the version for the underlying runtime native Senzing API.
   *
   * @return The version for the underlying runtime native Senzing API.
   */
  @Override
  public String getNativeApiVersion() {
    return this.nativeApiVersion;
  }

  /**
   * Gets the build version for the underlying runtime native Senzing API.
   *
   * @return The build version for the underlying runtime native Senzing API.
   */
  @Override
  public String getNativeApiBuildVersion() {
    return this.nativeApiBuildVersion;
  }

  /**
   * Gets the build number for the underlying runtime native Senzing API.
   *
   * @return The build number for the underlying runtime native Senzing API.
   */
  @Override
  public String getNativeApiBuildNumber() {
    return this.nativeApiBuildNumber;
  }

  /**
   * Gets the build date for the underlying runtime native Senzing API.
   *
   * @return The build date for the underlying runtime native Senzing API.
   */
  @Override
  public Date getNativeApiBuildDate() {
    return this.nativeApiBuildDate;
  }

  /**
   * Gets the configuration compatibility version for the underlying runtime
   * native Senzing API.
   *
   * @return The configuration compatibility version for the underlying runtime
   *         native Senzing API.
   */
  @Override
  public String getConfigCompatibilityVersion() {
    return this.configCompatibilityVersion;
  }

  /**
   * Returns the timings that were recorded for the operation as an
   * unmodifiable {@link Map} of {@link String} keys to {@link Long}
   * millisecond values.
   *
   * @return The timings that were recorded for the operation.
   */
  @Override
  public Map<String, Long> getTimings() {
    try {
      if (this.timings != null) {
        return this.timings.size() == 0 ? null : this.timings;
      }
      if (this.timers == null) {
        this.timings = Collections.emptyMap();
      } else {
        this.timings = Collections.unmodifiableMap(this.timers.getTimings());
      }
      return this.timings.size() == 0 ? null : this.timings;

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * If any of the response's timers are still accumulating time, this
   * causes them to cease.  Generally, this is only used in testing since
   * converting the object to JSON to serialize the response will trigger
   * a call to {@link #getTimings()} which will have the effect of concluding
   * all timers.
   *
   * If the timers are already concluded then this method does nothing.
   */
  @Override
  public void concludeTimers() {
    if (this.timings != null) return;
    this.getTimings();
  }

}
