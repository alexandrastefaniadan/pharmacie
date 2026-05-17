/**
 * Cross-cutting concerns shared across the application:
 * <ul>
 *   <li>{@code config} — application-wide Spring configuration (CORS, OpenAPI, JPA auditing).</li>
 *   <li>{@code audit}  — base classes for JPA auditing.</li>
 *   <li>{@code exception} — custom exceptions and the global {@code @RestControllerAdvice}
 *       producing RFC 7807 problem responses.</li>
 * </ul>
 */
package ma.pharmacie.common;

