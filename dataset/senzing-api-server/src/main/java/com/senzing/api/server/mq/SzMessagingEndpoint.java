package com.senzing.api.server.mq;

import com.senzing.api.services.SzMessage;
import com.senzing.api.services.SzMessageSink;
import com.senzing.api.server.SzApiServer;

import java.util.Map;
import java.util.Set;

/**
 * Represents an established connection to a messaging endpoint.
 */
public interface SzMessagingEndpoint extends SzMessageSink, AutoCloseable {
  /**
   * An interface for creating endpoint instances from a URL.
   */
  interface Initiator {
    /**
     * Given the specified {@link Map} of {@link String} keys to {@lnk Object}
     * values this method will create an instance of {@link SzMessagingEndpoint}
     * if the specified URL is in the format expected for this builder.  If the
     * specified URL format is not handled by this instance then <tt>null</tt>
     * is returned.
     *
     * @param props The {@link Map} of {@link String} keys to {@link Object}
     *              values to use for establishing the endpoint conenction.
     *
     * @param concurrency The concurrency of the {@link SzApiServer} to use for
     *                    creating pooled resources.
     *
     * @return The {@link SzMessagingEndpoint} created for the specified
     *         properties, or <tt>null</tt> if the specified properties are not
     *         handled by this instance.
     */
    SzMessagingEndpoint establish(Map<String, ?> props, int concurrency);

    /**
     * Gets the <b>unmodifiable</b> {@link Set} of property keys recognized by
     * this inititator.
     *
     * @return The <b>unmodifiable</b> {@link Set} of property keys recognized
     *         by this inititator.
     */
    Set<String> getPropertyKeys();
  }

  /**
   * Acquires an {@link SzMessageSink} interface to this endpoint to be used
   * on the current thread.
   *
   * @return The acquired {@link SzMessageSink} to this instance.
   *
   * @throws IllegalStateException If an {@link SzMessageSink} was already
   *                               acquired on this thread, but not released.
   */
  SzMessageSink acquireMessageSink() throws IllegalStateException;

  /**
   * Releases the {@link SzMessageSink} that was obtained on this thread.
   *
   * @param sink The {@link SzMessageSink} that was previously acquired on this
   *             thread that should be released.
   *
   * @throws IllegalStateException If an {@link SzMessageSink} was not
   *                               previously acquired on this thread or if the
   *                               previously acquired {@link SzMessageSink}
   *                               was a different reference.
   */
  void releaseMessageSink(SzMessageSink sink) throws IllegalStateException;

  /**
   * Closes the endpoint.
   *
   * @throws Exception If a failure occurs.
   */
  void close() throws Exception;

  /**
   * Checks if this endpoint has been closed.
   */
  boolean isClosed();
}
