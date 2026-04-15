package Services;

import Config.ReferenceUtils;
import Models.AnalyseLaboratoire;
import Models.ExecutionProduction;
import Models.LotOlives;
import Models.ProduitFinal;
import Models.Stock;
import Models.StockMovement;
import Models.Utilisateur;
import Repositories.AnalyseLaboratoireRepository;
import Repositories.ExecutionProductionRepository;
import Repositories.LotOlivesRepository;
import Repositories.StockRepository;
import Repositories.StockMovementRepository;
import dto.LotTraceabilityDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TraceabilityService {

    private final AnalyseLaboratoireRepository analyseLaboratoireRepository;
    private final LotOlivesRepository lotOlivesRepository;
    private final ExecutionProductionRepository executionProductionRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockRepository stockRepository;
    private final CurrentUserService currentUserService;

    public LotTraceabilityDTO getLotHistory(Long lotId) {
        LotOlives lot = lotOlivesRepository.findById(lotId)
                .orElseThrow(() -> new EntityNotFoundException("Lot non trouve"));

        // Avec le nouveau modèle, chercher le stock via la matière première et
        // l'huilerie
        List<Stock> lotStocks = lot.getHuilerie() != null && lot.getMatierePremiere() != null
                ? stockRepository.findByHuilerie_IdHuilerieAndMatierePremiere_Id(
                        lot.getHuilerie().getIdHuilerie(),
                        lot.getMatierePremiere().getId())
                        .map(java.util.List::of)
                        .orElse(java.util.List.of())
                : java.util.List.of();

        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();
            boolean inAdminScope = lotStocks.stream()
                    .map(Stock::getHuilerie)
                    .filter(huilerie -> huilerie != null && huilerie.getIdHuilerie() != null)
                    .map(huilerie -> huilerie.getIdHuilerie())
                    .anyMatch(accessibleHuilerieIds::contains);

            if (!inAdminScope && lot.getHuilerie() != null) {
                if (!accessibleHuilerieIds.contains(lot.getHuilerie().getIdHuilerie())) {
                    throw new AccessDeniedException("Acces refuse a un lot d'une autre entreprise");
                }
            }
        } else {
            Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
            if (lot.getHuilerie() == null || !lot.getHuilerie().getIdHuilerie().equals(huilerieId)) {
                throw new AccessDeniedException("Acces refuse a un lot d'une autre huilerie");
            }
        }

        List<LotTraceabilityDTO.LifecycleItem> events = new ArrayList<>();
        List<LotTraceabilityDTO.AnalyseItem> analyses = new ArrayList<>();

        LotTraceabilityDTO.LifecycleItem lotEvent = new LotTraceabilityDTO.LifecycleItem();
        lotEvent.setDate(lot.getDateReception());
        lotEvent.setEtape("LotOlives");
        lotEvent.setDescription("Reception lot OL-" + lot.getIdLot() + " variete " + lot.getVarieteOlive());
        lotEvent.setReference(lot.getReference());
        events.add(lotEvent);

        for (StockMovement movement : stockMovementRepository
                .findByLotOlives_IdLotOrderByDateMouvementAsc(lotId)) {
            LotTraceabilityDTO.LifecycleItem event = new LotTraceabilityDTO.LifecycleItem();
            event.setDate(movement.getDateMouvement());
            event.setEtape("Stock");
            event.setDescription("Mouvement " + movement.getTypeMouvement() + " applique sur le lot entier");
            event.setReference(ReferenceUtils.format("ST", movement.getIdStockMovement()));
            events.add(event);
        }

        for (ExecutionProduction executionProduction : executionProductionRepository.findByLotOlives_IdLot(lotId)) {
            LotTraceabilityDTO.LifecycleItem event = new LotTraceabilityDTO.LifecycleItem();
            event.setDate(executionProduction.getDateDebut());
            event.setEtape("ExecutionProduction");
            event.setDescription("Lot utilise en execution de production, statut = " + executionProduction.getStatut());
            event.setReference(ReferenceUtils.format("EX", executionProduction.getIdExecutionProduction()));
            events.add(event);

            ProduitFinal produitFinal = executionProduction.getProduitFinal();
            if (produitFinal != null) {
                LotTraceabilityDTO.LifecycleItem produitEvent = new LotTraceabilityDTO.LifecycleItem();
                produitEvent.setDate(produitFinal.getDateProduction());
                produitEvent.setEtape("ProduitFinal");
                produitEvent.setDescription(
                        produitFinal.getNomProduit() + " produit, quantite = " + produitFinal.getQuantiteProduite());
                produitEvent.setReference(produitFinal.getReference());
                events.add(produitEvent);
            }
        }

        for (AnalyseLaboratoire analyse : analyseLaboratoireRepository.findByLot_IdLotOrderByDateAnalyseAsc(lotId)) {
            LotTraceabilityDTO.AnalyseItem analyseItem = new LotTraceabilityDTO.AnalyseItem();
            analyseItem.setIdAnalyse(analyse.getIdAnalyse());
            analyseItem.setDate(analyse.getDateAnalyse());
            analyseItem.setAcidite(analyse.getAcidite());
            analyseItem.setIndicePeroxyde(analyse.getIndicePeroxyde());
            analyseItem.setK232(analyse.getK232());
            analyseItem.setK270(analyse.getK270());
            analyseItem.setClasseQualiteFinale(analyse.getClasseQualiteFinale());
            analyses.add(analyseItem);
        }

        events.sort(Comparator.comparing(LotTraceabilityDTO.LifecycleItem::getDate,
                Comparator.nullsLast(String::compareTo)));

        LotTraceabilityDTO dto = new LotTraceabilityDTO();
        dto.setLotId(lot.getIdLot());
        dto.setVarieteOlive(lot.getVarieteOlive());
        dto.setOrigine(lot.getOrigine());
        dto.setQuantiteInitiale(lot.getQuantiteInitiale());
        dto.setQuantiteRestante(lot.getQuantiteRestante());
        dto.setAnalyses(analyses);
        dto.setCycleVie(events);
        return dto;
    }
}
