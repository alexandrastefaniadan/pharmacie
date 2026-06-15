package ma.pharmacie.treatment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ma.pharmacie.common.enums.UsageType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Payload to create a new treatment.
 *
 * <p>{@link #usageType} is required and locks the type of medications that
 * can be attached: every id in {@link #medicationIds} must point at a
 * medication of the same usage type (the service rejects mixed bundles).
 *
 * <p>{@link #medicationIds} is sent as an ordered list so the order the
 * pharmacist entered (1st-line, 2nd-line, accompanying) is preserved.
 */
@Schema(description = "Body to create a treatment.")
public record TreatmentCreateRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        String notes,
        @NotNull UsageType usageType,
        @Schema(description = "Ordered list of medication ids. At least one is required.")
        @NotEmpty List<UUID> medicationIds,
        @Schema(description = "Optional symptom tags — reuse the same lookup as on medications.")
        Set<Integer> indicationIds
) {
}

