package Repositories;

import Models.ValeurReelleParametre;
import Models.ExecutionProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ValeurReelleParametreRepository extends JpaRepository<ValeurReelleParametre, Long> {

    /**
     * Récupère toutes les valeurs réelles d'une exécution
     */
    List<ValeurReelleParametre> findByExecutionProduction(ExecutionProduction execution);

    /**
     * Récupère les valeurs réelles créées après une date
     */
    @Query("SELECT v FROM ValeurReelleParametre v WHERE v.dateCreation >= :depuis")
    List<ValeurReelleParametre> findValeursSinceDate(@Param("depuis") LocalDateTime depuis);

    /**
     * Récupère les valeurs réelles avec déviation importante
     */
    @Query("SELECT v FROM ValeurReelleParametre v WHERE v.qualiteDeviation = 'IMPORTANTE'")
    List<ValeurReelleParametre> findOutlierValues();

    /**
     * Compte les valeurs réelles par exécution
     */
    @Query("SELECT COUNT(v) FROM ValeurReelleParametre v WHERE v.executionProduction = :execution")
    long countByExecution(@Param("execution") ExecutionProduction execution);

    /**
     * Récupère les valeurs réelles pour export (CSV/réentraînement)
     */
    @Query("""
                SELECT v FROM ValeurReelleParametre v
            WHERE v.dateCreation BETWEEN :depuis AND :jusqu
            ORDER BY v.dateCreation DESC
            """)
    List<ValeurReelleParametre> findForExport(
            @Param("depuis") LocalDateTime depuis,
            @Param("jusqu") LocalDateTime jusqu);

    /**
     * Récupère les paramètres spécifiques (température, durée, vitesse, pression)
     */
    @Query("""
                SELECT v FROM ValeurReelleParametre v
                JOIN v.parametreEtape p
                WHERE p.codeParametre IN ('temperature_malaxage_c', 'duree_malaxage_min',
                                         'vitesse_decanteur_tr_min', 'pression_extraction_bar')
                AND v.dateCreation >= :depuis
            """)
    List<ValeurReelleParametre> findMainParametersSince(@Param("depuis") LocalDateTime depuis);
}
