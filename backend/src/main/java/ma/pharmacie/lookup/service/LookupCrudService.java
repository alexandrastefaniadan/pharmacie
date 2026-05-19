package ma.pharmacie.lookup.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import ma.pharmacie.common.exception.ConflictException;
import ma.pharmacie.common.exception.NotFoundException;
import ma.pharmacie.lookup.LookupKind;
import ma.pharmacie.lookup.dto.LookupDto;
import ma.pharmacie.lookup.dto.LookupWriteRequest;
import ma.pharmacie.lookup.entity.AbstractLookup;
import ma.pharmacie.lookup.entity.AgeGroup;
import ma.pharmacie.lookup.entity.Indication;
import ma.pharmacie.lookup.entity.PharmaceuticalForm;
import ma.pharmacie.lookup.entity.TherapeuticClass;
import ma.pharmacie.lookup.mapper.LookupMapper;
import ma.pharmacie.lookup.repo.AgeGroupRepository;
import ma.pharmacie.lookup.repo.IndicationRepository;
import ma.pharmacie.lookup.repo.PharmaceuticalFormRepository;
import ma.pharmacie.lookup.repo.TherapeuticClassRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Create / update / delete operations for the four lookup dimensions, exposed
 * via a single kind-dispatched API to keep the controller small.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>{@code code} is case-insensitively unique within its table; if not
 *       supplied it is auto-derived from {@code labelFr} by slugifying.</li>
 *   <li>A lookup that is still referenced by at least one (non-soft-deleted)
 *       medication cannot be deleted — we return a 409 with a clear message
 *       and the usage count.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LookupCrudService {

    private final PharmaceuticalFormRepository formRepo;
    private final AgeGroupRepository ageGroupRepo;
    private final TherapeuticClassRepository therapeuticClassRepo;
    private final IndicationRepository indicationRepo;
    private final LookupMapper mapper;
    private final EntityManager em;

    // -------------------- public API --------------------

    public LookupDto create(LookupKind kind, LookupWriteRequest req) {
        String labelFr = requireLabel(req.labelFr());
        String code = normalizeCode(req.code(), labelFr);
        ensureCodeUnique(kind, code, null);

        AbstractLookup e = newEntity(kind);
        populate(e, code, labelFr, req.sortOrder());
        save(kind, e);
        return mapper.toDto(e);
    }

    public LookupDto update(LookupKind kind, Integer id, LookupWriteRequest req) {
        AbstractLookup e = loadOrThrow(kind, id);
        String labelFr = requireLabel(req.labelFr());
        String code = normalizeCode(req.code(), labelFr);
        ensureCodeUnique(kind, code, id);

        populate(e, code, labelFr, req.sortOrder());
        // Managed entity: changes flushed at TX commit.
        return mapper.toDto(e);
    }

    public void delete(LookupKind kind, Integer id) {
        loadOrThrow(kind, id); // 404 if missing
        long usages = countUsages(kind, id);
        if (usages > 0) {
            throw new ConflictException(
                    "Impossible de supprimer : utilisé par %d médicament(s).".formatted(usages));
        }
        switch (kind) {
            case FORMS -> formRepo.deleteById(id);
            case AGE_GROUPS -> ageGroupRepo.deleteById(id);
            case THERAPEUTIC_CLASSES -> therapeuticClassRepo.deleteById(id);
            case INDICATIONS -> indicationRepo.deleteById(id);
        }
    }

    // -------------------- helpers --------------------

    private AbstractLookup newEntity(LookupKind kind) {
        return switch (kind) {
            case FORMS -> new PharmaceuticalForm();
            case AGE_GROUPS -> new AgeGroup();
            case THERAPEUTIC_CLASSES -> new TherapeuticClass();
            case INDICATIONS -> new Indication();
        };
    }

    private void save(LookupKind kind, AbstractLookup e) {
        switch (kind) {
            case FORMS -> formRepo.save((PharmaceuticalForm) e);
            case AGE_GROUPS -> ageGroupRepo.save((AgeGroup) e);
            case THERAPEUTIC_CLASSES -> therapeuticClassRepo.save((TherapeuticClass) e);
            case INDICATIONS -> indicationRepo.save((Indication) e);
        }
    }

    private AbstractLookup loadOrThrow(LookupKind kind, Integer id) {
        Supplier<Optional<? extends AbstractLookup>> finder = switch (kind) {
            case FORMS -> () -> formRepo.findById(id);
            case AGE_GROUPS -> () -> ageGroupRepo.findById(id);
            case THERAPEUTIC_CLASSES -> () -> therapeuticClassRepo.findById(id);
            case INDICATIONS -> () -> indicationRepo.findById(id);
        };
        return finder.get().orElseThrow(() -> NotFoundException.of(kind.labelFr(), id));
    }

    /** Unused, kept for symmetry / future generic operations. */
    @SuppressWarnings("unused")
    private JpaRepository<? extends AbstractLookup, Integer> repoOf(LookupKind kind) {
        return switch (kind) {
            case FORMS -> formRepo;
            case AGE_GROUPS -> ageGroupRepo;
            case THERAPEUTIC_CLASSES -> therapeuticClassRepo;
            case INDICATIONS -> indicationRepo;
        };
    }

    private static void populate(AbstractLookup e, String code, String labelFr, Integer sortOrder) {
        e.setCode(code);
        e.setLabelFr(labelFr);
        e.setSortOrder(sortOrder == null ? 100 : sortOrder);
    }

    private void ensureCodeUnique(LookupKind kind, String code, Integer ignoreId) {
        String sql = "SELECT COUNT(*) FROM " + kind.tableName()
                + " WHERE UPPER(code) = UPPER(?1)"
                + (ignoreId == null ? "" : " AND id <> ?2");
        var q = em.createNativeQuery(sql).setParameter(1, code);
        if (ignoreId != null) q.setParameter(2, ignoreId);
        Number n = (Number) q.getSingleResult();
        if (n != null && n.longValue() > 0) {
            throw new ConflictException("Le code « " + code + " » est déjà utilisé.");
        }
    }

    private long countUsages(LookupKind kind, Integer id) {
        String sql = switch (kind) {
            case FORMS -> "SELECT COUNT(*) FROM medication WHERE form_id = ?1 AND deleted_at IS NULL";
            case AGE_GROUPS -> """
                    SELECT COUNT(*) FROM medication_age_group j
                    JOIN medication m ON m.id = j.medication_id
                    WHERE j.age_group_id = ?1 AND m.deleted_at IS NULL""";
            case THERAPEUTIC_CLASSES -> """
                    SELECT COUNT(*) FROM medication_therapeutic_class j
                    JOIN medication m ON m.id = j.medication_id
                    WHERE j.therapeutic_class_id = ?1 AND m.deleted_at IS NULL""";
            case INDICATIONS -> """
                    SELECT COUNT(*) FROM medication_indication j
                    JOIN medication m ON m.id = j.medication_id
                    WHERE j.indication_id = ?1 AND m.deleted_at IS NULL""";
        };
        Number n = (Number) em.createNativeQuery(sql).setParameter(1, id).getSingleResult();
        return n == null ? 0 : n.longValue();
    }

    private static String requireLabel(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("labelFr is required");
        }
        return s.trim();
    }

    private static String normalizeCode(String code, String fallbackLabel) {
        String src = (code != null && !code.isBlank()) ? code : fallbackLabel;
        return slugify(src).toUpperCase();
    }

    /** "Sirop pédiatrique" → "SIROP_PEDIATRIQUE". */
    private static String slugify(String s) {
        String stripped = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return stripped
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}

