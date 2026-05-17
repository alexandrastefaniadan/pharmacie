package ma.pharmacie.lookup.repo;

import ma.pharmacie.lookup.entity.AgeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgeGroupRepository extends JpaRepository<AgeGroup, Integer> {
}

