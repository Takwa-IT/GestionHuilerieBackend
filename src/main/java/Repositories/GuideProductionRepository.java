package Repositories;

import Models.GuideProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuideProductionRepository extends JpaRepository<GuideProduction, Long> {
    List<GuideProduction> findByHuilerie_IdHuilerie(Long huilerieId);
    List<GuideProduction> findByHuilerie_NomIgnoreCase(String huilerieNom);
}


