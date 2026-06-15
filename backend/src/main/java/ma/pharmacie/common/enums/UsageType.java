package ma.pharmacie.common.enums;

/**
 * Whether a catalog item (medication, treatment) is intended for humans or
 * animals. Stored as a {@code VARCHAR} via {@code @Enumerated(EnumType.STRING)}
 * so the database stays human-readable.
 *
 * <p>Kept intentionally as a closed enum (not a lookup table): the set is
 * never going to grow, and we want the type system to enforce coherence
 * between a {@code Treatment} and the medications it contains.
 */
public enum UsageType {
    /** Standard human medications and parapharmacy. */
    HUMAN,
    /** Veterinary products. Sold from the same officine but never mixed with HUMAN. */
    VETERINARY
}

