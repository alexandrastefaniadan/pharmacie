package ma.pharmacie.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata exposed at {@code /v3/api-docs} and rendered by Swagger UI
 * at {@code /swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pharmacieOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pharmacie API")
                        .description("REST API for the pharmacist helper application.")
                        .version("v1")
                        .contact(new Contact().name("Pharmacie").email("contact@pharmacie.local"))
                        .license(new License().name("Proprietary")));
    }
}

