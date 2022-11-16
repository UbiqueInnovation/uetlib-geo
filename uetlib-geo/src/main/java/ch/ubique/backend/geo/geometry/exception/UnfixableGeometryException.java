package ch.ubique.backend.geo.geometry.exception;

import org.locationtech.jts.operation.valid.TopologyValidationError;

public class UnfixableGeometryException extends Exception {

    public UnfixableGeometryException(TopologyValidationError validationError) {
        super(validationError.getMessage());
    }
}
