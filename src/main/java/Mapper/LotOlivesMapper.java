package Mapper;

import Models.Huilerie;
import Models.LotOlives;
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
        dto.setRegion(entity.getRegion());
        dto.setMethodeRecolte(entity.getMethodeRecolte());
        dto.setTypeSol(entity.getTypeSol());
        dto.setTempsDepuisRecolteHeures(entity.getTempsDepuisRecolteHeures());
        dto.setHumiditePourcent(entity.getHumiditePourcent());
        dto.setAciditeOlivesPourcent(entity.getAciditeOlivesPourcent());
        dto.setTauxFeuillesPourcent(entity.getTauxFeuillesPourcent());
        dto.setLavageEffectue(entity.getLavageEffectue());
        dto.setDateRecolte(entity.getDateRecolte());
        dto.setDateReception(entity.getDateReception());
        dto.setFournisseurNom(entity.getFournisseur() != null ? entity.getFournisseur().getNom() : null);
        dto.setFournisseurCIN(entity.getFournisseur() != null ? entity.getFournisseur().getCin() : null);
        dto.setDureeStockageAvantBroyage(entity.getDureeStockageAvantBroyage());
        dto.setPesee(entity.getPesee());
        dto.setQuantiteInitiale(entity.getQuantiteInitiale());
        dto.setQuantiteRestante(entity.getQuantiteRestante());
        dto.setBonPeseePdfPath(entity.getBonPeseePdfPath());
        dto.setFournisseurId(entity.getFournisseur() != null ? entity.getFournisseur().getIdFournisseur() : null);
        dto.setMatierePremiereId(entity.getMatierePremiere() != null ? entity.getMatierePremiere().getId() : null);
        dto.setMatierePremiereReference(
                entity.getMatierePremiere() != null ? entity.getMatierePremiere().getReference() : null);
        dto.setCampagneId(entity.getCampagne() != null ? entity.getCampagne().getIdCampagne() : null);
        Huilerie huilerie = resolveHuilerie(entity);
        dto.setHuilerieId(huilerie != null ? huilerie.getIdHuilerie() : null);
        dto.setHuilerieNom(huilerie != null ? huilerie.getNom() : null);
        return dto;
    }

    private Huilerie resolveHuilerie(LotOlives entity) {
        if (entity == null) {
            return null;
        }

        if (entity.getHuilerie() != null && entity.getHuilerie().getIdHuilerie() != null) {
            return entity.getHuilerie();
        }

        if (entity.getStocks() == null) {
            return null;
        }

        Set<Long> huilerieIds = entity.getStocks().stream()
                .map(stock -> stock.getLotOlives() != null ? stock.getLotOlives().getHuilerie() : null)
                .filter(huilerie -> huilerie != null && huilerie.getIdHuilerie() != null)
                .map(Huilerie::getIdHuilerie)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (huilerieIds.size() == 1) {
            Long huilerieId = huilerieIds.iterator().next();
            return entity.getStocks().stream()
                    .map(stock -> stock.getLotOlives() != null ? stock.getLotOlives().getHuilerie() : null)
                    .filter(huilerie -> huilerie != null && huilerieId.equals(huilerie.getIdHuilerie()))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
