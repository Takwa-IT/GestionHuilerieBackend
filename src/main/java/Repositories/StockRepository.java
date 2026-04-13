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
    //filtration de stock par huilerie
    List<Stock> findByHuilerie_IdHuilerie(Long huilerieId);
    List<Stock> findByHuilerie_NomIgnoreCase(String huilerieNom);
    //recherche stock par id lot
    List<Stock> findByLotOlives_IdLot(Long lotId);
    List<Stock> findByLotOlives_IdLotAndHuilerie_NomIgnoreCase(Long lotId, String huilerieNom);
    //recherche stock par id lot et huilerie
    List<Stock> findByLotOlives_IdLotAndHuilerie_IdHuilerie(Long lotId, Long huilerieId);
}
