package ma.pharmacie.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body for {@code POST /api/v1/auth/change-password}. The new password is
 * validated for minimum length here; the controller additionally verifies
 * that the current password is correct before persisting the new hash.
 */
@Schema(description = "Change-password payload.")
public record ChangePasswordRequest(
        @NotBlank @Schema(example = "currentSecret123") String currentPassword,
        @NotBlank @Size(min = 10, max = 100, message = "Le nouveau mot de passe doit comporter au moins 10 caractères.")
        @Schema(example = "newSecret456!") String newPassword
) {
}

