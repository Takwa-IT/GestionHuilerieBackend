package Config;

import java.util.*;

/**
 * Mappeur pour convertir les valeurs Frontend en valeurs Backend/Python conformes.
 *
 * Aligne:
 * - Frontend (TypeScript - chatbot-widget.component.ts)
 * - Backend (Java - PredictionInputDTO)
 * - Python (validation_config.json)
 *
 * ⚠️ IMPORTANT: Maintenir ce mappeur synchronisé avec les valeurs du frontend
 */
public class PredictionValueMapper {

    private static final Map<String, Map<String, String>> MAPPINGS = Map.ofEntries(
            // ═══════════════════════════════════════════════════════════════
            // RÉGION
            // Frontend peut envoyer des valeurs texte libre comme "Sfax",
            // on doit les mapper vers les régions acceptées
            // ═══════════════════════════════════════════════════════════════
            Map.entry("region", Map.ofEntries(
                    // Sfax → Nord (région productrice d'huile)
                    Map.entry("Sfax".toLowerCase(), "Nord"),
                    // Variantes possibles
                    Map.entry("nord".toLowerCase(), "Nord"),
                    Map.entry("centre".toLowerCase(), "Centre"),
                    Map.entry("sud".toLowerCase(), "Sud"),
                    Map.entry("north".toLowerCase(), "Nord"),
                    Map.entry("center".toLowerCase(), "Centre"),
                    Map.entry("south".toLowerCase(), "Sud")
            )),

            // ═══════════════════════════════════════════════════════════════
            // TYPE DE SOL
            // Frontend peut envoyer "argile" → on mappe vers "argileux"
            // ═══════════════════════════════════════════════════════════════
            Map.entry("typeSol", Map.ofEntries(
                    Map.entry("argile".toLowerCase(), "argileux"),
                    Map.entry("argileux".toLowerCase(), "argileux"),
                    Map.entry("clay".toLowerCase(), "argileux"),
                    Map.entry("calcaire".toLowerCase(), "calcaire"),
                    Map.entry("limestone".toLowerCase(), "calcaire"),
                    Map.entry("sable".toLowerCase(), "sableux"),
                    Map.entry("sableux".toLowerCase(), "sableux"),
                    Map.entry("sandy".toLowerCase(), "sableux")
            )),

            // ═══════════════════════════════════════════════════════════════
            // TYPE DE MACHINE
            // Frontend: "moderne_2_phases" → Python: "2_phase"
            // ═══════════════════════════════════════════════════════════════
            Map.entry("typeMachine", Map.ofEntries(
                    Map.entry("moderne_2_phases".toLowerCase(), "2_phase"),
                    Map.entry("2_phases".toLowerCase(), "2_phase"),
                    Map.entry("2phase".toLowerCase(), "2_phase"),
                    Map.entry("2_phase".toLowerCase(), "2_phase"),
                    Map.entry("decanteur_2_phases".toLowerCase(), "2_phase"),
                    Map.entry("2-phase".toLowerCase(), "2_phase"),

                    Map.entry("moderne_3_phases".toLowerCase(), "3_phase"),
                    Map.entry("3_phases".toLowerCase(), "3_phase"),
                    Map.entry("3phase".toLowerCase(), "3_phase"),
                    Map.entry("3_phase".toLowerCase(), "3_phase"),
                    Map.entry("decanteur_3_phases".toLowerCase(), "3_phase"),
                    Map.entry("3-phase".toLowerCase(), "3_phase"),

                    Map.entry("presse".toLowerCase(), "presse"),
                    Map.entry("presse_hydraulique".toLowerCase(), "presse"),
                    Map.entry("traditional_press".toLowerCase(), "presse"),
                    Map.entry("press".toLowerCase(), "presse")
            )),

            // ═══════════════════════════════════════════════════════════════
            // TYPE DE BROYEUR
            // Frontend: "standard" → peut être "marteaux" ou "meule"
            // (par défaut: "marteaux" si pas spécifié)
            // ═══════════════════════════════════════════════════════════════
            Map.entry("typeBroyeur", Map.ofEntries(
                    Map.entry("standard".toLowerCase(), "marteaux"),
                    Map.entry("marteaux".toLowerCase(), "marteaux"),
                    Map.entry("hammer".toLowerCase(), "marteaux"),
                    Map.entry("hammer_mill".toLowerCase(), "marteaux"),
                    Map.entry("meule".toLowerCase(), "meule"),
                    Map.entry("stone".toLowerCase(), "meule"),
                    Map.entry("stone_mill".toLowerCase(), "meule")
            )),

            // ═══════════════════════════════════════════════════════════════
            // TYPE DE MALAXEUR
            // Frontend: "standard" → peut être "horizontal" ou "vertical"
            // (par défaut: "horizontal" si pas spécifié)
            // ═══════════════════════════════════════════════════════════════
            Map.entry("typeMalaxeur", Map.ofEntries(
                    Map.entry("standard".toLowerCase(), "horizontal"),
                    Map.entry("horizontal".toLowerCase(), "horizontal"),
                    Map.entry("vertical".toLowerCase(), "vertical")
            )),

            // ═══════════════════════════════════════════════════════════════
            // TYPE DE NETTOYAGE
            // Frontend: "standard" → "laveuse_eau" (par défaut)
            // ═══════════════════════════════════════════════════════════════
            Map.entry("typeNettoyage", Map.ofEntries(
                    Map.entry("standard".toLowerCase(), "laveuse_eau"),
                    Map.entry("laveuse_eau".toLowerCase(), "laveuse_eau"),
                    Map.entry("water_washer".toLowerCase(), "laveuse_eau"),
                    Map.entry("separateur_feuilles".toLowerCase(), "separateur_feuilles"),
                    Map.entry("leaf_separator".toLowerCase(), "separateur_feuilles"),
                    Map.entry("soufflerie".toLowerCase(), "soufflerie"),
                    Map.entry("blower".toLowerCase(), "soufflerie")
            )),

            // ═══════════════════════════════════════════════════════════════
            // TYPE DE SÉPARATION
            // Frontend: "standard" → "decanteur_2_phases" (par défaut)
            // ═══════════════════════════════════════════════════════════════
            Map.entry("typeSeparation", Map.ofEntries(
                    Map.entry("standard".toLowerCase(), "decanteur_2_phases"),
                    Map.entry("decantation_naturelle".toLowerCase(), "decantation_naturelle"),
                    Map.entry("natural".toLowerCase(), "decantation_naturelle"),
                    Map.entry("decanteur_2_phases".toLowerCase(), "decanteur_2_phases"),
                    Map.entry("2_phase_decanter".toLowerCase(), "decanteur_2_phases"),
                    Map.entry("decanteur_3_phases".toLowerCase(), "decanteur_3_phases"),
                    Map.entry("3_phase_decanter".toLowerCase(), "decanteur_3_phases")
            ))
    );

    /**
     * Mappe une valeur Frontend vers une valeur Backend/Python acceptée
     *
     * @param fieldName Le nom du champ (typeSol, region, etc.)
     * @param value La valeur du frontend
     * @return La valeur mappée, ou la valeur originale si pas de mapping trouvé
     */
    public static String mapValue(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        Map<String, String> fieldMapping = MAPPINGS.get(fieldName);
        if (fieldMapping == null) {
            // Pas de mapping pour ce champ, retourner la valeur originale
            return value;
        }

        String mappedValue = fieldMapping.get(value.toLowerCase().trim());
        if (mappedValue != null) {
            return mappedValue;
        }

        // Si pas de mapping trouvé, retourner la valeur originale
        // (elle sera validée par le validateur)
        return value;
    }

    /**
     * Mappe les champs d'énumération d'un DTO PredictionInputDTO
     * Utile pour normaliser les valeurs Frontend avant validation
     */
    public static void normalizeEnumFields(dto.PredictionInputDTO dto) {
        if (dto == null) return;

        if (dto.getRegion() != null) {
            dto.setRegion(mapValue("region", dto.getRegion()));
        }
        if (dto.getTypeSol() != null) {
            dto.setTypeSol(mapValue("typeSol", dto.getTypeSol()));
        }
        if (dto.getTypeMachine() != null) {
            dto.setTypeMachine(mapValue("typeMachine", dto.getTypeMachine()));
        }
        if (dto.getTypeBroyeur() != null) {
            dto.setTypeBroyeur(mapValue("typeBroyeur", dto.getTypeBroyeur()));
        }
        if (dto.getTypeMalaxeur() != null) {
            dto.setTypeMalaxeur(mapValue("typeMalaxeur", dto.getTypeMalaxeur()));
        }
        if (dto.getTypeNettoyage() != null) {
            dto.setTypeNettoyage(mapValue("typeNettoyage", dto.getTypeNettoyage()));
        }
        if (dto.getTypeSeparation() != null) {
            dto.setTypeSeparation(mapValue("typeSeparation", dto.getTypeSeparation()));
        }
    }

    /**
     * Retourne les mappages disponibles pour un champ donné
     */
    public static Map<String, String> getMappingsForField(String fieldName) {
        return MAPPINGS.getOrDefault(fieldName, Collections.emptyMap());
    }
}
