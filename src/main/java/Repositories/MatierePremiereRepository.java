package Repositories;

import Models.MatierePremiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatierePremiereRepository extends JpaRepository<MatierePremiere, Long> {
    Optional<MatierePremiere> findByReference(String reference);

    @Query("SELECT m FROM MatierePremiere m WHERE lower(trim(m.reference)) = lower(trim(:reference))")
    Optional<MatierePremiere> findByNormalizedReference(@Param("reference") String reference);

    Optional<MatierePremiere> findByNomIgnoreCase(String nom);

    boolean existsByReference(String reference);

    List<MatierePremiere> findByHuilerie_IdHuilerie(Long huilerieId);

    List<MatierePremiere> findByHuilerie_NomIgnoreCase(String huilerieNom);
}


