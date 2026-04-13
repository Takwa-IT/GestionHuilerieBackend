package Repositories;

import Models.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    @Query("select m from Machine m where lower(m.huilerie.nom) = lower(:huilerieNom)")
    List<Machine> findByHuilerieNom(@Param("huilerieNom") String huilerieNom);

    List<Machine> findByHuilerie_IdHuilerie(Long idHuilerie);
    List<Machine> findByHuilerie_NomIgnoreCase(String huilerieNom);
}
