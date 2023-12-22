package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.api.server.SzApiServer;
import com.senzing.api.server.SzApiServerOptions;
import com.senzing.util.JsonUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static com.senzing.api.services.ResponseValidators.*;
import static com.senzing.api.model.SzHttpMethod.*;

@TestInstance(Lifecycle.PER_CLASS)
public class ConfigServicesWriteTest extends AbstractServiceTest
{
  private static final int ID_STEP = 2;

  private static final int RESOLVE_STEP = 3;

  private static final int CLASS_STEP = 3;

  private ConfigServices configServices;

  private int nextDataSourceId = 10001;

  private enum SpecifiedMode {
    AUTOMATIC,
    EXPLICIT,
    ALTERNATING;

    public boolean isSpecified(String code, int step) {
      switch (this) {
        case AUTOMATIC:
          return false;
        case EXPLICIT:
          return true;
        case ALTERNATING:
          return ((code.hashCode() % step) == 0);
        default:
          throw new IllegalStateException("Unhandled specified mode: " + this);
      }
    }
  }

  private static final SpecifiedMode AUTOMATIC   = SpecifiedMode.AUTOMATIC;
  private static final SpecifiedMode EXPLICIT    = SpecifiedMode.EXPLICIT;
  private static final SpecifiedMode ALTERNATING = SpecifiedMode.ALTERNATING;

  private enum Formatting {
    BARE_TEXT_CODE(0, 1, EnumSet.of(AUTOMATIC)),
    QUOTED_TEXT_CODE(0, -1, EnumSet.of(AUTOMATIC)),
    JSON_OBJECT(1, 1, EnumSet.of(AUTOMATIC, EXPLICIT)),
    JSON_OBJECT_ARRAY(0, -1, EnumSet.allOf(SpecifiedMode.class)),
    MIXED_FORMATTING(2, -1, EnumSet.allOf(SpecifiedMode.class));

    private int minCount = 0;
    private int maxCount = 0;
    private EnumSet<SpecifiedMode> detailsSupport;

    Formatting(int                    minCount,
               int                    maxCount,
               EnumSet<SpecifiedMode> detailsSupport)
    {
      this.minCount   = minCount;
      this.maxCount   = maxCount;
      this.detailsSupport = detailsSupport;
    }

    public int getMinCount() {
      return this.minCount;
    }

    public int getMaxCount() {
      return this.maxCount;
    }

    public EnumSet<SpecifiedMode> getDetailsSupport() {
      return this.detailsSupport;
    }

    public boolean isBare() {
      return this == BARE_TEXT_CODE;
    }

    public String getContentType() {
      return (this == BARE_TEXT_CODE)
          ? "text/plain; charset=UTF-8" : "application/json; charset=UTF-8";
    }
  }

  private static final Formatting BARE_TEXT_CODE   = Formatting.BARE_TEXT_CODE;
  private static final Formatting QUOTED_TEXT_CODE = Formatting.QUOTED_TEXT_CODE;
  private static final Formatting JSON_OBJECT      = Formatting.JSON_OBJECT;
  private static final Formatting FULL_JSON_OBJECT = Formatting.JSON_OBJECT_ARRAY;
  private static final Formatting MIXED_FORMATTING = Formatting.MIXED_FORMATTING;

  private static class DataSourceBodyVariant {
    /**
     * The formatting to use.
     */
    private Formatting formatting;

    /**
     * The number of data sources.
     */
    private int count;

    /**
     * Whether or not the data sources have explicit IDs.
     */
    private SpecifiedMode identifierMode;

    /**
     * Constructs the various parameters.
     */
    private DataSourceBodyVariant(Formatting      formatting,
                                  int             count,
                                  SpecifiedMode identifierMode)
    {
      this.formatting     = formatting;
      this.count          = count;
      this.identifierMode = identifierMode;
    }

    /**
     * Gets the formatting.
     */
    public Formatting getFormatting() {
      return this.formatting;
    }

    /**
     * Returns the number of data sources.
     */
    public int getCount() {
      return this.count;
    }

    /**
     * Returns the identifier mode for the data sources.
     */
    public SpecifiedMode getIdentifierMode() {
      return this.identifierMode;
    }

    /**
     * Formats the values.
     */
    public String formatValues(List<SzDataSource> values) {
      switch (this.formatting) {
        case BARE_TEXT_CODE:
        {
          int count = values.size();
          if (count == 0) return "";
          if (count != 1) {
            throw new IllegalStateException(
                "Cannot format bare with multiple values.");
          }
          return values.get(0).getDataSourceCode();
        }

        case QUOTED_TEXT_CODE:
        {
          JsonArrayBuilder jab = Json.createArrayBuilder();
          for (SzDataSource value : values) {
            jab.add(value.getDataSourceCode());
          }
          return JsonUtilities.toJsonText(jab);
        }

        case JSON_OBJECT:
        {
          SpecifiedMode idMode  = this.getIdentifierMode();
          SzDataSource      value   = values.get(0);
          String            code    = value.getDataSourceCode();
          boolean           withId  = idMode.isSpecified(code, ID_STEP);
          JsonObjectBuilder job     = Json.createObjectBuilder();
          value.buildJson(job);
          if (!withId) job.remove("dataSourceId");
          return JsonUtilities.toJsonText(job);
        }

        case JSON_OBJECT_ARRAY:
        {
          SpecifiedMode idMode = this.getIdentifierMode();
          JsonArrayBuilder jab = Json.createArrayBuilder();
          for (int index = 0; index < values.size(); index++) {
            SzDataSource      value   = values.get(index);
            String            code    = value.getDataSourceCode();
            boolean           withId  = idMode.isSpecified(code, ID_STEP);
            JsonObjectBuilder job     = Json.createObjectBuilder();
            value.buildJson(job);
            if (!withId) job.remove("dataSourceId");
            jab.add(job);
          }
          return JsonUtilities.toJsonText(jab);
        }

        case MIXED_FORMATTING:
        {
          SpecifiedMode idMode    = this.getIdentifierMode();
          boolean           codeOnly  = true;
          JsonArrayBuilder  jab     = Json.createArrayBuilder();
          for (int index = 0; index < values.size(); index++) {
            SzDataSource      value   = values.get(index);
            String            code    = value.getDataSourceCode();
            boolean           withId  = idMode.isSpecified(code, ID_STEP);
            if (withId || !codeOnly) {
              JsonObjectBuilder job = Json.createObjectBuilder();
              value.buildJson(job);
              if (!withId) {
                job.remove("dataSourceId");
                codeOnly = true;
              }
              jab.add(job);

            } else {
              codeOnly = false;
              jab.add(value.getDataSourceCode());
            }
          }
          return JsonUtilities.toJsonText(jab);
        }

        default:
          throw new IllegalStateException(
              "Unhandled formatting: " + this.formatting);
      }
    }

    /**
     * Converts this instance to a string.
     */
    public String toString() {
      return "formatting=[ " + this.formatting + " ], identifierMode=[ "
          + this.identifierMode + " ], count=[ " + this.count + " ]";
    }
  }

  /**
   * Sets the desired options for the {@link SzApiServer} during server
   * initialization.
   *
   * @param options The {@link SzApiServerOptions} to initialize.
   */
  protected void initializeServerOptions(SzApiServerOptions options) {
    super.initializeServerOptions(options);
    options.setAdminEnabled(true);
    options.setSkippingEnginePriming(true);
  }

  @BeforeAll public void initializeEnvironment() {
    this.beginTests();
    this.initializeTestEnvironment();
    this.configServices = new ConfigServices();
  }

  @AfterAll public void teardownEnvironment() {
    try {
      this.teardownTestEnvironment();
      this.conditionallyLogCounts(true);
    } finally {
      this.endTests();
    }
  }

  protected void revertToInitialConfig() {
    super.revertToInitialConfig();
    this.nextDataSourceId   = 10001;
  }

  private SzDataSource nextDataSource(SpecifiedMode idMode) {
    Integer sourceId    = this.nextDataSourceId++;
    String  sourceCode  = "TEST_SOURCE_" + sourceId;
    boolean withId      = idMode.isSpecified(sourceCode, ID_STEP);
    return SzDataSource.FACTORY.create(sourceCode, withId ? sourceId : null);
  }

  private List<SzDataSource> nextDataSources(int count, SpecifiedMode idMode)
  {
    List<SzDataSource> result = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      result.add(this.nextDataSource(idMode));
    }
    return result;
  }

  private List<DataSourceBodyVariant> createDataSourceBodyVariants() {
    List<DataSourceBodyVariant> result = new LinkedList<>();

    for (Formatting formatting: Formatting.values()) {
      int minCount = formatting.getMinCount();
      int maxCount = formatting.getMaxCount();
      if (maxCount < 0) maxCount = minCount + 3;
      EnumSet<SpecifiedMode> idModes = formatting.getDetailsSupport();
      for (int count = minCount; count <= maxCount; count++) {
        for (SpecifiedMode idMode: idModes) {
          DataSourceBodyVariant variant
              = new DataSourceBodyVariant(formatting, count, idMode);
          result.add(variant);
        }
      }
    }

    return result;
  }

  public List<Arguments> getPostDataSourcesParameters() {
    List<Arguments> result = new LinkedList<>();
    Boolean[] booleanVariants = { null, true, false };
    int[] queryCounts = { 0, 1, 3 };

    List<DataSourceBodyVariant> bodyVariants
        = this.createDataSourceBodyVariants();

    int rawIndex = 0;
    for (int queryCount : queryCounts) {
      Boolean withRaw = booleanVariants[rawIndex];
      rawIndex = (rawIndex + 1) % booleanVariants.length;

      for (DataSourceBodyVariant bodyVariant : bodyVariants) {
        int bodyCount = bodyVariant.getCount();
        Object[] argArray = {
            queryCount,
            bodyVariant,
            0,
            withRaw
        };
        result.add(arguments(argArray));

        // handle an overlap test as well conditionally
        if (queryCount == 3 && bodyCount > 1) {
          Object[] argArray2 = {
              queryCount,
              bodyVariant,
              2,
              withRaw
          };

          result.add(arguments(argArray2));
        }
      }
    }
    return result;
  }

  private String buildPostDataSourcesQuery(List<String> querySources,
                                           Boolean      withRaw)
  {
    StringBuilder sb = new StringBuilder();
    String prefix = "?";
    for (String sourceCode: querySources) {
      sb.append(prefix).append("dataSource=").append(sourceCode);
      prefix = "&";
    }
    if (withRaw != null) {
      sb.append(prefix).append("withRaw=").append(withRaw);
      prefix = "&";
    }
    return sb.toString();
  }

  @ParameterizedTest
  @MethodSource("getPostDataSourcesParameters")
  public void postDataSourcesTest(int                   querySourceCount,
                                  DataSourceBodyVariant bodySourceVariant,
                                  int                   overlapCount,
                                  Boolean               withRaw)
  {
    this.performTest(() -> {
      this.revertToInitialConfig();

      Map<String, SzDataSource> expectedSources = new LinkedHashMap<>();
      expectedSources.putAll(this.getInitialDataSources());

      int queryOnlyCount = querySourceCount - overlapCount;
      List<SzDataSource> querySources
          = this.nextDataSources(queryOnlyCount, AUTOMATIC);

      int             bodyCount = bodySourceVariant.getCount();
      SpecifiedMode idMode      = bodySourceVariant.getIdentifierMode();

      List<SzDataSource> overlapSources
          = this.nextDataSources(overlapCount, idMode);

      List<SzDataSource> bodySources = new ArrayList<>(bodyCount);
      bodySources.addAll(overlapSources);

      int bodyOnlyCount = bodyCount - overlapCount;
      bodySources.addAll(this.nextDataSources(bodyOnlyCount, idMode));

      querySources.addAll(overlapSources);

      List<String> queryCodes = new ArrayList<>(querySources.size());
      for (SzDataSource source: querySources) {
        queryCodes.add(source.getDataSourceCode());
        expectedSources.put(source.getDataSourceCode(), source);
      }
      for (SzDataSource source: bodySources) {
        expectedSources.put(source.getDataSourceCode(), source);
      }

      String suffix = this.buildPostDataSourcesQuery(queryCodes, withRaw);
      String relativeUri = "data-sources" + suffix;
      String uriText = this.formatServerUri(relativeUri);
      UriInfo uriInfo = this.newProxyUriInfo(uriText);
      String bodyContent = bodySourceVariant.formatValues(bodySources);
      String testInfo = this.formatTestInfo(relativeUri,
                                            bodyContent);

      long before = System.nanoTime();
      SzDataSourcesResponse response = this.configServices.addDataSources(
          queryCodes,
          TRUE.equals(withRaw),
          uriInfo,
          bodyContent);

      response.concludeTimers();
      long after = System.nanoTime();

      validateDataSourcesResponse(testInfo,
                                  response,
                                  POST,
                                  uriText,
                                  after - before,
                                  TRUE.equals(withRaw),
                                  expectedSources);
    });
  }

  @ParameterizedTest
  @MethodSource("getPostDataSourcesParameters")
  public void postDataSourcesViaHttpTest(
      int                   querySourceCount,
      DataSourceBodyVariant bodySourceVariant,
      int                   overlapCount,
      Boolean               withRaw)
  {
    this.performTest(() -> {
      this.revertToInitialConfig();

      Map<String, SzDataSource> expectedSources = new LinkedHashMap<>();
      expectedSources.putAll(this.getInitialDataSources());

      int queryOnlyCount = querySourceCount - overlapCount;
      List<SzDataSource> querySources
          = this.nextDataSources(queryOnlyCount, AUTOMATIC);

      int             bodyCount = bodySourceVariant.getCount();
      SpecifiedMode idMode    = bodySourceVariant.getIdentifierMode();

      List<SzDataSource> overlapSources
          = this.nextDataSources(overlapCount, idMode);

      List<SzDataSource> bodySources = new ArrayList<>(bodyCount);
      bodySources.addAll(overlapSources);

      int bodyOnlyCount = bodyCount - overlapCount;
      bodySources.addAll(this.nextDataSources(bodyOnlyCount, idMode));

      querySources.addAll(overlapSources);

      List<String> queryCodes = new ArrayList<>(querySources.size());
      for (SzDataSource source: querySources) {
        queryCodes.add(source.getDataSourceCode());
        expectedSources.put(source.getDataSourceCode(), source);
      }
      for (SzDataSource source: bodySources) {
        expectedSources.put(source.getDataSourceCode(), source);
      }

      String  suffix      = this.buildPostDataSourcesQuery(queryCodes, withRaw);
      String  relativeUri = "data-sources" + suffix;
      String  uriText     = this.formatServerUri(relativeUri);
      String  bodyContent = bodySourceVariant.formatValues(bodySources);
      String  testInfo    = this.formatTestInfo(relativeUri,
                                                bodyContent);

      // convert the body content to a byte array
      byte[] bodyContentData;
      try {
        bodyContentData = bodyContent.getBytes("UTF-8");
      } catch (UnsupportedEncodingException cannotHappen) {
        throw new IllegalStateException(cannotHappen);
      }
      long before = System.nanoTime();

      SzDataSourcesResponse response = this.invokeServerViaHttp(
          POST,
          uriText,
          null,
          bodySourceVariant.getFormatting().getContentType(),
          bodyContentData,
          SzDataSourcesResponse.class);

      response.concludeTimers();
      long after = System.nanoTime();

      validateDataSourcesResponse(testInfo,
                                  response,
                                  POST,
                                  uriText,
                                  after - before,
                                  TRUE.equals(withRaw),
                                  expectedSources);
    });
  }
}
