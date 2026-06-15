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
 * Full-replacement update payload (PUT semantics). {@code version} is required
 * for optimistic locking. Changing {@link #usageType} is allowed only when the
 * new medication list is fully coherent with the new type — same rule as on
 * create.
 */
@Schema(description = "Body to update a treatment (full replacement, PUT semantics).")
public record TreatmentUpdateRequest(
        @NotNull Long version,
        @NotBlank @Size(max = 255) String name,
        String description,
        String notes,
        @NotNull UsageType usageType,
        @NotEmpty List<UUID> medicationIds,
        Set<Integer> indicationIds
) {
}

