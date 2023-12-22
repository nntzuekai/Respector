package com.senzing.api.server.mq;

import com.senzing.api.services.SzMessage;
import com.senzing.api.services.SzMessageSink;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import static com.senzing.io.IOUtilities.UTF_8;

/**
 * Provides an abstract implementation of {@link SzMessagingEndpoint}.
 */
public abstract class SzAbstractMessagingEndpoint
    implements SzMessagingEndpoint
{
  /**
   * Inner class to provide a facade that limits access to the instance of
   * {@link SzMessagingEndpoint} to the {@link SzMessageSink} functions.
   */
  protected class SinkFacade implements SzMessageSink {
    @Override
    public void send(SzMessage message, FailureHandler onFailure)
      throws Exception
    {
      SzAbstractMessagingEndpoint.this.send(message, onFailure);
    }

    @Override
    public String getProviderType() {
      return SzAbstractMessagingEndpoint.this.getProviderType();
    }

    @Override
    public Integer getMessageCount() {
      return SzAbstractMessagingEndpoint.this.getMessageCount();
    }
  }

  /**
   * The {@link SzMessageSink} that was acquired on the current thread.
   */
  private final ThreadLocal<SzMessageSink> acquiredSink = new ThreadLocal<>();

  /**
   * The timeout for waiting when trying to close the endpoint.
   */
  private static final long CLOSE_TIMEOUT = 3000L;

  /**
   * The maximum amount of time to wait while closing.
   */
  private static final long MAXIMUM_CLOSE_WAIT = 15000L;

  /**
   * Flag to indicate if we are currently closing.
   */
  private boolean closing = false;

  /**
   * Flag indicating if this endpoint has been closed.
   */
  private boolean closed = false;

  /**
   * Flag indicating if an attempt is being made to acquire an
   * {@link SzMessageSink}.
   */
  private boolean acquiring = false;

  /**
   * Count indicating the number of {@link SzMessageSink} instances that
   * are acquired but not yet released.
   */
  private int acquiredCount = 0;

  /**
   * The monitor to use thread safety.
   */
  protected final Object monitor = new Object();

  /**
   * Default constructor.
   */
  protected SzAbstractMessagingEndpoint() {
    // do nothing
  }

  /**
   * Returns the underlying {@link SzMessageSink} interface to this instance.
   *
   * @return The underlying {@link SzMessageSink} interface to this instance.
   *
   * @throws IllegalStateException If the {@link SzMessageSink} was already
   *                               acquired on this thread.
   */
  @Override
  public SzMessageSink acquireMessageSink()
      throws IllegalStateException
  {
    // check if there is a sink already acquired on this thread
    if (this.acquiredSink.get() != null) {
      throw new IllegalStateException(
          "The SzMessageSink was already previously acquired on this thread.");
    }

    synchronized (this.monitor) {
      if (this.closing) {
        throw new IllegalStateException(
            "Endpoint is being closed.  Cannot acquire a message sink.");
      }
      if (this.closed) {
        throw new IllegalStateException(
            "Endpoint is closed.  Cannot acquire a message sink.");
      }

      // flag that we are acquiring a message sink
      this.acquiring = true;
      this.monitor.notifyAll();
    }

    try {
      // acquire the message sink
      SzMessageSink sink = this.doAcquireMessageSink();

      // set the sink
      this.acquiredSink.set(sink);

      // return the sink
      return sink;

    } finally {
      // signal the acquisition end and increase the acquisition count
      synchronized (this.monitor) {
        this.acquiring = false;
        this.acquiredCount++;
        this.monitor.notifyAll();
      }
    }
  }

  /**
   * Gets the {@link SzMessageSink} that was previously acquired on this thread.
   * This returns <tt>null</tt> if no {@link SzMessageSink} was previously
   * acquired on this thread.
   *
   * @return The {@link SzMessageSink} that was previously acquired on this
   *         thread, or <tt>null</tt> if none.
   */
  protected SzMessageSink getAcquiredSink() {
    return this.acquiredSink.get();
  }

  /**
   * Override this method to implement pooling.  The default implementation
   * returns a new {@link SzMessageSink} interface reference to this instance.
   *
   * @return The acquired {@link SzMessageSink}.
   */
  protected SzMessageSink doAcquireMessageSink() {
    return new SinkFacade();
  }

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
  public void releaseMessageSink(SzMessageSink sink)
      throws IllegalStateException
  {
    // get the thread sink
    SzMessageSink threadSink = this.acquiredSink.get();

    // check if there is no thread-local sink
    if (threadSink == null) {
      throw new IllegalStateException(
          "No SzMessageSink has been acquired on this thread.");
    }

    // check if a different thread sink
    if (threadSink != sink) {
      throw new IllegalStateException(
          "A different SzMessageSink was acquired on this thread.");
    }

    // clear the thread-local sink
    this.acquiredSink.set(null);

    // release the sink
    this.doReleaseMessageSink(sink);

    // update the endpoint state
    synchronized (this.monitor) {
      this.acquiredCount--;
      this.monitor.notifyAll();
    }
  };

  /**
   * Override to implement pooling functionality for the {@link SzMessageSink}.
   * The default implementation does nothing.
   *
   * @param sink The {@link SzMessageSink} to be released.
   */
  protected void doReleaseMessageSink(SzMessageSink sink) {
    // do nothing
  }

  /**
   * Parses the specified query string into name/value pairs that are added to
   * the specified {@link Map}.
   *
   * @param queryText The query string to parse.
   */
  protected static Map<String, List<String>> parseQueryString(String queryText)
  {
    Map<String, List<String>> map = new LinkedHashMap<>();
    if (queryText != null) {
      queryText = queryText.substring(1);
      String[] pairs = queryText.split("&");
      for (String pair: pairs) {
        int index = pair.indexOf("=");
        String key = pair;
        String value = "";
        if (index >= 0) {
          key = pair.substring(0, index);
          if (index < pair.length() - 1) {
            value = pair.substring(index + 1);
          }
        }

        // URL decode the values
        try {
          key = URLDecoder.decode(key, UTF_8);
          value = URLDecoder.decode(value, UTF_8);

        } catch (UnsupportedEncodingException cannotHappen) {
          throw new IllegalStateException("UTF-8 Encoding Not Supported");
        }

        // add the value to the properties map
        List<String> list = map.get(key);
        if (list == null) {
          list = new LinkedList<>();
          map.put(key, list);
        }
        list.add(value);
      }
    }

    // return the map
    return map;
  }

  @Override
  public boolean isClosed() {
    synchronized (this.monitor) {
      return this.closed;
    }
  }

  /**
   * Closes the endpoint.
   *
   * @throws Exception If a failure occurs.
   */
  public void close() throws Exception {
    synchronized (this.monitor) {
      // check if we are already closed
      if (this.closed) return;

      // check if we are already closing
      if (this.closing) {
        throw new IllegalStateException(
            "Already closing this endpoint");
      }

      // flag closing
      this.closing = true;
      this.monitor.notifyAll();
      try {
        // get the start time
        long startTime = System.currentTimeMillis();

        // wait until the acquired count drops to zero
        while (this.acquiredCount > 0 || this.acquiring) {
          // get the acquired count at the start in case we time out
          int startAcquiredCount = this.acquiredCount;

          try {
            // check if we have waited more than the maximum amount of time
            long now = System.currentTimeMillis();
            if (now - startTime > MAXIMUM_CLOSE_WAIT) {
              throw new IllegalStateException(
                  "Exceeded maximum wait time to close out messaging endpoint.  "
                      + "startAcquiredCount=[ " + startAcquiredCount
                      + " ], acquiredCount=[ " + this.acquiredCount
                      + " ], acquiring=[ " + this.acquiring + " ]");
            }

            // determine the time to wait
            long maxWaitTime = MAXIMUM_CLOSE_WAIT - (now - startTime);
            long waitTime = (CLOSE_TIMEOUT < maxWaitTime)
                ? CLOSE_TIMEOUT : maxWaitTime;

            // wait for notification
            this.monitor.wait(waitTime);

          } catch (InterruptedException ignore) {
            // ignore the exception
          }
        }

        // now actually close
        this.doClose();

        // set to the closed state and notify
        this.closed  = true;
        this.monitor.notifyAll();

      } finally {
        // set the closing state to false
        this.closing = false;
        this.monitor.notifyAll();
      }
    }
  }

  /**
   * Handles actually closing out resources once all acquired {@link
   * SzMessageSink} instances have been released.  This method is always
   * called within a synchronized block
   *
   * @throws Exception If a failure occurs.
   */
  protected abstract void doClose() throws Exception;

  /**
   * Overridden to provide a default implementation that returns <tt>null</tt>.
   * {@inheritDoc}
   */
  @Override
  public Integer getMessageCount() {
    return null;
  }
}
