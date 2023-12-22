package com.senzing.api.model.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.BuildInfo;
import com.senzing.api.model.SzVersionInfo;
import java.util.Date;

/**
 * Provides the default implementation of {@link SzVersionInfo}.
 */
@JsonDeserialize
public class SzVersionInfoImpl implements SzVersionInfo {
  /**
   * The version of the REST API Server implementation.
   */
  private String apiServerVersion = null;

  /**
   * The version of the REST API that is implemented.
   */
  private String restApiVersion = null;

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
  @JsonFormat(shape   = JsonFormat.Shape.STRING,
              pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
              locale  = "en_GB")
  private Date nativeApiBuildDate = null;

  /**
   * The configuration compatibility version for the underlying runtime
   * native Senzing API.
   */
  private String configCompatibilityVersion = null;

  /**
   * Default constructor.
   */
  public SzVersionInfoImpl() {
    this.apiServerVersion = BuildInfo.MAVEN_VERSION;
    this.restApiVersion   = BuildInfo.REST_API_VERSION;
  }

  /**
   * Gets the version of the REST API Server implementation.
   *
   * @return The version of the REST API Server implementation.
   */
  @Override
  public String getApiServerVersion() {
    return this.apiServerVersion;
  }

  /**
   * Private setter for the version of the REST API Server implementation.
   * This is used for JSON serialization -- otherwise the version cannot be
   * normally set (it is inferred).
   *
   * @param apiServerVersion The version of the REST API Server implementation.
   */
  private void setApiServerVersion(String apiServerVersion) {
    this.apiServerVersion = apiServerVersion;
  }

  /**
   * Gets the version of the REST API Specification that is implemented.
   *
   * @return The version of the REST API Specification that is implemented.
   */
  @Override
  public String getRestApiVersion() {
    return this.restApiVersion;
  }

  /**
   * Private setter for the REST API version implemented by the REST API
   * Server implementation.  This is used for JSON serialization -- otherwise
   * the version cannot be normally set (it is inferred).
   *
   * @param restApiVersion The version of the REST API Specification that is
   *                       implemented.
   */
  private void setRestApiVersion(String restApiVersion) {
    this.restApiVersion = restApiVersion;
  }

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
   * Sets the version for the underlying runtime native Senzing API.
   *
   * @param nativeApiVersion Sets the version for the underlying runtime
   *                         native Senzing API.
   */
  @Override
  public void setNativeApiVersion(String nativeApiVersion) {
    this.nativeApiVersion = nativeApiVersion;
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
   * Sets the build version for the underlying runtime native Senzing API.
   *
   * @param nativeApiBuildVersion The build version for the underlying runtime
   *                              native Senzing API.
   */
  @Override
  public void setNativeApiBuildVersion(String nativeApiBuildVersion) {
    this.nativeApiBuildVersion = nativeApiBuildVersion;
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
   * Sets the build number for the underlying runtime native Senzing API.
   *
   * @param nativeApiBuildNumber The build number for the underlying runtime
   *                             native Senzing API.
   */
  @Override
  public void setNativeApiBuildNumber(String nativeApiBuildNumber) {
    this.nativeApiBuildNumber = nativeApiBuildNumber;
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
   * Sets the build date for the underlying runtime native Senzing API.
   *
   * @param nativeApiBuildDate The build date for the underlying runtime
   *                           native Senzing API.
   */
  @Override
  public void setNativeApiBuildDate(Date nativeApiBuildDate) {
    this.nativeApiBuildDate = nativeApiBuildDate;
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
   * Sets the configuration compatibility version for the underlying runtime
   * native Senzing API.
   *
   * @param configCompatibilityVersion The configuration compatibility version
   *                                   for the underlying runtime native
   *                                   Senzing API.
   */
  @Override
  public void setConfigCompatibilityVersion(String configCompatibilityVersion) {
    this.configCompatibilityVersion = configCompatibilityVersion;
  }
}
