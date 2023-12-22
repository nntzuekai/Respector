package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.api.websocket.JsonEncoder;
import com.senzing.api.websocket.StringDecoder;
import com.senzing.datagen.*;
import com.senzing.io.ChunkedEncodingInputStream;
import com.senzing.io.IOUtilities;
import com.senzing.repomgr.RepositoryManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.*;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.beans.IntrospectionException;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

import static com.senzing.io.IOUtilities.*;
import static com.senzing.datagen.RecordType.*;
import static com.senzing.datagen.FeatureType.*;
import static com.senzing.datagen.UsageType.usageTypesFor;
import static com.senzing.datagen.FeatureDensity.*;
import static com.senzing.api.model.SzHttpMethod.POST;
import static com.senzing.api.services.ResponseValidators.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.api.Assertions.*;
import static com.senzing.api.model.SzBulkDataStatus.*;

@TestInstance(Lifecycle.PER_CLASS)
public class BulkDataServicesTest extends AbstractServiceTest {
  protected static final String CUSTOMER_DATA_SOURCE      = "CUSTOMER";
  protected static final String SUBSCRIBER_DATA_SOURCE    = "SUBSCRIBER";
  protected static final String EMPLOYEE_DATA_SOURCE      = "EMPLOYEE";
  protected static final String VENDOR_DATA_SOURCE        = "VENDOR";
  protected static final String PARTNER_DATA_SOURCE       = "PARTNER";
  protected static final String STORE_DATA_SOURCE         = "STORE";
  protected static final String CUSTOMERS_DATA_SOURCE     = "CUSTOMERS";
  protected static final String SUBSCRIBERS_DATA_SOURCE   = "SUBSCRIBERS";
  protected static final String EMPLOYEES_DATA_SOURCE     = "EMPLOYEES";
  protected static final String VENDORS_DATA_SOURCE       = "VENDORS";
  protected static final String PARTNERS_DATA_SOURCE      = "PARTNERS";
  protected static final String STORES_DATA_SOURCE        = "STORES";
  protected static final String CONTACTS_DATA_SOURCE      = "CONTACTS";

  protected static final Map<String, String> DATA_SOURCE_MAP;

  protected static final Map<String, RecordType> SOURCE_RECORD_TYPE_MAP;

  protected final long SEED = 8736123213L;

  private enum FlagValue {
    YES, NO, MIXED;
    public boolean toBoolean(int iteration) {
      switch (this) {
        case YES:
          return true;
        case NO:
          return false;
        case MIXED:
          return ((iteration % 2) == 0);
        default:
          throw new IllegalStateException(
              "Unhandled FlagValue: " + this);
      }
    }
    public FlagValue next() {
      switch (this) {
        case YES:
          return NO;
        case NO:
          return MIXED;
        case MIXED:
          return YES;
        default:
          throw new IllegalStateException(
              "Unhandled FlagValue: " + this);
      }
    }
  };

  static {
    try {
      Map<String, String> dataSourceMap = new LinkedHashMap<>();

      dataSourceMap.put(CUSTOMER_DATA_SOURCE, CUSTOMERS_DATA_SOURCE);
      dataSourceMap.put(SUBSCRIBER_DATA_SOURCE, SUBSCRIBERS_DATA_SOURCE);
      dataSourceMap.put(EMPLOYEE_DATA_SOURCE, EMPLOYEES_DATA_SOURCE);
      dataSourceMap.put(VENDOR_DATA_SOURCE, VENDORS_DATA_SOURCE);
      dataSourceMap.put(PARTNER_DATA_SOURCE, PARTNERS_DATA_SOURCE);
      dataSourceMap.put(STORE_DATA_SOURCE, STORES_DATA_SOURCE);

      DATA_SOURCE_MAP = Collections.unmodifiableMap(dataSourceMap);

      Map<String, RecordType> sourceRecordTypeMap = new LinkedHashMap<>();

      sourceRecordTypeMap.put(CUSTOMER_DATA_SOURCE, PERSON);
      sourceRecordTypeMap.put(EMPLOYEE_DATA_SOURCE, PERSON);
      sourceRecordTypeMap.put(SUBSCRIBER_DATA_SOURCE, PERSON);
      sourceRecordTypeMap.put(VENDOR_DATA_SOURCE, ORGANIZATION);
      sourceRecordTypeMap.put(PARTNER_DATA_SOURCE, ORGANIZATION);
      sourceRecordTypeMap.put(STORE_DATA_SOURCE, BUSINESS);

      SOURCE_RECORD_TYPE_MAP = Collections.unmodifiableMap(sourceRecordTypeMap);

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);
    }
  }

  protected BulkDataServices bulkDataServices;
  protected DataGenerator dataGenerator;
  protected Random prng;

  @BeforeAll public void initializeEnvironment() {
    this.beginTests();
    this.initializeTestEnvironment();
    this.bulkDataServices = new BulkDataServices();
    this.dataGenerator    = new DataGenerator(SEED);
    this.prng             = new Random(SEED);
  }

  /**
   * Overridden to configure some data sources.
   */
  protected void prepareRepository() {
    Set<String> dataSourceSet = new LinkedHashSet<>(DATA_SOURCE_MAP.values());
    dataSourceSet.add(CONTACTS_DATA_SOURCE);

    RepositoryManager.configSources(this.getRepositoryDirectory(),
                                    dataSourceSet,
                                    true);
  }

  @AfterAll public void teardownEnvironment() {
    try {
      this.teardownTestEnvironment();
      this.conditionallyLogCounts(true);
    } finally {
      this.endTests();
    }
  }

  /**
   * Override to return 8 threads.
   * @return The number of server threads (8).
   */
  protected int getServerConcurrency() {
    return 8;
  }

  private Map<FeatureType, Set<UsageType>> featureGenMap(RecordType recordType)
  {
    Map<FeatureType, Set<UsageType>> map = new LinkedHashMap<>();

    // determine the default feature counts
    int maxNames       = prng.nextInt(2) + 1;
    int maxAddresses   = prng.nextInt(3) + 1;
    int maxPhones      = prng.nextInt(3) + 1;
    int maxEmails      = prng.nextInt(3) + 1;

    map.put(NAME, UsageType.usageTypesFor(NAME, recordType, maxNames, true));
    if (recordType == PERSON) map.put(BIRTH_DATE, null);
    map.put(ADDRESS, usageTypesFor(ADDRESS, recordType, maxAddresses, true));
    map.put(PHONE, usageTypesFor(PHONE, recordType, maxPhones, true));
    map.put(EMAIL, usageTypesFor(EMAIL, recordType, maxEmails, true));

    return map;
  }

  private Map<FeatureType, FeatureDensity> featureDensityMap() {
    Map<FeatureType, FeatureDensity> map = new LinkedHashMap<>();
    map.put(NAME, FIRST_THEN_SPARSE);
    map.put(BIRTH_DATE, COMMON);
    map.put(ADDRESS, COMMON);
    map.put(PHONE, COMMON);
    map.put(EMAIL, COMMON);
    return map;
  }

  private List<Arguments> getLoadBulkRecordsParameters() {
    List<Arguments> analyzeParams = this.getAnalyzeBulkRecordsParameters();
    List<Arguments> result = new LinkedList<>();

    boolean evenOdd = true;
    for (Arguments args : analyzeParams) {
      Object[]            argArray  = args.get();
      String              testInfo  = (String) argArray[0];
      MediaType           mediaType = (MediaType) argArray[1];
      File                dataFile  = (File) argArray[2];
      SzBulkDataAnalysis  analysis  = (SzBulkDataAnalysis) argArray[3];

      Set<String> dataSources     = new LinkedHashSet<>();

      analysis.getAnalysisByDataSource().forEach(abds -> {
        dataSources.add(abds.getDataSource());
      });

      Map<String, String> dataSourceMap = new LinkedHashMap<>();

      for (String dataSource: dataSources) {
        if (dataSource != null) {
          dataSourceMap.put(dataSource, DATA_SOURCE_MAP.get(dataSource));
        }
      }

      testInfo = testInfo + ", dataSourceMap=[ " + dataSourceMap + " ]";

      if (evenOdd) {
        result.add(Arguments.of(
            testInfo + ", mapping=[ GENERIC ]",
            mediaType,
            dataFile,
            analysis,
            null,
            null));
      } else {
        result.add(Arguments.of(
            testInfo + ", mapping=[ SPECIFIC ]",
            mediaType,
            dataFile,
            analysis,
            dataSourceMap));
      }
      evenOdd = !evenOdd;
    }

    return result;
  }

  private List<Arguments> getAnalyzeBulkRecordsParameters() {
    Set<String> dataSources = DATA_SOURCE_MAP.keySet();

    String UTF8_SUFFIX          = "; charset=UTF-8";
    String CSV_SPEC             = "text/csv";
    String CSV_UTF8_SPEC        = CSV_SPEC + UTF8_SUFFIX;
    String JSON_SPEC            = "application/json";
    String JSON_UTF8_SPEC       = JSON_SPEC + UTF8_SUFFIX;
    String JSON_LINES_SPEC      = "application/x-jsonlines";
    String JSON_LINES_UTF8_SPEC = JSON_LINES_SPEC + UTF8_SUFFIX;
    String TEXT_SPEC            = "text/plain";
    String TEXT_UTF8_SPEC       = TEXT_SPEC + UTF8_SUFFIX;

    MediaType CSV             = MediaType.valueOf(CSV_SPEC);
    MediaType CSV_UTF8        = MediaType.valueOf(CSV_UTF8_SPEC);
    MediaType JSON            = MediaType.valueOf(JSON_SPEC);
    MediaType JSON_UTF8       = MediaType.valueOf(JSON_UTF8_SPEC);
    MediaType JSON_LINES      = MediaType.valueOf(JSON_LINES_SPEC);
    MediaType JSON_LINES_UTF8 = MediaType.valueOf(JSON_LINES_UTF8_SPEC);
    MediaType TEXT            = MediaType.valueOf(TEXT_SPEC);
    MediaType TEXT_UTF8       = MediaType.valueOf(TEXT_UTF8_SPEC);

    Map<MediaType, List<Arguments>> bulkDataMap = new LinkedHashMap<>();

    List<MediaType> csvMediaTypes = Arrays.asList(
        CSV, TEXT_UTF8);
    List<MediaType> jsonMediaTypes = Arrays.asList(
        JSON_UTF8, TEXT);
    List<MediaType> jsonLinesMediaTypes = Arrays.asList(
        JSON_LINES, TEXT);

    Map<String, List<MediaType>> mediaTypesMap = new LinkedHashMap<>();
    mediaTypesMap.put(CSV_SPEC, csvMediaTypes);
    mediaTypesMap.put(JSON_SPEC, jsonMediaTypes);
    mediaTypesMap.put(JSON_LINES_SPEC, jsonLinesMediaTypes);

    boolean[] booleans = { true, false };

    // setup the feature-gen maps by record type
    Map<RecordType, Map<FeatureType, Set<UsageType>>> featureGenMaps
        = new LinkedHashMap<>();

    // aggregate all features into a single map for CSV record handler
    for (RecordType recordType: RecordType.values()) {
      Map<FeatureType, Set<UsageType>> map = featureGenMap(recordType);
      featureGenMaps.put(recordType, map);
    }

    int dataFileIndex = 0;

    FlagValue withRecordIds   = FlagValue.MIXED;
    boolean   fullValues      = false;
    boolean   flatten         = false;

    // iterate over the data sources
    for (int dataSourceCount = 1; dataSourceCount < dataSources.size();
         dataSourceCount+=2)
    {
      // get the data source list
      List<String> dataSourceList = new ArrayList<>(dataSources);
      int start = (dataSourceCount > 3) ? 2 : 0;
      dataSourceList = dataSourceList.subList(start, dataSourceCount);

      // find the set of record types
      Set<RecordType> recordTypes = new LinkedHashSet<>();
      for (String dataSource: dataSourceList) {
        RecordType recordType = SOURCE_RECORD_TYPE_MAP.get(dataSource);
        recordTypes.add(recordType);
      }

      // create the aggregate feature gen map
      Map<FeatureType, Set<UsageType>> allFeatureGenMap = new LinkedHashMap<>();
      for (RecordType recordType: recordTypes) {
        Map<FeatureType,Set<UsageType>> map = featureGenMaps.get(recordType);
        map.entrySet().forEach(entry -> {
          FeatureType     featureType = entry.getKey();
          Set<UsageType>  usageTypes  = entry.getValue();
          Set<UsageType>  set         = allFeatureGenMap.get(featureType);

          if (set == null) {
            set = new LinkedHashSet<>();
            allFeatureGenMap.put(featureType, set);
          }
          if (usageTypes != null) set.addAll(usageTypes);
          else set.add(null);
        });
      }

      // get the feature density map
      Map<FeatureType, FeatureDensity> featureDensityMap = featureDensityMap();

      // set the boolean/flag values
      withRecordIds   = withRecordIds.next();
      fullValues      = !fullValues;
      flatten         = !flatten;

      String testInfo = "dataSources=[ " + dataSourceList
          + " ], withRecordIds=[ " + withRecordIds
          + " ], fullValues=[ " + fullValues
          + " ], flatten=[ " + flatten + " ]";

      File dataDir = new File(this.getRepositoryDirectory(), "data");
      dataDir.mkdirs();

      RecordHandler recordHandler = null;
      File          csvFile       = null;
      File          jsonFile      = null;
      File          jsonLinesFile = null;

      Map<String, File> dataFileMap = new LinkedHashMap<>();

      SzBulkDataAnalysis csvAnalysis    = SzBulkDataAnalysis.FACTORY.create();
      SzBulkDataAnalysis jsonAnalysis   = SzBulkDataAnalysis.FACTORY.create();
      SzBulkDataAnalysis jsonlAnalysis  = SzBulkDataAnalysis.FACTORY.create();

      SzBulkDataAnalysis[] analyses = {
          csvAnalysis, jsonAnalysis, jsonlAnalysis
      };
      for (SzBulkDataAnalysis analysis : analyses) {
        analysis.setCharacterEncoding("UTF-8");
      }
      csvAnalysis.setMediaType(CSV_SPEC);
      jsonAnalysis.setMediaType(JSON_SPEC);
      jsonlAnalysis.setMediaType(JSON_LINES_SPEC);

      try {
        dataFileIndex++;
        String prefix = "data-" + ((dataFileIndex<1000)?"0":"")
            + ((dataFileIndex<100)?"0":"") + ((dataFileIndex<10)?"0":"")
            + dataFileIndex + "-";
        csvFile = File.createTempFile(prefix, ".csv", dataDir);
        jsonFile = new File(
            csvFile.toString().replaceAll("\\.csv$", ".json"));
        jsonLinesFile = new File(
            csvFile.toString().replaceAll("\\.csv$", ".jsonl"));

        dataFileMap.put(CSV_SPEC, csvFile);
        dataFileMap.put(JSON_SPEC, jsonFile);
        dataFileMap.put(JSON_LINES_SPEC, jsonLinesFile);

        Writer csvWriter = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(csvFile), UTF_8));

        Writer jsonWriter = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(jsonFile), UTF_8));

        Writer jsonLinesWriter = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(jsonLinesFile), UTF_8));

        CSVRecordHandler csvHandler = new CSVRecordHandler(
            csvWriter,
            (withRecordIds != FlagValue.NO),
            (dataSourceList.size() > 0),
            allFeatureGenMap,
            recordTypes,
            fullValues);
        JsonArrayRecordHandler jsonHandler
            = new JsonArrayRecordHandler(jsonWriter);
        JsonLinesRecordHandler jsonLinesHandler
            = new JsonLinesRecordHandler(jsonLinesWriter);

        recordHandler = new CompoundRecordHandler(csvHandler,
                                                  jsonHandler,
                                                  jsonLinesHandler);

        int iteration = 0;
        for (String dataSource: dataSourceList) {
          RecordType recordType = SOURCE_RECORD_TYPE_MAP.get(dataSource);

          int recordCount = (dataSourceList.size() == 1) ? 500
              : Math.max(500, ((iteration + 1) * 2000) % 2500);

          Map<FeatureType, Set<UsageType>> featureGenMap
              = featureGenMaps.get(recordType);

          boolean includeRecordIds = withRecordIds.toBoolean(iteration);

          this.dataGenerator.generateRecords(recordHandler,
                                             recordType,
                                             recordCount,
                                             includeRecordIds,
                                             dataSource,
                                             featureGenMap,
                                             featureDensityMap,
                                             fullValues,
                                             flatten);

          for (SzBulkDataAnalysis analysis : analyses) {
            analysis.trackRecords(recordCount,
                                  dataSource,
                                  includeRecordIds);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      } finally {
        if (recordHandler != null) recordHandler.close();
      }

      Map<String, SzBulkDataAnalysis> analysisMap
          = new LinkedHashMap<>();
      for (SzBulkDataAnalysis analysis : analyses) {
        analysis.setStatus(SzBulkDataStatus.COMPLETED);
        analysisMap.put(analysis.getMediaType(), analysis);
      }

      mediaTypesMap.entrySet().forEach(entry -> {
        String mediaTypeSpec = entry.getKey();
        List<MediaType> mediaTypes = entry.getValue();
        SzBulkDataAnalysis analysis = analysisMap.get(mediaTypeSpec);
        File dataFile = dataFileMap.get(mediaTypeSpec);

        for (MediaType mediaType : mediaTypes) {
          String fullTestInfo = "recordCount[ " + analysis.getRecordCount()
              + " ], " + testInfo + ", mediaType=[ " + mediaType
              + " ], format=[ " + mediaTypeSpec + " ], dataFile=[ "
              + dataFile + " ]";

          List<Arguments> list = bulkDataMap.get(mediaType);
          if (list == null) {
            list = new LinkedList<>();
            bulkDataMap.put(mediaType, list);
          }
          list.add(Arguments.arguments(
              fullTestInfo,
              mediaType,
              dataFile,
              analysis
          ));
        }
      });
    }
    List<Arguments> result = new LinkedList<>();
    boolean[] firstLast = { true };
    bulkDataMap.values().forEach(argsList -> {
      if (argsList != null) {
        Arguments args = (firstLast[0])
            ? argsList.get(0) : argsList.get(argsList.size() - 1);
        result.add(args);
        firstLast[0] = !firstLast[0];
      }
    });
    return result;
  }

  @ParameterizedTest
  @MethodSource("getAnalyzeBulkRecordsParameters")
  public void analyzeBulkRecordsViaFormTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  expected)
  {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("bulk-data/analyze");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      try (FileInputStream fis = new FileInputStream(bulkDataFile)) {
        long before = System.nanoTime();
        SzBulkDataAnalysisResponse response
            = this.bulkDataServices.analyzeBulkRecordsViaForm(
            mediaType, fis, uriInfo);
        response.concludeTimers();
        long after = System.nanoTime();

        validateAnalyzeResponse(testInfo,
                                response,
                                POST,
                                uriText,
                                mediaType,
                                bulkDataFile,
                                expected,
                                after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getAnalyzeBulkRecordsParameters")
  public void analyzeBulkRecordsViaDirectHttpTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  expected)
  {
    this.performTest(() -> {
      String uriText = this.formatServerUri("bulk-data/analyze");

      try (FileInputStream fis = new FileInputStream(bulkDataFile)) {
        long before = System.nanoTime();
        SzBulkDataAnalysisResponse response = this.invokeServerViaHttp(
            POST, uriText, null, mediaType.toString(),
            bulkDataFile.length(), new FileInputStream(bulkDataFile),
            SzBulkDataAnalysisResponse.class);
        response.concludeTimers();
        long after = System.nanoTime();

        validateAnalyzeResponse(testInfo,
                                response,
                                POST,
                                uriText,
                                mediaType,
                                bulkDataFile,
                                expected,
                                after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getAnalyzeBulkRecordsParameters")
  public void analyzeBulkRecordsViaFormJavaClientTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  expected)
  {
    this.performTest(() -> {
      String uriText = this.formatServerUri("bulk-data/analyze");

      try (FileInputStream fis = new FileInputStream(bulkDataFile)) {
        long before = System.nanoTime();
        com.senzing.gen.api.model.SzBulkDataAnalysisResponse clientResponse
            = this.invokeServerViaHttp(
            POST, uriText, null, mediaType.toString(),
            bulkDataFile.length(), new FileInputStream(bulkDataFile),
            com.senzing.gen.api.model.SzBulkDataAnalysisResponse.class);
        long after = System.nanoTime();

        SzBulkDataAnalysisResponse response
            = jsonCopy(clientResponse, SzBulkDataAnalysisResponse.class);

        validateAnalyzeResponse(testInfo,
                                response,
                                POST,
                                uriText,
                                mediaType,
                                bulkDataFile,
                                expected,
                                after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ClientEndpoint
  public static class BulkDataWebSocketClient extends Thread {
    private Session   webSocketSession;
    private File      bulkDataFile;
    private MediaType mediaType;
    private List      queue;

    /**
     * Constructs with the bulk data file and media type.
     * @param file The file containing the bulk data.
     * @param mediaType The media type describing the file format.
     */
    public BulkDataWebSocketClient(File file, MediaType mediaType) {
      this.webSocketSession = null;
      this.bulkDataFile     = file;
      this.mediaType        = mediaType;
      this.queue            = new LinkedList<>();
    }

    @OnOpen
    public synchronized void onOpen(Session session, EndpointConfig config) {
      this.webSocketSession = session;
      this.notifyAll();
    }

    @OnMessage
    public synchronized void onMessage(String jsonText) {
      this.queue.add(jsonText);
      this.notifyAll();
    }

    @OnError
    public synchronized void onError(Throwable throwable)
        throws IOException
    {
      this.queue.add(throwable);
      this.notifyAll();
    }

    /**
     * Gets the next response (or error) that was generated.
     * @return The next response (or error) that was generated.
     */
    public Object getNextResponse() {
      synchronized (this) {
        while (this.isOpen() && this.queue.size() == 0) {
          try {
            this.wait(5000L);
          } catch (InterruptedException ignore) {
            // do nothing
          }
        }
        if (this.queue.size() > 0) {
          return this.queue.remove(0);
        }
        return null;
      }
    }

    /**
     * Checks if the web socket session is still open.
     * @return <tt>true</tt> if the session is still open, otherwise
     *         <tt>false</tt>.
     */
    public synchronized boolean isOpen() {
      return (this.webSocketSession != null && this.webSocketSession.isOpen());
    }

    @Override
    public void run() {
      synchronized (this) {
        // wait for web socket session to open
        while (this.webSocketSession == null) {
          try {
            this.wait(5000L);
          } catch (InterruptedException ignore) {
            // ignore
          }
        }
      }

      // check if we should send the file as text
      boolean asText = ((this.mediaType != null)
          && (this.mediaType.getParameters().get("charset") != null));

      try {
        if (asText) {
          this.sendText();
        } else {
          this.sendBinary();
        }

      } catch (IOException e) {
        synchronized (this) {
          this.queue.add(e);
          this.notifyAll();
        }
      }
    }

    /**
     * Sends the file as text.
     * @throws IOException If an I/O failure occurs.
     */
    private void sendText() throws IOException {
      String encoding = this.mediaType.getParameters().get("charset");
      char[] buffer = new char[1024];
      try (FileInputStream fis = new FileInputStream(this.bulkDataFile);
           BufferedInputStream bis = new BufferedInputStream(fis);
           InputStreamReader isr = new InputStreamReader(bis, encoding))
      {
        for (int readCount = isr.read(buffer);
             readCount >= 0;
             readCount = isr.read(buffer))
        {
          String text = new String(buffer, 0, readCount);
          this.webSocketSession.getBasicRemote().sendText(text);
        }
      }
    }

    /**
     * Sends the file as binary.
     * @throws IOException If an I/O failure occurs.
     */
    private void sendBinary() throws IOException {
      byte[] buffer = new byte[1024];
      try (FileInputStream fis = new FileInputStream(this.bulkDataFile);
           BufferedInputStream bis = new BufferedInputStream(fis))
      {
        long start = -1;
        for (int readCount = bis.read(buffer);
             readCount >= 0;
             readCount = bis.read(buffer))
        {
          ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, readCount);
          long end = System.nanoTime();
          if ((start > 0) && (end-start > 3000000000L)) {
            System.out.println("EXCESSIVE TIME BETWEEN SENDS: " + ((end-start)/1000000L));
          }
          this.webSocketSession.getBasicRemote().sendBinary(byteBuffer);
          start = end;
        }
      }

    }
  }

  @ClientEndpoint
  public static class BulkDataSSEClient extends Thread {
    private URL               url;
    private Socket            socket;
    private OutputStream      outputStream;
    private InputStream       inputStream;
    private File              bulkDataFile;
    private MediaType         mediaType;
    private List              queue;
    private boolean           open = true;
    private Integer           statusCode = null;
    private Thread            readerThread = null;

    /**
     * Constructs with the bulk data file and media type.
     * @param url The URL to connect to.
     * @param file The file containing the bulk data.
     * @param mediaType The media type describing the file format.
     */
    public BulkDataSSEClient(URL                url,
                             File               file,
                             MediaType          mediaType) {
      this.bulkDataFile = file;
      this.mediaType    = mediaType;
      this.queue        = new LinkedList<>();
      this.open         = true;
      this.statusCode   = null;

      // setup the headers
      try {
        String host = url.getHost();
        int port = url.getPort();
        this.socket = new Socket(host, port);
        this.outputStream = this.socket.getOutputStream();
        this.inputStream  = this.socket.getInputStream();

        StringBuilder sb = new StringBuilder();
        sb.append("POST ").append(url.getPath());
        if (url.getQuery() != null) {
          sb.append("?").append(url.getQuery());
        }
        sb.append(" HTTP/1.1\r\n");
        sb.append("Host: ").append(url.getHost()).append(":")
            .append(url.getPort()).append("\r\n");
        sb.append("Accept: text/event-stream\r\n");
        sb.append("Accept-Charset: utf-8\r\n");
        sb.append("Accept-Encoding: identity\r\n");
        sb.append("Accept-Transfer-Encoding: identity\r\n");
        sb.append("Content-Length: ").append(this.bulkDataFile.length())
            .append("\r\n");
        sb.append("Content-Type: ");
        if (this.mediaType != null) {
          sb.append(this.mediaType.toString());
        } else {
          sb.append(MediaType.TEXT_PLAIN);
        }
        sb.append("\r\n\r\n");
        this.outputStream.write(sb.toString().getBytes("ASCII"));
        this.outputStream.flush();

        if (this.readerThread == null) {
          this.readerThread = new Thread(() -> this.readRun());
          this.readerThread.start();
        }

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Gets the HTTP response status code.  This method returns <tt>null</tt>
     * if the status code has not yet been received.
     *
     * @return The HTTP response status code, or <tt>null</tt> if the code has
     *         not yet been received.
     */
    public synchronized Integer getStatusCode() { return this.statusCode; }

    /**
     * Sets the HTTP response status code to a non-null value.
     *
     * @param statusCode The status code value to set.
     */
    public synchronized void setStatusCode(int statusCode) {
      this.statusCode = statusCode;
      this.notifyAll();
    }

    /**
     * Checks if the connection is still open.
     */
    public synchronized boolean isOpen() {
      return this.open;
    }

    /**
     * Closes the connection.
     */
    public synchronized void close() {
      this.open = false;
      IOUtilities.close(this.outputStream);
      IOUtilities.close(this.inputStream);
      IOUtilities.close(this.socket);
      this.outputStream = null;
      this.inputStream = null;
      this.socket = null;
      this.notifyAll();
    }

    /**
     * Gets the next response (or error) that was generated.
     * @return The next response (or error) that was generated.
     */
    public Object getNextResponse() {
      synchronized (this) {
        while (this.isOpen() && this.queue.size() == 0) {
          try {
            this.wait(5000L);
          } catch (InterruptedException ignore) {
            // do nothing
          }
        }
        if (this.queue.size() > 0) {
          return this.queue.remove(0);
        }
        return null;
      }
    }

    public void readRun() {
      boolean completed = false;
      boolean chunked = false;
      boolean sse     = false;
      try (BufferedInputStream bis = new BufferedInputStream(this.inputStream))
      {
        boolean first = true;
        for (String line = readAsciiLine(bis);
             (line != null);
             line = readAsciiLine(bis))
        {
          // check if the first line
          if (first) {
            first = false;
            String[] tokens = line.split("\\s+");
            if (tokens.length < 3) {
              throw new IllegalStateException(
                  "Unexpected number of tokens in HTTP status line: "
                  + line);
            }
            try {
              int statusCode = Integer.parseInt(tokens[1]);

              // set the status code
              this.setStatusCode(statusCode);

            } catch (IllegalArgumentException e) {
              throw new IllegalStateException(
                  "Unable to parse HTTP status code (" + tokens[1] + ") from "
                  + "status line: " + line);
            }
          }

          // check if headers are complete
          if (line.trim().length() == 0) {
            break;
          }

          // check if chunked encoding
          if (line.toLowerCase().trim().startsWith("transfer-encoding:")) {
            chunked = (line.toLowerCase().trim().contains("chunked"));
          }

          // check if the response is SSE
          if (line.toLowerCase().trim().startsWith("content-type")) {
            sse = (line.toLowerCase().trim().contains("text/event-stream"));
          }
        }

        // create the input stream to read the remaining data
        InputStream is = (chunked) ? new ChunkedEncodingInputStream(bis) : bis;

        // create a reader
        InputStreamReader isr = new InputStreamReader(is, UTF_8);

        // if not SSE then read text
        if (!sse) {
          StringBuilder sb = new StringBuilder();
          try {
            for (int readChar = isr.read(); readChar >= 0; readChar = isr.read())
            {
              sb.append((char) readChar);
            }
          } catch (SocketException e) {
            System.err.println(
                "*** Received SocketException, assuming connection closed: "
                    + e);
          }
          synchronized (this) {
            this.queue.add(sb.toString());
            this.notifyAll();
          }
          this.close();
          return;
        }

        // create a buffered reader to read lines
        BufferedReader br = new BufferedReader(isr);

        // if SSE then read events
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          // skip empty lines
          if (line.trim().length() == 0) continue;

          // check if we get data after completed
          if (completed) {
            throw new IllegalStateException(
                "Received additional data after completed event: " + line);
          }

          // check if NOT an event
          if (!line.startsWith("event:")) {
            throw new IllegalStateException(
                "Received a line that was NOT an event: " + line);
          }

          // check if completed
          completed = line.equals("event: completed");

          StringWriter  sw = new StringWriter();
          PrintWriter   pw = new PrintWriter(sw);
          pw.println(line);
          line = br.readLine();
          if (!line.startsWith("id:")) {
            throw new IllegalStateException(
                "Unexpected text/event-stream output line.  "
                    + "Expected 'id:' property: " + sw.toString());
          }
          pw.println(line);
          line = br.readLine();
          if (!line.startsWith("retry:")) {
            throw new IllegalStateException(
                "Unexpected text/event-stream output line.  "
                    + "Expected 'retry:' property: " + sw.toString());
          }
          pw.println(line);
          line = br.readLine();
          if (!line.startsWith("data:")) {
            throw new IllegalStateException(
                "Unexpected text/event-stream output line.  "
                    + "Expected 'retry:' property: " + sw.toString());
          }
          pw.println(line);
          synchronized (this) {
            this.queue.add(line.substring("data:".length()));
            this.notifyAll();
          }
        }

        // if we reached the end of input mark as closed
        this.close();

      } catch (IOException e) {
        e.printStackTrace();
        synchronized (this) {
          this.queue.add(e);
          this.notifyAll();
        }
      }
    }

    @Override
    public void run() {
      byte[] buffer = new byte[8192];
      int writeCount = 0;
      long start = System.nanoTime();
      boolean firstChunk = true;
      try (FileInputStream fis = new FileInputStream(this.bulkDataFile))
      {
        for (int readCount = fis.read(buffer);
             readCount >= 0;
             readCount = fis.read(buffer))
        {
          synchronized (this) {
            // give the server a chance to deny the request out-right because
            // it is in read-only mode or if there is a client error
            if (firstChunk) {
              firstChunk = false;
              try {
                this.wait(600L);
              } catch (InterruptedException ignore) {
                // ignore the exception
              }
            }
            if (!this.isOpen()) break;
            try {
              Integer statusCode = this.getStatusCode();
              if (statusCode != null && statusCode != 200) break;
              this.outputStream.write(buffer, 0, readCount);
              this.outputStream.flush();
              writeCount += readCount;
            } catch (SocketException e) {
              System.err.println(
                  "*** Received SocketException, assuming connection closed: "
                      + e);
              break;
            }
          }
        }
      } catch (IOException e) {
        synchronized (this) {
          this.queue.add(e);
          this.notifyAll();
        }
      }
      long end = System.nanoTime();

      // wrap things up by joining with the reader thread and closing
      if (this.readerThread != null) {
        try {
          this.readerThread.join();
        } catch (InterruptedException ignore) {
          // do nothing
        }
        this.close();
      }
    }
  }

  @ParameterizedTest
  @MethodSource("getAnalyzeBulkRecordsParameters")
  public void analyzeBulkRecordsViaWebSocketsTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  expected)
  {
    this.performTest(() -> {
      String uriText = this.formatServerUri("bulk-data/analyze");
      uriText = uriText.replaceAll("^http:(.*)","ws:$1");

      try {
        long before = System.nanoTime();

        BulkDataWebSocketClient client = new BulkDataWebSocketClient(bulkDataFile, mediaType);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = container.connectToServer(client, URI.create(uriText));
        client.start();

        SzBulkDataAnalysisResponse finalResponse = null;
        // grab the results
        for (Object next = client.getNextResponse();
             next != null;
             next = client.getNextResponse())
        {
          // check if there was a failure
          if (next instanceof Throwable) {
            try {
              session.close();
            } catch (Exception ignore) {
              // ignore
            }
            fail((Throwable) next);
          }

          // get as a string
          String jsonText = next.toString();
          if (jsonText.matches(".*\"httpStatusCode\":\\s*200.*") ) {
            SzBulkDataAnalysisResponse response
                = jsonParse(jsonText, SzBulkDataAnalysisResponse.class);
            response.concludeTimers();
            SzBulkDataStatus status = response.getData().getStatus();
            switch (status) {
              case COMPLETED:
                finalResponse = response;
                break;
              case ABORTED:
                finalResponse = response;
                fail("Aborted analyze: " + jsonText);
                break;
              case NOT_STARTED:
              case IN_PROGRESS:
                // do nothing
                break;
              default:
                fail("Unrecognized status: " + status + " / "  + jsonText);
            }

          } else {
            SzErrorResponse response
                = jsonParse(jsonText, SzErrorResponse.class);
            response.concludeTimers();
            fail("Failed analyze: " + jsonText);
          }
        }

        long after = System.nanoTime();

        validateAnalyzeResponse(testInfo,
                                finalResponse,
                                POST,
                                uriText,
                                mediaType,
                                bulkDataFile,
                                expected,
                                after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }


  @ParameterizedTest
  @MethodSource("getAnalyzeBulkRecordsParameters")
  public void analyzeBulkRecordsTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  expected)
  {
    this.performTest(() -> {
      String uriText = this.formatServerUri("bulk-data/analyze");

      try {
        long before = System.nanoTime();

        URL url = new URL(uriText);

        BulkDataSSEClient client = new BulkDataSSEClient(url,
                                                         bulkDataFile,
                                                         mediaType);
        client.start();

        SzBulkDataAnalysisResponse finalResponse = null;
        // grab the results
        for (Object next = client.getNextResponse();
             next != null;
             next = client.getNextResponse())
        {
          // check if there was a failure
          if (next instanceof Throwable) {
            fail((Throwable) next);
          }

          // get as a string
          String jsonText = next.toString();
          if (jsonText.matches(".*\"httpStatusCode\":\\s*200.*") ) {
            SzBulkDataAnalysisResponse response
                = jsonParse(jsonText, SzBulkDataAnalysisResponse.class);
            response.concludeTimers();
            SzBulkDataStatus status = response.getData().getStatus();
            switch (status) {
              case COMPLETED:
                finalResponse = response;
                break;
              case ABORTED:
                finalResponse = response;
                fail("Aborted analyze: " + jsonText);
                break;
              case NOT_STARTED:
              case IN_PROGRESS:
                // do nothing
                break;
              default:
                fail("Unrecognized status: " + status + " / "  + jsonText);
            }

          } else {
            SzErrorResponse response
                = jsonParse(jsonText, SzErrorResponse.class);
            response.concludeTimers();
            fail("Failed analyze: " + jsonText);
          }
        }

        long after = System.nanoTime();

        validateAnalyzeResponse(testInfo,
                                finalResponse,
                                POST,
                                uriText,
                                mediaType,
                                bulkDataFile,
                                expected,
                                after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  protected String formatLoadURL(String               defaultDataSource,
                                 String               loadId,
                                 Integer              maxFailures,
                                 Map<String, String>  dataSourceMap,
                                 String               progressPeriod)
  {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("bulk-data/load");
      String prefix = "?";
      if (defaultDataSource != null) {
        sb.append(prefix).append("dataSource=").append(
            URLEncoder.encode(defaultDataSource, UTF_8));
        prefix = "&";
      }
      if (loadId != null) {
        sb.append(prefix).append("loadId=").append(
            URLEncoder.encode(loadId, UTF_8));
        prefix = "&";
      }
      if (maxFailures != null) {
        sb.append(prefix).append("maxFailures=").append(maxFailures);
        prefix = "&";
      }

      if (dataSourceMap != null) {
        String[]          prefixArr   = { prefix };
        boolean[]         jsonFlag    = { true };
        boolean[]         overlapFlag = { true };
        JsonObjectBuilder builder   = Json.createObjectBuilder();
        dataSourceMap.entrySet().forEach(entry -> {
          String  key   = entry.getKey();
          String  value = entry.getValue();
          if (jsonFlag[0] || overlapFlag[0]) {
            builder.add(key, value);

          } else {
            String mapping = ":" + key + ":" + value;
            try {
              sb.append(prefixArr[0]).append("mapDataSource=").append(
                  URLEncoder.encode(mapping, UTF_8));
              prefixArr[0] = "&";
            } catch (UnsupportedEncodingException cannotHappen) {
              throw new IllegalStateException("UTF-8 encoding not supported");
            }
            overlapFlag[0] = !overlapFlag[0];
          }
          jsonFlag[0] = !jsonFlag[0];
        });
        JsonObject jsonObject = builder.build();
        if (jsonObject.size() > 0) {
          String mapDataSources = jsonObject.toString();
          try {
            sb.append(prefixArr[0]).append("mapDataSources=").append(
                URLEncoder.encode(mapDataSources, UTF_8));

            prefixArr[0] = "&";

          } catch (UnsupportedEncodingException cannotHappen) {
            throw new IllegalStateException("UTF-8 encoding not supported");
          }
        }

        // update the prefix
        prefix = prefixArr[0];
      }

      if (progressPeriod != null) {
        sb.append(prefix).append("progressPeriod=").append(progressPeriod);
      }

      return sb.toString();

    } catch (UnsupportedEncodingException cannotHappen) {
      throw new IllegalStateException("UTF-8 encoding not supported");
    }
  }

  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  public void loadBulkRecordsViaFormTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  analysis,
      Map<String,String>  dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String  uriText = this.formatServerUri("bulk-data/load");

      MultivaluedMap  queryParams       = new MultivaluedHashMap();
      String          mapDataSources    = null;
      List<String>    mapDataSourceList = new LinkedList<>();
      if (dataSourceMap != null) {
        boolean[]         jsonFlag    = { true };
        boolean[]         overlapFlag = { true };
        JsonObjectBuilder builder   = Json.createObjectBuilder();
        dataSourceMap.entrySet().forEach(entry -> {
          String  key   = entry.getKey();
          String  value = entry.getValue();
          if (jsonFlag[0] || overlapFlag[0]) {
            builder.add(key, value);

          } else {
            String mapping = ":" + key + ":" + value;
            mapDataSourceList.add(mapping);
            queryParams.add("mapDataSource", mapping);
            overlapFlag[0] = !overlapFlag[0];
          }
          jsonFlag[0] = !jsonFlag[0];
        });
        JsonObject jsonObject = builder.build();
        if (jsonObject.size() > 0) {
          mapDataSources = jsonObject.toString();
          queryParams.add("mapDataSources", mapDataSources);
        }
      }

      UriInfo uriInfo = this.newProxyUriInfo(uriText, queryParams);

      try (FileInputStream fis = new FileInputStream(bulkDataFile)) {
        long before = System.nanoTime();
        SzBulkLoadResponse response
            = this.bulkDataServices.loadBulkRecordsViaForm(
                CONTACTS_DATA_SOURCE,
                mapDataSources,
                mapDataSourceList,
                null,
                0,
                mediaType,
                fis,
                null,
                uriInfo);
        response.concludeTimers();
        long after = System.nanoTime();

        Map<String,String> allDataSourceMap = new LinkedHashMap<>();
        allDataSourceMap.put(null, CONTACTS_DATA_SOURCE);
        if (dataSourceMap != null) allDataSourceMap.putAll(dataSourceMap);

        validateLoadResponse(testInfo,
                             response,
                             POST,
                             uriText,
                             COMPLETED,
                             mediaType,
                             bulkDataFile,
                             analysis,
                             analysis.getRecordCount(),
                             allDataSourceMap,
                             null,
                             after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  public void loadBulkRecordsViaDirectHttpTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  analysis,
      Map<String,String>  dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String uriText = this.formatServerUri(formatLoadURL(
          CONTACTS_DATA_SOURCE, null, null,
          dataSourceMap, null));

      try (FileInputStream fis = new FileInputStream(bulkDataFile)) {
        long before = System.nanoTime();
        SzBulkLoadResponse response = this.invokeServerViaHttp(
            POST, uriText, null, mediaType.toString(),
            bulkDataFile.length(), new FileInputStream(bulkDataFile),
            SzBulkLoadResponse.class);
        response.concludeTimers();
        long after = System.nanoTime();

        Map<String,String> allDataSourceMap = new LinkedHashMap<>();
        allDataSourceMap.put(null, CONTACTS_DATA_SOURCE);
        if (dataSourceMap != null) allDataSourceMap.putAll(dataSourceMap);

        validateLoadResponse(testInfo,
                             response,
                             POST,
                             uriText,
                             COMPLETED,
                             mediaType,
                             bulkDataFile,
                             analysis,
                             analysis.getRecordCount(),
                             allDataSourceMap,
                             null,
                             after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  public void loadBulkRecordsDirectJavaClientTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  analysis,
      Map<String,String>  dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String uriText = this.formatServerUri(formatLoadURL(
          CONTACTS_DATA_SOURCE, null, null,
          dataSourceMap, null));

      try (FileInputStream fis = new FileInputStream(bulkDataFile)) {
        long before = System.nanoTime();
        com.senzing.gen.api.model.SzBulkLoadResponse clientResponse
            = this.invokeServerViaHttp(
            POST, uriText, null, mediaType.toString(),
            bulkDataFile.length(), new FileInputStream(bulkDataFile),
            com.senzing.gen.api.model.SzBulkLoadResponse.class);

        long after = System.nanoTime();

        Map<String,String> allDataSourceMap = new LinkedHashMap<>();
        allDataSourceMap.put(null, CONTACTS_DATA_SOURCE);
        if (dataSourceMap != null) allDataSourceMap.putAll(dataSourceMap);

        SzBulkLoadResponse response = jsonCopy(clientResponse,
                                               SzBulkLoadResponse.class);

        validateLoadResponse(testInfo,
                             response,
                             POST,
                             uriText,
                             COMPLETED,
                             mediaType,
                             bulkDataFile,
                             analysis,
                             analysis.getRecordCount(),
                             allDataSourceMap,
                             null,
                             after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  public void loadBulkRecordsViaWebSocketsTest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  analysis,
      Map<String,String>  dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String uriText = this.formatServerUri(formatLoadURL(
          CONTACTS_DATA_SOURCE,null, null,
          dataSourceMap, null));
      uriText = uriText.replaceAll("^http:(.*)","ws:$1");

      try {
        long before = System.nanoTime();
        BulkDataWebSocketClient client = new BulkDataWebSocketClient(bulkDataFile, mediaType);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = container.connectToServer(client, URI.create(uriText));
        client.start();

        SzBulkLoadResponse finalResponse = null;
        // grab the results
        for (Object next = client.getNextResponse();
             next != null;
             next = client.getNextResponse())
        {
          // check if there was a failure
          if (next instanceof Throwable) {
            try {
              session.close();
            } catch (Exception ignore) {
              // ignore
            }
            fail((Throwable) next);
          }

          // get as a string
          String jsonText = next.toString();
          if (jsonText.matches(".*\"httpStatusCode\":\\s*200.*") ) {
            SzBulkLoadResponse response
                = jsonParse(jsonText, SzBulkLoadResponse.class);
            response.concludeTimers();
            SzBulkDataStatus status = response.getData().getStatus();
            switch (status) {
              case COMPLETED:
                finalResponse = response;
                break;
              case ABORTED:
                finalResponse = response;
                fail("Aborted bulk load: " + jsonText);
                break;
              case NOT_STARTED:
              case IN_PROGRESS:
                // do nothing
                break;
              default:
                fail("Unrecognized status: " + status + " / "  + jsonText);
            }

          } else {
            SzErrorResponse response
                = jsonParse(jsonText, SzErrorResponse.class);
            response.concludeTimers();
            fail("Failed bulk load: " + jsonText);
          }
        }

        long after = System.nanoTime();

        Map<String,String> allDataSourceMap = new LinkedHashMap<>();
        allDataSourceMap.put(null, CONTACTS_DATA_SOURCE);
        if (dataSourceMap != null) allDataSourceMap.putAll(dataSourceMap);

        validateLoadResponse(testInfo,
                             finalResponse,
                             POST,
                             uriText,
                             COMPLETED,
                             mediaType,
                             bulkDataFile,
                             analysis,
                             analysis.getRecordCount(),
                             allDataSourceMap,
                             null,
                             after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("getLoadBulkRecordsParameters")
  public void loadBulkRecordsViaSSETest(
      String              testInfo,
      MediaType           mediaType,
      File                bulkDataFile,
      SzBulkDataAnalysis  analysis,
      Map<String,String>  dataSourceMap)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String uriText = this.formatServerUri(formatLoadURL(
          CONTACTS_DATA_SOURCE, null, null,
          dataSourceMap, null));

      try {
        long before = System.nanoTime();

        URL url = new URL(uriText);

        BulkDataSSEClient client = new BulkDataSSEClient(url,
                                                         bulkDataFile,
                                                         mediaType);

        client.start();

        SzBulkLoadResponse finalResponse = null;
        // grab the results
        for (Object next = client.getNextResponse();
             next != null;
             next = client.getNextResponse())
        {
          // check if there was a failure
          if (next instanceof Throwable) {
            fail((Throwable) next);
          }

          // get as a string
          String jsonText = next.toString();
          if (jsonText.matches(".*\"httpStatusCode\":\\s*200.*") ) {
            SzBulkLoadResponse response
                = jsonParse(jsonText, SzBulkLoadResponse.class);
            response.concludeTimers();
            SzBulkDataStatus status = response.getData().getStatus();
            switch (status) {
              case COMPLETED:
                finalResponse = response;
                break;
              case ABORTED:
                finalResponse = response;
                fail("Aborted bulk load: " + jsonText);
                break;
              case NOT_STARTED:
              case IN_PROGRESS:
                // do nothing
                break;
              default:
                fail("Unrecognized status: " + status + " / "  + jsonText);
            }

          } else {
            SzErrorResponse response
                = jsonParse(jsonText, SzErrorResponse.class);
            response.concludeTimers();
            fail("Failed bulk load: " + jsonText);
          }
        }

        long after = System.nanoTime();

        Map<String,String> allDataSourceMap = new LinkedHashMap<>();
        allDataSourceMap.put(null, CONTACTS_DATA_SOURCE);
        if (dataSourceMap != null) allDataSourceMap.putAll(dataSourceMap);

        validateLoadResponse(testInfo,
                             finalResponse,
                             POST,
                             uriText,
                             COMPLETED,
                             mediaType,
                             bulkDataFile,
                             analysis,
                             analysis.getRecordCount(),
                             allDataSourceMap,
                             null,
                             after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  private List<Arguments> getMaxFailureArgs() {
    try {
      File[] tempFiles = {
          File.createTempFile("failures-test", ".jsonl"),
          File.createTempFile("failures-test", ".jsonl"),
          File.createTempFile("failures-test", ".jsonl")
      };

      List<RecordHandler> handlers = new ArrayList<>(tempFiles.length);
      for (File tempFile : tempFiles) {
        FileOutputStream      fos     = new FileOutputStream(tempFile);
        BufferedOutputStream  bos     = new BufferedOutputStream(fos);
        OutputStreamWriter    osw     = new OutputStreamWriter(bos, UTF_8);
        RecordHandler         handler = new JsonLinesRecordHandler(osw);
        handlers.add(handler);
      }

      Map<FeatureType, Set<UsageType>> featureGenMap = featureGenMap(PERSON);
      Map<FeatureType, FeatureDensity> featDensityMap = featureDensityMap();

      // add 12 bad records mixed in with 10 good ones (the 12 bad ones have
      // the data source as singular "CUSTOMER" rather than plural "CUSTOMERS")
      RecordHandler handler = new CompoundRecordHandler(handlers);
      this.dataGenerator.generateRecords(handler,
                                         PERSON,
                                         2,
                                         true,
                                         CUSTOMER_DATA_SOURCE,
                                         featureGenMap,
                                         featDensityMap,
                                         true,
                                         true);

      this.dataGenerator.generateRecords(handler,
                                         PERSON,
                                         10,
                                         true,
                                         CUSTOMERS_DATA_SOURCE,
                                         featureGenMap,
                                         featDensityMap,
                                         true,
                                         true);

      this.dataGenerator.generateRecords(handler,
                                         PERSON,
                                         10,
                                         true,
                                         CUSTOMER_DATA_SOURCE,
                                         featureGenMap,
                                         featDensityMap,
                                         true,
                                         true);

      // the first handler gets 978 additional good records to make an even 1000
      // (this is the maximum that should be handled in a single thread)
      this.dataGenerator.generateRecords(handlers.get(0),
                                         PERSON,
                                         978,
                                         true,
                                         CUSTOMERS_DATA_SOURCE,
                                         featureGenMap,
                                         featDensityMap,
                                         true,
                                         true);

      // the second handler gets 979 additional good records to make for 1001
      // (this is the minimum to trigger concurrent handling)
      this.dataGenerator.generateRecords(handlers.get(1),
                                         PERSON,
                                         979,
                                         true,
                                         CUSTOMERS_DATA_SOURCE,
                                         featureGenMap,
                                         featDensityMap,
                                         true,
                                         true);

      List<Arguments> result = new LinkedList<>();

      for (int index = 0; index < tempFiles.length; index++) {
        File tempFile = tempFiles[index];
        int recordCount = (index == 2) ? 22 : (1000 + index);
        result.add(Arguments.of(
            recordCount,
            null,
            COMPLETED,
            Collections.singletonMap(CUSTOMER_DATA_SOURCE, 12),
            tempFile));

        result.add(Arguments.of(
            recordCount,
            -1,
            COMPLETED,
            Collections.singletonMap(CUSTOMER_DATA_SOURCE, 12),
            tempFile));

        result.add(Arguments.of(
            recordCount,
            5,
            ABORTED,
            Collections.singletonMap(CUSTOMER_DATA_SOURCE, 5),
            tempFile));
      }
      return result;

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @ParameterizedTest
  @MethodSource("getMaxFailureArgs")
  public void testMaxFailuresOnLoad(
      int                   recordCount,
      Integer               maxFailures,
      SzBulkDataStatus      expectedStatus,
      Map<String, Integer>  failuresByDataSource,
      File                  dataFile)
  {
    this.performTest(() -> {
      this.livePurgeRepository();

      String testInfo = "recordCount=[ " + recordCount + " ], maxFailures=[ "
          + maxFailures + " ], status=[ "
          + expectedStatus + " ], failuresByDataSource=[ "
          + failuresByDataSource + " ], dataFile=[ " + dataFile + " ]";

      String uriText = this.formatServerUri("bulk-data/load");

      MultivaluedMap queryParams = new MultivaluedHashMap();
      if (maxFailures != null) {
        queryParams.add("maxFailures", String.valueOf(maxFailures));
      }
      UriInfo uriInfo = this.newProxyUriInfo(uriText, queryParams);

      SzBulkLoadResponse response = null;
      try (InputStream is = new FileInputStream(dataFile);
           BufferedInputStream bis = new BufferedInputStream(is)) {
        long before = System.nanoTime();
        response = this.bulkDataServices.loadBulkRecordsViaForm(
            null,
            null,
            null,
            null,
            maxFailures == null ? -1 : maxFailures,
            MediaType.valueOf("text/plain"),
            bis,
            null,
            uriInfo);

        response.concludeTimers();
        long after = System.nanoTime();

        this.validateLoadResponse(testInfo,
                                  response,
                                  POST,
                                  uriText,
                                  expectedStatus,
                                  MediaType.valueOf("text/plain"),
                                  dataFile,
                                  null,
                                  recordCount,
                                  null,
                                  failuresByDataSource,
                                  after - before);

      } catch (Exception e) {
        System.err.println("********** FAILED TEST: " + testInfo);
        e.printStackTrace();
        if (e instanceof RuntimeException) throw ((RuntimeException) e);
        throw new RuntimeException(e);
      }
    });
  }

  /**
   *
   */
  private void validateAnalyzeResponse(String                     testInfo,
                                       SzBulkDataAnalysisResponse response,
                                       SzHttpMethod               httpMethod,
                                       String                     selfLink,
                                       MediaType                  mediaType,
                                       File                       bulkDataFile,
                                       SzBulkDataAnalysis         expected,
                                       long                       maxDuration)
  {
    validateBasics(response, httpMethod, selfLink, maxDuration);

    SzBulkDataAnalysis actual = response.getData();

    assertNotNull(actual, "Response analysis is null: " + testInfo);

    assertEquals(expected.getCharacterEncoding(), actual.getCharacterEncoding(),
        "Character encoding in analysis not as expected: " + testInfo);

    assertEquals(expected.getMediaType(), actual.getMediaType(),
                 "Media type in analysis not as expected: " + testInfo);

    assertEquals(expected.getRecordCount(), actual.getRecordCount(),
                 "Total record count not as expected: " + testInfo);

    assertEquals(expected.getRecordsWithRecordIdCount(),
                 actual.getRecordsWithRecordIdCount(),
                 "Records with record ID count not as expected: "
                 + testInfo);

    assertEquals(expected.getRecordsWithDataSourceCount(),
                 actual.getRecordsWithDataSourceCount(),
                 "Records with data source count not as expected: "
                 + testInfo);

    // validate the analysis by data source fields
    List<SzDataSourceRecordAnalysis> actualSourceList
        = actual.getAnalysisByDataSource();

    List<SzDataSourceRecordAnalysis> expectedSourceList
        = expected.getAnalysisByDataSource();

    int actualCount   = actualSourceList.size();
    int expectedCount = expectedSourceList.size();

    assertEquals(expectedCount, actualCount,
                 "The number of items in the analysis-by-data-source "
                  + "list is not as expected: " + testInfo);

    for (int index = 0; index < actualCount; index++) {
      SzDataSourceRecordAnalysis actualSourceAnalysis
          = actualSourceList.get(index);
      SzDataSourceRecordAnalysis expectedSourceAnalysis
          = expectedSourceList.get(index);

      String expectedSource = expectedSourceAnalysis.getDataSource();
      String actualSource   = actualSourceAnalysis.getDataSource();

      assertEquals(expectedSource, actualSource,
                   "The data sources do not match: " + testInfo);

      assertEquals(expectedSourceAnalysis.getRecordCount(),
                   actualSourceAnalysis.getRecordCount(),
                   "The record counts for the " + actualSource
                       + " data source do not match: " + testInfo);

      assertEquals(expectedSourceAnalysis.getRecordsWithRecordIdCount(),
                   actualSourceAnalysis.getRecordsWithRecordIdCount(),
                   "The records with record ID counts for the "
                       + actualSource + " data source do not match: "
                       + testInfo);
    }
  }

  /**
   *
   */
  private void validateLoadResponse(String                     testInfo,
                                    SzBulkLoadResponse         response,
                                    SzHttpMethod               httpMethod,
                                    String                     selfLink,
                                    SzBulkDataStatus           expectedStatus,
                                    MediaType                  mediaType,
                                    File                       bulkDataFile,
                                    SzBulkDataAnalysis         analysis,
                                    Integer                    totalRecordCount,
                                    Map<String, String>        dataSourceMap,
                                    Map<String, Integer>       failuresBySource,
                                    long                       maxDuration)
  {
    validateBasics(testInfo,
                   response,
                   200,
                   httpMethod,
                   selfLink,
                   maxDuration,
                   this.getServerConcurrency());

    final Integer ZERO = 0;

    SzBulkLoadResult actual = response.getData();

    assertNotNull(actual, "Response result is null: " + testInfo);

    if (analysis != null) {
      assertEquals(analysis.getCharacterEncoding(), actual.getCharacterEncoding(),
                   "Character encoding in result not as expected: " + testInfo);

      assertEquals(analysis.getMediaType(), actual.getMediaType(),
                   "Media type in result not as expected: " + testInfo);

      assertEquals(analysis.getRecordCount(), totalRecordCount,
                   "Unexpected number of total records: " + testInfo);
    }

    if (expectedStatus != null) {
      assertEquals(
          expectedStatus, actual.getStatus(),
          "Unexpected status for bulk load result: " + testInfo
                  + "\nRESPONSE:\n" + toJsonString(response));
    }

    // determine how many failures are expected
    int expectedFailures = 0;
    if (failuresBySource != null) {
      for (Integer count : failuresBySource.values()) {
        expectedFailures += count;
      }
    }

    int concurrency         = this.getServerConcurrency();
    int minExpectedFailures = expectedFailures;
    int maxExpectedFailures = totalRecordCount <= 1000
        ? expectedFailures : (expectedFailures + concurrency - 1);

    if (maxExpectedFailures < actual.getFailedRecordCount()) {
      if (analysis != null) {
        System.out.println("----------------------------------");
        System.out.println("RECORD COUNT: " + analysis.getRecordCount());
      }
      for (SzBulkLoadError error : actual.getTopErrors()) {
        System.out.println(error);
        System.out.println();
      }
    }

    if (minExpectedFailures == maxExpectedFailures) {
      assertEquals(expectedFailures, actual.getFailedRecordCount(),
                   "Unexpected number of failed records: " + testInfo);
    } else {
      assertTrue(
          minExpectedFailures <= actual.getFailedRecordCount(),
          "Actual number of failures (" + actual.getFailedRecordCount()
              + ") is fewer than expected (" + minExpectedFailures + "): "
              + testInfo);
      assertTrue(
          maxExpectedFailures >= actual.getFailedRecordCount(),
          "Actual number of failures (" + actual.getFailedRecordCount()
              + ") is more than expected (" + maxExpectedFailures + "): "
              + testInfo);
    }

    // check if nothing more to validate
    if (analysis == null) return;

    // determine how many records are missing only data source
    int missingDataSourceCount = analysis.getRecordCount()
        - analysis.getRecordsWithDataSourceCount();

    // check if those missing data source are mapped
    if (dataSourceMap.containsKey(null)) missingDataSourceCount = 0;
    int incompleteCount = missingDataSourceCount;

    // check the total incomplete count
    assertEquals(incompleteCount, actual.getIncompleteRecordCount(),
                 "Unexpected number of incomplete records: "
                     + testInfo);

    // now determine how many records should have loaded
    int expectLoaded = totalRecordCount - expectedFailures
        - incompleteCount;

    assertEquals(expectLoaded, actual.getLoadedRecordCount(),
                 "Unexpected number of loaded records: " + testInfo);

    // determine the expected counts by data source
    Map<String, Integer> dataSourceCountMap = new LinkedHashMap<>();
    Map<String, Integer> sourceIncompleteMap = new LinkedHashMap<>();
    analysis.getAnalysisByDataSource().forEach(sourceAnalysis -> {
      String origDataSource = sourceAnalysis.getDataSource();

      String dataSource = dataSourceMap.containsKey(origDataSource)
          ? dataSourceMap.get(origDataSource) : dataSourceMap.get(null);
      if (dataSource == null) dataSource = origDataSource;

      Integer currentCount = dataSourceCountMap.containsKey(dataSource)
        ? dataSourceCountMap.get(dataSource) : ZERO;

      Integer currentIncomplete = sourceIncompleteMap.containsKey(dataSource)
          ? sourceIncompleteMap.get(dataSource) : ZERO;

      // get the total number of records and increase it accordingly
      int sourceCount = sourceAnalysis.getRecordCount() + currentCount;
      if (sourceCount > 0) {
        dataSourceCountMap.put(dataSource, sourceCount);

        // determine how many are incomplete
        int incomplete = 0;
        if (dataSource == null) {
          incomplete = sourceCount;
        }

        // update the incomplete count
        sourceIncompleteMap.put(dataSource, currentIncomplete + incomplete);
      }
    });

    assertEquals(
        dataSourceCountMap.containsKey(null) ? dataSourceCountMap.get(null) : 0,
        actual.getMissingDataSourceCount(),
        "Missing data source count not as expected: " + testInfo);

    // validate the analysis by data source fields
    List<SzDataSourceBulkLoadResult> dataSourceResults
        = actual.getResultsByDataSource();

    int actualCount   = dataSourceResults.size();
    int expectedCount = dataSourceCountMap.size();

    assertEquals(expectedCount, actualCount,
                 "The number of items in the results-by-data-source "
                     + "list is not as expected: " + testInfo);

    for (SzDataSourceBulkLoadResult sourceResults : dataSourceResults) {
      String dataSource = sourceResults.getDataSource();

      assertTrue(dataSourceCountMap.containsKey(dataSource),
                 "Data source results for unexpected data source ("
                  + dataSource + "): " + testInfo);

      int sourceTotal       = dataSourceCountMap.get(dataSource);
      int sourceFailures    = (failuresBySource != null
                               && failuresBySource.containsKey(dataSource))
                               ? failuresBySource.get(dataSource) : 0;
      int sourceIncomplete  = sourceIncompleteMap.get(dataSource);
      int sourceLoaded      = sourceTotal - sourceIncomplete - sourceFailures;

      assertEquals(sourceTotal,
                   sourceResults.getRecordCount(),
                   "The record counts for the " + dataSource
                       + " data source do not match: " + testInfo);

      assertEquals(sourceIncomplete,
                   sourceResults.getIncompleteRecordCount(),
                   "The incomplete counts for the " + dataSource
                       + " data source do not match: " + testInfo);

      assertEquals(sourceFailures,
                   sourceResults.getFailedRecordCount(),
                   "The failed counts for the " + dataSource
                       + " data source do not match: " + testInfo);

      assertEquals(sourceLoaded,
                   sourceResults.getLoadedRecordCount(),
                   "The loaded counts for the " + dataSource
                       + " data source do not match: " + testInfo);
    }
  }
}

