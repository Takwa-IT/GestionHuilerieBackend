package Repositories;

import Models.ExecutionProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionProductionRepository extends JpaRepository<ExecutionProduction, Long> {
	List<ExecutionProduction> findByLotOlives_IdLot(Long lotId);
	boolean existsByCodeLot(String codeLot);

	@Query("""
			select e
			from ExecutionProduction e
			join e.guideProduction gp
			where gp.huilerie.idHuilerie = :huilerieId
			""")
	List<ExecutionProduction> findAllByHuilerieId(@Param("huilerieId") Long huilerieId);

	@Query("""
			select e
			from ExecutionProduction e
			join e.guideProduction gp
			where lower(gp.huilerie.nom) = lower(:huilerieNom)
			""")
	List<ExecutionProduction> findAllByHuilerieNom(@Param("huilerieNom") String huilerieNom);
}
