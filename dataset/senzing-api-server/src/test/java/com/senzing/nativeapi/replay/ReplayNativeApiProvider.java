package com.senzing.nativeapi.replay;

import com.senzing.nativeapi.NativeApiProvider;
import com.senzing.g2.engine.*;
import com.senzing.io.IOUtilities;
import com.senzing.util.AccessToken;
import com.senzing.util.JsonUtilities;

import javax.json.*;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.lang.Boolean.*;
import static com.senzing.io.IOUtilities.*;
import static com.senzing.util.ZipUtilities.*;
import static javax.json.stream.JsonGenerator.PRETTY_PRINTING;

/**
 * An instance of {@link NativeApiProvider} that can record results to and replay
 * results from a JSON file.
 */
public class ReplayNativeApiProvider implements NativeApiProvider {
  /**
   * The prefix for the system properties used by this class.
   */
  public static final String SYSTEM_PROPERTY_PREFIX
      = "com.senzing.api.test.replay";

  /**
   * The system property for forcing direct use of the native API (no replay).
   */
  public static final String DIRECT_PROPERTY
      = SYSTEM_PROPERTY_PREFIX + ".direct";

  /**
   * The system property for forcing the cache version to use.
   */
  public static final String VERSION_PROPERTY
      = SYSTEM_PROPERTY_PREFIX + ".version";

  /**
   * The system property to force recording of a new cache.
   */
  public static final String RECORD_PROPERTY
      = SYSTEM_PROPERTY_PREFIX + ".record";

  /**
   * Property indicating if stale caches should automatically be refreshed.
   */
  public static final String REFRESH_PROPERTY
      = SYSTEM_PROPERTY_PREFIX + ".refresh";

  /**
   * Property to activate verbose output from replay cache.
   */
  public static final String VERBOSE_PROPERTY
      = SYSTEM_PROPERTY_PREFIX + ".verbose";

  /**
   * Pretty printing {@link JsonWriterFactory}.
   */
  private static JsonWriterFactory PRETTY_WRITER_FACTORY
      = Json.createWriterFactory(
      Collections.singletonMap(PRETTY_PRINTING, true));

  /**
   * The number of characters after which to extenralize the parameters or
   * results.
   */
  private static final int EXTERNALIZE_THRESHOLD = 200;

  /**
   * The prefix to use for the return value keys.
   */
  private static final String RETURN_VALUE_KEY = "returnValue";

  /**
   * The prefix to use for the result parameter keys.
   */
  private static final String PARAM_RESULT_KEY_PREFIX = "parameter";

  /**
   * The package prefix for the package associated with this class.  This is
   * used to exclude stack frames from the hash when the stack frames pertain
   * to this provider.
   */
  private static final String PACKAGE_PREFIX
      = ReplayNativeApiProvider.class.getPackage().getName() + ".";

  /**
   * The current working directory.
   */
  private static final File CURRENT_DIR
      = new File(System.getProperty("user.dir"));

  /**
   * The target directory of the maven build.
   */
  private static final File TARGET_DIR;
  static {
    String propValue = System.getProperty("project.build.directory");
    File file = (propValue == null) ? null : new File(propValue);
    TARGET_DIR = (file != null && file.exists()) ? file
        : new File(CURRENT_DIR, "target");
  }

  /**
   * The source directory of the maven build..
   */
  private static final File SOURCE_DIR = new File(CURRENT_DIR, "src");

  /**
   * The main source directory of the maven build.
   */
  private static final File MAIN_SOURCE_DIR = new File(SOURCE_DIR, "main");

  /**
   * The test source directory of the maven build.
   */
  private static final File TEST_SOURCE_DIR = new File(SOURCE_DIR, "test");


  /**
   * The main java source directory.
   */
  private static final File MAIN_JAVA_DIR
      = new File(MAIN_SOURCE_DIR, "java");

  /**
   * The test java source directory.
   */
  private static final File TEST_JAVA_DIR
      = new File(TEST_SOURCE_DIR, "java");

  /**
   * The main java class directory.
   */
  private static final File MAIN_CLASS_DIR
      = new File(TARGET_DIR, "classes");

  /**
   * The test java class directory.
   */
  private static final File TEST_CLASS_DIR
      = new File(TARGET_DIR, "test-classes");

  /**
   * The test resource directory.
   */
  private static final File TEST_RESOURCE_DIR
      = new File(TEST_SOURCE_DIR, "resources");

  /**
   * The base directory for storing caches.  Sub-directories represent
   * specific caches for specific API versions.
   */
  private static final File BASE_CACHE_DIR
      = new File(TARGET_DIR, "replay-cache");

  /**
   * The base directory for storing caches.  Sub-directories represent
   * specific caches for specific API versions.
   */
  private static final File RESOURCE_CACHE_DIR
      = new File(TEST_RESOURCE_DIR,
                 PACKAGE_PREFIX.replaceAll(
                     "\\.",
                     File.separator.equals("\\") ? "\\\\" : File.separator)
                     + "cache");

  /**
   * The prefix to all cache directories.
   */
  private static final String CACHE_DIR_PREFIX = "cache-v";

  /**
   * The existing cache directories.
   */
  private static final Set<File> EXISTING_CACHE_DIRS;

  /**
   * Utility method for getting a system property value.
   */
  private static String getPropertyValue(String prop) {
    String propValue = System.getProperty(prop);
    if (propValue != null) {
      propValue = propValue.trim();
      if (propValue.length() == 0) propValue = null;
    }
    return propValue;
  }

  /**
   * Determine the existing cache directories.
   */
  static {
    Set<File> dirSet = new LinkedHashSet<>();
    try {
      if (RESOURCE_CACHE_DIR.exists()) {
        File[] dirs = RESOURCE_CACHE_DIR.listFiles((File f) -> {
          return f.isDirectory() && f.getName().matches(
              CACHE_DIR_PREFIX + "([\\d]+\\.?)+");
        });

        Arrays.sort(dirs, (f1, f2) -> {
          String    n1  = f1.getName();
          String    n2  = f2.getName();
          String    v1  = n1.substring(CACHE_DIR_PREFIX.length());
          String    v2  = n2.substring(CACHE_DIR_PREFIX.length());
          String[]  a1  = v1.split("\\.");
          String[]  a2  = v2.split("\\.");
          int       l1  = a1.length;
          int       l2  = a2.length;
          int       min = Math.min(l1, l2);
          for (int index = 0; index < min; index++) {
            int p1 = Integer.parseInt(a1[index]);
            int p2 = Integer.parseInt(a2[index]);
            if (p1 != p2) return (p2 - p1);
          }
          return l2 - l1;
        });

        for (File dir: dirs) {
          dirSet.add(dir);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);
    } finally {
      EXISTING_CACHE_DIRS = Collections.unmodifiableSet(dirSet);
    }
  }

  /**
   * The {@link Map} of native API interfaces to their implementation classes.
   */
  private static final Map<Class, String> API_IMPLEMENTATIONS;

  static {
    Map<Class, String> map = new LinkedHashMap<>();
    try {
      map.put(G2Engine.class, "com.senzing.g2.engine.G2JNI");
      map.put(G2Config.class, "com.senzing.g2.engine.G2ConfigJNI");
      map.put(G2ConfigMgr.class, "com.senzing.g2.engine.G2ConfigMgrJNI");
      map.put(G2Product.class, "com.senzing.g2.engine.G2ProductJNI");
      map.put(G2Diagnostic.class, "com.senzing.g2.engine.G2DiagnosticJNI");
    } finally {
      API_IMPLEMENTATIONS = Collections.unmodifiableMap(map);
    }
  }

  /**
   * The prefix for all Senzing packages.  This is used to only include stack
   * frames that belong to Senzing classes in the hash for method lookup.
   */
  private static final String SENZING_PACKAGE_PREFIX = "com.senzing.";

  /**
   * Separator run of bytes.
   */
  private static final byte[] ZEROES = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

  /**
   * The native G2 version.
   */
  private static final String G2_NATIVE_VERSION;

  static {
    String nativeVersion = null;

    try {
      G2Product productApi = new G2ProductJNI();
      String versionJson = productApi.version();

      JsonObject versionObj = JsonUtilities.parseJsonObject(versionJson);
      nativeVersion = versionObj.getString("VERSION");

    } catch (UnsatisfiedLinkError e) {
      // if Senzing is not installed then the UnsatisfiedLinkError will
      // complain about libG2 not being in "java.library.path", otherwise
      // the problem is libG2 trying to load a dependency, which is an error    
      boolean installed = (!e.getMessage().contains("java.library.path"));

      // check if the build requested "direct" API usage or for the test
      // process to "record" the native API results.  this requires a
      // working Senzing installation
      boolean direct = TRUE.toString().equalsIgnoreCase(
          getPropertyValue(DIRECT_PROPERTY));

      boolean record = TRUE.toString().equalsIgnoreCase(
          getPropertyValue(RECORD_PROPERTY));

      // if Senzing is installed or "direct" API usage is required or
      // recording is required or we have no cached API test directories
      // to use for a "replay" test run THEN we have to fail with an error
      if (installed || direct || record || EXISTING_CACHE_DIRS.isEmpty()) {
        e.printStackTrace();
        throw new ExceptionInInitializerError(
            "Failed to find valid Senzing installation for integration test "
            + "run.");
      }

    } catch (Error e) {
      e.printStackTrace();
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      String forceVersion = getPropertyValue(VERSION_PROPERTY);
      G2_NATIVE_VERSION = (forceVersion != null) ? forceVersion: nativeVersion;
    }
  }

  /**
   * This is a {@link Map} of classes to methods for which the parameters to
   * the methods are irrlevant for playback purposes.
   */
  private static final Map<Class, Set<Method>> IRRELEVANT_METHODS_MAP;

  static {
    Map<Class, Set<Method>> map = new LinkedHashMap<>();
    Class[] classes = {
        G2Engine.class,
        G2Product.class,
        G2Config.class,
        G2ConfigMgr.class,
        G2Diagnostic.class
    };

    try {
      for (Class c : classes) {
        Set<Method> set = map.get(c);
        if (set == null) {
          set = new LinkedHashSet<>();
          map.put(c, set);
        }

        // handle the methods that are the same across interfaces
        for (Method method : c.getMethods()) {
          String name = method.getName();
          switch (name) {
            case "init":
            case "initWithConfigID":
              set.add(method);
              break;
            default:
              // do nothing
          }
        }
      }

      // handle G2Config.load()
      Set<Method> set = map.get(G2Config.class);
      Method loadMethod = G2Config.class.getMethod(
          "load", String.class, Result.class);
      set.add(loadMethod);

      // handle G2ConfigMgr.addConfig()
      set = map.get(G2ConfigMgr.class);
      Method addConfigMethod = G2ConfigMgr.class.getMethod(
          "addConfig", String.class, String.class, Result.class);
      set.add(addConfigMethod);

      // handle G2Engine.addRecord() and similar methods
      set = map.get(G2Engine.class);
      for (Method method: G2Engine.class.getMethods()) {
        switch (method.getName()) {
          case "addRecord":
          case "replaceRecord":
          case "replaceRecordWithInfo":
          case "addRecordWithReturnedRecordID":
          case "addRecordWithInfo":
          case "deleteRecord":
          case "deleteRecordWithInfo":
          case "reevaluateRecord":
          case "reevaluateRecordWithInfo":
            set.add(method);
            break;
          default:
            // do nothing
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      IRRELEVANT_METHODS_MAP = Collections.unmodifiableMap(map);
    }
  }

  /**
   * The current test class.
   */
  private Class currentTestClass = null;

  /**
   * The access token for ending the current test class after beginning it.
   */
  private AccessToken accessToken = null;

  /**
   * Indicates if this instance is falling back to direct access with no
   * recording and no replaying.
   */
  private boolean direct = true;

  /**
   * Indicates of this instance is currently recording.
   */
  private boolean recording = false;

  /**
   * The cache dir where the values are cached.
   */
  private File cacheDir;

  /**
   * The extracted cache directory to use for recording or for replaying.
   * The items from an existing cache in {@link #cacheDir} are extracted here.
   */
  private File workingCacheDir;

  /**
   * The set of dependencies for the current test.
   */
  private Set<String> currentDependencies = null;
  /**
   * The map of {@link String} invocation hashes to {@link InvocationCache}
   * instances.
   */
  private Map<String, InvocationCache> currentCache = null;

  /**
   * Flag indicating if the cache dependencies have changed and the cache is
   * stale.
   */
  private boolean cacheStale = false;

  /**
   * Flag indicating if we are forcing recording of cache.
   */
  private boolean forceRecord;

  /**
   * Default constructor.
   */
  public ReplayNativeApiProvider() {
    this.cacheDir         = null;
    this.workingCacheDir  = null;

    this.direct = TRUE.toString().equalsIgnoreCase(
        getPropertyValue(DIRECT_PROPERTY));

    // check if running inside a maven build
    if (TARGET_DIR.exists() && MAIN_JAVA_DIR.exists() && TEST_JAVA_DIR.exists()
        && MAIN_CLASS_DIR.exists() && TEST_CLASS_DIR.exists() && !direct)
    {
      if (G2_NATIVE_VERSION != null) {
        // get the cache directory based on the G2 native version
        this.cacheDir = new File(RESOURCE_CACHE_DIR,
                                 CACHE_DIR_PREFIX + G2_NATIVE_VERSION);

        // check if forcing creation of the cache
        this.forceRecord = TRUE.toString().equalsIgnoreCase(
            getPropertyValue(RECORD_PROPERTY));

        // check if running all tests or specific tests
        boolean runningAllTests = System.getProperty("test") == null;

        // if the cache exists and we are forcing creation, then delete the
        // cache file
        if (this.forceRecord && runningAllTests && this.cacheDir.exists()) {
          try {
            IOUtilities.recursiveDeleteDirectory(this.cacheDir);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        // if the cache directory does not exist then create it
        if (!this.cacheDir.exists()) {
          this.cacheDir.mkdirs();
        }

        // setup the working cache dir for this run
        this.workingCacheDir
            = new File(BASE_CACHE_DIR,CACHE_DIR_PREFIX + G2_NATIVE_VERSION);

      } else {
        // use the latest cache dir
        this.cacheDir = EXISTING_CACHE_DIRS.iterator().next();

        // get the version number
        String ver = cacheDir.getName().substring(CACHE_DIR_PREFIX.length());

        // create the working cache dir
        this.workingCacheDir = new File(BASE_CACHE_DIR,
                                        CACHE_DIR_PREFIX + ver);
      }

    } else {
      // we can only operate in direct mode if not running in a maven build
      // or if explicitly told to do so via system property
      this.direct = true;
    }

    // setup the working cache dir
    if (this.workingCacheDir != null) {
      if (this.workingCacheDir.exists()) {
        try {
          IOUtilities.recursiveDeleteDirectory(this.workingCacheDir);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      this.workingCacheDir.mkdirs();
    }
  }

  @Override
  public G2Engine createEngineApi() {
    if (this.direct) return new G2JNI();
    return this.createProxy(G2Engine.class);
  }

  @Override
  public G2Config createConfigApi() {
    if (this.direct) return new G2ConfigJNI();
    return this.createProxy(G2Config.class);
  }

  @Override
  public G2Product createProductApi() {
    if (this.direct) return new G2ProductJNI();
    return this.createProxy(G2Product.class);
  }

  @Override
  public G2ConfigMgr createConfigMgrApi() {
    if (this.direct) return new G2ConfigMgrJNI();
    return this.createProxy(G2ConfigMgr.class);
  }

  @Override
  public G2Diagnostic createDiagnosticApi() {
    if (this.direct) return new G2DiagnosticJNI();
    return this.createProxy(G2Diagnostic.class);
  }

  /**
   * Create a new proxy instance for the specified interface class.
   *
   * @param interfaceClass The interface class.
   * @return The proxy instance that implements the specified class.
   */
  private <T> T createProxy(Class<T> interfaceClass) {
    InvocationHandler handler = new ReplayInvocationHandler(interfaceClass);
    Class[] interfaces = { interfaceClass };
    ClassLoader classLoader = this.getClass().getClassLoader();
    return (T) Proxy.newProxyInstance(classLoader, interfaces, handler);
  }

  /**
   * Sets the current test class.
   */
  public synchronized AccessToken beginTests(Class testClass) {
    // check if already set and set to the same
    if (this.currentTestClass != null && this.currentTestClass != testClass) {
      throw new IllegalStateException(
          "Currently running tests for a different class: "
              + this.currentTestClass);
    }

    // checks if already set to the same class
    if (this.currentTestClass != null) {
      return this.accessToken;
    }

    // set the test class and the access token
    this.currentTestClass = testClass;
    this.accessToken      = new AccessToken();

    // parse the cache file
    if (!this.direct) {
      try {
        this.recording = this.loadCacheFile();
        this.recording = this.loadCacheFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      // check if recording
      if (this.recording) {
        System.out.println();
        System.out.println("RECORDING CACHE: " + this.getTestCacheZip());
        System.out.println();
        // if recording, setup empty caches
        this.currentDependencies  = new LinkedHashSet<>();
        this.currentCache         = new LinkedHashMap<>();
      } else {
        System.out.println();
        System.out.println("USING CACHE: " + this.getTestCacheZip());
        System.out.println();
      }
    }

    // return the access token
    return this.accessToken;
  }

  /**
   * Checks if the provider is in replay mode for the current tests.
   *
   * @return <tt>true</tt> if replaying previous results, otherwise
   *         <tt>false</tt>.
   */
  public synchronized boolean isReplaying() {
    return (!this.direct && !this.recording);
  }

  /**
   * Checks if the cache being used for replay may be stale -- meaning that the
   * dependencies have changed since the cache was created.
   *
   * @return <tt>true</tt> if the cache may be stale, otherwise <tt>false</tt>.
   */
  public synchronized boolean isCacheStale() {
    return this.cacheStale;
  }

  /**
   * Ends the current test class run using the specified {@link AccessToken}
   * to authorize the operation.
   *
   * @param token The {@link AccessToken} to authorize the operation.
   */
  public synchronized void endTests(AccessToken token) {
    Objects.requireNonNull(token, "The token cannot be null");
    if (this.accessToken == null) return;
    if (!this.accessToken.equals(token)) {
      throw new IllegalArgumentException(
          "Invalid access token to authorize operation.");
    }

    // save the cache if recording
    if (this.recording) {
      try {
        // save the cache file
        this.saveCacheFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // clear the dependencies and cache out
    this.currentDependencies  = null;
    this.currentCache         = null;

    // clear the fields
    this.accessToken      = null;
    this.currentTestClass = null;
  }

  /**
   *
   */
  protected static class RecordedResult {
    private int repeatCount = 1;
    private Object value = null;
    private RecordedResult(Object value) {
      this(value, 1);
    }
    private RecordedResult(Object value, int repeatCount) {
      this.value = value;
      this.repeatCount = repeatCount;
    }
    private boolean remaining() {
      return (this.repeatCount > 0);
    }
    private void increment() {
      this.repeatCount++;
    }
    private void decrement() {
      this.repeatCount--;
    }
  }

  /**
   * The describes the cache associated with a specific invocation of a method
   * with parameter values.
   */
  protected static class InvocationCache {
    private String                apiClass;
    private String                apiMethod;
    private List<String>          parameterTypes;
    private String                parametersHash;
    private List                  parameters;
    private List<RecordedResult>  results;
    private RecordedResult        lastResult = null;
    private Set<Long>             threadIdSet = new LinkedHashSet<>();
  }

  /**
   * Reads a {@link JsonArray} as a list of objects.
   *
   * @param jsonArray The {@link JsonArray} to read.
   *
   * @return The {@link List} of objects read.
   */
  private List readArray(JsonArray jsonArray) {
    List result = new LinkedList<>();
    for (int index = 0; index < jsonArray.size(); index++) {
      JsonValue           jsonValue = jsonArray.get(index);
      JsonValue.ValueType valueType = jsonValue.getValueType();
      switch (valueType) {
        case FALSE:
          result.add(Boolean.FALSE);
          break;
        case TRUE:
          result.add(Boolean.TRUE);
          break;
        case NULL:
          result.add(null);
          break;
        case STRING:
          result.add(jsonArray.getString(index));
          break;
        case NUMBER:
          result.add(jsonArray.getJsonNumber(index).numberValue());
          break;
        case OBJECT:
          result.add(JsonUtilities.toJsonText(jsonArray.getJsonObject(index)));
          break;
        case ARRAY:
          result.add(JsonUtilities.toJsonText(jsonArray.getJsonArray(index)));
          break;
        default:
          throw new IllegalStateException("Unhandled value type: " + valueType);
      }
    }
    return result;
  }

  /**
   * Reads a {@link JsonObject} as a map of {@link String} keys and
   * object values.
   *
   * @param jsonObject The {@link JsonObject} to read.
   *
   * @return The {@link List} of objects read.
   */
  private Map<String,Object> readResults(JsonObject jsonObject) {
    Map<String,Object>  result = new LinkedHashMap<>();
    Set<String>         keySet = new LinkedHashSet<>();
    keySet.add(RETURN_VALUE_KEY);
    for (String key : jsonObject.keySet()) {
      if (key.startsWith(PARAM_RESULT_KEY_PREFIX)) {
        keySet.add(key);
      }
    }

    keySet.forEach(key -> {
      int                 index     = key.indexOf(":");
      String              javaType  = (index < 0) ? null : key.substring(index);
      JsonValue           value     = jsonObject.get(key);
      JsonValue.ValueType valueType = value.getValueType();
      switch (valueType) {
        case FALSE:
          result.put(key, Boolean.FALSE);
          break;
        case TRUE:
          result.put(key, Boolean.TRUE);
          break;
        case NULL:
          result.put(key, null);
          break;
        case STRING:
          result.put(key, jsonObject.getString(key));
          break;
        case NUMBER:
          result.put(key, JsonUtilities.getLong(jsonObject, key));
          break;
        case OBJECT:
          result.put(key, JsonUtilities.toJsonText(jsonObject.getJsonObject(key)));
          break;
        case ARRAY:
          result.put(key, JsonUtilities.toJsonText(jsonObject.getJsonArray(key)));
          break;
        default:
          throw new IllegalStateException("Unhandled value type: " + valueType);
      }
    });

    return result;
  }

  /**
   * Loads the cache file if it exists.
   *
   * @return <tt>true</tt> if we are recording the results and <tt>false</tt>
   *         if we are replaying the results.
   */
  private synchronized boolean loadCacheFile() throws IOException {
    // clear the cache stale flag
    this.cacheStale = false;

    // check if there is a cache to extract
    File cacheZip = this.getTestCacheZip();
    File cacheDir = this.getTestCacheDir();

    // check if forcing re-recording
    if (this.forceRecord) {
      if (cacheZip.exists()) cacheZip.delete();

      if (cacheDir.exists()) {
        recursiveDeleteDirectory(cacheDir);
      }
    }

    // if there is no cache, try unzipping the existing cache
    if (cacheZip.exists() && !cacheDir.exists()) {
      unzip(cacheZip, this.workingCacheDir);
    }

    // check for the cache file (post extraction)
    File cacheFile = this.getTestCacheFile();
    if (!cacheFile.exists()) {
      // if the cache file does not exist, but the directory does, then clean up
      if (cacheDir.exists()) {
        recursiveDeleteDirectory(cacheDir);
      }
      cacheDir.mkdirs();
      return true;
    }

    try {
      String      jsonText    = readTextFileAsString(cacheFile, UTF_8);
      JsonObject  jsonObject  = JsonUtilities.parseJsonObject(jsonText);

      // load and check the dependencies and check for changes
      this.currentDependencies = new LinkedHashSet<>();
      JsonArray dependencies = jsonObject.getJsonArray("dependencies");
      for (JsonString dependency : dependencies.getValuesAs(JsonString.class)) {
        this.currentDependencies.add(dependency.getString());
      }
      String computedDependencyHash = this.hashDependencies();
      String cachedDependencyHash   = jsonObject.getString("dependencyHash");
      this.cacheStale = (!computedDependencyHash.equals(cachedDependencyHash));

      // check if refreshing stale caches
      boolean refresh = TRUE.toString().equalsIgnoreCase(
          getPropertyValue(REFRESH_PROPERTY));

      // check if always reporting stale
      boolean verbose = TRUE.toString().equalsIgnoreCase(
          getPropertyValue(VERBOSE_PROPERTY));

      if (this.cacheStale && verbose) {
        System.out.println("***** CACHE IS STALE: " + cacheZip);
      }
      if (this.cacheStale && refresh) {
        this.cacheStale = false;
        if (cacheDir.exists()) {
          recursiveDeleteDirectory(cacheDir);
        }
        cacheDir.mkdirs();
        return true;
      }

      // create the cache map
      this.currentCache = new LinkedHashMap<>();

      // find all the cached invocations
      JsonObject invocations = jsonObject.getJsonObject("invocations");

      // iterate over the cached invocations and populate the cache
      invocations.entrySet().forEach(entry -> {
        String          hashKey       = entry.getKey();
        JsonObject      cacheObj      = entry.getValue().asJsonObject();
        InvocationCache cache         = new InvocationCache();
        JsonValue       paramsValue   = cacheObj.get("parameters");
        JsonArray       resultValues  = cacheObj.getJsonArray("results");
        cache.apiClass                = cacheObj.getString("apiClass");
        cache.apiMethod               = cacheObj.getString("apiMethod");
        if (paramsValue.getValueType() == JsonValue.ValueType.STRING) {
          cache.parametersHash  = cacheObj.getString("parameters");
          cache.parameters      = null;
        } else {
          JsonArray paramsArray = cacheObj.getJsonArray("parameters");
          cache.parametersHash  = null;
          cache.parameters      = this.readArray(paramsArray);
        }
        cache.results = new LinkedList<>();
        for (JsonObject result : resultValues.getValuesAs(JsonObject.class)) {
          int repeatCount = JsonUtilities.getInteger(result, "repeatCount", 1);
          JsonValue value = result.get("value");
          RecordedResult recordedResult = null;
          if (value.getValueType() == JsonValue.ValueType.STRING) {
            recordedResult = new RecordedResult(
                ((JsonString) value).getString(), repeatCount);
            cache.results.add(recordedResult);
          } else {
            recordedResult = new RecordedResult(
                this.readResults((JsonObject) value), repeatCount);
            cache.results.add(recordedResult);
          }
          cache.lastResult = null;
        }
        this.currentCache.put(hashKey, cache);
      });

      return false;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Saves the current cache to the file.
   */
  private synchronized void saveCacheFile() throws IOException {
    // get the cache directory
    File cacheDir = this.getTestCacheDir();
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }

    File cacheFile = this.getTestCacheFile();

    try (OutputStream os = new FileOutputStream(cacheFile);
         OutputStreamWriter osw = new OutputStreamWriter(os, UTF_8);
         JsonWriter jsonWriter = PRETTY_WRITER_FACTORY.createWriter(osw))
    {
      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("testClass", this.currentTestClass.getName());
      job.add("nativeApiVersion", G2_NATIVE_VERSION);

      JsonArrayBuilder jab = Json.createArrayBuilder();
      for (String dependency : this.currentDependencies) {
        jab.add(dependency);
      }
      job.add("dependencies", jab);
      String dependencyHash = this.hashDependencies();

      job.add("dependencyHash", dependencyHash);

      JsonObjectBuilder invocationsJob = Json.createObjectBuilder();

      this.currentCache.entrySet().forEach(entry -> {
        String hashKey = entry.getKey();
        InvocationCache cache = entry.getValue();
        JsonObjectBuilder cacheJob = Json.createObjectBuilder();
        cacheJob.add("apiClass", cache.apiClass);
        cacheJob.add("apiMethod", cache.apiMethod);
        JsonArrayBuilder paramTypeJab = Json.createArrayBuilder();
        for (String paramType : cache.parameterTypes) {
          paramTypeJab.add(paramType);
        }
        cacheJob.add("parameterTypes", paramTypeJab);
        if (cache.parametersHash != null) {
          cacheJob.add("parameters", cache.parametersHash);
        } else {
          JsonArrayBuilder paramsJab = Json.createArrayBuilder();
          cache.parameters.forEach(param -> {
            this.arrayAdd(paramsJab, param);
          });
          cacheJob.add("parameters", paramsJab);
        }
        JsonArrayBuilder resultJab = Json.createArrayBuilder();
        for (RecordedResult recordedResult: cache.results) {
          JsonObjectBuilder recordedJob = Json.createObjectBuilder();
          recordedJob.add("repeatCount", recordedResult.repeatCount);
          Object resultItem = recordedResult.value;
          if (resultItem instanceof String) {
            recordedJob.add("value", resultItem.toString());
          } else {
            JsonObjectBuilder resultJob = Json.createObjectBuilder();
            Map<String, Object> resultMap = (Map<String,Object>) resultItem;
            resultMap.entrySet().forEach(resultEntry -> {
              String key    = resultEntry.getKey();
              Object value  = resultEntry.getValue();
              this.objectAdd(resultJob, key, value);
            });
            recordedJob.add("value", resultJob);
          }
          resultJab.add(recordedJob);
        }
        cacheJob.add("results", resultJab);

        invocationsJob.add(hashKey, cacheJob);
      });

      job.add("invocations", invocationsJob);

      jsonWriter.writeObject(job.build());
    }

    // delete any existing zip cache
    File cacheZip = this.getTestCacheZip();
    if (cacheZip.exists()) {
      cacheZip.delete();
    }

    // zip the cache for this test
    zip(this.getTestCacheDir(), cacheZip);
  }

  /**
   * Creates the results file name for the results hash.
   * @param resultHash The hash for the results.
   * @return The file name for the results.
   */
  private static final String resultsFileName(String resultHash) {
    resultHash = resultHash.replace('/', '_');
    return "results-" + resultHash + ".json";
  }

  /**
   * Creates the parameters file name for the parameters hash.
   * @param paramsHash The hash for the parameters.
   * @return The file name for the parameters.
   */
  private static final String paramsFileName(String paramsHash) {
    paramsHash = paramsHash.replace('/', '_');
    return "params-" + paramsHash + ".json";
  }

  /**
   * Saves the parameters in the specified array to a parameters file.
   *
   * @param parameters The parameters to be saved.
   *
   * @return The hash for the saved parameters.
   */
  private synchronized String saveParameters(List parameters) {
    JsonArrayBuilder paramJab = Json.createArrayBuilder();
    parameters.forEach(param -> {
      this.arrayAdd(paramJab, param);
    });
    String jsonText = JsonUtilities.toJsonText(paramJab, true);
    if (jsonText.length() < EXTERNALIZE_THRESHOLD) return null;
    String paramsHash = null;
    try {
      byte[] jsonBytes = jsonText.getBytes(UTF_8);
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.update(jsonBytes);
      paramsHash = Base64.getEncoder().encodeToString(md5.digest());
    } catch (NoSuchAlgorithmException|UnsupportedEncodingException cantHappen) {
      throw new RuntimeException(cantHappen);
    }
    File dir = this.getTestCacheDir();
    if (!dir.exists()) dir.mkdirs();

    String  name        = paramsFileName(paramsHash);
    File    paramsFile  = new File(dir, name);

    // check if the result file already exists
    if (paramsFile.exists()) return paramsHash;

    // create the results file
    try (FileOutputStream   fos = new FileOutputStream(paramsFile);
         OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8))
    {
      osw.write(jsonText);
      osw.flush();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // return the result hash
    return paramsHash;
  }

  /**
   * Saves the results in the specified result map to a result file.
   *
   * @param resultMap The result map to be saved.
   *
   * @return The hash for the saved results.
   */
  private synchronized String saveResults(Map<String, Object> resultMap) {
    JsonObjectBuilder resultJob = Json.createObjectBuilder();
    resultMap.entrySet().forEach(resultEntry -> {
      String key = resultEntry.getKey();
      Object value = resultEntry.getValue();
      this.objectAdd(resultJob, key, value);
    });
    String jsonText = JsonUtilities.toJsonText(resultJob.build(), true);
    if (jsonText.length() < EXTERNALIZE_THRESHOLD) return null;
    String resultHash = null;
    try {
      byte[] jsonBytes = jsonText.getBytes(UTF_8);
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.update(jsonBytes);
      resultHash = Base64.getEncoder().encodeToString(md5.digest());
    } catch (NoSuchAlgorithmException|UnsupportedEncodingException cantHappen) {
      throw new RuntimeException(cantHappen);
    }
    File dir = this.getTestCacheDir();
    if (!dir.exists()) dir.mkdirs();

    String  name        = resultsFileName(resultHash);
    File    resultFile  = new File(dir, name);

    // check if the result file already exists
    if (resultFile.exists()) return resultHash;

    // create the results file
    try (FileOutputStream   fos = new FileOutputStream(resultFile);
         OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8))
    {
      osw.write(jsonText);
      osw.flush();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // return the result hash
    return resultHash;
  }

  /**
   * Loads the results in the specified result hash and returns the result
   * map describing the results.
   *
   * @param resultHash The hash for the saved results.
   *
   * @return The result map that was loaded.
   */
  private synchronized Map<String, Object> loadResults(String resultHash) {
    File    dir         = this.getTestCacheDir();
    String  name        = resultsFileName(resultHash);
    File    resultFile  = new File(dir, name);

    try {
      String jsonText = IOUtilities.readTextFileAsString(resultFile, UTF_8);
      JsonObject resultsObj = JsonUtilities.parseJsonObject(jsonText);
      return this.readResults(resultsObj);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Adds the specified value to the {@link JsonArrayBuilder}.
   *
   * @param jab The {@link JsonArrayBuilder} to add the value to.
   * @param value The value to be added.
   */
  private static void arrayAdd(JsonArrayBuilder jab, Object value) {
    if (value == null) {
      jab.addNull();
      return;
    }

    switch (value.getClass().getName()) {
      case "java.lang.Boolean":
      case "boolean":
        jab.add((Boolean) value);
        break;
      case "java.lang.Integer":
      case "int":
        jab.add((Integer) value);
        break;
      case "java.lang.Long":
      case "long":
        jab.add((Long) value);
        break;
      case "java.lang.Short":
      case "short":
        jab.add((Short) value);
        break;
      case "java.lang.Byte":
      case "byte":
        jab.add((Byte) value);
        break;
      case "java.lang.Double":
      case "double":
        jab.add((Double) value);
        break;
      case "java.lang.Float":
      case "float":
        jab.add((Float) value);
        break;
      case "java.math.BigInteger":
        jab.add((BigInteger) value);
        break;
      case "java.math.BigDecimal":
        jab.add((BigDecimal) value);
        break;
      case "java.lang.String":
        String text = (String) value;
        text = text.trim();
        boolean added = false;
        if (text.startsWith("{") && text.endsWith("}")) {
          try {
            JsonObject jsonObject = JsonUtilities.parseJsonObject(text);
            jab.add(Json.createObjectBuilder(jsonObject));
            added = true;
          } catch (Exception ignore) {
            // ignore the exception -- not a JSON object
          }
        } else if (text.startsWith("[") && text.endsWith("]")) {
          try {
            JsonArray jsonArray = JsonUtilities.parseJsonArray(text);
            jab.add(Json.createArrayBuilder(jsonArray));
            added = true;
          } catch (Exception ignore) {
            // ignore the exception -- not a JSON array
          }
        }
        if (!added) {
          jab.add((String) value);
          added = true;
        }
        break;
      default:
        jab.add(value.getClass().getName());
    }
  }

  /**
   * Adds the specified value to the {@link JsonObjectBuilder}.
   *
   * @param job The {@link JsonObjectBuilder} to add the value to.
   * @param key The string key for the value to be added.
   * @param value The value to be added.
   */
  private static void objectAdd(JsonObjectBuilder job,
                                String            key,
                                Object            value) {
    if (value == null) {
      job.addNull(key);
      return;
    }

    switch (value.getClass().getName()) {
      case "java.lang.Boolean":
      case "boolean":
        job.add(key, (Boolean) value);
        break;
      case "java.lang.Integer":
      case "int":
        job.add(key, (Integer) value);
        break;
      case "java.lang.Long":
      case "long":
        job.add(key, (Long) value);
        break;
      case "java.lang.Short":
      case "short":
        job.add(key, (Short) value);
        break;
      case "java.lang.Byte":
      case "byte":
        job.add(key, (Byte) value);
        break;
      case "java.lang.Double":
      case "double":
        job.add(key, (Double) value);
        break;
      case "java.lang.Float":
      case "float":
        job.add(key, (Float) value);
        break;
      case "java.math.BigInteger":
        job.add(key, (BigInteger) value);
        break;
      case "java.math.BigDecimal":
        job.add(key, (BigDecimal) value);
        break;
      case "java.lang.String":
        String text = (String) value;
        text = text.trim();
        boolean added = false;
        if (text.startsWith("{") && text.endsWith("}")) {
          try {
            JsonObject jsonObject = JsonUtilities.parseJsonObject(text);
            job.add(key, Json.createObjectBuilder(jsonObject));
            added = true;
          } catch (Exception ignore) {
            // ignore the exception -- not a JSON object
          }
        } else if (text.startsWith("[") && text.endsWith("]")) {
          try {
            JsonArray jsonArray = JsonUtilities.parseJsonArray(text);
            job.add(key, Json.createArrayBuilder(jsonArray));
            added = true;
          } catch (Exception ignore) {
            // ignore the exception -- not a JSON array
          }
        }
        if (!added) {
          job.add(key, (String) value);
          added = true;
        }
        break;

      default:
        job.add(key, value.getClass().getName());
    }
  }

  /**
   * Produces a hash key for the dependencies associated with the test.
   * @return The hash key for the dependencies.
   */
  private String hashDependencies() {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte[] bytes = new byte[8192];

      for (String dependency : this.currentDependencies) {
        dependency = dependency.replace('.', '/');
        dependency = dependency + ".class";
        File classFile = new File(MAIN_CLASS_DIR, dependency);
        if (!classFile.exists()) {
          classFile = new File(TEST_CLASS_DIR, dependency);
        }
        if (classFile.exists()) {
          try (InputStream is = new FileInputStream(classFile)) {
            // read bytes and update the hash
            for (int readCount = is.read(bytes); readCount >= 0;
                 readCount = is.read(bytes))
            {
              md5.update(bytes, 0, readCount);
            }
            md5.update(ZEROES);

          } catch (IOException e) {
            throw new IllegalStateException(e);
          }
        }
      }

      return Base64.getEncoder().encodeToString(md5.digest());

    } catch (NoSuchAlgorithmException cannotHappen) {
      throw new IllegalStateException(cannotHappen);
    }
  }

  /**
   * Produces a hash for the invocation.
   *
   * @param apiClass The API class the method is called on.
   * @param apiMethod The API method that was called.
   * @param parameters The list of parameter values.
   * @param stackTrace The stack trace for the call.
   */
  private String hashInvocation(Class                    apiClass,
                                Method                   apiMethod,
                                List                     parameters,
                                List<StackTraceElement>  stackTrace)
  {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");

      // hash the class name
      byte[] bytes = apiClass.getName().getBytes(UTF_8);
      md5.update(bytes);
      md5.update(ZEROES);

      // has the method name
      bytes = apiMethod.getName().getBytes(UTF_8);
      md5.update(bytes);
      md5.update(ZEROES);

      // hash the parameter types
      Class[] paramTypes = apiMethod.getParameterTypes();
      for (Class paramType: paramTypes) {
        md5.update(paramType.getName().getBytes(UTF_8));
        md5.update(ZEROES);
      }

      // hash the parameter values
      if (apiClass.equals(G2Engine.class)
          && apiMethod.getName().startsWith("addRecord")) {
        // special case for "addMethod" since it is called concurrently
        String jsonText = (String) parameters.get(2);
        JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonText);

        String dataSource = JsonUtilities.getString(jsonObject, "DATA_SOURCE");

        // add the data source to the hash
        if (dataSource != null) {
          md5.update(dataSource.toString().getBytes(UTF_8));
        } else {
          md5.update(ZEROES);
        }
        md5.update(ZEROES);

      } else {
        Set<Method> methods = IRRELEVANT_METHODS_MAP.get(apiClass);
        if (methods == null || !methods.contains(apiMethod)) {
          for (Object paramValue: parameters) {
            if (paramValue == null) {
              md5.update(ZEROES);
              md5.update(ZEROES);

            } else if ((paramValue instanceof Number)
                || (paramValue instanceof String)
                || (paramValue instanceof Boolean))
            {
              md5.update(paramValue.toString().getBytes(UTF_8));
              md5.update(ZEROES);

            } else if ((paramValue instanceof StringBuffer)
                || (paramValue instanceof StringBuilder)
                || (paramValue instanceof Result))
            {
              md5.update(paramValue.getClass().getName().getBytes(UTF_8));
              md5.update(ZEROES);

            } else {
              throw new IllegalStateException(
                  "Unsupported parameter value type: " + paramValue.getClass());
            }
          }
        }
      }

      int stackIndex = 0;
      for (StackTraceElement stackFrame : stackTrace) {
        String stackClass = stackFrame.getClassName();
        if (stackClass.startsWith(PACKAGE_PREFIX)) continue;
        if (!stackClass.startsWith(SENZING_PACKAGE_PREFIX)) continue;
        if (stackClass.contains("$Proxy")) stackClass = apiClass.getName();
        md5.update(((stackIndex++) + ": " + stackClass).getBytes(UTF_8));
        md5.update(ZEROES);

        String stackMethod = stackFrame.getMethodName();
        md5.update(stackMethod.getBytes(UTF_8));
        md5.update(ZEROES);
      }

      byte[] digest = md5.digest();

      return Base64.getEncoder().encodeToString(digest);

    } catch (NoSuchAlgorithmException|UnsupportedEncodingException cantHappen) {
      throw new IllegalStateException(cantHappen);
    }
  }


  /**
   * Produces the hash info for the invocation.
   *
   * @param apiClass The API class the method is called on.
   * @param apiMethod The API method that was called.
   * @param parameters The list of parameter values.
   * @param stackTrace The stack trace for the call.
   */
  private String invocationHashInfo(Class                    apiClass,
                                    Method                   apiMethod,
                                    List                     parameters,
                                    List<StackTraceElement>  stackTrace)
  {
    StringBuilder sb = new StringBuilder();

    sb.append("apiClass=[ " + apiClass.getName() + " ]");
    sb.append(", methodName=[ " + apiMethod.getName() + " ]");
    sb.append(", paramTypes=[ ");
    // hash the parameter types
    Class[] paramTypes = apiMethod.getParameterTypes();
    String prefix = "";
    for (Class paramType: paramTypes) {
      sb.append(prefix).append(paramType.getName());
      prefix = ", ";
    }
    sb.append(" ]");

    // hash the parameter values
    if (apiClass.equals(G2Engine.class)
        && apiMethod.getName().startsWith("addRecord")) {

      // special case for "addMethod" since it is called concurrently
      String jsonText = (String) parameters.get(2);
      JsonObject jsonObject = JsonUtilities.parseJsonObject(jsonText);

      String dataSource = JsonUtilities.getString(jsonObject, "DATA_SOURCE");

      sb.append(", dataSource=[ ").append(dataSource).append(" ]");

    } else {
      Set<Method> methods = IRRELEVANT_METHODS_MAP.get(apiClass);
      if (methods == null || !methods.contains(apiMethod)) {
        sb.append(", parameters=[ ");
        prefix = "";
        for (Object paramValue: parameters) {
          if ((paramValue instanceof StringBuffer)
              || (paramValue instanceof StringBuilder)
              || (paramValue instanceof Result))
          {
            sb.append(paramValue.getClass().getName());
          } else {
            sb.append(prefix).append(paramValue);
          }
          prefix = ", ";
        }
        sb.append(" ]");
      }
    }

    sb.append(", stackTrace=[ ");
    int stackIndex = 0;
    for (StackTraceElement stackFrame : stackTrace) {
      String stackClass = stackFrame.getClassName();
      if (stackClass.startsWith(PACKAGE_PREFIX)) continue;
      if (!stackClass.startsWith(SENZING_PACKAGE_PREFIX)) continue;
      if (stackClass.contains("$Proxy")) stackClass = apiClass.getName();
      sb.append("\n    " + (stackIndex++) + ": " + stackClass);
      String stackMethod = stackFrame.getMethodName();
      sb.append("." + stackMethod);
    }
    sb.append("\n]");

    return sb.toString();
  }

  /**
   * Gets the cache file for the current test class.
   */
  public File getTestCacheFile() {
    File dir = this.getTestCacheDir();
    File cacheFile = new File(dir, "cache-index.json");
    return cacheFile;
  }

  /**
   * Gets the results cache directory for the current test class.
   */
  public File getTestCacheDir() {
    String  simpleClassName = this.currentTestClass.getSimpleName();
    String  cacheFileName   = simpleClassName + "-cache";
    File    result          = new File(this.workingCacheDir, cacheFileName);
    return result;
  }

  /**
   * Gets the result cache ZIP file for the current test class.
   */
  public File getTestCacheZip() {
    String  simpleClassName = this.currentTestClass.getSimpleName();
    String  cacheFileName   = simpleClassName + "-cache.zip";
    File    result          = new File(this.cacheDir, cacheFileName);
    return result;
  }

  /**
   * An invocation handler to handle recording and playing back the
   * invocation results by method.
   *
   */
  protected class ReplayInvocationHandler<T> implements InvocationHandler {
    /**
     * The API interface that this handler is managing.
     */
    private Class apiInterface;

    /**
     * The native API instances.
     */
    private T nativeApi = null;

    /**
     * Constructs with the API interface.
     *
     * @param apiInterface The {@link Class} representing the API interface.
     */
    private ReplayInvocationHandler(Class<T> apiInterface)
    {
      this.apiInterface = apiInterface;
    }

    /**
     * Gets the native API instances.
     *
     * @return The native API instance.
     */
    private T getNativeApi() {
      synchronized (ReplayNativeApiProvider.this) {
        if (this.nativeApi == null) {
          String className = API_IMPLEMENTATIONS.get(this.apiInterface);
          try {
            Class apiClass = Class.forName(className);
            this.nativeApi = (T) apiClass.newInstance();

          } catch (RuntimeException e) {
            throw e;

          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
        return this.nativeApi;
      }
    }

    /**
     *
     */
    private String invocationToString(Method method, Object[] args) {
      StringWriter sw = new StringWriter();
      sw.append(this.apiInterface.getSimpleName());
      sw.append(".").append(method.getName()).append("(");
      String prefix = "";
      int argIndex = 0;
      for (Class argType : method.getParameterTypes()) {
        sw.append(prefix);
        sw.append(argType.getSimpleName());
        if (argType != StringBuffer.class && argType != Result.class) {
          sw.append(" (");
          sw.append(String.valueOf(args[argIndex]));
          sw.append(")");
        }
        argIndex++;
        prefix = ", ";
      }
      sw.append(")");
      return sw.toString();
    }

    /**
     * Handles recording or replaying the results for the invocation.
     *
     * @param proxy The proxy on which it is called.
     * @param method The method that was called.
     * @param args The arguments to call the method with.
     * @return The value being returned.
     * @throws Throwable If a failure occurs.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
      ReplayNativeApiProvider provider = ReplayNativeApiProvider.this;
      synchronized (provider) {
        StackTraceElement[] stackFrames = Thread.currentThread().getStackTrace();
        List<StackTraceElement> stackTrace = Arrays.asList(stackFrames);
        List parameters = (args == null)
            ? Collections.emptyList() : Arrays.asList(args);

        String invocationHash = provider.hashInvocation(this.apiInterface,
                                                        method,
                                                        parameters,
                                                        stackTrace);

        String resultType = method.getGenericReturnType().getTypeName();
        String resultKey  = RETURN_VALUE_KEY;

        if (provider.recording) {
          // get the stack trace dependencies
          Set<String> dependencies = new LinkedHashSet<>();
          for (StackTraceElement stackFrame: stackTrace) {
            String className = stackFrame.getClassName();
            if (className.startsWith(PACKAGE_PREFIX)) continue;
            if (!className.startsWith(SENZING_PACKAGE_PREFIX)) continue;
            dependencies.add(className);
          }

          // add the dependencies
          provider.currentDependencies.addAll(dependencies);

          // invoke the method
          Object result = method.invoke(this.getNativeApi(), args);

          // cache the results
          Map<String, Object> resultMap = new LinkedHashMap<>();
          resultMap.put(resultKey, result);
          Class[] paramClasses = method.getParameterTypes();
          Type[]  paramTypes   = method.getGenericParameterTypes();

          for (int index = 0; index < paramClasses.length; index++) {
            Class   paramClass      = paramClasses[index];
            String  paramType       = paramTypes[index].getTypeName();
            Object  param           = args[index];
            String  paramResultKey  = PARAM_RESULT_KEY_PREFIX + index;

            switch (paramClass.getName()) {
              case "java.lang.StringBuffer":
              case "java.lang.StringBuilder":
                resultMap.put(paramResultKey, param.toString());
                break;
              case "com.senzing.g2.engine.Result":
                resultMap.put(paramResultKey, ((Result) param).getValue());
                break;
              default:
                // do nothing -- assume this is an input parameter
            }
          }

          // cache the results
          InvocationCache cache = provider.currentCache.get(invocationHash);
          if (cache == null) {
            if (args == null) args = new Object[0];
            cache = new InvocationCache();
            cache.apiClass        = this.apiInterface.getName();
            cache.apiMethod       = method.getName();

            List<String> paramTypeList = new ArrayList<>(args.length);
            for (Type paramType: paramTypes) {
              paramTypeList.add(paramType.getTypeName());
            }
            cache.parameterTypes  = Collections.unmodifiableList(paramTypeList);
            List paramList   = new ArrayList<>(args.length);

            Set<Method> methods = IRRELEVANT_METHODS_MAP.get(this.apiInterface);
            if (!methods.contains(method)) {
              for (Object arg : args) {
                if (arg instanceof StringBuffer) {
                  paramList.add(new StringBuffer());
                } else if (arg instanceof StringBuilder) {
                  paramList.add(new StringBuilder());
                } else if (arg instanceof Result) {
                  paramList.add(new Result());
                } else {
                  paramList.add(arg);
                }
              }
            }
            cache.parametersHash = provider.saveParameters(paramList);
            if (cache.parametersHash == null) {
              cache.parameters = Collections.unmodifiableList(paramList);
            }
            cache.results = new LinkedList<>();
            provider.currentCache.put(invocationHash, cache);
          }
          String resultsHash = provider.saveResults(resultMap);
          Object newValue = (resultsHash == null ? resultMap : resultsHash);

          // check if this one is the same as the previous result
          if (cache.lastResult != null
              && newValue.equals(cache.lastResult.value))
          {
            // simply increment the repeat count
            cache.lastResult.increment();

          } else {
            // otherwise save this new recorded result
            RecordedResult recordedResult = new RecordedResult(newValue);
            cache.results.add(recordedResult);
            cache.lastResult = recordedResult;
          }

          // return the return value
          return result;

        } else {
          InvocationCache cache = provider.currentCache.get(invocationHash);

          if (cache == null) {
            throw new IllegalStateException(
                "No cache for method " + this.invocationToString(method, args)
                    + " with specified parameters (" + invocationHash
                    + ") -- manually remove cache: "
                    + provider.getTestCacheDir());
          }
          // track the thread ID
          long threadid = Thread.currentThread().getId();
          cache.threadIdSet.add(threadid);

          // get the results item
          Object resultsItem;
          if (cache.lastResult != null && cache.lastResult.remaining()) {
            cache.lastResult.decrement();
            resultsItem = cache.lastResult.value;

          } else if (cache.results.size() > 0) {
            cache.lastResult = cache.results.remove(0);
            cache.lastResult.decrement();
            resultsItem = cache.lastResult.value;

          } else {
            // repeat the last result to handle multi-threaded timing
            if (cache.threadIdSet.size() == 1) {
              // only one thread is involved -- we should not get here
              String hashInfo = provider.invocationHashInfo(this.apiInterface,
                                                            method,
                                                            parameters,
                                                            stackTrace);
              throw new IllegalStateException(
                  "Invocation ran out of results in single-threaded "
                      + "environment: " + hashInfo);
            }
            resultsItem = cache.lastResult.value;
          }

          Map<String, Object> results = null;
          if (resultsItem instanceof String) {
            results = provider.loadResults(resultsItem.toString());
          } else {
            results = (Map<String,Object>) resultsItem;
          }
          Class[] paramClasses  = method.getParameterTypes();
          Type[]  paramTypes    = method.getGenericParameterTypes();
          for (int index = 0; index < paramClasses.length; index++) {
            // if no parameter result exists, skip this one
            String paramType = paramTypes[index].getTypeName();
            String paramResultKey = PARAM_RESULT_KEY_PREFIX + index;
            if (!results.containsKey(paramResultKey)) continue;

            Class   paramClass  = paramClasses[index];
            Object  param       = args[index];
            Object  paramValue  = results.get(paramResultKey);

            switch (paramClass.getName()) {
              case "java.lang.StringBuffer":
              case "java.lang.StringBuilder":
                ((Appendable) param).append(paramValue.toString());
                break;
              case "com.senzing.g2.engine.Result":
                switch (paramType) {
                  case "int":
                  case "java.lang.Integer":
                  case "com.senzing.g2.engine.Result(java.lang.Integer)":
                    ((Result) param).setValue(((Long)paramValue).intValue());
                    break;
                  default:
                    ((Result) param).setValue(paramValue);
                }
                break;
              default:
                throw new IllegalStateException(
                    "Unable to set output parameter for method call.  method=[ "
                        + method.getName() + " ], parameterIndex=[ " + index
                        + " ], parameterType=[ " + paramType
                        + " ], testCacheDir=[ " + provider.getTestCacheDir() + " ]");
            }
          }

          // return the cached result
          Object resultValue = results.get(resultKey);
          switch (resultType) {
            case "int":
            case "java.lang.Integer":
            case "com.senzing.g2.engine.Result(java.lang.Integer)":
              return ((Long) resultValue).intValue();
            default:
              return resultValue;
          }
        }
      }
    }
  }
}
