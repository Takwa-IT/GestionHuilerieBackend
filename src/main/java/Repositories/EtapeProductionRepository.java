package Repositories;

import Models.EtapeProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtapeProductionRepository extends JpaRepository<EtapeProduction, Long> {
}


