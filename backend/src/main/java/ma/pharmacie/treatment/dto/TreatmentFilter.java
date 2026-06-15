package ma.pharmacie.treatment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ma.pharmacie.common.enums.UsageType;

import java.util.Set;
import java.util.UUID;

/**
 * Composable filter for the treatment list endpoint. All fields optional and
 * AND-combined. The dedicated UI pages (Traitements / Traitements vétérinaires)
 * always supply {@link #usageType} so the two lists stay strictly separated.
 */
@Schema(description = "Treatment list filter. All fields optional and AND-combined.")
public record TreatmentFilter(
        @Schema(description = "Free-text search on the treatment name.")
        String q,
        @Schema(description = "Filter by usage type: HUMAN or VETERINARY. Null = both.")
        UsageType usageType,
        @Schema(description = "Only treatments tagged with at least one of these indications.")
        Set<Integer> indicationIds,
        @Schema(description = "Only treatments that include at least one of these medications.")
        Set<UUID> medicationIds
) {

    /** Empty / null-safe accessor. */
    public static TreatmentFilter empty() {
        return new TreatmentFilter(null, null, null, null);
    }
}

