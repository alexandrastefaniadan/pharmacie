package ma.pharmacie.medication.repo;

import ma.pharmacie.medication.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface MedicationRepository
        extends JpaRepository<Medication, UUID>, JpaSpecificationExecutor<Medication> {

    boolean existsByNameIgnoreCase(String name);
}

