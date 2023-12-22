package com.senzing.api.model.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.senzing.api.model.SzLinks;
import com.senzing.api.services.ServicesUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

/**
 * Provides the default {@link SzLinks} implementation.
 */
@JsonDeserialize
public class SzLinksImpl implements SzLinks {
  /**
   * The self link.
   */
  private String self;

  /**
   * The link for the Open API specification.
   */
  private String openApiSpecification;

  /**
   * Default constructor.
   */
  public SzLinksImpl() {
    this.self = null;
  }

  /**
   * Constructs with the specified {@link HttpServletRequest}.
   *
   * @param request The {@link HttpServletRequest} to build from.
   */
  public SzLinksImpl(HttpServletRequest request) {
    this.self = request.getRequestURI();

    String queryString = request.getQueryString();
    if (queryString != null && queryString.trim().length() > 0) {
      this.self += ("?" + queryString);
    }

    String baseUri = ServicesUtil.getBaseUri(request).toString();

    if (baseUri != null) {
      this.openApiSpecification = baseUri + "/specifications/open-api";
    } else {
      this.openApiSpecification = null;
    }
  }

  /**
   * Constructs with the specified {@link UriInfo}.
   *
   * @param uriInfo The {@link UriInfo} for extracting the self link.
   */
  public SzLinksImpl(UriInfo uriInfo) {
    this.self = uriInfo.getRequestUri().toString();

    String baseUri = uriInfo.getBaseUri().toString();
    if (!baseUri.endsWith("/")) baseUri += "/";
    if (baseUri.startsWith("ws:")) {
      baseUri = baseUri.replace("ws:", "http:");
    } else if (baseUri.startsWith("wss:")) {
      baseUri = baseUri.replace("wss:", "https:");
    }
    this.openApiSpecification = baseUri + "specifications/open-api";
  }

  /**
   * Gets the self link.
   *
   * @return The self link.
   */
  @Override
  public String getSelf() {
    return this.self;
  }

  /**
   * Sets the self link.
   *
   * @param self The self link.
   */
  @Override
  public void setSelf(String self) {
    this.self = self;
  }

  /**
   * Gets the Open API specification link.
   *
   * @return The Open API specification link.
   */
  @Override
  public String getOpenApiSpecification() {
    return this.openApiSpecification;
  }

  /**
   * Sets the Open API specification link.
   *
   * @param openApiSpec The Open API specification link.
   */
  @Override
  public void setOpenApiSpecification(String openApiSpec) {
    this.openApiSpecification = openApiSpec;
  }
}
