package com.senzing.api.services;


import com.senzing.util.ErrorLogSuppressor;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * Utility functions for services.
 */
public class ServicesUtil {
  /**
   * The suppression state for info errors.
   */
  private static final ErrorLogSuppressor INFO_ERROR_SUPPRESSOR
      = new ErrorLogSuppressor();

  /**
   * The hash of the system identity codes of the last info message exception
   * as well as the the message object that was sent.
   */
  private static final ThreadLocal<Long> LAST_INFO_ERROR_HASH
      = new ThreadLocal<>();

  /**
   * Generates a 64-bit hash from the system identity hash codes of the two
   * specified objects.
   *
   * @param obj1 The first object whose identity hash code goes to the high-order
   *             bits.
   * @param obj2 The second object whose identity hash code goes to the low-order
   *             bits.
   *
   * @return The <tt>long</tt> hash code of the object pair.
   */
  public static long identityPairHash(Object obj1, Object obj2) {
    long high = (long) ((obj1 == null) ? 0 : System.identityHashCode(obj1));
    long low = (long) ((obj2 == null) ? 0 : System.identityHashCode(obj2));
    return (high << 32) | low;
  }

  /**
   * Logs an error related to sending asynchronous info messages.
   *
   * @param e The {@link Exception} that occurred.
   * @param message The info message that failed.
   */
  public static void logFailedAsyncInfo(Exception e, SzMessage message) {
    // check what was previously logged and avoid double-logging
    Long previous = LAST_INFO_ERROR_HASH.get();
    long hash = identityPairHash(e, message);
    if (previous != null && previous == hash) return;
    LAST_INFO_ERROR_HASH.set(hash);

    Date timestamp = new Date();
    String info = message.getBody();
    System.err.println(
        timestamp + ": FAILED TO SEND ASYNC INFO MESSAGE: " + info);
    synchronized (INFO_ERROR_SUPPRESSOR) {
      boolean suppressing = INFO_ERROR_SUPPRESSOR.isSuppressing();
      ErrorLogSuppressor.Result result = INFO_ERROR_SUPPRESSOR.updateOnError();
      switch (result.getState()) {
        case SUPPRESSED:
          if (!suppressing) {
            System.err.println(
                timestamp + ": SUPPRESSING ASYNC INFO MESSAGE ERRORS FOR "
                    + INFO_ERROR_SUPPRESSOR.getSuppressDuration() + "ms");
          }
          break;
        case REACTIVATED:
          if (result.getSuppressedCount() > 0) {
            System.err.println(
                timestamp + ": RESUMING ASYNC INFO MESSAGE ERRORS ("
                    + result.getSuppressedCount() + " SUPPRESSED)");
          }
        case ACTIVE:
          e.printStackTrace();
          break;
      }
    }
  }

  /**
   * Returns the Base {@link URI} given the specified request {@link URI}.
   *
   * @param requestUri The request {@link URI} for the request.
   *
   * @return The Base {@link URI} for the specified request {@link URI}.
   */
  public static URI getBaseUri(URI requestUri) {
    // determine the scheme
    String scheme = requestUri.getScheme();
    if (scheme.equalsIgnoreCase("wss")) {
      scheme = "https";
    } else if (scheme.equalsIgnoreCase("ws")) {
      scheme = "http";
    }

    // get the host
    String host = requestUri.getHost();

    // get the port
    int port = requestUri.getPort();
    String portText = ":" + port;
    if ((port == 80 && scheme.equalsIgnoreCase("http"))
        || (port == 443 && scheme.equalsIgnoreCase("https")))
    {
      portText = "";
    }

    // get the context path
    SzApiProvider provider = SzApiProvider.Factory.getProvider();
    String contextPath = provider.getBasePath();
    if (!contextPath.endsWith("/")) contextPath += "/";
    if (!contextPath.startsWith("/")) contextPath = "/" + contextPath;

    // build the URI
    String uriText = scheme + "://" + host + portText + contextPath;

    try {
      // return the result
      return new URI(uriText);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the Base {@link URI} for the specified {@link HttpServletRequest}.
   *
   * @param request The {@link HttpServletRequest}.
   *
   * @return The Base {@link URI} for the specified {@link HttpServletRequest}.
   */
  public static URI getBaseUri(HttpServletRequest request) {
    // determine the scheme
    String scheme = request.getScheme();
    if (scheme.equalsIgnoreCase("wss")) {
      scheme = "https";
    } else if (scheme.equalsIgnoreCase("ws")) {
      scheme = "http";
    }

    // get the host
    String host = request.getServerName();

    // get the port
    int port = request.getServerPort();
    String portText = ":" + port;
    if ((port == 80 && scheme.equalsIgnoreCase("http"))
        || (port == 443 && scheme.equalsIgnoreCase("https")))
    {
      portText = "";
    }

    // get the context path
    String contextPath = request.getContextPath();
    if (!contextPath.endsWith("/")) contextPath += "/";
    if (!contextPath.startsWith("/")) contextPath = "/" + contextPath;

    // build the URI
    String uriText = scheme + "://" + host + portText + contextPath;

    try {
      // return the result
      return new URI(uriText);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
