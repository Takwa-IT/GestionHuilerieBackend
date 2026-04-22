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
import java.util.Set;

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
            return stocks.stream()
                    .filter(stock -> stock.getLotOlives() != null
                            && stock.getLotOlives().getHuilerie() != null
                            && accessibleHuilerieIds.contains(stock.getLotOlives().getHuilerie().getIdHuilerie()))
                    .map(this::toDTO)
                    .toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return stockRepository.findByLotOlives_Huilerie_IdHuilerie(huilerieId).stream().map(this::toDTO).toList();
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
            return stockRepository.findByLotOlives_Huilerie_IdHuilerie(currentHuilerieId).stream()
                    .map(this::toDTO)
                    .toList();
        }

        currentUserService.ensureCanAccessHuilerie(huilerieId);
        return stockRepository.findByLotOlives_Huilerie_IdHuilerie(huilerieId).stream()
                .map(this::toDTO)
                .toList();
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
}
