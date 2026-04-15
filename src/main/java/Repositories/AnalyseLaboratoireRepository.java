package Repositories;

import Models.AnalyseLaboratoire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyseLaboratoireRepository extends JpaRepository<AnalyseLaboratoire, Long> {
    //cherche les analyses d'un lot
    List<AnalyseLaboratoire> findByLot_IdLotOrderByDateAnalyseAsc(Long lotId);}


