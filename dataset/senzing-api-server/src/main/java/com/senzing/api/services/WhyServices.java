package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Engine;
import com.senzing.util.JsonUtilities;
import com.senzing.util.Timers;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import java.util.*;

import static com.senzing.api.model.SzHttpMethod.*;

/**
 * Provides "why" API services.
 */
@Path("/")
@Produces("application/json; charset=UTF-8")
public class WhyServices implements ServicesSupport {
  /**
   * Implements the
   * <tt>GET /data-sources/{dataSourceCode}/records/{recordId}/entity/why</tt>
   * operation.
   *
   * @param dataSourceCode The data source code from the URI path that in-part
   *                       identifies the record that belongs to the entity.
   * @param recordId The record ID from the URI path that in-part identifies the
   *                 record that belongs to the entity.
   * @param forceMinimal Whether or not the returned entities should be in
   *                     the minimal format.
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   * @param withFeatureStats Whether or not feature stats should be included
   *                         with the returned entities.
   * @param withInternalFeatures Whether or not internal features should be
   *                             included with the returned entities.
   * @param withRelationships Whether or not relationships should be included
   *                          in the returned entities.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included with the response.
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzWhyEntityResponse} describing the response.
   */
  @GET
  @Path("data-sources/{dataSourceCode}/records/{recordId}/entity/why")
  public SzWhyEntityResponse whyEntityByRecordId(
      @PathParam("dataSourceCode")                                String        dataSourceCode,
      @PathParam("recordId")                                      String        recordId,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean       forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode featureMode,
      @DefaultValue("true") @QueryParam("withFeatureStats")       boolean       withFeatureStats,
      @DefaultValue("true") @QueryParam("withInternalFeatures")   boolean       withInternalFeatures,
      @DefaultValue("false") @QueryParam("withRelationships")     boolean       withRelationships,
      @DefaultValue("false") @QueryParam("withRaw")               boolean       withRaw,
      @Context                                                    UriInfo       uriInfo)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();
      dataSourceCode = dataSourceCode.toUpperCase();

      final String dataSource = dataSourceCode;

      StringBuffer sb = new StringBuffer();

      String rawData = null;

      long flags = this.getFlags(forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 withRelationships);

      this.enteringQueue(timers);
      rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        this.callingNativeAPI(timers, "engine", "whyEntityByRecordID");

        // perform the "why" operation and check the result
        int result = engineApi.whyEntityByRecordID(
            dataSource, recordId, flags, sb);

        this.calledNativeAPI(timers, "engine", "whyEntityByRecordID");

        if (result != 0) {
          throw this.newWebApplicationException(
              GET, uriInfo, timers, engineApi);
        }

        return sb.toString();
      });

      return this.createWhyEntityResponse(rawData,
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
   * Implements the <tt>GET /entities/{entityId}/why</tt> operation.
   *
   * @param entityId The entity ID of the entity from the URI path.
   * @param forceMinimal Whether or not the returned entities should be in
   *                     the minimal format.
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   * @param withFeatureStats Whether or not feature stats should be included
   *                         with the returned entities.
   * @param withInternalFeatures Whether or not internal features should be
   *                             included with the returned entities.
   * @param withRelationships Whether or not relationships should be included
   *                          in the returned entities.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included with the response.
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzWhyEntityResponse} describing the response.
   */
  @GET
  @Path("entities/{entityId}/why")
  public SzWhyEntityResponse whyEntityByEntityId(
      @PathParam("entityId")                                      long          entityId,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean       forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode featureMode,
      @DefaultValue("true") @QueryParam("withFeatureStats")       boolean       withFeatureStats,
      @DefaultValue("true") @QueryParam("withInternalFeatures")   boolean       withInternalFeatures,
      @DefaultValue("false") @QueryParam("withRelationships")     boolean       withRelationships,
      @DefaultValue("false") @QueryParam("withRaw")               boolean       withRaw,
      @Context                                                    UriInfo       uriInfo)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();

      StringBuffer sb = new StringBuffer();

      String rawData = null;

      long flags = this.getFlags(forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 withRelationships);

      this.enteringQueue(timers);
      rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        this.callingNativeAPI(timers, "engine", "whyEntityByEntityID");

        // perform the "why" operation and check the result
        int result = engineApi.whyEntityByEntityID(entityId, flags, sb);

        this.calledNativeAPI(timers, "engine", "whyEntityByEntityID");

        if (result != 0) {
          throw this.newWebApplicationException(GET, uriInfo, timers, engineApi);
        }

        return sb.toString();
      });

      return this.createWhyEntityResponse(rawData,
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
   * Implments the <tt>GET /why/records</tt> operation.
   *
   * @param dataSourceCode1 The data source code for the first subject record.
   * @param recordId1 The record ID for the first subject record.
   * @param dataSourceCode2 The data source code for the second subject record.
   * @param recordId2 The record ID for the second subject record.
   * @param forceMinimal Whether or not the returned entities should be in
   *                     the minimal format.
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   * @param withFeatureStats Whether or not feature stats should be included
   *                         with the returned entities.
   * @param withInternalFeatures Whether or not internal features should be
   *                             included with the returned entities.
   * @param withRelationships Whether or not relationships should be included
   *                          in the returned entities.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included with the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzWhyRecordsResponse} describing the response.
   */
  @GET
  @Path("why/records")
  public SzWhyRecordsResponse whyRecords(
      @QueryParam("dataSource1")                                  String        dataSourceCode1,
      @QueryParam("recordId1")                                    String        recordId1,
      @QueryParam("dataSource2")                                  String        dataSourceCode2,
      @QueryParam("recordId2")                                    String        recordId2,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean       forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode featureMode,
      @DefaultValue("true") @QueryParam("withFeatureStats")       boolean       withFeatureStats,
      @DefaultValue("true") @QueryParam("withInternalFeatures")   boolean       withInternalFeatures,
      @DefaultValue("false") @QueryParam("withRelationships")     boolean       withRelationships,
      @DefaultValue("false") @QueryParam("withRaw")               boolean       withRaw,
      @Context                                                    UriInfo       uriInfo)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();

      // check the parameters
      if (dataSourceCode1 == null || dataSourceCode1.trim().length() == 0) {
        throw this.newBadRequestException(
            GET, uriInfo, timers, "The dataSource1 parameter is required.");
      }
      if (recordId1 == null || recordId1.trim().length() == 0) {
        throw this.newBadRequestException(
            GET, uriInfo, timers, "The recordId1 parameter is required.");
      }
      if (dataSourceCode2 == null || dataSourceCode2.trim().length() == 0) {
        throw this.newBadRequestException(
            GET, uriInfo, timers, "The dataSource2 parameter is required.");
      }
      if (recordId2 == null || recordId2.trim().length() == 0) {
        throw this.newBadRequestException(
            GET, uriInfo, timers, "The recordId2 parameter is required.");
      }

      // normalize the data source parameters
      dataSourceCode1 = dataSourceCode1.trim().toUpperCase();
      dataSourceCode2 = dataSourceCode2.trim().toUpperCase();

      final String dataSource1 = dataSourceCode1;
      final String dataSource2 = dataSourceCode2;

      StringBuffer sb = new StringBuffer();

      String rawData = null;

      long flags = this.getFlags(forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 withRelationships);

      this.enteringQueue(timers);
      rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        this.callingNativeAPI(timers, "engine", "whyRecords");

        // perform the "why" operation
        int result = engineApi.whyRecords(
            dataSource1, recordId1, dataSource2, recordId2, flags, sb);

        this.calledNativeAPI(timers, "engine", "whyRecords");

        if (result != 0) {
          int errorCode = engineApi.getLastExceptionCode();
          if (errorCode == DATA_SOURCE_NOT_FOUND_CODE
              || errorCode == RECORD_NOT_FOUND_CODE)
          {
            throw this.newBadRequestException(GET, uriInfo, timers, engineApi);
          }
          throw this.newInternalServerErrorException(
              GET, uriInfo, timers, engineApi);
        }

        return sb.toString();
      });

      this.processingRawData(timers);
      // parse the result
      JsonObject  json        = JsonUtilities.parseJsonObject(rawData);
      JsonArray   whyArray    = json.getJsonArray("WHY_RESULTS");
      JsonArray   entityArray = json.getJsonArray("ENTITIES");

      List<SzWhyRecordsResult> whyResults
          = this.parseWhyRecordsResultList(whyArray);

      if (whyResults.size() != 1) {
        throw new IllegalStateException(
            "Unexpected number of why results (" + whyResults.size()
            + ") for whyRecords() operation: dataSource1=[ " + dataSource1
            + " ], recordId1=[ " + recordId1 + " ], dataSource2=[ "
            + dataSource2 + " ], recordId2=[ " + recordId2 + " ]");
      }

      List<SzEntityData> entities = this.parseEntityDataList(
          entityArray, provider::getAttributeClassForFeature);
      this.processedRawData(timers);

      // construct the response
      SzWhyRecordsResponse response = this.newWhyRecordsResponse(
          GET, 200, uriInfo, timers, whyResults.get(0), entities);

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
   * Constructs a new instance of {@link SzWhyRecordsResponse} with the
   * specified parameters:
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param whyResult The {@link SzWhyRecordsResult} for the response.
   * @param entityDataList The {@link List} of {@link SzEntityData} instances.
   * @return The created {@link SzWhyRecordsResponse} instance.
   */
  protected SzWhyRecordsResponse newWhyRecordsResponse(
      SzHttpMethod        httpMethod,
      int                 httpStatusCode,
      UriInfo             uriInfo,
      Timers              timers,
      SzWhyRecordsResult  whyResult,
      List<SzEntityData>  entityDataList)
  {
    // construct the response
    return SzWhyRecordsResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newWhyRecordsResponseData(whyResult, entityDataList));
  }

  /**
   * Constructs a new instance of {@link SzWhyRecordsResponseData} with the
   * specified parameters:
   *
   * @param whyResult The {@link SzWhyRecordsResult} for the response.
   * @param entityDataList The {@link List} of {@link SzEntityData} instances.
   *
   * @return The created {@link SzWhyRecordsResponseData} instance.
   */
  protected SzWhyRecordsResponseData newWhyRecordsResponseData(
      SzWhyRecordsResult  whyResult,
      List<SzEntityData>  entityDataList)
  {
    // construct the response
    SzWhyRecordsResponseData data = SzWhyRecordsResponseData.FACTORY.create();
    data.setWhyResult(whyResult);
    data.setEntities(entityDataList);
    return data;
  }

  /**
   * Parses the raw JSON described by the specified {@link JsonArray} as a
   * {@link List} of {@link SzWhyRecordsResult} instances.
   *
   * @param whyArray The {@link JsonArray} describing the why results/
   *
   * @return The parsed {@link List} of {@link SzWhyRecordsResult} instances.
   */
  protected List<SzWhyRecordsResult> parseWhyRecordsResultList(
      JsonArray whyArray)
  {
    return SzWhyRecordsResult.parseWhyRecordsResultList(null, whyArray);
  }

  /**
   * Implements the <tt>GET /why/entities</tt> operation.
   *
   * @param entity1 The encoded {@link String} describing the {@link
   *                SzEntityIdentifier} for the first subject entity.
   * @param entity2 The encoded {@link String} describing the {@link
   *                SzEntityIdentifier} for the second subject entity.
   * @param forceMinimal Whether or not the returned entities should be in
   *                     the minimal format.
   * @param detailLevel The {@link SzDetailLevel} describing the requested
   *                    level of detail for the entity data, if
   *                    <code>null</code> this defaults to {@link
   *                    SzDetailLevel#VERBOSE}.
   * @param featureMode The {@link SzFeatureMode} query parameter indicating how
   *                    the features should be returned, if <code>null</code>
   *                    this defaults to {@link SzFeatureMode#WITH_DUPLICATES}.
   * @param withFeatureStats Whether or not feature stats should be included
   *                         with the returned entities.
   * @param withInternalFeatures Whether or not internal features should be
   *                             included with the returned entities.
   * @param withRelationships Whether or not relationships should be included
   *                          in the returned entities.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included with the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzWhyEntitiesResponse} describing the response.
   */
  @GET
  @Path("why/entities")
  public SzWhyEntitiesResponse whyEntities(
      @QueryParam("entity1")                                      String        entity1,
      @QueryParam("entity2")                                      String        entity2,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean       forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode featureMode,
      @DefaultValue("true") @QueryParam("withFeatureStats")       boolean       withFeatureStats,
      @DefaultValue("true") @QueryParam("withInternalFeatures")   boolean       withInternalFeatures,
      @DefaultValue("false") @QueryParam("withRelationships")     boolean       withRelationships,
      @DefaultValue("false") @QueryParam("withRaw")               boolean       withRaw,
      @Context                                                    UriInfo       uriInfo)
  {
    Timers timers = this.newTimers();

    try {
      SzApiProvider provider = this.getApiProvider();

      SzEntityIdentifier ident1 = null;
      SzEntityIdentifier ident2 = null;

      // check the parameters
      try {
        if (entity1 == null) {
          throw this.newBadRequestException(
              GET, uriInfo, timers,
              "Parameter missing or empty: \"entity1\".  "
                  + "The 'entity1' entity identifier is required.");
        }
        if (entity2 == null) {
          throw this.newBadRequestException(
              GET, uriInfo, timers,
              "Parameter missing or empty: \"entity2\".  "
                  + "The 'entity2' entity identifier is required.");
        }

        try {
          ident1 = this.parseEntityIdentifier(entity1.trim());
        } catch (Exception e) {
          throw this.newBadRequestException(
              GET, uriInfo, timers,
              "Parameter is not formatted correctly: \"entity1\".");
        }

        try {
          ident2 = this.parseEntityIdentifier(entity2.trim());
        } catch (Exception e) {
          throw this.newBadRequestException(
              GET, uriInfo, timers,
              "Parameter is not formatted correctly: \"entity2\".");
        }

        // check for consistent from/to
        if (ident1.getClass() != ident2.getClass()) {
          throw this.newBadRequestException(
              GET, uriInfo, timers,
              "Entity identifiers must be consistent types.  entity1="
                  + entity1 + ", entity2=" + entity2);
        }

      } catch (WebApplicationException e) {
        throw e;
      } catch (Exception e) {
        throw this.newBadRequestException(GET, uriInfo, timers, e.getMessage());
      }

      String rawData = null;

      long flags = this.getFlags(forceMinimal,
                                 detailLevel,
                                 featureMode,
                                 withFeatureStats,
                                 withInternalFeatures,
                                 withRelationships);

      this.enteringQueue(timers);

      final SzEntityIdentifier entityIdent1 = ident1;
      final SzEntityIdentifier entityIdent2 = ident2;

      rawData = provider.executeInThread(() -> {
        StringBuffer sb = new StringBuffer();

        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        Long    entityId1       = null;
        Long    entityId2       = null;
        boolean entitiesChanged = false;
        do {
          // set the entities changes flag to false
          entitiesChanged = false;
          sb.delete(0, sb.length());

          // resolve the entity ID's
          entityId1 = this.resolveEntityId(
              GET, uriInfo, timers, engineApi, entityIdent1);
          entityId2 = this.resolveEntityId(
              GET, uriInfo, timers, engineApi, entityIdent2);

          this.callingNativeAPI(timers, "engine", "whyEntities");

          // perform the "why" operation
          int result = engineApi.whyEntities(
              entityId1, entityId2, flags, sb);

          this.calledNativeAPI(timers, "engine", "whyEntities");

          if (result != 0) {
            int errorCode = engineApi.getLastExceptionCode();
            if (errorCode == ENTITY_NOT_FOUND_CODE) {
              throw this.newBadRequestException(GET, uriInfo, timers, engineApi);
            }
            throw this.newInternalServerErrorException(
                GET, uriInfo, timers, engineApi);
          }

          // check if we had record ID's
          if (entityIdent1 instanceof SzRecordId) {
            // resolve the entity ID's again, checking for a race condition
            Long postEntityId1 = this.resolveEntityId(
                GET, uriInfo, timers, engineApi, entityIdent1);
            Long postEntityId2 = this.resolveEntityId(
                GET, uriInfo, timers, engineApi, entityIdent2);

            // confirm no changes
            entitiesChanged = ((!entityId1.equals(postEntityId1))
                || (!entityId2.equals(postEntityId2)));
          }

        } while (entitiesChanged);

        return sb.toString();
      });

      // construct the response
      return this.createWhyEntitiesResponse(
          rawData, timers, uriInfo, withRaw, provider);

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
   * Determines if the failure that occurred with respect to the specified
   * {@link G2Engine} instance and either returns a {@link NotFoundException}
   * or an {@link InternalServerErrorException}.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param engineApi The {@link G2Engine} that is checked for the cause of the
   *                  error.
   * @return The {@link WebApplicationException} that was constructed.
   */
  protected WebApplicationException newWebApplicationException(
      SzHttpMethod  httpMethod,
      UriInfo       uriInfo,
      Timers        timers,
      G2Engine      engineApi)
  {
    int errorCode = engineApi.getLastExceptionCode();
    if (errorCode == DATA_SOURCE_NOT_FOUND_CODE
        || errorCode == RECORD_NOT_FOUND_CODE
        || errorCode == ENTITY_NOT_FOUND_CODE)
    {
      return this.newNotFoundException(httpMethod, uriInfo, timers, engineApi);
    }
    return this.newInternalServerErrorException(
        httpMethod, uriInfo, timers, engineApi);
  }

  /**
   * Parses the specified raw JSON and creates a new {@link SzWhyEntityResponse}
   * with the specified parameters.
   *
   * @param rawData The raw JSON text to parse.
   * @param timers The {@link Timers} for the operation.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param withRaw Whether or not the raw JSON should be included in the
   *                response.
   * @param provider The {@link SzApiProvider} to use.
   * @return The {@link SzWhyEntityResponse} that was created.
   */
  protected SzWhyEntityResponse createWhyEntityResponse(
      String        rawData,
      Timers        timers,
      UriInfo       uriInfo,
      boolean       withRaw,
      SzApiProvider provider)
  {
    this.processingRawData(timers);
    // parse the result
    JsonObject  json        = JsonUtilities.parseJsonObject(rawData);
    JsonArray   whyArray    = json.getJsonArray("WHY_RESULTS");
    JsonArray   entityArray = json.getJsonArray("ENTITIES");

    List<SzWhyEntityResult> whyResults
        = this.parseWhyEntityResultList(whyArray);

    List<SzEntityData> entities = this.parseEntityDataList(
        entityArray, provider::getAttributeClassForFeature);
    this.processedRawData(timers);

    // construct the response
    SzWhyEntityResponse response = this.newWhyEntityResponse(
        GET, 200, uriInfo, timers, whyResults, entities);

    if (withRaw) {
      response.setRawData(rawData);
    }

    return response;
  }

  /**
   * Parses the specified raw JSON described by the specified {@link JsonArray}
   * as a {@link List} of {@link SzWhyEntityResult} instances.
   *
   * @param whyArray The {@Link JsonArray} to parse.
   * @return The {@link List} of {@link SzWhyEntityResult} instances.
   */
  protected List<SzWhyEntityResult> parseWhyEntityResultList(JsonArray whyArray)
  {
    return SzWhyEntityResult.parseWhyEntityResultList(null, whyArray);
  }

  /**
   * Creates a new instance of {@link SzWhyEntityResponse} with the specified
   * parameters:
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param whyResults The {@link List} of {@link SzWhyEntityResult} instances.
   * @param entityDataList The {@link List} of {@link SzEntityData} instances.
   *
   * @return The {@link SzWhyEntityResponse} that was created.
   */
  protected SzWhyEntityResponse newWhyEntityResponse(
      SzHttpMethod            httpMethod,
      int                     httpStatusCode,
      UriInfo                 uriInfo,
      Timers                  timers,
      List<SzWhyEntityResult> whyResults,
      List<SzEntityData>      entityDataList)
  {
    return SzWhyEntityResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newWhyEntityResponseData(whyResults, entityDataList));
  }

  /**
   * Creates a new instance of {@link SzWhyEntityResponseData} with the
   * specified parameters.
   *
   * @param whyResults The {@link List} of {@link SzWhyEntityResult} instances.
   * @param entityDataList The {@link List} of {@link SzEntityData} instances.
   *
   * @return The {@link SzWhyEntityResponseData} that was created.
   */
  protected SzWhyEntityResponseData newWhyEntityResponseData(
      List<SzWhyEntityResult> whyResults,
      List<SzEntityData>      entityDataList)
  {
    SzWhyEntityResponseData data = SzWhyEntityResponseData.FACTORY.create();

    data.setWhyResults(whyResults);
    data.setEntities(entityDataList);

    return data;
  }

  /**
   * Parses the specified raw JSON and creates a new {@link
   * SzWhyEntitiesResponse} with the specified parameters.
   *
   * @param rawData The raw JSON text to parse.
   * @param timers The {@link Timers} for the operation.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param withRaw Whether or not the raw JSON should be included in the
   *                response.
   * @param provider The {@link SzApiProvider} to use.
   * @return The {@link SzWhyEntitiesResponse} that was created.
   */
  private SzWhyEntitiesResponse createWhyEntitiesResponse(
      String        rawData,
      Timers        timers,
      UriInfo       uriInfo,
      boolean       withRaw,
      SzApiProvider provider)
  {
    this.processingRawData(timers);
    // parse the result
    JsonObject  json        = JsonUtilities.parseJsonObject(rawData);
    JsonArray   whyArray    = json.getJsonArray("WHY_RESULTS");
    JsonArray   entityArray = json.getJsonArray("ENTITIES");

    List<SzWhyEntitiesResult> whyResults
        = SzWhyEntitiesResult.parseWhyEntitiesResultList(null, whyArray);

    List<SzEntityData> entities = this.parseEntityDataList(
        entityArray, provider::getAttributeClassForFeature);
    this.processedRawData(timers);

    if (whyResults.size() != 1) {
      throw new IllegalStateException(
          "Unexpected number of why-entities result.  Expected only one: "
              + whyResults.size() + " / " + whyResults);
    }

    // construct the response
    SzWhyEntitiesResponse response = this.newWhyEntitiesResponse(
        GET, 200, uriInfo, timers, whyResults.get(0), entities);

    if (withRaw) {
      response.setRawData(rawData);
    }

    return response;
  }

  /**
   * Creates a new instance of {@link SzWhyEntitiesResponse} with the specified
   * parameters:
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param whyResult The {@link SzWhyEntitiesResult} instance.
   * @param entityDataList The {@link List} of {@link SzEntityData} instances.
   *
   * @return The {@link SzWhyEntitiesResponse} that was created.
   *
   */
  protected SzWhyEntitiesResponse newWhyEntitiesResponse(
      SzHttpMethod        httpMethod,
      int                 httpStatusCode,
      UriInfo             uriInfo,
      Timers              timers,
      SzWhyEntitiesResult whyResult,
      List<SzEntityData>  entityDataList)
  {
    return SzWhyEntitiesResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newWhyEntitiesResponseData(whyResult, entityDataList));
  }

  /**
   * Creates a new instance of {@link SzWhyEntitiesResponseData} with the
   * specified parameters:
   *
   * @param whyResult The {@link SzWhyEntitiesResult} instance.
   * @param entityDataList The {@link List} of {@link SzEntityData} instances.
   *
   * @return The {@link SzWhyEntitiesResponseData} that was created.
   *
   */
  protected SzWhyEntitiesResponseData newWhyEntitiesResponseData(
      SzWhyEntitiesResult whyResult,
      List<SzEntityData>  entityDataList)
  {
    SzWhyEntitiesResponseData data = SzWhyEntitiesResponseData.FACTORY.create();
    data.setWhyResult(whyResult);
    data.setEntities(entityDataList);
    return data;
  }
}
