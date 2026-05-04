package Services;

import Models.AnalyseLaboratoire;
import Models.EtapeProduction;
import Models.ExecutionProduction;
import Models.GuideProduction;
import Models.LotOlives;
import Models.ParametreEtape;
import Models.Prediction;
import Repositories.ExecutionProductionRepository;
import Repositories.PredictionRepository;
import dto.ExecutionPredictionStartDTO;
import dto.PredictionCreateDTO;
import dto.PredictionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PredictionService {

    private static final String QUALITY_EXTRA_VIERGE = "Extra Vierge";
    private static final String QUALITY_VIERGE = "Vierge";
    private static final String QUALITY_LAMPANTE = "Lampante";

    private final PredictionRepository predictionRepository;
    private final ExecutionProductionRepository executionProductionRepository;

    @Value("${ai.prediction.base-url:http://localhost:7500}")
    private String aiPredictionBaseUrl;

    public PredictionDTO predictOnStart(Long executionId, ExecutionPredictionStartDTO overrides) {
        ExecutionProduction execution = executionProductionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution de production non trouvee"));

        applyOverrides(execution, overrides);

        Map<String, Object> payload = buildAiPayload(execution);
        Map<String, Object> aiResponse = callAiPredictGuide(payload);
        Map<String, Object> predictionBlock = extractPredictionBlock(aiResponse);

        PredictionCreateDTO dto = new PredictionCreateDTO();
        dto.setExecutionProductionId(executionId);
        dto.setModePrediction(asString(predictionBlock.get("mode_prediction")));
        dto.setQualitePredite(normalizeQualityLabel(asString(predictionBlock.get("qualite_predite"))));
        dto.setProbabiliteQualite(asDouble(predictionBlock.get("probabilite_qualite")));
        dto.setRendementPreditPourcent(asDouble(predictionBlock.get("rendement_predit_pourcent")));
        dto.setQuantiteHuileRecalculeeLitres(asDouble(predictionBlock.get("quantite_huile_recalculee_litres")));

        return create(dto);
    }

    public PredictionDTO create(PredictionCreateDTO dto) {
        ExecutionProduction execution = executionProductionRepository.findById(dto.getExecutionProductionId())
                .orElseThrow(() -> new RuntimeException("Execution de production non trouvée"));

        Prediction prediction = new Prediction();
        prediction.setModePrediction(dto.getModePrediction());
        prediction.setQualitePredite(dto.getQualitePredite());
        prediction.setProbabiliteQualite(dto.getProbabiliteQualite());
        prediction.setRendementPreditPourcent(dto.getRendementPreditPourcent());
        prediction.setQuantiteHuileRecalculeeLitres(dto.getQuantiteHuileRecalculeeLitres());
        prediction.setExecutionProduction(execution);
        prediction.setDateCreation(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Prediction saved = predictionRepository.save(prediction);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public PredictionDTO findById(Long id) {
        return predictionRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Prédiction non trouvée"));
    }

    @Transactional(readOnly = true)
    public List<PredictionDTO> findAll() {
        return predictionRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PredictionDTO> findByExecutionProductionId(Long executionId) {
        return predictionRepository.findByExecutionProductionId(executionId).stream()
                .map(this::toDTO)
                .toList();
    }

    public PredictionDTO update(Long id, PredictionCreateDTO dto) {
        Prediction prediction = predictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prédiction non trouvée"));

        prediction.setModePrediction(dto.getModePrediction());
        prediction.setQualitePredite(dto.getQualitePredite());
        prediction.setProbabiliteQualite(dto.getProbabiliteQualite());
        prediction.setRendementPreditPourcent(dto.getRendementPreditPourcent());
        prediction.setQuantiteHuileRecalculeeLitres(dto.getQuantiteHuileRecalculeeLitres());

        if (dto.getExecutionProductionId() != null
                && !dto.getExecutionProductionId()
                        .equals(prediction.getExecutionProduction().getIdExecutionProduction())) {
            ExecutionProduction execution = executionProductionRepository.findById(dto.getExecutionProductionId())
                    .orElseThrow(() -> new RuntimeException("Execution de production non trouvée"));
            prediction.setExecutionProduction(execution);
        }

        return toDTO(predictionRepository.save(prediction));
    }

    public void delete(Long id) {
        predictionRepository.deleteById(id);
    }

    private void applyOverrides(ExecutionProduction execution, ExecutionPredictionStartDTO overrides) {
        if (overrides == null) {
            return;
        }

        boolean changed = false;

        if (hasText(overrides.getRegion())) {
            execution.setRegion(overrides.getRegion().trim());
            changed = true;
        }
        if (hasText(overrides.getMethodeRecolte())) {
            execution.setMethodeRecolte(overrides.getMethodeRecolte().trim());
            changed = true;
        }
        if (hasText(overrides.getTypeSol())) {
            execution.setTypeSol(overrides.getTypeSol().trim());
            changed = true;
        }
        if (overrides.getControleTemperature() != null) {
            execution.setControleTemperature(overrides.getControleTemperature());
            changed = true;
        }
        if (overrides.getTemperatureMalaxageC() != null) {
            execution.setTemperatureMalaxageC(overrides.getTemperatureMalaxageC());
            changed = true;
        }
        if (overrides.getDureeMalaxageMin() != null) {
            execution.setDureeMalaxageMin(overrides.getDureeMalaxageMin());
            changed = true;
        }
        if (overrides.getVitesseDecanteurTrMin() != null) {
            execution.setVitesseDecanteurTrMin(overrides.getVitesseDecanteurTrMin());
            changed = true;
        }
        if (overrides.getHumiditePourcent() != null) {
            execution.setHumiditePourcent(overrides.getHumiditePourcent());
            changed = true;
        }
        if (overrides.getAciditeOlivesPourcent() != null) {
            execution.setAciditeOlivesPourcent(overrides.getAciditeOlivesPourcent());
            changed = true;
        }
        if (overrides.getTauxFeuillesPourcent() != null) {
            execution.setTauxFeuillesPourcent(overrides.getTauxFeuillesPourcent());
            changed = true;
        }
        if (overrides.getPressionExtractionBar() != null) {
            execution.setPressionExtractionBar(overrides.getPressionExtractionBar());
            changed = true;
        }
        if (overrides.getPresenceSeparateur() != null) {
            execution.setPresenceSeparateur(overrides.getPresenceSeparateur());
            changed = true;
        }
        if (overrides.getPresenceAjoutEau() != null) {
            execution.setPresenceAjoutEau(overrides.getPresenceAjoutEau());
            changed = true;
        }

        if (changed) {
            executionProductionRepository.save(execution);
        }
    }

    private Map<String, Object> callAiPredictGuide(Map<String, Object> payload) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    normalizeBaseUrl(aiPredictionBaseUrl) + "/predict",
                    payload,
                    Map.class);

            if (response == null || response.isEmpty()) {
                throw new RuntimeException("Reponse vide du microservice IA");
            }
            return response;
        } catch (Exception ex) {
            throw new RuntimeException("Echec appel microservice IA: " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPredictionBlock(Map<String, Object> response) {
        Object nested = response.get("prediction");
        if (nested instanceof Map<?, ?> nestedMap) {
            return (Map<String, Object>) nestedMap;
        }
        return response;
    }

    private Map<String, Object> buildAiPayload(ExecutionProduction execution) {
        LotOlives lot = execution.getLotOlives();
        GuideProduction guide = execution.getGuideProduction();

        if (lot == null) {
            throw new RuntimeException("Lot absent sur l'execution");
        }
        if (guide == null) {
            throw new RuntimeException("Guide absent sur l'execution");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("variete", normalizeText(lot.getVariete()));
        payload.put("region", normalizeText(execution.getRegion() != null ? execution.getRegion() : lot.getRegion()));
        payload.put("methode_recolte", normalizeText(
                execution.getMethodeRecolte() != null ? execution.getMethodeRecolte() : lot.getMethodeRecolte()));
        payload.put("type_sol",
                normalizeText(execution.getTypeSol() != null ? execution.getTypeSol() : lot.getTypeSol()));
        payload.put("poids_olives_kg", firstNonNull(lot.getPesee(), lot.getQuantiteInitiale()));
        payload.put("maturite_niveau_1_5", parseMaturite(lot.getMaturite()));
        payload.put("duree_stockage_jours", defaultInt(lot.getDureeStockageAvantBroyage(), 0));
        payload.put("temps_depuis_recolte_heures", defaultInt(lot.getTempsDepuisRecolteHeures(), 0));
        payload.put("temperature_malaxage_c",
                firstNonNull(execution.getTemperatureMalaxageC(),
                        extractGuideValue(guide, "temperature_malaxage_c", 26.0)));
        payload.put("duree_malaxage_min",
                firstNonNull(execution.getDureeMalaxageMin(), extractGuideValue(guide, "duree_malaxage_min", 30.0)));
        payload.put("vitesse_decanteur_tr_min",
                firstNonNull(execution.getVitesseDecanteurTrMin(),
                        extractGuideValue(guide, "vitesse_decanteur_tr_min", 3200.0)));
        payload.put("humidite_pourcent",
                firstNonNull(execution.getHumiditePourcent(), lot.getHumiditePourcent()));
        payload.put("acidite_olives_pourcent",
                firstNonNull(execution.getAciditeOlivesPourcent(), lot.getAciditeOlivesPourcent()));
        payload.put("taux_feuilles_pourcent",
                firstNonNull(execution.getTauxFeuillesPourcent(), lot.getTauxFeuillesPourcent()));
        payload.put("lavage_effectue", normalizeOuiNon(lot.getLavageEffectue()));
        payload.put("type_machine", extractGuideTypeMachine(guide));
        payload.put("controle_temperature",
                execution.getControleTemperature() != null && execution.getControleTemperature() ? "Oui" : "Non");
        payload.put("presence_separateur",
                execution.getPresenceSeparateur() != null && execution.getPresenceSeparateur() ? 1 : 0);
        payload.put("presence_ajout_eau",
                execution.getPresenceAjoutEau() != null && execution.getPresenceAjoutEau() ? 1 : 0);
        payload.put("etapes", buildGuideSteps(guide, execution));

        AnalyseLaboratoire analyseLaboratoire = lot.getAnalyseLaboratoire();
        if (analyseLaboratoire != null) {
            payload.put("acidite_huile_pourcent", analyseLaboratoire.getAcidite_huile_pourcent());
            payload.put("indice_peroxyde_meq_o2_kg", analyseLaboratoire.getIndice_peroxyde_meq_o2_kg());
            payload.put("polyphenols_mg_kg", analyseLaboratoire.getPolyphenols_mg_kg());
            payload.put("k232", analyseLaboratoire.getK232());
            payload.put("k270", analyseLaboratoire.getK270());
        }

        validateRequiredPayload(payload);
        return payload;
    }

    private List<Map<String, Object>> buildGuideSteps(GuideProduction guide, ExecutionProduction execution) {
        List<Map<String, Object>> stepsPayload = new ArrayList<>();

        List<EtapeProduction> steps = new ArrayList<>(guide.getEtapes() == null ? List.of() : guide.getEtapes());
        steps.sort(Comparator.comparing(EtapeProduction::getOrdre, Comparator.nullsLast(Integer::compareTo)));

        for (EtapeProduction step : steps) {
            Double stepDuration = extractStepValue(step, "duree_malaxage_min");
            if (stepDuration == null || stepDuration <= 0) {
                stepDuration = 1.0;
            }

            Map<String, Object> aiStep = new LinkedHashMap<>();
            aiStep.put("duree_etape_min", stepDuration);

            Double temperature = extractStepValue(step, "temperature_malaxage_c");
            if (temperature != null) {
                aiStep.put("temperature_malaxage_c", temperature);
            }

            Double malaxage = extractStepValue(step, "duree_malaxage_min");
            if (malaxage != null) {
                aiStep.put("duree_malaxage_min", malaxage);
            }

            Double vitesse = extractStepValue(step, "vitesse_decanteur_tr_min");
            if (vitesse != null) {
                aiStep.put("vitesse_decanteur_tr_min", vitesse);
            }

            Double pression = extractStepValue(step, "pression_extraction_bar");
            if (pression != null) {
                aiStep.put("pression_extraction_bar", pression);
            }

            stepsPayload.add(aiStep);
        }

        boolean hasOverride = execution.getTemperatureMalaxageC() != null
                || execution.getDureeMalaxageMin() != null
                || execution.getVitesseDecanteurTrMin() != null
                || execution.getPressionExtractionBar() != null;

        if (hasOverride) {
            Map<String, Object> overrideStep = new LinkedHashMap<>();
            double overrideDuration = execution.getDureeMalaxageMin() != null && execution.getDureeMalaxageMin() > 0
                    ? execution.getDureeMalaxageMin()
                    : 1.0;
            overrideStep.put("duree_etape_min", overrideDuration);

            if (execution.getTemperatureMalaxageC() != null) {
                overrideStep.put("temperature_malaxage_c", execution.getTemperatureMalaxageC());
            }
            if (execution.getDureeMalaxageMin() != null) {
                overrideStep.put("duree_malaxage_min", execution.getDureeMalaxageMin());
            }
            if (execution.getVitesseDecanteurTrMin() != null) {
                overrideStep.put("vitesse_decanteur_tr_min", execution.getVitesseDecanteurTrMin());
            }
            if (execution.getPressionExtractionBar() != null) {
                overrideStep.put("pression_extraction_bar", execution.getPressionExtractionBar());
            }

            stepsPayload.add(overrideStep);
        }

        return stepsPayload;
    }

    private Double extractStepValue(EtapeProduction step, String code) {
        if (step.getParametres() == null) {
            return null;
        }

        for (ParametreEtape param : step.getParametres()) {
            if (param == null || param.getCodeParametre() == null) {
                continue;
            }
            if (!param.getCodeParametre().trim().equalsIgnoreCase(code)) {
                continue;
            }
            return parseDoubleSafe(param.getValeur());
        }
        return null;
    }

    private Double extractGuideValue(GuideProduction guide, String code, Double fallback) {
        if (guide == null || guide.getEtapes() == null) {
            return fallback;
        }

        List<EtapeProduction> steps = new ArrayList<>(guide.getEtapes());
        steps.sort(Comparator.comparing(EtapeProduction::getOrdre, Comparator.nullsLast(Integer::compareTo)));

        for (EtapeProduction step : steps) {
            Double value = extractStepValue(step, code);
            if (value != null) {
                return value;
            }
        }

        return fallback;
    }

    private void validateRequiredPayload(Map<String, Object> payload) {
        String[] required = new String[] {
                "variete",
                "region",
                "methode_recolte",
                "type_sol",
                "poids_olives_kg",
                "maturite_niveau_1_5",
                "duree_stockage_jours",
                "temps_depuis_recolte_heures",
                "temperature_malaxage_c",
                "duree_malaxage_min",
                "vitesse_decanteur_tr_min",
                "humidite_pourcent",
                "acidite_olives_pourcent",
                "taux_feuilles_pourcent",
                "lavage_effectue",
                "type_machine",
                "controle_temperature",
                "etapes"
        };

        List<String> missing = new ArrayList<>();
        for (String key : required) {
            Object value = payload.get(key);
            if (value == null) {
                missing.add(key);
                continue;
            }
            if (value instanceof String s && s.trim().isEmpty()) {
                missing.add(key);
            }
        }

        if (!missing.isEmpty()) {
            throw new RuntimeException("Champs IA manquants pour la prediction: " + String.join(", ", missing));
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        String trimmed = baseUrl == null ? "" : baseUrl.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeOuiNon(String value) {
        String normalized = normalizeText(value);
        if (normalized == null || normalized.isBlank()) {
            return "Non";
        }
        String lower = normalized.toLowerCase();
        if (lower.equals("oui") || lower.equals("yes") || lower.equals("true") || lower.equals("1")) {
            return "Oui";
        }
        if (lower.equals("non") || lower.equals("no") || lower.equals("false") || lower.equals("0")) {
            return "Non";
        }
        return normalized;
    }

    private Integer parseMaturite(String value) {
        if (value == null || value.isBlank()) {
            return 3;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return 3;
        }
        int parsed = Integer.parseInt(digits);
        if (parsed < 1) {
            return 1;
        }
        if (parsed > 5) {
            return 5;
        }
        return parsed;
    }

    private Integer defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace(',', '.').trim();
        return Double.parseDouble(normalized);
    }

    private Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isBlank()) {
            return null;
        }
        return Double.parseDouble(text);
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isBlank() ? null : text;
    }

    private Double firstNonNull(Double first, Double second) {
        return first != null ? first : second;
    }

    private String extractGuideTypeMachine(GuideProduction guide) {
        if (guide == null || guide.getEtapes() == null || guide.getEtapes().isEmpty()) {
            return "Inconnue";
        }
        return guide.getEtapes().stream()
                .map(EtapeProduction::getMachine)
                .filter(machine -> machine != null && machine.getTypeMachine() != null)
                .map(machine -> machine.getTypeMachine().trim())
                .filter(type -> !type.isEmpty())
                .findFirst()
                .orElse("Inconnue");
    }

    private PredictionDTO toDTO(Prediction prediction) {
        PredictionDTO dto = new PredictionDTO();
        dto.setIdPrediction(prediction.getIdPrediction());
        dto.setModePrediction(prediction.getModePrediction());
        dto.setQualitePredite(normalizeQualityLabel(prediction.getQualitePredite()));
        dto.setProbabiliteQualite(prediction.getProbabiliteQualite());
        dto.setRendementPreditPourcent(prediction.getRendementPreditPourcent());
        dto.setQuantiteHuileRecalculeeLitres(prediction.getQuantiteHuileRecalculeeLitres());
        if (prediction.getExecutionProduction() != null) {
            dto.setExecutionProductionId(prediction.getExecutionProduction().getIdExecutionProduction());
        }
        dto.setDateCreation(prediction.getDateCreation());
        return dto;
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
}
