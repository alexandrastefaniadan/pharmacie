package ma.pharmacie.medication.repo;

import ma.pharmacie.common.enums.UsageType;
import ma.pharmacie.medication.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface MedicationRepository
        extends JpaRepository<Medication, UUID>, JpaSpecificationExecutor<Medication> {

    /** Legacy, kept for back-compat. Prefer {@link #existsByNameIgnoreCaseAndUsageType}. */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Active-name uniqueness is scoped per {@link UsageType} (matches the
     * partial unique index {@code ux_medication_name_active}). The same brand
     * name can therefore exist once as HUMAN and once as VETERINARY.
     */
    boolean existsByNameIgnoreCaseAndUsageType(String name, UsageType usageType);
}



