package Repositories;

import Models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    // Recherche stock par huilerie et matière première (modèle consolidé)
    Optional<Stock> findByHuilerie_IdHuilerieAndMatierePremiere_Id(Long huilerieId, Long matierePremiereId);

    // Filtration de stock par huilerie
    List<Stock> findByHuilerie_IdHuilerie(Long huilerieId);

    List<Stock> findByHuilerie_NomIgnoreCase(String huilerieNom);
}
