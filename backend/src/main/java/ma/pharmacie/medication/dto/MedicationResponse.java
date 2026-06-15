package ma.pharmacie.medication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ma.pharmacie.common.enums.UsageType;
import ma.pharmacie.lookup.dto.LookupDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Response shape for medication endpoints. Relationships are flattened to {@link LookupDto}. */
@Schema(description = "A medication as returned by the API.")
public record MedicationResponse(
        UUID id,
        String name,
        String inn,
        String dosage,
        String description,
        boolean parapharmacy,
        @Schema(description = "HUMAN or VETERINARY.", example = "HUMAN")
        UsageType usageType,
        @Schema(description = "Manual visual price ranking (0..5). 0 = not rated.", example = "3")
        int priceTier,
        LookupDto form,
        List<LookupDto> ageGroups,
        List<LookupDto> therapeuticClasses,
        List<LookupDto> indications,
        String barcode,
        String externalCip,
        String dataSource,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}



