package ch.ubique.backend.geo.common.exceptions;

import org.apache.commons.lang3.exception.UncheckedException;
import org.geotools.api.referencing.operation.TransformException;

public class UncheckedTransformException extends UncheckedException {

    public UncheckedTransformException(final TransformException cause) {
        super(cause);
    }
}
