package Repositories;

import Models.CampagneOlives;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampagneOlivesRepository extends JpaRepository<CampagneOlives, Long> {
    Optional<CampagneOlives> findByAnnee(String annee);

    Optional<CampagneOlives> findByReference(String reference);
}
