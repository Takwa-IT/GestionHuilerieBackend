package Services;

import Models.ExecutionProduction;
import Models.GuideProduction;
import Models.LotOlives;
import Models.Machine;
import Models.ParametreEtape;
import Models.Prediction;
import Models.TypeMouvement;
import Repositories.ParametreEtapeRepository;
import dto.ValeurReelleParametreDTO;

import Models.Utilisateur;
import Repositories.ExecutionProductionRepository;
import Repositories.GuideProductionRepository;
import Repositories.LotOlivesRepository;
import Repositories.MachineRepository;
import dto.ExecutionProductionCreateDTO;
import dto.ExecutionProductionDTO;
import dto.StockMovementCreateDTO;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ExecutionProductionService {

    private final ParametreEtapeRepository parametreEtapeRepository;
    private final ExecutionProductionRepository executionProductionRepository;
    private final GuideProductionRepository guideProductionRepository;
    private final MachineRepository machineRepository;
    private final LotOlivesRepository lotOlivesRepository;
    private final StockMovementService stockMovementService;
    private final CurrentUserService currentUserService;

    public ExecutionProductionDTO create(ExecutionProductionCreateDTO dto) {
        GuideProduction guideProduction = guideProductionRepository.findById(dto.getGuideProductionId())
                .orElseThrow(() -> new RuntimeException("Guide de production non trouve"));
        Machine machine = machineRepository.findById(dto.getMachineId())
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        LotOlives lot = lotOlivesRepository.findById(dto.getLotId())
                .orElseThrow(() -> new RuntimeException("Lot d'olives non trouve"));

        Long huilerieId = resolveHuilerieId(guideProduction, machine, lot);
        currentUserService.ensureCanAccessHuilerie(huilerieId);

        String referenceUnique = buildUniqueCodeLot(dto.getReference(), lot.getIdLot());

        ExecutionProduction executionProduction = new ExecutionProduction();
        executionProduction.setReference(referenceUnique);
        executionProduction.setDateDebut(dto.getDateDebut());
        executionProduction.setDateFinPrevue(dto.getDateFinPrevue());
        executionProduction.setDateFinReelle(dto.getDateFinReelle());
        executionProduction.setStatut(dto.getStatut());
        executionProduction.setRendement(dto.getRendement());
        executionProduction.setObservations(dto.getObservations());
        executionProduction.setControleTemperature(dto.getControleTemperature());
        executionProduction.setGuideProduction(guideProduction);
        executionProduction.setMachine(machine);
        executionProduction.setLot(lot);

        ExecutionProduction savedExecution = executionProductionRepository.save(executionProduction);

        StockMovementCreateDTO transferDto = new StockMovementCreateDTO();
        transferDto.setLotId(lot.getIdLot());
        transferDto.setHuilerieId(huilerieId);
        transferDto.setTypeMouvement(TypeMouvement.TRANSFERT);
        transferDto.setDateMouvement(savedExecution.getDateDebut());
        transferDto.setCommentaire("Transfert automatique lors de l'execution " + savedExecution.getReference());
        stockMovementService.create(transferDto);

        return toDTO(savedExecution);
    }

    private Long resolveHuilerieId(GuideProduction guideProduction, Machine machine, LotOlives lot) {
        Long guideHuilerieId = resolveHuilerieId(guideProduction != null ? guideProduction.getHuilerie() : null,
                "guide de production");
        Long machineHuilerieId = resolveHuilerieId(machine != null ? machine.getHuilerie() : null, "machine");
        Long lotHuilerieId = resolveHuilerieId(lot != null ? lot.getHuilerie() : null, "lot d'olives");

        if (!guideHuilerieId.equals(machineHuilerieId) || !guideHuilerieId.equals(lotHuilerieId)) {
            throw new RuntimeException("Le guide, la machine et le lot doivent appartenir a la meme huilerie");
        }

        return guideHuilerieId;
    }

    private Long resolveHuilerieId(Models.Huilerie huilerie, String source) {
        if (huilerie == null || huilerie.getIdHuilerie() == null) {
            throw new RuntimeException(source + " sans huilerie associee");
        }
        return huilerie.getIdHuilerie();
    }

    private String buildUniqueCodeLot(String requestedCodeLot, Long lotId) {
        String baseCode = requestedCodeLot != null && !requestedCodeLot.isBlank()
                ? requestedCodeLot.trim()
                : "LOT-" + lotId;

        if (!executionProductionRepository.existsByCodeLot(baseCode)) {
            return baseCode;
        }

        int suffix = 2;
        String candidate = baseCode + "-" + suffix;
        while (executionProductionRepository.existsByCodeLot(candidate)) {
            suffix++;
            candidate = baseCode + "-" + suffix;
        }
        return candidate;
    }

    @Transactional(readOnly = true)
    public String buildCodeLotForLot(Long lotId) {
        LotOlives lot = lotOlivesRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException("Lot d'olives non trouve"));

        String lotReference = lot.getReference();
        return buildUniqueCodeLot(lotReference, lot.getIdLot());
    }

    @Transactional(readOnly = true)
    public ExecutionProductionDTO findById(Long idExecutionProduction) {
        return toDTO(findExecution(idExecutionProduction));
    }

    @Transactional(readOnly = true)
    public List<ExecutionProductionDTO> findAll(String huilerieNom) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        List<ExecutionProduction> executions = currentUserService.isAdmin(utilisateur)
                ? (hasText(huilerieNom)
                ? executionProductionRepository.findAllByHuilerieNom(huilerieNom)
                : executionProductionRepository.findAll())
                : executionProductionRepository.findAllByHuilerieId(currentUserService.getCurrentHuilerieIdOrThrow());

        return executions.stream()
                .map(this::safeToDTO)
                .flatMap(Optional::stream)
                .toList();
    }

    public ExecutionProduction findExecution(Long idExecutionProduction) {
        return executionProductionRepository.findById(idExecutionProduction)
                .orElseThrow(() -> new RuntimeException("Execution de production non trouvee"));
    }

    private ExecutionProductionDTO toDTO(ExecutionProduction executionProduction) {
        ExecutionProductionDTO dto = new ExecutionProductionDTO();
        dto.setIdExecutionProduction(executionProduction.getIdExecutionProduction());
        dto.setReference(executionProduction.getReference());
        dto.setDateDebut(executionProduction.getDateDebut());
        dto.setDateFinPrevue(executionProduction.getDateFinPrevue());
        dto.setDateFinReelle(executionProduction.getDateFinReelle());
        dto.setStatut(executionProduction.getStatut());
        dto.setRendement(executionProduction.getRendement());
        dto.setObservations(executionProduction.getObservations());
        dto.setControleTemperature(executionProduction.getControleTemperature());
        dto.setHuilerieId(resolveHuilerieId(executionProduction));
        dto.setHuilerieNom(resolveHuilerieNom(executionProduction));

        if (executionProduction.getGuideProduction() != null) {
            dto.setGuideProductionId(executionProduction.getGuideProduction().getIdGuideProduction());
            dto.setGuideProductionReference(executionProduction.getGuideProduction().getReference());
        }
        if (executionProduction.getMachine() != null) {
            dto.setMachineId(executionProduction.getMachine().getIdMachine());
            dto.setMachineNom(executionProduction.getMachine().getNomMachine());
        }
        if (executionProduction.getLot() != null) {
            dto.setLotId(executionProduction.getLot().getIdLot());
            dto.setLotVariete(executionProduction.getLot().getVarieteOlive());
        }
        if (executionProduction.getProduitFinal() != null) {
            dto.setProduitFinalId(executionProduction.getProduitFinal().getIdProduit());
            dto.setProduitFinalReference(executionProduction.getProduitFinal().getReference());
            dto.setProduitFinalNomProduit(executionProduction.getProduitFinal().getNomProduit());
        }

        dto.setValeursReelles(loadValeursReelles(executionProduction));

        if (executionProduction.getPredictions() != null) {
            dto.setPredictions(executionProduction.getPredictions().stream()
                    .map(this::toPredictionDTO)
                    .toList());
        }

        return dto;
    }

    private dto.PredictionDTO toPredictionDTO(Prediction prediction) {
        dto.PredictionDTO dto = new dto.PredictionDTO();
        dto.setIdPrediction(prediction.getIdPrediction());
        dto.setModePrediction(prediction.getModePrediction());
        dto.setQualitePredite(prediction.getQualitePredite());
        dto.setProbabiliteQualite(prediction.getProbabiliteQualite());
        dto.setRendementPreditPourcent(prediction.getRendementPreditPourcent());
        dto.setQuantiteHuileRecalculeeLitres(prediction.getQuantiteHuileRecalculeeLitres());
        dto.setExecutionProductionId(prediction.getExecutionProduction() != null
                ? prediction.getExecutionProduction().getIdExecutionProduction()
                : null);
        dto.setDateCreation(prediction.getDateCreation());
        return dto;
    }

    private List<ValeurReelleParametreDTO> loadValeursReelles(ExecutionProduction executionProduction) {
        if (executionProduction == null || executionProduction.getGuideProduction() == null) {
            return List.of();
        }

        if (executionProduction.getParametres() != null && !executionProduction.getParametres().isEmpty()) {
            return executionProduction.getParametres().stream()
                    .sorted(java.util.Comparator
                            .comparing((ParametreEtape p) -> p.getEtapeProduction() != null
                                    && p.getEtapeProduction().getOrdre() != null
                                    ? p.getEtapeProduction().getOrdre()
                                    : Integer.MAX_VALUE)
                            .thenComparing(
                                    p -> p.getIdParametreEtape() == null ? Long.MAX_VALUE : p.getIdParametreEtape()))
                    .map(this::toDTO)
                    .toList();
        }

        if (executionProduction.getGuideProduction().getEtapes() == null) {
            return List.of();
        }

        return executionProduction.getGuideProduction().getEtapes().stream()
                .sorted(java.util.Comparator
                        .comparing(etape -> etape.getOrdre() == null ? Integer.MAX_VALUE : etape.getOrdre()))
                .flatMap(etape -> (etape.getParametres() == null ? List.<ParametreEtape>of() : etape.getParametres())
                        .stream()
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

    private Optional<ExecutionProductionDTO> safeToDTO(ExecutionProduction executionProduction) {
        try {
            return Optional.of(toDTO(executionProduction));
        } catch (Exception ex) {
            Long executionId = executionProduction != null ? executionProduction.getIdExecutionProduction() : null;
            log.warn("Execution ignoree lors du mapping (id={}): {}", executionId, ex.getMessage());
            return Optional.empty();
        }
    }

    private Long resolveHuilerieId(ExecutionProduction executionProduction) {
        Models.Huilerie huilerie = resolveHuilerie(executionProduction);
        return huilerie != null ? huilerie.getIdHuilerie() : null;
    }

    private String resolveHuilerieNom(ExecutionProduction executionProduction) {
        Models.Huilerie huilerie = resolveHuilerie(executionProduction);
        return huilerie != null ? huilerie.getNom() : null;
    }

    private Models.Huilerie resolveHuilerie(ExecutionProduction executionProduction) {
        if (executionProduction == null) {
            return null;
        }

        if (executionProduction.getGuideProduction() != null
                && executionProduction.getGuideProduction().getHuilerie() != null
                && executionProduction.getGuideProduction().getHuilerie().getIdHuilerie() != null) {
            return executionProduction.getGuideProduction().getHuilerie();
        }

        if (executionProduction.getMachine() != null
                && executionProduction.getMachine().getHuilerie() != null
                && executionProduction.getMachine().getHuilerie().getIdHuilerie() != null) {
            return executionProduction.getMachine().getHuilerie();
        }

        if (executionProduction.getLot() == null || executionProduction.getLot().getStocks() == null) {
            return null;
        }

        return executionProduction.getLot().getStocks().stream()
                .map(stock -> stock.getLotOlives() != null ? stock.getLotOlives().getHuilerie() : null)
                .filter(huilerie -> huilerie != null && huilerie.getIdHuilerie() != null)
                .findFirst()
                .orElse(null);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Transactional
    public void saveValeursReelles(Long idExecutionProduction, List<ValeurReelleParametreDTO> valeurs) {
        ExecutionProduction execution = findExecution(idExecutionProduction);

        for (ValeurReelleParametreDTO dto : valeurs) {
            ParametreEtape paramOriginal = parametreEtapeRepository.findById(dto.getParametreEtapeId())
                    .orElseThrow(() -> new RuntimeException("Paramètre non trouvé"));

            Long guideId = execution.getGuideProduction() != null
                    ? execution.getGuideProduction().getIdGuideProduction()
                    : null;
            Long paramGuideId = paramOriginal.getEtapeProduction() != null
                    && paramOriginal.getEtapeProduction().getGuideProduction() != null
                    ? paramOriginal.getEtapeProduction().getGuideProduction().getIdGuideProduction()
                    : null;
            if (guideId == null || !guideId.equals(paramGuideId)) {
                throw new RuntimeException("Le paramètre ne correspond pas au guide de cette exécution");
            }

            ParametreEtape paramNouveau = new ParametreEtape();
            paramNouveau.setNom(paramOriginal.getNom());
            paramNouveau.setUniteMesure(paramOriginal.getUniteMesure());
            paramNouveau.setDescription(paramOriginal.getDescription());
            paramNouveau.setValeurEstime(paramOriginal.getValeurEstime());
            paramNouveau.setValeurReelle(dto.getValeurReelle());
            paramNouveau.setEtapeProduction(paramOriginal.getEtapeProduction());
            paramNouveau.setExecutionProduction(execution);

            parametreEtapeRepository.save(paramNouveau);
        }
    }
}
