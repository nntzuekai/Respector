package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Engine;
import com.senzing.io.IOUtilities;
import com.senzing.io.RecordReader;
import com.senzing.io.TemporaryDataCache;
import com.senzing.util.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.json.*;
import javax.websocket.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.io.*;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

import static com.senzing.api.model.SzBulkDataStatus.ABORTED;
import static com.senzing.api.model.SzBulkDataStatus.COMPLETED;
import static com.senzing.api.model.SzHttpMethod.POST;
import static com.senzing.io.IOUtilities.UTF_8;
import static com.senzing.reflect.ReflectionUtilities.synchronizedProxy;
import static com.senzing.text.TextUtilities.randomPrintableText;
import static com.senzing.util.AsyncWorkerPool.AsyncResult;
import static com.senzing.util.LoggingUtilities.*;
import static javax.ws.rs.core.MediaType.*;
import static com.senzing.api.services.ServicesUtil.*;

/**
 * Extends {@link ServicesSupport} to add functions specific to bulk-data
 * operations.
 */
public interface BulkDataSupport extends ServicesSupport {
  /**
   * The size of the piped input stream buffer size (10MB).
   */
  int PIPE_SIZE = 1024 * 1024 * 10;

  /**
   * The {@link MediaType} with text/plain and charset=utf8
   */
  MediaType TEXT_PLAIN_UTF8_TYPE = new MediaType(
      TEXT_PLAIN_TYPE.getType(), TEXT_PLAIN_TYPE.getSubtype(), UTF_8);

  /**
   * The <tt>"text/csv"</tt> media type string.
   */
  String TEXT_CSV = "text/csv";

  /**
   * The <tt>"application/x-jsonlines"</tt> media type string.
   */
  String APPLICATION_JSONLINES = "application/x-jsonlines";

  /**
   * The <tt>"text/event-stream"</tt> media type string.
   */
  String TEXT_EVENT_STREAM = "text/event-stream";

  /**
   * The file date pattern.
   */
  String FILE_DATE_PATTERN = "yyyyMMdd_HHmmssX";

  /**
   * The default progress period as the number of milliseconds between sending
   * progress responses to the client for SSE and Web Sockets.
   */
  Long DEFAULT_PROGRESS_PERIOD = 3000L;

  /**
   * The time zone used for the time component of the build number.
   */
  ZoneId UTC_ZONE = ZoneId.of("UTC");

  /**
   * The formatter for the file date.
   */
  DateTimeFormatter FILE_DATE_FORMATTER
      = DateTimeFormatter.ofPattern(FILE_DATE_PATTERN);

  /**
   * The reconnect delay to use for events when providing SSE events.
   */
  long RECONNECT_DELAY = 5000L;

  /**
   * SSE event type string for progress events.
   */
  String PROGRESS_EVENT = "progress";

  /**
   * SSE event type string for failure events.
   */
  String FAILED_EVENT = "failed";

  /**
   * SSE event type string for completion events.
   */
  String COMPLETED_EVENT = "completed";

  /**
   * Validates the progress period parameter for SSE or Web Socket requests.
   *
   * @param progressPeriod The specified progress period.
   * @param timers The {@link Timers} for the request.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param sseEventSink The {@link SseEventSink} or <tt>null</tt> if not an
   *                     SSE request.
   * @param sse The {@link Sse} or <tt>null</tt> if not an SSE request.
   * @param webSocketSession The Web Socket {@link Session} for the request.
   *
   * @throws BadRequestException If the progress period is negative, but we are
   *                             handling an SSE request.
   */
  default void validateProgressPeriod(Long          progressPeriod,
                                      Timers        timers,
                                      UriInfo       uriInfo,
                                      SseEventSink  sseEventSink,
                                      Sse           sse,
                                      Session       webSocketSession)
    throws BadRequestException
  {
    // check if the progress period parameter is being ignored
    if (sseEventSink == null && sse == null && webSocketSession == null) {
      return;
    }

    // check the parameters
    if (progressPeriod != null && progressPeriod < 0L) {
      throw this.newBadRequestException(
          POST, uriInfo, timers,
          "The progressPeriod parameter cannot be negative: " + progressPeriod);
    }
  }

  /**
   * Creates a new instance of {@link SzBulkDataAnalysis} and returns it.
   *
   * @return The new instance of {@link SzBulkDataAnalysis}.
   */
  default SzBulkDataAnalysis newBulkDataAnalysis() {
    return SzBulkDataAnalysis.FACTORY.create();
  }

  /**
   * Analyzes the bulk data and returns information about it.
   */
  default SzBulkDataAnalysisResponse analyzeBulkRecords(
      SzApiProvider               provider,
      Timers                      timers,
      MediaType                   mediaType,
      InputStream                 dataInputStream,
      UriInfo                     uriInfo,
      Long                        progressPeriod,
      SseEventSink                sseEventSink,
      Sse                         sse,
      Session                     webSocketSession)
  {
    // convert progress period to nanoseconds
    Long progressNanos = (progressPeriod == null)
        ? null : progressPeriod * 1000000L;

    OutboundSseEvent.Builder eventBuilder
        = (sseEventSink != null && sse != null) ? sse.newEventBuilder() : null;

    ProgressState progressState = new ProgressState();

    SzBulkDataAnalysis dataAnalysis = synchronizedProxy(
        SzBulkDataAnalysis.class, this.newBulkDataAnalysis(), progressState);

    // check the progress period
    this.validateProgressPeriod(progressPeriod,
                                timers,
                                uriInfo,
                                sseEventSink,
                                sse,
                                webSocketSession);

    try {
      BulkDataSet         bulkDataSet = new BulkDataSet(mediaType, dataInputStream);
      TemporaryDataCache  dataCache   = bulkDataSet.getDataCache();

      // if charset is unknown then try to detect
      String charset = bulkDataSet.getCharacterEncoding();
      dataAnalysis.setCharacterEncoding(charset);

      debugLog("Analyze bulk data character encoding: "
                + charset);

      long start = System.nanoTime();
      // check if we need to auto-detect the media type
      try (InputStream        is  = dataCache.getInputStream(true);
           InputStreamReader  isr = new InputStreamReader(is, charset);
           BufferedReader     br  = new BufferedReader(isr))
      {
        // if format is null then RecordReader will auto-detect
        RecordReader recordReader
            = new RecordReader(bulkDataSet.getFormat(), br);
        bulkDataSet.setFormat(recordReader.getFormat());
        debugLog("Analyze bulk data format: "+ bulkDataSet.getFormat());

        if (bulkDataSet.getFormat() != null) {
          dataAnalysis.setMediaType(bulkDataSet.getFormat().getMediaType());
        } else {
          dataAnalysis.setMediaType(null);
        }

        ProgressUpdater<SzBulkDataAnalysisResponse> progressUpdater = null;
        try {
          for (JsonObject record = recordReader.readRecord();
               (record != null);
               record = recordReader.readRecord()) {
            String dataSrc = JsonUtilities.getString(record, "DATA_SOURCE");
            String recordId = JsonUtilities.getString(record, "RECORD_ID");
            dataAnalysis.trackRecord(dataSrc, recordId);

            // check if the progress period has expired
            if ((progressNanos != null) && (progressUpdater == null)
                && (eventBuilder != null || webSocketSession != null))
            {
              progressState.setStartTime(System.nanoTime());
              Supplier<SzBulkDataAnalysisResponse> supplier = () -> {
                return this.newBulkDataAnalysisResponse(
                    POST, 200, uriInfo, timers, dataAnalysis);
              };
              progressUpdater = new ProgressUpdater<>(progressNanos,
                                                      progressState,
                                                      progressState, // monitor
                                                      supplier,
                                                      sseEventSink,
                                                      eventBuilder,
                                                      webSocketSession);
              progressUpdater.start();
            }
          }
        } finally {
          // make sure to clean up the progress updater
          if (progressUpdater != null) {
            // calling this should mark it complete and trigger wake-up
            progressUpdater.complete();
            try {
              // wait for the thread to complete before proceeding
              progressUpdater.join();
            } catch (InterruptedException ignore) {
              // ignore the exception
            }
          }
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
      dataAnalysis.setStatus(ABORTED);

      SzBulkDataAnalysisResponse response = this.newBulkDataAnalysisResponse(
          POST,200, uriInfo, timers, dataAnalysis);

      this.abortOperation(e,
                          response,
                          uriInfo,
                          timers,
                          progressState.nextEventId(),
                          eventBuilder,
                          sseEventSink,
                          webSocketSession);

      return response;
    }

    dataAnalysis.setStatus(COMPLETED);

    SzBulkDataAnalysisResponse response = this.newBulkDataAnalysisResponse(
        POST, 200, uriInfo, timers, dataAnalysis);

    return this.completeOperation(eventBuilder,
                                  sseEventSink,
                                  progressState.nextEventId(),
                                  webSocketSession,
                                  response);
  }

  /**
   * Creates a new instance of {@link SzBulkDataAnalysisResponse} with the
   * following parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the response.
   * @param httpStatusCode The status code for the response.
   * @param uriInfo The {@link UriInfo} for the operation.
   * @param timers The {@link Timers} tracking the timing for the operation.
   * @param dataAnalysis The {@link SzBulkDataAnalysis} describing the analysis.
   */
  default SzBulkDataAnalysisResponse newBulkDataAnalysisResponse(
      SzHttpMethod        httpMethod,
      int                 httpStatusCode,
      UriInfo             uriInfo,
      Timers              timers,
      SzBulkDataAnalysis  dataAnalysis)
  {
    return SzBulkDataAnalysisResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo), dataAnalysis);
  }

  /**
   * Creates a new instance of {@link SzBulkLoadResult} and returns it.
   *
   * @return The new instance of {@link SzBulkLoadResult}.
   */
  default SzBulkLoadResult newBulkLoadResult() {
    return SzBulkLoadResult.FACTORY.create();
  }

  /**
   * Loads the records found in the bulk data.
   */
  default SzBulkLoadResponse loadBulkRecords(
      SzApiProvider               provider,
      Timers                      timers,
      String                      dataSource,
      String                      mapDataSources,
      List<String>                mapDataSourceList,
      String                      explicitLoadId,
      int                         maxFailures,
      MediaType                   mediaType,
      InputStream                 dataInputStream,
      FormDataContentDisposition  fileMetaData,
      UriInfo                     uriInfo,
      Long                        progressPeriod,
      SseEventSink                sseEventSink,
      Sse                         sse,
      Session                     webSocketSession)
  {
    // convert the progress period to nanoseconds
    Long progressNanos = (progressPeriod == null)
        ? null : progressPeriod * 1000000L;

    OutboundSseEvent.Builder eventBuilder
        = (sseEventSink != null && sse != null) ? sse.newEventBuilder() : null;

    ProgressState progressState = new ProgressState();

    SzBulkLoadResult bulkLoadResult = synchronizedProxy(
        SzBulkLoadResult.class, this.newBulkLoadResult(), progressState);

    // populate the data source map
    Map<String, String> dataSourceMap = new HashMap<>();
    this.prepareBulkDataMappings(provider,
                                 uriInfo,
                                 timers,
                                 dataSource,
                                 mapDataSources,
                                 mapDataSourceList,
                                 dataSourceMap);

    try {
      BulkDataSet bulkDataSet = new BulkDataSet(mediaType, dataInputStream);

      TemporaryDataCache dataCache = bulkDataSet.getDataCache();

      String charset = bulkDataSet.getCharacterEncoding();

      debugLog("Load bulk data character encoding: " + charset);

      String loadId = (explicitLoadId == null)
          ? formatLoadId(dataCache, fileMetaData) : explicitLoadId;

      int concurrency = provider.getConcurrency();
      AsyncWorkerPool<AddRecordResult> asyncPool
          = new AsyncWorkerPool<>(loadId, concurrency);

      List<Timers> timerPool = new ArrayList<>(concurrency);
      for (int index = 0; index < concurrency; index++) {
        timerPool.add(new Timers());
      }

      // check if we need to auto-detect the media type
      try (InputStream        is  = dataCache.getInputStream(true);
           InputStreamReader  isr = new InputStreamReader(is, charset);
           BufferedReader     br  = new BufferedReader(isr))
      {
        // if format is null then RecordReader will auto-detect
        RecordReader recordReader = new RecordReader(bulkDataSet.getFormat(),
                                                     br,
                                                     dataSourceMap,
                                                     loadId);

        bulkDataSet.setFormat(recordReader.getFormat());
        debugLog("Load bulk data format: " + bulkDataSet.getFormat());

        bulkLoadResult.setCharacterEncoding(charset);
        bulkLoadResult.setMediaType(bulkDataSet.getFormat().getMediaType());

        boolean           concurrent       = false;
        boolean           done             = false;
        List<JsonObject>  first1000Records = new LinkedList<>();

        boolean aborted = false;
        ProgressUpdater<SzBulkLoadResponse> progressUpdater = null;
        try {
          // loop through the records and handle each record
          while (!done) {
            JsonObject record = null;
            if (concurrent && first1000Records.size() > 0) {
              // get the first record from the buffer of up to 1000 records
              record = first1000Records.remove(0);
            } else {
              record = recordReader.readRecord();
            }

            // check if the record is null
            if (record == null) {
              done = true;
              continue;
            }

            // peel off the first 1000 records to see if we have less than 1000
            if (!concurrent && first1000Records.size() <= 1000) {
              // add the record to the first-1000 cache
              first1000Records.add(record);

              // check if we have more than 1000 records
              if (first1000Records.size() > 1000) concurrent = true;

              // continue for now
              continue;
            }

            // check if we have a data source
            String resolvedDS = JsonUtilities.getString(record, "DATA_SOURCE");
            if (resolvedDS == null || resolvedDS.trim().length() == 0)
            {
              debugLog("INCOMPLETE RECORD NOT LOADED: "
                        + JsonUtilities.toJsonText(record));

              bulkLoadResult.trackIncompleteRecord(resolvedDS);

            } else {
              Timers subTimers = timerPool.remove(0);
              AsyncResult<AddRecordResult> asyncResult = null;
              try {
                asyncResult = this.asyncProcessRecord(asyncPool,
                                                      provider,
                                                      subTimers,
                                                      record,
                                                      loadId);

              } finally {
                this.trackLoadResult(asyncResult, bulkLoadResult);
                timerPool.add(subTimers);
              }
            }

            // count the number of failures
            int failedCount = bulkLoadResult.getFailedRecordCount()
                + bulkLoadResult.getIncompleteRecordCount();

            // break if aborted
            if (maxFailures > 0 && failedCount >= maxFailures) {
              aborted = true;
              break;
            }

            // check if the timing has gone beyond the specified progress period
            if ((progressNanos != null) && (progressUpdater == null)
                && (eventBuilder != null || webSocketSession != null))
            {
              // create the update response if there is a client expecting it
              progressState.setStartTime(System.nanoTime());
              Supplier<SzBulkLoadResponse> supplier = () -> {
                return this.newBulkLoadResponse(
                    POST, 200, uriInfo, timers, bulkLoadResult);
              };
              progressUpdater = new ProgressUpdater<>(progressNanos,
                                                      progressState,
                                                      progressState, // monitor
                                                      supplier,
                                                      sseEventSink,
                                                      eventBuilder,
                                                      webSocketSession);
              progressUpdater.start();
            }
          }

          // check if we have less than 1000 records
          if (first1000Records.size() > 0 && !aborted) {
            this.processRecords(provider,
                                timers,
                                first1000Records,
                                loadId,
                                bulkLoadResult,
                                maxFailures);
          }

          // close out any in-flight loads from the asynchronous pool
          List<AsyncResult<AddRecordResult>> results = asyncPool.close();
          for (AsyncResult<AddRecordResult> asyncResult : results) {
            this.trackLoadResult(asyncResult, bulkLoadResult);
          }

          // merge the timers
          for (Timers subTimer : timerPool) {
            timers.mergeWith(subTimer);
          }

        } finally {
          // make sure to clean up the progress updater
          if (progressUpdater != null) {
            // calling this should mark it complete and trigger wake-up
            progressUpdater.complete();
            try {
              // wait for the thread to complete before proceeding
              progressUpdater.join();
            } catch (InterruptedException ignore) {
              // ignore the exception
            }
          }

          // check if aborted
          if (aborted) {
            bulkLoadResult.setStatus(ABORTED);
          }
        }

        // mark completed if we get here without an exception
        if (bulkLoadResult.getStatus() != ABORTED) {
          bulkLoadResult.setStatus(COMPLETED);
        }

      } finally {
        dataCache.delete();
      }

    } catch (IOException e) {
      bulkLoadResult.setStatus(ABORTED);
      SzBulkLoadResponse response = this.newBulkLoadResponse(POST,
                                                             200,
                                                             uriInfo,
                                                             timers,
                                                             bulkLoadResult);
      this.abortOperation(e,
                          response,
                          uriInfo,
                          timers,
                          progressState.nextEventId(),
                          eventBuilder,
                          sseEventSink,
                          webSocketSession);
    }

    SzBulkLoadResponse response = this.newBulkLoadResponse(POST,
                                                           200,
                                                           uriInfo,
                                                           timers,
                                                           bulkLoadResult);

    return this.completeOperation(eventBuilder,
                                  sseEventSink,
                                  progressState.nextEventId(),
                                  webSocketSession,
                                  response);
  }

  /**
   * Creates a new instance of {@link SzBulkDataAnalysisResponse} with the
   * following parameters.
   *
   * @param httpMethod The {@link SzHttpMethod} for the response.
   * @param httpStatusCode The status code for the response.
   * @param uriInfo The {@link UriInfo} for the operation.
   * @param timers The {@link Timers} tracking the timing for the operation.
   * @param bulkLoadResult The {@link SzBulkLoadResult} describing the analysis.
   */
  default SzBulkLoadResponse newBulkLoadResponse(
      SzHttpMethod        httpMethod,
      int                 httpStatusCode,
      UriInfo             uriInfo,
      Timers              timers,
      SzBulkLoadResult    bulkLoadResult)
  {
    return SzBulkLoadResponse.FACTORY.create(
        this.newMeta(httpMethod, httpStatusCode, timers),
        this.newLinks(uriInfo), bulkLoadResult);
  }

  /**
   * Asynchronously process a record using the specified {@link SzApiProvider}
   * and {@link AsyncWorkerPool}.  The returned {@link AsyncResult} is from
   * a previously executed task on the same thread or <tt>null</tt> if the
   * worker thread employed has not previously executed a task.
   */
  default AsyncResult<AddRecordResult> asyncProcessRecord(
      AsyncWorkerPool<AddRecordResult> asyncPool,
      SzApiProvider                 provider,
      Timers                        timers,
      JsonObject                    record,
      String                        loadId)
  {
    String dataSource = JsonUtilities.getString(record, "DATA_SOURCE");
    String recordId   = JsonUtilities.getString(record, "RECORD_ID");
    String recordJSON = JsonUtilities.toJsonText(record);

    G2Engine engineApi = provider.getEngineApi();
    return asyncPool.execute(() -> {
      try {
        // otherwise try to load the record
        this.enteringQueue(timers);
        return provider.executeInThread(() -> {
          this.exitingQueue(timers);
          debugLog("Async loading record: " + recordJSON);

          int returnCode = this.addRecord(engineApi,
                                          provider,
                                          dataSource,
                                          recordId,
                                          recordJSON,
                                          loadId,
                                          timers);

          return this.newAddRecordResult(
              dataSource, timers, returnCode, engineApi);
        });

      } catch (Exception e) {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("dataSource", dataSource);
        String details = JsonUtilities.toJsonText(job);
        throw new Exception(details, e);
      }
    });
  }

  /**
   * Asynchronously process a record using the specified {@link SzApiProvider}
   * and {@link AsyncWorkerPool}.  The returned {@link AsyncResult} is from
   * a previously executed task on the same thread or <tt>null</tt> if the
   * worker thread employed has not previously executed a task.
   */
  default void processRecords(
      SzApiProvider     provider,
      Timers            timers,
      List<JsonObject>  records,
      String            loadId,
      SzBulkLoadResult  bulkLoadResult,
      int               maxFailures)
  {
    G2Engine engineApi = provider.getEngineApi();
    // otherwise try to load the record
    this.enteringQueue(timers);
    provider.executeInThread(() -> {
      this.exitingQueue(timers);
      for (JsonObject record : records) {

        String dataSource = JsonUtilities.getString(record, "DATA_SOURCE");
        String recordId   = JsonUtilities.getString(record, "RECORD_ID");
        String recordJSON = JsonUtilities.toJsonText(record);

        // check if we have a data source
        if (dataSource == null || dataSource.trim().length() == 0) {
          debugLog("Incomplete record not loaded: " + recordJSON);

          bulkLoadResult.trackIncompleteRecord(dataSource);

        } else {
          debugLog("Sync loading record: " + recordJSON);

          int returnCode = this.addRecord(engineApi,
                                          provider,
                                          dataSource,
                                          recordId,
                                          recordJSON,
                                          loadId,
                                          timers);

          AddRecordResult addRecordResult = this.newAddRecordResult(
              dataSource, timers, returnCode, engineApi);

          this.trackLoadResult(addRecordResult, bulkLoadResult);
        }

        // count the number of failures
        int failedCount = bulkLoadResult.getFailedRecordCount()
            + bulkLoadResult.getIncompleteRecordCount();

        if (maxFailures > 0 && failedCount >= maxFailures) {
          bulkLoadResult.setStatus(ABORTED);
          break;
        }
      }

      // return null
      return null;
    });
  }

  /**
   * Constructs with the specified parameters.
   *
   * @param dataSource The data source for the record.
   * @param timers The {@link Timers} for the operation.
   * @param returnCode The return code from native add-record function.
   * @param engine The {@link G2Engine} instance that was used.
   *
   * @return The newly created instance of {@link AddRecordResult}.
   */
  default AddRecordResult newAddRecordResult(String    dataSource,
                                             Timers    timers,
                                             int       returnCode,
                                             G2Engine  engine)
  {
    return new AddRecordResult(dataSource,
                               timers,
                               returnCode,
                               engine);
  }

  /**
   * Adds the record either with or without a record ID and tracks the timing.
   */
  default int addRecord(G2Engine      engineApi,
                        SzApiProvider provider,
                        String        dataSource,
                        String        recordId,
                        String        recordJSON,
                        String        loadId,
                        Timers        timers)
  {
    int returnCode;
    boolean asyncInfo = provider.hasInfoSink();
    if (asyncInfo) {
      StringBuffer sb = new StringBuffer();
      this.callingNativeAPI(timers, "engine", "addRecordWithInfo");
      returnCode = engineApi.addRecordWithInfo(
          dataSource,
          (recordId == null) ? "" : recordId, // empty record ID
          recordJSON,
          loadId,
          0,
          sb);
      this.calledNativeAPI(timers, "engine", "addRecordWithInfo");

      // check the return code before trying to send out the info
      if (returnCode == 0) {
        String rawInfo = sb.toString();

        // check if we have raw info to send
        if (rawInfo != null && rawInfo.trim().length() > 0) {
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
      }

    } else if (recordId != null) {
      this.callingNativeAPI(timers, "engine", "addRecord");
      returnCode = engineApi.addRecord(dataSource,
                                       recordId,
                                       recordJSON,
                                       loadId);
      this.calledNativeAPI(timers, "engine", "addRecord");

    } else {
      this.callingNativeAPI(timers, "engine",
                       "addRecordWithReturnedRecordID");
      StringBuffer sb = new StringBuffer();
      returnCode = engineApi.addRecordWithReturnedRecordID(
          dataSource, sb, recordJSON, loadId);
      this.calledNativeAPI(timers, "engine",
                      "addRecordWithReturnedRecordID");
    }
    return returnCode;
  }

  /**
   * Tracks the asynchronous record load result in the {@link SzBulkLoadResult}.
   */
  default void trackLoadResult(AsyncResult<AddRecordResult> asyncResult,
                               SzBulkLoadResult             bulkLoadResult)
  {
    // check the result
    if (asyncResult != null) {
      AddRecordResult addRecordResult = null;
      try {
        // get the value from the async result (may throw an exception)
        addRecordResult = asyncResult.getValue();

      } catch (Exception e) {
        // an exception was thrown in trying to get the result
        String      jsonText  = e.getMessage();
        JsonObject  jsonObj   = JsonUtilities.parseJsonObject(jsonText);

        String failDataSource = JsonUtilities.getString(jsonObj, "dataSource");
        Throwable cause = e.getCause();
        bulkLoadResult.trackFailedRecord(
            failDataSource, this.newError(cause.getMessage()));
      }

      // track the result
      if (addRecordResult != null) {
        this.trackLoadResult(addRecordResult, bulkLoadResult);
      }
    }
  }

  /**
   * Tracks the asynchronous record load result in the {@link SzBulkLoadResult}.
   */
  default void trackLoadResult(AddRecordResult  addRecordResult,
                               SzBulkLoadResult bulkLoadResult)
  {
    // check if the add failed or succeeded
    if (addRecordResult.isFailed()) {
      // adding the record failed, record the failure
      bulkLoadResult.trackFailedRecord(
          addRecordResult.getDataSource(),
          addRecordResult.getErrorCode(),
          addRecordResult.getErrorMessage());
    } else {
      // adding the record succeeded, record the loaded record
      bulkLoadResult.trackLoadedRecord(addRecordResult.getDataSource());
    }
  }

  /**
   * Formats load ID using the specified data cache
   */
  default String formatLoadId(TemporaryDataCache          dataCache,
                              FormDataContentDisposition  fileMetaData)
  {
    String fileKey = (fileMetaData != null) ? fileMetaData.getName() : null;
    if (fileKey == null) {
      try (InputStream is = dataCache.getInputStream();)
      {
        byte[]        bytes     = new byte[1024];
        MessageDigest md5       = MessageDigest.getInstance("MD5");
        int           readCount = is.read(bytes);
        md5.update(bytes, 0, readCount);
        byte[] hash = md5.digest();
        fileKey = Base64.getEncoder().encodeToString(hash);

      } catch (Exception e) {
        fileKey = randomPrintableText(30);
      }
    }
    Date fileDate = (fileMetaData != null)
        ? fileMetaData.getModificationDate()
        : null;

    if (fileDate == null && fileMetaData != null) {
      fileDate = fileMetaData.getCreationDate();
    }

    ZonedDateTime fileDateTime = (fileDate == null)
        ? null : ZonedDateTime.ofInstant(fileDate.toInstant(), UTC_ZONE);
    ZonedDateTime now = ZonedDateTime.now(UTC_ZONE);

    String fileDateText = (fileDate == null)
        ? "?" : FILE_DATE_FORMATTER.format(fileDate.toInstant());
    String nowText = (now == null) ? "?" : FILE_DATE_FORMATTER.format(now);

    return fileKey + "_" + fileDateText + "_" + nowText;
  }

  /**
   * Encapsulates a bulk data set.
   */
  class BulkDataSet {
    /**
     * The character encoding of the bulk data stream (possibly auto-detected).
     */
    protected String characterEncoding;

    /**
     * The media type for the bulk data stream (possibly auto-detected).
     */
    protected String mediaType = null;

    /**
     * The corresponding {@link RecordReader.Format} for the bulk data stream
     * (possibly auto-detected).
     */
    protected RecordReader.Format format;

    /**
     * The {@link TemporaryDataCache} for reading the data from the bulk data
     * stream.
     */
    protected TemporaryDataCache dataCache;

    /**
     * Constructs with the specified media type (if known) and the specified
     * {@link InputStream}.  If the media type is not known it will be
     * automatically detected.
     *
     * @param mediaType The media type for the data that will be read, or
     *                  <tt>null</tt> if not known and it should be
     *                  automatically detected.
     * @param inputStream The {@link InputStream} to read the data.
     * @throws IOException If an I/O failure occurs.
     */
    public BulkDataSet(MediaType mediaType, InputStream inputStream)
        throws IOException
    {
      this.characterEncoding = mediaType.getParameters().get("charset");
      String baseMediaType = mediaType.getType() + "/" + mediaType.getSubtype();
      if (baseMediaType != null) baseMediaType = baseMediaType.toLowerCase();
      switch (baseMediaType) {
        case "multipart/form-data":
          this.characterEncoding = null;
        default:
          this.format = RecordReader.Format.fromMediaType(baseMediaType);
      }

      if (this.format != null) {
        this.mediaType = this.format.getMediaType();
      }

      try {
        this.dataCache = new TemporaryDataCache(inputStream);

        // if charset is unknown then try to detect
        if (this.characterEncoding == null) {
          try (InputStream is = this.dataCache.getInputStream()) {
            this.characterEncoding = IOUtilities.detectCharacterEncoding(is);
          }
          if (this.characterEncoding == null) this.characterEncoding = "UTF-8";
        }

      } catch (IOException e) {
        if (!isLastLoggedException(e)) {
          e.printStackTrace();
        }
        setLastLoggedAndThrow(e);
      }
    }

    /**
     * Gets the character encoding for this instance.
     *
     * @return The character encoding for this instance.
     */
    public String getCharacterEncoding() {
      return this.characterEncoding;
    }

    /**
     * Gets the media type for this instance.
     *
     * @return The media type for this instance.
     */
    public String getMediaType() {
      return this.mediaType;
    }

    /**
     * Gets the format for this instance.
     *
     * @return The format for this instance.
     */
    public RecordReader.Format getFormat() {
      return this.format;
    }

    /**
     * Sets the format for this instance.
     *
     * @param format The format to set.
     */
    public void setFormat(RecordReader.Format format) {
      this.format = format;
    }

    /**
     * Gets the {@link TemporaryDataCache} for this instance to read the data.
     *
     * @return The {@link TemporaryDataCache} for this instance to read the
     *         data.
     */
    public TemporaryDataCache getDataCache() {
      return dataCache;
    }
  }

  /**
   * Describes the result from the engine to track the result of attempting to
   * add a record.  This is used to aggregate results from across threads.
   */
  class AddRecordResult {
    /**
     * The return code from the the native add-record function.
     */
    protected int returnCode = 0;

    /**
     * The data source for the record.
     */
    protected String dataSource = null;

    /**
     * The error code from the native add-record function if it failed, or
     * <tt>null</tt> if it succeeded.
     */
    protected String errorCode = null;

    /**
     * The error message from the native add-record function if it failed, or
     * <tt>null</tt> if it succeeded.
     */
    protected String errorMsg = null;

    /**
     * The {@link Timers} for the operation.
     */
    protected Timers timers = null;

    /**
     * Constructs with the specified parameters.
     *
     * @param dataSource The data source for the record.
     * @param timers The {@link Timers} for the operation.
     * @param returnCode The return code from native add-record function.
     * @param engine The {@link G2Engine} instance that was used.
     */
    public AddRecordResult(String    dataSource,
                           Timers    timers,
                           int       returnCode,
                           G2Engine  engine)
    {
      this.dataSource = dataSource;
      this.returnCode = returnCode;
      this.timers     = timers;
      if (this.returnCode != 0) {
        this.errorCode  = "" + engine.getLastExceptionCode();
        this.errorMsg   = engine.getLastException();
      }
    }

    /**
     * Gets the return code from add-record operation.
     *
     * @return The return code form the add-record operation.
     */
    public int getReturnCode() {
      return this.returnCode;
    }

    /**
     * Gets the data source for the record that was being added.
     *
     * @return The data source for the record that was being added.
     */
    public String getDataSource() {
      return this.dataSource;
    }

    /**
     * Gets the error code (if any) from the {@link G2Engine} with which this
     * instance was constructed.
     *
     * @return The error code (if any) from the {@link G2Engine} with which this
     *         instance was constructed.
     */
    public String getErrorCode() {
      return this.errorCode;
    }

    /**
     * Gets the error message (if any) from the {@link G2Engine} with which this
     * instance was constructed.
     *
     * @return The error message (if any) from the {@link G2Engine} with which
     *         this instance was constructed.
     */
    public String getErrorMessage() {
      return this.errorMsg;
    }

    /**
     * Gets the {@link Timers} instance for the add-record operation.
     *
     * @return The {@link Timers} instance for the add-record operation.
     */
    public Timers getTimers() {
      return this.timers;
    }

    /**
     * Checks if this instance describes a failure when performing the
     * add-record operation.  If this returns <tt>true</tt> then details can
     * be obtained from {@link #getErrorCode()} and {@link #getErrorMessage()}.
     *
     * @return <tt>true</tt> if the add-record operation failed, and
     *         <tt>false</tt> if it succeeded.
     */
    public boolean isFailed() {
      return (this.returnCode != 0);
    }

    /**
     * Produces a diagnostic {@link String} describing this instance.
     *
     * @return A diagnostic {@link String} describing this instance.
     */
    public String toString() {
      return "{ returnCode=[ " + this.returnCode
              + " ], dataSource=[ " + this.dataSource
              + " ], errorCode=[ " + this.errorCode
              + " ], errorMsg=[ " + this.errorMsg
              + " ] }";
    }
  }

  /**
   * Completes the operation using the specified parameters to determine what
   * sort of request was made.
   *
   * @param eventBuilder The event builder if an SSE request.
   * @param sseEventSink The event sink if an SSE request.
   * @param eventId The event ID if an SSE request.
   * @param webSocketSession The web socket session if a web socket request.
   * @param response The response object to be sent.
   * @param <T> The type of the response object.
   *
   * @return The specified response object.
   */
  default <T extends SzBasicResponse> T completeOperation(
      OutboundSseEvent.Builder  eventBuilder,
      SseEventSink              sseEventSink,
      int                       eventId,
      Session                   webSocketSession,
      T                         response)
  {
    if (eventBuilder != null || webSocketSession != null) {
      // check if sending an SSE event
      if (eventBuilder != null) {
        OutboundSseEvent event
            = eventBuilder.name(COMPLETED_EVENT)
            .id(String.valueOf(eventId++))
            .mediaType(APPLICATION_JSON_TYPE)
            .data(response)
            .reconnectDelay(RECONNECT_DELAY)
            .build();
        sseEventSink.send(event);
        sseEventSink.close();
      }

      // check if sending a message on the web socket session
      if (webSocketSession != null) {
        try {
          webSocketSession.getBasicRemote().sendObject(response);
          webSocketSession.close();

        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // return null here if we sent the response
      return null;
    }

    // return the response
    return response;
  }

  /**
   * Aborts the operation using the specified parameters to determine what
   * sort of request was made.
   *
   * @param failure The failure that triggered the abort.
   * @param response The response object to send.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} associated with the operation.
   * @param eventId The event ID if an SSE request.
   * @param eventBuilder The event builder if an SSE request.
   * @param sseEventSink The event sink if an SSE request.
   * @param webSocketSession The web socket session if a web sockets request.
   * @param <T> The type of the response object.
   * @return The specified response object.
   * @throws WebApplicationException If a web application failure occurs.
   */
  default <T extends SzBasicResponse> T abortOperation(
      Exception                 failure,
      T                         response,
      UriInfo                   uriInfo,
      Timers                    timers,
      int                       eventId,
      OutboundSseEvent.Builder  eventBuilder,
      SseEventSink              sseEventSink,
      Session                   webSocketSession)
      throws WebApplicationException
  {
    if (!isLastLoggedException(failure)) {
      failure.printStackTrace();
    }
    setLastLoggedException(failure);

    // determine if we need to construct an error response
    SzErrorResponse errorResponse = null;
    if (eventBuilder != null || webSocketSession != null) {
      errorResponse = this.newErrorResponse(
          this.newMeta(POST, 500, timers),
          this.newLinks(uriInfo), failure);
    }

    // check if this is a standard HTTP request
    if (eventBuilder == null && webSocketSession == null) {
      throw this.newInternalServerErrorException(
          POST, uriInfo, timers, failure);
    }

    if (eventBuilder != null) {
      // handle SSE response
      OutboundSseEvent abortEvent
          = eventBuilder.name(PROGRESS_EVENT)
          .id(String.valueOf(eventId++))
          .mediaType(APPLICATION_JSON_TYPE)
          .data(response)
          .reconnectDelay(RECONNECT_DELAY)
          .build();
      sseEventSink.send(abortEvent);

      OutboundSseEvent failEvent
          = eventBuilder.name(FAILED_EVENT)
          .id(String.valueOf(eventId++))
          .mediaType(APPLICATION_JSON_TYPE)
          .data(errorResponse)
          .reconnectDelay(RECONNECT_DELAY)
          .build();
      sseEventSink.send(failEvent);
      sseEventSink.close();
    }

    // check if we have a web socket session
    if (webSocketSession != null) {
      try {
        webSocketSession.getBasicRemote().sendObject(response);
        webSocketSession.getBasicRemote().sendObject(errorResponse);

      } catch (RuntimeException e) {
        e.printStackTrace();
        throw e;

      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }

    // return null
    return null;
  }

  /**
   * Populates the specified {@link Map} with the data source mappings found
   * in the specified JSON text.
   *
   * @param dataSourceMap The {@link Map} to be populated.
   * @param mapDataSources The JSON text describing the data source mappings.
   * @param provider The {@link SzApiProvider} to use.
   * @param timers The {@link Timers} for the operation.
   * @param uriInfo The {@link UriInfo} for the operation.
   */
  default void processDataSources(Map<String, String> dataSourceMap,
                                  String              mapDataSources,
                                  SzApiProvider       provider,
                                  Timers              timers,
                                  UriInfo             uriInfo)
  {
    // check if the mapDataSources parameter is provided
    if (mapDataSources != null && mapDataSources.trim().length() > 0) {
      try {
        JsonObject jsonObject = JsonUtilities.parseJsonObject(mapDataSources);
        jsonObject.entrySet().forEach(entry -> {
          String key = entry.getKey();
          JsonValue value = entry.getValue();
          if (value.getValueType() != JsonValue.ValueType.STRING) {
            throw this.newBadRequestException(
                POST, uriInfo, timers,
                "At least one JSON property (\"" + key + "\") in the "
                    + "\"mapDataSources\" parameter does NOT have a "
                    + "String JSON value (" + JsonUtilities.toJsonText(value)
                    + "): " + mapDataSources);
          }
          String source = ((JsonString) value).getString().trim().toUpperCase();
          if (!provider.getDataSources(source).contains(source)) {
            throw newBadRequestException(
                POST, uriInfo, timers,
                "The data source mapping for \"" + key + "\" in the "
                    + "\"mapDataSources\" parameter has a value (\"" + source
                    + "\") that is not a configured data source: "
                    + mapDataSources);
          }
          dataSourceMap.put(key, source);
        });
      } catch (Exception e) {
        throw newBadRequestException(
            POST, uriInfo, timers,
            "The \"mapDataSources\" parameter is not a valid URL-encoded JSON "
                + "of String property names and String data source code values: "
                + mapDataSources);
      }
    }
  }

  /**
   * Populates the specified {@link Map} with the data source mappings found
   * in the specified {@link List} of encoded strings.
   *
   * @param dataSourceMap The {@link Map} to be populated.
   * @param mapDataSourceList The {@link List} of encoded strings describing the
   *                          data source mappings.
   * @param provider The {@link SzApiProvider} to use.
   * @param timers The {@link Timers} for the operation.
   * @param uriInfo The {@link UriInfo} for the operation.
   */
  default void processDataSources(Map<String, String> dataSourceMap,
                                  List<String>        mapDataSourceList,
                                  SzApiProvider       provider,
                                  Timers              timers,
                                  UriInfo             uriInfo)
  {
    // check if the mapDataSources parameter is provided
    if (mapDataSourceList != null && mapDataSourceList.size() > 0) {
      for (String mapping : mapDataSourceList) {
        char sep = mapping.charAt(0);
        int index = mapping.indexOf(sep, 1);
        if (index < 0 || index == mapping.length() - 1) {
          throw newBadRequestException(
              POST, uriInfo, timers,
              "The specified data source mapping is not a valid "
                  + "delimited string: " + mapping);
        }
        String source1 = mapping.substring(1, index).trim();
        String source2 = mapping.substring(index + 1).trim().toUpperCase();

        if (!provider.getDataSources(source2).contains(source2)) {
          throw newBadRequestException(
              POST, uriInfo, timers,
              "The data source mapping for \"" + source1 + "\" in the "
                  + "\"mapDataSource\" parameter has a value (\"" + source2
                  + "\") that is not a configured data source: "
                  + mapping);
        }
        dataSourceMap.put(source1, source2);
      }
    }
  }

  /**
   * Prepares for performing a bulk-loading operation by ensuring the server is
   * not in read-only mode and a long-running operation is authorized.
   *
   * @parma provider The {@link SzApiProvider} to use.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} tracking timing for the operation.
   *
   * @return The {@link AccessToken} for the prolonged operation.
   *
   * @throws ForbiddenException If no load queue is configured.
   * @throws ServiceUnavailableException If too many long-running operaitons are
   *                                     already running.
   */
  default AccessToken prepareBulkLoadOperation(SzApiProvider  provider,
                                               UriInfo        uriInfo,
                                               Timers         timers)
      throws ForbiddenException, ServiceUnavailableException
  {
    this.ensureLoadingIsAllowed(provider, POST, uriInfo, timers);

    return this.prepareProlongedOperation(provider, POST, uriInfo, timers);
  }

  /**
   * Prepares for performing a bulk-loading operation by ensuring the
   * required data sources are configured.
   *
   * @parma provider The {@link SzApiProvider} to use.
   * @param uriInfo The {@link UriInfo} for the request.
   * @param timers The {@link Timers} tracking timing for the operation.
   * @param dataSource The data source to assign to the loaded records unless
   *                   another data source mapping supercedes this default.
   * @param mapDataSources The JSON string mapping specific data sources to
   *                       alternate data source names.  A mapping from
   *                       empty-string is used for mapping records with no
   *                       data source specified.
   * @param mapDataSourceList The {@link List} of delimited strings that begin
   *                          the delimiter, followed by the "from" data source
   *                          then the delimiter then the target data source.
   * @param dataSourceMap The {@link Map} of configured data source codes to
   *                      configured data sources.
   *
   * @throws BadRequestException If any of the data sources are not recognized.
   */
  default void prepareBulkDataMappings(SzApiProvider        provider,
                                       UriInfo              uriInfo,
                                       Timers               timers,
                                       String               dataSource,
                                       String               mapDataSources,
                                       List<String>         mapDataSourceList,
                                       Map<String, String>  dataSourceMap)
  {
    // normalize and validate the data source
    if (dataSource != null) {
      dataSource = dataSource.trim().toUpperCase();

      if (!provider.getDataSources(dataSource).contains(dataSource)) {
        throw this.newBadRequestException(
            POST, uriInfo, timers,
            "The value for the specified \"dataSource\" parameter is not a "
                + "configured data source: " + dataSource);
      }
    }

    this.processDataSources(
        dataSourceMap, mapDataSources, provider, timers, uriInfo);
    this.processDataSources(
        dataSourceMap, mapDataSourceList, provider, timers, uriInfo);

    // put the default overrides in the map with the null key
    if (dataSource != null) dataSourceMap.put(null, dataSource);
  }

  /**
   * Handles checking if the processing should be aborted and handles reporting
   * progress if the timing indicates that a progress update is due.
   *
   */
  default boolean checkAbortLoadDoProgress(
      UriInfo                   uriInfo,
      Timers                    timers,
      SzBulkLoadResult          bulkLoadResult,
      int                       maxFailures,
      ProgressState             progressState,
      Long                      progressPeriod,
      SseEventSink              sseEventSink,
      OutboundSseEvent.Builder  sseEventBuilder,
      Session                   webSocketSession)
  {
    // convert the progress period to nanoseconds
    Long progressNanos = (progressPeriod == null)
        ? null : progressPeriod * 1000000L;

    // count the number of failures
    int failedCount = bulkLoadResult.getFailedRecordCount()
        + bulkLoadResult.getIncompleteRecordCount();

    if (maxFailures > 0 && failedCount >= maxFailures) {
      bulkLoadResult.setStatus(ABORTED);
      return true;
    }

    long now = System.nanoTime();
    long duration = now - progressState.getStartTime();

    // check if the timing has gone beyond the specified progress period
    if ((progressNanos != null) && (duration > progressNanos)) {
      // create the update response if there is a client expecting it
      SzBulkLoadResponse update = null;
      if (sseEventBuilder != null || webSocketSession != null) {
        progressState.setStartTime(now);
        update = this.newBulkLoadResponse(
            POST, 200, uriInfo, timers, bulkLoadResult);
      }

      // check if sending an SSE response
      if (sseEventBuilder != null) {
        OutboundSseEvent event =
            sseEventBuilder.name(PROGRESS_EVENT)
                .id(String.valueOf(progressState.nextEventId()))
                .mediaType(APPLICATION_JSON_TYPE)
                .data(update)
                .reconnectDelay(RECONNECT_DELAY)
                .build();
        sseEventSink.send(event);
      }

      // check if sending a web socket response
      if (webSocketSession != null) {
        try {
          // send the web socket message and handle exceptions
          webSocketSession.getBasicRemote().sendObject(update);

        } catch (RuntimeException e) {
          e.printStackTrace();
          throw e;

        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    }

    // return false since we are not aborting
    return false;
  }

  /**
   * The progress state to track when to produce progress updates for SSE and
   * web sockets.
   */
  class ProgressState {
    /**
     * The start time.
     */
    private long startTime = 0L;

    /**
     * The next event ID.
     */
    private int nextEventId = 0;

    /**
     * Default constructor.
     */
    public ProgressState() {
      this.startTime        = System.nanoTime();
      this.nextEventId      = 0;
    }

    /**
     * Gets the next event ID and increments the event ID for the next call.
     *
     * @return The next event ID.
     */
    public synchronized int nextEventId() {
      return this.nextEventId++;
    }

    /**
     * Gets the most recent start time.
     * @return The most recent start time.
     */
    public synchronized long getStartTime() {
      return this.startTime;
    }

    /**
     * Updates the start time to the specified time.
     * @param time The new start time.
     */
    public synchronized void setStartTime(long time) {
      this.startTime = time;
    }

  }

  /**
   * The progress updater thread to handle sending the progress updates.
   */
  class ProgressUpdater<T> extends Thread {
    /**
     * The time between progress updates in nanoseconds.
     */
    private long progressNanos = 0L;

    /**
     * The progress state.
     */
    private ProgressState progressState = null;

    /**
     * The {@link Object} to synchronize on.
     */
    protected final Object monitor;

    /**
     * The SSE event sink if sending updates via SSE.
     */
    private SseEventSink sseEventSink = null;

    /**
     * The SSE event builder if sending updates via SSE.
     */
    private OutboundSseEvent.Builder sseEventBuilder = null;

    /**
     * The Web Sockets Session if sending updates via web sockets.
     */
    private Session webSocketSession = null;

    /**
     * The supplier for providing the updates.
     */
    private Supplier<T> supplier = null;

    /**
     * Whether or not we are completed.
     */
    private boolean completed = false;

    /**
     * Constructs with the specified parameters.
     *
     * @param progressNanos The number of nano-seconds between progress updates.
     * @param progressState The {@link ProgressState} to manage the progress
     *                      updates.
     * @param monitor The object to synchronize, wait and notify on or
     *                <tt>null</tt> if it should synchronize on itself.
     * @param supplier THe {@link Supplier} for providing the update object.
     * @param sseEventSink The event sink for SSE if sending progress updates
     *                     via SSE, or <tt>null</tt> if not sending via SSE.
     * @param sseEventBuilder The SSE event builder if sending progress updates
     *                        via SSE, or <tt>null</tt> if not sending via SSE.
     * @param webSocketSession The web sockets {@link Session} if sending
     *                         progress udpates via web sockets, or
     *                         <tt>null</tt> if not sending via web sockets.
     */
    public ProgressUpdater(long                     progressNanos,
                           ProgressState            progressState,
                           Object                   monitor,
                           Supplier<T>              supplier,
                           SseEventSink             sseEventSink,
                           OutboundSseEvent.Builder sseEventBuilder,
                           Session                  webSocketSession)
    {
      Objects.requireNonNull(progressState,"The progressState cannot be null.");
      Objects.requireNonNull(supplier, "The supplier cannot be null.");

      this.progressNanos    = progressNanos;
      this.progressState    = progressState;
      this.monitor          = (monitor == null) ? this : monitor;
      this.supplier         = supplier;
      this.sseEventSink     = sseEventSink;
      this.sseEventBuilder  = sseEventBuilder;
      this.webSocketSession = webSocketSession;
    }

    /**
     * Marks this instance as completed.
     */
    public void complete() {
      synchronized (this.monitor) {
        this.completed = true;
        monitor.notifyAll();
      }
    }

    /**
     * Checks if this instance is completed.
     *
     * @return <tt>true</tt> if this instance is completed, otherwise
     *         <tt>false</tt>.
     */
    public boolean isCompleted() {
      synchronized (this.monitor) {
        return this.completed;
      }
    }

    /**
     * Implemented to send the progress updates periodically.
     */
    public void run() {
      while (!this.isCompleted()) {
        long now        = System.nanoTime();
        long duration   = now - this.progressState.getStartTime();
        long sleepTime  = (this.progressNanos - duration) / 1000000L;

        // check if it is not yet time for the next progress update
        if (sleepTime > 0) {
          // sleep for the
          synchronized (this.monitor) {
            try {
              // wait for notification or the maximum sleep time
              this.monitor.wait(sleepTime);
            } catch (InterruptedException ignore) {
              // do nothing -- ignore
            }
          }

        } else {
          // send a progress update (if not completed)
          this.sendProgressUpdate();
        }
      }
    }

    /**
     * Sends the next progress update.
     */
    protected void sendProgressUpdate() {
      synchronized (this.monitor) {
        // check if already completed
        if (this.isCompleted()) return;

        // get the current time
        long now = System.nanoTime();

        // create the update response if there is a client expecting it
        T update = null;
        if (this.sseEventBuilder != null || this.webSocketSession != null) {
          this.progressState.setStartTime(now);
          update = this.supplier.get();
        }

        // check if sending an SSE response
        if (this.sseEventBuilder != null) {
          OutboundSseEvent event =
              this.sseEventBuilder.name(PROGRESS_EVENT)
                  .id(String.valueOf(this.progressState.nextEventId()))
                  .mediaType(APPLICATION_JSON_TYPE)
                  .data(update)
                  .reconnectDelay(RECONNECT_DELAY)
                  .build();
          this.sseEventSink.send(event);
        }

        // check if sending a web socket response
        if (this.webSocketSession != null) {
          try {
            // send the web socket message and handle exceptions
            this.webSocketSession.getBasicRemote().sendObject(update);

          } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;

          } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

}
