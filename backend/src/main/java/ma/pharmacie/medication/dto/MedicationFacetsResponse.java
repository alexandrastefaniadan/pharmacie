package ma.pharmacie.medication.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Cascading-filter facet response. For each of the four lookup dimensions, the
 * server returns the lookup ids that still match the current filter (with that
 * dimension itself excluded, so the user can change their mind), together with
 * the count of distinct medications behind each option.
 *
 * <p>The frontend uses this to disable options that would yield zero results
 * and to show counts in dropdowns ("Toux sèche (12)").
 */
@Schema(description = "Available filter options given the current selection.")
public record MedicationFacetsResponse(
        List<FacetCount> forms,
        List<FacetCount> ageGroups,
        List<FacetCount> therapeuticClasses,
        List<FacetCount> indications
) {
}

