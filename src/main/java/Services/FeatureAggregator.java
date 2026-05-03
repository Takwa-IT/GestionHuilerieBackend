package Services;

import Models.EtapeProduction;
import Models.ExecutionProduction;
import Models.GuideProduction;
import Models.LotOlives;
import Models.ParametreEtape;
import Models.ValeurReelleParametre;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FeatureAggregator: Transforme un guide complexe multi-étapes
 * en features simplifiées et reproductibles pour le modèle ML.
 *
 * Stratégie:
 * 1. Renommer chaque paramètre avec son numéro d'étape
 *    Ex: temperature_malaxage_c_etape_1, temperature_malaxage_c_etape_2
 * 2. Agréger les paramètres canoniques par MOYENNE pour éviter
 *    la perte d'information sur les guides multi-étapes.
 * 3. Respecter la priorité: exécution > guide > lot > default
 */
@Component
public class FeatureAggregator {

    // Les 4 paramètres canoniques pour le modèle ML
    private static final String[] CANONICAL_CODES = {
            "temperature_malaxage_c",
            "duree_malaxage_min",
            "vitesse_decanteur_tr_min",
            "pression_extraction_bar"
    };

    /**
     * Extraire tous les paramètres du guide en incluant le numéro d'étape
     *
     * Résultat:
     * {
     *   "temperature_malaxage_c_etape_1" -> "45",
     *   "temperature_malaxage_c_etape_2" -> "42",
     *   "temperature_malaxage_c_etape_3" -> "40",
     *   "duree_malaxage_min_etape_1" -> "30",
     *   ...
     * }
     */
    public Map<String, String> extractAllParametersWithEtapeIndex(GuideProduction guide) {
        Map<String, String> result = new LinkedHashMap<>();

        if (guide == null || guide.getEtapes() == null) {
            return result;
        }

        // Itérer avec index pour trackery le numéro d'étape
        for (int etapeIndex = 0; etapeIndex < guide.getEtapes().size(); etapeIndex++) {
            EtapeProduction etape = guide.getEtapes().get(etapeIndex);
            int etapeNum = etapeIndex + 1; // Numéro 1-based pour l'utilisateur

            if (etape.getParametres() == null) {
                continue;
            }

            for (ParametreEtape param : etape.getParametres()) {
                String code = normalizeKey(param.getCodeParametre());
                if (!code.isBlank() && param.getValeur() != null) {
                    // Renommer avec numéro d'étape: code_etape_N
                    String keyWithEtape = code + "_etape_" + etapeNum;
                    result.put(keyWithEtape, param.getValeur());
                }
            }
        }

        return result;
    }

    /**
     * Grouper les paramètres par code SANS le suffixe étape
     *
     * Résultat:
     * {
     *   "temperature_malaxage_c" -> ["45", "42", "40"],
     *   "duree_malaxage_min" -> ["30"],
     *   ...
     * }
     */
    public Map<String, List<String>> groupParametersByCanonicalCode(GuideProduction guide) {
        Map<String, List<String>> result = new LinkedHashMap<>();

        if (guide == null || guide.getEtapes() == null) {
            return result;
        }

        for (EtapeProduction etape : guide.getEtapes()) {
            if (etape.getParametres() == null) {
                continue;
            }

            for (ParametreEtape param : etape.getParametres()) {
                String code = normalizeKey(param.getCodeParametre());
                if (!code.isBlank() && param.getValeur() != null) {
                    result.computeIfAbsent(code, k -> new ArrayList<>())
                            .add(param.getValeur());
                }
            }
        }

        return result;
    }

    /**
     * Agréger un paramètre canonique par MOYENNE.
     */
    public Double aggregateCanonicalParameter(String code, List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        List<Double> doubleValues = values.stream()
                .map(this::parseDoubleSafe)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (doubleValues.isEmpty()) {
            return null;
        }

        return doubleValues.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
    }

    /**
     * Extraire les valeurs réelles de l'exécution avec renommage
     *
     * Résultat:
     * {
     *   "temperature_malaxage_c_override" -> "48",  // Valeur réelle saisie
     *   ...
     * }
     */
    public Map<String, String> extractExecutionRealValues(ExecutionProduction execution) {
        Map<String, String> result = new LinkedHashMap<>();

        if (execution == null || execution.getValeursReelles() == null) {
            return result;
        }

        for (ValeurReelleParametre valeur : execution.getValeursReelles()) {
            if (valeur.getParametreEtape() == null || valeur.getValeurReelle() == null) {
                continue;
            }

            String code = normalizeKey(valeur.getParametreEtape().getCodeParametre());
            if (!code.isBlank() && valeur.getValeurReelle() != null) {
                // Ajouter le suffixe _override pour indiquer que c'est une valeur réelle saisie
                String keyWithOverride = code + "_override";
                result.put(keyWithOverride, String.valueOf(valeur.getValeurReelle()));
            }
        }

        return result;
    }

    /**
     * Construire un Map avec TOUTES les features renommées et agrégées
     *
     * Résultat:
     * {
     *   // Canonical parameters (agrégés)
     *   "temperature_malaxage_c" -> 42.33,
     *   "duree_malaxage_min" -> 30,
     *
     *   // Avec étape (pas agrégés, pour analyse)
     *   "temperature_malaxage_c_etape_1" -> 45,
     *   "temperature_malaxage_c_etape_2" -> 42,
     *   "temperature_malaxage_c_etape_3" -> 40,
     *
     *   // Override (exécution réelle)
     *   "temperature_malaxage_c_override" -> 48
     * }
     */
    public Map<String, Object> buildAggregatedFeatures(
            GuideProduction guide,
            ExecutionProduction execution,
            LotOlives lot) {

        Map<String, Object> features = new LinkedHashMap<>();

        // 1. Extraire tous les paramètres avec index d'étape
        Map<String, String> paramsWithEtape = extractAllParametersWithEtapeIndex(guide);
        Map<String, List<String>> paramsByCode = groupParametersByCanonicalCode(guide);

        // 2. Ajouter les paramètres avec étape au résultat (pour traçabilité)
        paramsWithEtape.forEach((key, value) -> {
            try {
                features.put(key, parseDoubleSafe(value)); // Convertir en Double
            } catch (Exception e) {
                features.put(key, value); // Garder comme String si échec
            }
        });

        // 3. Agréger les paramètres canoniques
        for (String code : CANONICAL_CODES) {
            Double aggregated = aggregateCanonicalParameter(code, paramsByCode.get(code));
            if (aggregated != null) {
                features.put(code, aggregated);
            }
        }

        // 4. Ajouter les override de l'exécution réelle
        Map<String, String> executionRealValues = extractExecutionRealValues(execution);
        executionRealValues.forEach((key, value) -> {
            try {
                features.put(key, parseDoubleSafe(value));
            } catch (Exception e) {
                features.put(key, value);
            }
        });

        return features;
    }

    /**
     * Obtenir la priorité d'une valeur (pour audit trail)
     * Priority: execution_real > guide > lot > default
     */
    public String getValueSource(
            String code,
            ExecutionProduction execution,
            GuideProduction guide,
            LotOlives lot) {

        // Priority 1: Exécution réelle
        if (execution != null && execution.getValeursReelles() != null) {
            for (ValeurReelleParametre vrp : execution.getValeursReelles()) {
                if (vrp.getParametreEtape() != null
                        && normalizeKey(vrp.getParametreEtape().getCodeParametre()).equals(code)
                        && vrp.getValeurReelle() != null) {
                    return "EXECUTION_REAL_VALUE";
                }
            }
        }

        // Priority 2: Guide
        if (guide != null && guide.getEtapes() != null) {
            for (EtapeProduction etape : guide.getEtapes()) {
                if (etape.getParametres() != null) {
                    for (ParametreEtape param : etape.getParametres()) {
                        if (normalizeKey(param.getCodeParametre()).equals(code)
                                && param.getValeur() != null) {
                            return "GUIDE_PARAMETER";
                        }
                    }
                }
            }
        }

        // Priority 3: Lot
        if (lot != null) {
            return "LOT_VALUE";
        }

        // Priority 4: Default
        return "DEFAULT";
    }

    // ===== Helper Methods =====

    private String normalizeKey(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace(',', '.').trim();
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
