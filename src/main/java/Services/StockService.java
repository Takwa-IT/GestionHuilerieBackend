package Services;

import Mapper.StockMapper;
import Models.LotOlives;
import Models.Stock;
import Models.StockMovement;
import Models.Utilisateur;
import Repositories.LotOlivesRepository;
import Repositories.StockMovementRepository;
import Repositories.StockRepository;
import dto.StockDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final LotOlivesRepository lotOlivesRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockMapper stockMapper;
    private final CurrentUserService currentUserService;

    public List<StockDTO> findAll(String huilerieNom) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<Models.Stock> stocks = hasText(huilerieNom)
                    ? stockRepository.findByLotOlives_Huilerie_NomIgnoreCase(huilerieNom)
                    : stockRepository.findAll();
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();
            stocks = stocks.stream()
                    .filter(stock -> stock.getLotOlives() != null
                            && stock.getLotOlives().getHuilerie() != null
                            && accessibleHuilerieIds.contains(stock.getLotOlives().getHuilerie().getIdHuilerie()))
                    .toList();
            return aggregateAndMapStocks(stocks);
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        List<Models.Stock> stocks = stockRepository.findByLotOlives_Huilerie_IdHuilerie(huilerieId);
        return aggregateAndMapStocks(stocks);
    }

    public List<StockDTO> findByLot(Long lotId, String huilerieNom) {
        LotOlives lot = lotOlivesRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot non trouve"));

        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<Models.Stock> stocks = hasText(huilerieNom)
                    ? stockRepository.findByLotOlives_Huilerie_NomIgnoreCase(huilerieNom)
                    : stockRepository.findAll();
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();
            return stocks.stream()
                    .filter(stock -> stock.getLotOlives() != null
                            && stock.getLotOlives().getHuilerie() != null
                            && accessibleHuilerieIds.contains(stock.getLotOlives().getHuilerie().getIdHuilerie())
                            && stock.getLotOlives() != null
                            && stock.getLotOlives().getMatierePremiere() != null
                            && lot.getMatierePremiere() != null
                            && stock.getLotOlives().getMatierePremiere().getId().equals(lot.getMatierePremiere().getId()))
                    .map(this::toDTO)
                    .toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return stockRepository.findByLotOlives_Huilerie_IdHuilerie(huilerieId).stream()
                .filter(stock -> stock.getLotOlives() != null
                        && stock.getLotOlives().getMatierePremiere() != null
                        && lot.getMatierePremiere() != null
                        && stock.getLotOlives().getMatierePremiere().getId().equals(lot.getMatierePremiere().getId()))
                .map(this::toDTO)
                .toList();
    }

    public StockDTO findByLotAndHuilerie(Long lotId, Long huilerieId) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        Long effectiveHuilerieId = huilerieId;

        if (!currentUserService.isAdmin(utilisateur)) {
            Long currentHuilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
            if (!currentHuilerieId.equals(huilerieId)) {
                throw new AccessDeniedException("Acces refuse a une autre huilerie");
            }
            effectiveHuilerieId = currentHuilerieId;
        } else {
            currentUserService.ensureCanAccessHuilerie(huilerieId);
        }

        LotOlives lot = lotOlivesRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot non trouve"));

        return stockRepository.findByLotOlives_Huilerie_IdHuilerie(effectiveHuilerieId).stream()
                .filter(stock -> stock.getLotOlives() != null
                        && stock.getLotOlives().getMatierePremiere() != null
                        && lot.getMatierePremiere() != null
                        && stock.getLotOlives().getMatierePremiere().getId().equals(lot.getMatierePremiere().getId()))
                .findFirst()
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Stock non trouve"));
    }

    public List<StockDTO> findAllByHuilerieId(Long huilerieId) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();

        if (!currentUserService.isAdmin(utilisateur)) {
            Long currentHuilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
            if (!currentHuilerieId.equals(huilerieId)) {
                throw new AccessDeniedException("Acces refuse a une autre huilerie");
            }
            List<Models.Stock> stocks = stockRepository.findByLotOlives_Huilerie_IdHuilerie(currentHuilerieId);
            return aggregateAndMapStocks(stocks);
        }

        currentUserService.ensureCanAccessHuilerie(huilerieId);
        List<Models.Stock> stocks = stockRepository.findByLotOlives_Huilerie_IdHuilerie(huilerieId);
        return aggregateAndMapStocks(stocks);
    }

    public StockDTO findById(Long idStock) {
        Stock stock = stockRepository.findById(idStock)
                .orElseThrow(() -> new RuntimeException("Stock non trouve"));

        if (stock.getLotOlives() == null || stock.getLotOlives().getHuilerie() == null || stock.getLotOlives().getHuilerie().getIdHuilerie() == null) {
            throw new RuntimeException("Stock sans huilerie associee");
        }

        currentUserService.ensureCanAccessHuilerie(stock.getLotOlives().getHuilerie().getIdHuilerie());
        return toDTO(stock);
    }

    private StockDTO toDTO(Stock stock) {
        StockDTO dto = stockMapper.toDTO(stock);
        Long matierePremiereId = resolveMatierePremiereId(stock);
        dto.setMatierePremiereId(matierePremiereId);
        dto.setLotReferences(resolveLotReferences(stock, matierePremiereId));
        return dto;
    }

    private Long resolveMatierePremiereId(Stock stock) {
        if (stock == null || stock.getLotOlives() == null || stock.getLotOlives().getMatierePremiere() == null) {
            return null;
        }
        return stock.getLotOlives().getMatierePremiere().getId();
    }

    private List<String> resolveLotReferences(Stock stock, Long matierePremiereId) {
        if (stock == null || stock.getIdStock() == null) {
            return List.of();
        }

        Set<String> lotReferences = new LinkedHashSet<>();
        List<StockMovement> movements = stockMovementRepository
                .findByStock_IdStockOrderByDateMouvementAsc(stock.getIdStock());

        for (StockMovement movement : movements) {
            if (movement.getLotOlives() != null
                    && movement.getLotOlives().getReference() != null
                    && isSameMatierePremiere(movement.getLotOlives(), matierePremiereId)) {
                lotReferences.add(movement.getLotOlives().getReference());
            }
        }

        if (stock.getLotOlives() != null
                && stock.getLotOlives().getReference() != null
                && isSameMatierePremiere(stock.getLotOlives(), matierePremiereId)) {
            lotReferences.add(stock.getLotOlives().getReference());
        }

        return List.copyOf(lotReferences);
    }

    private boolean isSameMatierePremiere(LotOlives lot, Long expectedMatierePremiereId) {
        if (lot == null || lot.getMatierePremiere() == null || expectedMatierePremiereId == null) {
            return false;
        }

        Long lotMatiereId = lot.getMatierePremiere().getId();
        return lotMatiereId != null && expectedMatierePremiereId.equals(lotMatiereId);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Agrège les stocks par (huilerieId, variété normalisée) et somme les quantités.
     * Retourne une liste de StockDTO représentant les stocks uniques avec quantités fusionnées.
     */
    private List<StockDTO> aggregateAndMapStocks(List<Stock> stocks) {
        // Clé : "huilerieId|variete"
        Map<String, AggregatedStock> aggregated = stocks.stream()
                .filter(stock -> stock.getLotOlives() != null
                        && stock.getLotOlives().getHuilerie() != null
                        && stock.getVariete() != null)
                .collect(Collectors.toMap(
                        stock -> buildAggregationKey(stock),
                        stock -> new AggregatedStock(stock),
                        AggregatedStock::merge
                ));

        return aggregated.values().stream()
                .map(AggregatedStock::toDTO)
                .collect(Collectors.toList());
    }

    private String buildAggregationKey(Stock stock) {
        Long huilerieId = stock.getLotOlives().getHuilerie().getIdHuilerie();
        String variete = stock.getVariete();
        return huilerieId + "|" + variete;
    }

    /**
     * Classe interne pour aggréger les données de stock par (huilerie, variété).
     */
    private class AggregatedStock {
        private Long huilerieId;
        private String huilerieNom;
        private String typeStock;
        private String variete;
        private Double quantiteDisponible;
        private Long idStock; // ID du premier stock
        private Set<Long> stockIds;
        private Set<String> lotReferences;

        AggregatedStock(Stock stock) {
            this.huilerieId = stock.getLotOlives().getHuilerie().getIdHuilerie();
            this.huilerieNom = stock.getLotOlives().getHuilerie().getNom();
            this.typeStock = stock.getTypeStock();
            this.variete = stock.getVariete();
            this.quantiteDisponible = safe(stock.getQuantiteDisponible());
            this.idStock = stock.getIdStock();
            this.stockIds = new LinkedHashSet<>();
            this.stockIds.add(stock.getIdStock());
            this.lotReferences = new LinkedHashSet<>();
            collectLotReferences(stock);
        }

        AggregatedStock merge(AggregatedStock other) {
            this.quantiteDisponible += safe(other.quantiteDisponible);
            this.stockIds.addAll(other.stockIds);
            other.lotReferences.forEach(this.lotReferences::add);
            return this;
        }

        private void collectLotReferences(Stock stock) {
            if (stock == null || stock.getIdStock() == null) {
                return;
            }

            // Récupérer les références de lots via les mouvements
            List<StockMovement> movements = stockMovementRepository
                    .findByStock_IdStockOrderByDateMouvementAsc(stock.getIdStock());

            for (StockMovement movement : movements) {
                if (movement.getLotOlives() != null
                        && movement.getLotOlives().getReference() != null) {
                    lotReferences.add(movement.getLotOlives().getReference());
                }
            }

            // Ajouter la référence du lot d'origine
            if (stock.getLotOlives() != null
                    && stock.getLotOlives().getReference() != null) {
                lotReferences.add(stock.getLotOlives().getReference());
            }
        }

        StockDTO toDTO() {
            StockDTO dto = new StockDTO();
            dto.setIdStock(idStock);
            dto.setHuilerieId(huilerieId);
            dto.setHuilerieNom(huilerieNom);
            dto.setTypeStock(typeStock);
            dto.setVariete(variete);
            dto.setQuantiteDisponible(quantiteDisponible);
            dto.setReference("ST-" + idStock); // Ou utiliser ReferenceUtils.format si préféré
            dto.setLotReferences(List.copyOf(lotReferences));
            return dto;
        }
    }

    private Double safe(Double value) {
        return value != null ? value : 0d;
    }
}

