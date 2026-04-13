package Mapper;

import Models.LotOlives;
import Models.Huilerie;
import Models.Stock;
import dto.LotOlivesDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class LotOlivesMapper {

    public LotOlivesDTO toDTO(LotOlives entity) {
        if (entity == null) {
            return null;
        }

        LotOlivesDTO dto = new LotOlivesDTO();
        dto.setIdLot(entity.getIdLot());
        dto.setReference(entity.getReference());
        dto.setVarieteOlive(entity.getVarieteOlive());
        dto.setMaturite(entity.getMaturite());
        dto.setOrigine(entity.getOrigine());
        dto.setDateRecolte(entity.getDateRecolte());
        dto.setDateReception(entity.getDateReception());
        dto.setDureeStockageAvantBroyage(entity.getDureeStockageAvantBroyage());
        dto.setQuantiteInitiale(entity.getQuantiteInitiale());
        dto.setQuantiteRestante(entity.getQuantiteRestante());
        dto.setMatierePremiereId(entity.getMatierePremiere() != null ? entity.getMatierePremiere().getId() : null);
        dto.setMatierePremiereReference(entity.getMatierePremiere() != null ? entity.getMatierePremiere().getReference() : null);
        dto.setCampagneId(entity.getCampagne() != null ? entity.getCampagne().getIdCampagne() : null);
        Huilerie huilerie = resolveHuilerie(entity);
        dto.setHuilerieId(huilerie != null ? huilerie.getIdHuilerie() : null);
        dto.setHuilerieNom(huilerie != null ? huilerie.getNom() : null);
        return dto;
    }

    private Huilerie resolveHuilerie(LotOlives entity) {
        if (entity == null || entity.getStocks() == null) {
            return null;
        }

        Set<Long> huilerieIds = entity.getStocks().stream()
                .map(Stock::getHuilerie)
                .filter(huilerie -> huilerie != null && huilerie.getIdHuilerie() != null)
                .map(Huilerie::getIdHuilerie)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (huilerieIds.size() == 1) {
            Long huilerieId = huilerieIds.iterator().next();
            return entity.getStocks().stream()
                    .map(Stock::getHuilerie)
                    .filter(huilerie -> huilerie != null && huilerieId.equals(huilerie.getIdHuilerie()))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
