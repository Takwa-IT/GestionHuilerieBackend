package Services;

import Models.AnalyseLaboratoire;
import Models.EtapeProduction;
import Models.ExecutionProduction;
import Models.GuideProduction;
import Models.LotOlives;
import Models.Machine;
import Models.ParametreEtape;
import Models.ValeurReelleParametre;
import Repositories.ExecutionProductionRepository;
import dto.ExecutionPredictionStartDTO;
import dto.PredictionCreateDTO;
import dto.PredictionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ExecutionPredictionService {

    private final ExecutionProductionRepository executionProductionRepository;
    private final PredictionService predictionService;

    @Value("${ai.prediction.base-url:http://localhost:7500}")
    private String aiPredictionBaseUrl;

    public PredictionDTO predictOnStart(Long executionId, ExecutionPredictionStartDTO overrides) {
        ExecutionProduction execution = executionProductionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution de production non trouvee"));

        applyOverrides(execution, overrides);

        Map<String, Object> payload = buildAiPayload(execution);
        Map<String, Object> aiResponse = callAiPredict(payload);

        PredictionCreateDTO dto = new PredictionCreateDTO();
        dto.setExecutionProductionId(executionId);
        dto.setModePrediction(asString(aiResponse.get("mode_prediction")));
        dto.setQualitePredite(asString(aiResponse.get("qualite_predite")));
        dto.setProbabiliteQualite(asDouble(aiResponse.get("probabilite_qualite")));
        dto.setRendementPreditPourcent(asDouble(aiResponse.get("rendement_predit_pourcent")));
        dto.setQuantiteHuileRecalculeeLitres(asDouble(aiResponse.get("quantite_huile_recalculee_litres")));

        return predictionService.create(dto);
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

        if (changed) {
            executionProductionRepository.save(execution);
        }
    }

    private Map<String, Object> callAiPredict(Map<String, Object> payload) {
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

    private Map<String, Object> buildAiPayload(ExecutionProduction execution) {
        LotOlives lot = execution.getLotOlives();
        Machine machine = execution.getMachine();
        GuideProduction guide = execution.getGuideProduction();

        if (lot == null) {
            throw new RuntimeException("Lot absent sur l'execution");
        }
        if (machine == null) {
            throw new RuntimeException("Machine absente sur l'execution");
        }
        if (guide == null) {
            throw new RuntimeException("Guide absent sur l'execution");
        }

        Map<String, String> valeursParCode = extractProcessValues(guide, execution);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("variete", normalizeText(lot.getVariete()));
        // Utiliser les valeurs de l'execution si disponibles, sinon fallback au lot
        payload.put("region", normalizeText(execution.getRegion() != null ? execution.getRegion() : lot.getRegion()));
        payload.put("methode_recolte", normalizeText(execution.getMethodeRecolte() != null ? execution.getMethodeRecolte() : lot.getMethodeRecolte()));
        payload.put("type_sol", normalizeText(execution.getTypeSol() != null ? execution.getTypeSol() : lot.getTypeSol()));
        payload.put("poids_olives_kg", firstNonNull(lot.getPesee(), lot.getQuantiteInitiale()));
        payload.put("maturite_niveau_1_5", parseMaturite(lot.getMaturite()));
        payload.put("duree_stockage_jours", defaultInt(lot.getDureeStockageAvantBroyage(), 0));
        payload.put("temps_depuis_recolte_heures", defaultInt(lot.getTempsDepuisRecolteHeures(), 0));
        // Utiliser la valeur de l'execution en priorité, puis valeursParCode, puis valeur par défaut
        payload.put("temperature_malaxage_c",
                execution.getTemperatureMalaxageC() != null ? execution.getTemperatureMalaxageC() :
                        parseDoubleSafe(valeursParCode.get("temperature_malaxage_c")));
        payload.put("duree_malaxage_min",
                execution.getDureeMalaxageMin() != null ? execution.getDureeMalaxageMin() :
                        parseDoubleSafe(valeursParCode.get("duree_malaxage_min")));
        payload.put("vitesse_decanteur_tr_min",
                execution.getVitesseDecanteurTrMin() != null ? execution.getVitesseDecanteurTrMin() :
                        parseDoubleSafe(valeursParCode.get("vitesse_decanteur_tr_min")));
        payload.put("humidite_pourcent",
                firstNonNull(execution.getHumiditePourcent(), lot.getHumiditePourcent()));
        payload.put("acidite_olives_pourcent",
                firstNonNull(execution.getAciditeOlivesPourcent(), lot.getAciditeOlivesPourcent()));
        payload.put("taux_feuilles_pourcent",
                firstNonNull(execution.getTauxFeuillesPourcent(), lot.getTauxFeuillesPourcent()));
        payload.put("lavage_effectue", normalizeOuiNon(lot.getLavageEffectue()));
        payload.put("type_machine", normalizeText(machine.getTypeMachine()));
        payload.put("pression_extraction_bar",
                execution.getPressionExtractionBar() != null ? execution.getPressionExtractionBar() :
                        parseDoubleSafe(valeursParCode.get("pression_extraction_bar")));
        payload.put("controle_temperature",
                execution.getControleTemperature() != null && execution.getControleTemperature() ? "Oui" : "Non");

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

    private Map<String, String> extractProcessValues(GuideProduction guide, ExecutionProduction execution) {
        Map<String, String> values = new HashMap<>();

        if (guide.getEtapes() != null) {
            for (EtapeProduction etape : guide.getEtapes()) {
                if (etape.getParametres() == null) {
                    continue;
                }
                for (ParametreEtape parametre : etape.getParametres()) {
                    String code = normalizeKey(parametre.getCodeParametre());
                    if (!code.isBlank() && parametre.getValeur() != null) {
                        values.put(code, parametre.getValeur());
                    }
                }
            }
        }

        if (execution.getValeursReelles() != null) {
            for (ValeurReelleParametre valeurReelle : execution.getValeursReelles()) {
                if (valeurReelle.getParametreEtape() == null) {
                    continue;
                }
                String code = normalizeKey(valeurReelle.getParametreEtape().getCodeParametre());
                if (!code.isBlank() && valeurReelle.getValeurReelle() != null
                        && !valeurReelle.getValeurReelle().isBlank()) {
                    values.put(code, valeurReelle.getValeurReelle());
                }
            }
        }

        return values;
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
                "pression_extraction_bar",
                "controle_temperature"
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

    private String normalizeKey(String value) {
        return value == null ? "" : value.trim().toLowerCase();
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
}
