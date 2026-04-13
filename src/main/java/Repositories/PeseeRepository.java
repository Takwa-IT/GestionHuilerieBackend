package Repositories;

import Models.Pesee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeseeRepository extends JpaRepository<Pesee, Long> {
    //cherche lot par pesee par id de lot
    List<Pesee> findByLot_IdLotOrderByDatePeseeDesc(Long lotId);

    List<Pesee> findAllByOrderByDatePeseeDesc();

        @Query("""
            select distinct p
            from Pesee p
            join p.lot l
            join l.stocks s
            where s.huilerie.idHuilerie = :huilerieId
            order by p.datePesee desc
            """)
        List<Pesee> findAllByHuilerie_IdHuilerieOrderByDatePeseeDesc(@Param("huilerieId") Long huilerieId);

        @Query("""
            select distinct p
            from Pesee p
            join p.lot l
            join l.stocks s
            where lower(s.huilerie.nom) = lower(:huilerieNom)
            order by p.datePesee desc
            """)
        List<Pesee> findAllByHuilerieNomOrderByDatePeseeDesc(@Param("huilerieNom") String huilerieNom);

    Optional<Pesee> findByReference(String reference);

    boolean existsByReference(String reference);
}
