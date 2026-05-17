package ma.pharmacie.medication.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * Composable filter for the medication list endpoint. Every field is optional;
 * supplied fields are combined with logical AND.
 *
 * <p>Example: {@code form=SYRUP, indication=COUGH_DRY, ageGroup=CHILD}
 * → "syrups for dry cough for kids".
 */
@Schema(description = "Medication list filter. All fields are optional and AND-combined.")
public record MedicationFilter(
        @Schema(description = "Free-text search on name or INN (case-insensitive, trigram-backed).")
        String q,
        Set<Integer> formIds,
        Set<Integer> ageGroupIds,
        Set<Integer> therapeuticClassIds,
        Set<Integer> indicationIds,
        Boolean parapharmacy,
        @Schema(description = "Filter by origin: MANUAL | SOBRUS | DMP")
        String dataSource
) {

    /** Empty / null-safe accessor. */
    public static MedicationFilter empty() {
        return new MedicationFilter(null, null, null, null, null, null, null);
    }
}

