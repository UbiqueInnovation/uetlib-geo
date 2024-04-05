package ch.ubique.backend.geo.tile.vector.models.tlm;

public sealed interface TlmInfo permits TlmPathInfo, RailwayInfo, UnstructuredTlmInfo {}
