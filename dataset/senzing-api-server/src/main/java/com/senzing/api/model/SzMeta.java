package com.senzing.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.impl.SzMetaImpl;
import com.senzing.util.Timers;

import java.util.*;

/**
 * Describes the meta-data section for each response.
 */
@JsonDeserialize(using=SzMeta.Factory.class)
public interface SzMeta {
  /**
   * Gets the description of the server/provider that produced the response.
   *
   * @return The description of the server/provider that produced the response.
   */
  String getServer();

  /**
   * The HTTP method for the REST request.
   *
   * @return HTTP method for the REST request.
   */
  SzHttpMethod getHttpMethod();

  /**
   * The HTTP response status code for the REST request.
   *
   * @return The HTTP response status code for the REST request.
   */
  int getHttpStatusCode();

  /**
   * Returns the timestamp that the request was completed.
   *
   * @return The timestamp that the request was completed.
   */
  @JsonFormat(shape = JsonFormat.Shape.STRING,
              pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
              locale = "en_GB")
  Date getTimestamp();

  /**
   * Returns the build version of the server implementation.
   *
   * @return The build version of the server implementation.
   */
  String getVersion();

  /**
   * Returns the Senzing REST API version implemented by the server.
   *
   * @return The Senzing REST API version implemented by the server.
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
  @JsonFormat(shape   = JsonFormat.Shape.STRING,
              pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
              locale  = "en_GB")
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
   * Returns the timings that were recorded for the operation as an
   * unmodifiable {@link Map} of {@link String} keys to {@link Long}
   * millisecond values.
   *
   * @return The timings that were recorded for the operation.
   */
  Map<String, Long> getTimings();

  /**
   * If any of the response's timers are still accumulating time, this
   * causes them to cease.  Generally, this is only used in testing since
   * converting the object to JSON to serialize the response will trigger
   * a call to {@link #getTimings()} which will have the effect of concluding
   * all timers.
   *
   * If the timers are already concluded then this method does nothing.
   */
  void concludeTimers();

  /**
   * A {@link ModelProvider} for instances of {@link SzMeta}.
   */
  interface Provider extends ModelProvider<SzMeta> {
    /**
     * Creates a new instance of {@link SzMeta} with the specified parameters.
     *
     * @param httpMethod The HTTP method with which to construct.
     *
     * @param httpStatusCode The HTTP response code.
     *
     * @param timers The {@link Timers} instance that tracked timing.
     *
     * @return The new instance of {@link SzMeta}
     */
    SzMeta create(SzHttpMethod httpMethod,
                  int          httpStatusCode,
                  Timers       timers);
  }

  /**
   * Provides a default {@link Provider} implementation for {@link SzMeta}
   * that produces instances of {@link SzMetaImpl}.
   */
  class DefaultProvider extends AbstractModelProvider<SzMeta>
    implements Provider
  {
    /**
     * Default constructor.
     */
    public DefaultProvider() {
      super(SzMeta.class, SzMetaImpl.class);
    }

    @Override
    public SzMeta create(SzHttpMethod httpMethod,
                         int          httpStatusCode,
                         Timers       timers) {
      return new SzMetaImpl(httpMethod, httpStatusCode, timers);
    }
  }

  /**
   * Provides a {@link ModelFactory} implementation for {@link SzMeta}.
   */
  class Factory extends ModelFactory<SzMeta, Provider> {
    /**
     * Default constructor.  This is public and can only be called after the
     * singleton master instance is created as it inherits the same state from
     * the master instance.
     */
    public Factory() {
      super(SzMeta.class);
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
     * Creates a new instance of {@link SzMeta} with the specified parameters.
     *
     * @param httpMethod     The HTTP method with which to construct.
     * @param httpStatusCode The HTTP response code.
     * @param timers         The {@link Timers} instance that tracked timing.
     *
     * @return A new instance of {@link SzMeta} created with the specified
     *         parameters.
     */
    public SzMeta create(SzHttpMethod httpMethod,
                         int httpStatusCode,
                         Timers timers)
    {
      return this.getProvider().create(httpMethod, httpStatusCode, timers);
    }
  }

  /**
   * The {@link Factory} instance for this interface.
   */
  Factory FACTORY = new Factory(new DefaultProvider());
}
