package Services;

import Mapper.LotOlivesMapper;
import Models.LotOlives;
import Models.Utilisateur;
import Repositories.LotOlivesRepository;
import dto.LotOlivesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotOlivesService {

    private final LotOlivesRepository lotOlivesRepository;
    private final LotOlivesMapper lotOlivesMapper;
    private final CurrentUserService currentUserService;

    public List<LotOlivesDTO> findAll(String huilerieNom) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<LotOlives> lots = hasText(huilerieNom)
                    ? lotOlivesRepository.findAllByHuilerieNom(huilerieNom)
                    : lotOlivesRepository.findAll();
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();
            return lots.stream()
                    .filter(lot -> lot.getStocks() != null && lot.getStocks().stream()
                            .map(Models.Stock::getHuilerie)
                            .filter(huilerie -> huilerie != null && huilerie.getIdHuilerie() != null)
                            .map(Models.Huilerie::getIdHuilerie)
                            .anyMatch(accessibleHuilerieIds::contains))
                    .map(lotOlivesMapper::toDTO)
                    .toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return lotOlivesRepository.findAllByHuilerieId(huilerieId).stream().map(lotOlivesMapper::toDTO).toList();
    }

    public LotOlivesDTO findById(Long idLot) {
        return lotOlivesMapper.toDTO(findLot(idLot));
    }

    public LotOlives findLot(Long idLot) {
        return lotOlivesRepository.findById(idLot)
                .orElseThrow(() -> new RuntimeException("Lot non trouve"));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
