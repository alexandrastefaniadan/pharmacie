package ma.pharmacie.medication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.pharmacie.medication.dto.MedicationCreateRequest;
import ma.pharmacie.medication.dto.MedicationFacetsResponse;
import ma.pharmacie.medication.dto.MedicationFilter;
import ma.pharmacie.medication.dto.MedicationResponse;
import ma.pharmacie.medication.dto.MedicationUpdateRequest;
import ma.pharmacie.medication.service.MedicationService;
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
 * REST endpoints for the medication catalog.
 *
 * <pre>
 *   GET    /api/v1/medications        paged + filtered + sortable list
 *   POST   /api/v1/medications        create
 *   GET    /api/v1/medications/{id}   one
 *   PUT    /api/v1/medications/{id}   full update (optimistic locking via version)
 *   DELETE /api/v1/medications/{id}   soft delete
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
@Tag(name = "Medications", description = "Medication catalog: CRUD + filterable list")
public class MedicationController {

    private final MedicationService service;

    @Operation(summary = "List medications (paged, filterable, sortable)",
            description = "All filter fields are optional and combined with AND. " +
                          "Use the standard Spring pageable params: page, size, sort (e.g. sort=name,asc).")
    @GetMapping
    public Page<MedicationResponse> list(
            @ParameterObject MedicationFilter filter,
            @ParameterObject @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return service.search(filter == null ? MedicationFilter.empty() : filter, pageable);
    }

    @Operation(summary = "Get one medication by id")
    @GetMapping("/{id}")
    public MedicationResponse get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @Operation(summary = "Cascading-filter facets",
            description = "Returns, for each lookup dimension (form, age group, therapeutic class, " +
                          "indication), the option ids that still have at least one matching " +
                          "medication given the current filter — together with the count. " +
                          "Each dimension is computed with itself excluded from the filter, so the " +
                          "user can still change their selection in that same dropdown.")
    @GetMapping("/facets")
    public MedicationFacetsResponse facets(@ParameterObject MedicationFilter filter) {
        return service.facets(filter);
    }

    @Operation(summary = "Create a medication")
    @PostMapping
    public ResponseEntity<MedicationResponse> create(@Valid @RequestBody MedicationCreateRequest req) {
        MedicationResponse created = service.create(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update a medication (full replacement)")
    @PutMapping("/{id}")
    public MedicationResponse update(@PathVariable UUID id,
                                     @Valid @RequestBody MedicationUpdateRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Soft-delete a medication")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Parameter(description = "Medication UUID") UUID id) {
        service.delete(id);
    }
}

