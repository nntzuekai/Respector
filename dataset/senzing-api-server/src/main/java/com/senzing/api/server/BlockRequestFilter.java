package com.senzing.api.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

public class BlockRequestFilter implements Filter {
  /**
   * The default response code which is {@value #DEFAULT_RESPONSE_CODE}.
   */
  public static final int DEFAULT_RESPONSE_CODE = 404;

  /**
   * The init parameter key for specifying the response code to use.
   * The response code defaults to {@value #DEFAULT_RESPONSE_CODE} if not
   * specified.  The value of this key is {@value #RESPONSE_CODE_PARAM}.
   * The value specified must fall between 400 and 499 as it must designate
   * an HTTP client error (usualy 403 is a good alternative to 404).
   */
  public static final String RESPONSE_CODE_PARAM = "RESPONSE_CODE";

  private int responseCode;

  public BlockRequestFilter() {
    this.responseCode = DEFAULT_RESPONSE_CODE;
  }

  public void init(FilterConfig config)
    throws ServletException {
    String paramVal = config.getInitParameter(RESPONSE_CODE_PARAM);
    if (paramVal != null) {
      try {
        this.responseCode = Integer.parseInt(paramVal);
        if (this.responseCode < 400 || this.responseCode >= 500) {
          throw new ServletException(
            "Response code must be designated as a client error 4xx code: "
            + this.responseCode);
        }
      } catch (ServletException e) {
        throw e;
        
      } catch (Exception e) {
        throw new ServletException(e);
      }
    }
  }

  public void doFilter(ServletRequest   request,
                       ServletResponse  response,
                       FilterChain      filterChain)
    throws ServletException, IOException {
      try {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.sendError(this.responseCode);

      } catch (IOException e) {
        throw e;

      } catch (Exception e) {
        throw new ServletException(e);
      }
  }

  public void destroy() {

  }
}
