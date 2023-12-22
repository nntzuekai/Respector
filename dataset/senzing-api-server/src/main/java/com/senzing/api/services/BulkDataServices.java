package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.util.AccessToken;
import com.senzing.util.Timers;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.io.*;
import java.util.*;

import static com.senzing.api.model.SzHttpMethod.POST;
import static javax.ws.rs.core.MediaType.*;
import static com.senzing.util.LoggingUtilities.*;

/**
 * Bulk data REST services.
 */
@Path("/bulk-data")
@Produces(APPLICATION_JSON)
public class BulkDataServices implements BulkDataSupport {
  /**
   * Analyzes the bulk data records.
   */
  @POST
  @Path("/analyze")
  public SzBulkDataAnalysisResponse analyzeBulkRecordsViaForm(
      @HeaderParam("Content-Type") MediaType mediaType,
      @FormDataParam("data") InputStream dataInputStream,
      @Context UriInfo uriInfo)
  {
    SzApiProvider provider    = this.getApiProvider();
    Timers        timers      = this.newTimers();
    AccessToken   accessToken = this.prepareProlongedOperation(provider,
                                                               POST,
                                                               uriInfo,
                                                               timers);

    try {
      return this.analyzeBulkRecords(provider,
                                     timers,
                                     mediaType,
                                     dataInputStream,
                                     uriInfo,
                                     null,
                                     null,
                                     null,
                                     null);

    } catch (RuntimeException e) {
      throw logOnceAndThrow(e);

    } catch (Exception e) {
      throw logOnceAndThrow(new RuntimeException(e));
    } finally {
      provider.concludeProlongedOperation(accessToken);
    }
  }

  /**
   * Analyzes the bulk data records.
   */
  @POST
  @Path("/analyze")
  @Consumes({ APPLICATION_JSON,
              TEXT_PLAIN,
              TEXT_CSV,
              APPLICATION_JSONLINES })
  public SzBulkDataAnalysisResponse analyzeBulkRecordsDirect(
      @HeaderParam("Content-Type") MediaType mediaType,
      InputStream dataInputStream,
      @Context UriInfo uriInfo)
  {
    SzApiProvider provider    = this.getApiProvider();
    Timers        timers      = this.newTimers();
    AccessToken   accessToken = this.prepareProlongedOperation(provider,
                                                               POST,
                                                               uriInfo,
                                                               timers);

    try {
      return this.analyzeBulkRecords(provider,
                                     timers,
                                     mediaType,
                                     dataInputStream,
                                     uriInfo,
                                     null,
                                     null,
                                     null,
                                     null);

    } catch (RuntimeException e) {
      throw logOnceAndThrow(e);

    } catch (Exception e) {
      throw logOnceAndThrow(new RuntimeException(e));

    } finally {
      provider.concludeProlongedOperation(accessToken);
    }
  }

  /**
   * Analyzes the bulk data records via direct upload using SSE.
   *
   * @param mediaType The media type for the content.
   * @param dataInputStream The input stream to read the uploaded data.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param progressPeriod The suggested maximum time between SSE `progress`
   *                       events specified in milliseconds.  If not specified
   *                       then the default of `3000` milliseconds (i.e.: 3
   *                       seconds) is used.
   * @param sseEventSink The {@link SseEventSink} for the SSE protocol.
   * @param sse The {@link Sse} instance for the SSE protocol.
   */
  @POST
  @Path("/analyze")
  @Consumes({ APPLICATION_JSON,
              TEXT_PLAIN,
              TEXT_CSV,
              APPLICATION_JSONLINES })
  @Produces(TEXT_EVENT_STREAM)
  public void analyzeBulkRecordsDirect(
      @HeaderParam("Content-Type") MediaType mediaType,
      InputStream dataInputStream,
      @Context UriInfo uriInfo,
      @QueryParam("progressPeriod") @DefaultValue("3000") long progressPeriod,
      @Context SseEventSink sseEventSink,
      @Context Sse sse)
  {
    try {
      SzApiProvider provider    = this.getApiProvider();
      Timers        timers      = this.newTimers();
      AccessToken   accessToken = this.prepareProlongedOperation(provider,
                                                                 POST,
                                                                 uriInfo,
                                                                 timers);

      try {
        this.analyzeBulkRecords(provider,
                                timers,
                                mediaType,
                                dataInputStream,
                                uriInfo,
                                progressPeriod,
                                sseEventSink,
                                sse,
                                null);

      } catch (RuntimeException e) {
        throw logOnceAndThrow(e);

      } catch (Exception e) {
        throw logOnceAndThrow(new RuntimeException(e));

      } finally {
        provider.concludeProlongedOperation(accessToken);
      }

    } catch (WebApplicationException e) {
      OutboundSseEvent.Builder eventBuilder = sse.newEventBuilder();
      OutboundSseEvent event =
          eventBuilder.name(FAILED_EVENT)
              .id(String.valueOf(0))
              .mediaType(APPLICATION_JSON_TYPE)
              .data(e.getResponse().getEntity())
              .reconnectDelay(RECONNECT_DELAY)
              .build();
      sseEventSink.send(event);
    }
  }

  /**
   * Analyzes the bulk data records via form data using SSE.
   *
   * @param mediaType The media type for the content.
   * @param dataInputStream The input stream to read the uploaded data.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param progressPeriod The suggested maximum time between SSE `progress`
   *                       events specified in milliseconds.  If not specified
   *                       then the default of `3000` milliseconds (i.e.: 3
   *                       seconds) is used.
   * @param sseEventSink The {@link SseEventSink} for the SSE protocol.
   * @param sse The {@link Sse} instance for the SSE protocol.
   */
  @POST
  @Path("/analyze")
  @Produces(TEXT_EVENT_STREAM)
  public void analyzeBulkRecordsViaForm(
      @HeaderParam("Content-Type") MediaType mediaType,
      @FormDataParam("data") InputStream dataInputStream,
      @Context UriInfo uriInfo,
      @QueryParam("progressPeriod") @DefaultValue("3000") long progressPeriod,
      @Context SseEventSink sseEventSink,
      @Context Sse sse)
  {
    try {
      Timers        timers      = this.newTimers();
      SzApiProvider provider    = this.getApiProvider();
      AccessToken   accessToken = this.prepareProlongedOperation(provider,
                                                                 POST,
                                                                 uriInfo,
                                                                 timers);

      try {
        this.analyzeBulkRecords(provider,
                                timers,
                                mediaType,
                                dataInputStream,
                                uriInfo,
                                progressPeriod,
                                sseEventSink,
                                sse,
                                null);

      } catch (RuntimeException e) {
        throw logOnceAndThrow(e);

      } catch (Exception e) {
        throw logOnceAndThrow(new RuntimeException(e));
      } finally {
        provider.concludeProlongedOperation(accessToken);
      }
    } catch (WebApplicationException e) {
      OutboundSseEvent.Builder eventBuilder = sse.newEventBuilder();
      OutboundSseEvent event =
          eventBuilder.name(FAILED_EVENT)
              .id(String.valueOf(0))
              .mediaType(APPLICATION_JSON_TYPE)
              .data(e.getResponse().getEntity())
              .reconnectDelay(RECONNECT_DELAY)
              .build();
      sseEventSink.send(event);
    }
  }

  /**
   * Loads the bulk data records via form.
   *
   * @param dataSource The data source to assign to the loaded records unless
   *                   another data source mapping supercedes this default.
   * @param mapDataSources The JSON string mapping specific data sources to
   *                       alternate data source names.  A mapping from
   *                       empty-string is used for mapping records with no
   *                       data source specified.
   * @param mapDataSourceList The {@link List} of delimited strings that begin
   *                          the delimiter, followed by the "from" data source
   *                          then the delimiter then the target data source.
   * @param loadId The optional load ID to use for loading the records.
   * @param maxFailures The maximum number of failures or a negative number if
   *                    no maximum.
   * @param mediaType The media type for the content.
   * @param dataInputStream The input stream to read the uploaded data.
   * @param fileMetaData The form meta data for the uploaded file.
   * @param uriInfo The {@link UriInfo} for the request.
   */
  @POST
  @Path("/load")
  public SzBulkLoadResponse loadBulkRecordsViaForm(
      @QueryParam("dataSource") String dataSource,
      @QueryParam("mapDataSources") String mapDataSources,
      @QueryParam("mapDataSource") List<String> mapDataSourceList,
      @QueryParam("loadId") String loadId,
      @DefaultValue("0") @QueryParam("maxFailures") int maxFailures,
      @HeaderParam("Content-Type") MediaType mediaType,
      @FormDataParam("data") InputStream dataInputStream,
      @FormDataParam("data") FormDataContentDisposition fileMetaData,
      @Context UriInfo uriInfo)
  {
    SzApiProvider provider    = this.getApiProvider();
    Timers        timers      = this.newTimers();
    AccessToken   accessToken = this.prepareBulkLoadOperation(provider,
                                                              uriInfo,
                                                              timers);
    try {
      return this.loadBulkRecords(provider,
                                  timers,
                                  dataSource,
                                  mapDataSources,
                                  mapDataSourceList,
                                  loadId,
                                  maxFailures,
                                  mediaType,
                                  dataInputStream,
                                  fileMetaData,
                                  uriInfo,
                                  null,
                                  null,
                                  null,
                                  null);

    } catch (ForbiddenException e) {
      throw e;

    } catch (RuntimeException e) {
      throw logOnceAndThrow(e);

    } catch (Exception e) {
      throw logOnceAndThrow(new RuntimeException(e));

    } finally {
      provider.concludeProlongedOperation(accessToken);
    }
  }

  /**
   * Loads the bulk data records via direct upload.
   *
   * @param dataSource The data source to assign to the loaded records unless
   *                   another data source mapping supercedes this default.
   * @param mapDataSources The JSON string mapping specific data sources to
   *                       alternate data source names.  A mapping from
   *                       empty-string is used for mapping records with no
   *                       data source specified.
   * @param mapDataSourceList The {@link List} of delimited strings that begin
   *                          the delimiter, followed by the "from" data source
   *                          then the delimiter then the target data source.
   * @param loadId The optional load ID to use for loading the records.
   * @param maxFailures The maximum number of failures or a negative number if
   *                    no maximum.
   */
  @POST
  @Path("/load")
  @Consumes({ MediaType.APPLICATION_JSON,
      MediaType.TEXT_PLAIN,
      "text/csv",
      "application/x-jsonlines"})
  public SzBulkLoadResponse loadBulkRecordsDirect(
      @QueryParam("dataSource") String dataSource,
      @QueryParam("mapDataSources") String mapDataSources,
      @QueryParam("mapDataSource") List<String> mapDataSourceList,
      @QueryParam("loadId") String loadId,
      @DefaultValue("0") @QueryParam("maxFailures") int maxFailures,
      @HeaderParam("Content-Type") MediaType mediaType,
      InputStream dataInputStream,
      @Context UriInfo uriInfo)
  {
    SzApiProvider provider    = this.getApiProvider();
    Timers        timers      = this.newTimers();
    AccessToken   accessToken = this.prepareBulkLoadOperation(provider,
                                                              uriInfo,
                                                              timers);
    try {
      return this.loadBulkRecords(provider,
                                  timers,
                                  dataSource,
                                  mapDataSources,
                                  mapDataSourceList,
                                  loadId,
                                  maxFailures,
                                  mediaType,
                                  dataInputStream,
                                  null,
                                  uriInfo,
                                  null,
                                  null,
                                  null,
                                  null);

    } catch (ForbiddenException e) {
      throw e;

    } catch (RuntimeException e) {
      throw logOnceAndThrow(e);

    } catch (Exception e) {
      throw logOnceAndThrow(new RuntimeException(e));

    } finally {
      provider.concludeProlongedOperation(accessToken);
    }
  }

  /**
   * Loads bulk data records via form using SSE.
   *
   * @param dataSource The data source to assign to the loaded records unless
   *                   another data source mapping supercedes this default.
   * @param mapDataSources The JSON string mapping specific data sources to
   *                       alternate data source names.  A mapping from
   *                       empty-string is used for mapping records with no
   *                       data source specified.
   * @param mapDataSourceList The {@link List} of delimited strings that begin
   *                          the delimiter, followed by the "from" data source
   *                          then the delimiter then the target data source.
   * @param loadId The optional load ID to use for loading the records.
   * @param maxFailures The maximum number of failures or a negative number if
   *                    no maximum.
   * @param progressPeriod The suggested maximum time between SSE `progress`
   *                       events specified in milliseconds.  If not specified
   *                       then the default of `3000` milliseconds (i.e.: 3
   *                       seconds) is used.
   * @param mediaType The media type for the content.
   * @param dataInputStream The input stream to read the uploaded data.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param sseEventSink The {@link SseEventSink} for the SSE protocol.
   * @param sse The {@link Sse} instance for the SSE protocol.
   */
  @POST
  @Path("/load")
  @Produces(TEXT_EVENT_STREAM)
  public void loadBulkRecordsViaForm(
      @QueryParam("dataSource") String dataSource,
      @QueryParam("mapDataSources") String mapDataSources,
      @QueryParam("mapDataSource") List<String> mapDataSourceList,
      @QueryParam("loadId") String loadId,
      @DefaultValue("0") @QueryParam("maxFailures") int maxFailures,
      @HeaderParam("Content-Type") MediaType mediaType,
      @FormDataParam("data") InputStream dataInputStream,
      @FormDataParam("data") FormDataContentDisposition fileMetaData,
      @Context UriInfo uriInfo,
      @QueryParam("progressPeriod") @DefaultValue("3000") long progressPeriod,
      @Context SseEventSink sseEventSink,
      @Context Sse sse)

  {
    SzApiProvider provider    = this.getApiProvider();
    Timers        timers      = this.newTimers();
    AccessToken   accessToken = this.prepareBulkLoadOperation(provider,
                                                              uriInfo,
                                                              timers);
    try {
      this.loadBulkRecords(provider,
                           timers,
                           dataSource,
                           mapDataSources,
                           mapDataSourceList,
                           loadId,
                           maxFailures,
                           mediaType,
                           dataInputStream,
                           fileMetaData,
                           uriInfo,
                           progressPeriod,
                           sseEventSink,
                           sse,
                           null);

    } catch (ForbiddenException e) {
      throw e;

    } catch (RuntimeException e) {
      throw logOnceAndThrow(e);

    } catch (Exception e) {
      throw logOnceAndThrow(new RuntimeException(e));

    } finally {
      provider.concludeProlongedOperation(accessToken);
    }
  }

  /**
   * Loads the bulk data records via direct upload using SSE.
   *
   * @param dataSource The data source to assign to the loaded records unless
   *                   another data source mapping supercedes this default.
   * @param mapDataSources The JSON string mapping specific data sources to
   *                       alternate data source names.  A mapping from
   *                       empty-string is used for mapping records with no
   *                       data source specified.
   * @param mapDataSourceList The {@link List} of delimited strings that begin
   *                          the delimiter, followed by the "from" data source
   *                          then the delimiter then the target data source.
   * @param loadId The optional load ID to use for loading the records.
   * @param maxFailures The maximum number of failures or a negative number if
   *                    no maximum.
   * @param progressPeriod The suggested maximum time between SSE `progress`
   *                       events specified in milliseconds.  If not specified
   *                       then the default of `3000` milliseconds (i.e.: 3
   *                       seconds) is used.
   * @param mediaType The media type for the content.
   * @param dataInputStream The input stream to read the uploaded data.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param sseEventSink The {@link SseEventSink} for the SSE protocol.
   * @param sse The {@link Sse} instance for the SSE protocol.
   */
  @POST
  @Path("/load")
  @Consumes({ APPLICATION_JSON,
              TEXT_PLAIN,
              TEXT_CSV,
              APPLICATION_JSONLINES })
  @Produces(TEXT_EVENT_STREAM)
  public void loadBulkRecordsDirect(
      @QueryParam("dataSource") String dataSource,
      @QueryParam("mapDataSources") String mapDataSources,
      @QueryParam("mapDataSource") List<String> mapDataSourceList,
      @QueryParam("loadId") String loadId,
      @DefaultValue("0") @QueryParam("maxFailures") int maxFailures,
      @HeaderParam("Content-Type") MediaType mediaType,
      InputStream dataInputStream,
      @Context UriInfo uriInfo,
      @QueryParam("progressPeriod") @DefaultValue("3000") long progressPeriod,
      @Context SseEventSink sseEventSink,
      @Context Sse sse)
  {
    SzApiProvider provider    = this.getApiProvider();
    Timers        timers      = this.newTimers();
    AccessToken   accessToken = this.prepareBulkLoadOperation(provider,
                                                              uriInfo,
                                                              timers);
    try {
      this.loadBulkRecords(provider,
                           timers,
                           dataSource,
                           mapDataSources,
                           mapDataSourceList,
                           loadId,
                           maxFailures,
                           mediaType,
                           dataInputStream,
                           null,
                           uriInfo,
                           progressPeriod,
                           sseEventSink,
                           sse,
                           null);

    } catch (ForbiddenException e) {
      throw e;

    } catch (RuntimeException e) {
      throw logOnceAndThrow(e);

    } catch (Exception e) {
      throw logOnceAndThrow(new RuntimeException(e));

    } finally {
      provider.concludeProlongedOperation(accessToken);
    }
  }
}
