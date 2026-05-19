package ma.pharmacie.lookup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.pharmacie.lookup.dto.LookupDto;
import ma.pharmacie.lookup.dto.LookupWriteRequest;
import ma.pharmacie.lookup.mapper.LookupMapper;
import ma.pharmacie.lookup.repo.AgeGroupRepository;
import ma.pharmacie.lookup.repo.IndicationRepository;
import ma.pharmacie.lookup.repo.PharmaceuticalFormRepository;
import ma.pharmacie.lookup.repo.TherapeuticClassRepository;
import ma.pharmacie.lookup.service.LookupCrudService;
import org.springframework.data.domain.Sort;
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
import java.util.List;

/**
 * Endpoints that feed the UI dropdowns and let the user manage reference data.
 *
 * <p>Read endpoints stay specific per kind for convenience and OpenAPI clarity;
 * write endpoints share a single kind-dispatched route to keep the controller
 * small.
 */
@RestController
@RequestMapping("/api/v1/lookups")
@RequiredArgsConstructor
@Tag(name = "Lookups", description = "Reference data: forms, age groups, therapeutic classes, indications")
public class LookupController {

    private static final Sort BY_SORT_ORDER = Sort.by("sortOrder", "labelFr");

    private final PharmaceuticalFormRepository formRepo;
    private final AgeGroupRepository ageGroupRepo;
    private final TherapeuticClassRepository therapeuticClassRepo;
    private final IndicationRepository indicationRepo;
    private final LookupMapper mapper;
    private final LookupCrudService crud;

    // -------------------- reads --------------------

    @Operation(summary = "List all pharmaceutical forms")
    @GetMapping("/forms")
    public List<LookupDto> forms() {
        return mapper.toDto(formRepo.findAll(BY_SORT_ORDER));
    }

    @Operation(summary = "List all age groups")
    @GetMapping("/age-groups")
    public List<LookupDto> ageGroups() {
        return mapper.toDto(ageGroupRepo.findAll(BY_SORT_ORDER));
    }

    @Operation(summary = "List all therapeutic classes")
    @GetMapping("/therapeutic-classes")
    public List<LookupDto> therapeuticClasses() {
        return mapper.toDto(therapeuticClassRepo.findAll(BY_SORT_ORDER));
    }

    @Operation(summary = "List all indications")
    @GetMapping("/indications")
    public List<LookupDto> indications() {
        return mapper.toDto(indicationRepo.findAll(BY_SORT_ORDER));
    }

    // -------------------- writes (kind-dispatched) --------------------

    @Operation(summary = "Create a lookup row (form, age group, therapeutic class or indication)")
    @PostMapping("/{kind}")
    public ResponseEntity<LookupDto> create(
            @Parameter(description = "forms | age-groups | therapeutic-classes | indications")
            @PathVariable String kind,
            @Valid @RequestBody LookupWriteRequest req) {

        LookupKind k = LookupKind.fromPath(kind);
        LookupDto created = crud.create(k, req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update a lookup row")
    @PutMapping("/{kind}/{id}")
    public LookupDto update(
            @PathVariable String kind,
            @PathVariable Integer id,
            @Valid @RequestBody LookupWriteRequest req) {
        return crud.update(LookupKind.fromPath(kind), id, req);
    }

    @Operation(summary = "Delete a lookup row (409 if still used by a medication)")
    @DeleteMapping("/{kind}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String kind, @PathVariable Integer id) {
        crud.delete(LookupKind.fromPath(kind), id);
    }
}
