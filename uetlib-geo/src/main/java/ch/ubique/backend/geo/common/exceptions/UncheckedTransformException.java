package ch.ubique.backend.geo.common.exceptions;

import org.apache.commons.lang3.exception.UncheckedException;

public class UncheckedTransformException extends UncheckedException {

    public UncheckedTransformException(final Exception cause) {
        super(cause);
    }
}
