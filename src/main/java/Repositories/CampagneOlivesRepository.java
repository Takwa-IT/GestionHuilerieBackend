package Repositories;

import Models.CampagneOlives;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampagneOlivesRepository extends JpaRepository<CampagneOlives, Long> {
    Optional<CampagneOlives> findByAnnee(String annee);

    Optional<CampagneOlives> findByReference(String reference);

    @Query("SELECT c FROM CampagneOlives c WHERE lower(trim(c.reference)) = lower(trim(:reference))")
    Optional<CampagneOlives> findByNormalizedReference(@Param("reference") String reference);

    List<CampagneOlives> findAllByHuilerie_IdHuilerie(Long huilerieId);

    List<CampagneOlives> findAllByHuilerie_Entreprise_IdEntreprise(Long entrepriseId);
}
