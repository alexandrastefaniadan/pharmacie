package ma.pharmacie.lookup.repo;

import ma.pharmacie.lookup.entity.PharmaceuticalForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmaceuticalFormRepository extends JpaRepository<PharmaceuticalForm, Integer> {
}

