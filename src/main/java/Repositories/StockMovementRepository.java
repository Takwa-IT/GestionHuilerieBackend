package Repositories;

import Models.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByStock_IdStockOrderByDateMouvementAsc(Long stockId);

    List<StockMovement> findByLotOlives_IdLotOrderByDateMouvementAsc(Long lotId);

    List<StockMovement> findByStock_LotOlives_Huilerie_IdHuilerieOrderByDateMouvementDesc(Long huilerieId);

    List<StockMovement> findByStock_LotOlives_Huilerie_NomIgnoreCaseOrderByDateMouvementDesc(String huilerieNom);
}
// DEPRECATED: Use MouvementRepository instead
