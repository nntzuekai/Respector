package com.senzing.api.websocket;

import com.senzing.util.LoggingUtilities;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Objects;

import static javax.servlet.http.HttpServletResponse.*;
import static com.senzing.util.LoggingUtilities.*;

/**
 * Provides a {@link Filter} that will do a pre-flight check on an
 * <tt>UPGRADE</tt> HTTP call before creating a Web Socket.
 */
public class WebSocketFilter implements Filter {
  /**
   * The initialization parameter for reading the class name to check for a
   * valid static method that is annotated with {@link OnUpgrade}.
   */
  public static final String CLASS_PARAMETER = "class";

  /**
   * The {@link Class} that contains the {@link Method} to invoke as a
   * pre-flight check.
   */
  private Class onUpgradeClass = null;

  /**
   * The {@link OnUpgrade} {@link Method} to call.
   */
  private Method onUpgradeMethod = null;

  /**
   * Constructs with the {@link Class} and {@link Method} to call.
   *
   * @param c The {@link Class} on which to call the static method.
   * @param m The static {@link Method} to call.
   */
  private WebSocketFilter(Class c, Method m) {
    this.onUpgradeClass   = c;
    this.onUpgradeMethod  = m;
  }

  /**
   * Default constructor for constructing via configuration.
   */
  public WebSocketFilter() {
    this.onUpgradeClass   = null;
    this.onUpgradeMethod  = null;
  }

  /**
   * Gets the static {@link OnUpgrade} {@link Method} from the specified
   * {@link Class} and validates it.  If the {@link Class} has no method
   * annotated with {@link OnUpgrade} then <tt>null</tt> is returned.  If
   * a method is annotated, but not valid then an {@link IllegalStateException}
   * is thrown.
   *
   * @param c The {@link Class} to check for the {@link OnUpgrade} method.
   *
   * @return The valid static {@link OnUpgrade} method, or <tt>null</tt> if no
   *         {@link OnUpgrade} annotation is found.
   */
  private static Method getOnUpgradeMethod(Class c) {
    Method[] methods = c.getMethods();
    for (Method method : methods) {
      // check if the method is annotated as OnUpgrade
      if (method.getAnnotation(OnUpgrade.class) == null) continue;

      // check the return type
      if (method.getReturnType() != boolean.class) {
        throw new IllegalStateException(
            "The OnUpgrade method must return boolean: class=[ " + c.getName()
                + " ], method=[ " + method + " ]");
      }

      // check the parameters
      Class[] paramTypes = method.getParameterTypes();
      if ((paramTypes.length != 2)
          || (paramTypes[0] != HttpServletRequest.class)
          || (paramTypes[1] != HttpServletResponse.class))
      {
        throw new IllegalStateException(
            "The OnUpgrade method must have exactly two parameters or types "
                + "HttpServletRequest and HttpServletResponse, respectively: "
                + "class=[ " + c.getName() + " ], method=[ " + method + " ]");
      }

      // set the on-upgrade method
      return method;
    }

    // if we get here then return null
    return null;
  }

  /**
   * Creates a new instance if the specified {@link Class} has an annotated
   * {@link OnUpgrade} static method that has a <tt>boolean</tt> return type
   * and two parameters of type {@link HttpServletRequest} and {@link
   * HttpServletResponse}.
   *
   * @param c The {@link Class} to check for an {@link OnUpgrade} method.
   *
   * @return The {@link WebSocketFilter} that was created to handle the
   *         pre-flight {@link OnUpgrade} check, or <tt>null</tt> if no filter
   *         is required because there is not an {@link OnUpgrade} annotation.
   *
   * @throws IllegalStateException If the specified {@link Class} contains a
   *                               method annotated with {@link OnUpgrade} but
   *                               that method is not a static method with the
   *                               valid return type and parameter types.
   */
  public static WebSocketFilter createIfOnUpgrade(Class c)
    throws IllegalStateException
  {
    Objects.requireNonNull(c, "The specified Class must not be null");

    // get the on-upgrade method
    Method onUpgradeMethod = getOnUpgradeMethod(c);

    // check if no on-upgrade method
    if (onUpgradeMethod == null) return null;

    // return a new instance
    return new WebSocketFilter(c, onUpgradeMethod);
  }

  /**
   * Provides an empty implementation.
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    // check if the class and method are already known
    if (this.onUpgradeMethod != null) return;

    // if the on-upgrade method is not set, then clear the class as well
    this.onUpgradeClass   = null;
    this.onUpgradeMethod  = null;

    String paramVal = config.getInitParameter(CLASS_PARAMETER);
    if ((paramVal == null) && (this.onUpgradeClass == null)) {
      throw new ServletException(
          "The OnUpgrade class name parameter (" + CLASS_PARAMETER
              + ") must be provided");
    }

    try {
      Class c = Class.forName(paramVal);
      Method onUpgradeMethod = getOnUpgradeMethod(c);

      if (onUpgradeMethod == null) {
        throw new ServletException(
            "The specified class does not have an OnUpgrade method: "
                + paramVal);
      }

      // set the fields
      this.onUpgradeClass   = c;
      this.onUpgradeMethod  = onUpgradeMethod;

    } catch (ClassNotFoundException e) {
      throw new ServletException(
          "The specified class name in in the init parameters could not be "
          + "found: " + paramVal);

    } catch (ServletException e) {
      e.printStackTrace();
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw new ServletException(e);
    }
  }

  /**
   * Checks if the specified {@link HttpServletRequest} represents a valid
   * WebSocket upgrade request.
   *
   * @param request The {@link HttpServletRequest} to check.
   *
   * @return <tt>true</tt> if a valid WebSocket upgrade request, otherwise
   *         <tt>false</tt>.
   */
  private static boolean isUpgradeRequest(HttpServletRequest request) {
    // ensure the method is GET
    if (!request.getMethod().equalsIgnoreCase("GET")) return false;

    // check for the "Connection: Upgrade" header
    boolean found = false;
    Enumeration<String> headerEnum = request.getHeaders("Connection");
    while (headerEnum.hasMoreElements()) {
      if ("upgrade".equalsIgnoreCase(headerEnum.nextElement())) {
        found = true;
        break;
      }
    }
    if (!found) return false;

    // check for the "Upgrade: websocket" header
    found = false;
    headerEnum = request.getHeaders("Upgrade");
    while (headerEnum.hasMoreElements()) {
      if ("websocket".equalsIgnoreCase(headerEnum.nextElement())) {
        found = true;
        break;
      }
    }

    // if true if found and false if not
    return found;
  }

  /**
   * Provides an implementation that delegates to the {@link OnUpgrade} method
   * with which this instance was constructed.
   */
  @Override
  public void doFilter(ServletRequest   request,
                       ServletResponse  response,
                       FilterChain      filterChain)
      throws ServletException, IOException
  {
    try {
      HttpServletRequest  httpRequest  = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      // check if this is an upgrade request
      if (!isUpgradeRequest(httpRequest)) {
        filterChain.doFilter(request, response);
        return;
      }

      // get the request URI
      String requestUri   = httpRequest.getRequestURI();
      String queryString  = httpRequest.getQueryString();
      if (queryString != null && queryString.trim().length() > 0) {
        requestUri = requestUri + "?" + queryString;
      }

      debugLog("Received upgrade request: " + requestUri);

      // call the pre-upgrade function
      try {
        // do the pre-upgrade check
        boolean proceed = this.preUpgrade(httpRequest, httpResponse);

        debugLog("Pre-upgrade checks (PROCEED / COMMITTED): "
                 + proceed + " / " + response.isCommitted());

        // if not proceeding, check if the response was not committed
        if (!proceed && !response.isCommitted()) {
          httpResponse.setContentType(MediaType.TEXT_PLAIN);
          httpResponse.sendError(
              SC_FORBIDDEN,
              "The request is not allowed, but no further "
              + "information was provided.");
          return;
        }

        // if proceeding, then do so
        if (proceed) {
          filterChain.doFilter(request, response);
          return;
        }

        // if we get here then assume we are short-circuiting the forward-chain
        // and we have already responded
        assert(!proceed && response.isCommitted());

      } catch (Exception e) {
        e.printStackTrace();

        if (!httpResponse.isCommitted()) {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          pw.println(e.getClass().getName() + "occurred in pre-upgrade check: "
                     + e.getMessage());
          e.printStackTrace(pw);
          httpResponse.setContentType(MediaType.TEXT_PLAIN);
          httpResponse.sendError(SC_INTERNAL_SERVER_ERROR, sw.toString());
        }
      }

    } catch (IOException e) {
      throw e;

    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  /**
   * Implement this method to check for a pre-upgrade error when a Web Socket
   * connection is requested.  If there is no pre-upgrade error then
   * <tt>null</tt> should be returned to indicate the operation should proceed.
   *
   * @param request The {@link HttpServletRequest}.
   *
   * @param response The {@link HttpServletResponse}.
   *
   * @return <tt>true</tt> if the upgrade to a Web Socket should proceed, and
   *         <tt>false</tt> if the upgrade should be prevented because an error
   *         has already been returned on the {@link HttpServletResponse}.
   */
  @SuppressWarnings("unchecked")
  protected boolean preUpgrade(HttpServletRequest   request,
                               HttpServletResponse  response)
    throws ServletException, IOException
  {
    try {
      Object[]    params      = {request, response};
      Constructor constructor = this.onUpgradeClass.getDeclaredConstructor();
      Object      instance    = constructor.newInstance();

      return (Boolean) this.onUpgradeMethod.invoke(instance, params);

    } catch (Exception e) {
      throw new ServletException(
          "Failed to call OnUpgrade method: class=[ " + this.onUpgradeClass
          + " ], method=[ " + this.onUpgradeMethod + " ]", e);
    }
  }

  /**
   * Simply nullifies the fields.
   */
  @Override
  public void destroy() {
    this.onUpgradeClass   = null;
    this.onUpgradeMethod  = null;
  }
}
