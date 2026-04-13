package Repositories;

import Models.LotOlives;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotOlivesRepository extends JpaRepository<LotOlives, Long> {

	@Query("""
			select distinct l
			from LotOlives l
			join l.stocks s
			where s.huilerie.idHuilerie = :huilerieId
			""")
	List<LotOlives> findAllByHuilerieId(@Param("huilerieId") Long huilerieId);

	@Query("""
			select distinct l
			from LotOlives l
			join l.stocks s
			where lower(s.huilerie.nom) = lower(:huilerieNom)
			""")
	List<LotOlives> findAllByHuilerieNom(@Param("huilerieNom") String huilerieNom);
}
