package Repositories;

import Models.ExecutionProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionProductionRepository extends JpaRepository<ExecutionProduction, Long> {
}