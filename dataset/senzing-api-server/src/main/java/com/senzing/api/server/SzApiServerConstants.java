package com.senzing.api.server;

import java.util.Set;

/**
 * Utility class to provide common constants pertaining to the Senzing API
 * Server.  These are factored into their own class to avoid circular
 * dependencies.
 */
public final class SzApiServerConstants {
  /**
   * The default port for the API Server ({@value}).
   */
  public static final int DEFAULT_PORT = 2080;

  /**
   * The default port as a string.
   */
  static final String DEFAULT_PORT_PARAM = String.valueOf(DEFAULT_PORT);

  /**
   * The default secure port for the API Server ({@value}).
   */
  public static final int DEFAULT_SECURE_PORT = 2443;

  /**
   * The default secure port as a string.
   */
  static final String DEFAULT_SECURE_PORT_PARAM
      = String.valueOf(DEFAULT_SECURE_PORT);

  /**
   * The default bind address option ({@value}).
   */
  public static final String DEFAULT_BIND_ADDRESS = "loopback";

  /**
   * The default module name ({@value}).
   */
  public static final String DEFAULT_MODULE_NAME = "senzing-api-server";

  /**
   * The default concurrency setting used by API server instances if
   * an explicit concurrency is not provided.  The default value is {@value}.
   */
  public static final int DEFAULT_CONCURRENCY = 8;

  /**
   * The default concurrency as a string.
   */
  static final String DEFAULT_CONCURRENCY_PARAM
      = String.valueOf(DEFAULT_CONCURRENCY);


  /**
   * The default number of threads for the web server thread pool.
   */
  public static final int DEFAULT_HTTP_CONCURRENCY = 200;

  /**
   * The minimum number of threads for the web server thread pool.
   */
  public static final int MINIMUM_HTTP_CONCURRENCY = 10;

  /**
   * THe default HTTP concurrency as a string.
   */
  static final String DEFAULT_HTTP_CONCURRENCY_PARAM
      = String.valueOf(DEFAULT_HTTP_CONCURRENCY);

  /**
   * The default stats interval for logging stats.  This is the default
   * minimum period of time between logging of stats.  The actual interval
   * may be longer if the API Server is idle or not performing activities
   * related to entity scoring (i.e.: activities that would affect stats).
   * The default is every fifteen minutes.
   */
  public static final long DEFAULT_STATS_INTERVAL = 1000L * 60L * 15L;

  /**
   * The default stats interval as a string.
   */
  static final String DEFAULT_STATS_INTERVAL_PARAM
      = String.valueOf(DEFAULT_STATS_INTERVAL);

  /**
   * The number of milliseconds to wait in between checking for changes in the
   * configuration and automatically refreshing the configuration.
   */
  public static final long DEFAULT_CONFIG_REFRESH_PERIOD = 10000;

  /**
   * The maximum size for a message sent in web sockets.
   */
  public static final int WEB_SOCKETS_MESSAGE_MAX_SIZE = 1024*1024*10;

  /**
   * The config auto refresh period as a string.
   */
  static final String DEFAULT_CONFIG_REFRESH_PERIOD_PARAM
      = String.valueOf(DEFAULT_CONFIG_REFRESH_PERIOD);

  /**
   * The {@link SzApiServerOption} group for the RabbitMQ info queue options.
   */
  static final String RABBITMQ_INFO_QUEUE_GROUP = "rabbitmq-info";

  /**
   * The {@link SzApiServerOption} group for the Kafka info queue options.
   */
  static final String KAFKA_INFO_QUEUE_GROUP = "kafka-info";

  /**
   * The {@link SzApiServerOption} group for the SQS info queue options.
   */
  static final String SQS_INFO_QUEUE_GROUP = "sqs-info";

  /***
   * The <b>unmodifiable</b> {@link Set} of group names for info queue groups.
   */
  static final Set<String> INFO_QUEUE_GROUPS = Set.of(
      RABBITMQ_INFO_QUEUE_GROUP, KAFKA_INFO_QUEUE_GROUP, SQS_INFO_QUEUE_GROUP);

  /**
   * The prefix for environment variables used that are specific to the
   * Senzing REST API Server.
   */
  static final String ENV_PREFIX = "SENZING_API_SERVER_";
}
