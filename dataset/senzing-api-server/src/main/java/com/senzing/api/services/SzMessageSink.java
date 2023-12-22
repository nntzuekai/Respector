package com.senzing.api.services;

import java.util.function.BiConsumer;

/**
 * The message sink for sending messages on the associated queue or topic.
 */
public interface SzMessageSink {
  /**
   * Extends {@link BiConsumer} to define a function to handle failures.
   * The function will take an {@link Exception} and {@link SzMessage} as
   * arguments.
   */
  interface FailureHandler {
    /**
     * Handles the exception that occurred when trying to send the specified
     * message.
     * @param exception The exception that occurred.
     * @param message The message that was sent, triggering the exception.
     * @throws Exception If the exception is being rethrown.
     */
    void handle(Exception exception, SzMessage message)
      throws Exception;
  }

  /**
   * Sends the specified {@link SzMessage} on the associated queue or topic.
   * The optional on-failure function allows for a callback for asynchronous
   * failures to be registered properly.
   *
   * @param message The {@link SzMessage} to be sent.
   *
   * @param onFailure The function to call upon failure to send the message,
   *                  or <tt>null</tt> if none.
   *
   * @throws Exception If a failure occurs in sending the message.
   */
  void send(SzMessage message, FailureHandler onFailure)
      throws Exception;

  /**
   * Gets a {@link String} describing the provider for the queue.
   *
   * @return A {@link String} describing the provider for the queue.
   */
  String getProviderType();

  /**
   * Gets the (likely approximate) number of messages currently on the {@link
   * SzMessageSink} queue that have not been processed.  If this is not known
   * for this type of queue then <tt>null</tt> is returned.
   *
   * @return The (likely approximate) number of messages on the {@link
   *         SzMessageSink} queue, or <tt>null</tt> if not known.
   */
  Integer getMessageCount();
}
