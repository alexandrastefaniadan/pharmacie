package ma.pharmacie.lookup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.pharmacie.lookup.dto.LookupDto;
import ma.pharmacie.lookup.mapper.LookupMapper;
import ma.pharmacie.lookup.repo.AgeGroupRepository;
import ma.pharmacie.lookup.repo.IndicationRepository;
import ma.pharmacie.lookup.repo.PharmaceuticalFormRepository;
import ma.pharmacie.lookup.repo.TherapeuticClassRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only endpoints that feed the UI dropdowns / multi-selects.
 * Write endpoints (add a new form, indication, …) will be added with auth.
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
}

