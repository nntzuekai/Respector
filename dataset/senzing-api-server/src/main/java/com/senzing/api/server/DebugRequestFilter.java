package com.senzing.api.server;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import static com.senzing.util.LoggingUtilities.*;

public class DebugRequestFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest   servletRequest,
                       ServletResponse  servletResponse,
                       FilterChain      filterChain)
      throws IOException, ServletException
  {
    long threadId = Thread.currentThread().getId();
    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    String requestUri   = httpRequest.getRequestURI();
    String queryString  = httpRequest.getQueryString();
    if (queryString != null && queryString.trim().length() > 0) {
      requestUri = requestUri + "?" + queryString;
    }
    long start = System.nanoTime();
    debugLog("Received request on thread " + threadId + ": " + requestUri);
    try {
      filterChain.doFilter(servletRequest, servletResponse);

    } catch (IOException|ServletException|RuntimeException e) {
      debugLog("Request failed on thread " + threadId + ": " + requestUri,
               e.toString());
      throw e;

    } finally {
      long end = System.nanoTime();
      long millis = (end - start) / 1000000L;
      debugLog("Request concluded in " + millis + "ms on thread "
                   + threadId + ": " + requestUri);
    }
  }

  @Override
  public void destroy() {

  }
}
