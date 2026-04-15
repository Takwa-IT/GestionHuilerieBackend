package Repositories;

import Models.ProduitFinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProduitFinalRepository extends JpaRepository<ProduitFinal, Long> {
    boolean existsByReference(String reference);
}


