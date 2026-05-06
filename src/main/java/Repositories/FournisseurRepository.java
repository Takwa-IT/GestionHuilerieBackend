package Repositories;

import Models.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    boolean existsByCin(String cin);

    Optional<Fournisseur> findByCin(String cin);
}
