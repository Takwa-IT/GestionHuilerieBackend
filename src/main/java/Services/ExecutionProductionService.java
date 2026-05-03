package Services;

import Models.ExecutionProduction;
import Models.GuideProduction;
import Models.LotOlives;
import Models.ParametreEtape;
import Models.Prediction;
import Models.TypeMouvement;
import Models.ValeurReelleParametre;
import Repositories.ParametreEtapeRepository;
import dto.ValeurReelleParametreDTO;

import Models.Utilisateur;
import Repositories.ExecutionProductionRepository;
import Repositories.GuideProductionRepository;
import Repositories.LotOlivesRepository;
import dto.ExecutionProductionCreateDTO;
import dto.ExecutionProductionDTO;
import dto.StockMovementCreateDTO;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ExecutionProductionService {

    private static final String QUALITY_EXTRA_VIERGE = "Extra Vierge";
    private static final String QUALITY_VIERGE = "Vierge";
    private static final String QUALITY_LAMPANTE = "Lampante";

    private static final Set<String> ALLOWED_PARAM_KEYS = Set.of(
            "temperature_malaxage_c",
            "duree_malaxage_min",
            "vitesse_decanteur_tr_min",
            "vitesse_decanteur",
            "vitesse",
            "pression_extraction_bar",
            "presence_presse",
            "presence_ajout_eau");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ParametreEtapeRepository parametreEtapeRepository;
    private final ExecutionProductionRepository executionProductionRepository;
    private final GuideProductionRepository guideProductionRepository;
    private final LotOlivesRepository lotOlivesRepository;
    private final StockMovementService stockMovementService;
    private final CurrentUserService currentUserService;

    public ExecutionProductionDTO create(ExecutionProductionCreateDTO dto) {
        GuideProduction guideProduction = guideProductionRepository.findById(dto.getGuideProductionId())
                .orElseThrow(() -> new RuntimeException("Guide de production non trouve"));
        LotOlives lot = lotOlivesRepository.findById(dto.getLotId())
                .orElseThrow(() -> new RuntimeException("Lot d'olives non trouve"));

        Long huilerieId = resolveHuilerieId(guideProduction, lot);
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
        executionProduction.setLot(lot);

        // Copier les champs IA depuis le lot pour la prédiction
        executionProduction.setRegion(firstNonBlank(dto.getRegion(), lot.getRegion()));
        executionProduction.setMethodeRecolte(firstNonBlank(dto.getMethodeRecolte(), lot.getMethodeRecolte()));
        executionProduction.setTypeSol(firstNonBlank(dto.getTypeSol(), lot.getTypeSol()));
        executionProduction.setTemperatureMalaxageC(dto.getTemperatureMalaxageC());
        executionProduction.setDureeMalaxageMin(dto.getDureeMalaxageMin());
        executionProduction.setVitesseDecanteurTrMin(dto.getVitesseDecanteurTrMin());
        executionProduction.setHumiditePourcent(firstNonNull(dto.getHumiditePourcent(), lot.getHumiditePourcent()));
        executionProduction.setAciditeOlivesPourcent(firstNonNull(dto.getAciditeOlivesPourcent(),
                lot.getAciditeOlivesPourcent()));
        executionProduction.setTauxFeuillesPourcent(firstNonNull(dto.getTauxFeuillesPourcent(),
                lot.getTauxFeuillesPourcent()));
        executionProduction.setPressionExtractionBar(dto.getPressionExtractionBar());

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

    private Long resolveHuilerieId(GuideProduction guideProduction, LotOlives lot) {
        Long guideHuilerieId = resolveHuilerieId(guideProduction != null ? guideProduction.getHuilerie() : null,
                "guide de production");
        Long lotHuilerieId = resolveHuilerieId(lot != null ? lot.getHuilerie() : null, "lot d'olives");

        if (!guideHuilerieId.equals(lotHuilerieId)) {
            throw new RuntimeException("Le guide et le lot doivent appartenir a la meme huilerie");
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
        dto.setRegion(executionProduction.getRegion());
        dto.setMethodeRecolte(executionProduction.getMethodeRecolte());
        dto.setTypeSol(executionProduction.getTypeSol());
        dto.setTemperatureMalaxageC(executionProduction.getTemperatureMalaxageC());
        dto.setDureeMalaxageMin(executionProduction.getDureeMalaxageMin());
        dto.setVitesseDecanteurTrMin(executionProduction.getVitesseDecanteurTrMin());
        dto.setHumiditePourcent(executionProduction.getHumiditePourcent());
        dto.setAciditeOlivesPourcent(executionProduction.getAciditeOlivesPourcent());
        dto.setTauxFeuillesPourcent(executionProduction.getTauxFeuillesPourcent());
        dto.setPressionExtractionBar(executionProduction.getPressionExtractionBar());
        dto.setHuilerieId(resolveHuilerieId(executionProduction));
        dto.setHuilerieNom(resolveHuilerieNom(executionProduction));

        if (executionProduction.getGuideProduction() != null) {
            dto.setGuideProductionId(executionProduction.getGuideProduction().getIdGuideProduction());
            dto.setGuideProductionReference(executionProduction.getGuideProduction().getReference());
        }
        if (executionProduction.getLot() != null) {
            dto.setLotId(executionProduction.getLot().getIdLot());
            dto.setLotVariete(executionProduction.getLot().getVarieteOlive());
        }
        if (executionProduction.getProduitFinal() != null) {
            dto.setProduitFinalId(executionProduction.getProduitFinal().getIdProduit());
            dto.setProduitFinalReference(executionProduction.getProduitFinal().getReference());
            dto.setProduitFinalNomProduit(executionProduction.getProduitFinal().getNomProduit());
            dto.setProduitFinalQualite(normalizeQualityLabel(executionProduction.getProduitFinal().getQualite()));
            dto.setProduitFinalQuantiteProduite(executionProduction.getProduitFinal().getQuantiteProduite());
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
        dto.setQualitePredite(normalizeQualityLabel(prediction.getQualitePredite()));
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

        Map<Long, ValeurReelleParametre> valeursByParametreId = executionProduction.getValeursReelles() == null
                ? Map.of()
                : executionProduction.getValeursReelles().stream()
                .filter(v -> v.getParametreEtape() != null
                        && v.getParametreEtape().getIdParametreEtape() != null)
                .collect(Collectors.toMap(v -> v.getParametreEtape().getIdParametreEtape(), Function.identity(),
                        (first, second) -> second));

        if (executionProduction.getGuideProduction().getEtapes() == null) {
            return List.of();
        }

        return executionProduction.getGuideProduction().getEtapes().stream()
                .sorted(java.util.Comparator
                        .comparing(etape -> etape.getOrdre() == null ? Integer.MAX_VALUE : etape.getOrdre()))
                .flatMap(etape -> (etape.getParametres() == null ? List.<ParametreEtape>of() : etape.getParametres())
                        .stream()
                        .filter(this::isParametreAutorise))
                .map(parametre -> toDTO(parametre, valeursByParametreId.get(parametre.getIdParametreEtape())))
                .toList();
    }

    private ValeurReelleParametreDTO toDTO(ParametreEtape parametre, ValeurReelleParametre valeurReelle) {
        ValeurReelleParametreDTO dto = new ValeurReelleParametreDTO();
        dto.setParametreEtapeId(parametre.getIdParametreEtape());
        dto.setNomParametre(parametre.getNomParametre());
        dto.setUniteMesure(parametre.getUniteMesure());
        dto.setValeurEstimee(parseDoubleSafely(parametre.getValeur()));

        if (valeurReelle != null) {
            dto.setIdValeurReelleParametre(valeurReelle.getIdValeurReelleParametre());
            dto.setExecutionProductionId(valeurReelle.getExecutionProduction() != null
                    ? valeurReelle.getExecutionProduction().getIdExecutionProduction()
                    : null);
            dto.setValeurReelle(valeurReelle.getValeurReelle());
            dto.setDeviation(valeurReelle.getDeviation());
            dto.setQualiteDeviation(valeurReelle.getQualiteDeviation());
            dto.setDateCreation(formatDateTime(valeurReelle.getDateCreation()));
            dto.setDateModification(formatDateTime(valeurReelle.getDateModification()));
        }

        return dto;
    }

    private Double parseDoubleSafely(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
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

    private String normalizeQualityLabel(String value) {
        if (value == null) {
            return null;
        }

        return switch (value.trim()) {
            case "Excellente", "Extra Vierge" -> QUALITY_EXTRA_VIERGE;
            case "Bonne", "Vierge" -> QUALITY_VIERGE;
            case "Moyenne", "Lampante" -> QUALITY_LAMPANTE;
            default -> value.trim();
        };
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

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first.trim() : second;
    }

    private Double firstNonNull(Double first, Double second) {
        return first != null ? first : second;
    }

    private boolean isParametreAutorise(ParametreEtape parametre) {
        String codeParam = parametre.getCodeParametre() != null && !parametre.getCodeParametre().isBlank()
                ? parametre.getCodeParametre()
                : parametre.getNomParametre();
        String normalizedKey = normalizeParamKey(codeParam);
        return ALLOWED_PARAM_KEYS.contains(normalizedKey);
    }

    private String normalizeParamKey(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        return key.trim().toLowerCase().replaceAll("[\\s_]+", "_");
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
    }

    @Transactional
    public void saveValeursReelles(Long idExecutionProduction, List<ValeurReelleParametreDTO> valeurs) {
        ExecutionProduction execution = findExecution(idExecutionProduction);

        if (execution.getValeursReelles() == null) {
            execution.setValeursReelles(new java.util.ArrayList<>());
        }

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

            execution.getValeursReelles().removeIf(v -> v.getParametreEtape() != null
                    && dto.getParametreEtapeId().equals(v.getParametreEtape().getIdParametreEtape()));

            if (dto.getValeurReelle() != null) {
                ValeurReelleParametre valeurReelle = new ValeurReelleParametre();
                valeurReelle.setExecutionProduction(execution);
                valeurReelle.setParametreEtape(paramOriginal);
                valeurReelle.setValeurReelle(dto.getValeurReelle());
                execution.getValeursReelles().add(valeurReelle);
            }
        }

        executionProductionRepository.save(execution);
    }
}
