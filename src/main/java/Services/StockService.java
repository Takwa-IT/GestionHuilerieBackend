package Services;

import Mapper.StockMapper;
import Models.Utilisateur;
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
    private final StockMapper stockMapper;
    private final CurrentUserService currentUserService;

    public List<StockDTO> findAll() {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            return stockRepository.findAll().stream().map(stockMapper::toDTO).toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return stockRepository.findByHuilerie_IdHuilerie(huilerieId).stream().map(stockMapper::toDTO).toList();
    }

    public List<StockDTO> findByLot(Long lotId) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            return stockRepository.findByLotOlives_IdLot(lotId).stream().map(stockMapper::toDTO).toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return stockRepository.findByLotOlives_IdLotAndHuilerie_IdHuilerie(lotId, huilerieId)
                .stream()
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
        }

        return stockRepository.findByHuilerie_IdHuilerieAndLotOlives_IdLot(effectiveHuilerieId, lotId)
                .map(stockMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Stock non trouve"));
    }
}
