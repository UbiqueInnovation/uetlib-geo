# CHANGELOG

## 4.1.0

- `uetlib-parent` updated to
  `2026.11.5`: [Changelog/2026.11.5](https://github.com/UbiqueInnovation/uetlib-parent/blob/main/CHANGELOG.md#2026115) [Diff/2026.11.1-2026.11.5](https://github.com/UbiqueInnovation/uetlib-parent/compare/v2026.11.1...v2026.11.5)
- `uetlib-core` updated to
  `3.1.0`: [Changelog/3.1.0](https://github.com/UbiqueInnovation/uetlib-core/blob/main/CHANGELOG.md#310) [Diff/3.0.1-3.1.0](https://github.com/UbiqueInnovation/uetlib-core/compare/v3.0.1...v3.1.0)
- `uetlib-db` updated to
  `3.1.0`: [Changelog/3.1.0](https://github.com/UbiqueInnovation/uetlib-db/blob/main/CHANGELOG.md#310) [Diff/3.0.1-3.1.0](https://github.com/UbiqueInnovation/uetlib-db/compare/v3.0.1...v3.1.0)
- `uetlib-job` updated to
  `6.1.0`: [Changelog/6.1.0](https://github.com/UbiqueInnovation/uetlib-job/blob/main/CHANGELOG.md#610) [Diff/6.0.2-6.1.0](https://github.com/UbiqueInnovation/uetlib-job/compare/v6.0.2...v6.1.0)
- `uetlib-spring-boot` updated to
  `4.1.0`: [Changelog/4.1.0](https://github.com/UbiqueInnovation/uetlib-spring-boot/blob/main/CHANGELOG.md#410) [Diff/4.0.2-4.1.0](https://github.com/UbiqueInnovation/uetlib-spring-boot/compare/v4.0.2...v4.1.0)
- `uetlib-test` updated to
  `3.1.0`: [Changelog/3.1.0](https://github.com/UbiqueInnovation/uetlib-test/blob/main/CHANGELOG.md#310) [Diff/3.0.1-3.1.0](https://github.com/UbiqueInnovation/uetlib-test/compare/v3.0.1...v3.1.0)
- Updated dependencies and actions.

- Fix the warnings of Error Prone's default checks: TLM values and enum names convert case with `Locale.ROOT`,
  `VectorTileGenerator` no longer discards task failures, `ArrayList` and `ArrayDeque` replace `LinkedList`,
  comparators use primitive keys, and `@NonNull` is removed from primitive `RailwayInfo` components.

## 4.0.5

- Build pull requests, including forks, and pushes to `main` instead of every branch push; use shared Maven
  workflows at `v1.1.1`.
- Use shared Maven workflows at `v1.1.0`: wait for the SonarCloud quality gate and analyse with the
  uetlib-only Sonar token.
- Use shared Maven build and publish workflows at `v1.0.0` to enforce formatting checks in CI.
- Use `uetlib-core` 3.0.1; the referenced 3.0.0 was never published to Central.

## 4.0.4

- Replace the disabled internal Geofabrik download test with a small synthetic offline OSM fixture and graph assertions.

## 4.0.3

- Document attribution for the vector tile schema.

## 4.0.2

- Move `uetlib-spring-boot-geo` into the geo repository as `uetlib-geo-spring-boot`, with packages under
  `ch.uetlib.geo.spring.boot`; update consumers and migration mappings.

- Move Java packages and class references from `ch.ubique.uetlib` to `ch.uetlib`.

- Rename geo integration packages to `ch.uetlib.geo.db` and `ch.uetlib.geo.job`.

### Breaking changes

- Move Java packages from `ch.ubique.backend` to `ch.uetlib`, including class-name resources. The combined OpenRewrite
  migration also renames known Spring configuration keys to `ch.uetlib.*`.
- Remove the inherited Ubique Artifactory repository and deployment destination; private consumers must configure their
  own registry.
- Add Apache-2.0 licensing for original code, preserve third-party licenses, and add public project metadata and
  contribution/security guidance.

- Move Maven coordinates from `ch.ubique:uetlib-*` to `ch.uetlib:uetlib-*`.
- Align uetlib parent, BOM and library references with the migration releases.

### Migration

After the migration releases and `uetlib-rewrite` are published, run this from the consuming project, then review the
diff and run its tests:

```sh
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run \
  -Drewrite.recipeArtifactCoordinates=ch.uetlib:uetlib-rewrite:RELEASE \
  -Drewrite.activeRecipes=ch.uetlib.MigrateToPublicUetlib
```

The recipe updates Maven coordinates/versions, Java references and known class-name resources. Review application-owned
packages under the old namespace, Kotlin, reflection patterns, native-access module flags and persisted class names
separately. Use `:dryRun` instead of `:run` to preview a patch.

## 3.4.0

### New Features

- Add GeoBuf encoding with typed properties, feature IDs, CRS metadata and configurable coordinate precision.

## 3.3.0

### Improvements

- Add `SemicircleUtil` for converting degrees, radians and semicircle units used by GPS devices and protocols, including
  Garmin-compatible signed 32-bit encoding and decoding.
- version updates

## 3.2.5

### Improvements

- Use consistent representative coordinates for OSM ways and areas.
- Use the Ubique Geofabrik mirror.

## 3.2.4

- latest uetlib parent
- version updates

## 3.2.3

- Nullability for `GdalWarpOptions` setters.

## 3.2.2

- updated actions to not run tests twice for sonar
- update uetlib version

## 3.2.1

- Amendments to new smart drawing protobuf format. Technically, a format breaking change.

## 3.2.0

- Added a new smart drawing protobuf format, see [smart_drawing.proto](uetlib-geo/src/main/proto/smart_drawing.proto)

## 3.1.1

- Added `distanceComparator` and `streamSortedByDistance` to `WGS84SphericalDistanceCalculator`

## 3.1.0

- `BaseRoutingGraphGeneratorJob` is now based on `BaseStateJob`

## 3.0.0

### Features

- Use Jspecify and `@NullMarked` for module

### Breaking Changes

- Removed deprecated `GeoPackageParser`
- Some method signatures changed for nullability compliance

## 2.2.0

### Improvements

- Close GeoTools readers, iterators and writers deterministically to avoid resource leaks, and add
  lifecycle-safe `GeotoolsUtils.withReader/withIterator` helper APIs with tests.
- Add usage guidance comments: prefer `withReader`/`withIterator`; `stream(...)` variants should be
  used only when the returned stream is explicitly closed.

## 2.1.0

### Updates

- Update `uetlib-job` to `3.1.0`

## 2.0.0

### Breaking Changes

- `ObjectMapperUtils` replaced by `JsonMapperUtils`. This one should not be used directly anymore in
  a Spring Boot App
  to create a bean. For that, use the new autoconfiguration in module `uetlib-spring-boot-geo`
  instead.
  registers the necessary `JsonMapper` config and your config has to define one of the available
  `JsonMapperBuilderCustomizer`:
  ```diff
  - @Bean
  - public ObjectMapper objectMapper() {
  -     return ObjectMapperUtils.jsonMapperLv95();
  - }
  + @Bean
  + public JsonMapperBuilderCustomizer objectMapper() {
  +     return new UetlibJacksonGeoJtsLv95JsonMapperBuilderCustomizer();
  + }
  ```
  If you have additional customizations, add them as separate `JsonMapperBuilderCustomizer` beans.
  All these customizers are applied to the Spring Boot autoconfigured bean.

## 1.1.10

### Improvements

- `JtsConverter.transform` now defaults to transforming the geometry without intersecting it with
  coordinate system boundaries.

## 1.1.9

### Improvements

- Added `GdbConverter`

## 1.1.8

### Improvements

- Added `GeoPackageSqlReader` to read non-geometry data from a GeoPackage.

## 1.1.7

### Improvements

- Added `parseFeatures` to `ShapefileParser`, that directly parses the features without creating
  in-memory files. Should be prefered over `ShapefileParser.loadData`.

## 1.1.6

### Improvements

- `JtsUtil` now has a `toMultiGeometry` method.

## 1.1.5

### Improvements

- `JtsUtil` now has a `representativeCoordinate(Geometry)` method.

## 1.1.4

### Improvements

- `KmlParser` parses automatically closes linear rings if necessary.

## 1.1.3

### Improvements

- `KmlParser` parses geometries instead of just coordinates.

## 1.1.2

### Improvements

- Added surface type to the routing drawing graph.

## 1.1.1

### Bugfixes

- Creation of MBTiles works again

## 1.1.0

### Improvements

- Latest uetlib dependencies

## 1.0.10

- Added `OsmParserWithStore` that goes over OSM files in three passes and stores coordinates, to
  later extract geometries for ways and relations.
- Added `GraphGeopackageEncoder` and `RoutingGraphGeopackageEncoder` for writing graphs to
  GeoPackage.
  This can be useful for visualizing with e.g., QGIS.
- Added `RoutingGraphTileParser` for creating routing graphs directly from protobuf files.

## 1.0.9

- Added `Dijkstra` for searching graphs.

## 1.0.8

- `LinesGraphBuilder` optimized performance when snapping tolerance is 0.

## 1.0.7

- `LinesGraphBuilder` now supports both adding directed and undirected edges.

## 1.0.6

- Vector Tiles: Allow encoding values as signed integers based on a config property

## 1.0.5

- Vector Tile Features do not require and `id` to be set. That's why a default implementation and a
  new constructor are now provided.

## 1.0.4

- Added new methods to `JtsUtil`: `midPoint(LineString)`, `midPointSegment(LineString)` and
  `closestSegment(LineString, Coordinate)`
- Added `DuplicateAwareTypedKdTree`

## 1.0.3

- Added `WGS84SphericalDistanceCalculator.bearingAngle`

## 1.0.2

- Added more distance calculations to `WGS84SphericalDistanceCalculator`
- Dependency updates

## 1.0.0

- Initial release
