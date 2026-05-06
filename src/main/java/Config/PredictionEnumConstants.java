package Config;

import java.util.*;

/**
 * Constantes d'énumération pour la prédiction.
 *
 * Alignement entre:
 * - Backend (Java)
 * - Frontend (TypeScript/Angular - chatbot-widget.component.ts)
 * - Python (systeme prediction/validation_config.json)
 *
 * ⚠️ CES VALEURS DOIVENT RESTER SYNCHRONISÉES AVEC PYTHON
 */
public class PredictionEnumConstants {

    // ═══════════════════════════════════════════════════════════════════
    // VARIÉTÉS D'OLIVE
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> VARIETE_VALUES = Set.of(
            "Arbequina",
            "Chemlali",
            "Chetoui"
    );

    // ═══════════════════════════════════════════════════════════════════
    // RÉGIONS
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> REGION_VALUES = Set.of(
            "Centre",
            "Nord",
            "Sud"
    );

    // ═══════════════════════════════════════════════════════════════════
    // MÉTHODES DE RÉCOLTE
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> METHODE_RECOLTE_VALUES = Set.of(
            "manuelle",
            "mecanique",
            "semi-mecanique"
    );

    // ═══════════════════════════════════════════════════════════════════
    // TYPES DE SOL
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> TYPE_SOL_VALUES = Set.of(
            "argileux",
            "calcaire",
            "sableux"
    );

    // ═══════════════════════════════════════════════════════════════════
    // LAVAGE EFFECTUÉ (OUI/NON)
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> LAVAGE_EFFECTUE_VALUES = Set.of(
            "oui",
            "non"
    );

    // ═══════════════════════════════════════════════════════════════════
    // TYPES DE MACHINE
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> TYPE_MACHINE_VALUES = Set.of(
            "2_phase",
            "3_phase",
            "presse"
    );

    // ═══════════════════════════════════════════════════════════════════
    // TYPES DE BROYEUR
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> TYPE_BROYEUR_VALUES = Set.of(
            "marteaux",
            "meule",
            "disque"
    );

    // ═══════════════════════════════════════════════════════════════════
    // TYPES DE MALAXEUR
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> TYPE_MALAXEUR_VALUES = Set.of(
            "horizontal",
            "vertical",
            "malaxeur double cuve"

    );

    // ═══════════════════════════════════════════════════════════════════
    // TYPES DE NETTOYAGE
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> TYPE_NETTOYAGE_VALUES = Set.of(
            "laveuse_eau",
            "separateur_feuilles",
            "soufflerie"
    );

    // ═══════════════════════════════════════════════════════════════════
    // TYPES DE SÉPARATION
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> TYPE_SEPARATION_VALUES = Set.of(
            "decantation_naturelle",
            "decanteur_2_phases",
            "decanteur_3_phases"
    );
    public static final Set<String> TYPE_EXTRACTION_VALUES = Set.of(
    "centrifugation_3_phases",
            "centrifugation_2_phases",
            "presse_hydraulique"
    );
    // ═══════════════════════════════════════════════════════════════════
    // CONTRÔLE DE TEMPÉRATURE (OUI/NON)
    // ═══════════════════════════════════════════════════════════════════
    public static final Set<String> CONTROLE_TEMPERATURE_VALUES = Set.of(
            "oui",
            "non"
    );

    // ═══════════════════════════════════════════════════════════════════
    // RANGES NUMÉRIQUES (MIN-MAX)
    // ═══════════════════════════════════════════════════════════════════

    public static class NumericRange {
        public final double min;
        public final double max;

        public NumericRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public boolean isInRange(double value) {
            return value >= min && value <= max;
        }

        public String getMessage() {
            return String.format("[%.1f, %.1f]", min, max);
        }
    }

    // Ranges de validation (alignés avec Python validation_config.json)
    public static final Map<String, NumericRange> NUMERIC_RANGES = Map.ofEntries(
            Map.entry("maturiteNiveau15", new NumericRange(1.0, 5.0)),
            Map.entry("temperatureMalaxageC", new NumericRange(24.0, 27.0)),
            Map.entry("dureeMalaxageMin", new NumericRange(25.0, 40.0)),
            Map.entry("vitesseDecanteurTrMin", new NumericRange(3000.0, 3400.0)),
            Map.entry("humiditePourcent", new NumericRange(10.0, 30.5)),
            Map.entry("aciditeOlivesPourcent", new NumericRange(0.1, 2.5)),
            Map.entry("tauxFeuillesPourcent", new NumericRange(0.0, 5.0)),
            Map.entry("pressionExtractionBar", new NumericRange(50.0, 350.0)),
            // Champs Lab (optionnels)
            Map.entry("aciditeHuilePourcent", new NumericRange(0.1, 5.0)),
            Map.entry("indicePeroxydeMeqO2Kg", new NumericRange(5.0, 40.0)),
            Map.entry("polyphenolsMgKg", new NumericRange(100.0, 800.0)),
            Map.entry("k232", new NumericRange(1.5, 3.5)),
            Map.entry("k270", new NumericRange(0.1, 0.50))
    );

    // ═══════════════════════════════════════════════════════════════════
    // MÉTHODES DE VALIDATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Vérifie qu'une valeur énumérée est valide
     */
    public static boolean isValidEnum(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        return switch (fieldName.toLowerCase()) {
            case "variete" -> VARIETE_VALUES.contains(value);
            case "region" -> REGION_VALUES.contains(value);
            case "methoderecolte" -> METHODE_RECOLTE_VALUES.contains(value);
            case "typesol" -> TYPE_SOL_VALUES.contains(value);
            case "lavageeffectue" -> LAVAGE_EFFECTUE_VALUES.contains(value);
            case "typemachine" -> TYPE_MACHINE_VALUES.contains(value);
            case "typebroyeur" -> TYPE_BROYEUR_VALUES.contains(value);
            case "typemalaxeur" -> TYPE_MALAXEUR_VALUES.contains(value);
            case "typenettoyage" -> TYPE_NETTOYAGE_VALUES.contains(value);
            case "typeseparation" -> TYPE_SEPARATION_VALUES.contains(value);
            case "typeextraction" -> TYPE_EXTRACTION_VALUES.contains(value);
            case "controletemperature" -> CONTROLE_TEMPERATURE_VALUES.contains(value);
            default -> false;
        };
    }

    /**
     * Vérifie qu'une valeur numérique est dans le range accepté
     */
    public static boolean isNumericInRange(String fieldName, double value) {
        NumericRange range = NUMERIC_RANGES.get(fieldName);
        if (range == null) {
            return false; // Champ non reconnu
        }
        return range.isInRange(value);
    }

    /**
     * Retourne le range accepté pour un champ numérique
     */
    public static NumericRange getNumericRange(String fieldName) {
        return NUMERIC_RANGES.get(fieldName);
    }

    /**
     * Liste toutes les valeurs acceptées pour un champ énumération
     */
    public static Set<String> getEnumValues(String fieldName) {
        return switch (fieldName.toLowerCase()) {
            case "variete" -> VARIETE_VALUES;
            case "region" -> REGION_VALUES;
            case "methoderecolte" -> METHODE_RECOLTE_VALUES;
            case "typesol" -> TYPE_SOL_VALUES;
            case "lavageeffectue" -> LAVAGE_EFFECTUE_VALUES;
            case "typemachine" -> TYPE_MACHINE_VALUES;
            case "typebroyeur" -> TYPE_BROYEUR_VALUES;
            case "typemalaxeur" -> TYPE_MALAXEUR_VALUES;
            case "typenettoyage" -> TYPE_NETTOYAGE_VALUES;
            case "typeseparation" -> TYPE_SEPARATION_VALUES;
            case "typeextraction" -> TYPE_EXTRACTION_VALUES;
            case "controletemperature" -> CONTROLE_TEMPERATURE_VALUES;
            default -> Collections.emptySet();
        };
    }
}
