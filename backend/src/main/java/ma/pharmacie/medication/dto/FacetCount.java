package ma.pharmacie.medication.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single facet entry: the id of a lookup option and the number of distinct
 * (non-deleted) medications that still match the current filter if this option
 * were selected.
 */
@Schema(description = "Facet entry — lookup id and remaining medication count.")
public record FacetCount(
        @Schema(example = "12") Integer id,
        @Schema(example = "7")  long count
) {
}

