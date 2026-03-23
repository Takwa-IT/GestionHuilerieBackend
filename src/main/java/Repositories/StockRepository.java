package Repositories;

import Models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    //filtration de stock par huilerie et lot
    Optional<Stock> findByHuilerie_IdHuilerieAndLotOlives_IdLot(Long huilerieId, Long lotId);
}
