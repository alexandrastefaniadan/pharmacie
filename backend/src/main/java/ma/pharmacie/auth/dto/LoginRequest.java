package ma.pharmacie.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Login request body. */
@Schema(description = "Login credentials.")
public record LoginRequest(
        @NotBlank @Schema(example = "alex") String username,
        @NotBlank @Schema(example = "••••••••") String password
) {
}

