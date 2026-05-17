package ma.pharmacie.common.exception;

/**
 * Thrown when a request would create a state conflict, e.g. a duplicate unique key.
 * Mapped to HTTP 409 by {@link GlobalExceptionHandler}.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

