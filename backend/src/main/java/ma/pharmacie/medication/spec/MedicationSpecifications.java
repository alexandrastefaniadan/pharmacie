package ma.pharmacie.medication.spec;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import ma.pharmacie.common.enums.UsageType;
import ma.pharmacie.lookup.entity.AgeGroup;
import ma.pharmacie.lookup.entity.Indication;
import ma.pharmacie.lookup.entity.PharmaceuticalForm;
import ma.pharmacie.lookup.entity.TherapeuticClass;
import ma.pharmacie.medication.dto.MedicationFilter;
import ma.pharmacie.medication.entity.Medication;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

/**
 * Builds a {@link Specification} from a {@link MedicationFilter}. Each non-null
 * filter field contributes an AND predicate; null fields are ignored.
 *
 * <p>The collection-valued filters use DISTINCT semantics on the join, so a
 * medication that matches several indications is not duplicated in the result.
 */
public final class MedicationSpecifications {

    private MedicationSpecifications() {}

    /** Always-true predicate, used when no filter is provided. */
    private static <T> Specification<T> all() {
        return (root, cq, cb) -> cb.conjunction();
    }

    public static Specification<Medication> from(MedicationFilter f) {
        if (f == null) return all();

        return Specification.allOf(
                nameOrInnContains(f.q()),
                formIn(f.formIds()),
                ageGroupIn(f.ageGroupIds()),
                therapeuticClassIn(f.therapeuticClassIds()),
                indicationIn(f.indicationIds()),
                parapharmacyEq(f.parapharmacy()),
                usageTypeEq(f.usageType()),
                dataSourceEq(f.dataSource())
        );
    }

    // ---- single-field predicates ----

    private static Specification<Medication> nameOrInnContains(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.toLowerCase().trim() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("inn")), like));
    }

    private static Specification<Medication> formIn(Set<Integer> ids) {
        if (isEmpty(ids)) return null;
        return (root, cq, cb) -> {
            Join<Medication, PharmaceuticalForm> j = root.join("form", JoinType.LEFT);
            return j.get("id").in(ids);
        };
    }

    private static Specification<Medication> ageGroupIn(Set<Integer> ids) {
        if (isEmpty(ids)) return null;
        return (root, cq, cb) -> {
            if (cq.getResultType() != Long.class) cq.distinct(true);
            Join<Medication, AgeGroup> j = root.join("ageGroups", JoinType.LEFT);
            return j.get("id").in(ids);
        };
    }

    private static Specification<Medication> therapeuticClassIn(Set<Integer> ids) {
        if (isEmpty(ids)) return null;
        return (root, cq, cb) -> {
            if (cq.getResultType() != Long.class) cq.distinct(true);
            Join<Medication, TherapeuticClass> j = root.join("therapeuticClasses", JoinType.LEFT);
            return j.get("id").in(ids);
        };
    }

    private static Specification<Medication> indicationIn(Set<Integer> ids) {
        if (isEmpty(ids)) return null;
        return (root, cq, cb) -> {
            if (cq.getResultType() != Long.class) cq.distinct(true);
            Join<Medication, Indication> j = root.join("indications", JoinType.LEFT);
            return j.get("id").in(ids);
        };
    }

    private static Specification<Medication> parapharmacyEq(Boolean v) {
        if (v == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("parapharmacy"), v);
    }

    private static Specification<Medication> usageTypeEq(UsageType v) {
        if (v == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("usageType"), v);
    }

    private static Specification<Medication> dataSourceEq(String src) {
        if (src == null || src.isBlank()) return null;
        return (root, cq, cb) -> cb.equal(root.get("dataSource"), src.toUpperCase());
    }

    private static boolean isEmpty(Set<?> s) {
        return s == null || s.isEmpty();
    }
}

