package ma.pharmacie.medication.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
import ma.pharmacie.medication.dto.FacetCount;
import ma.pharmacie.medication.dto.MedicationCreateRequest;
import ma.pharmacie.medication.dto.MedicationFacetsResponse;
import ma.pharmacie.medication.dto.MedicationFilter;
import ma.pharmacie.medication.dto.MedicationResponse;
import ma.pharmacie.medication.dto.MedicationUpdateRequest;
import ma.pharmacie.medication.entity.Medication;
import ma.pharmacie.medication.mapper.MedicationMapper;
import ma.pharmacie.medication.repo.MedicationRepository;
import ma.pharmacie.medication.spec.MedicationSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
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
    private final EntityManager em;

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

    /**
     * Cascading-filter facets. For each of the four lookup dimensions we run a
     * grouped count of distinct medications, excluding that dimension itself
     * from the WHERE clause — so the user can still change their selection in
     * the same dropdown without it collapsing to a single option.
     *
     * <p>Soft-deleted medications are ignored automatically thanks to the
     * {@code @SQLRestriction} on {@link Medication}.
     */
    @Transactional(readOnly = true)
    public MedicationFacetsResponse facets(MedicationFilter filter) {
        MedicationFilter f = filter == null ? MedicationFilter.empty() : filter;
        return new MedicationFacetsResponse(
                countByJoin("form",               withoutForms(f)),
                countByJoin("ageGroups",          withoutAgeGroups(f)),
                countByJoin("therapeuticClasses", withoutTherapeuticClasses(f)),
                countByJoin("indications",        withoutIndications(f))
        );
    }

    private List<FacetCount> countByJoin(String attribute, MedicationFilter filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Medication> root = cq.from(Medication.class);
        // INNER JOIN: medications without a value for the dimension don't
        // contribute to any facet entry, which is exactly what we want.
        Join<Medication, ?> j = root.join(attribute, JoinType.INNER);

        Specification<Medication> spec = MedicationSpecifications.from(filter);
        Predicate p = spec.toPredicate(root, cq, cb);

        cq.multiselect(j.get("id"), cb.countDistinct(root));
        if (p != null) cq.where(p);
        cq.groupBy(j.get("id"));

        return em.createQuery(cq).getResultList().stream()
                .map(t -> new FacetCount((Integer) t.get(0), (Long) t.get(1)))
                .toList();
    }

    private static MedicationFilter withoutForms(MedicationFilter f) {
        return new MedicationFilter(f.q(), null, f.ageGroupIds(),
                f.therapeuticClassIds(), f.indicationIds(), f.parapharmacy(), f.dataSource());
    }

    private static MedicationFilter withoutAgeGroups(MedicationFilter f) {
        return new MedicationFilter(f.q(), f.formIds(), null,
                f.therapeuticClassIds(), f.indicationIds(), f.parapharmacy(), f.dataSource());
    }

    private static MedicationFilter withoutTherapeuticClasses(MedicationFilter f) {
        return new MedicationFilter(f.q(), f.formIds(), f.ageGroupIds(),
                null, f.indicationIds(), f.parapharmacy(), f.dataSource());
    }

    private static MedicationFilter withoutIndications(MedicationFilter f) {
        return new MedicationFilter(f.q(), f.formIds(), f.ageGroupIds(),
                f.therapeuticClassIds(), null, f.parapharmacy(), f.dataSource());
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
                .priceTier(req.priceTier() == null ? 0 : req.priceTier())
                .barcode(trim(req.barcode()))
                .externalCip(trim(req.externalCip()))
                .dataSource("MANUAL")
                .build();

        applyForm(m, req.formId());
        applyAgeGroups(m, req.ageGroupIds());
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
        m.setPriceTier(req.priceTier() == null ? 0 : req.priceTier());
        m.setBarcode(trim(req.barcode()));
        m.setExternalCip(trim(req.externalCip()));

        applyForm(m, req.formId());
        applyAgeGroups(m, req.ageGroupIds());
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

    private void applyAgeGroups(Medication m, Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            m.getAgeGroups().clear();
            return;
        }
        Set<AgeGroup> loaded = new HashSet<>(ageGroupRepo.findAllById(ids));
        if (loaded.size() != ids.size()) {
            throw NotFoundException.of("AgeGroup", ids);
        }
        m.setAgeGroups(loaded);
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

