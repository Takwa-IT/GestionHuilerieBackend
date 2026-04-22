package Services;

import Config.ReferenceUtils;
import Mapper.ProduitFinalMapper;
import Models.ExecutionProduction;
import Models.ParametreEtape;
import Models.ProduitFinal;
import Repositories.ExecutionProductionRepository;
import Repositories.ProduitFinalRepository;
import dto.ExecutionProductionDTO;
import dto.ProduitFinalCreateDTO;
import dto.ProduitFinalDTO;
import dto.ProduitFinalUpdateDTO;
import dto.ValeurReelleParametreDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProduitFinalService {

    private final ProduitFinalRepository produitFinalRepository;
    private final ExecutionProductionRepository executionProductionRepository;
    private final ProduitFinalMapper produitFinalMapper;

    public ExecutionProductionDTO create(ProduitFinalCreateDTO dto) {
        ExecutionProduction executionProduction = executionProductionRepository.findById(dto.getExecutionProductionId())
                .orElseThrow(() -> new RuntimeException("Execution de production non trouvee"));
        if (executionProduction.getProduitFinal() != null) {
            throw new RuntimeException("Un produit final existe deja pour cette execution");
        }

        ProduitFinal entity = produitFinalMapper.toEntity(dto);
        entity.setReference("TMP-PF-" + UUID.randomUUID());
        entity.setExecutionProduction(executionProduction);

        ProduitFinal savedProduitFinal = produitFinalRepository.save(entity);
        savedProduitFinal.setReference(ReferenceUtils.format("PF", savedProduitFinal.getIdProduit()));
        savedProduitFinal = produitFinalRepository.save(savedProduitFinal);
        executionProduction.setProduitFinal(savedProduitFinal);
        executionProduction.setStatut("TERMINEE");
        if (executionProduction.getDateFinReelle() == null || executionProduction.getDateFinReelle().isBlank()) {
            executionProduction.setDateFinReelle(dto.getDateProduction());
        }

        ExecutionProduction savedExecution = executionProductionRepository.save(executionProduction);
        return toExecutionDTO(savedExecution);
    }

    public ProduitFinalDTO update(Long idProduit, ProduitFinalUpdateDTO dto) {
        ProduitFinal entity = findProduitFinal(idProduit);
        produitFinalMapper.updateFromDTO(dto, entity);
        return produitFinalMapper.toDTO(produitFinalRepository.save(entity));
    }

    public void delete(Long idProduit) {
        produitFinalRepository.delete(findProduitFinal(idProduit));
    }

    @Transactional(readOnly = true)
    public ProduitFinalDTO findById(Long idProduit) {
        return produitFinalMapper.toDTO(findProduitFinal(idProduit));
    }

    @Transactional(readOnly = true)
    public List<ProduitFinalDTO> findAll() {
        return produitFinalRepository.findAll().stream().map(produitFinalMapper::toDTO).toList();
    }

    public ProduitFinal findProduitFinal(Long idProduit) {
        return produitFinalRepository.findById(idProduit)
                .orElseThrow(() -> new RuntimeException("Produit final non trouve"));
    }

    private ExecutionProductionDTO toExecutionDTO(ExecutionProduction executionProduction) {
        ExecutionProductionDTO dto = new ExecutionProductionDTO();
        dto.setIdExecutionProduction(executionProduction.getIdExecutionProduction());
        dto.setReference(executionProduction.getReference());
        dto.setDateDebut(executionProduction.getDateDebut());
        dto.setDateFinPrevue(executionProduction.getDateFinPrevue());
        dto.setDateFinReelle(executionProduction.getDateFinReelle());
        dto.setStatut(executionProduction.getStatut());
        dto.setRendement(executionProduction.getRendement());
        dto.setObservations(executionProduction.getObservations());

        if (executionProduction.getGuideProduction() != null) {
            dto.setGuideProductionId(executionProduction.getGuideProduction().getIdGuideProduction());
            dto.setGuideProductionReference(executionProduction.getGuideProduction().getReference());
        }
        if (executionProduction.getMachine() != null) {
            dto.setMachineId(executionProduction.getMachine().getIdMachine());
            dto.setMachineNom(executionProduction.getMachine().getNomMachine());
        }
        if (executionProduction.getLotOlives() != null) {
            dto.setLotId(executionProduction.getLotOlives().getIdLot());
            dto.setLotVariete(executionProduction.getLotOlives().getVarieteOlive());
        }
        dto.setValeursReelles(loadValeursReelles(executionProduction));
        if (executionProduction.getProduitFinal() != null) {
            dto.setProduitFinalId(executionProduction.getProduitFinal().getIdProduit());
            dto.setProduitFinalReference(executionProduction.getProduitFinal().getReference());
            dto.setProduitFinalNomProduit(executionProduction.getProduitFinal().getNomProduit());
        }
        return dto;
    }

    private java.util.List<ValeurReelleParametreDTO> loadValeursReelles(ExecutionProduction executionProduction) {
        if (executionProduction == null || executionProduction.getGuideProduction() == null) {
            return java.util.List.of();
        }

        if (executionProduction.getParametres() != null && !executionProduction.getParametres().isEmpty()) {
            return executionProduction.getParametres().stream()
                    .sorted(java.util.Comparator
                            .comparing((ParametreEtape p) -> p.getEtapeProduction() != null
                                    && p.getEtapeProduction().getOrdre() != null
                                    ? p.getEtapeProduction().getOrdre()
                                    : Integer.MAX_VALUE)
                            .thenComparing(p -> p.getIdParametreEtape() == null ? Long.MAX_VALUE : p.getIdParametreEtape()))
                    .map(this::toDTO)
                    .toList();
        }

        if (executionProduction.getGuideProduction().getEtapes() == null) {
            return java.util.List.of();
        }

        return executionProduction.getGuideProduction().getEtapes().stream()
                .sorted(java.util.Comparator.comparing(etape -> etape.getOrdre() == null ? Integer.MAX_VALUE : etape.getOrdre()))
                .flatMap(etape -> (etape.getParametres() == null ? java.util.List.<ParametreEtape>of() : etape.getParametres()).stream()
                        .filter(parametre -> parametre.getExecutionProduction() == null))
                .map(this::toDTO)
                .toList();
    }

    private ValeurReelleParametreDTO toDTO(ParametreEtape parametre) {
        ValeurReelleParametreDTO dto = new ValeurReelleParametreDTO();
        dto.setParametreEtapeId(parametre.getIdParametreEtape());
        dto.setParametreEtapeNom(parametre.getNom());
        dto.setValeurEstime(parametre.getValeur());
        dto.setValeurReelle(parametre.getValeurReelle());
        return dto;
    }
}
