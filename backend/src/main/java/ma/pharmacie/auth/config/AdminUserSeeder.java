package ma.pharmacie.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.pharmacie.auth.entity.AppUser;
import ma.pharmacie.auth.repo.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Creates the initial admin user on startup if no user exists yet. The
 * credentials are taken from environment variables {@code ADMIN_USERNAME} and
 * {@code ADMIN_PASSWORD}, so they never live in source control.
 *
 * <p>In the {@code dev} profile, sensible defaults are used to make local
 * onboarding effortless: {@code admin / admin}. <strong>Do not</strong> rely
 * on these in production.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminUserSeeder {

    @Bean
    public ApplicationRunner seedAdminUser(AppUserRepository users,
                                           PasswordEncoder encoder,
                                           @Value("${app.security.admin-username:admin}") String username,
                                           @Value("${app.security.admin-password:admin}") String password) {
        return args -> {
            if (users.count() > 0) {
                log.debug("Users already exist – skipping admin seeding.");
                return;
            }
            AppUser admin = AppUser.builder()
                    .username(username)
                    .passwordHash(encoder.encode(password))
                    .role("ADMIN")
                    .enabled(true)
                    .build();
            users.save(admin);
            log.warn("Seeded initial admin user '{}'. CHANGE THE PASSWORD if it is the default.", username);
        };
    }
}

