package ma.pharmacie.medication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ma.pharmacie.common.enums.UsageType;

import java.util.UUID;

/**
 * Lightweight projection of a {@link MedicationResponse} used when a medication
 * is embedded inside another resource (e.g. inside a treatment). Keeps the JSON
 * payload small when many meds are returned at once.
 */
@Schema(description = "Minimal info about a medication, used when embedded inside another resource.")
public record MedicationSummary(
        UUID id,
        String name,
        String inn,
        String dosage,
        @Schema(description = "Pharmaceutical form label, e.g. \"Sirop\". Null if not set.")
        String formLabel,
        UsageType usageType,
        @Schema(description = "Manual visual price ranking (0..5). 0 = not rated.")
        int priceTier
) {
}

