package ma.pharmacie.treatment.repo;

import ma.pharmacie.common.enums.UsageType;
import ma.pharmacie.treatment.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TreatmentRepository
        extends JpaRepository<Treatment, UUID>, JpaSpecificationExecutor<Treatment> {

    /**
     * Active-name uniqueness is scoped per usage type (matches the partial
     * unique index {@code ux_treatment_name_active}).
     */
    boolean existsByNameIgnoreCaseAndUsageType(String name, UsageType usageType);
}

