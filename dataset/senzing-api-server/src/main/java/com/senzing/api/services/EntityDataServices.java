package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Engine;
import com.senzing.util.JsonUtilities;
import com.senzing.util.SemanticVersion;
import com.senzing.util.Timers;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import java.util.*;
import java.util.function.Function;

import static com.senzing.api.model.SzHttpMethod.*;
import static com.senzing.api.model.SzFeatureMode.*;
import static com.senzing.api.model.SzRelationshipMode.*;
import static com.senzing.api.model.SzAttributeSearchResultType.*;
import static com.senzing.g2.engine.G2Engine.*;
import static javax.ws.rs.core.MediaType.*;
import static com.senzing.api.services.ServicesUtil.*;
import static com.senzing.util.JsonUtilities.*;

/**
 * Provides entity data related API services.
 */
@Path("/")
@Produces(APPLICATION_JSON)
public class EntityDataServices implements ServicesSupport {
  /**
   * The minimum native API version to support search filtering.
   */
  public static final SemanticVersion MINIMUM_SEARCH_FILTERING_VERSION
      = new SemanticVersion("2.4.1");

  /**
   * The {@link Map} of {@link SzAttributeSearchResultType} keys to {@link
   * Integer} values representing the flags to apply.
   */
  private static final Map<SzAttributeSearchResultType, Long>
      RESULT_TYPE_FLAG_MAP;

  static {
    Map<SzAttributeSearchResultType, Long> map = new LinkedHashMap<>();
    map.put(MATCH, G2_EXPORT_INCLUDE_RESOLVED);
    map.put(POSSIBLE_MATCH, G2_EXPORT_INCLUDE_POSSIBLY_SAME);
    map.put(POSSIBLE_RELATION, G2_EXPORT_INCLUDE_POSSIBLY_RELATED);
    map.put(NAME_ONLY_MATCH, G2_EXPORT_INCLUDE_NAME_ONLY);
    RESULT_TYPE_FLAG_MAP = Collections.unmodifiableMap(map);
  }

  /**
   * Provides the implementation for
   * <tt>POST /data-sources/{dataSourceCode}/records</tt>.
   *
   * @param dataSourceCode The data source code from the URI path.
   * @param loadId The optional load ID query parameter for the record.
   * @param withInfo Flag indicating if resolution info should be produced
   *                 when loading the record (from the query parameter).
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param recordJsonData The Senzing-format JSON text describing the record.
   * @return The {@link SzLoadRecordResponse} describing the response.
   */
  @POST
  @Path("data-sources/{dataSourceCode}/records")
  public SzLoadRecordResponse loadRecord(
      @PathParam("dataSourceCode")                    String  dataSourceCode,
      @QueryParam("loadId")                           String  loadId,
      @QueryParam("withInfo") @DefaultValue("false")  boolean withInfo,
      @QueryParam("withRaw")  @DefaultValue("false")  boolean withRaw,
      @Context                                        UriInfo uriInfo,
      String                                                  recordJsonData)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();
      dataSourceCode = dataSourceCode.trim().toUpperCase();
      this.ensureLoadingIsAllowed(provider, POST, uriInfo, timers);

      final String dataSource = dataSourceCode;

      final String normalizedLoadId = normalizeString(loadId);

      String recordText = this.ensureJsonFields(
          POST,
          uriInfo,
          timers,
          recordJsonData,
          Collections.singletonMap("DATA_SOURCE", dataSource),
          Collections.emptyMap());

      JsonObject  recordJson    = JsonUtilities.parseJsonObject(recordText);
      String      jsonRecordId  = JsonUtilities.getString(recordJson, "RECORD_ID");
      if (jsonRecordId != null) {
        if (jsonRecordId.trim().length() == 0) {
          // we have an empty record ID, we need to strip it from the JSON
          JsonObjectBuilder jsonBuilder = Json.createObjectBuilder(recordJson);
          jsonBuilder.remove("RECORD_ID");
          recordJson    = jsonBuilder.build();
          jsonRecordId  = null;
          recordText    = JsonUtilities.toJsonText(recordJson);
        }
      }

      final String inRecordId     = jsonRecordId;
      final String recordJsonText = recordText;
      this.checkDataSource(POST, uriInfo, timers, dataSource, provider);

      StringBuffer sb = new StringBuffer();

      // get the asynchronous info queue
      boolean asyncInfo = provider.hasInfoSink();

      this.enteringQueue(timers);
      String text = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        int result;
        if (withInfo || asyncInfo) {
          this.callingNativeAPI(timers, "engine", "addRecordWithInfo");
          result = engineApi.addRecordWithInfo(
              dataSource,
              (inRecordId == null) ? "" : inRecordId, // empty record ID
              recordJsonText,
              normalizedLoadId,
              0,
              sb);
          this.calledNativeAPI(timers, "engine", "addRecordWithInfo");

        } else if (inRecordId == null) {
          this.callingNativeAPI(timers, "engine", "addRecordWithReturnedRecordID");
          result = engineApi.addRecordWithReturnedRecordID(dataSource,
                                                           sb,
                                                           recordJsonText,
                                                           normalizedLoadId);
          this.calledNativeAPI(timers, "engine", "addRecordWithReturnedRecordID");

        } else {
          this.callingNativeAPI(timers, "engine", "addRecord");
          result = engineApi.addRecord(dataSource,
                                       inRecordId,
                                       recordJsonText,
                                       normalizedLoadId);
          this.calledNativeAPI(timers, "engine", "addRecord");
        }

        if (result != 0) {
          throw this.newPossiblyNotFoundException(
              POST, uriInfo, timers, engineApi);
        }

        return sb.toString().trim();
      });

      String            recordId  = inRecordId;
      SzResolutionInfo  info      = null;
      String            rawData   = null;

      if (withInfo || asyncInfo) {
        rawData = text;
        JsonObject jsonObject = JsonUtilities.parseJsonObject(rawData);

        // if info was requested or we need to return the record ID then we need
        // to parse the info so we can return it or extract the record ID
        if (withInfo || inRecordId == null) {
          info = this.parseResolutionInfo(jsonObject);
        }

        // check if the info sink is configured
        if (asyncInfo && rawData != null && rawData.trim().length() > 0) {
          SzMessageSink infoSink = provider.acquireInfoSink();
          SzMessage message = new SzMessage(rawData);
          try {
            this.sendingAsyncMessage(timers, INFO_QUEUE_NAME);
            // send the info on the async queue
            infoSink.send(message, ServicesUtil::logFailedAsyncInfo);

          } catch (Exception e) {
            // failed async logger will not double-log
            logFailedAsyncInfo(e, message);

          } finally {
            this.sentAsyncMessage(timers, INFO_QUEUE_NAME);
            provider.releaseInfoSink(infoSink);
          }
        }

        // if the record ID is generated, we need to return it
        if (inRecordId == null) recordId = info.getRecordId();

        // nullify the info object reference if the info was not requested
        if (!withInfo) info = null;

      } else if (inRecordId == null) {
        recordId = text;
      }

      // construct the response
      SzLoadRecordResponse response = this.newLoadRecordResponse(
          POST, 200, uriInfo, timers, recordId, info);

      if (withRaw && withInfo) {
        response.setRawData(rawData);
      }

      // return the response
      return response;

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(POST, uriInfo, timers, e);
    }
  }

  /**
   * Parses the raw JSON described by the specified {@link JsonObject} and
   * produces an instance of {@link SzResolutionInfo}.
   *
   * @param jsonObject The {@link JsonObject} describing the resolution info
   *                   in the raw JSON format.
   * @return The {@link SzResolutionInfo} created form the {@link JsonObject}.
   */
  protected SzResolutionInfo parseResolutionInfo(JsonObject jsonObject) {
    return SzResolutionInfo.parseResolutionInfo(null, jsonObject);
  }

  /**
   * Creates a new instance of {@link SzLoadRecordResponse} with the specified
   * parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param recordId The record ID of the record that was loaded.
   * @param info The optional {@link SzResolutionInfo} for the response.
   * @return The newly created {@link SzLoadRecordResponse} instance.
   */
  protected SzLoadRecordResponse newLoadRecordResponse(
      SzHttpMethod      httpMethod,
      int               httpStatusCode,
      UriInfo           uriInfo,
      Timers            timers,
      String            recordId,
      SzResolutionInfo  info)
  {
    return SzLoadRecordResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newLoadRecordResponseData(recordId, info));
  }

  /**
   * Creates a new instance of {@link SzLoadRecordResponseData} with the
   * specified parameters.
   *
   * @param recordId The record ID of the record that was loaded.
   * @param info The optional {@link SzResolutionInfo} for the response.
   * @return The newly created {@link SzLoadRecordResponseData} instance.
   */
  protected SzLoadRecordResponseData newLoadRecordResponseData(
      String            recordId,
      SzResolutionInfo  info)
  {
    return SzLoadRecordResponseData.FACTORY.create(recordId, info);
  }

  /**
   * Provides the implementation for
   * <tt>PUT /data-sources/{dataSourceCode}/records/{recordId}</tt>.
   *
   * @param dataSourceCode The data source code from the URI path.
   * @param recordId The record ID of the record being loaded.
   * @param loadId The optional load ID query parameter for the record.
   * @param withInfo Flag indicating if resolution info should be produced
   *                 when loading the record (from the query parameter).
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param recordJsonData The Senzing-format JSON text describing the record.
   * @return The {@link SzLoadRecordResponse} describing the response.
   */
  @PUT
  @Path("data-sources/{dataSourceCode}/records/{recordId}")
  public SzLoadRecordResponse loadRecord(
      @PathParam("dataSourceCode")                    String  dataSourceCode,
      @PathParam("recordId")                          String  recordId,
      @QueryParam("loadId")                           String  loadId,
      @QueryParam("withInfo") @DefaultValue("false")  boolean withInfo,
      @QueryParam("withRaw")  @DefaultValue("false")  boolean withRaw,
      @Context                                        UriInfo uriInfo,
      String                                                  recordJsonData)
  {
    Timers timers = this.newTimers();
    try {
      SzApiProvider provider = this.getApiProvider();
      this.ensureLoadingIsAllowed(provider, PUT, uriInfo, timers);
      dataSourceCode = dataSourceCode.trim().toUpperCase();

      final String dataSource = dataSourceCode;

      final String normalizedLoadId = normalizeString(loadId);

      Map<String,String> map = Map.of("DATA_SOURCE", dataSource,
                                      "RECORD_ID", recordId);

      Map<String,String> defaultMap = Collections.emptyMap();

      String recordText = this.ensureJsonFields(PUT,
                                                uriInfo,
                                                timers,
                                                recordJsonData,
                                                map,
                                                defaultMap);

      this.checkDataSource(PUT, uriInfo, timers, dataSource, provider);

      // get the asynchronous info sink
      boolean asyncInfo = provider.hasInfoSink();

      this.enteringQueue(timers);
      String rawInfo = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API
        G2Engine engineApi = provider.getEngineApi();

        int result;
        String rawData = null;
        if (withInfo || asyncInfo) {
          StringBuffer sb = new StringBuffer();
          this.callingNativeAPI(timers, "engine", "addRecordWithInfo");
          result = engineApi.addRecordWithInfo(dataSource,
                                               recordId,
                                               recordText,
                                               normalizedLoadId,
                                               0,
                                               sb);
          this.calledNativeAPI(timers, "engine", "addRecordWithInfo");
          rawData = sb.toString();
        } else {
          this.callingNativeAPI(timers, "engine", "addRecord");
          result = engineApi.addRecord(dataSource,
                                       recordId,
                                       recordText,
                                       normalizedLoadId);
          this.calledNativeAPI(timers, "engine", "addRecord");
        }
        if (result != 0) {
          throw this.newPossiblyNotFoundException(
              PUT, uriInfo, timers, engineApi);
        }

        return rawData;
      });

      SzResolutionInfo info = null;
      if (rawInfo != null && rawInfo.trim().length() > 0) {
        // check if the info sink is configured
        if (asyncInfo) {
          SzMessageSink infoSink = provider.acquireInfoSink();
          SzMessage message = new SzMessage(rawInfo);
          try {
            this.sendingAsyncMessage(timers, INFO_QUEUE_NAME);
            // send the info on the async queue
            infoSink.send(message, ServicesUtil::logFailedAsyncInfo);

          } catch (Exception e) {
            logFailedAsyncInfo(e, message);

          } finally {
            this.sentAsyncMessage(timers, INFO_QUEUE_NAME);
            provider.releaseInfoSink(infoSink);
          }
        }

        // check if the info was requested
        if (withInfo) {
          JsonObject jsonObject = JsonUtilities.parseJsonObject(rawInfo);
          info = this.parseResolutionInfo(jsonObject);
        }
      }

      // construct the response
      SzLoadRecordResponse response = this.newLoadRecordResponse(
          PUT, 200, uriInfo, timers, recordId, info);

      // check if we have info and raw data was requested
      if (withRaw && withInfo) {
        response.setRawData(rawInfo);
      }

      // return the response
      return response;

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(PUT, uriInfo, timers, e);
    }
  }

  /**
   * Provides the implementation for
   * <tt>DELETE /data-sources/{dataSourceCode}/records/{recordId}</tt>.
   *
   * @param dataSourceCode The data source code from the URI path.
   * @param recordId The record ID of the record being deleted.
   * @param loadId The optional load ID query parameter for the record.
   * @param withInfo Flag indicating if resolution info should be produced
   *                 when loading the record (from the query parameter).
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzDeleteRecordResponse} describing the response.
   */
  @DELETE
  @Path("data-sources/{dataSourceCode}/records/{recordId}")
  public SzDeleteRecordResponse deleteRecord(
      @PathParam("dataSourceCode")                    String  dataSourceCode,
      @PathParam("recordId")                          String  recordId,
      @QueryParam("loadId")                           String  loadId,
      @QueryParam("withInfo") @DefaultValue("false")  boolean withInfo,
      @QueryParam("withRaw")  @DefaultValue("false")  boolean withRaw,
      @Context                                        UriInfo uriInfo)
  {
    Timers timers = this.newTimers();
    try {
      SzApiProvider provider = this.getApiProvider();
      this.ensureLoadingIsAllowed(provider, DELETE, uriInfo, timers);
      dataSourceCode = dataSourceCode.trim().toUpperCase();

      final String dataSource = dataSourceCode;

      Set<String> dataSources = provider.getDataSources(dataSource);

      if (!dataSources.contains(dataSource)) {
        throw this.newNotFoundException(
            DELETE, uriInfo, timers,
            "The specified data source is not recognized: " + dataSource);
      }

      final String normalizedLoadId = normalizeString(loadId);

      // get the asynchronous info sink (if configured)
      boolean asyncInfo = provider.hasInfoSink();

      enteringQueue(timers);
      String rawInfo = provider.executeInThread(() -> {
        exitingQueue(timers);

        // get the engine API
        G2Engine engineApi = provider.getEngineApi();

        int returnCode;
        String rawData = null;
        if (withInfo || asyncInfo) {
          StringBuffer sb = new StringBuffer();
          this.callingNativeAPI(timers, "engine", "deleteRecordWithInfo");
          returnCode = engineApi.deleteRecordWithInfo(
              dataSource, recordId, normalizedLoadId,0, sb);
          this.calledNativeAPI(timers, "engine", "deleteRecordWithInfo");
          rawData = sb.toString();
        } else {
          this.callingNativeAPI(timers, "engine", "deleteRecord");
          returnCode = engineApi.deleteRecord(
              dataSource, recordId, normalizedLoadId);
          this.calledNativeAPI(timers, "engine", "deleteRecord");
        }
        if (returnCode != 0) {
          int errorCode = engineApi.getLastExceptionCode();
          // if the record was not found, that is okay -- treat as idempotent,
          // but note that "info" will differ when deleting a not-found record
          if (errorCode == RECORD_NOT_FOUND_CODE) {
            return null;
          }
          // otherwise throw a server error
          throw this.newInternalServerErrorException(
              DELETE, uriInfo, timers, engineApi);
        }

        return rawData;
      });

      SzResolutionInfo info = null;
      if (rawInfo != null && rawInfo.trim().length() > 0) {
        // check if the info sink is configured
        if (asyncInfo) {
          SzMessageSink infoSink = provider.acquireInfoSink();
          SzMessage message = new SzMessage(rawInfo);
          try {
            this.sendingAsyncMessage(timers, INFO_QUEUE_NAME);
            // send the info on the async queue
            infoSink.send(message, ServicesUtil::logFailedAsyncInfo);

          } catch (Exception e) {
            logFailedAsyncInfo(e, message);

          } finally {
            this.sentAsyncMessage(timers, INFO_QUEUE_NAME);
            provider.releaseInfoSink(infoSink);
          }
        }

        // check if the info was explicitly requested
        if (withInfo) {
          JsonObject jsonObject = JsonUtilities.parseJsonObject(rawInfo);
          info = this.parseResolutionInfo(jsonObject);
          if ((normalizeString(info.getDataSource()) == null)
              && (normalizeString(info.getRecordId()) == null)
              && (info.getAffectedEntities().size() == 0)
              && (info.getFlaggedEntities().size() == 0))
          {
            // setup an SzResolutionInfo object to match what would be returned
            // by version 3.2 and later which does set data source and record ID
            info = SzResolutionInfo.FACTORY.create();
            info.setDataSource(dataSource);
            info.setRecordId(recordId);
          }
        }
      }

      // construct the response
      SzDeleteRecordResponse response = this.newDeleteRecordResponse(
          DELETE, 200, uriInfo, timers, info);

      // check if we have info and raw data was requested
      if (withRaw && withInfo) {
        response.setRawData(rawInfo);
      }

      // return the response
      return response;

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(DELETE, uriInfo, timers, e);
    }
  }

  /**
   * Creates a new instance of {@link SzDeleteRecordResponse} with the specified
   * parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param info The optional {@link SzResolutionInfo} for the response.
   * @return The newly created {@link SzDeleteRecordResponse} instance.
   */
  protected SzDeleteRecordResponse newDeleteRecordResponse(
      SzHttpMethod      httpMethod,
      int               httpStatusCode,
      UriInfo           uriInfo,
      Timers            timers,
      SzResolutionInfo  info)
  {
    return SzDeleteRecordResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newDeleteRecordResponseData(info));
  }

  /**
   * Creates a new instance of {@link SzDeleteRecordResponseData} with the
   * specified {@link SzResolutionInfo}.
   *
   * @param info The {@link SzResolutionInfo} for the {@link
   *             SzDeleteRecordResponse}.
   *
   * @return The newly created {@link SzDeleteRecordResponse} instance.
   */
  protected SzDeleteRecordResponseData newDeleteRecordResponseData(
      SzResolutionInfo  info)
  {
    return SzDeleteRecordResponseData.FACTORY.create(info);
  }

  /**
   * Provides the implementation for
   * <tt>POST /data-sources/{dataSourceCode}/records/{recordId}/reevaluate</tt>.
   *
   * @param dataSourceCode The data source code from the URI path.
   * @param recordId The record ID of the record being reevaluated.
   * @param withInfo Flag indicating if resolution info should be produced
   *                 when loading the record (from the query parameter).
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzReevaluateResponse} describing the response.
   */
  @POST
  @Path("data-sources/{dataSourceCode}/records/{recordId}/reevaluate")
  public SzReevaluateResponse reevaluateRecord(
      @PathParam("dataSourceCode")                    String  dataSourceCode,
      @PathParam("recordId")                          String  recordId,
      @QueryParam("withInfo") @DefaultValue("false")  boolean withInfo,
      @QueryParam("withRaw")  @DefaultValue("false")  boolean withRaw,
      @Context                                        UriInfo uriInfo)
  {
    Timers timers = this.newTimers();
    try {
      SzApiProvider provider = this.getApiProvider();
      this.ensureLoadingIsAllowed(provider, POST, uriInfo, timers);
      dataSourceCode = dataSourceCode.trim().toUpperCase();

      final String dataSource = dataSourceCode;

      Set<String> dataSources = provider.getDataSources(dataSource);

      if (!dataSources.contains(dataSource)) {
        throw this.newNotFoundException(
            POST, uriInfo, timers,
            "The specified data source is not recognized: " + dataSource);
      }

      // get the configured info message sink (if any)
      boolean asyncInfo = provider.hasInfoSink();

      this.enteringQueue(timers);
      String rawInfo = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API
        G2Engine engineApi = provider.getEngineApi();

        int returnCode;
        String rawData = null;
        if (withInfo || asyncInfo) {
          StringBuffer sb = new StringBuffer();
          this.callingNativeAPI(timers, "engine", "reevaluateRecordWithInfo");
          returnCode = engineApi.reevaluateRecordWithInfo(
              dataSource, recordId,0, sb);
          this.calledNativeAPI(timers, "engine", "reevaluateRecordWithInfo");
          rawData = sb.toString();
        } else {
          this.callingNativeAPI(timers, "engine", "reevaluateRecord");
          returnCode = engineApi.reevaluateRecord(dataSource, recordId,0);
          this.calledNativeAPI(timers, "engine", "reevaluateRecord");
        }
        if (returnCode != 0) {
          throw this.newPossiblyNotFoundException(
              POST, uriInfo, timers, engineApi);
        }

        return rawData;
      });

      SzResolutionInfo info = null;
      if (rawInfo != null && rawInfo.trim().length() > 0) {
        // check if the info sink is configured
        if (asyncInfo) {
          SzMessageSink infoSink = provider.acquireInfoSink();
          SzMessage message = new SzMessage(rawInfo);
          try {
            this.sendingAsyncMessage(timers, INFO_QUEUE_NAME);
            infoSink.send(message, ServicesUtil::logFailedAsyncInfo);

          } catch (Exception e) {
            logFailedAsyncInfo(e, message);

          } finally {
            this.sentAsyncMessage(timers, INFO_QUEUE_NAME);
            provider.releaseInfoSink(infoSink);
          }
        }

        // check if the info was explicitly requested
        if (withInfo) {
          JsonObject jsonObject = JsonUtilities.parseJsonObject(rawInfo);
          info = this.parseResolutionInfo(jsonObject);
        }
      }

      // construct the response
      SzReevaluateResponse response = this.newReevaluateResponse(
          POST, 200, uriInfo, timers, info);

      // check if we have info and raw data was requested
      if (withRaw && withInfo) {
        response.setRawData(rawInfo);
      }

      // return the response
      return response;

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(POST, uriInfo, timers, e);
    }
  }

  /**
   * Creates a new instance of {@link SzDeleteRecordResponse} with the specified
   * parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param info The optional {@link SzResolutionInfo} for the response.
   * @return The newly created {@link SzDeleteRecordResponse} instance.
   */
  protected SzReevaluateResponse newReevaluateResponse(
      SzHttpMethod      httpMethod,
      int               httpStatusCode,
      UriInfo           uriInfo,
      Timers            timers,
      SzResolutionInfo  info)
  {
    return SzReevaluateResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newReevaluateResponseData(info));
  }

  /**
   * Creates a new instance of {@link SzDeleteRecordResponseData} with the
   * specified parameters.
   *
   * @param info The optional {@link SzResolutionInfo} for the response.
   * @return The newly created {@link SzDeleteRecordResponseData} instance.
   */
  protected SzReevaluateResponseData newReevaluateResponseData(
      SzResolutionInfo  info)
  {
    return SzReevaluateResponseData.FACTORY.create(info);
  }

  /**
   * Provides the implementation for
   * <tt>GET /data-sources/{dataSourceCode}/records/{recordId}</tt>.
   *
   * @param dataSourceCode The data source code from the URI path.
   * @param recordId The record ID of the record being requested.
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzRecordResponse} describing the response.
   */
  @GET
  @Path("data-sources/{dataSourceCode}/records/{recordId}")
  public SzRecordResponse getRecord(
      @PathParam("dataSourceCode")                  String  dataSourceCode,
      @PathParam("recordId")                        String  recordId,
      @DefaultValue("false") @QueryParam("withRaw") boolean withRaw,
      @Context                                      UriInfo uriInfo)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();
      dataSourceCode = dataSourceCode.trim().toUpperCase();

      StringBuffer sb = new StringBuffer();

      final String dataSource = dataSourceCode;

      this.enteringQueue(timers);
      String rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API
        G2Engine engineApi = provider.getEngineApi();

        this.callingNativeAPI(timers, "engine", "getRecord");
        int result = engineApi.getRecord(
            dataSource, recordId, DEFAULT_RECORD_FLAGS, sb);
        this.calledNativeAPI(timers, "engine", "getRecord");

        if (result != 0) {
          throw this.newPossiblyNotFoundException(
              GET, uriInfo, timers, engineApi);
        }

        return sb.toString();
      });

      this.processingRawData(timers);

      // parse the raw data
      JsonObject jsonObject = JsonUtilities.parseJsonObject(rawData);

      SzEntityRecord entityRecord = this.parseEntityRecord(jsonObject);

      this.processedRawData(timers);

      // construct the response
      SzRecordResponse response = this.newRecordResponse(
          GET, 200, uriInfo, timers, entityRecord);

      // if including raw data then add it
      if (withRaw) response.setRawData(rawData);

      // return the response
      return response;

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Parses the raw JSON described by the specified {@link JsonObject} and
   * produces an instance of {@link SzEntityRecord}.
   *
   * @param jsonObject The {@link JsonObject} describing the resolution info
   *                   in the raw JSON format.
   * @return The {@link SzEntityRecord} created form the {@link JsonObject}.
   */
  protected SzEntityRecord parseEntityRecord(JsonObject jsonObject) {
    return SzEntityRecord.parseEntityRecord(null, jsonObject);
  }

  /**
   * Creates a new instance of {@link SzRecordResponse} with the specified
   * parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param entityRecord The {@link SzEntityRecord} describing the record.
   * @return The newly created {@link SzRecordResponse} instance.
   */
  protected SzRecordResponse newRecordResponse(
      SzHttpMethod      httpMethod,
      int               httpStatusCode,
      UriInfo           uriInfo,
      Timers            timers,
      SzEntityRecord    entityRecord)
  {
    return SzRecordResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newRecordResponseData(entityRecord));
  }

  /**
   * Creates a new instance of {@link SzRecordResponse} with the specified
   * parameters.
   *
   * @param entityRecord The {@link SzEntityRecord} describing the record.
   * @return The newly created {@link SzRecordResponse} instance.
   */
  protected SzRecordResponseData newRecordResponseData(
      SzEntityRecord entityRecord)
  {
    return SzRecordResponseData.FACTORY.create(entityRecord);
  }

  /**
   * Provides the implementation for
   * <tt>GET /data-sources/{dataSourceCode}/records/{recordId}/entity</tt>.
   *
   * @param dataSourceCode The data source code from the URI path.
   * @param recordId The record ID of the record for entity being requested.
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param withRelated Flag indicating if related entities should be included.
   * @param forceMinimal Flag indicating if the minimal response format is
   *                     requested.
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   * @param withFeatureStats Flag indicating if feature stats should be included
   *                         in the response.
   * @param withInternalFeatures Flag indicating if internal features should be
   *                             included in the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzEntityResponse} describing the response.
   */
  @GET
  @Path("data-sources/{dataSourceCode}/records/{recordId}/entity")
  public SzEntityResponse getEntityByRecordId(
      @PathParam("dataSourceCode")                                String              dataSourceCode,
      @PathParam("recordId")                                      String              recordId,
      @DefaultValue("false") @QueryParam("withRaw")               boolean             withRaw,
      @DefaultValue("PARTIAL") @QueryParam("withRelated")         SzRelationshipMode  withRelated,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean             forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel       detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode       featureMode,
      @DefaultValue("false") @QueryParam("withFeatureStats")      boolean             withFeatureStats,
      @DefaultValue("false") @QueryParam("withInternalFeatures")  boolean             withInternalFeatures,
      @Context                                                    UriInfo             uriInfo)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();
      dataSourceCode = dataSourceCode.trim().toUpperCase();

      final String dataSource = dataSourceCode;

      StringBuffer sb = new StringBuffer();

      SzEntityData entityData = null;

      long flags = this.getFlags(forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 (withRelated != SzRelationshipMode.NONE));

      String rawData = null;

      // check if we want 1-degree relations as well -- if so we need to
      // find the network instead of a simple lookup
      if (withRelated == FULL && !forceMinimal) {
        // build the record IDs JSON to find the network
        JsonObjectBuilder builder1 = Json.createObjectBuilder();
        JsonArrayBuilder builder2 = Json.createArrayBuilder();
        JsonObjectBuilder builder3 = Json.createObjectBuilder();
        builder1.add("RECORD_ID", recordId);
        builder1.add("DATA_SOURCE", dataSource);
        builder2.add(builder1);
        builder3.add("RECORDS", builder2);
        String recordIds = JsonUtilities.toJsonText(builder3);

        // set the other arguments
        final int maxDegrees = 1;
        final int buildOutDegrees = 1;
        final int maxEntityCount = 1000;

        boolean                 retry     = false;
        Map<Long, SzEntityData> dataMap   = null;
        Long                    entityId  = null;
        do {
          // setup a place to store the entity ID if needed
          final long[] entityIdArr = {0L};

          this.enteringQueue(timers);
          rawData = provider.executeInThread(() -> {
            this.exitingQueue(timers);

            // get the engine API and the config API
            G2Engine engineApi = provider.getEngineApi();

            this.callingNativeAPI(timers, "engine", "findNetworkByRecordID");
            // find the network and check the result
            int result = engineApi.findNetworkByRecordID(
                recordIds, maxDegrees, buildOutDegrees, maxEntityCount, flags, sb);

            this.calledNativeAPI(timers, "engine", "findNetworkByRecordID");

            if (result != 0) {
              throw this.newPossiblyNotFoundException(
                  GET, uriInfo, timers, engineApi);
            }

            // check if records are not coming back
            if ((flags & G2_ENTITY_INCLUDE_RECORD_DATA) == 0) {
              StringBuffer sb2 = new StringBuffer();
              result = engineApi.getEntityByRecordID(
                  dataSource, recordId, 0L, sb2);
              if (result != 0) {
                throw this.newPossiblyNotFoundException(
                    GET, uriInfo, timers, engineApi);
              }
              String jsonText = sb2.toString();
              JsonObject jsonObj = parseJsonObject(jsonText);
              jsonObj = getJsonObject(jsonObj, "RESOLVED_ENTITY");
              entityIdArr[0] = getLong(jsonObj, "ENTITY_ID");
            }

            return sb.toString();
          });

          this.processingRawData(timers);

          // organize all the entities into a map for lookup
          dataMap = this.parseEntityDataList(sb.toString(), provider);

          // check if no entities were found
          if (dataMap.size() == 0) {
            throw new IllegalStateException(
                "ERROR: Possible database corruption.  No entity found for "
                    + "record but Senzing API did not indicate an error code for "
                    + "an unrecognized record ID.  dataSource=[ " + dataSource
                    + " ], recordId=[ " + recordId + " ]");
          }

          // find the entity ID matching the data source and record ID
          for (SzEntityData edata : dataMap.values()) {
            SzResolvedEntity resolvedEntity = edata.getResolvedEntity();
            // check if records were not retrieved
            if ((flags & G2_ENTITY_INCLUDE_RECORD_DATA) == 0) {
              // no records, use the previous lookup
              if (resolvedEntity.getEntityId() == entityIdArr[0]) {
                entityId = entityIdArr[0];
                break;
              }
            } else {
              // check if this entity is the one that was requested by record ID
              for (SzMatchedRecord record : resolvedEntity.getRecords()) {
                if (record.getDataSource().equalsIgnoreCase(dataSource)
                    && record.getRecordId().equals(recordId)) {
                  // found the entity ID for the record ID
                  entityId = resolvedEntity.getEntityId();
                  break;
                }
              }
            }
            if (entityId != null) break;
          }

          // check for the entity not being found
          if (entityId == null) {
            // if records were not retrieved and we did not find the entity
            // then we need retry because it changed between calls
            retry = ((flags & G2_ENTITY_INCLUDE_RECORD_DATA) == 0L);

            // if no retry then we need throw an exception
            if (!retry) {
              throw new IllegalStateException(
                  "ERROR: Possible database corruption.  No entity found for "
                      + "record but Senzing API did not indicate an error code "
                      + "for an unrecognized record ID.  dataSource=[ "
                      + dataSource + " ], recordId=[ " + recordId + " ]");
            }
          }
        } while (retry);

        // get the result entity data
        entityData = this.getAugmentedEntityData(entityId, dataMap, provider);

      } else {
        this.enteringQueue(timers);
        rawData = provider.executeInThread(() -> {
          this.exitingQueue(timers);

          // get the engine API and the config API
          G2Engine engineApi = provider.getEngineApi();

          this.callingNativeAPI(timers, "engine", "getEntityByRecordID");
          // 1-degree relations are not required, so do a standard lookup
          int result = engineApi.getEntityByRecordID(dataSource, recordId, flags, sb);
          this.calledNativeAPI(timers, "engine", "getEntityByRecordID");

          String engineJSON = sb.toString();
          this.checkEntityResult(result, engineJSON, uriInfo, timers, engineApi);

          return engineJSON;
        });

        this.processingRawData(timers);
        // parse the result
        entityData = this.parseEntityData(
            JsonUtilities.parseJsonObject(rawData),
            (f) -> provider.getAttributeClassForFeature(f));
      }

      this.postProcessEntityData(
          entityData, forceMinimal, detailLevel, featureMode);

      this.processedRawData(timers);

      // construct the response
      SzEntityResponse response = this.newEntityResponse(
          GET, 200, uriInfo, timers, entityData);

      if (withRaw) {
        response.setRawData(rawData);
      }

      return response;

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Parses the entity data from a {@link JsonObject} describing JSON
   * for the Senzing native API format for an entity data and populates
   * the specified {@link SzEntityData} or creates a new instance.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON in the
   *                   Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link SzEntityData}.
   *
   */
  protected SzEntityData parseEntityData(
      JsonObject                jsonObject,
      Function<String, String>  featureToAttrClassMapper)
  {
    return SzEntityData.parseEntityData(
        null, jsonObject, featureToAttrClassMapper);
  }

  /**
   * Provides the implementation for <tt>GET /entities/{entityId}</tt>.
   *
   * @param entityId The entity ID of the entity being requested.
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param withRelated Flag indicating if related entities should be included.
   * @param forceMinimal Flag indicating if the minimal response format is
   *                     requested.
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   * @param withFeatureStats Flag indicating if feature stats should be included
   *                         in the response.
   * @param withInternalFeatures Flag indicating if internal features should be
   *                             included in the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzEntityResponse} describing the response.
   */
  @GET
  @Path("entities/{entityId}")
  public SzEntityResponse getEntityByEntityId(
      @PathParam("entityId")                                      long                entityId,
      @DefaultValue("false") @QueryParam("withRaw")               boolean             withRaw,
      @DefaultValue("PARTIAL") @QueryParam("withRelated")         SzRelationshipMode  withRelated,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean             forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel       detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode       featureMode,
      @DefaultValue("false") @QueryParam("withFeatureStats")      boolean             withFeatureStats,
      @DefaultValue("false") @QueryParam("withInternalFeatures")  boolean             withInternalFeatures,
      @Context                                                    UriInfo             uriInfo)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();

      StringBuffer sb = new StringBuffer();

      SzEntityData entityData = null;

      String rawData = null;

      long flags = this.getFlags(forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 (withRelated != SzRelationshipMode.NONE));

      // check if we want 1-degree relations as well -- if so we need to
      // find the network instead of a simple lookup
      if (withRelated == FULL && !forceMinimal) {
        // build the entity IDs JSON to find the network
        JsonObjectBuilder builder1 = Json.createObjectBuilder();
        JsonArrayBuilder builder2 = Json.createArrayBuilder();
        JsonObjectBuilder builder3 = Json.createObjectBuilder();
        builder1.add("ENTITY_ID", entityId);
        builder2.add(builder1);
        builder3.add("ENTITIES", builder2);
        String entityIds = JsonUtilities.toJsonText(builder3);

        // set the other arguments
        final int maxDegrees = 1;
        final int maxEntityCount = 1000;
        final int buildOutDegrees = 1;

        this.enteringQueue(timers);
        rawData = provider.executeInThread(() -> {
          this.exitingQueue(timers);
          // get the engine API
          G2Engine engineApi = provider.getEngineApi();

          this.callingNativeAPI(timers, "engine", "findNetworkByEntityID");
          // find the network and check the result
          int result = engineApi.findNetworkByEntityID(
              entityIds, maxDegrees, buildOutDegrees, maxEntityCount, flags, sb);

          this.calledNativeAPI(timers, "engine", "findNetworkByEntityID");

          if (result != 0) {
            throw this.newPossiblyNotFoundException(
                GET, uriInfo, timers, engineApi);
          }
          return sb.toString();
        });

        this.processingRawData(timers);

        // organize all the entities into a map for lookup
        Map<Long, SzEntityData> dataMap
            = this.parseEntityDataList(rawData, provider);

        // check for the entity not being found
        if (dataMap.size() == 0 || !dataMap.containsKey(entityId)) {
          throw new IllegalStateException(
              "WARNING: Possible database corruption.  No entity found for "
                  + "entity ID but Senzing API did not indicate an error code "
                  + "for an unrecognized entity ID.  entityId=[ "
                  + entityId + " ]");
        }

        // get the result entity data
        entityData = this.getAugmentedEntityData(entityId, dataMap, provider);

      } else {
        this.enteringQueue(timers);
        rawData = provider.executeInThread(() -> {
          this.exitingQueue(timers);

          // get the engine API
          G2Engine engineApi = provider.getEngineApi();

          this.callingNativeAPI(timers, "engine", "getEntityByEntityID");
          // 1-degree relations are not required, so do a standard lookup
          int result = engineApi.getEntityByEntityID(entityId, flags, sb);
          this.calledNativeAPI(timers, "engine", "getEntityByEntityID");

          String engineJSON = sb.toString();

          this.checkEntityResult(result, engineJSON, uriInfo, timers, engineApi);

          return engineJSON;
        });

        this.processingRawData(timers);

        // parse the result
        entityData = this.parseEntityData(
            JsonUtilities.parseJsonObject(rawData),
            (f) -> provider.getAttributeClassForFeature(f));
      }

      this.postProcessEntityData(
          entityData, forceMinimal, detailLevel, featureMode);

      this.processedRawData(timers);

      // construct the response
      SzEntityResponse response = this.newEntityResponse(
          GET, 200, uriInfo, timers, entityData);

      if (withRaw) {
        response.setRawData(rawData);
      }

      return response;

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Provides the implementation for <tt>GET /entities</tt>.
   *
   * @param attrs The senzing JSON describing the attributes to search on.
   * @param attrList The list of encoded {@link String} attributes to search on.
   * @param includeOnlySet The {@link Set} of search match levels to only be
   *                       included in the response.
   * @param forceMinimal Flag indicating if the minimal response format is
   *                     requested.
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   * @param withFeatureStats Flag indicating if feature stats should be included
   *                         in the response.
   * @param withInternalFeatures Flag indicating if internal features should be
   *                             included in the response.
   * @param withRelationships Flag indicating if entity relationships should be
   *                          included in the response.
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzAttributeSearchResponse} describing the response.
   */
  @GET
  @Path("entities")
  public SzAttributeSearchResponse searchEntitiesByGet(
      @QueryParam("attrs")                                        String              attrs,
      @QueryParam("attr")                                         List<String>        attrList,
      @QueryParam("includeOnly")                                  Set<String>         includeOnlySet,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean             forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel       detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode       featureMode,
      @DefaultValue("false") @QueryParam("withFeatureStats")      boolean             withFeatureStats,
      @DefaultValue("false") @QueryParam("withInternalFeatures")  boolean             withInternalFeatures,
      @DefaultValue("false") @QueryParam("withRelationships")     boolean             withRelationships,
      @DefaultValue("false") @QueryParam("withRaw")               boolean             withRaw,
      @Context                                                    UriInfo             uriInfo)
  {
    Timers timers = this.newTimers();
    try {
      JsonObject searchCriteria = null;
      if (attrs != null && attrs.trim().length() > 0) {
        try {
          searchCriteria = JsonUtilities.parseJsonObject(attrs);
        } catch (Exception e) {
          throw this.newBadRequestException(
              GET, uriInfo, timers,
              "The search criteria specified via the \"attrs\" parameter "
                  + "does not parse as valid JSON: " + attrs);
        }
      } else if (attrList != null && attrList.size() > 0) {
        Map<String, List<String>> attrMap = new LinkedHashMap<>();
        JsonObjectBuilder objBuilder = Json.createObjectBuilder();
        for (String attrParam : attrList) {
          // check for the colon
          int index = attrParam.indexOf(":");

          // if not found that is a problem
          if (index < 0) {
            throw this.newBadRequestException(
                GET, uriInfo, timers,
                "The attr param value must be a colon-delimited string, "
                    + "but no colon character was found: " + attrParam);
          }
          if (index == 0) {
            throw this.newBadRequestException(
                GET, uriInfo, timers,
                "The attr param value must contain a property name followed by "
                    + "a colon, but no property was provided before the colon: "
                    + attrParam);
          }

          // get the property name
          String propName = attrParam.substring(0, index);
          String propValue = "";
          if (index < attrParam.length() - 1) {
            propValue = attrParam.substring(index + 1);
          }

          // store in the map
          List<String> values = attrMap.get(propName);
          if (values == null) {
            values = new LinkedList<>();
            attrMap.put(propName, values);
          }
          values.add(propValue);
        }
        attrMap.entrySet().forEach(entry -> {
          String propName = entry.getKey();
          List<String> propValues = entry.getValue();
          if (propValues.size() == 1) {
            // add the attribute to the object builder
            objBuilder.add(propName, propValues.get(0));
          } else {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            for (String propValue : propValues) {
              JsonObjectBuilder job = Json.createObjectBuilder();
              job.add(propName, propValue);
              jab.add(job);
            }
            objBuilder.add(propName + "_LIST", jab);
          }
        });
        searchCriteria = objBuilder.build();
      }

      // check if we have no attributes at all
      if (searchCriteria == null || searchCriteria.size() == 0) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "At least one search criteria attribute must be provided via the "
                + "\"attrs\" or \"attr\" parameter.  attrs=[ " + attrs
                + " ], attrList=[ " + attrList + " ]");
      }

      // defer to the internal method
      return this.searchByAttributes(searchCriteria,
                                     includeOnlySet,
                                     forceMinimal,
                                     detailLevel,
                                     featureMode,
                                     withFeatureStats,
                                     withInternalFeatures,
                                     withRelationships,
                                     withRaw,
                                     uriInfo,
                                     GET,
                                     timers);

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Provides the implementation for <tt>POST /search-entities</tt>.
   *
   * @param includeOnlySet The {@link Set} of search match levels to only be
   *                       included in the response.
   * @param forceMinimal Flag indicating if the minimal response format is
   *                     requested.
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   * @param withFeatureStats Flag indicating if feature stats should be included
   *                         in the response.
   * @param withInternalFeatures Flag indicating if internal features should be
   *                             included in the response.
   * @param withRelationships Flag indicating if entity relationships should be
   *                          included in the response.
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param attrs The JSON request body describing the attributes to search on.
   * @return The {@link SzAttributeSearchResponse} describing the response.
   */
  @POST
  @Path("search-entities")
  public SzAttributeSearchResponse searchEntitiesByPost(
      @QueryParam("includeOnly")                                  Set<String>     includeOnlySet,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean         forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel       detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode   featureMode,
      @DefaultValue("false") @QueryParam("withFeatureStats")      boolean         withFeatureStats,
      @DefaultValue("false") @QueryParam("withInternalFeatures")  boolean         withInternalFeatures,
      @DefaultValue("false") @QueryParam("withRelationships")     boolean         withRelationships,
      @DefaultValue("false") @QueryParam("withRaw")               boolean         withRaw,
      @Context                                                    UriInfo         uriInfo,
      String                                                                      attrs)
  {
    Timers timers = this.newTimers();
    try {
      JsonObject searchCriteria = null;
      if (attrs == null || attrs.trim().length() == 0) {
        throw this.newBadRequestException(
            POST, uriInfo, timers, "The request body must be provided");
      }
      try {
        searchCriteria = JsonUtilities.parseJsonObject(attrs);
      } catch (Exception e) {
        throw this.newBadRequestException(
            POST, uriInfo, timers,
              "The search criteria in the request body does not parse as "
            + "valid JSON: " + attrs);
      }

      // check if we have no attributes at all
      if (searchCriteria == null || searchCriteria.size() == 0) {
        throw this.newBadRequestException(
            POST, uriInfo, timers,
            "At least one search criteria attribute must be provided in the "
                + "JSON request body.  requestBody=[ " + attrs + " ]");
      }

      // defer to the internal method
      return this.searchByAttributes(searchCriteria,
                                     includeOnlySet,
                                     forceMinimal,
                                     detailLevel,
                                     featureMode,
                                     withFeatureStats,
                                     withInternalFeatures,
                                     withRelationships,
                                     withRaw,
                                     uriInfo,
                                     POST,
                                     timers);

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(POST, uriInfo, timers, e);
    }
  }

  /**
   * Consolidates the search-by-attributes functionality into a common function.
   * Searching is possible via multiple endpoints (e.g.: one GET and one POST).
   *
   * @param searchCriteria The {@link JsonObject} describing the search criteria
   *                       attributes.
   * @param includeOnlySet The {@link Set} of search match levels to only be
   *                       included in the response.
   * @param forceMinimal Flag indicating if the minimal response format is
   *                     requested.
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   * @param withFeatureStats Flag indicating if feature stats should be included
   *                         in the response.
   * @param withInternalFeatures Flag indicating if internal features should be
   *                             included in the response.
   * @param withRelationships Flag indicating if entity relationships should be
   *                          included in the response.
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzAttributeSearchResponse} describing the response.
   */
  protected SzAttributeSearchResponse searchByAttributes(
      JsonObject          searchCriteria,
      Set<String>         includeOnlySet,
      boolean             forceMinimal,
      SzDetailLevel       detailLevel,
      SzFeatureMode       featureMode,
      boolean             withFeatureStats,
      boolean             withInternalFeatures,
      boolean             withRelationships,
      boolean             withRaw,
      UriInfo             uriInfo,
      SzHttpMethod        httpMethod,
      Timers              timers)
  {
    try {
      SzApiProvider provider = this.getApiProvider();

      // check for the include-only parameters, convert to result types
      if (includeOnlySet == null) includeOnlySet = Collections.emptySet();
      List<SzAttributeSearchResultType> resultTypes
          = new ArrayList<>(includeOnlySet.size());
      for (String includeOnly : includeOnlySet) {
        try {
          resultTypes.add(SzAttributeSearchResultType.valueOf(includeOnly));

        } catch (Exception e) {
          throw this.newBadRequestException(
              httpMethod, uriInfo, timers,
              "At least one of the includeOnly parameter values was not "
              + "recognized: " + includeOnly);
        }
      }

      // augment the flags based on includeOnly parameter result types
      long includeFlags = 0L;
      SemanticVersion version
          = new SemanticVersion(provider.getNativeApiVersion());

      boolean supportFiltering
          = MINIMUM_SEARCH_FILTERING_VERSION.compareTo(version) <= 0;

      // only support the include flags on versions where it works
      if (supportFiltering) {
        for (SzAttributeSearchResultType resultType : resultTypes) {
          Long flag = RESULT_TYPE_FLAG_MAP.get(resultType);
          if (flag == null) continue;
          includeFlags |= flag.longValue();
        }
      }

      // create the response buffer
      StringBuffer sb = new StringBuffer();

      // get the flags
      long flags = this.getFlags(includeFlags,
                                 forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 withRelationships);

      // format the search JSON
      final String searchJson = JsonUtilities.toJsonText(searchCriteria);

      this.enteringQueue(timers);
      provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API
        G2Engine engineApi = provider.getEngineApi();

        this.callingNativeAPI(timers, "engine", "searchByAttributes");
        int result = engineApi.searchByAttributes(searchJson, flags, sb);
        this.calledNativeAPI(timers, "engine", "searchByAttributes");
        if (result != 0) {
          throw this.newInternalServerErrorException(
              httpMethod, uriInfo, timers, engineApi);
        }
        return sb.toString();
      });

      this.processingRawData(timers);

      JsonObject jsonObject = JsonUtilities.parseJsonObject(sb.toString());
      JsonArray jsonResults = jsonObject.getValue(
          "/RESOLVED_ENTITIES").asJsonArray();

      // parse the result
      List<SzAttributeSearchResult> list = this.parseSearchResultList(
          jsonResults, (f) -> provider.getAttributeClassForFeature(f));


      this.postProcessSearchResults(
          list, forceMinimal, detailLevel, featureMode, withRelationships);

      // construct the response
      SzAttributeSearchResponse response = this.newAttributeSearchResponse(
           httpMethod, 200, uriInfo, timers, list);

      if (withRaw) {
        response.setRawData(sb.toString());
      }

      this.processedRawData(timers);

      // return the response
      return response;

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(httpMethod, uriInfo, timers, e);
    }
  }

  /**
   * Parses a list of resolved entities from a {@link JsonArray} describing a
   * JSON array in the Senzing native API format for entity features and
   * a new {@link List} of {@link SzAttributeSearchResult}.
   *
   * @param jsonArray The {@link JsonArray} describing the JSON in the
   *                  Senzing native API format.
   *
   * @param featureToAttrClassMapper Mapping function to map feature names to
   *                                 attribute classes.
   *
   * @return The populated (or created) {@link List} of {@link
   *         SzAttributeSearchResult} instances.
   */
  protected List<SzAttributeSearchResult> parseSearchResultList(
      JsonArray                     jsonArray,
      Function<String,String>       featureToAttrClassMapper)
  {
    // parse the result
    return SzAttributeSearchResult.parseSearchResultList(
        null, jsonArray, featureToAttrClassMapper);
  }

  /**
   * Creates a new instance of {@link SzAttributeSearchResponse} from the
   * specified parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param searchResults The {@link List} of {@link SzAttributeSearchResult}
   *                      instances.
   * @return The newly created {@link SzAttributeSearchResponse} instance.
   */
  protected SzAttributeSearchResponse newAttributeSearchResponse(
      SzHttpMethod                  httpMethod,
      int                           httpStatusCode,
      UriInfo                       uriInfo,
      Timers                        timers,
      List<SzAttributeSearchResult> searchResults)
  {
    return SzAttributeSearchResponse.FACTORY.create(
            this.newMeta(httpMethod, httpStatusCode, timers),
            this.newLinks(uriInfo),
            this.newAttributeSearchResponseData(searchResults));
  }

  /**
   * Creates a new instance of {@link SzAttributeSearchResponseData} from the
   * specified parameters.
   *
   * @param searchResults The {@link List} of {@link SzAttributeSearchResult}
   *                      instances.
   * @return The newly created {@link SzAttributeSearchResponseData} instance.
   */
  protected SzAttributeSearchResponseData newAttributeSearchResponseData(
      List<SzAttributeSearchResult> searchResults)
  {
    return SzAttributeSearchResponseData.FACTORY.create(searchResults);
  }

  /**
   * Provides the implementation for <tt>POST /reevaluate-entity</tt>.
   *
   * @param entityId The entity ID of the entity to be reevaluated.
   * @param withInfo Whether or not resolution info should be included in the
   *                 response.
   * @param withRaw Whether or not the raw JSON should be included with the
   *                response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzReevaluateResponse} describing the response.
   */
  @POST
  @Path("reevaluate-entity")
  public SzReevaluateResponse reevaluateEntity(
      @QueryParam("entityId")                         Long    entityId,
      @QueryParam("withInfo") @DefaultValue("false")  boolean withInfo,
      @QueryParam("withRaw")  @DefaultValue("false")  boolean withRaw,
      @Context                                        UriInfo uriInfo)
  {
    Timers timers = this.newTimers();
    try {
      SzApiProvider provider = this.getApiProvider();
      this.ensureLoadingIsAllowed(provider, POST, uriInfo, timers);

      if (entityId == null) {
        throw this.newBadRequestException(
            POST, uriInfo, timers, "The entityId parameter is required.");
      }

      // get the info sink (if configured)
      boolean asyncInfo = provider.hasInfoSink();

      this.enteringQueue(timers);
      String rawInfo = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API
        G2Engine engineApi = provider.getEngineApi();

        int returnCode;
        String rawData = null;
        if (withInfo || asyncInfo) {
          StringBuffer sb = new StringBuffer();
          this.callingNativeAPI(timers, "engine", "reevaluateEntityWithInfo");
          returnCode = engineApi.reevaluateEntityWithInfo(entityId,0, sb);
          this.calledNativeAPI(timers, "engine", "reevaluateEntityWithInfo");
          rawData = sb.toString();
        } else {
          this.callingNativeAPI(timers, "engine", "reevaluateEntity");
          returnCode = engineApi.reevaluateEntity(entityId,0);
          this.calledNativeAPI(timers, "engine", "reevaluateEntity");
        }

        if (returnCode != 0) {
          int errorCode = engineApi.getLastExceptionCode();
          if (errorCode == ENTITY_NOT_FOUND_CODE) {
            throw this.newBadRequestException(
                POST, uriInfo, timers, "The specified entityId was not found: "
                    + entityId);
          } else {
            throw this.newPossiblyNotFoundException(
                POST, uriInfo, timers, engineApi);
          }
        }

        return rawData;
      });

      SzResolutionInfo info = null;
      if (rawInfo != null && rawInfo.trim().length() > 0) {
        // check if the info sink is configured
        if (asyncInfo) {
          SzMessageSink infoSink = provider.acquireInfoSink();
          SzMessage message = new SzMessage(rawInfo);
          try {
            this.sendingAsyncMessage(timers, INFO_QUEUE_NAME);
            // send the info on the async queue
            infoSink.send(message, ServicesUtil::logFailedAsyncInfo);

          } catch (Exception e) {
            logFailedAsyncInfo(e, message);

          } finally {
            this.sentAsyncMessage(timers, INFO_QUEUE_NAME);
            provider.releaseInfoSink(infoSink);
          }
        }

        // if the info was requested, then we also want to parse and return it
        if (withInfo) {
          info = this.parseResolutionInfo(JsonUtilities.parseJsonObject(rawInfo));
        }
      }

      // construct the response
      SzReevaluateResponse response = this.newReevaluateResponse(
          POST, 200, uriInfo, timers, info);

      // check if we have info and raw data was requested
      if (withRaw && withInfo) {
        response.setRawData(rawInfo);
      }

      // return the response
      return response;

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(POST, uriInfo, timers, e);
    }
  }

  /**
   * For each of the entities in the specified {@link Map} of entity ID keys to
   * {@link SzEntityData} values, the related entities are obtained and for each
   * if the same partial entity exists as a fully-populated entity in the map
   * then it is replaced with the fully-populated non-partial entity.
   *
   * @param entityId The main entity ID for which this is being done.
   * @param dataMap The {@link Map} of entity ID keys to {@link SzEntityData}
   *                values.
   * @param provider The {@link SzApiProvider} to use.
   * @return Return the {@link SzEntityData} instance for the specified entity
   *         ID after its partial related entities have been replaced with the
   *         fully-populated entities.
   */
  protected SzEntityData getAugmentedEntityData(
      long                      entityId,
      Map<Long, SzEntityData>   dataMap,
      SzApiProvider             provider)
  {
    // get the result entity data
    SzEntityData entityData = dataMap.get(entityId);

    // check if we can augment the related entities that were found
    // so they are not partial responses since they are part of the
    // entity network build-out
    List<SzRelatedEntity> relatedEntities
        = entityData.getRelatedEntities();

    // loop over the related entities
    for (SzRelatedEntity relatedEntity : relatedEntities) {
      // get the related entity data (should be present)
      SzEntityData relatedData = dataMap.get(relatedEntity.getEntityId());

      // just in case not present because of entity count limits
      if (relatedData == null) continue;

      // get the resolved entity for the related entity
      SzResolvedEntity related = relatedData.getResolvedEntity();

      // get the features and records
      Map<String, List<SzEntityFeature>> features
          = related.getFeatures();

      List<SzMatchedRecord> records = related.getRecords();

      // summarize the records
      List<SzDataSourceRecordSummary> summaries
          = SzResolvedEntity.summarizeRecords(records);

      // set the features and "data" fields
      relatedEntity.setFeatures(
          features, (f) -> provider.getAttributeClassForFeature(f));

      // set the records and record summaries
      relatedEntity.setRecords(records);
      relatedEntity.setRecordSummaries(summaries);
      relatedEntity.setPartial(false);
    }

    return entityData;
  }

  /**
   * Parses the specified raw data using the specified {@link SzApiProvider} to
   * produce a {@link Map} of {@link Long} entity ID keys to {@link
   * SzEntityData} instances.
   *
   * @param rawData The JSON raw data to parse.
   * @param provider The {@link SzApiProvider} providing the mapping of features
   *                 to attribute classes.
   * @return The {@link Map} of of {@link Long} entity ID keys to {@link
   *         SzEntityData} instances.
   */
  protected Map<Long, SzEntityData> parseEntityDataList(String        rawData,
                                                        SzApiProvider provider)
  {
    // parse the raw response and extract the entities that were found
    JsonObject jsonObj = JsonUtilities.parseJsonObject(rawData);
    JsonArray jsonArr = jsonObj.getJsonArray("ENTITIES");

    List<SzEntityData> list = this.parseEntityDataList(
        jsonArr, (f) -> provider.getAttributeClassForFeature(f));

    // organize all the entities into a map for lookup
    Map<Long, SzEntityData> dataMap = new LinkedHashMap<>();
    for (SzEntityData edata : list) {
      SzResolvedEntity resolvedEntity = edata.getResolvedEntity();
      dataMap.put(resolvedEntity.getEntityId(), edata);
    }

    return dataMap;
  }

  /**
   * Creates a new instance of {@link SzEntityResponse} with the specified
   * parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param entityData The {@link SzEntityData} for the response.
   * @return The newly created and configured {@link SzEntityResponse}.
   */
  protected SzEntityResponse newEntityResponse(SzHttpMethod httpMethod,
                                               int          httpStatusCode,
                                               UriInfo      uriInfo,
                                               Timers       timers,
                                               SzEntityData entityData)
  {
    // construct the response
    SzEntityResponse response = SzEntityResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo), entityData);

    // return the response
    return response;
  }

  /**
   * Checks the result of a {@link G2Engine} API function that would retrieve an
   * entity or record.  If successful and the Senzing native JSON response was
   * obtained, then this method does nothing.  If unsuccessful, then checks if
   * the error code indicates a "404 Not Found" error or a
   * "500 Internal Server Error" and throws the appropriate exception.  If
   * specified JSON text is empty then a {@link NotFoundException} is thrown
   * even if the return code indicates success (zero).
   *
   * @param returnCode The return code from the {@link G2Engine} API function.
   * @param nativeJson The native Senzing JSON that was returned by the
   *                   function, if empty then a {@link NotFoundException} is
   *                   thrown.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the request.
   * @param engineApi The {@link G2Engine} instance for obtaining the error code
   *                  and error message.
   * @throws NotFoundException If the record or entity was not found.
   * @throws InternalServerErrorException If an internal server error was
   *                                      detected.
   */
  protected void checkEntityResult(int      returnCode,
                                   String   nativeJson,
                                   UriInfo  uriInfo,
                                   Timers   timers,
                                   G2Engine engineApi)
  {
    // check if failed to find result
    if (returnCode != 0) {
      throw newPossiblyNotFoundException(GET, uriInfo, timers, engineApi);
    }
    if (nativeJson.trim().length() == 0) {
      throw newNotFoundException(GET, uriInfo, timers);
    }
  }

  /**
   * Ensures the specified data source exists for the provider and throws a
   * {@link NotFoundException} if not found.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param dataSource The data source code to check.
   * @param apiProvider The {@link SzApiProvider} to validate against.
   * @throws NotFoundException If the data source is not found.
   */
  protected void checkDataSource(SzHttpMethod   httpMethod,
                                 UriInfo        uriInfo,
                                 Timers         timers,
                                 String         dataSource,
                                 SzApiProvider  apiProvider)
    throws NotFoundException
  {
    Set<String> dataSources = apiProvider.getDataSources(dataSource);

    if (!dataSources.contains(dataSource)) {
      throw this.newNotFoundException(
          httpMethod, uriInfo, timers,
          "The specified data source is not recognized: " + dataSource);
    }
  }

  /**
   * Utility method to ensure the JSON fields in the specified JSON text are
   * valid and augments the JSON with missing defaults.  The first {@link Map}
   * of key/value pairs is used to ensure the specified JSON either is missing
   * those keys and if so adds them with the associated value, and if not
   * absent, then ensures the values are equal and if not throws a {@link
   * BadRequestException}.  The second {@link Map} of key/value pairs is used
   * to specify default values that if missing from the JSON are added to the
   * JSON, but if already present with non-empty values are left as-is.  This
   * method returns the modified JSON text.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} the timers for the operation.
   * @param jsonText The JSON text to validate and possibly modify.
   * @param map The {@link Map} of values to ensure are either absent and should
   *            be added or if present have the specified values.
   * @param defaultMap The {@link Map} of values to augment the JSON with if
   *                   they are missing as default values.
   * @return The modified JSON text.
   */
  protected String ensureJsonFields(SzHttpMethod        httpMethod,
                                    UriInfo             uriInfo,
                                    Timers              timers,
                                    String              jsonText,
                                    Map<String, String> map,
                                    Map<String, String> defaultMap)
  {
    try {
      JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonText);
      JsonObjectBuilder jsonBuilder = Json.createObjectBuilder(jsonObject);

      map.entrySet().forEach(entry -> {
        String key = entry.getKey();
        String val = entry.getValue();

        String jsonVal = JsonUtilities.getString(jsonObject, key.toUpperCase());
        if (jsonVal == null) {
          jsonVal = JsonUtilities.getString(jsonObject, key.toLowerCase());
        }
        if (jsonVal != null && jsonVal.trim().length() > 0) {
          if (!jsonVal.equalsIgnoreCase(val)) {
            throw this.newBadRequestException(
                httpMethod, uriInfo, timers,
                key + " from path and from request body do not match.  "
                    + "fromPath=[ " + val + " ], fromRequestBody=[ "
                    + jsonVal + " ]");
          }
        } else {
          // we need to add the value for the key
          jsonBuilder.add(key, val);
        }
      });

      // iterate over the default values
      defaultMap.forEach((key, val) -> {
        // get the value for the key
        String jsonVal = JsonUtilities.getString(jsonObject, key.toUpperCase());
        if (jsonVal == null) {
          jsonVal = JsonUtilities.getString(jsonObject, key.toLowerCase());
          if (jsonVal != null) key = key.toLowerCase();
        }
        if (jsonVal == null || jsonVal.trim().length() == 0) {
          if (jsonObject.containsKey(key)) jsonBuilder.remove(key);
          jsonBuilder.add(key, val);
        }
      });

      return JsonUtilities.toJsonText(jsonBuilder);

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      throw this.newBadRequestException(httpMethod,
                                        uriInfo,
                                        timers,
                                        e.getMessage());
    }
  }

  /**
   * Post-processes the search results.  This handles potentially removing
   * elements from the response according to the parameters.  This method
   * leverages the following methods conditionally:
   * <ul>
   *   <li>{@link #setEntitiesPartial(List)}</li>
   *   <li>{@link #stripDuplicateFeatureValues(List)}</li>
   * </ul>
   *
   * @param searchResults The {@link List} of {@link SzAttributeSearchResult}
   *                      to modify.
   *
   * @param forceMinimal Whether or not minimal format is forced.
   *
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   *
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   *
   * @param withRelationships Whether or not to include relationships.
   */
  protected void postProcessSearchResults(
      List<SzAttributeSearchResult>   searchResults,
      boolean                         forceMinimal,
      SzDetailLevel                   detailLevel,
      SzFeatureMode                   featureMode,
      boolean                         withRelationships)
  {
    // check if we need to strip out duplicate features
    if (featureMode == REPRESENTATIVE) {
      stripDuplicateFeatureValues(searchResults);
    }

    // check if fields are going to be null if they would otherwise be set
    if (featureMode == SzFeatureMode.NONE || forceMinimal) {
      setEntitiesPartial(searchResults);
    }
  }

  /**
   * Sets the partial flags for the resolved entity and related
   * entities in the {@link SzEntityData}.  Call this method only if it is
   * known that the entities only contain partial data.  This method is called
   * by {@link #postProcessSearchResults(List, boolean, SzDetailLevel,
   * SzFeatureMode, boolean)}.
   *
   * @param searchResults The {@link List} of {@link SzAttributeSearchResult}
   *                      instances to mark as partial.
   */
  protected void setEntitiesPartial(
      List<SzAttributeSearchResult> searchResults)
  {
    searchResults.forEach(e -> {
      e.setPartial(true);

      e.getRelatedEntities().forEach(e2 -> {
        e2.setPartial(true);
      });
    });
  }

  /**
   * Strips out duplicate feature values for each feature in the search
   * result entities of the specified {@link List} of {@link
   * SzAttributeSearchResult} instances.
   *
   * @param searchResults The {@link List} of {@link SzAttributeSearchResult}
   *                      instances to modify.
   */
  protected void stripDuplicateFeatureValues(
      List<SzAttributeSearchResult> searchResults)
  {
    searchResults.forEach(e -> {
      this.stripDuplicateFeatureValues(e);

      e.getRelatedEntities().forEach(e2 -> {
        this.stripDuplicateFeatureValues(e2);
      });
    });
  }
}
