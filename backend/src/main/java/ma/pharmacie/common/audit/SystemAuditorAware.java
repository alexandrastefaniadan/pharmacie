package ma.pharmacie.common.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides the current user for JPA auditing.
 *
 * <p>For now we have no auth, so we return a fixed value ({@code "system"}).
 * When Spring Security / Keycloak are added, replace this with one that reads
 * the authenticated principal from {@code SecurityContextHolder}.
 */
@Component("auditorAware")
public class SystemAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("system");
    }
}

