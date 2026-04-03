package Repositories;

import Models.GuideProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuideProductionRepository extends JpaRepository<GuideProduction, Long> {
    boolean existsByCode(String code);
}