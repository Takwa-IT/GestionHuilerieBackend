package Repositories;

import Models.MatierePremiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatierePremiereRepository extends JpaRepository<MatierePremiere, Long> {
    Optional<MatierePremiere> findByReference(String reference);

    boolean existsByReference(String reference);

    @Query("""
            select distinct mp
            from MatierePremiere mp
            join mp.lots l
            join l.stocks s
            where s.huilerie.idHuilerie = :huilerieId
            """)
    List<MatierePremiere> findByHuilerie_IdHuilerie(@Param("huilerieId") Long huilerieId);
}
