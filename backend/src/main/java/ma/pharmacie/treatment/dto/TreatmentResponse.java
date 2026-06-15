package ma.pharmacie.treatment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ma.pharmacie.common.enums.UsageType;
import ma.pharmacie.lookup.dto.LookupDto;
import ma.pharmacie.medication.dto.MedicationSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A treatment as returned by the API. */
@Schema(description = "A treatment with its medications and (optional) indication tags.")
public record TreatmentResponse(
        UUID id,
        String name,
        String description,
        String notes,
        UsageType usageType,
        @Schema(description = "Medications in the order the pharmacist entered them.")
        List<MedicationSummary> medications,
        List<LookupDto> indications,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}

