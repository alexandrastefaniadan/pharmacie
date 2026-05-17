package ma.pharmacie.medication.service;

import lombok.RequiredArgsConstructor;
import ma.pharmacie.common.exception.ConflictException;
import ma.pharmacie.common.exception.NotFoundException;
import ma.pharmacie.lookup.entity.AgeGroup;
import ma.pharmacie.lookup.entity.Indication;
import ma.pharmacie.lookup.entity.PharmaceuticalForm;
import ma.pharmacie.lookup.entity.TherapeuticClass;
import ma.pharmacie.lookup.repo.AgeGroupRepository;
import ma.pharmacie.lookup.repo.IndicationRepository;
import ma.pharmacie.lookup.repo.PharmaceuticalFormRepository;
import ma.pharmacie.lookup.repo.TherapeuticClassRepository;
import ma.pharmacie.medication.dto.MedicationCreateRequest;
import ma.pharmacie.medication.dto.MedicationFilter;
import ma.pharmacie.medication.dto.MedicationResponse;
import ma.pharmacie.medication.dto.MedicationUpdateRequest;
import ma.pharmacie.medication.entity.Medication;
import ma.pharmacie.medication.mapper.MedicationMapper;
import ma.pharmacie.medication.repo.MedicationRepository;
import ma.pharmacie.medication.spec.MedicationSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Business logic for the medication catalog: CRUD, soft delete, paged
 * specification-driven search.
 *
 * <p>All public methods are transactional. Read methods use {@code readOnly = true}
 * for a small Hibernate optimization.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MedicationService {

    private final MedicationRepository medicationRepo;
    private final PharmaceuticalFormRepository formRepo;
    private final AgeGroupRepository ageGroupRepo;
    private final TherapeuticClassRepository therapeuticClassRepo;
    private final IndicationRepository indicationRepo;
    private final MedicationMapper mapper;

    // -------------------- queries --------------------

    @Transactional(readOnly = true)
    public Page<MedicationResponse> search(MedicationFilter filter, Pageable pageable) {
        return medicationRepo.findAll(MedicationSpecifications.from(filter), pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MedicationResponse getById(UUID id) {
        return mapper.toResponse(loadOrThrow(id));
    }

    // -------------------- commands --------------------

    public MedicationResponse create(MedicationCreateRequest req) {
        String name = req.name().trim();
        if (medicationRepo.existsByNameIgnoreCase(name)) {
            throw new ConflictException("A medication with name '" + name + "' already exists.");
        }
        Medication m = Medication.builder()
                .name(name)
                .inn(trim(req.inn()))
                .dosage(trim(req.dosage()))
                .description(req.description())
                .parapharmacy(Boolean.TRUE.equals(req.parapharmacy()))
                .barcode(trim(req.barcode()))
                .externalCip(trim(req.externalCip()))
                .dataSource("MANUAL")
                .build();

        applyForm(m, req.formId());
        applyAgeGroup(m, req.ageGroupId());
        applyTherapeuticClasses(m, req.therapeuticClassIds());
        applyIndications(m, req.indicationIds());

        return mapper.toResponse(medicationRepo.save(m));
    }

    public MedicationResponse update(UUID id, MedicationUpdateRequest req) {
        Medication m = loadOrThrow(id);

        if (m.getVersion() != req.version()) {
            throw new ConflictException(
                    "Medication has been modified by someone else. Reload and try again.");
        }

        String newName = req.name().trim();
        if (!newName.equalsIgnoreCase(m.getName()) && medicationRepo.existsByNameIgnoreCase(newName)) {
            throw new ConflictException("A medication with name '" + newName + "' already exists.");
        }

        m.setName(newName);
        m.setInn(trim(req.inn()));
        m.setDosage(trim(req.dosage()));
        m.setDescription(req.description());
        m.setParapharmacy(Boolean.TRUE.equals(req.parapharmacy()));
        m.setBarcode(trim(req.barcode()));
        m.setExternalCip(trim(req.externalCip()));

        applyForm(m, req.formId());
        applyAgeGroup(m, req.ageGroupId());
        applyTherapeuticClasses(m, req.therapeuticClassIds());
        applyIndications(m, req.indicationIds());

        return mapper.toResponse(m);
    }

    /** Soft delete — Hibernate's {@code @SQLDelete} flips {@code deleted_at}. */
    public void delete(UUID id) {
        Medication m = loadOrThrow(id);
        medicationRepo.delete(m);
    }

    // -------------------- helpers --------------------

    private Medication loadOrThrow(UUID id) {
        return medicationRepo.findById(id)
                .orElseThrow(() -> NotFoundException.of("Medication", id));
    }

    private void applyForm(Medication m, Integer formId) {
        if (formId == null) { m.setForm(null); return; }
        PharmaceuticalForm form = formRepo.findById(formId)
                .orElseThrow(() -> NotFoundException.of("PharmaceuticalForm", formId));
        m.setForm(form);
    }

    private void applyAgeGroup(Medication m, Integer ageGroupId) {
        if (ageGroupId == null) { m.setAgeGroup(null); return; }
        AgeGroup ag = ageGroupRepo.findById(ageGroupId)
                .orElseThrow(() -> NotFoundException.of("AgeGroup", ageGroupId));
        m.setAgeGroup(ag);
    }

    private void applyTherapeuticClasses(Medication m, Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            m.getTherapeuticClasses().clear();
            return;
        }
        Set<TherapeuticClass> loaded = new HashSet<>(therapeuticClassRepo.findAllById(ids));
        if (loaded.size() != ids.size()) {
            throw NotFoundException.of("TherapeuticClass", ids);
        }
        m.setTherapeuticClasses(loaded);
    }

    private void applyIndications(Medication m, Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            m.getIndications().clear();
            return;
        }
        Set<Indication> loaded = new HashSet<>(indicationRepo.findAllById(ids));
        if (loaded.size() != ids.size()) {
            throw NotFoundException.of("Indication", ids);
        }
        m.setIndications(loaded);
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

