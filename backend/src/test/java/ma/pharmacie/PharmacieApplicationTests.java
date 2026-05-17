package ma.pharmacie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Smoke test: boots the full Spring context against a real PostgreSQL
 * (via Testcontainers) and runs Flyway migrations. Catches schema errors
 * and wiring problems before they hit a developer machine.
 */
@SpringBootTest
@Testcontainers
class PharmacieApplicationTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("pharmacie")
                    .withUsername("pharmacie")
                    .withPassword("pharmacie");

    @DynamicPropertySource
    static void dataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void contextLoads() {
        // Spring context bootstrap + Flyway migrations succeed.
    }
}

