package ma.pharmacie.treatment.entity;

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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.pharmacie.common.audit.Auditable;
import ma.pharmacie.common.enums.UsageType;
import ma.pharmacie.lookup.entity.Indication;
import ma.pharmacie.medication.entity.Medication;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A treatment suggestion: a named bundle of medications recommended for a
 * given condition. Strictly one of {@link UsageType#HUMAN} or
 * {@link UsageType#VETERINARY} — the linked medications must share that
 * usage type (enforced by the service layer).
 *
 * <p>Soft deletion mirrors {@link Medication}: {@code deleted_at} flips
 * via {@code @SQLDelete}, and {@code @SQLRestriction} hides soft-deleted
 * rows from every query.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "treatment")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE treatment SET deleted_at = now(), version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class Treatment extends Auditable {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    /** Patient-facing explanation (what the treatment does, how to use it). */
    @Column(columnDefinition = "text")
    private String description;

    /** Internal pharmacist notes — contraindications, "ask if pregnant", etc. */
    @Column(columnDefinition = "text")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 20)
    private UsageType usageType;

    /**
     * Medications that make up this treatment. Ordered via {@code @OrderColumn}
     * on the join table's {@code position} column: JPA reads & writes that
     * column automatically when the list is mutated, preserving the order
     * (1st-line, 2nd-line, accompanying) the pharmacist entered.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "treatment_medication",
            joinColumns = @JoinColumn(name = "treatment_id"),
            inverseJoinColumns = @JoinColumn(name = "medication_id"))
    @OrderColumn(name = "position")
    @Builder.Default
    private List<Medication> medications = new ArrayList<>();

    /** Optional symptom tags — enables future "search by symptom → treatments". */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "treatment_indication",
            joinColumns = @JoinColumn(name = "treatment_id"),
            inverseJoinColumns = @JoinColumn(name = "indication_id"))
    @Builder.Default
    private Set<Indication> indications = new HashSet<>();

    // ---- soft delete + optimistic locking ----
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(nullable = false)
    private long version;
}

