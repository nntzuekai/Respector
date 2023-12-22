package com.senzing.api.services;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Engine;
import com.senzing.util.JsonUtilities;
import com.senzing.util.Timers;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import java.util.List;
import java.util.Set;

import static com.senzing.api.model.SzFeatureMode.NONE;
import static com.senzing.api.model.SzFeatureMode.REPRESENTATIVE;
import static com.senzing.api.model.SzHttpMethod.GET;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static com.senzing.util.JsonUtilities.*;

/**
 * Provides "how entity" and supporting services.
 */
@Path("/")
@Produces(APPLICATION_JSON)
public class HowRelatedServices implements ServicesSupport {

  @GET
  @Path("virtual-entities")
  public SzVirtualEntityResponse getVirtualEntity(
      @QueryParam("r")                                            List<String>    recordsParam,
      @QueryParam("records")                                      String          recordList,
      @DefaultValue("false") @QueryParam("withRaw")               boolean         withRaw,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean         forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel   detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode   featureMode,
      @DefaultValue("false") @QueryParam("withFeatureStats")      boolean         withFeatureStats,
      @DefaultValue("false") @QueryParam("withInternalFeatures")  boolean         withInternalFeatures,
      @Context                                                    UriInfo         uriInfo)
  {
    Timers timers = this.newTimers();
    try {
      SzApiProvider provider = this.getApiProvider();

      Set<SzRecordId> records = this.parseRecordIdentifiers(
          recordsParam, "r", GET, uriInfo, timers);

      if (recordList != null && recordList.trim().length() > 0) {
        Set<SzRecordId> ids = this.parseRecordIdentifiers(recordList);

        records.addAll(ids);
      }

      // check if no records were specified
      if (records.size() == 0) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "At least one record must be specified via the \"r\" or \"records\" "
                + "query parameters.");
      }

      // get the record JSON to send as the first parameter
      JsonArrayBuilder jab = Json.createArrayBuilder();
      for (SzRecordId recordId : records) {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("DATA_SOURCE", recordId.getDataSourceCode());
        job.add("RECORD_ID", recordId.getRecordId());
        jab.add(job);
      }
      JsonObjectBuilder builder = Json.createObjectBuilder();
      builder.add("RECORDS", jab);
      JsonObject jsonObject = builder.build();
      String recordsJson = toJsonText(jsonObject);

      StringBuffer sb = new StringBuffer();

      SzVirtualEntityData entityData = null;

      long flags = this.getFlags(forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 false);

      String rawData = null;


      this.enteringQueue(timers);
      rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        this.callingNativeAPI(
            timers, "engine", "getVirtualEntityByRecordID");
        int result = engineApi.getVirtualEntityByRecordID(recordsJson, flags, sb);
        this.calledNativeAPI(
            timers, "engine", "getVirtualEntityByRecordID");

        String engineJSON = sb.toString();
        if (result != 0) {
          throw this.newPossiblyBadRequestException(
              GET, uriInfo, timers, engineApi);
        }

        return engineJSON;
      });

      this.processingRawData(timers);

      // parse the result
      entityData = SzVirtualEntityData.parseEntityData(
          null,
          JsonUtilities.parseJsonObject(rawData),
          (f) -> provider.getAttributeClassForFeature(f));

      this.postProcessEntityData(
          entityData, forceMinimal, detailLevel, featureMode);

      this.processedRawData(timers);

      // construct the response
      SzVirtualEntityResponse response = this.newVirtualEntityResponse(
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
   * Post-processes the virtual entity data according to the specified
   * parameters.
   *
   * @param entityData The {@link SzEntityData} to modify.
   *
   * @param forceMinimal Whether or not minimal format is forced.
   *
   * @param detailLevel The {@link SzDetailLevel} describing the requested level
   *                    of detail.
   *
   * @param featureMode The {@link SzFeatureMode} describing how features
   *                    are retrieved.
   */
  protected void postProcessEntityData(SzVirtualEntityData  entityData,
                                       boolean              forceMinimal,
                                       SzDetailLevel        detailLevel,
                                       SzFeatureMode        featureMode)
  {
    // check if we need to strip out duplicate features
    if (featureMode == REPRESENTATIVE) {
      stripDuplicateFeatureValues(entityData.getResolvedEntity());
    }

    // check if fields are going to be null if they would otherwise be set
    if (featureMode == NONE || forceMinimal) {
      entityData.getResolvedEntity().setPartial(true);
    }
  }

  /**
   * Creates a new instance of {@link SzVirtualEntityResponse} with the
   * specified parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param entityData The {@link SzEntityData} for the response.
   * @return The newly created and configured {@link SzEntityResponse}.
   */
  protected SzVirtualEntityResponse newVirtualEntityResponse(
      SzHttpMethod        httpMethod,
      int                 httpStatusCode,
      UriInfo             uriInfo,
      Timers              timers,
      SzVirtualEntityData entityData)
  {
    // construct the response
    SzVirtualEntityResponse response = SzVirtualEntityResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo), entityData);

    // return the response
    return response;
  }

  /**
   * Implements the <tt>GET /entities/{entityId}/how</tt> operation.
   *
   * @param entityId The entity ID of the entity from the URI path.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included with the response.
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzWhyEntityResponse} describing the response.
   */
  @GET
  @Path("entities/{entityId}/how")
  public SzHowEntityResponse howEntityByEntityId(
      @PathParam("entityId")                                      long          entityId,
      @DefaultValue("false") @QueryParam("withRaw")               boolean       withRaw,
      @Context                                                    UriInfo       uriInfo)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();

      StringBuffer sb = new StringBuffer();

      String rawData = null;

      this.enteringQueue(timers);
      rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        this.callingNativeAPI(
            timers, "engine", "howEntityByEntityID");

        // perform the "why" operation and check the result
        int result = engineApi.howEntityByEntityID(entityId, sb);

        this.calledNativeAPI(
            timers, "engine", "howEntityByEntityID");

        if (result != 0) {
          throw this.newPossiblyNotFoundException(
              GET, uriInfo, timers, engineApi);
        }

        return sb.toString();
      });

      return this.createHowEntityResponse(rawData,
                                          timers,
                                          uriInfo,
                                          withRaw,
                                          provider);

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
   * Implements the
   * <code>GET /data-sources/{dataSourceCode}/records/{recordId}/entity/how</code>
   * operation.
   *
   * @param dataSourceCode The data source code for the record.
   * @param recordId The record ID for the record.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included with the response.
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzWhyEntityResponse} describing the response.
   */
  @GET
  @Path("data-sources/{dataSourceCode}/records/{recordId}/entity/how")
  public SzHowEntityResponse howEntityByRecordId(
      @PathParam("dataSourceCode")                  String    dataSourceCode,
      @PathParam("recordId")                        String    recordId,
      @DefaultValue("false") @QueryParam("withRaw") boolean   withRaw,
      @Context                                      UriInfo   uriInfo)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();

      StringBuffer sb = new StringBuffer();

      String rawData = null;

      SzRecordId identifier
          = SzRecordId.FACTORY.create(dataSourceCode, recordId);

      this.enteringQueue(timers);
      rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        boolean entityChanged = false;
        do {
          entityChanged = false;
          sb.delete(0, sb.length());

          // resolve the entity ID
          Long entityId = this.resolveEntityId(
              GET, uriInfo, timers, engineApi, identifier, true);

          this.callingNativeAPI(
              timers, "engine", "howEntityByEntityID");

          // perform the "why" operation and check the result
          int result = engineApi.howEntityByEntityID(entityId, sb);

          this.calledNativeAPI(
              timers, "engine", "howEntityByEntityID");

          if (result != 0) {
            throw this.newPossiblyNotFoundException(
                GET, uriInfo, timers, engineApi);
          }

          Long postEntityId = this.resolveEntityId(
              GET, uriInfo, timers, engineApi, identifier, true);

          entityChanged = (!entityId.equals(postEntityId));

        } while (entityChanged);

        return sb.toString();
      });

      return this.createHowEntityResponse(rawData,
                                          timers,
                                          uriInfo,
                                          withRaw,
                                          provider);

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
   * Parses the specified raw JSON and creates a new {@link
   * SzHowEntityResponse} with the specified parameters.
   *
   * @param rawData The raw JSON text to parse.
   * @param timers The {@link Timers} for the operation.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param withRaw Whether or not the raw JSON should be included in the
   *                response.
   * @param provider The {@link SzApiProvider} to use.
   * @return The {@link SzWhyEntityResponse} that was created.
   */
  protected SzHowEntityResponse createHowEntityResponse(
      String        rawData,
      Timers        timers,
      UriInfo       uriInfo,
      boolean       withRaw,
      SzApiProvider provider)
  {
    this.processingRawData(timers);
    // parse the result
    JsonObject  json      = JsonUtilities.parseJsonObject(rawData);
    JsonObject  howObject = json.getJsonObject("HOW_RESULTS");

    SzHowEntityResult howEntityResult
        = SzHowEntityResult.parseHowEntityResult(howObject);

    this.processedRawData(timers);

    // construct the response
    SzHowEntityResponse response = this.newHowEntityResponse(
        GET, 200, uriInfo, timers, howEntityResult);

    if (withRaw) {
      response.setRawData(rawData);
    }

    return response;
  }


  /**
   * Creates a new instance of {@link SzHowEntityResponse} with the specified
   * parameters:
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param howEntityResult The {@link SzHowEntityResult} describing how the
   *                        entity resolved.
   *
   * @return The {@link SzHowEntityResponse} that was created.
   */
  protected SzHowEntityResponse newHowEntityResponse(
      SzHttpMethod            httpMethod,
      int                     httpStatusCode,
      UriInfo                 uriInfo,
      Timers                  timers,
      SzHowEntityResult       howEntityResult)
  {
    return SzHowEntityResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        howEntityResult);
  }
}
