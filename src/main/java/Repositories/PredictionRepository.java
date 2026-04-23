package Repositories;

import Models.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    @Query("select p from Prediction p where p.executionProduction.idExecutionProduction = :executionId")
    List<Prediction> findByExecutionProductionId(@Param("executionId") Long executionId);

    @Query("select p from Prediction p where p.executionProduction.idExecutionProduction = :executionId")
    Optional<Prediction> findOneByExecutionProductionId(@Param("executionId") Long executionId);
}
