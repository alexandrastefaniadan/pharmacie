package ma.pharmacie.lookup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body for creating or updating any of the 4 lookup rows
 * (form, age group, therapeutic class, indication).
 *
 * <p>{@link #code} is optional on create — if blank, the server derives a
 * stable ASCII code by slugifying {@link #labelFr} (e.g. "Sirop pédiatrique"
 * → {@code SIROP_PEDIATRIQUE}). {@link #sortOrder} defaults to 100.
 */
@Schema(description = "Create/update payload for a lookup row.")
public record LookupWriteRequest(
        @Schema(example = "SYRUP", description = "Stable ASCII identifier. Auto-derived from labelFr if omitted.")
        @Size(max = 60) String code,

        @Schema(example = "Sirop", description = "Display label in French.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 120) String labelFr,

        @Schema(example = "40", description = "Sort key used in dropdowns. Defaults to 100.")
        Integer sortOrder
) {
}

