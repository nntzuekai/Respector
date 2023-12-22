package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzVersionInfoImpl;
import com.senzing.util.JsonUtilities;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Describes the version information associated with the API Server, the
 * Senzing REST API Specification that is implemented and the native Senzing
 * API.
 */
@JsonDeserialize(using=SzVersionInfo.Factory.class)
public interface SzVersionInfo {
  /**
   * The {@link String} token used to identify development builds when parsing
   * the version info.
   */
  String DEVELOPMENT_VERSION_TOKEN = "DEVELOPMENT_VERSION";

  /**
   * The date-time pattern for the build number.
   */
  String BUILD_NUMBER_PATTERN = "yyyy_MM_dd__HH_mm";

  /**
   * The time zone used for the time component of the build number.
   */
  ZoneId BUILD_ZONE = ZoneId.of("America/Los_Angeles");

  /**
   * The {@link DateTimeFormatter} for interpreting the build number as a
   * LocalDateTime instance.
   */
  DateTimeFormatter BUILD_NUMBER_FORMATTER
      = DateTimeFormatter.ofPattern(BUILD_NUMBER_PATTERN);

  /**
   * Gets the version of the REST API Server implementation.
   *
   * @return The version of the REST API Server implementation.
   */
  String getApiServerVersion();

  /**
   * Gets the version of the REST API Specification that is implemented.
   *
   * @return The version of the REST API Specification that is implemented.
   */
  String getRestApiVersion();

  /**
   * Gets the version for the underlying runtime native Senzing API.
   *
   * @return The version for the underlying runtime native Senzing API.
   */
  String getNativeApiVersion();

  /**
   * Sets the version for the underlying runtime native Senzing API.
   *
   * @param nativeApiVersion Sets the version for the underlying runtime
   *                         native Senzing API.
   */
  void setNativeApiVersion(String nativeApiVersion);

  /**
   * Gets the build version for the underlying runtime native Senzing API.
   *
   * @return The build version for the underlying runtime native Senzing API.
   */
  String getNativeApiBuildVersion();

  /**
   * Sets the build version for the underlying runtime native Senzing API.
   *
   * @param nativeApiBuildVersion The build version for the underlying runtime
   *                              native Senzing API.
   */
  void setNativeApiBuildVersion(String nativeApiBuildVersion);

  /**
   * Gets the build number for the underlying runtime native Senzing API.
   *
   * @return The build number for the underlying runtime native Senzing API.
   */
  String getNativeApiBuildNumber();

  /**
   * Sets the build number for the underlying runtime native Senzing API.
   *
   * @param nativeApiBuildNumber The build number for the underlying runtime
   *                             native Senzing API.
   */
  void setNativeApiBuildNumber(String nativeApiBuildNumber);

  /**
   * Gets the build date for the underlying runtime native Senzing API.
   *
   * @return The build date for the underlying runtime native Senzing API.
   */
  @JsonFormat(shape   = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
      locale  = "en_GB")
  Date getNativeApiBuildDate();

  /**
   * Sets the build date for the underlying runtime native Senzing API.
   *
   * @param nativeApiBuildDate The build date for the underlying runtime
   *                           native Senzing API.
   */
  void setNativeApiBuildDate(Date nativeApiBuildDate);

  /**
   * Gets the configuration compatibility version for the underlying runtime
   * native Senzing API.
   *
   * @return The configuration compatibility version for the underlying runtime
   *         native Senzing API.
   */
  String getConfigCompatibilityVersion();

  /**
   * Sets the configuration compatibility version for the underlying runtime
   * native Senzing API.
   *
   * @param configCompatibilityVersion The configuration compatibility version
   *                                   for the underlying runtime native
   *                                   Senzing API.
   */
  void setConfigCompatibilityVersion(String configCompatibilityVersion);

  /**
   * A {@link ModelProvider} for instances of {@link SzVersionInfo}.
   */
  interface Provider extends ModelProvider<SzVersionInfo> {
    /**
     * Creates a new instance of {@link SzVersionInfo}.
     *
     * @return The new instance of {@link SzVersionInfo}
     */
    SzVersionInfo create();
  }

  /**
   * Provides a default {@link Provider} implementation for {@link
   * SzVersionInfo} that produces instances of {@link SzVersionInfoImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzVersionInfo>
      implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzVersionInfo.class, SzVersionInfoImpl.class);
    }

    @Override
    public SzVersionInfo create() {
      return new SzVersionInfoImpl();
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzVersionInfo}.
   */
  class Factory extends ModelFactory<SzVersionInfo, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzVersionInfo.class);
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
     * Creates a new instance of {@link SzVersionInfo}.
     * @return The new instance of {@link SzVersionInfo}.
     */
    public SzVersionInfo create()
    {
      return this.getProvider().create();
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());

  /**
   * Parses a JSON array of the engine API JSON to create or populate a
   * {@link List} of {@link SzVersionInfo} instances.
   *
   * @param list The {@link List} to populate or <tt>null</tt> if a new
   *             {@link List} should be created.
   *
   * @param jsonArray The {@link JsonArray} of {@link JsonObject} instances
   *                  to parse from the engine API.
   *
   * @return An unmodifiable view of the specified (or newly created) {@link
   *         List} of {@link SzVersionInfo} instances.
   */
  static List<SzVersionInfo> parseVersionInfoList(
      List<SzVersionInfo>   list,
      JsonArray             jsonArray)
  {
    if (list == null) {
      list = new ArrayList<>(jsonArray.size());
    }
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      list.add(parseVersionInfo(null, jsonObject));
    }
    return list;
  }

  /**
   * Parses the engine API JSON to create an instance of {@link SzVersionInfo}.
   *
   * @param info The {@link SzVersionInfo} object to initialize or <tt>null</tt>
   *             if a new one should be created.
   *
   * @param jsonObject The {@link JsonObject} to parse from the engine API.
   *
   * @return The specified (or newly created) {@link SzVersionInfo}
   */
  static SzVersionInfo parseVersionInfo(SzVersionInfo info,
                                        JsonObject    jsonObject)
  {
    if (info == null) info = SzVersionInfo.FACTORY.create();

    String nativeVersion = JsonUtilities.getString(jsonObject, "VERSION");
    String buildVersion  = JsonUtilities.getString(jsonObject, "BUILD_VERSION");
    String buildNumber   = JsonUtilities.getString(jsonObject, "BUILD_NUMBER");

    JsonObject compatVersion
        = JsonUtilities.getJsonObject(jsonObject, "COMPATIBILITY_VERSION");

    String configCompatVersion = JsonUtilities.getString(compatVersion,
                                                     "CONFIG_VERSION");

    Date buildDate = null;
    if (buildNumber != null && buildNumber.length() > 0
        && buildNumber.indexOf(DEVELOPMENT_VERSION_TOKEN) < 0)
    {
      LocalDateTime localDateTime = LocalDateTime.parse(buildNumber,
                                                        BUILD_NUMBER_FORMATTER);
      ZonedDateTime zonedDateTime = localDateTime.atZone(BUILD_ZONE);
      buildDate = Date.from(zonedDateTime.toInstant());

    } else {
      buildDate = new Date();
    }

    info.setConfigCompatibilityVersion(configCompatVersion);
    info.setNativeApiVersion(nativeVersion);
    info.setNativeApiBuildDate(buildDate);
    info.setNativeApiBuildVersion(buildVersion);
    info.setNativeApiBuildNumber(buildNumber);

    return info;
  }
}
