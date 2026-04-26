package Repositories;

import Models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByLotOlives_Huilerie_IdHuilerieAndVariete(Long huilerieId, String variete);

    // Compatibilité temporaire avec les appels existants le temps de la migration des usages.
    Optional<Stock> findByLotOlives_Huilerie_IdHuilerieAndLotOlives_MatierePremiere_Id(Long huilerieId, Long matierePremiereId);

    // Filtration de stock par huilerie
    List<Stock> findByLotOlives_Huilerie_IdHuilerie(Long huilerieId);

    List<Stock> findByLotOlives_Huilerie_NomIgnoreCase(String huilerieNom);
}
