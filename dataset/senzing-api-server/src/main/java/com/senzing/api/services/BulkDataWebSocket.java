package com.senzing.api.services;

import com.senzing.api.model.SzErrorResponse;
import com.senzing.api.model.SzLinks;
import com.senzing.api.model.SzMeta;
import com.senzing.io.IOUtilities;
import com.senzing.util.LoggingUtilities;
import com.senzing.util.Timers;

import javax.websocket.*;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.Map;

import static com.senzing.io.IOUtilities.UTF_8;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static com.senzing.api.model.SzHttpMethod.*;
import static com.senzing.util.LoggingUtilities.*;

/**
 * Provides a base class for web socket implementations that accept a
 * continuous stream of data from the client and provides periodic responses
 * to the client.
 */
public abstract class BulkDataWebSocket implements BulkDataSupport {
  /**
   * The default maximum number of seconds to wait between receiving
   * Web Socket messages from the client before triggering EOF on the
   * incoming stream.
   */
  public static final Long DEFAULT_EOF_SEND_TIMEOUT = 3L;

  /**
   * The EOF detector thread.
   */
  protected class EOFDetector extends Thread {
    /**
     * Checks if this instance is completed.
     */
    protected boolean completed = false;

    /**
     * Implemented to close the stream when we have not received in the
     * specified timeout for the web socket thread.
     */
    public void run() {
      long nanoTimeout = BulkDataWebSocket.this.eofSendTimeout * 1000000000L;

      synchronized (BulkDataWebSocket.this) {
        long waitTime = BulkDataWebSocket.this.eofSendTimeout * 1000L;
        while (!this.completed
            && BulkDataWebSocket.this.pipedOutputStream != null) {
          // wait for a period
          try {
            BulkDataWebSocket.this.wait(waitTime);

          } catch (InterruptedException ignore) {
            // ignore
          }

          // check the time
          long now = System.nanoTime();
          long duration = (now - BulkDataWebSocket.this.lastMessageTime);
          boolean timedOut = (duration > nanoTimeout);

          if (LoggingUtilities.isDebugLogging()) {
            if (timedOut) {
              debugLog("Timed out waiting for input.  Assuming EOF.");
            } else if (this.completed) {
              debugLog("Completed reading input without timeout.  "
                       + "EOF reached.");
            }
          }
          if (this.completed || timedOut) {
            // signal EOF
            IOUtilities.close(BulkDataWebSocket.this.pipedOutputStream);
            BulkDataWebSocket.this.pipedOutputStream = null;
            BulkDataWebSocket.this.notifyAll();
            break;
          }

          // now check how long to wait next time
          waitTime = (BulkDataWebSocket.this.eofSendTimeout * 1000L);
          waitTime -= (duration / 1000000L);
        }
      }
    }

    /**
     * Method to mark this instance as completed.
     */
    protected void complete() {
      synchronized (BulkDataWebSocket.this) {
        this.completed = true;
        BulkDataWebSocket.this.notifyAll();
      }
    }
  }

  /**
   * The internal {@link Thread} used for reading the bulk data stream.
   */
  protected class ReaderThread extends Thread {
    /**
     * Implemented to start the EOF detector and then defer to the {@link
     * #doRun()} method.
     */
    public final void run() {
      final BulkDataWebSocket socket = BulkDataWebSocket.this;

      // defer the run
      try {
        // check for a failure detected during onOpen()
        if (BulkDataWebSocket.this.openErrorResponse != null) {
          // delay to allow the web socket to open
          Thread.currentThread().sleep(100);

          // send the error
          socket.session.getBasicRemote().sendObject(socket.openErrorResponse);

          // close the web socket due to error
          BulkDataWebSocket.this.onError(socket.session,
                                         socket.openException);
        }

        // call the doRun() method
        BulkDataWebSocket.this.doRun();

      } catch (EncodeException|InterruptedException|IOException ignore) {
        // thrown by the onError() function or pre-sleep delay -- ignore this

      } finally {
        // notify completion
        BulkDataWebSocket.this.eofDetector.complete();
        try {
          BulkDataWebSocket.this.eofDetector.join();

        } catch (InterruptedException ignore) {
          // ignore
        }
      }
    }
  }

  /**
   * The Web Socket {@link Session} for this instance.
   */
  protected Session session = null;

  /**
   * The {@link Timers} object to use for the websocket.
   */
  protected Timers timers = null;

  /**
   * The {@link PipedInputStream} to attach to the {@link PipedOutputStream}.
   */
  protected PipedInputStream pipedInputStream = null;

  /**
   * The {@link PipedOutputStream} to attach to the {@link PipedInputStream}.
   */
  protected PipedOutputStream pipedOutputStream = null;

  /**
   * Define the progress period for reporting progress on the web socket.
   */
  protected Long progressPeriod = DEFAULT_PROGRESS_PERIOD;

  /**
   * The EOF send timeout.
   */
  protected Long eofSendTimeout = DEFAULT_EOF_SEND_TIMEOUT;

  /**
   * The time in milliseconds that the last message was received.
   */
  protected long lastMessageTime = -1L;

  /**
   * The {@link UriInfo} for the request.
   */
  protected UriInfo uriInfo = null;

  /**
   * The {@link ReaderThread} used for reading the data.
   */
  protected ReaderThread readerThread = null;

  /**
   * The {@link EOFDetector} thread for monitoring for EOF.
   */
  protected EOFDetector eofDetector = null;

  /**
   * Flag indicating if we have started processing.
   */
  protected boolean started = false;

  /**
   * Flag indicating if we have begun shutting down.
   */
  protected boolean closing = false;

  /**
   * The failure that occurred when opening the web socket.
   */
  protected Exception openException = null;

  /**
   * The error response to give for any on-open failure that occurred.
   */
  protected SzErrorResponse openErrorResponse = null;

  /**
   * The {@link MediaType} to assume -- if text is sent rather than binary
   * then {@link MediaType#TEXT_PLAIN_TYPE} will be used.
   */
  protected MediaType mediaType = TEXT_PLAIN_TYPE;

  /**
   * Default constructor.
   */
  public BulkDataWebSocket() {
    // do nothing
  }

  /**
   * Handles opening the web socket with the specified session.
   *
   * @param session The {@link Session} to open the web socket with.
   * @throws IOException              If an I/O failure occurs.
   * @throws IllegalArgumentException If the progress period is invalid.
   */
  @OnOpen
  public synchronized void onOpen(Session session)
      throws IOException, IllegalArgumentException
  {
    this.timers = newTimers();
    this.session = session;
    this.pipedInputStream = new PipedInputStream(PIPE_SIZE);
    this.pipedOutputStream = new PipedOutputStream(this.pipedInputStream);
    this.uriInfo = this.newProxyUriInfo(this.session);
    this.started = false;
    this.lastMessageTime = System.nanoTime();

    Map<String, List<String>> params = this.session.getRequestParameterMap();

    // get the progress period
    List<String> paramValues = params.get("progressPeriod");
    if (paramValues != null && paramValues.size() > 0) {
      try {
        this.progressPeriod = Long.parseLong(paramValues.get(0));
        if (this.progressPeriod < 0L) throw new IllegalArgumentException();

      } catch (IllegalArgumentException e) {
        this.openException = new BadRequestException(
            "The specified progress period (progressPeriod) must be a "
                + "non-negative long integer: " + paramValues.get(0));

        this.openErrorResponse = newErrorResponse(
            SzMeta.FACTORY.create(POST, 403, this.timers),
            SzLinks.FACTORY.create(this.uriInfo),
            this.openException.getMessage());
      }
    }

    // get the EOF send timeout
    paramValues = params.get("eofSendTimeout");
    if (paramValues != null && paramValues.size() > 0) {
      try {
        this.eofSendTimeout = Long.parseLong(paramValues.get(0));
        if (this.eofSendTimeout < 0L) throw new IllegalArgumentException();

      } catch (IllegalArgumentException e) {
        this.openException = new BadRequestException(
            "The specified EOF send timeout (eofSendTimeout) must be a "
                + "non-negative long integer: " + paramValues.get(0));

        this.openErrorResponse = newErrorResponse(
            SzMeta.FACTORY.create(POST, 403, this.timers),
            SzLinks.FACTORY.create(this.uriInfo),
            this.openException.getMessage());
      }
    }

    // create the EOF thread
    this.readerThread = new ReaderThread();
    this.eofDetector = new EOFDetector();
    this.eofDetector.start();

    // check if we had an exception
    if (this.openErrorResponse != null) {
      this.readerThread.start();
    }
  }

  /**
   * Handles an incoming binary message that is treated as a chunk of the
   * bulk data file.
   *
   * @param bytes The bytes of the incoming message.
   * @throws IOException If an I/O failure occurs.
   */
  @OnMessage
  public synchronized void onMessage(byte[] bytes) throws IOException {
    debugLog("Binary web socket message received (" + bytes.length
                 + " bytes): "
                 + this.uriInfo.getRequestUri().toString(),
             "-----------------------------------",
             new String(bytes, UTF_8),
             "-----------------------------------");

    long now = System.nanoTime();
    if (this.pipedOutputStream == null) {
      // if session closed, ignore the message
      if (!this.session.isOpen() || this.closing) return;

      // if session is not closed then throw an exception
      throw new IllegalStateException(
          "Output stream is already closed: "
              + ((now - this.lastMessageTime) / 1000000L)
              + "ms since last message");
    }


    // check if started, and if not then start the thread
    if (!this.started) {
      this.started = true;
      this.readerThread.start();
    }

    if (this.pipedOutputStream != null) {
      this.pipedOutputStream.write(bytes);
      this.pipedOutputStream.flush();
    }
    this.lastMessageTime = System.nanoTime();
    this.notifyAll();
  }

  /**
   * Handles an incoming text message that is treated as a chunk of the
   * bulk data file.
   *
   * @param text The text of the incoming message.
   * @throws IOException If an I/O failure occurs.
   */
  @OnMessage
  public synchronized void onMessage(String text) throws IOException {
    debugLog("Text web socket message received (" + text.length()
                 + " characters): "
                 + this.uriInfo.getRequestUri().toString(),
             "-----------------------------------",
             text,
             "-----------------------------------");

    long now = System.nanoTime();
    if (this.pipedOutputStream == null) {
      if (!this.session.isOpen() || this.closing) return;
      throw new IllegalStateException(
          "Output stream is already closed: "
              + ((now - this.lastMessageTime) / 1000000L)
              + "ms since last message");
    }

    // check if started, and if not then start the thread
    if (!this.started) {
      // text is being sent so set the media type to use UTF-8 charset
      this.mediaType = TEXT_PLAIN_UTF8_TYPE;
      this.started = true;
      this.readerThread.start();
    }

    if (this.pipedOutputStream != null) {
      this.pipedOutputStream.write(text.getBytes(UTF_8));
      this.pipedOutputStream.flush();
    }
    this.lastMessageTime = System.nanoTime();
    this.notifyAll();
  }

  /**
   * Handles the closing of the web socket.
   *
   * @param session The web socket session that is being closed.
   * @throws IOException If an I/O failure occurs.
   */
  @OnClose
  public synchronized void onClose(Session session) throws IOException {
    IOUtilities.close(this.pipedOutputStream);
    this.pipedOutputStream = null;
    this.notifyAll();
  }

  /**
   * Handles the occurrence of an error on the web socket.
   *
   * @param session   The web socket session that is being closed.
   * @param throwable The exception that occurred.
   * @throws IOException If an I/O failure occurs.
   */
  @OnError
  public synchronized void onError(Session session, Throwable throwable)
      throws IOException {
    throwable.printStackTrace();
    IOUtilities.close(this.pipedOutputStream);
    this.pipedOutputStream = null;

    CloseReason.CloseCode closeCode
        = (throwable instanceof BadRequestException)
        ? CloseReason.CloseCodes.PROTOCOL_ERROR
        : CloseReason.CloseCodes.UNEXPECTED_CONDITION;

    this.closing = true;
    this.session.close(new CloseReason(closeCode, throwable.getMessage()));
  }

  /**
   * Override this to handle running the web socket functionality.
   */
  protected abstract void doRun();

  /**
   * Completes the run by closing the session and streams.
   */
  protected void completeRun() {
    try {
      synchronized (this) {
        this.closing = true;
        this.session.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    synchronized (this) {
      IOUtilities.close(this.pipedInputStream);
      IOUtilities.close(this.pipedOutputStream);
      this.pipedInputStream = null;
      this.pipedOutputStream = null;
      this.notifyAll();
    }
  }
}
