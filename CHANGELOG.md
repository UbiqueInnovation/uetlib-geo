# CHANGELOG

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
