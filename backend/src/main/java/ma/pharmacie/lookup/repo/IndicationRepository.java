package ma.pharmacie.lookup.repo;

import ma.pharmacie.lookup.entity.Indication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicationRepository extends JpaRepository<Indication, Integer> {
}

