package Services;

import Models.ExecutionProduction;
import Models.GuideProduction;
import Models.LotOlives;
import dto.FeatureTransformationDTO;
import dto.FeatureTransformationDTO.AuditTrailEntry;
import dto.FeatureTransformationDTO.TransformationSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour analyser et générer un rapport de transformation des features
 * du guide multi-étapes vers un format ML simplifié
 */
@Service
public class TransformationAnalysisService {

    @Autowired
    private FeatureAggregator featureAggregator;

    private static final String[] CANONICAL_CODES = {
            "temperature_malaxage_c",
            "duree_malaxage_min",
            "vitesse_decanteur_tr_min",
            "pression_extraction_bar"
    };

    /**
     * Générer un rapport complet de transformation des features
     */
    public FeatureTransformationDTO analyzeTransformation(
            GuideProduction guide,
            ExecutionProduction execution,
            LotOlives lot,
            Map<String, Object> finalAIPayload) {

        FeatureTransformationDTO report = new FeatureTransformationDTO();

        // 1. Extraire les paramètres avec index d'étape
        Map<String, String> paramsWithEtape = featureAggregator.extractAllParametersWithEtapeIndex(guide);
        Map<String, Object> paramsWithEtapeConverted = convertToDoubleMap(paramsWithEtape);
        report.setParametersWithEtapeIndex(paramsWithEtapeConverted);

        // 2. Grouper par code canonique
        Map<String, List<String>> paramsByCode = featureAggregator.groupParametersByCanonicalCode(guide);
        Map<String, List<Object>> paramsByCodeConverted = new LinkedHashMap<>();
        paramsByCode.forEach((code, values) -> {
            List<Object> doubleValues = values.stream()
                    .map(this::parseDoubleSafe)
                    .collect(Collectors.toList());
            paramsByCodeConverted.put(code, doubleValues);
        });
        report.setParametersByCanonicalCode(paramsByCodeConverted);

        // 3. Paramètres canoniques agrégés
        Map<String, Object> canonicalAggregated = new LinkedHashMap<>();
        for (String code : CANONICAL_CODES) {
            Double aggregated = featureAggregator.aggregateCanonicalParameter(code, paramsByCode.get(code));
            if (aggregated != null) {
                canonicalAggregated.put(code, aggregated);
            }
        }
        report.setCanonicalAggregated(canonicalAggregated);

        // 4. Override de l'exécution
        Map<String, String> executionRealValues = featureAggregator.extractExecutionRealValues(execution);
        Map<String, Object> executionOverridesConverted = convertToDoubleMap(executionRealValues);
        report.setExecutionOverrides(executionOverridesConverted);

        // 5. Audit trail
        Map<String, AuditTrailEntry> auditTrail = generateAuditTrail(guide, execution, lot, paramsByCode);
        report.setAuditTrail(auditTrail);

        // 6. AI Payload final
        report.setFinalAIPayload(finalAIPayload);

        // 7. Résumé
        TransformationSummary summary = generateSummary(guide, paramsByCode, paramsWithEtape);
        report.setSummary(summary);

        return report;
    }

    /**
     * Générer l'audit trail: source et valeur pour chaque paramètre
     */
    private Map<String, AuditTrailEntry> generateAuditTrail(
            GuideProduction guide,
            ExecutionProduction execution,
            LotOlives lot,
            Map<String, List<String>> paramsByCode) {

        Map<String, AuditTrailEntry> auditTrail = new LinkedHashMap<>();

        for (String code : CANONICAL_CODES) {
            AuditTrailEntry entry = new AuditTrailEntry();
            entry.setParameterCode(code);

            List<String> rawValues = paramsByCode.get(code);
            List<Object> intermediate = rawValues != null ?
                    rawValues.stream().map(this::parseDoubleSafe).collect(Collectors.toList()) :
                    new ArrayList<>();
            entry.setIntermediateValues(intermediate);

            // Déterminer la source
            String source = featureAggregator.getValueSource(code, execution, guide, lot);
            entry.setSource(source);

            // Déterminer la valeur finale
            Double finalValue = featureAggregator.aggregateCanonicalParameter(code, rawValues);
            entry.setFinalValue(finalValue);

            // Déterminer la stratégie d'agrégation
            String strategy = getAggregationStrategy(code);
            entry.setAggregationStrategy(strategy);

            // Valeur brute (si applicable)
            if (rawValues != null && !rawValues.isEmpty()) {
                entry.setRawValue(rawValues.get(0));
            }

            auditTrail.put(code, entry);
        }

        return auditTrail;
    }

    /**
     * Générer un résumé de la transformation
     */
    private TransformationSummary generateSummary(
            GuideProduction guide,
            Map<String, List<String>> paramsByCode,
            Map<String, String> paramsWithEtape) {

        TransformationSummary summary = new TransformationSummary();

        // Compter les étapes
        int etapeCount = guide.getEtapes() != null ? guide.getEtapes().size() : 0;
        summary.setEtapeCount(etapeCount);

        // Compter les paramètres
        int parameterCount = paramsWithEtape.size();
        summary.setParameterCount(parameterCount);

        // Compter les canoniques
        int canonicalCount = 0;
        for (String code : CANONICAL_CODES) {
            if (paramsByCode.containsKey(code)) {
                canonicalCount++;
            }
        }
        summary.setCanonicalParameterCount(canonicalCount);
        summary.setCustomParameterCount(parameterCount - canonicalCount);

        // Compter ceux avec multiple valeurs
        int multiValueCount = 0;
        for (List<String> values : paramsByCode.values()) {
            if (values.size() > 1) {
                multiValueCount++;
            }
        }
        summary.setParametersWithMultipleValues(multiValueCount);

        // Calculer le taux de préservation d'information
        // Information perdue = paramètres multi-value × (nbValeur - 1) / total
        double infoLost = 0;
        for (List<String> values : paramsByCode.values()) {
            if (values.size() > 1) {
                infoLost += (values.size() - 1);
            }
        }
        double infoPreservation = parameterCount > 0 ?
                ((parameterCount - infoLost) / parameterCount) * 100 :
                100.0;
        summary.setInformationPreservationRate(Math.max(0, Math.min(100, infoPreservation)));

        // Évaluer le niveau de risque
        String riskLevel = evaluateRiskLevel(multiValueCount, etapeCount, parameterCount);
        summary.setRiskLevel(riskLevel);

        // Générer les avertissements
        List<String> warnings = generateWarnings(guide, paramsByCode, multiValueCount);
        summary.setWarnings(warnings);

        return summary;
    }

    /**
     * Évaluer le niveau de risque
     */
    private String evaluateRiskLevel(int multiValueCount, int etapeCount, int parameterCount) {
        if (multiValueCount > 0 && multiValueCount > (parameterCount / 2)) {
            return "HIGH"; // Plus de la moitié des paramètres ont des doublons
        } else if (multiValueCount > 0) {
            return "MEDIUM"; // Quelques doublons
        } else {
            return "LOW"; // Aucun doublon
        }
    }

    /**
     * Générer les avertissements pour l'utilisateur
     */
    private List<String> generateWarnings(
            GuideProduction guide,
            Map<String, List<String>> paramsByCode,
            int multiValueCount) {

        List<String> warnings = new ArrayList<>();

        // Avertissement 1: Doublons détectés
        if (multiValueCount > 0) {
            List<String> duplicatedCodes = new ArrayList<>();
            for (String code : CANONICAL_CODES) {
                List<String> values = paramsByCode.get(code);
                if (values != null && values.size() > 1) {
                    duplicatedCodes.add(code);
                }
            }
            warnings.add("⚠️ " + multiValueCount + " paramètre(s) canonique(s) apparaissent plusieurs fois: " +
                    String.join(", ", duplicatedCodes) + ". Agrégation appliquée.");
        }

        // Avertissement 2: Guide complexe
        int etapeCount = guide.getEtapes() != null ? guide.getEtapes().size() : 0;
        if (etapeCount > 5) {
            warnings.add("ℹ️ Guide complexe avec " + etapeCount + " étapes. Vérifier que toutes les valeurs sont correctes.");
        }

        // Avertissement 3: Paramètres manquants
        int totalCanonical = 0;
        for (String code : CANONICAL_CODES) {
            if (paramsByCode.get(code) != null) {
                totalCanonical++;
            }
        }
        if (totalCanonical < CANONICAL_CODES.length) {
            warnings.add("⚠️ Certains paramètres canoniques sont manquants. Defaults seront utilisés.");
        }

        return warnings;
    }

    /**
     * Obtenir la stratégie d'agrégation pour un code
     */
    private String getAggregationStrategy(String code) {
        return "AVERAGE";
    }

    // ===== Helper Methods =====

    private Map<String, Object> convertToDoubleMap(Map<String, String> input) {
        Map<String, Object> result = new LinkedHashMap<>();
        input.forEach((key, value) -> {
            Double doubleValue = parseDoubleSafe(value);
            result.put(key, doubleValue != null ? doubleValue : value);
        });
        return result;
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String normalized = value.replace(',', '.').trim();
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
