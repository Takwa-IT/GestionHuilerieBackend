package Repositories;

import Models.ExecutionProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionProductionRepository extends JpaRepository<ExecutionProduction, Long> {
	List<ExecutionProduction> findByLotOlives_IdLot(Long lotId);
	boolean existsByCodeLot(String codeLot);
}