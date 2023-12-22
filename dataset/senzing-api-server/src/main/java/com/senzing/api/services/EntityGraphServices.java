package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Engine;
import com.senzing.util.JsonUtilities;
import com.senzing.util.Timers;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.senzing.api.model.SzHttpMethod.GET;
import static com.senzing.g2.engine.G2Engine.*;

/**
 * Provides entity graph related API services.
 */
@Path("/")
@Produces("application/json; charset=UTF-8")
public class EntityGraphServices implements ServicesSupport {
  /**
   * Implements the <tt>GET /entity-paths</tt> operation.
   *
   * @param fromParam The {@link SzEntityIdentifier} for the start of the
   *                  requested path.
   * @param toParam The {@link SzEntityIdentifier} for the end of the requested
   *                path.
   * @param maxDegrees The maximum number of degrees for the requested path.
   * @param avoidParam The optional {@link List} of {@link SzEntityIdentifier}
   *                   instances identifying those entities to avoid.
   * @param avoidList The optional text describing a {@link List} of {@link
   *                  SzEntityIdentifier} instances identifying those entities
   *                  to avoid.
   * @param forbidAvoided Flag indicating whether or not avoided entities should
   *                      be strictly forbidden (<tt>true</tt>) or only avoided
   *                      if possible (<tt>false</tt>).
   * @param sourcesParam The {@link List} of data source codes that are legal
   *                     for entities included in the path.
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
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included with the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzEntityPathResponse} describing the response.
   */
  @GET
  @Path("entity-paths")
  public SzEntityPathResponse getEntityPath(
      @QueryParam("from")                                         String              fromParam,
      @QueryParam("to")                                           String              toParam,
      @DefaultValue("3") @QueryParam("maxDegrees")                int                 maxDegrees,
      @QueryParam("x")                                            List<String>        avoidParam,
      @QueryParam("avoidEntities")                                String              avoidList,
      @DefaultValue("false") @QueryParam("forbidAvoided")         boolean             forbidAvoided,
      @QueryParam("s")                                            List<String>        sourcesParam,
      @DefaultValue("false") @QueryParam("forceMinimal")          boolean             forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel       detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode       featureMode,
      @DefaultValue("false") @QueryParam("withFeatureStats")      boolean             withFeatureStats,
      @DefaultValue("false") @QueryParam("withInternalFeatures")  boolean             withInternalFeatures,
      @DefaultValue("false") @QueryParam("withRaw")               boolean             withRaw,
      @Context                                                    UriInfo             uriInfo)
  {
    Timers timers = this.newTimers();
    SzApiProvider provider = this.getApiProvider();

    SzEntityIdentifier        from;
    SzEntityIdentifier        to;
    Set<SzEntityIdentifier>   avoidEntities = null;
    List<String>              withSources   = null;
    try {
      if (fromParam == null) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Parameter missing or empty: \"from\".  "
                + "The 'from' entity identifier is required.");
      }
      if (toParam == null) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Parameter missing or empty: \"to\".  "
                + "The 'to' entity identifier is required.");
      }

      try {
        from = this.parseEntityIdentifier(fromParam.trim());
      } catch (Exception e) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Parameter is not formatted correctly: \"from\".");
      }

      try {
        to = this.parseEntityIdentifier(toParam.trim());
      } catch (Exception e) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Parameter is not formatted correctly: \"to\".");
      }

      // check for consistent from/to
      if (from.getClass() != to.getClass()) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Entity identifiers must be consistent types.  from=" + from
                + ", to=" + to);
      }

      // check if we have entities to avoid (or forbid)
      if ((avoidParam != null && avoidParam.size() > 0)
          || (avoidList != null && avoidList.trim().length() > 0))
      {
        // parse the multi-valued parameters
        avoidEntities = this.parseEntityIdentifiers(
            avoidParam, "avoidEntities", GET, uriInfo, timers);

        // check if the avoid list is specified
        if (avoidList != null && avoidList.trim().length() > 0) {
          SzEntityIdentifiers ids = this.parseEntityIdentifiers(avoidList);

          avoidEntities.addAll(ids.getIdentifiers());
        }

        if (!checkConsistent(avoidEntities)) {
          throw this.newBadRequestException(
              GET, uriInfo, timers,
              "Entity identifiers for avoided entities must be of "
                  + "consistent types: " + avoidEntities);
        }
      }

      if (avoidEntities == null || avoidEntities.size() == 0) {
        forbidAvoided = false;
      }

      if (sourcesParam != null && sourcesParam.size() > 0) {
        Set<String> dataSources = provider.getDataSources();
        withSources = new ArrayList<>(dataSources.size());

        for (String source : sourcesParam) {
          if (dataSources.contains(source)) {
            withSources.add(source);
          } else {
            throw this.newBadRequestException(
                GET, uriInfo, timers,
                "Unrecognized data source: " + source);
          }
        }
      }
      if (maxDegrees < 1) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Max degrees must be greater than zero: " + maxDegrees);
      }
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      throw this.newBadRequestException(GET, uriInfo, timers, e.getMessage());
    }

    final String encodedAvoid = (avoidEntities == null)
        ? null : this.nativeJsonEncodeEntityIds(avoidEntities);

    final List<String> originalSources = withSources;

    final String encodedSources = (withSources == null)
        ? null : this.nativeJsonEncodeDataSources(withSources);

    final long flags = (forbidAvoided ? 0L : G2_FIND_PATH_PREFER_EXCLUDE)
                    | this.getFlags(forceMinimal,
                                    detailLevel,
                                    featureMode,
                                    withFeatureStats,
                                    withInternalFeatures,
                                    true);

    try {
      this.enteringQueue(timers);
      String rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        StringBuffer responseDataBuffer = new StringBuffer();

        int result;
        if (from instanceof SzRecordId) {
          String source1 = ((SzRecordId) from).getDataSourceCode();
          String source2 = ((SzRecordId) to).getDataSourceCode();
          String id1 = ((SzRecordId) from).getRecordId();
          String id2 = ((SzRecordId) to).getRecordId();

          if (encodedAvoid == null && encodedSources == null) {
            this.callingNativeAPI(timers, "engine", "findPathByRecordID");
            result = engineApi.findPathByRecordID(source1,
                                                  id1,
                                                  source2,
                                                  id2,
                                                  maxDegrees,
                                                  flags,
                                                  responseDataBuffer);
            this.calledNativeAPI(timers, "engine", "findPathByRecordID");

          } else if (encodedSources == null) {
            this.callingNativeAPI(timers, "engine", "findPathExcludingByRecordID");
            result = engineApi.findPathExcludingByRecordID(
                source1,
                id1,
                source2,
                id2,
                maxDegrees,
                (encodedAvoid != null ? encodedAvoid
                    : nativeJsonEncodeEntityIds(Collections.emptyList())),
                flags,
                responseDataBuffer);
            this.calledNativeAPI(timers, "engine", "findPathExcludingByRecordID");

          } else {
            this.callingNativeAPI(timers, "engine", "findPathIncludingSourceByRecordID");
            result = engineApi.findPathIncludingSourceByRecordID(
                source1,
                id1,
                source2,
                id2,
                maxDegrees,
                (encodedAvoid != null ? encodedAvoid
                    : nativeJsonEncodeEntityIds(Collections.emptyList())),
                (encodedSources != null ? encodedSources
                    : nativeJsonEncodeDataSources(Collections.emptyList())),
                flags,
                responseDataBuffer);
            this.calledNativeAPI(timers, "engine", "findPathIncludingSourceByRecordID");
          }
        } else {
          SzEntityId id1 = (SzEntityId) from;
          SzEntityId id2 = (SzEntityId) to;

          if (encodedAvoid == null && encodedSources == null) {
            this.callingNativeAPI(timers, "engine", "findPathByEntityID");
            result = engineApi.findPathByEntityID(id1.getValue(),
                                                  id2.getValue(),
                                                  maxDegrees,
                                                  flags,
                                                  responseDataBuffer);
            this.calledNativeAPI(timers, "engine", "findPathByEntityID");

          } else if (encodedSources == null) {
            this.callingNativeAPI(timers, "engine", "findPathExcludingByEntityID");
            result = engineApi.findPathExcludingByEntityID(
                id1.getValue(),
                id2.getValue(),
                maxDegrees,
                (encodedAvoid != null ? encodedAvoid
                    : nativeJsonEncodeEntityIds(Collections.emptyList())),
                flags,
                responseDataBuffer);
            this.calledNativeAPI(timers, "engine", "findPathExcludingByEntityID");

          } else {
            this.callingNativeAPI(timers, "engine", "findPathIncludingSourceByEntityID");
            result = engineApi.findPathIncludingSourceByEntityID(
                id1.getValue(),
                id2.getValue(),
                maxDegrees,
                (encodedAvoid != null ? encodedAvoid
                    : nativeJsonEncodeEntityIds(Collections.emptyList())),
                (encodedSources != null ? encodedSources
                    : nativeJsonEncodeDataSources(Collections.emptyList())),
                flags,
                responseDataBuffer);
            this.calledNativeAPI(timers, "engine", "findPathIncludingSourceByEntityID");
          }
        }

        if (result != 0) {
          System.err.println("********* SOURCES: " + originalSources);
          System.err.println("********* ENCODED SOURCES: " + encodedSources);
          throw this.newWebApplicationException(GET, uriInfo, timers, engineApi);
        }

        // parse the raw data
        return responseDataBuffer.toString();
      });

      this.processingRawData(timers);
      JsonObject jsonObject = JsonUtilities.parseJsonObject(rawData);
      SzEntityPathData entityPathData = this.parseEntityPathData(
          jsonObject,
          provider::getAttributeClassForFeature);

      entityPathData.getEntities().forEach(e -> {
        this.postProcessEntityData(e, forceMinimal, detailLevel, featureMode);
      });

      this.processedRawData(timers);

      // construct the response
      SzEntityPathResponse response = this.newEntityPathResponse(
          GET, 200, uriInfo, timers, entityPathData);

      // if including raw data then add it
      if (withRaw) response.setRawData(rawData);

      // return the response
      return response;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      throw this.newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Parses the raw Senzing JSON described by the specified {@link JsonObject}
   * as an instance of {@link SzEntityPathData}.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON.
   * @param getAttributeClassForFeature The mapping function to map features to
   *                                    attribute classes.
   * @return The {@link SzEntityPathData} instance that was created.
   */
  protected SzEntityPathData parseEntityPathData(
      JsonObject              jsonObject,
      Function<String,String> getAttributeClassForFeature)
  {
    return SzEntityPathData.parseEntityPathData(
        jsonObject, getAttributeClassForFeature);
  }

  /**
   * Constructs a new instance of {@link SzEntityPathResponse} with the
   * specified parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param entityPathData The {@link SzEntityPathData} for the response.
   *
   * @return The {@link SzEntityPathResponse} that was created.
   */
  protected SzEntityPathResponse newEntityPathResponse(
      SzHttpMethod      httpMethod,
      int               httpStatusCode,
      UriInfo           uriInfo,
      Timers            timers,
      SzEntityPathData  entityPathData)
  {
    return SzEntityPathResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo), entityPathData);
  }

  /**
   * Implements the <tt>GET /entity-paths</tt> operation.
   *
   * @param entitiesParam The {@link List} of encoded strings describing
   *                      {@link SzEntityIdentifier} instances for the network.
   * @param entityList The encoded {@link String} describing the the
   *                   {@link SzEntityIdentifiers} instance for the network.
   * @param maxDegrees The maximum degrees for finding the paths between the
   *                   identified entities.
   * @param buildOut The number of related entities to build out from the
   *                 found entities.
   * @param maxEntities The maximum number of build-out entities to return.
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
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included with the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @return The {@link SzEntityNetworkResponse} describing the response.
   */
  @GET
  @Path("entity-networks")
  public SzEntityNetworkResponse getEntityNetwork(
      @QueryParam("e")                                            List<String>  entitiesParam,
      @QueryParam("entities")                                     String        entityList,
      @DefaultValue("3")      @QueryParam("maxDegrees")           int           maxDegrees,
      @DefaultValue("1")      @QueryParam("buildOut")             int           buildOut,
      @DefaultValue("1000")   @QueryParam("maxEntities")          int           maxEntities,
      @DefaultValue("false")  @QueryParam("forceMinimal")         boolean       forceMinimal,
      @DefaultValue("VERBOSE") @QueryParam("detailLevel")         SzDetailLevel detailLevel,
      @DefaultValue("WITH_DUPLICATES") @QueryParam("featureMode") SzFeatureMode featureMode,
      @DefaultValue("false") @QueryParam("withFeatureStats")      boolean       withFeatureStats,
      @DefaultValue("false") @QueryParam("withInternalFeatures")  boolean       withInternalFeatures,
      @DefaultValue("false")  @QueryParam("withRaw")              boolean       withRaw,
      @Context                                                    UriInfo       uriInfo)
  {
    Timers timers = this.newTimers();
    SzApiProvider provider = this.getApiProvider();

    Set<SzEntityIdentifier> entities;
    // check for consistent entity IDs
    try {
      if ((entitiesParam == null || entitiesParam.isEmpty())
          && ((entityList == null) || entityList.isEmpty()))
      {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "One of the following parameters is required to specify at least "
                + "one entity: 'e' or 'entities'.  "
                + "At least one of 'e' or 'entities' parameter must specify at "
                + "least one entity identifier.");
      }

      entities = this.parseEntityIdentifiers(
          entitiesParam, "e", GET, uriInfo, timers);

      if (entityList != null && entityList.trim().length() > 0) {
        SzEntityIdentifiers ids = this.parseEntityIdentifiers(entityList);

        entities.addAll(ids.getIdentifiers());
      }


      if (!this.checkConsistent(entities)) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Entity identifiers for entities must be of consistent "
                + "types: " + entities);
      }

      if (maxDegrees < 0) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Max degrees must not be negative: " + maxDegrees);
      }

      if (buildOut < 0) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Build out must be zero or greater: " + buildOut);
      }

      if (maxEntities < 0) {
        throw this.newBadRequestException(
            GET, uriInfo, timers,
            "Max entities must be zero or greater: " + maxEntities);
      }

    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw this.newBadRequestException(GET, uriInfo, timers, e.getMessage());
    }

    final String encodedEntityIds = (entities == null)
        ? null : this.nativeJsonEncodeEntityIds(entities);

    final long flags = this.getFlags(forceMinimal,
                                     detailLevel,
                                     featureMode,
                                     withFeatureStats,
                                     withInternalFeatures,
                                     true);

    try {
      this.enteringQueue(timers);
      String rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        StringBuffer sb = new StringBuffer();

        int result;

        if (entities.iterator().next().getClass() == SzRecordId.class) {
          this.callingNativeAPI(timers, "engine", "findNetworkByRecordID");
          result = engineApi.findNetworkByRecordID(
              encodedEntityIds,
              maxDegrees,
              buildOut,
              maxEntities,
              flags,
              sb);
          this.calledNativeAPI(timers, "engine", "findNetworkByRecordID");

        } else {
          this.callingNativeAPI(timers, "engine", "findNetworkByEntityID");
          result = engineApi.findNetworkByEntityID(
              encodedEntityIds,
              maxDegrees,
              buildOut,
              maxEntities,
              flags,
              sb);
          this.calledNativeAPI(timers, "engine", "findNetworkByEntityID");
        }

        if (result != 0) {
          throw this.newWebApplicationException(GET, uriInfo, timers, engineApi);
        }

        // parse the raw data
        return sb.toString();
      });

      this.processingRawData(timers);

      JsonObject jsonObject = JsonUtilities.parseJsonObject(rawData);

      SzEntityNetworkData entityNetworkData = this.parseEntityNetworkData(
              jsonObject,
              provider::getAttributeClassForFeature);

      entityNetworkData.getEntities().forEach(e -> {
        this.postProcessEntityData(e, forceMinimal, detailLevel, featureMode);
      });

      this.processedRawData(timers);

      // construct the response
      SzEntityNetworkResponse response = this.newEntityNetworkResponse(
          GET, 200, uriInfo, timers, entityNetworkData);

      // if including raw data then add it
      if (withRaw) response.setRawData(rawData);

      // return the response
      return response;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      throw this.newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Parses the raw JSON described by the specified {@link JsonObject} as an
   * instance of {@link SzEntityNetworkData}.
   *
   * @param jsonObject The {@link JsonObject} describing the JSON.
   * @param getAttributeClassForFeature The function mapping features to
   *                                    attribute classes.
   * @return The parsed {@link SzEntityNetworkData} instance.
   */
  protected SzEntityNetworkData parseEntityNetworkData(
      JsonObject              jsonObject,
      Function<String,String> getAttributeClassForFeature)
  {
    return SzEntityNetworkData.parseEntityNetworkData(
        jsonObject, getAttributeClassForFeature);
  }

  /**
   * Creates a new instance of {@link SzEntityNetworkResponse} with the
   * specified parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param entityNetworkData The {@link SzEntityNetworkData} for the response.
   *
   * @return The {@link SzEntityNetworkResponse} that was created.
   */
  protected SzEntityNetworkResponse newEntityNetworkResponse(
      SzHttpMethod        httpMethod,
      int                 httpStatusCode,
      UriInfo             uriInfo,
      Timers              timers,
      SzEntityNetworkData entityNetworkData)
  {
    return SzEntityNetworkResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo), entityNetworkData);
  }

  /**
   * Checks if the entity ID's in the specified list are of a consistent type.
   *
   * @param entities The list of {@link SzEntityIdentifier} instances.
   *
   * @return <tt>true</tt> if they are consistent, and <tt>false</tt> if not.
   */
  protected boolean checkConsistent(Set<SzEntityIdentifier> entities) {
    if (entities != null && !entities.isEmpty()) {
      Class idClass = null;
      for (SzEntityIdentifier id : entities) {
        if (idClass == null) {
          idClass = id.getClass();
        } else if (idClass != id.getClass()) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Similar to the method {@link
   * #newPossiblyNotFoundException(SzHttpMethod, UriInfo, Timers, G2Engine)},
   * this method either throws a {@link BadRequestException} if an entity is
   * not found or an {@link InternalServerErrorException}.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param engineApi The {@link G2Engine} instance to determine if the entity
   *                  was not found or if there was a system error.
   * @return The appropriate {@link WebApplicationException}.
   */
  protected WebApplicationException newWebApplicationException(
      SzHttpMethod  httpMethod,
      UriInfo       uriInfo,
      Timers        timers,
      G2Engine      engineApi)
  {
    int errorCode = engineApi.getLastExceptionCode();
    if (errorCode == ENTITY_NOT_FOUND_CODE
        || errorCode == RECORD_NOT_FOUND_CODE) {
      return this.newBadRequestException(
          httpMethod, uriInfo, timers, engineApi);
    }
    return this.newInternalServerErrorException(
        httpMethod, uriInfo, timers, engineApi);
  }
}
