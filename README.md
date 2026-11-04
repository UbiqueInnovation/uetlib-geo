# Uetlib Geo

Ubique Library for Geo Spatial Data

## Changelog

See [CHANGELOG.md](CHANGELOG.md)

## GPS angle conversions

`ch.uetlib.geo.coordinate.util.SemicircleUtil` converts degrees and radians
into semicircle units (one semicircle is 180 degrees), and back. Semicircles are
used beyond Garmin and FIT: GPS navigation messages also express orbital angles
in this unit. Their field scales differ, so a GPS protocol using semicircles does
not necessarily use Garmin's 32-bit encoding. See the
[GPS interface specification](https://www.gps.gov/technical/icwg/IS-GPS-200M.pdf).

The double-valued `degreesToSemicircles`, `semicirclesToDegrees`,
`radiansToSemicircles` and `semicirclesToRadians` methods are independent of the
protocol's integer field scale and can be used when integrating other GPS devices
and protocols.

For Garmin positions and FIT coordinates, or another protocol that explicitly
uses the same signed 32-bit scale, use the `…Semicircles32` / `semicircles32…`
methods. Garmin documents this format in its
[Semicircles library](https://github.com/garmin/connectiq-apps/blob/master/barrels/Semicircles/README.md):

```java
int encodedLatitude = SemicircleUtil.degreesToSemicircles32(47.3769);
double latitude = SemicircleUtil.semicircles32ToDegrees(encodedLatitude);
```

The 32-bit encoding uses 2^31 units per semicircle, truncates towards zero and
accepts angles from -180 to +180 degrees (or -pi to +pi radians). Both endpoints
encode as `Integer.MIN_VALUE` and decode to -180 degrees. Non-finite and out-of-range
input is rejected. Validate latitude bounds and handle any protocol-specific
reserved values separately.

## License

Except where otherwise noted, original uetlib code is licensed under the [Apache License 2.0](LICENSE). Third-party files retain their original license terms; see [NOTICE](NOTICE) and any third-party notices in this repository.
