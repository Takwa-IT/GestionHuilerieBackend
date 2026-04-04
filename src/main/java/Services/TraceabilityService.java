package Services;

import Models.AnalyseLaboratoire;
import Models.ExecutionProduction;
import Models.LotOlives;
import Models.Pesee;
import Models.ProduitFinal;
import Models.StockMovement;
import Repositories.AnalyseLaboratoireRepository;
import Repositories.ExecutionProductionRepository;
import Repositories.LotOlivesRepository;
import Repositories.PeseeRepository;
import Repositories.StockMovementRepository;
import dto.LotTraceabilityDTO;
import lombok.RequiredArgsConstructor;
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
    private final PeseeRepository peseeRepository;
    private final ExecutionProductionRepository executionProductionRepository;
    private final StockMovementRepository stockMovementRepository;

    public LotTraceabilityDTO getLotHistory(Long lotId) {
        LotOlives lot = lotOlivesRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot non trouve"));

        List<LotTraceabilityDTO.LifecycleItem> events = new ArrayList<>();
        List<LotTraceabilityDTO.PeseeItem> pesees = new ArrayList<>();
        List<LotTraceabilityDTO.AnalyseItem> analyses = new ArrayList<>();

        LotTraceabilityDTO.LifecycleItem lotEvent = new LotTraceabilityDTO.LifecycleItem();
        lotEvent.setDate(lot.getDateReception());
        lotEvent.setEtape("LotOlives");
        lotEvent.setDescription("Reception lot OL-" + lot.getIdLot() + " variete " + lot.getVarieteOlive());
        lotEvent.setReference("LOT-OL-" + lot.getIdLot());
        events.add(lotEvent);

        for (Pesee pesee : peseeRepository.findByLot_IdLotOrderByDatePeseeDesc(lotId)) {
            LotTraceabilityDTO.PeseeItem peseeDTO = new LotTraceabilityDTO.PeseeItem();
            peseeDTO.setIdPesee(pesee.getIdPesee());
            peseeDTO.setDate(pesee.getDatePesee());
            peseeDTO.setPoidsBrut(pesee.getPoidsBrut());
            peseeDTO.setPoidsTare(pesee.getPoidsTare());
            peseeDTO.setPoidsNet(pesee.getPoidsNet());
            pesees.add(peseeDTO);

            LotTraceabilityDTO.LifecycleItem event = new LotTraceabilityDTO.LifecycleItem();
            event.setDate(pesee.getDatePesee());
            event.setEtape("Pesee");
            event.setDescription("Pesee enregistree, poids net = " + pesee.getPoidsNet() + " kg");
            event.setReference("PES-" + pesee.getIdPesee());
            events.add(event);
        }

        for (StockMovement movement : stockMovementRepository.findByStock_LotOlives_IdLotOrderByDateMouvementAsc(lotId)) {
            LotTraceabilityDTO.LifecycleItem event = new LotTraceabilityDTO.LifecycleItem();
            event.setDate(movement.getDateMouvement());
            event.setEtape("Stock");
            event.setDescription("Mouvement " + movement.getTypeMouvement() + ", quantite = " + movement.getQuantite() + " kg");
            event.setReference("STOCK-" + movement.getIdStockMovement());
            events.add(event);
        }

        for (ExecutionProduction executionProduction : executionProductionRepository.findByLotOlives_IdLot(lotId)) {
            LotTraceabilityDTO.LifecycleItem event = new LotTraceabilityDTO.LifecycleItem();
            event.setDate(executionProduction.getDateDebut());
            event.setEtape("ExecutionProduction");
            event.setDescription("Lot utilise en execution de production, statut = " + executionProduction.getStatut());
            event.setReference("EXE-" + executionProduction.getIdExecutionProduction());
            events.add(event);

            ProduitFinal produitFinal = executionProduction.getProduitFinal();
            if (produitFinal != null) {
                LotTraceabilityDTO.LifecycleItem produitEvent = new LotTraceabilityDTO.LifecycleItem();
                produitEvent.setDate(produitFinal.getDateProduction());
                produitEvent.setEtape("ProduitFinal");
                produitEvent.setDescription(produitFinal.getNomProduit() + " produit, quantite = " + produitFinal.getQuantiteProduite());
                produitEvent.setReference("PF-" + produitFinal.getIdProduit());
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

        events.sort(Comparator.comparing(LotTraceabilityDTO.LifecycleItem::getDate, Comparator.nullsLast(String::compareTo)));

        LotTraceabilityDTO dto = new LotTraceabilityDTO();
        dto.setLotId(lot.getIdLot());
        dto.setVarieteOlive(lot.getVarieteOlive());
        dto.setOrigine(lot.getOrigine());
        dto.setQuantiteInitiale(lot.getQuantiteInitiale());
        dto.setQuantiteRestante(lot.getQuantiteRestante());
        dto.setPesees(pesees);
        dto.setAnalyses(analyses);
        dto.setCycleVie(events);
        return dto;
    }
}
