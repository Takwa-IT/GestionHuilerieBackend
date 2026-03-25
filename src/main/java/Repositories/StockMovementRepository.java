package Repositories;

import Models.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    //filtration des mouvement par stock
    List<StockMovement> findByStock_IdStockOrderByDateMouvementAsc(Long stockId);
    //filtration des mouvement par lot
    List<StockMovement> findByStock_LotOlives_IdLotOrderByDateMouvementAsc(Long lotId);

    List<StockMovement> findByStock_Huilerie_IdHuilerieOrderByDateMouvementDesc(Long huilerieId);
}
