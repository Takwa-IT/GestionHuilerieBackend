package Config;

import dto.PredictionInputDTO;
import java.util.*;

/**
 * Validateur pour PredictionInputDTO.
 * Assure la conformité avec Python validation_config.json
 *
 * Utilise: PredictionEnumConstants pour les énumérations et ranges
 */
public class PredictionInputValidator {

    /**
     * Valide un PredictionInputDTO complet
     * @return List d'erreurs (vide si valide)
     */
    public static List<String> validate(PredictionInputDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto == null) {
            errors.add("PredictionInputDTO ne doit pas être null");
            return errors;
        }

        // ═══════════════════════════════════════════════════════════════
        // VALIDATION DES CHAMPS CATÉGORIQUES (Énumérations)
        // ═══════════════════════════════════════════════════════════════

        validateEnumField(dto.getVariete(), "variete", errors);
        validateEnumField(dto.getRegion(), "region", errors);
        validateEnumField(dto.getMethodeRecolte(), "methodeRecolte", errors);
        validateEnumField(dto.getTypeSol(), "typeSol", errors);
        validateEnumField(dto.getLavageEffectue(), "lavageEffectue", errors);
        validateEnumField(dto.getTypeMachine(), "typeMachine", errors);
        validateEnumField(dto.getTypeBroyeur(), "typeBroyeur", errors);
        validateEnumField(dto.getTypeMalaxeur(), "typeMalaxeur", errors);
        validateEnumField(dto.getTypeNettoyage(), "typeNettoyage", errors);
        validateEnumField(dto.getTypeSeparation(), "typeSeparation", errors);
        validateEnumField(dto.getControleTemperature(), "controleTemperature", errors);

        // ═══════════════════════════════════════════════════════════════
        // VALIDATION DES CHAMPS NUMÉRIQUES
        // ═══════════════════════════════════════════════════════════════

        validateNumericField(dto.getPoidsOlivesKg(), "poidsOlivesKg", errors);
        validateNumericField(dto.getMaturiteNiveau15(), "maturiteNiveau15", errors);
        validateNumericField(dto.getDureeStockageJours(), "dureeStockageJours", errors);
        validateNumericField(dto.getTempsDepuisRecolteHeures(), "tempsDepuisRecolteHeures", errors);
        validateNumericField(dto.getTemperatureMalaxageC(), "temperatureMalaxageC", errors);
        validateNumericField(dto.getDureeMalaxageMin(), "dureeMalaxageMin", errors);
        validateNumericField(dto.getVitesseDecanteurTrMin(), "vitesseDecanteurTrMin", errors);
        validateNumericField(dto.getHumiditePourcent(), "humiditePourcent", errors);
        validateNumericField(dto.getAciditeOlivesPourcent(), "aciditeOlivesPourcent", errors);
        validateNumericField(dto.getTauxFeuillesPourcent(), "tauxFeuillesPourcent", errors);
        validateNumericField(dto.getPressionExtractionBar(), "pressionExtractionBar", errors);

        // ═══════════════════════════════════════════════════════════════
        // VALIDATION DES CHAMPS BOOLÉENS
        // ═══════════════════════════════════════════════════════════════

        validateBooleanField(dto.getPresenceAjoutEau(), "presenceAjoutEau", errors);
        validateBooleanField(dto.getPresencePresse(), "presencePresse", errors);
        validateBooleanField(dto.getPresenceSeparateur(), "presenceSeparateur", errors);

        // ═══════════════════════════════════════════════════════════════
        // VALIDATION DES CHAMPS LAB (OPTIONNELS)
        // ═══════════════════════════════════════════════════════════════

        // Lab fields are optional, only validate if present
        if (dto.getAciditeHuilePourcent() != null) {
            validateNumericField(dto.getAciditeHuilePourcent(), "aciditeHuilePourcent", errors);
        }
        if (dto.getIndicePeroxydeMeqO2Kg() != null) {
            validateNumericField(dto.getIndicePeroxydeMeqO2Kg(), "indicePeroxydeMeqO2Kg", errors);
        }
        if (dto.getPolyphenolsMgKg() != null) {
            validateNumericField(dto.getPolyphenolsMgKg(), "polyphenolsMgKg", errors);
        }
        if (dto.getK232() != null) {
            validateNumericField(dto.getK232(), "k232", errors);
        }
        if (dto.getK270() != null) {
            validateNumericField(dto.getK270(), "k270", errors);
        }

        return errors;
    }

    /**
     * Valide un champ énumération
     */
    private static void validateEnumField(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(String.format("Champ '%s' est requis", fieldName));
            return;
        }

        if (!PredictionEnumConstants.isValidEnum(fieldName, value)) {
            Set<String> validValues = PredictionEnumConstants.getEnumValues(fieldName);
            errors.add(String.format(
                    "Champ '%s' a une valeur invalide '%s'. Valeurs acceptées: %s",
                    fieldName, value, validValues
            ));
        }
    }

    /**
     * Valide un champ numérique
     */
    private static void validateNumericField(Double value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(String.format("Champ '%s' est requis et doit être un nombre", fieldName));
            return;
        }

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            errors.add(String.format("Champ '%s' doit être un nombre valide (pas NaN ou Infinity)", fieldName));
            return;
        }

        if (!PredictionEnumConstants.isNumericInRange(fieldName, value)) {
            PredictionEnumConstants.NumericRange range =
                    PredictionEnumConstants.getNumericRange(fieldName);
            if (range != null) {
                errors.add(String.format(
                        "Champ '%s' = %.2f est hors de la plage acceptée %s",
                        fieldName, value, range.getMessage()
                ));
            }
        }
    }

    /**
     * Valide un champ booléen
     */
    private static void validateBooleanField(Boolean value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(String.format("Champ '%s' est requis et doit être true/false", fieldName));
        }
    }

    /**
     * Vérifie si le DTO est valide
     * @return true si valide, false sinon
     */
    public static boolean isValid(PredictionInputDTO dto) {
        return validate(dto).isEmpty();
    }

    /**
     * Retourne les erreurs de validation sous forme de chaîne
     */
    public static String getValidationErrorsAsString(PredictionInputDTO dto) {
        List<String> errors = validate(dto);
        if (errors.isEmpty()) {
            return "Aucune erreur";
        }
        return String.join("\n- ", errors);
    }
}
