# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
[markdownlint](https://dlaa.me/markdownlint/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.3.0] - 2022-08-17

### Changed in 3.3.0

- Added ability to leverage `G2Engine.G2_ENTITY_INCLUDE_RECORD_FEATURE_IDS` 
  flag when requesting entity data via REST API
  - Added `SzFeatureMode.ATTRIBUTED` enumerated value
  - Added `com.senzing.api.model.SzFeatureReference` interface
  - Added `com.senzing.api.model.impl.SzFeatureReferenceImpl` class
  - Added `featureReferences` property to the `SzEntityRecord` interface
  - Added `featureReferences` property to the `SzEntityRecordImpl` class
- Added ability to leverage `G2Engine.getVirtualEntityByRecordID()` via REST API
  - Added `com.senzing.api.services.HowRelatedServices` class
    - Added `HowRelatedServices.getVirtualEntity()` function to provide the
      `GET /virtual-entities` operation.
  - Added auto tests for `HowRelatedServiecs.getVirtualEntity()`
  - Added `com.senzing.api.model.SzVirtualEntityResponse` interface
  - Added `com.senzing.api.model.SzVirtualEntityData` interface
  - Added `com.senzing.api.model.impl.SzVirtualEntityResponseImpl` class.
  - Added `com.senzing.api.model.impl.SzVirtualEntityDataImpl` class
  - Added functions in `ServicesSupport` class to handle record identifiers
- Added ability to leverage `G2Engine.howEntityByEntityID()` via REST API
  - Added `com.senzing.api.services.HowRelatedServices` class
    - Added `HowRelatedServices.howEntityByRecordId()` function to provide the
      `GET /data-sources/{dataSource}/records/{recordId}/entity/how` operation
    - Added `HowRelatedServices.howEntityByEntityId()` function to provide the
      `GET /entities/{entityId}/how` operation.
  - Added `com.senzing.api.model.SzHowMatchInfo` interface
  - Added `com.senzing.api.model.SzVirtualEntityRecord` interface
  - Added `com.senzing.api.model.SzVirtualEntity` interface
  - Added `com.senzing.api.model.SzResolutionStep` interface
  - Added `com.senzing.api.model.SzHowEntityResult` interface
  - Added `com.senzing.api.model.SzHowEntityResponse` interface
  - Added `com.senzing.api.model.impl.SzHowMatchInfoImpl` class
  - Added `com.senzing.api.model.impl.SzVirtualEntityRecordImpl` class
  - Added `com.senzing.api.model.impl.SzVirtualEntityImpl` class
  - Added `com.senzing.api.model.impl.SzResolutionStepImpl` class
  - Added `com.senzing.api.model.impl.SzHowEntityResultImpl` class
  - Added `com.senzing.api.model.impl.SzHowEntityResponseImpl` class
- Updated `com.senzing.api.services.WhyServices` to handle `whyEntities()`
  race conditions where the entity ID's associated with the specified 
  record ID's change after looking up the entity ID's but before returning the
  `WhyEntitiesResponse`.

## [3.2.0] - 2022-08-09

### Changed in 3.2.0

- Added support for `SENZING_ENGINE_CONFIGURATION_JSON` environment variable
  for the `--init-json` command-line option.
- Updated handling of `DELETE /data-sources/{code}/records/{id}?withInfo=true`
  (`EntityDataServices.deleteRecord()`) when the record no longer exists to 
  still produce an INFO message with no affected entity ID's, but still having a
  data source code and record ID.
- Added support for REST API Spec v3.1.0 including "detail levels"
  - Added `SzDetailLevel` in support of REST API Spec v3.1.0 "detail levels"
  - Added `detailLevel` parameter to `EntityDataServices` methods
  - Added `detailLevel` parameter to `WhyServices` methods
  - Added `detailLevel` parameter to `EntityGraphServices` methods
  - Added `detailLevel` parameter to `ServicesSupport.getFlags()` method
- Updated dependency versions
  - Updated Jetty dependencies to version `9.4.45.v20220203`
  - Updated Jersey dependencies to version `2.36`
  - Updated ICU4j dependency to version `71.1`
  - Updated test-scoped Spring Framework dependency to `[5.3.22,5.9999.9999)`
  - Updated test-scoped Swagger Codegen dependency to `3.0.20`
- Deleted test cache data for version `3.0.0`
- Generated test cache data for version `3.2.0`
- Updated `pom.xml` and `Dockerfile` to reflect version `3.2.0`

## [3.1.1] - 2022-07-20

### Changed in 3.1.1

- In `Dockerfile`, bump from `senzing/senzingapi-runtime:3.1.0` to `senzing/senzingapi-runtime:3.1.1`

## [3.1.0] - 2022-07-12

### Changed in 3.1.0

- Migrated to `senzing/senzingapi-runtime` as Docker base image

## [3.0.2] - 2022-06-08

### Changed in 3.0.2

- Upgrade `Dockerfile` to `FROM debian:11.3-slim@sha256:06a93cbdd49a265795ef7b24fe374fee670148a7973190fb798e43b3cf7c5d0f`

## [3.0.1] - 2022-05-06

### Changed in 3.0.1

- Added `libodbc1` to Dockerfile

## [3.0.0] - 2022-05-04

### Changed in 3.0.0

- Removed endpoints, classes and methods pertaining to Entity Types
- Removed endpoints, classes and methods pertaining to Entity Classes
- Removed `matchScore` and `refScore` from `SzBaseRelatedEntity` since
  `MATCH_SCORE` and `REF_SCORE` were removed from the native/raw JSON in v3.0
- Removed `refScore` from `SzMatchedRecord` since `REF_SCORE` was removed in v3.0
- Updated `senzing-commons-java` dependency to version `3.x`
- Updated `g2-sdk-java` dependency to version `3.x`
- Updated supported API specification version to `3.0.0`

## [2.8.6] - 2022-04-18

### Added to 2.8.6

- Modified `Dockerfile` to upgrade Debian base version to 11.3

## [2.8.5] - 2022-03-29

### Changed in 2.8.5

- Updates to latest dependency versions to resolve/avoid security issues.
  Specifically resolves CVE-2020-36518 for jackson-databind.
- Added `@SuppressWarnings()` annotation to some files to eliminate specific
  compile-time warnings.

## [2.8.4] - 2022-03-07

### Changed in 2.8.4

- Updates to `pom.xml` to prevent pulling beta `3.0.0` versions of
  `senzing-sdk-java` and `senzing-commons-java` via the Maven dependency range.

## [2.8.3] - 2022-02-04

### Changed in 2.8.3

- Updated pom.xml to depend on `senzing-commons` release version `2.x`
- Updated classes that previously used `com.senzing.util.JsonUtils` to
  instead use `com.senzing.util.JsonUtilities`

## [2.8.2] - 2022-01-25

### Changed in 2.8.2

- Updated Docker image with new version of Debian
- Removed dependence on senzing-base image

## [2.8.1] - 2022-01-13

### Changed in 2.8.1

- Updated `README.MD` to include latest usage message for `--enable-admin`
- Updated spring framework dependencies to resolve security vulnerability.
- Updated cached test data to version 2.8.5 of Senzing

## [2.8.0] - 2021-11-30

### Changed in 2.8.0

- Reduced footprint of cached test data to only the latest version of Senzing.
  For other versions the tests will have to be run with a live Senzing SDK.
- Modified to factor out the `senzing-commons-java` dependency.
- Modified to mask sensitive command-line option parameters when logging the
  startup parameters as JSON.
- Updated to senzing/senzing-base:1.6.3

## [2.7.5] - 2021-10-11

### Changed in 2.7.5

- Updated to senzing/senzing-base:1.6.2

## [2.7.4] - 2021-09-08

### Changed in 2.7.4

- Improved debug logging with standardized formatting and timestamps.
- Added more debug logging to TemporaryDataCache.
- Renamed enum constant for debug logging from DEBUG to DEBUG_LOGGING.
- Added debug option to SzApiServerOptions for programmatic configuration.
- Added `com.senzing.api.test.debug=true` system property check to run
  automated tests with debug logging enabled.
- Modified usage message to include information on `--debug` option.

## [2.7.3] - 2021-09-02

### Changed in 2.7.3

- Added --debug command-line option with SENZING_API_SERVER_DEBUG environment
  variable equivalent to enable API Server debug logging.
- Added debug logging of every incoming request if --debug enabled.
- Added debug logging for bulk-data related operations if --debug enabled.
- Updated build-info.properties so the Maven build timestamp is properly
  filtered during build and token-replaced.
- Minor bug fix for internal CommandLineUtilities class to process default
  values for "base" options.  This allows others to extend the Senzing API
  Server code with new options but still have the default values from the
  base options be populated (required for `Senzing/senzing-poc-server`).

## [2.7.2] - 2021-08-20

### Changed in 2.7.2

- Minor bug fix for internal CommandLineUtilities class to recognize environment
  variables for "base" options.  This allows others to extends the Senzing API
  Server code with new options but still have the environment variables from the
  base options be recognized (required for `Senzing/senzing-poc-server`).

## [2.7.1] - 2021-08-16

### Changed in 2.7.1

- Modified EntityDataServices.java to check for empty networks when getting
  entities with "FULL" related entities.  This guards against a
  `NullPointerException` in the event of data corruption.  Affected endpoints:
  - `GET /entities/{entityId}?withRelated=FULL`
  - `GET /data-sources/{dataSourceCode}/records/{recordId}/entity?withRelated=FULL`
- Updated cached test mock data for Senzing versions 2.0.0 through 2.8.1 to
  cover the version 2.8.1 release.

## [2.7.0] - 2021-07-22

### Changed in 2.7.0

- Added support for HTTPS/SSL communication with optional client SSL
  authentication.
- Added support for GZIP compression of HTTP response content when the client
  sends the "Accept-Encoding: gzip" request header is sent by the client.
- Added `GET /` endpoint that mimics the functionality of `GET /heartbeat`
- Added `GET /specifications/open-api` endpoint to get the Open API
  specification as JSON data.  The optional `?asRaw=true` query parameter
  promotes the Open API JSON from the `data` property of the response to the
  root of the response.
- Updated Senzing REST API Specification version to 2.7.0.
- Modifications to make the BulkDataServicesReadOnlyTest less sensitive to
  timing issues.
- Refactored model classes, service classes and API server classes to allow for
  extending and customizing the API Server.  **NOTE**: while these changes will
  **NOT** break backwards compatibility for REST API clients, Java projects that
  extend API Server Java classes (e.g.: customized API servers) will be
  affected.  The semantic versioning for the API Server guarantees that minor
  releases maintain backwards compatibility for REST API clients.  However,
  these changes are working towards **possibly** maintaining backwards
  compatibility for extended Java code in minor releases in a future major
  release.

## [2.6.2] - 2021-07-15

### Changed in 2.6.2

- Updated to senzing/senzing-base:1.6.1

## [2.6.1] - 2021-06-09

### Changed in 2.6.1

- Modified auto tests to accommodate forth-coming changes in Senzing v2.7.0
- Added new cached auto-test data for versions 2.0.0 through 2.7.0
- Updated Jetty dependencies for security patch

## [2.6.0] - 2021-04-23

### Changed in 2.6.0

- Modified `SzApiServer` to enable Web Sockets connectivity
- Modified `SzApiServer`, `SzApiServerOption` and `SzApiServerOptions` to enable
  configurability of the Jetty Web Server HTTP Thread pool via the
  `--http-concurrency` option (and `SENZING_API_SERVER_HTTP_CONCURRENCY
  environment variable)
- Modified `SzApiServer` to enable support for limitations on concurrent
  prolonged operations to prevent exhaustion of Jetty Web Server HTTP pool
- Added Web Sockets and SSE tests in `BulkDataServicesTests` for
  `POST /bulk-data/analyze` and `POST /bulk-data/load`
- Modified `ServicesUtil` to provide utility method for producing 503 responses
- Modified `BulkDataServices` to limit the number of concurrent executions of
  `POST /bulk-data/analyze` and `POST /bulk-data/load` as "prolonged" operations
  when executed over HTTP rather than Web Sockets.  These operations produce a
  "503 Service Unavailable" response if too many concurrent HTTP executions.
- Modified `BulkDataServices` to add Web Sockets support to
  `POST /bulk-data/analyze` and `POST /bulk-data/load`
- Modified `AdminServices` to set the `webSocketsMessageMaxSize` property on
  the `SzServerInfo` as part of the `GET /server-info` implementation.
- Modified `SzApiProvider` to add a means to get the `webSocketsMessageMaxSize`
  and to provide a means to track and limit prolonged operations.
- Added `webSocketsMessageMaxSize` property to `SzServerInfo`
- Added HTTP concurrency and Web Sockets message size constants to
  `SzApiServerConstants`
- Added `com.senzin.io.ChunkedEncodingInputStream` to aid in SSE auto tests.
  This class provides decoding of chunked transfer encoding.
- Minor changes to `RecordReader` to make it more robust when auto-detecting the
  file format.
- Modified `TemporaryDataCache` to more aggressively create file parts to make
  them available for reading rather than waiting for a specific number of bytes
  to be read from the source stream.
- Added `readAsciiLine()` function to `IOUtilities`
- Updated REST API Specification version to 2.6.0 in `BuildInfo` class.

## [2.5.0] - 2021-03-24

### Changed in 2.5.0

- Added `POST /search-entities` endpoint in `EntityDataServices` along with
  auto tests in `EntityDataReadServicesTest`
- Converted command-line options to use `--command-line-option` format with
  `-commandLineOption` synonyms to maintain backwards compatibility
- Added environment variable support to command-line options so many of the
  options can now be specified via environment variables.  This is intended to
  primarily help with Docker deployments.
- Added command-line option for `--url-base-path` / `-urlBasePath` (along with
  `SENZING_API_SERVER_URL_BASE_PATH` environment variable) to set the base path
  of the API server on startup
- Updated usage string of `SzApiServer` to reflect all changes regarding
  command-line options
- Updated/fixed the output when running with `-version` command-line option
- Modified `SzVersionInfo` to include `nativeApiBuildVersion` field on the
  `GET /version` response
- Modified `SzMeta` to include `nativeApiBuildVersion` field
- Modified `SzApiProvider` to include `nativeApiBuildVersion` field

### Fixed in 2.5.0

- Fixed the `pom.xml` so the replacement of `${project.version}` in the
  `build-info.properties` is restored
- Fixed typo bug with the meta timings of `searchByAttributesV2` in
  `EntityDataServices` class
- Removed extraneous debug logging code in `EntityDataServices` that caused
  both a security issue and a memory leak

## [2.4.0] - 2021-03-11

### Changed in 2.4.0

- Added `includeOnly` query parameter to `GET /entities` endpoint.  NOTE: this
  parameter is only recognized if the underlying native Senzing API Product is
  version 2.4.1 or later.
- Modified `SzMeta` to include four new fields to be included in the meta
  section of each response:
  - `nativeApiVersion`
  - `nativeApiBuildNumber`
  - `nativeApiBuildDate`
  - `configCompatibilityVersion`
- Added `ServiceUtil.getFlags()` variant that takes a base flags parameter
- Added automatic engine priming on startup
- Added `-skipEnginePriming` option to skip engine priming on startup
- Updated supported specification version to 2.4.0
- Updated recorded test data for Senzing API versions 2.0.0 through version 2.4.0

## [2.3.2] - 2021-01-21

### Changed in 2.3.2

- Added JAXB and Javassist dependencies to fix ClassNotFoundException during
  OPTIONS requests (especially for CORS support with `-allowedOrigins` option)
- Added additional allowed HTTP methods for CORS support (`PUT`, `DELETE`,
  `PATCH` and `OPTIONS` in addition to the already supported `GET`, `POST`,
  and `HEAD`)
- Added DiagnoseRequestFilter to log the request line to `stderr` if an
  exception is caught in processing the request
- Updated recorded test data for Senzing API versions 2.0.0 through version 2.3.0
- Added NOTICES file to account for Eclipse Distribution License v1.0 requirements

## [2.3.1] - 2020-12-15

### Changed in 2.3.1

- Updated `WhyServicesTest` to properly validate `whyEntitiesV2()` responses for
  Senzing API version 2.3.0
- Added recorded test data for Senzing API version 2.2.6
- Added recorded test data for Senzing API version 2.3.0
- Updated Jetty to version 9.4.35.v20201120 for security patch

## [2.3.0] - 2020-11-18

### Changed in 2.3.0

- Added `WhyServices.whyEntities()` method for `GET /why/entities`
- Modified `com.senzing.api.model.SzMatchInfo` to support `whyEntities()`
- Modified `com.senzing.api.model.SzScoredFeature` to support `whyEntities()`
- Modified `com.senzing.api.model.SzEntityData` to exclude `relatedEntities`
  from the JSON serialization if empty or null.
- Added `com.senzing.api.model.SzWhyEntitiesResponse`
- Added `com.senzing.api.model.SzWhyEntitiesResult`
- Added `com.senzing.api.model.SzDisclosedRelation`
- Added `com.senzing.api.model.SzRelationDirection`
- Added `com.senzing.api.model.SzRelatedFeatures`
- Refactored some functionality from `EntityDataServices` to `ServicesUtil`
- Added new tests for `WhyServices.whyEntities()`
- Pared down the number of tests ran from `WhyServicesTest` for faster runs.
- Re-ran tests for all versions of native Senzing SDK from 2.0.0 to 2.2.5
and recorded mock test data.

## [2.2.2] - 2020-11-05

### Changed in 2.2.2

- Modified `com.senzing.api.server.G2EngineRetryHandler` so that it
recognizes new functions in `com.senzing.g2.engine.G2Engine`.
- Re-ran tests for all versions of native Senzing SDK from 2.0.0 to 2.3.0
and recorded mock test data.

## [2.2.1] - 2020-10-16

### Changed in 2.2.1

- Modified `com.senzing.api.services.EntityDataServices` so that
`POST /data-sources/{dataSourceCode}/records` call will be tolerant of the
`RECORD_ID` specified in the JSON payload for the record.
- Updated EntityDataWriteServicesTest to handle testing POST with various
record ID variants.
- Re-ran tests for all versions of native Senzing SDK from 2.0.0 to 2.2.1

## [2.2.0] - 2020-10-15

### Changed in 2.2.0

- Added `com.senzing.api.model.SzNameScoring` to describe name scoring details
- Added `com.senzing.api.model.SzSearchFeatureScore` for search feature scores
- Modified `com.senzing.api.model.SzBaseRelatedEntity` to remove `fullNameScore`
field since it has not been populated since switch to version 2.0.0 of native
Senzing SDK.
- Added `bestNameScore` field to `com.senzing.api.model.SzAttributeSearchResult`
to replace `fullNameScore` in the place where the name score was previously
used with version 1.x of the native Senzing SDK (i.e.: to sort search results
based on the strength of the name match).
- Modified `com.senzing.api.model.SzAttributeSearchResult` to include the
`featureScores` field to provide feature scores without using "raw data"
- Added `nameScoringDetails` field to `com.senzing.api.model.SzFeatureScore`
class to provide `SzNameScoring` name scoring details on why operations,
- Updated `com.senzing.api.model.SzFeatureScore` to set its `score` field to
the most sensible score value from the `nameScoringDetails` for `"NAME"`
features since the `FULL_SCORE` field is not available for names.
- Updated to latest `senzing-rest-api-specification` specification.
- Updated version numbers to 2.2.0
- Re-ran tests for all versions of native Senzing SDK from 2.0.0 to 2.2.1

## [2.1.1] - 2020-10-06

### Changed in 2.1.1

- No longer errors when encountering the `NOT_SCORED` value for `SzScoringBucket`
- No longer errors when encountering a numeric `RECORD_ID` on bulk data load

## [2.1.0] - 2020-10-01

### Changed in 2.1.0

- Fixed defect preventing fields in `SzMatchInfo` from being properly populated
  on "why" endpoints -- now all the matching data comes back.
- Added `SzMatchLevel` enum type to enumerate and describe the various
  match levels and added a `matchLevel` field of that type to `SzMatchInfo`
  to expose the `MATCH_LEVEL_CODE` in the "why" endpoints.
- Added `lastSeenTimestamp` field to expose `LAST_SEEN_DT` for
  `SzResolvedEntity` and `SzEntityRecord`.
- Added periodic logging of `stats()` from the `G2Engine` API with automatic
  suppression if the API Server is idle or not performing tasks that involve
  entity scoring and would therefore not affect the result from `stats()` call.
- Added `-statsInterval` option to specify the time interval in milliseconds for
  stats logging with a 15-minute default and option to suppress if zero (0) is
  specified.
- Added logging of performance information and database performance at startup
- Added `-skipStartupPerf` option to prevent startup performance logging
- Modified tests to validate `lastSeenTimestamp` field.
- Updated test data to include versions 2.0.0 through 2.1.1

## [2.0.2] - 2020-08-25

### Changed in 2.0.2

- Updated ReplayNativeApiProvider to reduce file size of test data files
  to reduce memory usage when running auto-builds on github.
- Produced new auto-test mock data files for several versions of native
  API.

## [2.0.1] - 2020-07-23

### Changed in 2.0.1

- Upgraded to senzing/senzing-base:1.5.2

## [2.0.0] - 2020-07-16

### Changed in 2.0.0

- Modified to build with version 2.x of Senzing product include use of new
  entity formatting flags.

- Updated REST API Version to v2.0.0

- Renamed `com.senzing.api.model.SzFeatureInclusion` to
  `com.senzing.api.model.SzFeatureMode`

- Added `com.senzing.api.model.SzRelationshipMode` with values of `NONE`,
  `PARTIAL` and `FULL`.

- Added new model classes to support Senzing REST API 2.0

- Added `withInfo` and `withRaw` query parameters to following endpoints:
  - `POST /data-sources/{dataSourceCode}/records`
  - `PUT /data-sources/{dataSourceCode}/records/{recordId}`

- Added `DELETE /data-sources/{dataSourceCode}/records/{recordId}` endpoint
  including `withInfo` and `withRaw` query parameters.

- Added the following endpoints to reevaluate entities or specific records
  including `withInfo` and `withRaw` query parameters:
  - `POST /data-sources/{dataSourceCode}/records/{recordId}/entity/reevaluate`
  - `POST /reevaluate-entities`

- In `com.senzing.io.RecordReader` a `null` value in the `dataSourceMap` and
  `entityTypeMap` is now used as the general overriding data source or entity
  type, respectively, while the value mapped to empty-string is used to assign
  a data source or entity type (respectively) to a record that is missing a
  value for that field.

- In `com.senzing.api.model.SzResolvedEntity` the property `attributeData` was
  renamed to `characteristicData` to match the OpenAPI Specification for the
  Senzing REST API.  **NOTE**: Client code that was written to look for
  `attributeData` must be modified or will find a missing property.

- Potentially Backward-Compatibility Breaking Changes by Java class and
  API Endpoint:
  - `com.senzing.api.services.ConfigServices`
    - `GET /entity-classes`
    - `GET /entity-classes/{entityClassCode}`
    - `POST /entity-types`
    - `POST /entity-classes/{entityClassCode}/entity-types`
    - `GET /entity-classes/{entityClassCode}/entity-types/{entityTypeCode}`
      - Removed support for any entity class other than ACTOR as it was
        discovered that the underlying product does not properly support entity
        resolution when using entity classes other than ACTOR and it may not for
        some time. This will change if and when additional entity classes are
        supported.
        - *MIGRATION*: Ensure the value provided for all entity classes are
          changed to `ACTOR`.

    - `POST /entity-classes`
      - Removed this operation as it was discovered that the underlying product
        does not fully properly support entity resolution when using entity
        classes other than ACTOR and it may not for some time.  This will change
        if and when additional entity classes are supported.
        - *MIGRATION*: Remove calls to create new entity classes and instead
          leverage the default entity class `ACTOR`.

    - `GET /config/current`
      - Renamed to `GET /configs/active` since “current” is ambiguous with
        regards to the “currently active config” versus the configuration
        managers currently configured “default config”.
        - *MIGRATION*: Use the `/configs/active` path in place of
          `/config/current`.

    - `GET /config/default`
      - Renamed to `GET /configs/template` since “default” is ambiguous with the
        configuration managers “default config” setting.
        - *MIGRATION*: Use the `/configs/template` path in place of
          `/config/default`.

  - `com.senzing.api.services.EntityDataServices`
    - `GET /data-sources/{dataSourceCode}/records/{recordId}`
      - The `data` property of `SzRecordResponse` was previously of type
        `SzEntityRecord`.  However, the Open API specification for the Senzing
         REST API had always documented it as an object with a single property
         named `record` whose type was `SzEntityRecord` (an additional level of
         indirection).  In order to conform with the specification and make it
         consistent with `SzEntityResponse`, the server has been modified with
         class `SzRecordResponse` now having a `data` property with a `record`
         sub-property.
        - *MIGRATION*: Change direct references to the `data` field to instead
          reference `data.record`.
      - The `SzEntityRecord` in the response will exclude fields that are `null`
        or empty arrays.
        - *MIGRATION*: Depending on the client language, check if fields are
          missing, `null` or `undefined` before attempting to use them.

    - `GET /entities/{entityId}`
    - `GET /data-sources/{dataSourceCode}/records/{recordId}/entity`
      - The `withRelated` parameter is no longer a `boolean` value that accepts
        `true` or `false`.  It now accepts an enumerated value of
         type `com.senzing.api.model.SzRelationshipMode` with values of `NONE`,
        `PARTIAL` or `FULL`.
        - *MIGRATION*: Use `?withRelated=FULL` in place of `?withRelated=true`
          and use `?withRelated=PARTIAL` in place of `?withRelated=false`.
      - The `SzResolvedEntity` and the contained `SzEntityRecord` instances in
        the response will exclude fields that are `null` or empty arrays.
        - *MIGRATION*: Depending on the client language, check if fields are
          missing, `null` or `undefined` before attempting to use them.

    - `GET /entities`
      - Removed the `attr_[PROPERTY_NAME]` parameters and replaced with the
        multi-valued `attr` parameter so that this parameter could better be
        documented in the Open API Spec and examples provided via Swagger
        Editor.
        - *MIGRATION*: Use `?attr=NAME_FIRST:Joe` in place of
          `?attr_NAME_FIRST=Joe` or use the `attrs` parameter with a JSON value.
      - The `SzAttributeSearchResult` instances and contained `SzRelatedEntity`
        and `SzEntityRecord` instances in the response will exclude fields that
        are `null` or empty arrays.
        - *MIGRATION*: Depending on the client language, check if fields are
          missing, `null` or `undefined` before attempting to use them.
      - The `withRelationships` query parameter now defaults to `false` instead
        of `true`.
        - *MIGRATION*: Use `?withRelationships=true` if relationships are
          desired.

    - `POST /data-sources/{dataSourceCode}/records/`
    - `PUT /data-sources/{dataSourceCode}/records/{recordId}`
      - Modified to default `ENTITY_TYPE` to `GENERIC` if `ENTITY_TYPE` not
        found in record.
        - *MIGRATION*: Specify an entity type if `GENERIC` is not desired.

  - `com.senzing.api.services.EntityGraphServices`
    - `GET /entity-networks`
      - Changed the default value for `maxDegrees` parameter from 5 to 3
        - *MIGRATION*: Use `?maxDegrees=5` if the old default is desired.
      - The `SzResolvedEntity` instances and the contained `SzEntityRecord`
        instances in the response will exclude fields that are `null` or empty
        arrays.
        - *MIGRATION*: Depending on the client language, check if fields are
          missing, `null` or `undefined` before attempting to use them.
    - `GET /entity-paths`
      - The `SzResolvedEntity` instances and the contained `SzEntityRecord`
        instances in the response will exclude fields that are `null` or empty
        arrays.
        - *MIGRATION*: Depending on the client language, check if fields are
          missing, `null` or `undefined` before attempting to use them.

  - `com.senzing.api.services.BulkDataServices`
    - `POST /bulk-data/load`
      - Replaced the `dataSource_[DATA_SOURCE_CODE]` parameters with the
        multi-valued `mapDataSource` parameter so that this parameter
        could better be documented in Open API Spec and examples provided via
        Swagger Editor.
        - *MIGRATION*: Use `?mapDataSource=FOO:BAR` in place of
          `?dataSource_FOO=BAR` or use the new `mapDataSources` parameter
           instead.
      - Replaced the `entityType_[ENTITY_TYPE_CODE]` parameters with the
        multi-valued `mapEntityType` parameter so that this parameter could
        better be documented in Open API Spec and examples provided via
        Swagger Editor.
        - *MIGRATION*: Use `?mapEntityType=FOO:BAR` in place of
          `?entityType_FOO=BAR` or use the new `mapEntityTypes` parameter instead.

- Other Changes by Java class and API Endpoint:
  - `com.senzing.api.services.AdminServices`
    - `GET /license`
      - Added the previously undocumented (but always-supported) the “withRaw”
        parameter.

    - `GET /version`
      - Added the previously undocumented (but always-supported) the “withRaw”
        parameter.

    - `POST /bulk-data/load`
      - Added the single-valued `mapDataSources` parameter which accepts
        URL-encoded JSON to map the original data sources to target data
        sources.
      - Added the single-valued `mapEntityTypes` parameter which accepts
        URL-encoded JSON to map the original entity types to target entity
        types.

- Removed pre-recorded mock data from integration tests for versions prior to
  2.0.0 and added pre-recorded mock data for integratioon tests for v2.0.0.

## [1.8.6] - 2020-10-06

### Changed in 1.8.6

- No longer errors when encountering the NOT_SCORED value for SzScoringBucket
- No longer errors when encountering a numeric RECORD_ID on bulk data load

## [1.8.5] - 2020-07-08

### Changed in 1.8.5

- Works with senzing versions up to 1.15.6
- Not supported for senzing version 2.0.0 and above

## [1.8.4] - 2020-05-07

### Fixed in 1.8.4

- Updated EntityGraphServicesTest to account for bug fix in Senzing 1.15.2
- Updated test runs to include additional product versions

## [1.8.3] - 2020-04-24

### Fixed in 1.8.3

- .dockterignore was causing the `-dirty` suffix to be added to docker build versions.

## [1.8.2] - 2020-04-15

### Changed in 1.8.2

- Added WHY operations
  - GET /data-sources/{dataSourceCode}/records/{recordId}/entity/why
  - GET /entities/{entityId}/why
  - GET /why/records
- Added support for the "withFeatureStatistics" and "withDerivedFeatures"
  parameters across the following endpoints:
  - GET /data-sources/{dataSourceCode}/records/{recordId}/entity
  - GET /data-sources/{dataSourceCode}/records/{recordId}/entity/why
  - GET /entities/{entityId
  - GET /entities/{entityId}/why
  - GET /why/records
  - GET /entity-paths
  - GET /entity-networks
- Added the "featureDetails" property to entity results to support obtaining
  the feature ID as well as the feature statistics (if requested).

## [1.8.1] - 2020-03-30

### Changed in 1.8.1

- Supports environment variables for Senzing install locations
  - SENZING_G2_DIR
  - SENZING_DATA_DIR
  - SENZING_ETC_DIR
- Supports default to `/opt/senzing/data` as the support directory if the
  versioned sub-directory is not found and the base directory contains expected
  files.
- Shortens the test time for TemporaryDataCacheTest

## [1.8.0] - 2020-03-27

### Changed in 1.8.0

- Now supports and requires OpenJDK 11.0.x (Java 8 no longer supported)
- Now requires Apache Maven 3.6.1 or later
- Adds config modification functions (data sources, entity types, etc...)
- Adds `-enableAdmin` command-line option to enable adding data sources and
  entity types (if not provided then `403 Forbidden` responses)
- Adds bulk data analyze and load functions
- Adds new testing cache mechanism to replay native API calls without using the
  native API and even run auto tests without having it installed.
- Includes cached native API results for building with v1.13.4 through v1.13.8
  as well as v1.14.1 through v1.14.7

## [1.7.10] - 2020-01-29

### Changed in 1.7.10

- Update to senzing/senzing-base:1.4.0

## [1.7.9] - 2019-11-13

### Changes in 1.7.9

- Added support for MS SQL in Dockerfile by upgrading to senzing/senzing-base:1.3.0
- Updated jackson-databind to version 2.9.10.1

## [1.7.8] - 2019-10-14

### Changes in 1.7.8

- Changes to support RPM file layout on Linux for auto tests including support.
- Added JSON pretty-printing option to JsonUtils' toJsonText() functions
- Removed Dockerfile-package

## [1.7.7] - 2019-09-30

### Changes in 1.7.7

- Fixed auto tests to skip instead of fail if Senzing native libraries are not
available.
- Fixed output when command line options do not provide initialization
parameters

## [1.7.6] - 2019-09-25

### Changes in 1.7.6

- Updated dependency on FasterBind's Jackson library to version 2.9.10 to
address security vulnerabilities.
- Added missing G2ConfigMgr.destroy() call in SzApiServer during shutdown
- Updated repository manager code used for JUnit tests to check for errors
when initializing the configuration manager and when creating the standard
configuration.
- Changes to Unit Tests:
  - Updated unit tests to preserve repos if any tests associated with that
    repo failed.
  - Updated location of unit test entity repos to live in the
    `./target/test-repos` directory during a Maven build and modified the
    repo directory names to be based off the associated unit test name.
  - Updated the module name used for Senzing initialization in auto tests to
    match the current auto test for post-failure diagnostic purposes.
  - Added forced preservation of unit test entity repos passing the
    `-Dsenzing.preserve.test.repos=true` option to Maven.

## [1.7.5] - 2019-09-17

### Changes in 1.7.5

- Corrected errant definition of SzVersionInfo's `configCompatabilityVersion`
field as an integer to make it a string to allow for semantic versioning.  This
changes the response to the `GET /version` endpoint.
*NOTE*: This change may require that previously generated client stubs be
regenerated to avoid trying to parse the semantic version string as an integer.

## [1.7.4] - 2019-09-13

### Changes in 1.7.4

- Less repetitive/verbose output when invalid command line options are provided.
- Fixes auto tests when building with 1.11.1.x versions of native Senzing libs.

## [1.7.3] - 2019-08-30

### Changes in 1.7.3

- Fixed bug where the initialization of the configuration manager
(`G2ConfigMgr.initV2()`) was not checked for success or failure.  Now the
API server ensures that initialization succeeded before proceeding further.
- Removed warnings that could occur if building with version 1.11.x of g2.jar

## [1.7.2] - 2019-08-19

### Added in 1.7.2

- Added `--configmgr` option to handle managing configurations when initializing
with JSON and leveraging the configuration from the database.  This includes the
ability to migrate an INI file to JSON and conditionally upload the referenced
configuration file to the database and make it the default (see: `--migrateIni`)
- Added the ability to auto reinitialize the configuration with the latest
default configuration for the repository if the default configuration changes
while the API server is running.  This is monitored for changes every 10 seconds
and will be checked on demand if specific G2Engine functions fail.
- Added the `-configId` option to lock the API server to a specific
configuration.  When this option is used then the auto reinitialization does not
occur since the chosen configuration is likely not be the default configuration.
- Added `-readOnly` command-line option to cause the `PUT` and `POST` endpoints
for loading records to always return an `HTTP 403 Forbidden` response.
- Now a non-root, immutable container.
- RPM based installation.

### Changed in 1.7.2

- senzing/senzing-api-server:1.7.2 pinned to senzing/senzing-base:1.2.1
- Modified `SzResolvedEntity` so the `relationshipData` is populated from the
features and added auto tests to verify.  Note: this will not be provided if
`featureMode` is set to `NONE`.
- Modified `SzResolvedEntity` so the `otherData` is populated from the records
and added auto tests to verify.  Note: this will not be populated if
`forceMinimal` is true since no records will be retrieved.
- Internally upgraded the processing of command line arguments to reuse the same
functions for the ConfigurationManager and SzApiServer classes as well as the
intenral RepositoryManager class (used in auto tests).
- Fixed Junit auto tests with windows (EntityGraphServicesTest) -- worked around
libpostal bug in native Senzing API (Windows version).

### Deprecated in 1.7.2

- Deprecated `-iniFile` option in favor of newly added `-initFile`,
`-initEnvVar` and `-initJson` options to initialize with JSON instead of an INI
file.

## [1.7.1] - 2019-08-07

### Changed in 1.7.1

- Modified Makefile to disable Junit tests during Docker build until failures
specific to the Docker build can be diagnosed.

### Security in 1.7.1

- Upgraded third-party dependencies to get security patches

## [1.7.0] - 2019-07-22

### Added in 1.7.0

- Added `GET /version` endpoint to get detailed version information
- Added `GET /config/current` to get the raw configuration JSON that is
currently being used by the API Server
- Added `GET /config/default` to get a default bootstrap configuration JSON
file that can be used to compare versus the current configuration.  NOTE: this
is NOT the same as the "default configuration" in the G2ConfigMgr API, but
rather represents a brand new out-of-the-box configuration.
- Added Junit Jupiter auto tests to verify all functionality and fixed entity
path "including source" functions which were broken (as exposed by tests).
- Now requires g2.jar version 1.10.x or higher

## [1.6.1] - 2019-06-10

### Added in 1.6.1

- Merge pull request #52 from Senzing/issue-51.caceres.record-resolutio…
- …n-fields
- Added SzMatchedRecord which extends SzEntityRecord to resolve issue 51
- Added `SzMatchedRecord`, extending `SzEntityRecord`, to add match score
information to matched records in an `SzResolvedEntity`.  Modified
`SzResolvedEntity` to use `SzMatchedRecord` instead of `SzEntityRecord` for its
record list.
