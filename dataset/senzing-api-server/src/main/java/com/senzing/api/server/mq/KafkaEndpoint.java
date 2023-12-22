package com.senzing.api.server.mq;

import com.senzing.api.services.SzMessage;
import com.senzing.api.services.SzMessageSink;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.senzing.io.IOUtilities.UTF_8;

/**
 * Provides a Kafka implementation of {@link SzMessageSink}.
 *
 * The Kafka initialization URL looks like this:
 * <pre>
 *  kafka://{host}:{port}/{topic}[?{prop1}={value1}&{prop2}={value2}]
 * </pre>
 *
 * The optional query-string properties are used to initialize the
 * {@link KafkaProducer}.  If specified, those properties should be
 * single-valued and should omit the <tt>"bootstrap.servers"</tt> property
 * which is specified as the first part of the URL.
 */
public class KafkaEndpoint extends SzAbstractMessagingEndpoint {
  /**
   * The message sink type for Kafka. The value is {@value}.
   */
  public static final String KAFKA_SINK_TYPE = "Kafka";

  /**
   * The {@link Initiator} for the {@link KafkaEndpoint} class.
   */
  public static final Initiator INITIATOR = new KafkaInitiator();

  /**
   * The prefix to use for the initialization properties.
   */
  public static final String PROPERTY_PREFIX = "kafka-";

  /**
   * The property key for the Kafka bootstrap servers.
   */
  public static final String BOOTSTRAP_SERVERS_PROPERTY_KEY
      = PROPERTY_PREFIX + "bootstrap-servers";

  /**
   * The property key for the Kafka group ID.
   */
  public static final String GROUP_ID_PROPERTY_KEY
      = PROPERTY_PREFIX + "group-id";

  /**
   * The property key for the Kafka topic.
   */
  public static final String TOPIC_PROPERTY_KEY = PROPERTY_PREFIX + "topic";

  /**
   * The <b>unmodifiable</b> {@link Set} of {@link String} property keys for
   * creating an {@link KafkaEndpoint} via {@link KafkaInitiator}.
   */
  public static final Set<String> PROPERTY_KEYS
      = Set.of(BOOTSTRAP_SERVERS_PROPERTY_KEY,
               GROUP_ID_PROPERTY_KEY,
               TOPIC_PROPERTY_KEY);

  /**
   * The class name for the string serializer.
   */
  private static final String STRING_SERIALIZER
      = "org.apache.kafka.common.serialization.StringSerializer";

  /**
   * The producer for sending the messages.
   */
  private KafkaProducer<String, String> producer = null;

  /**
   * The topic on which to publish messages.
   */
  private String topic = null;

  /**
   * Constructs with the specified topic and {@link KafkaProducer}.
   *
   * @param topic The topic to construct with.
   * @param producer The producer to construct with.
   */
  public KafkaEndpoint(String topic, KafkaProducer<String, String> producer)
  {
    this.topic = topic;
    this.producer = producer;
  }

  @Override
  public void send(SzMessage message, FailureHandler onFailure)
      throws Exception
  {
    // create the record to send
    ProducerRecord<String, String> record
        = new ProducerRecord<>(this.topic, message.getBody());

    // add any message properties as headers if they exist
    Map<String, String> props = message.getProperties();
    if (props == null && props.size() > 0) {
      props.forEach((key, value)-> {
        try {
          record.headers().add(key, value.getBytes(UTF_8));

        } catch (UnsupportedEncodingException cannotHappen) {
          throw new IllegalStateException(cannotHappen);
        }
      });
    }

    // send the record -- account for immediate and asynchronous exceptions
    Exception[] failure = { null };
    this.producer.send(record, ((recordMetadata, exception) -> {
      // check if a handler is defined
      if (onFailure != null) {
        try {
          // handle the failure
          onFailure.handle(exception, message);

        } catch (Exception ignore) {
          ignore.printStackTrace();
        }
      }

      // set the failure as an exception object in case this was blocking
      failure[0] = exception;
    }));

    // check if non-null and rethrow (usually not the case)
    if (failure[0] != null) throw failure[0];
  }

  /**
   * Handles closing the underling {@link KafkaProducer} object.
   *
   * @throws Exception If a failure occurs.
   */
  protected void doClose() throws Exception {
    this.producer.close();
  }

  /**
   * Provides an {@link Initiator} implementation that creates an instance of
   * {@link KafkaEndpoint} from a URL.
   */
  private static class KafkaInitiator implements SzMessagingEndpoint.Initiator {
    /**
     * Default constructor.
     */
    private KafkaInitiator() {
      // do nothing
    }

    /**
     * Handles establishing a Kafka connection.
     */
    @Override
    public SzMessagingEndpoint establish(Map<String, ?> props,
                                         int            concurrency)
    {
      if (props == null) return null;
      Set<String> propKeys = this.getPropertyKeys();
      int count = 0;
      for (String key: propKeys) {
        if (props.containsKey(key)) count++;
      }
      if (count == 0) return null;
      String servers = (String) props.get(BOOTSTRAP_SERVERS_PROPERTY_KEY);
      String groupId = (String) props.get(GROUP_ID_PROPERTY_KEY);
      String topic   = (String) props.get(TOPIC_PROPERTY_KEY);

      // check if the servers and topic are provided
      if (servers == null || topic == null) {
        throw new IllegalArgumentException(
            "The bootstrap servers and topic properties are required for a "
            + "Kafka connection.  props=[ " + props + " ]");
      }

      // create the kafka properties object
      Properties kafkaProps  = new Properties();

      kafkaProps.put("bootstrap.servers", servers);
      kafkaProps.put("group.id", groupId);
      kafkaProps.put("key.serializer", STRING_SERIALIZER);
      kafkaProps.put("value.serializer", STRING_SERIALIZER);

      // create the producer
      KafkaProducer<String,String> producer = new KafkaProducer<>(kafkaProps);

      // return the endpoint
      return new KafkaEndpoint(topic, producer);
    }

    /**
     * Implemented to return {@link #PROPERTY_KEYS}.
     */
    @Override
    public Set<String> getPropertyKeys() {
      return PROPERTY_KEYS;
    }
  }

  /**
   * Implemented to return {@link #KAFKA_SINK_TYPE}.
   * {@inheritDoc}
   */
  @Override
  public String getProviderType() {
    return KAFKA_SINK_TYPE;
  }

}
