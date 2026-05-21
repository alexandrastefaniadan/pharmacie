package ma.pharmacie.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Translates exceptions into RFC 7807 problem-detail JSON responses.
 *
 * <p>Always returns {@code application/problem+json} with at minimum:
 * {@code type}, {@code title}, {@code status}, {@code detail}, {@code timestamp}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- 404 ----
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), "not-found");
    }

    // ---- 409 ----
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), "conflict");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "Data integrity violation",
                "The request conflicts with existing data (likely a duplicate or invalid reference).",
                "data-integrity");
    }

    // ---- 400: bean validation on @RequestBody ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBeanValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(),
                        fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()));
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more fields are invalid.", "validation");
        pd.setProperty("errors", errors);
        return pd;
    }

    // ---- 400: bean validation on @RequestParam / @PathVariable ----
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), "validation");
    }

    // ---- 400: malformed JSON ----
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request body",
                "The request body could not be parsed as JSON.", "malformed-json");
    }

    // ---- 400: bad parameter type (e.g. invalid UUID in path) ----
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = "Parameter '%s' has an invalid value: %s".formatted(ex.getName(), ex.getValue());
        return problem(HttpStatus.BAD_REQUEST, "Invalid parameter", msg, "invalid-parameter");
    }

    // ---- 400: bad arguments coming from services ----
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage(), "bad-request");
    }

    // ---- 413: upload too large ----
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "File too large",
                "The uploaded file exceeds the allowed size.", "upload-too-large");
    }

    // ---- pass-through for explicit ResponseStatusException (401, 403, …) ----
    // Without this, the catch-all below would log them as "Unhandled" and
    // return a misleading 500 to the client. Reason phrase is used as detail
    // when available, otherwise we fall back to a sensible message.
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String detail = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        String slug = status.name().toLowerCase().replace('_', '-');
        return problem(status, status.getReasonPhrase(), detail, slug);
    }

    // ---- 500: catch-all ----
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "An unexpected error occurred. Please retry later.", "internal");
    }

    // ---- helpers ----
    private static ProblemDetail problem(HttpStatus status, String title, String detail, String slug) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("https://pharmacie.local/errors/" + slug));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
