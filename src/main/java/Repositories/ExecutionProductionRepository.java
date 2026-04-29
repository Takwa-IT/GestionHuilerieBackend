package Repositories;

import Models.ExecutionProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionProductionRepository extends JpaRepository<ExecutionProduction, Long> {
	@Query("select e from ExecutionProduction e where e.lot.idLot = :lotId")
	List<ExecutionProduction> findByLotOlives_IdLot(@Param("lotId") Long lotId);

	boolean existsByReference(String reference);

	default boolean existsByCodeLot(String codeLot) {
		return existsByReference(codeLot);
	}

	@Query("""
			select e
			from ExecutionProduction e
			left join e.guideProduction gp
			where gp.huilerie.idHuilerie = :huilerieId or gp is null
			""")
	List<ExecutionProduction> findAllByHuilerieId(@Param("huilerieId") Long huilerieId);

	@Query("""
			select e
			from ExecutionProduction e
			left join e.guideProduction gp
			where lower(gp.huilerie.nom) = lower(:huilerieNom) or gp is null
			""")
	List<ExecutionProduction> findAllByHuilerieNom(@Param("huilerieNom") String huilerieNom);
}
