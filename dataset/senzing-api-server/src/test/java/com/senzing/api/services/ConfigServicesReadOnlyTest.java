package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.api.server.SzApiServer;
import com.senzing.api.server.SzApiServerOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.senzing.api.model.SzHttpMethod.POST;
import static com.senzing.api.services.ResponseValidators.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ConfigServicesReadOnlyTest extends AbstractServiceTest
{
  /**
   * The config services instance.
   */
  private ConfigServices configServices;

  /**
   * Sets the desired options for the {@link SzApiServer} during server
   * initialization.
   *
   * @param options The {@link SzApiServerOptions} to initialize.
   */
  protected void initializeServerOptions(SzApiServerOptions options) {
    super.initializeServerOptions(options);
    options.setAdminEnabled(true);
    options.setReadOnly(true);
    options.setSkippingEnginePriming(true);
  }

  @BeforeAll public void initializeEnvironment() {
    try {
      this.beginTests();
      try {
        this.initializeTestEnvironment();
      } catch (Error error) {
        error.printStackTrace();
        throw error;
      }
      this.configServices = new ConfigServices();
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
  }

  @AfterAll public void teardownEnvironment() {
    try {
      this.teardownTestEnvironment();
      this.conditionallyLogCounts(true);
    } finally {
      this.endTests();
    }
  }

  @Test
  public void postDataSourcesTest()
  {
    this.performTest(() -> {
      SzDataSource dataSource
          = SzDataSource.FACTORY.create("SHOULD_FAIL", 10000);

      String  relativeUri = "data-sources";
      String  uriText     = this.formatServerUri(relativeUri);
      UriInfo uriInfo     = this.newProxyUriInfo(uriText);
      String  bodyContent = dataSource.toJson();
      String  testInfo    = this.formatTestInfo(relativeUri, bodyContent);

      long before = System.nanoTime();
      try {
        this.configServices.addDataSources(
            Collections.emptyList(),
            true,
            uriInfo,
            bodyContent);

        fail("Expected ForbiddenException for forbidden config modification");

      } catch (ForbiddenException expected) {
        SzErrorResponse response
            = (SzErrorResponse) expected.getResponse().getEntity();
        response.concludeTimers();
        long after = System.nanoTime();
        validateBasics(
            testInfo, response, 403, POST, uriText, after - before);
      }
    });
  }

  @Test
  public void postDataSourcesViaHttpTest()
  {
    this.performTest(() -> {
      SzDataSource dataSource
          = SzDataSource.FACTORY.create("SHOULD_FAIL", 10000);

      String  relativeUri = "data-sources";
      String  uriText     = this.formatServerUri(relativeUri);
      String  bodyContent = dataSource.toJson();
      String  testInfo    = this.formatTestInfo(relativeUri, bodyContent);

      // convert the body content to a byte array
      byte[] bodyContentData;
      try {
        bodyContentData = bodyContent.getBytes("UTF-8");
      } catch (UnsupportedEncodingException cannotHappen) {
        throw new IllegalStateException(cannotHappen);
      }
      long before = System.nanoTime();

      SzErrorResponse response = this.invokeServerViaHttp(
          POST,
          uriText,
          null,
          "application/json",
          bodyContentData,
          SzErrorResponse.class);

      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(
          testInfo, response, 403, POST, uriText, after - before);
    });
  }
}
