package ma.pharmacie.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Identity payload returned by {@code GET /auth/me} and after a successful login. */
@Schema(description = "Currently authenticated user.")
public record UserInfo(
        @Schema(example = "alex") String username,
        @Schema(example = "[\"ADMIN\"]") List<String> roles
) {
}

