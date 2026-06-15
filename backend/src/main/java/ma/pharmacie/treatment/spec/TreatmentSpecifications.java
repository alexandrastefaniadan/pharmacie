package ma.pharmacie.treatment.spec;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import ma.pharmacie.common.enums.UsageType;
import ma.pharmacie.lookup.entity.Indication;
import ma.pharmacie.medication.entity.Medication;
import ma.pharmacie.treatment.dto.TreatmentFilter;
import ma.pharmacie.treatment.entity.Treatment;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;
import java.util.UUID;

/**
 * Builds a {@link Specification} from a {@link TreatmentFilter}. Each non-null
 * filter field contributes an AND predicate; null fields are ignored.
 *
 * <p>Collection-valued filters use DISTINCT semantics on the join so a
 * treatment matching several indications / medications is not duplicated.
 */
public final class TreatmentSpecifications {

    private TreatmentSpecifications() {}

    private static <T> Specification<T> all() {
        return (root, cq, cb) -> cb.conjunction();
    }

    public static Specification<Treatment> from(TreatmentFilter f) {
        if (f == null) return all();

        return Specification.allOf(
                nameContains(f.q()),
                usageTypeEq(f.usageType()),
                indicationIn(f.indicationIds()),
                medicationIn(f.medicationIds())
        );
    }

    // ---- single-field predicates ----

    private static Specification<Treatment> nameContains(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.toLowerCase().trim() + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("name")), like);
    }

    private static Specification<Treatment> usageTypeEq(UsageType v) {
        if (v == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("usageType"), v);
    }

    private static Specification<Treatment> indicationIn(Set<Integer> ids) {
        if (isEmpty(ids)) return null;
        return (root, cq, cb) -> {
            if (cq.getResultType() != Long.class) cq.distinct(true);
            Join<Treatment, Indication> j = root.join("indications", JoinType.LEFT);
            return j.get("id").in(ids);
        };
    }

    private static Specification<Treatment> medicationIn(Set<UUID> ids) {
        if (isEmpty(ids)) return null;
        return (root, cq, cb) -> {
            if (cq.getResultType() != Long.class) cq.distinct(true);
            Join<Treatment, Medication> j = root.join("medications", JoinType.LEFT);
            return j.get("id").in(ids);
        };
    }

    private static boolean isEmpty(Set<?> s) {
        return s == null || s.isEmpty();
    }
}

