# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
[markdownlint](https://dlaa.me/markdownlint/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.2.0] - 2022-08-17

### Changed in 3.2.0

- Added new `ATTRIBUTED` value for `SzFeatureMode` for pulling back feature
  references at the record level to indicate which records contributed which
  features:
  - Added `SzFeatureReference` schema definition
  - Added `featureReferences` property to `SzEntityRecord`
- Added `GET /virtual-entities` operation definition.
  - Added `SzVirtualEntityResponse` schema definition
  - Added `SzVirtualEntityData` schema definition
  - Added `SzRecordIdentifier` schema definition
  - Added `SzRecordIdentifiers` schema definition
- Added operations to support "how entity" functionality:
  - Added `GET /entities/{entityId}/how` operation definition
  - Added `GET /data-sources/{dataSourceCode}/records/{recordId}/entity/how`
    operation definition
  - Added `SzHowMatchInfo` schema definition
  - Added `SzVirtualEntityRecord` schema definition
  - Added `SzVirtualEntity` schema definition 
  - Added `SzResolutionStep` schema definition
  - Added `SzHowEntityResult` schema definition
  - Added `SzHowEntityResponse` schema definition
- Updated `GET /entity-paths` parameter documentation

## [3.1.0] - 2022-08-09

### Changed in 3.1.0

- Added `SzDetailLevel` enumeration for detail levels
- Added `detailLevelQueryParam` reference to operations that also took
  `featureMode` parameter to allow detail level specification with default
  value of `VERBOSE` to match pre-existing behavior for backwards compatibility.
- Updated documentation of `SzRelationshipMode` and `withRelated` parameter to
  reflect interdependence with `detailLevel`.
- Updated documentation of `partial` flag for `SzResolvedEntity` to reflect how
  it might be set to `true` depending on the specified `detailLevel`.

## [3.0.0] - 2022-05-04

### Changed in 3.0.0

- Removed endpoints pertaining to Entity Types
- Removed endpoints pertaining to Entity Classes
- Removed schema objects for Entity Types
- Removed schema objects for Entity Classes
- Removed `SzBaseRelatedEntity.matchScore` since `MATCH_SCORE` removed in 3.0
- Removed `SzMatchedRecord.refScore` since `REF_SCORE` removed in 3.0
- Removed `SzBaseRelatedEntity.refScore` since `REF_SCORE` removed in 3.0

## [2.7.1] - 2022-01-12

### Changed in 2.7.1

- Changes to bulk-data operation examples for improved documentation

## [2.7.0] - 2021-07-22

### Changed in 2.7.0

- Added `GET /` endpoint that mimics the functionality of `GET /heartbeat`
- Added `GET /specifications/open-api` endpoint to get the Open API
  specification as JSON data.  The optional `?asRaw=true` query parameter
  promotes the Open API JSON from the `data` property of the response to the
  root of the response.
- Added 'infoQueueConfigured' property to SzServerInfo  
- Modified inline data segments to be first-class named types.

## [2.6.0] - 2021-04-23

### Changed in 2.6.0

- Added `webSocketsMessageMaxSize` to `GET /server-info`'s `SzServerInfo`
  response.
- Added `eofSendTimeout` parameter to `POST /bulk-data/analyze` and 
  `POST /bulk-data/load` endpoints for Web Sockets support.
- Added missing documentation of the `maxFailures` query parameter for
  `POST /bulk-data/load` endpoint.
- Added information for invoking Bulk Data endpoints via Web Sockets.
- Added detailed descriptions for all operations.
- Added additional documentation tying the `characteristicData` field to the
  `ATTRIBUTE_DATA` field in the raw data.

## [2.5.0] - 2021-03-24

### Changed in 2.5.0

- Added `nativeApiBuildVersion` to meta (`SzMeta`)
- Added `nativeApiBuildVersion` to `SzVersionInfo`
- Added `POST /search-entities` endpoint to provide a `POST` variant of
  `GET /entities` that uses the request body instead of the `attr` and `attrs`
  query parameters
- Fixed type of `progressPeriod` parameter

## [2.4.0] - 2021-03-11

### Changed in 2.4.0

- Added `includeOnly` query parameter to `GET /entities` endpoint.  NOTE: this
  parameter is only recognized if the underlying native Senzing API Product
  is version 2.4.1 or later.
- Modified `SzBaseResponse` to include four new fields to be included in the 
  meta section of each response:
  - `nativeApiVersion`
  - `nativeApiBuildNumber`
  - `nativeApiBuildDate`
  - `configCompatibilityVersion`
- Fixed definition of `SzFlaggedRecord` so `dataSource` is defined as a string

## [2.3.0] - 2020-11-18

### Changed in 2.3.0

- Added `GET /why/entities` operation
- Modified `SzMatchInfo` to support disclosed relationships
- Added `SzWhyEntitiesResponse` model schema
- Added `SzWhyEntitiesResult` model schema
- Added `SzDisclosedRelation` model schema
- Added `SzRelationDirection` model schema
- Added `SzRelatedFeatures`  model schema
- Updated some documentation for clarification.

## [2.2.0] - 2020-10-15

### Changed in 2.2.0

- Added `SzNameScoring` to describe name scoring details
- Added `SzSearchFeatureScore` for search feature scores
- Modified `SzBaseRelatedEntity` to remove `fullNameScore` field since it has
not been populated since switch to version 2.0.0 of native Senzing SDK and
never made sense in the "base class" since only `SzAttributeSearchResult` had
this field populated under native Senzing SDK version 1.x.
- Added `bestNameScore` field to `SzAttributeSearchResult` to replace
`fullNameScore` in the place where the name score was previously
used with version 1.x of the native Senzing SDK (i.e.: to sort search results
based on the strength of the name match).
- Modified `SzAttributeSearchResult` to include the `featureScores` field to
provide feature scores without using "raw data"
- Added `nameScoringDetails` field to `SzFeatureScore` class to provide
`SzNameScoring` name scoring details on why operations,
- Updated `com.senzing.api.model.SzFeatureScore` to define its `score` field as 
the most sensible score value from the `nameScoringDetails` for `"NAME"`
features since the `FULL_SCORE` field is not available for names.
- Updated version numbers to 2.2.0

## [2.1.1] - 2020-10-06

### Changed in 2.1.1

- Added the `NOT_SCORED` value for `SzScoringBucket` enum.

## [2.1.0] - 2020-10-01

### Changed in 2.1.0

- Added `SzMatchLevel` type to enumerate and describe the various match levels.
- Added `matchLevel` field to `SzMatchInfo` to include the match level from
  `MATCH_LEVEL_CODE` in the responses from the "why" endpoints.
- Added `lastSeenTimestamp` field to `SzResolvedEntity` and `SzEntityRecord` to
  include `LAST_SEEN_DT` field in the primary schema rather than only the raw.

## [2.0.0] - 2020-07-13

### Changed in 2.0.0

- Added `withInfo` and `withRaw` query parameters to following endpoints:
  - `POST /data-sources/{dataSourceCode}/records`
  - `PUT /data-sources/{dataSourceCode}/records/{recordId}`

- Added `DELETE /data-sources/{dataSourceCode}/records/{recordId}` endpoint
  including `withInfo` and `withRaw` query parameters.

- Added the following endpoints to reevaluate entities or specific records
  including `withInfo` and `withRaw` query parameters:
  - `POST /data-sources/{dataSourceCode}/records/{recordId}/entity/reevaluate`
  - `POST /reevaluate-entities`

- Added many examples to use with Swagger Editor
  - NOTE: some limitations and bugs with Swagger editor make some of these
    difficult to use with the “Try it Out” function — for example, necessary
    line breaks are removed from “JSON lines” input files when curl commands
    are generated.

- Potentially Backward-Compatibility Breaking Changes by API Endpoint:
  - `GET /data-sources/{dataSourceCode}/records/{recordId}`
    - Modified the response to match the Senzing REST API specification so that
      the `data` field is no longer an instance of `SzRecordResponse` but is
      rather an object with a `record` field of type `SzRecordResponse`.
      - *MIGRATION*: Change direct references to the `data` field to instead
        reference `data.record`.
  - `GET /entity-classes`
  - `GET /entity-classes/{entityClassCode}`
  - `POST /entity-types`
  - `POST /entity-classes/{entityClassCode}/entity-types`
  - `GET /entity-classes/{entityClassCode}/entity-types/{entityTypeCode}`
    - Removed support for any entity class other than ACTOR as it was discovered
      that the underlying product does not properly support entity resolution
      when using entity classes other than ACTOR and it may not for some time.
      This will change if and when additional entity classes are supported.
      - *MIGRATION*: Ensure the value provided for all entity classes are
        changed to `ACTOR`.

  - `POST /entity-classes`
    - Removed this operation as it was discovered that the underlying product
      does not fully properly support entity resolution when using entity
      classes other than ACTOR and it may not for some time.  This will change
      if and when additional entity classes are supported.
      - *MIGRATION*: Remove calls to create new entity classes and instead use
        the default entity class of `ACTOR`.

  - `GET /entities/{entityId}`
  - `GET /data-sources/{dataSourceCode}/records/{recordId}/entity`
    - The `withRelated` parameter is no longer a `boolean` value that accepts
      `true` or `false`.  It now accepts an enumerated value of `NONE`,
      `PARTIAL` or `FULL`.
      - *MIGRATION*: Use `?withRelated=true` if relationships are desired.
    - The `attributeData` field of `com.senzing.api.model.SzResolvedEntity` was
      renamed to `characteristicData` to match the API spec.
      - *MIGRATION*: Use `?withRelated=FULL` in place of `?withRelated=true`
        and use `?withRelated=PARTIAL` in place of `?withRelated=false`.

  - `GET /config/current`
    - Renamed to `GET /configs/active` since “current” is ambiguous with regards
      to the “currently active config” versus the configuration managers
      currently configured “default config”.
      - *MIGRATION*: Use the `/configs/active` path in place of
        `/config/current`.

  - `GET /config/default`
    - Renamed to `GET /configs/template` since “default” is ambiguous with the
      configuration managers “default config” setting.
      - *MIGRATION*: Use the `/configs/template` path in place of
        `/config/default`.

  - `GET /entities`
    - Removed the `attr_[PROPERTY_NAME]` parameters and replaced with the
      multi-valued `attr` parameter so that this parameter could better be
      documented in the Open API Spec and examples provided via Swagger Editor.
      - *MIGRATION*: Use `?attr=NAME_FIRST:Joe` in place of
        `?attr_NAME_FIRST=Joe` or use the `attrs` parameter with a JSON value.
    - The `withRelationships` query parameter now defaults to `false` instead
      of `true`.
      - *MIGRATION*: Use `?withRelationships=true` if relationships are desired.

  - `GET /entity-networks`
    - Changed the default value for `maxDegrees` parameter from 5 to 3

  - `POST /bulk-data/load`
    - Replaced the `dataSource_[DATA_SOURCE_CODE]` parameters with the
      multi-valued `mapDataSource` parameter so that this parameter
      could better be documented in Open API Spec and examples provided via
      Swagger Editor.
      - *MIGRATION*: Use `?mapDataSource=FOO:BAR` in place of
        `?dataSource_FOO=BAR` or use the new `mapDataSources` parameter instead.
    - Replaced the `entityType_[ENTITY_TYPE_CODE]` parameters with the
      multi-valued `mapEntityType` parameter so that this parameter could
      better be documented in Open API Spec and examples provided via
      Swagger Editor.
      - *MIGRATION*: Use `?mapEntityType=FOO:BAR` in place of
        `?entityType_FOO=BAR` or use the new `mapEntityTypes` parameter instead.

- Other Changes by API Endpoint:
  - `GET /license`
    - Added the previously undocumented (but always-supported) the “withRaw”
      parameter to the specification.

  - `GET /version`
    - Added the previously undocumented (but always-supported) the “withRaw”
      parameter to the specification.

  - `POST /bulk-data/load`
    - Added the single-valued `mapDataSources` parameter which accepts
      URL-encoded JSON to map the original data sources to target data
      sources.
    - Added the single-valued `mapEntityTypes` parameter which accepts
      URL-encoded JSON to map the original entity types to target entity
      types.

## [1.8.2] - 2020-07-08

### Changed in 1.8.2

- Improved documentation
- Works with senzing versions up to 1.15.6
- Not supported for senzing version 2.0.0 and above

## [1.8.1] - 2020-04-15

### Changed in 1.8.1

- Added WHY operations
  - `GET /data-sources/{dataSourceCode}/records/{recordId}/entity/why`
  - `GET /entities/{entityId}/why`
  - `GET /why/records`
- Added support for the `withFeatureStatistics` and `withDerivedFeatures`
  parameters across the following endpoints:
  - `GET /data-sources/{dataSourceCode}/records/{recordId}/entity`
  - `GET /data-sources/{dataSourceCode}/records/{recordId}/entity/why`
  - `GET /entities/{entityId}`
  - `GET /entities/{entityId}/why`
  - `GET /why/records`
  - `GET /entity-paths`
  - `GET /entity-networks`
- Added the `featureDetails` property to entity results to support obtaining
  the feature ID as well as the feature statistics (if requested).

## [1.8.0] - 2020-03-27

### Changed in 1.8.0

- Adds config modification operations (data sources, entity types, etc...)
- Adds bulk data analyze and load operations

## [1.7.5] - 2019-09-17

### Changes in 1.7.5

- Changed `configCompatibilityVersion` field in `GET /version` endpoint to be
a string rather than an integer.  When it was specified as an integer, this
was a mistake in the original release.  NOTE: this will require that client
code be regenerated.
