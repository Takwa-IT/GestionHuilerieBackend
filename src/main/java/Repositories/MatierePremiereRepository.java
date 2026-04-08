package Repositories;

import Models.MatierePremiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatierePremiereRepository extends JpaRepository<MatierePremiere, Long> {
    Optional<MatierePremiere> findByReference(String reference);

    boolean existsByReference(String reference);
}
