package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO pour visualiser la transformation des paramètres du guide
 * en features simplifiées pour le modèle ML
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureTransformationDTO {

    /**
     * Tous les paramètres du guide, renommés avec index d'étape
     * Ex: {"temperature_malaxage_c_etape_1": 45, "temperature_malaxage_c_etape_2": 42}
     */
    private Map<String, Object> parametersWithEtapeIndex;

    /**
     * Paramètres groupés par code canonique
     * Ex: {"temperature_malaxage_c": [45, 42, 40]}
     */
    private Map<String, List<Object>> parametersByCanonicalCode;

    /**
     * Paramètres canoniques agrégés (utilisés pour la prédiction)
     * Ex: {"temperature_malaxage_c": 42.33, "duree_malaxage_min": 30}
     */
    private Map<String, Object> canonicalAggregated;

    /**
     * Override de l'exécution réelle (priority #1)
     * Ex: {"temperature_malaxage_c_override": 48}
     */
    private Map<String, Object> executionOverrides;

    /**
     * Audit trail pour chaque paramètre: quelle est sa source?
     * "execution_real" > "guide" > "lot" > "default"
     */
    private Map<String, AuditTrailEntry> auditTrail;

    /**
     * Le payload final qui sera envoyé au modèle ML
     */
    private Map<String, Object> finalAIPayload;

    /**
     * Rapport de synthèse
     */
    private TransformationSummary summary;

    /**
     * Une entrée audit trail pour un paramètre
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditTrailEntry {
        private String parameterCode;
        private String source; // "EXECUTION_REAL_VALUE", "GUIDE_PARAMETER", "LOT_VALUE", "DEFAULT"
        private Object rawValue;
        private Object finalValue;
        private String aggregationStrategy; // "AVERAGE", "MAX", "MIN", "LAST", "N/A"
        private List<Object> intermediateValues;
    }

    /**
     * Résumé de la transformation
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransformationSummary {
        private int etapeCount; // Nombre d'étapes dans le guide
        private int parameterCount; // Total de paramètres
        private int canonicalParameterCount; // Nombre de paramètres canoniques
        private int customParameterCount; // Nombre de paramètres custom
        private int parametersWithMultipleValues; // Params qui apparaissent plusieurs fois
        private double informationPreservationRate; // % d'information préservée (0-100)
        private String riskLevel; // "LOW", "MEDIUM", "HIGH"
        private List<String> warnings; // Avertissements pour l'utilisateur
    }
}
