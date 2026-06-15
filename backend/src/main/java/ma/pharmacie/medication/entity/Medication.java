package ma.pharmacie.medication.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.pharmacie.common.audit.Auditable;
import ma.pharmacie.common.enums.UsageType;
import ma.pharmacie.lookup.entity.AgeGroup;
import ma.pharmacie.lookup.entity.Indication;
import ma.pharmacie.lookup.entity.PharmaceuticalForm;
import ma.pharmacie.lookup.entity.TherapeuticClass;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A medication in the pharmacy catalog.
 *
 * <p>Soft deletion: {@link #deletedAt} is set instead of removing the row.
 * Hibernate's {@code @SQLDelete} rewrites {@code DELETE} into an {@code UPDATE},
 * and {@code @SQLRestriction} filters soft-deleted rows from every query
 * automatically — so callers never need to add {@code deleted_at IS NULL}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "medication")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE medication SET deleted_at = now(), version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class Medication extends Auditable {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String inn;

    @Column(length = 80)
    private String dosage;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "is_parapharmacy", nullable = false)
    @Builder.Default
    private boolean parapharmacy = false;

    /**
     * Whether this medication is for humans or animals. Stored as a string
     * for readability. Defaults to {@link UsageType#HUMAN} for back-compat
     * with rows created before the column existed.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 20)
    @Builder.Default
    private UsageType usageType = UsageType.HUMAN;

    /**
     * Manual visual price ranking (0..5), entered by the pharmacist. 0 means
     * "not rated"; higher values are shown as more dollar icons in the UI.
     * We intentionally do not store actual prices.
     */
    @Column(name = "price_tier", nullable = false)
    @Builder.Default
    private int priceTier = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    private PharmaceuticalForm form;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "medication_age_group",
            joinColumns = @JoinColumn(name = "medication_id"),
            inverseJoinColumns = @JoinColumn(name = "age_group_id"))
    @Builder.Default
    private Set<AgeGroup> ageGroups = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "medication_therapeutic_class",
            joinColumns = @JoinColumn(name = "medication_id"),
            inverseJoinColumns = @JoinColumn(name = "therapeutic_class_id"))
    @Builder.Default
    private Set<TherapeuticClass> therapeuticClasses = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "medication_indication",
            joinColumns = @JoinColumn(name = "medication_id"),
            inverseJoinColumns = @JoinColumn(name = "indication_id"))
    @Builder.Default
    private Set<Indication> indications = new HashSet<>();

    // ---- enrichment / import hooks ----
    @Column(length = 20)
    private String barcode;

    @Column(name = "external_cip", length = 20)
    private String externalCip;

    @Column(name = "data_source", nullable = false, length = 20)
    @Builder.Default
    private String dataSource = "MANUAL";

    // ---- soft delete + optimistic locking ----
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(nullable = false)
    private long version;
}

