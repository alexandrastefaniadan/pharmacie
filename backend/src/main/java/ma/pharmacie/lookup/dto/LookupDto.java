package ma.pharmacie.lookup.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic lookup row. All 4 lookup tables share this shape, so a single DTO
 * keeps the client code simple (one TypeScript interface for everything).
 */
@Schema(description = "Reference data row (form, age group, therapeutic class, indication).")
public record LookupDto(
        @Schema(example = "12") Integer id,
        @Schema(example = "SYRUP", description = "Stable ASCII identifier — safe to use in code") String code,
        @Schema(example = "Sirop", description = "Display label in French") String labelFr,
        @Schema(example = "40", description = "Used to order options in dropdowns") Integer sortOrder
) {
}

