package ma.pharmacie.treatment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.pharmacie.treatment.dto.TreatmentCreateRequest;
import ma.pharmacie.treatment.dto.TreatmentFilter;
import ma.pharmacie.treatment.dto.TreatmentResponse;
import ma.pharmacie.treatment.dto.TreatmentUpdateRequest;
import ma.pharmacie.treatment.service.TreatmentService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * REST endpoints for treatments.
 *
 * <pre>
 *   GET    /api/v1/treatments        paged + filtered + sortable list
 *   POST   /api/v1/treatments        create
 *   GET    /api/v1/treatments/{id}   one
 *   PUT    /api/v1/treatments/{id}   full update (optimistic locking via version)
 *   DELETE /api/v1/treatments/{id}   soft delete
 * </pre>
 *
 * <p>Each treatment is scoped to a single {@link ma.pharmacie.common.enums.UsageType}
 * (HUMAN or VETERINARY). The two SPA pages (Traitements / Traitements
 * vétérinaires) always send {@code usageType} on the list endpoint so the two
 * catalogs stay strictly separated.
 */
@RestController
@RequestMapping("/api/v1/treatments")
@RequiredArgsConstructor
@Tag(name = "Treatments", description = "Treatment catalog: CRUD + filterable list")
public class TreatmentController {

    private final TreatmentService service;

    @Operation(summary = "List treatments (paged, filterable, sortable)",
            description = "All filter fields are optional and combined with AND. " +
                          "Use the standard Spring pageable params: page, size, sort (e.g. sort=name,asc).")
    @GetMapping
    public Page<TreatmentResponse> list(
            @ParameterObject TreatmentFilter filter,
            @ParameterObject @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return service.search(filter == null ? TreatmentFilter.empty() : filter, pageable);
    }

    @Operation(summary = "Get one treatment by id")
    @GetMapping("/{id}")
    public TreatmentResponse get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @Operation(summary = "Create a treatment")
    @PostMapping
    public ResponseEntity<TreatmentResponse> create(@Valid @RequestBody TreatmentCreateRequest req) {
        TreatmentResponse created = service.create(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update a treatment (full replacement)")
    @PutMapping("/{id}")
    public TreatmentResponse update(@PathVariable UUID id,
                                    @Valid @RequestBody TreatmentUpdateRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Soft-delete a treatment")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Parameter(description = "Treatment UUID") UUID id) {
        service.delete(id);
    }
}

