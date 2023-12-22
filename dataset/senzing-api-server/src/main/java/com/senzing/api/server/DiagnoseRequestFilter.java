package com.senzing.api.server;

import javax.servlet.*;
import java.io.IOException;

public class DiagnoseRequestFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest   servletRequest,
                       ServletResponse  servletResponse,
                       FilterChain      filterChain)
      throws IOException, ServletException
  {
    try {
      filterChain.doFilter(servletRequest, servletResponse);
    } catch (IOException|ServletException|RuntimeException e) {
      e.printStackTrace();
      System.err.println("REQUEST: " + servletRequest);
      throw e;
    }
  }

  @Override
  public void destroy() {

  }
}
