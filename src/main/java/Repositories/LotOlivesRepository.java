package Repositories;

import Models.LotOlives;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LotOlivesRepository extends JpaRepository<LotOlives, Long> {
    Optional<LotOlives> findByReference(String reference);

    boolean existsByCampagne_IdCampagne(Long idCampagne);

    @Query("select l from LotOlives l where lower(l.huilerie.nom) = lower(:huilerieNom)")
    List<LotOlives> findAllByHuilerieNom(@Param("huilerieNom") String huilerieNom);

    @Query("select l from LotOlives l where l.huilerie.idHuilerie = :huilerieId")
    List<LotOlives> findAllByHuilerieId(@Param("huilerieId") Long huilerieId);
}
