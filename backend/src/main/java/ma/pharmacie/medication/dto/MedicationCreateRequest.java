package ma.pharmacie.medication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Payload to create a new medication. {@code name} is the only required field. */
@Schema(description = "Body to create a medication.")
public record MedicationCreateRequest(
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

