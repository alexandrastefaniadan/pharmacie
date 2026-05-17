package ma.pharmacie.common.exception;

/**
 * Thrown when a requested resource does not exist (or has been soft-deleted).
 * Mapped to HTTP 404 by {@link GlobalExceptionHandler}.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException("%s with id '%s' not found".formatted(entity, id));
    }
}

