package ma.pharmacie.lookup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Common shape for all lookup (reference-data) tables: integer surrogate id,
 * stable ASCII {@code code}, French label and a {@code sortOrder} for UI display.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "label_fr", nullable = false, length = 120)
    private String labelFr;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 100;
}

