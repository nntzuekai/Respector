package com.senzing.api.services;

import com.senzing.api.model.SzErrorResponse;
import com.senzing.api.websocket.OnUpgrade;
import com.senzing.util.Timers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static com.senzing.api.model.SzHttpMethod.GET;
import static com.senzing.io.IOUtilities.UTF_8;

/**
 * Provides support for blocking web socket upgrade if the provider is
 * read-only.
 */
public interface BlockUpgradeIfReadOnly extends BulkDataSupport {
  /**
   * Provides a pre-flight check to make sure the server is not in read-only
   * mode before opening the web socket.
   *
   * @param request The {@link HttpServletRequest}.
   * @param response The {@link HttpServletResponse}.
   *
   * @return <tt>true</tt> if not read-only and <tt>false</tt> if so.
   */
  @OnUpgrade
  default boolean onUpgrade(HttpServletRequest  request,
                            HttpServletResponse response)
      throws IOException
  {
    // get the provider
    SzApiProvider provider = this.getApiProvider();

    // if not read only then simply return true
    if (!provider.isReadOnly()) return true;

    // create the timers and construct an error response
    Timers timers = this.newTimers();
    SzErrorResponse errorResponse = this.newErrorResponse(
        this.newMeta(GET, 403, timers),
        this.newLinks(request),
        "Loading data is not allowed if Senzing API Server started "
            + "in read-only mode");
    errorResponse.concludeTimers();

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json; charset=utf-8");

    String  jsonText  = this.toJsonString(errorResponse);
    byte[]  jsonBytes = jsonText.getBytes(UTF_8);
    int     length    = jsonBytes.length;

    response.setContentLength(length);

    OutputStream os = response.getOutputStream();
    os.write(jsonBytes);
    os.flush();

    // return false
    return false;
  }

}
