package ma.pharmacie.auth.repo;

import ma.pharmacie.auth.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    /** Case-insensitive lookup used by Spring Security's authentication. */
    Optional<AppUser> findByUsernameIgnoreCase(String username);
}

