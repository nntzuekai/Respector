package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Config;
import com.senzing.g2.engine.G2ConfigMgr;
import com.senzing.g2.engine.G2Engine;
import com.senzing.g2.engine.Result;
import com.senzing.util.JsonUtilities;
import com.senzing.util.Timers;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import java.util.*;

import static com.senzing.api.model.SzHttpMethod.*;

/**
 * Provides config related API services.
 */
@Produces("application/json; charset=UTF-8")
@Path("/")
public class ConfigServices implements ServicesSupport {
  /**
   * The maximum length for comments used when adding a config via the
   * {@link G2ConfigMgr#addConfig(String, String, Result)} function.
   */
  private static final int MAX_CONFIG_COMMENT_LENGTH = 150;

  /**
   * Provides the implementation of <tt>GET /data-sources</tt>.
   *
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included in the response.
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzDataSourcesResponse} describing the response.
   */
  @GET
  @Path("data-sources")
  public SzDataSourcesResponse getDataSources(
      @DefaultValue("false") @QueryParam("withRaw") boolean withRaw,
      @Context UriInfo uriInfo) {
    Timers timers = this.newTimers();
    SzApiProvider provider = this.getApiProvider();

    try {
      // get the engine API and the config API
      G2Engine engineApi = provider.getEngineApi();
      G2Config configApi = provider.getConfigApi();

      this.enteringQueue(timers);
      String rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);
        return this.doGetDataSources(GET, uriInfo, timers, engineApi, configApi);
      });

      return this.buildDataSourcesResponse(
          GET, uriInfo, timers, rawData, withRaw);

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Provides the implementation of <tt>GET /data-sources/{dataSourceCode}</tt>.
   *
   * @param dataSourceCode The data source code from the URL path.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included in the response.
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzDataSourceResponse} describing the response.
   */
  @GET
  @Path("data-sources/{dataSourceCode}")
  public SzDataSourceResponse getDataSource(
      @PathParam("dataSourceCode") String dataSourceCode,
      @DefaultValue("false") @QueryParam("withRaw") boolean withRaw,
      @Context UriInfo uriInfo)
  {
    Timers timers = this.newTimers();
    SzApiProvider provider = this.getApiProvider();

    try {
      // get the engine API and the config API
      G2Engine engineApi = provider.getEngineApi();
      G2Config configApi = provider.getConfigApi();

      this.enteringQueue(timers);
      String rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);
        String code = dataSourceCode.trim().toUpperCase();
        if (!provider.getDataSources(code).contains(code)) {
          throw this.newNotFoundException(
              GET, uriInfo, timers,
              "The specified data source code was not recognized: " + code);
        }
        return this.doGetDataSources(GET, uriInfo, timers, engineApi, configApi);
      });

      return this.buildDataSourceResponse(
          GET, uriInfo, timers, dataSourceCode, rawData, withRaw);

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Handles getting the data sources for an operation.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param engineApi The {@link G2Engine} instance to use.
   * @param configApi The {@link G2Config} instance to use.
   *
   * @return The raw data describing the data sources.
   */
  protected String doGetDataSources(SzHttpMethod  httpMethod,
                                    UriInfo       uriInfo,
                                    Timers        timers,
                                    G2Engine      engineApi,
                                    G2Config      configApi)
  {
    String config = this.exportConfig(GET, uriInfo, timers, engineApi);

    Long configId = null;
    try {
      Result<Long> configIdResult = new Result<>();

      // load into a config object by ID
      this.callingNativeAPI(timers, "config", "load");
      int returnCode = configApi.load(config, configIdResult);
      this.calledNativeAPI(timers, "config", "load");

      if (returnCode != 0) {
        throw this.newInternalServerErrorException(
            GET, uriInfo, timers, configApi);
      }

      // get the config ID
      configId = configIdResult.getValue();

      // create a string buffer for the result
      StringBuffer sb = new StringBuffer();

      // list the data sources on the config
      this.callingNativeAPI(timers, "config", "listDataSources");
      returnCode = configApi.listDataSources(configId, sb);
      if (returnCode != 0) {
        throw this.newInternalServerErrorException(
            GET, uriInfo, timers, configApi);
      }
      this.calledNativeAPI(timers, "config", "listDataSources");

      return sb.toString();

    } finally {
      if (configId != null) {
        configApi.close(configId);
      }
    }
  }

  /**
   * Builds an {@link SzDataSourcesResponse} from the specified raw data using
   * the specified parameters of the request.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param rawData The raw data JSON text describing the data sources.
   * @param withRaw <tt>true</tt> if the raw data should be included in the
   *                response, and <tt>false</tt> if it should be excluded.
   *
   * @return The {@link SzDataSourcesResponse} created with the specified
   *         parameters.
   */
  protected SzDataSourcesResponse buildDataSourcesResponse(
      SzHttpMethod httpMethod,
      UriInfo uriInfo,
      Timers timers,
      String rawData,
      boolean withRaw)
  {
    this.processingRawData(timers);
    // parse the raw data
    JsonObject jsonObject = JsonUtilities.parseJsonObject(rawData);

    // get the array and construct the response
    JsonArray jsonArray = jsonObject.getJsonArray("DATA_SOURCES");
    List<SzDataSource> dataSources = this.parseDataSourceList(jsonArray);

    SzDataSourcesResponse response = this.newDataSourcesResponse(
        httpMethod, 200, uriInfo, timers, dataSources);
    this.processedRawData(timers);

    // if including raw data then add it
    if (withRaw) response.setRawData(rawData);

    return response;
  }

  /**
   * Parses the specified {@link JsonArray} describing the data sources in the
   * raw format and produces a {@link List} of {@link SzDataSource} instances.
   *
   * @param jsonArray The {@link JsonArray} describing the data sources.
   *
   * @return The created {@link List} of {@link SzDataSource} instances.
   */
  protected List<SzDataSource> parseDataSourceList(JsonArray jsonArray) {
    return SzDataSource.parseDataSourceList(null, jsonArray);
  }

  /**
   * Creates a new instance of {@link SzDataSourceResponse} and configures it
   * with the specified {@link List} of {@link SzDataSource} instances.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param dataSources The {@link List} of {@link SzDataSource} instances.
   *
   * @return The newly created {@link SzDataSourcesResponse}.
   */
  protected SzDataSourcesResponse newDataSourcesResponse(
      SzHttpMethod        httpMethod,
      int                 httpStatusCode,
      UriInfo             uriInfo,
      Timers              timers,
      List<SzDataSource>  dataSources)
  {
    return SzDataSourcesResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newDataSourcesResponseData(dataSources));
  }

  /**
   * Creates a new instance of {@link SzDataSourceResponseData} and configures
   * it with the specified {@link List} of {@link SzDataSource} instances.
   *
   * @param dataSources The {@link List} of {@link SzDataSource} instances.
   *
   * @return The newly created {@link SzDataSourcesResponse}.
   */
  protected SzDataSourcesResponseData newDataSourcesResponseData(
      List<SzDataSource>  dataSources)
  {
    return SzDataSourcesResponseData.FACTORY.create(dataSources);
  }

  /**
   * Builds an {@link SzDataSourceResponse} from the specified raw data using
   * the specified parameters of the request.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param dataSourceCode The data source code for the data source.
   * @param rawData The raw data JSON text describing the data sources.
   * @param withRaw <tt>true</tt> if the raw data should be included in the
   *                response, and <tt>false</tt> if it should be excluded.
   *
   * @return The {@link SzDataSourceResponse} created with the specified
   *         parameters.
   */
  protected SzDataSourceResponse buildDataSourceResponse(
      SzHttpMethod  httpMethod,
      UriInfo       uriInfo,
      Timers        timers,
      String        dataSourceCode,
      String        rawData,
      boolean       withRaw)
  {
    this.processingRawData(timers);
    dataSourceCode = dataSourceCode.trim().toUpperCase();

    // parse the raw data
    JsonObject jsonObject = JsonUtilities.parseJsonObject(rawData);

    // get the array of data sources
    JsonArray jsonArray = jsonObject.getJsonArray("DATA_SOURCES");

    // find the one matching the specified data source code
    jsonObject = null;
    for (JsonObject jsonObj : jsonArray.getValuesAs(JsonObject.class)) {
      String code = JsonUtilities.getString(jsonObj, "DSRC_CODE");
      if (code.contentEquals(dataSourceCode)) {
        jsonObject = jsonObj;
        break;
      }
    }

    // check if not found
    if (jsonObject == null) {
      throw this.newNotFoundException(
          httpMethod, uriInfo, timers,
          "The specified data source was not recognized: " + dataSourceCode);
    }

    // parse the data source
    SzDataSource dataSource = this.parseDataSource(jsonObject);

    // build the response
    SzDataSourceResponse response = this.newDataSourceResponse(
        httpMethod, 200, uriInfo, timers, dataSource);

    this.processedRawData(timers);

    // if including raw data then add it
    if (withRaw) response.setRawData(JsonUtilities.toJsonText(jsonObject));

    return response;
  }

  /**
   * Parses the JSON described by the specified {@link JsonObject} as a
   * data source in the raw format and produces an instance of {@link
   * SzDataSource}.
   *
   * @param jsonObject The {@link JsonObject} describing the data source in
   *                   the raw format.
   *
   * @return The newly created and configured {@link SzDataSource}.
   */
  protected SzDataSource parseDataSource(JsonObject jsonObject) {
    return SzDataSource.parseDataSource(null, jsonObject);
  }

  /**
   * Creates a new {@link SzDataSourceResponse} with the specified parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param dataSource The {@link SzDataSource} instance.
   *
   * @return The newly created {@link SzDataSourceResponse}.
   */
  protected SzDataSourceResponse newDataSourceResponse(
      SzHttpMethod  httpMethod,
      int           httpStatusCode,
      UriInfo       uriInfo,
      Timers        timers,
      SzDataSource  dataSource)
  {
    return SzDataSourceResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newDataSourceResponseData(dataSource));
  }

  /**
   * Creates a new {@link SzDataSourceResponse} with the specified parameters.
   *
   * @param dataSource The {@link SzDataSource} instance.
   *
   * @return The newly created {@link SzDataSourceResponse}.
   */
  protected SzDataSourceResponseData newDataSourceResponseData(
      SzDataSource  dataSource)
  {
    return SzDataSourceResponseData.FACTORY.create(dataSource);
  }

  /**
   * Provides the implementation of <tt>POST /data-sources</tt>.
   *
   * @param dataSourceCodes The {@link List} of data source codes from the
   *                        query parameters.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included in the response.
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzDataSourcesResponse} describing the response.
   */
  @POST
  @Path("data-sources")
  public SzDataSourcesResponse addDataSources(
      @QueryParam("dataSource") List<String> dataSourceCodes,
      @DefaultValue("false") @QueryParam("withRaw") boolean withRaw,
      @Context UriInfo uriInfo,
      String dataSourcesInBody)
  {
    Timers timers = this.newTimers();

    SzApiProvider provider = this.getApiProvider();

    this.ensureConfigChangesAllowed(provider, POST, uriInfo, timers);

    Map<String, SzDataSource> map = new LinkedHashMap<>();
    for (String code : dataSourceCodes) {
      SzDataSource dataSource = this.newDataSource(code, null);
      map.put(dataSource.getDataSourceCode(), dataSource);
    }

    // get the body data sources
    if (dataSourcesInBody != null && dataSourcesInBody.trim().length() > 0) {
      // trim the string
      dataSourcesInBody = dataSourcesInBody.trim();

      // create a list for the parsed data sources
      SzDataSourceDescriptors descriptors = null;

      try {
        descriptors = this.parseDataSourceDescriptors(dataSourcesInBody);

      } catch (Exception e) {
        throw this.newBadRequestException(POST, uriInfo, timers, e);
      }

      // loop through the data sources and put them in the map
      for (SzDataSourceDescriptor desc : descriptors.getDescriptors()) {
        SzDataSource dataSource = desc.toDataSource();
        map.put(dataSource.getDataSourceCode(), dataSource);
      }
    }

    return this.doAddDataSources(POST, map.values(), uriInfo, withRaw, timers);
  }

  /**
   * Constructs a new instance of {@link SzDataSource} with specified
   * data source code and optional data source ID.
   *
   * @param dataSourceCode The data source code to construct the instance with.
   * @param dataSourceId The data source ID to construct with or <tt>null</tt>
   *                     not known.
   *
   * @return The newly created instance of {@link SzDataSource}.
   */
  protected SzDataSource newDataSource(String   dataSourceCode,
                                       Integer  dataSourceId)
  {
    return SzDataSource.FACTORY.create(dataSourceCode, dataSourceId);
  }

  /**
   * Parses the specified text as an instance of {@link
   * SzDataSourceDescriptors}.
   *
   * @param text The text to parse.
   *
   * @return The parsed instance of {@link SzDataSourceDescriptors}.
   */
  protected SzDataSourceDescriptors parseDataSourceDescriptors(String text) {
    return SzDataSourceDescriptors.valueOf(text);
  }

  /**
   * Internal method for adding one or more data sources.
   *
   * @param httpMethod The {@link SzHttpMethod} for the operation.
   * @param dataSources The {@link Collection} of {@link SzDataSource} instances
   *                    describing the data sources to be added.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param withRaw Whether or not raw data is being requested.
   * @param timers The {@link Timers} for the operation.
   * @return An SzDataSourcesResponse with all the configured data sources.
   */
  protected SzDataSourcesResponse doAddDataSources(
      SzHttpMethod              httpMethod,
      Collection<SzDataSource>  dataSources,
      UriInfo                   uriInfo,
      boolean                   withRaw,
      Timers                    timers)
  {
    SzApiProvider provider = this.getApiProvider();

    try {
      // get the engine API and the config API
      G2Engine    engineApi     = provider.getEngineApi();
      G2Config    configApi     = provider.getConfigApi();
      G2ConfigMgr configMgrApi  = provider.getConfigMgrApi();
      Set<String> createdSet    = new LinkedHashSet<>();

      if (configMgrApi == null) {
        throw this.newForbiddenException(
            httpMethod, uriInfo, timers, "Configuration changes not permitted.");
      }

      // loop until the provider has the data source code we are looking for
      this.enteringQueue(timers);
      String rawData = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get an array of the data source codes
        String[] arr = dataSources.stream()
            .map(ds -> ds.getDataSourceCode())
            .toArray(String[]::new);

        // create a set of the data source codes
        Set<String> dataSourceCodes = new LinkedHashSet<>();
        for (String code : arr) {
          dataSourceCodes.add(code);
        }

        while (!provider.getDataSources(arr).containsAll(dataSourceCodes)) {
          // get the current default config
          Result<Long> result = new Result<>();
          String configJSON = this.getDefaultConfig(httpMethod,
                                                    uriInfo,
                                                    configMgrApi,
                                                    timers,
                                                    result);
          Long defaultConfigId = result.getValue();

          Long configHandle = null;
          try {
            Result<Long> configHandleResult = new Result<>();
            // load into a config object by ID
            this.callingNativeAPI(timers, "config", "load");
            int returnCode = configApi.load(configJSON, configHandleResult);
            this.calledNativeAPI(timers, "config", "load");

            if (returnCode != 0) {
              throw this.newInternalServerErrorException(
                  httpMethod, uriInfo, timers, configApi);
            }

            configHandle = configHandleResult.getValue();

            // get the current data sources
            Map<String, SzDataSource> dataSourceMap
                = this.getDataSourcesMap(httpMethod,
                                         uriInfo,
                                         configApi,
                                         timers,
                                         configHandle);

            // check for consistency against existing data sources
            for (SzDataSource dataSource : dataSources) {
              String        dataSourceCode  = dataSource.getDataSourceCode();
              Integer       dataSourceId    = dataSource.getDataSourceId();
              SzDataSource  existingDS      = dataSourceMap.get(dataSourceCode);
              if (existingDS != null && dataSourceId != null
                  && !dataSourceId.equals(existingDS.getDataSourceId()))
              {
                throw this.newBadRequestException(
                    httpMethod, uriInfo, timers,
                    "At least one data source already exists, but "
                    + "with a different data source ID.  specified=[ "
                    + dataSource + " ], existing=[ " + existingDS + " ]");
              }
            }

            // loop through the data sources that need to be created
            for (SzDataSource dataSource : dataSources) {
              // skip attempting to create the data source if it already exists
              if (dataSourceMap.containsKey(dataSource.getDataSourceCode())) {
                continue;
              }

              // add the data source to the config without a data source ID
              this.callingNativeAPI(timers, "config", "addDataSource");
              returnCode = configApi.addDataSource(
                  configHandle, dataSource.toNativeJson(), new StringBuffer());
              this.calledNativeAPI(timers, "config", "addDataSource");

              if (returnCode != 0) {
                throw this.newInternalServerErrorException(
                    httpMethod, uriInfo, timers, configApi);
              }

              createdSet.add(dataSource.getDataSourceCode());
            }

            if (createdSet.size() > 0) {
              this.updateDefaultConfig(
                  httpMethod,
                  uriInfo,
                  configApi,
                  configMgrApi,
                  timers,
                  defaultConfigId,
                  configHandle,
                  "Added data source(s): " + createdSet);
            }

          } finally {
            if (configHandle != null) {
              configApi.close(configHandle);
            }
          }
        }

        // return the raw data sources string
        return this.doGetDataSources(
            httpMethod, uriInfo, timers, engineApi, configApi);

      });

      // return the data sources response
      return this.buildDataSourcesResponse(
          httpMethod, uriInfo, timers, rawData, withRaw);

    } catch (ServerErrorException e) {
      e.printStackTrace();
      throw e;

    } catch (WebApplicationException e) {
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw this.newInternalServerErrorException(
          httpMethod, uriInfo, timers, e);
    }
  }

  /**
   * Obtains the JSON text for the configuration that is currently
   * designated as the default configuration.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param configMgrApi The {@link G2ConfigMgr} instance to use.
   * @param timers The {@link Timers} for the operation.
   * @param configId The {@link Result} object to populate with the config ID.
   * @return The JSON text for the default configuration.
   */
  protected String getDefaultConfig(SzHttpMethod  httpMethod,
                                    UriInfo       uriInfo,
                                    G2ConfigMgr   configMgrApi,
                                    Timers        timers,
                                    Result<Long>  configId)
  {
    synchronized (configMgrApi) {
      this.callingNativeAPI(timers, "configMgr", "getDefaultConfigID");
      int returnCode = configMgrApi.getDefaultConfigID(configId);
      this.calledNativeAPI(timers, "configMgr", "getDefaultConfigID");
      // check the return code
      if (returnCode != 0) {
        throw this.newInternalServerErrorException(
            httpMethod, uriInfo, timers, configMgrApi);
      }

      long defaultConfigId = configId.getValue();
      StringBuffer sb = new StringBuffer();

      // get the config
      this.callingNativeAPI(timers, "configMgr", "getConfig");
      returnCode = configMgrApi.getConfig(defaultConfigId, sb);
      this.calledNativeAPI(timers, "configMgr", "getConfig");
      if (returnCode != 0) {
        throw this.newInternalServerErrorException(
            httpMethod, uriInfo, timers, configMgrApi);
      }

      // get the JSON
      return sb.toString();
    }
  }

  /**
   * Replaces the current default configuration with the newly updated
   * default configuration assuming the default configuration has not changed
   * since the updates were made.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} the request.
   * @param configApi The {@link G2Config} instance to use.
   * @param configMgrApi The {@link G2ConfigMgr} instance to use.
   * @param timers The {@link Timers} for the operation.
   * @param defaultConfigId The default configuration ID that was configured
   *                        prior to the modifications.
   * @param configHandle The configuration handle for the modified
   *                     configuration that will be set as the new default.
   * @param configComment The comments to associate with the modified
   *                      configuration.
   * @return <tt>true</tt> if the current default configuration ID is the same
   *         as the specified configuration ID, and <tt>false</tt> if it has
   *         changed and the update was aborted.
   */
  protected boolean updateDefaultConfig(SzHttpMethod  httpMethod,
                                        UriInfo       uriInfo,
                                        G2Config      configApi,
                                        G2ConfigMgr   configMgrApi,
                                        Timers        timers,
                                        long          defaultConfigId,
                                        long          configHandle,
                                        String        configComment)
  {
    StringBuffer sb     = new StringBuffer();
    Result<Long> result = new Result<>();

    // check the size of the config comment
    if (configComment.length() > MAX_CONFIG_COMMENT_LENGTH) {
      configComment = configComment.substring(0, MAX_CONFIG_COMMENT_LENGTH - 4)
          + "....";
    }
    
    // convert the config to a JSON string
    this.callingNativeAPI(timers, "config", "save");
    int returnCode = configApi.save(configHandle, sb);
    if (returnCode != 0) {
      throw this.newInternalServerErrorException(
          httpMethod, uriInfo, timers, configApi);
    }
    this.calledNativeAPI(timers, "config", "save");

    String configJSON = sb.toString();

    // save the configuration
    this.obtainingLock(timers, "configMgrApi");
    synchronized (configMgrApi) {
      this.obtainedLock(timers, "configMgrApi");
      this.callingNativeAPI(timers, "configMgr", "addConfig");
      returnCode = configMgrApi.addConfig(configJSON, configComment, result);

      if (returnCode != 0) {
        throw this.newInternalServerErrorException(
            httpMethod, uriInfo, timers, configMgrApi);
      }
      this.calledNativeAPI(timers, "configMgr", "addConfig");

      // get the config ID for the newly saved config
      long newConfigId = result.getValue();

      this.callingNativeAPI(timers, "configMgr", "getDefaultConfigID");
      returnCode = configMgrApi.getDefaultConfigID(result);
      if (returnCode != 0) {
        throw this.newInternalServerErrorException(
            httpMethod, uriInfo, timers, configMgrApi);
      }
      this.calledNativeAPI(timers, "configMgr", "getDefaultConfigID");

      // check if the default configuration ID has changed
      if (!result.getValue().equals(defaultConfigId)) {
        System.out.println(
            "Concurrent configuration change detected.  Retrying...");
        return false;
      }

      this.callingNativeAPI(timers, "configMgr", "setDefaultConfigID");
      returnCode = configMgrApi.setDefaultConfigID(newConfigId);
      if (returnCode != 0) {
        throw this.newInternalServerErrorException(
            httpMethod, uriInfo, timers, configMgrApi);
      }
      this.calledNativeAPI(timers, "configMgr", "setDefaultConfigID");
    }
    return true;
  }

  /**
   * Parses the data sources in the configuration with the specified
   * configuration handle as {@link SzDataSource} instances and returns a map
   * of data source code keys to {@link SzDataSource} instances.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param configApi The {@link G2Config} instance to use.
   * @param timers The {@link Timers} for the operation.
   * @param configHandle The configuration handle for the config to get the
   *                     data sources from.
   * @return The {@link Map} of {@link String} data source code keys to
   *         {@link SzDataSource} instances.
   */
  protected Map<String, SzDataSource> getDataSourcesMap(
      SzHttpMethod  httpMethod,
      UriInfo       uriInfo,
      G2Config      configApi,
      Timers        timers,
      long          configHandle)
  {
    StringBuffer sb = new StringBuffer();
    this.callingNativeAPI(timers, "config", "listDataSources");
    int returnCode = configApi.listDataSources(configHandle, sb);
    this.calledNativeAPI(timers, "config", "listDataSources");

    if (returnCode != 0) {
      throw this.newInternalServerErrorException(
          httpMethod, uriInfo, timers, configApi);
    }
    JsonObject jsonObject = JsonUtilities.parseJsonObject(sb.toString());
    JsonArray jsonArray = jsonObject.getJsonArray("DATA_SOURCES");

    List<SzDataSource> dataSources = this.parseDataSourceList(jsonArray);

    Map<String, SzDataSource> dataSourceMap = new LinkedHashMap<>();
    dataSources.forEach(dataSource -> {
      dataSourceMap.put(dataSource.getDataSourceCode(), dataSource);
    });

    return dataSourceMap;
  }

  /**
   * Provides the implementation of <tt>GET /attribute-types</tt>.
   *
   * @param withInternal Boolean flag from the query parameter indicating if
   *                     internal attribute types should be included in the
   *                     response.
   * @param attributeClass The optional attribute class for filtering the
   *                       attribute types to be include in the response.
   * @param featureType The optional feature type for filtering the attribute
   *                    types to be include in the response.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included in the response.
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzAttributeTypesResponse} describing the response.
   */
  @GET
  @Path("attribute-types")
  public SzAttributeTypesResponse getAttributeTypes(
      @DefaultValue("false") @QueryParam("withInternal") boolean withInternal,
      @QueryParam("attributeClass")                      String  attributeClass,
      @QueryParam("featureType")                         String  featureType,
      @DefaultValue("false") @QueryParam("withRaw")      boolean withRaw,
      @Context                                           UriInfo uriInfo)
  {
    Timers timers = this.newTimers();
    SzApiProvider provider = this.getApiProvider();

    SzAttributeClass ac = null;
    if (attributeClass != null && attributeClass.trim().length() > 0) {
      try {
        ac = SzAttributeClass.valueOf(attributeClass.trim().toUpperCase());

      } catch (IllegalArgumentException e) {
        throw this.newBadRequestException(
            GET, uriInfo, timers, "Unrecognized attribute class: " + attributeClass);
      }
    }
    final SzAttributeClass attrClass = ac;
    final String featType
        = ((featureType != null && featureType.trim().length() > 0)
        ? featureType.trim() : null);

    try {
      this.enteringQueue(timers);
      JsonObject configRoot = provider.executeInThread(() -> {
        this.exitingQueue(timers);

        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        return this.getCurrentConfigRoot(GET, uriInfo, timers, engineApi);
      });
      
      this.processingRawData(timers);
      // get the array and construct the response
      JsonArray jsonArray = configRoot.getJsonArray("CFG_ATTR");

      List<SzAttributeType> attrTypes = this.parseAttributeTypeList(jsonArray);

      // check if filtering out internal attribute types
      if (!withInternal) {
        attrTypes.removeIf(SzAttributeType::isInternal);
      }

      // filter by attribute class if filter is specified
      if (attrClass != null) {
        attrTypes.removeIf(
            attrType -> (!attrType.getAttributeClass().equals(attrClass)));
      }

      // filter by feature type if filter is specified
      if (featType != null) {
        attrTypes.removeIf(
            at -> (!featType.equalsIgnoreCase(at.getFeatureType())));
      }
      processedRawData(timers);

      // build the response
      SzAttributeTypesResponse response = this.newAttributeTypesResponse(
          GET, 200, uriInfo, timers, attrTypes);

      // if including raw data then add it
      if (withRaw) {
        this.processingRawData(timers);
        // check if we need to filter the raw value as well
        if (!withInternal || attrClass != null || featType != null) {
          JsonArrayBuilder jab = Json.createArrayBuilder();
          for (JsonObject jsonObj : jsonArray.getValuesAs(JsonObject.class)) {
            if (!withInternal) {
              if (SzAttributeType.interpretBoolean(jsonObj, "INTERNAL")) {
                continue;
              }
            }
            if (attrClass != null) {
              String rawAC = jsonObj.getString("ATTR_CLASS");
              if (!attrClass.getRawValue().equalsIgnoreCase(rawAC)) continue;
            }
            if (featType != null) {
              String ft = jsonObj.getString("FTYPE_CODE");
              if (!featType.equalsIgnoreCase(ft)) continue;
            }
            jab.add(jsonObj);
          }
          jsonArray = jab.build();
        }
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("CFG_ATTR", jsonArray);
        String rawData = JsonUtilities.toJsonText(job.build());
        this.processedRawData(timers);
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
      throw newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Parses the specified {@link JsonArray} describing the attribute types in
   * the raw format and produces a {@link List} of {@link SzAttributeType}
   * instances.
   *
   * @param jsonArray The {@link JsonArray} describing the data sources.
   *
   * @return The created {@link List} of {@link SzAttributeType} instances.
   */
  protected List<SzAttributeType> parseAttributeTypeList(JsonArray jsonArray) {
    return SzAttributeType.parseAttributeTypeList(null, jsonArray);
  }

  /**
   * Creates a new instance of {@link SzAttributeTypesResponse} and configures
   * it with the specified {@link List} of {@link SzAttributeType} instances.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param attributeTypes The {@link List} of {@link SzAttributeType}
   *                       instances.
   *
   * @return The newly created {@link SzAttributeTypesResponse}.
   */
  protected SzAttributeTypesResponse newAttributeTypesResponse(
      SzHttpMethod          httpMethod,
      int                   httpStatusCode,
      UriInfo               uriInfo,
      Timers                timers,
      List<SzAttributeType> attributeTypes)
  {
    return SzAttributeTypesResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newAttributeTypesResponseData(attributeTypes));
  }

  /**
   * Creates a new instance of {@link SzAttributeTypesResponseData} with the
   * specified {@link List} of {@link SzAttributeType} instances.
   *
   * @param attributeTypes The {@link Collection} of {@link SzAttributeType}
   *                       instances.
   *
   * @return The newly created {@link SzAttributeTypesResponse}.
   */
  protected SzAttributeTypesResponseData newAttributeTypesResponseData(
      Collection<? extends SzAttributeType> attributeTypes)
  {
    return SzAttributeTypesResponseData.FACTORY.create(attributeTypes);
  }

  /**
   * Provides the implementation of
   * <tt>GET /attribute-types/{attributeTypeCode}</tt>.
   *
   * @param attributeCode The attribute code from the URI path identifying the
   *                      attribute type.
   * @param withRaw Whether or not the raw native Senzing JSON should be
   *                included in the response.
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzAttributeTypeResponse} describing the response.
   */
  @GET
  @Path("attribute-types/{attributeCode}")
  public SzAttributeTypeResponse getAttributeType(
      @PathParam("attributeCode")                   String  attributeCode,
      @DefaultValue("false") @QueryParam("withRaw") boolean withRaw,
      @Context                                      UriInfo uriInfo)
  {
    Timers timers = this.newTimers();
    SzApiProvider provider = this.getApiProvider();

    try {
      this.enteringQueue(timers);
      JsonObject configRoot = provider.executeInThread(() -> {
        this.exitingQueue(timers);
        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        JsonObject obj = this.getCurrentConfigRoot(
            GET, uriInfo, timers, engineApi);

        return obj;
      });

      this.processingRawData(timers);

      // get the array and construct the response
      JsonArray jsonArray = configRoot.getJsonArray("CFG_ATTR");

      JsonObject jsonAttrType = null;

      for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
        String attrCode = jsonObject.getString("ATTR_CODE");
        if (attrCode == null) continue;
        attrCode = attrCode.trim().toUpperCase();
        if (attrCode.equals(attributeCode)) {
          jsonAttrType = jsonObject;
          break;
        }
      }

      if (jsonAttrType == null) {
        throw this.newNotFoundException(
            GET, uriInfo, timers,
            "Attribute code not recognized: " + attributeCode);
      }

      SzAttributeType attrType = this.parseAttributeType(jsonAttrType);

      SzAttributeTypeResponse response = this.newAttributeTypeResponse(
          GET, 200, uriInfo, timers, attrType);

      // if including raw data then add it
      if (withRaw) {
        String rawData = JsonUtilities.toJsonText(jsonAttrType);

        response.setRawData(rawData);
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
      throw this.newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Parses the JSON described by the specified {@link JsonObject} as an
   * attribute type in the raw format and produces an instance of {@link
   * SzAttributeType}.
   *
   * @param jsonObject The {@link JsonObject} describing the attribute type in
   *                   the raw format.
   *
   * @return The newly created and configured {@link SzAttributeType}.
   */
  protected SzAttributeType parseAttributeType(JsonObject jsonObject) {
    return SzAttributeType.parseAttributeType(null, jsonObject);
  }

  /**
   * Creates a new {@link SzAttributeTypeResponse} with the specified
   * parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param attributeType The {@link SzAttributeType} instance.
   *
   * @return The newly created {@link SzAttributeTypeResponse}.
   */
  protected SzAttributeTypeResponse newAttributeTypeResponse(
      SzHttpMethod    httpMethod,
      int             httpStatusCode,
      UriInfo         uriInfo,
      Timers          timers,
      SzAttributeType attributeType)
  {
    return SzAttributeTypeResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        this.newAttributeTypeResponseData(attributeType));
  }

  /**
   * Creates a new {@link SzAttributeTypeResponseData} with the specified
   * parameters.
   *
   * @param attributeType The {@link SzAttributeType} instance.
   *
   * @return The newly created {@link SzAttributeTypeResponseData}.
   */
  protected SzAttributeTypeResponseData newAttributeTypeResponseData(
      SzAttributeType attributeType)
  {
    return SzAttributeTypeResponseData.FACTORY.create(attributeType);
  }

  /**
   * Provides the implementation of <tt>GET /configs/active</tt>.
   *
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzConfigResponse} describing the response.
   */
  @GET
  @Path("configs/active")
  public SzConfigResponse getActiveConfig(@Context UriInfo uriInfo)
  {
    Timers timers = this.newTimers();
    SzApiProvider provider = this.getApiProvider();

    try {
      this.enteringQueue(timers);
      JsonObject configObject = provider.executeInThread(() -> {
        this.exitingQueue(timers);
        // get the engine API and the config API
        G2Engine engineApi = provider.getEngineApi();

        // export the config
        String config = this.exportConfig(GET, uriInfo, timers, engineApi);

        // parse the raw data
        this.processingRawData(timers);
        JsonObject configObj = JsonUtilities.parseJsonObject(config);
        this.processedRawData(timers);

        return configObj;
      });

      this.processingRawData(timers);
      String rawData = JsonUtilities.toJsonText(configObject);
      SzConfigResponse response = this.newConfigResponse(
          GET, 200, uriInfo, timers, rawData);
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
      throw newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }

  /**
   * Creates a new {@link SzConfigResponse} with the specified parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param httpStatusCode The HTTP status code for the response.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param configRawData The JSON text for the config.
   *
   * @return The newly created {@link SzDataSourceResponse}.
   */
  protected SzConfigResponse newConfigResponse(SzHttpMethod httpMethod,
                                               int          httpStatusCode,
                                               UriInfo      uriInfo,
                                               Timers       timers,
                                               String       configRawData)
  {
    return SzConfigResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo),
        configRawData);
  }

  /**
   * Provides the implementation of <tt>GET /configs/template</tt>.
   *
   * @param uriInfo The {@link UriInfo} for the request.
   *
   * @return The {@link SzConfigResponse} describing the response.
   */
  @GET
  @Path("configs/template")
  public SzConfigResponse getTemplateConfig(@Context UriInfo uriInfo)
  {
    Timers timers = this.newTimers();
    SzApiProvider provider = this.getApiProvider();

    try {
      this.enteringQueue(timers);
      JsonObject configObject = provider.executeInThread(() -> {
        this.exitingQueue(timers);
        // get the engine API and the config API
        G2Config configApi = provider.getConfigApi();

        Result<Long> configIdResult = new Result<>();

        // create the default config
        StringBuffer sb = new StringBuffer();
        this.callingNativeAPI(timers, "config", "create");
        int returnCode = configApi.create(configIdResult);
        if (returnCode != 0) {
          throw this.newInternalServerErrorException(
              GET, uriInfo, timers, configApi);
        }
        this.calledNativeAPI(timers, "config", "create");

        long configId = configIdResult.getValue();

        try {
          this.callingNativeAPI(timers, "config", "save");
          returnCode = configApi.save(configId, sb);
          if (returnCode != 0) {
            throw this.newInternalServerErrorException(
                GET, uriInfo, timers, configApi);
          }
          this.calledNativeAPI(timers, "config", "save");

        } finally {
          this.callingNativeAPI(timers, "config", "close");
          configApi.close(configId);
          this.calledNativeAPI(timers, "config", "close");
        }

        String config = sb.toString();

        // parse the raw data
        this.processingRawData(timers);
        JsonObject configObj = JsonUtilities.parseJsonObject(config);
        this.processedRawData(timers);

        return configObj;
      });

      this.processingRawData(timers);
      String rawData = JsonUtilities.toJsonText(configObject);
      SzConfigResponse response = this.newConfigResponse(
          GET, 200, uriInfo, timers, rawData);
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
      throw this.newInternalServerErrorException(GET, uriInfo, timers, e);
    }
  }


  /**
   * Exports the config using the specified {@link G2Engine} instance.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param engineApi The {@link G2Engine} instance to use.
   */
  protected String exportConfig(SzHttpMethod  httpMethod,
                                UriInfo       uriInfo,
                                Timers        timers,
                                G2Engine      engineApi)
  {
    StringBuffer sb = new StringBuffer();
    this.callingNativeAPI(timers, "engine", "exportConfig");
    int result = engineApi.exportConfig(sb);
    this.calledNativeAPI(timers, "engine", "exportConfig");
    if (result != 0) {
      throw this.newInternalServerErrorException(
          httpMethod, uriInfo, timers, engineApi);
    }
    return sb.toString();
  }

  /**
   * From an exported config, this pulls the <tt>"G2_CONFIG"</tt>
   * {@link JsonObject} from it.
   *
   * @param httpMethod The {@link SzHttpMethod} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} for the operation.
   * @param engineApi The {@link G2Engine} instance to use.
   */
  protected JsonObject getCurrentConfigRoot(SzHttpMethod  httpMethod,
                                            UriInfo       uriInfo,
                                            Timers        timers,
                                            G2Engine      engineApi)
  {
    // export the config
    String config = this.exportConfig(httpMethod, uriInfo, timers, engineApi);

    // parse the raw data
    this.processingRawData(timers);
    JsonObject configObj = JsonUtilities.parseJsonObject(config);
    this.processedRawData(timers);
    return configObj.getJsonObject("G2_CONFIG");
  }
}
