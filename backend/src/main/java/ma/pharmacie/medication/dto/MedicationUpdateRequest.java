package ma.pharmacie.medication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Full-replacement update payload (PUT semantics). Every field that should
 * remain set must be supplied; nulls clear the corresponding column / relation.
 *
 * <p>{@code version} is required and used for optimistic locking.
 */
@Schema(description = "Body to update a medication (full replacement, PUT semantics).")
public record MedicationUpdateRequest(
        @NotNull Long version,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String inn,
        @Size(max = 80) String dosage,
        String description,
        Boolean parapharmacy,
        Integer formId,
        Set<Integer> ageGroupIds,
        Set<Integer> therapeuticClassIds,
        Set<Integer> indicationIds,
        @Size(max = 20) String barcode,
        @Size(max = 20) String externalCip
) {
}

