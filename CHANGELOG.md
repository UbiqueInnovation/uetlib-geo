# CHANGELOG

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
