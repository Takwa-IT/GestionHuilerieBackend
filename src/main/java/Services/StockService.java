package Services;

import Mapper.StockMapper;
import Models.LotOlives;
import Models.Utilisateur;
import Repositories.LotOlivesRepository;
import Repositories.StockRepository;
import dto.StockDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final LotOlivesRepository lotOlivesRepository;
    private final StockMapper stockMapper;
    private final CurrentUserService currentUserService;

    public List<StockDTO> findAll(String huilerieNom) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<Models.Stock> stocks = hasText(huilerieNom)
                    ? stockRepository.findByHuilerie_NomIgnoreCase(huilerieNom)
                    : stockRepository.findAll();
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();
            return stocks.stream()
                    .filter(stock -> stock.getHuilerie() != null
                            && accessibleHuilerieIds.contains(stock.getHuilerie().getIdHuilerie()))
                    .map(stockMapper::toDTO)
                    .toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return stockRepository.findByHuilerie_IdHuilerie(huilerieId).stream().map(stockMapper::toDTO).toList();
    }

    public List<StockDTO> findByLot(Long lotId, String huilerieNom) {
        LotOlives lot = lotOlivesRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot non trouve"));

        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<Models.Stock> stocks = hasText(huilerieNom)
                    ? stockRepository.findByHuilerie_NomIgnoreCase(huilerieNom)
                    : stockRepository.findAll();
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();
            return stocks.stream()
                    .filter(stock -> stock.getHuilerie() != null
                            && accessibleHuilerieIds.contains(stock.getHuilerie().getIdHuilerie())
                            && stock.getMatierePremiere() != null
                            && lot.getMatierePremiere() != null
                            && stock.getMatierePremiere().getId().equals(lot.getMatierePremiere().getId()))
                    .map(stockMapper::toDTO)
                    .toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return stockRepository.findByHuilerie_IdHuilerie(huilerieId).stream()
                .filter(stock -> stock.getMatierePremiere() != null
                        && lot.getMatierePremiere() != null
                        && stock.getMatierePremiere().getId().equals(lot.getMatierePremiere().getId()))
                .map(stockMapper::toDTO)
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

        return stockRepository.findByHuilerie_IdHuilerie(effectiveHuilerieId).stream()
                .filter(stock -> stock.getMatierePremiere() != null
                        && lot.getMatierePremiere() != null
                        && stock.getMatierePremiere().getId().equals(lot.getMatierePremiere().getId()))
                .findFirst()
                .map(stockMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Stock non trouve"));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
