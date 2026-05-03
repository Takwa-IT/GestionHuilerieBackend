package Services;

import Models.*;
import Repositories.*;
import dto.SaveValeursReellesRequest;
import dto.ValeurReelleParametreDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Service
@Transactional
public class ValeurReelleService {

    @Autowired
    private ValeurReelleParametreRepository valeurReelleRepo;

    @Autowired
    private ExecutionProductionRepository executionRepo;

    @Autowired
    private ParametreEtapeRepository parametreEtapeRepo;

    private static final double TOLERANCE_DEFAULT = 10.0; // ±10%
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Set<String> ALLOWED_PARAM_KEYS = Set.of(
            "temperature_malaxage_c",
            "duree_malaxage_min",
            "vitesse_decanteur_tr_min",
            "vitesse_decanteur",
            "vitesse",
            "pression_extraction_bar",
            "presence_presse",
            "presence_ajout_eau",
            "presence_separateur");

    /**
     * Sauvegarde les valeurs réelles d'une exécution
     */
    public List<ValeurReelleParametreDTO> saveValeursReelles(
            Long executionId,
            SaveValeursReellesRequest request) throws Exception {

        // Valider l'exécution
        ExecutionProduction execution = executionRepo.findById(executionId)
                .orElseThrow(() -> new Exception("Exécution non trouvée: " + executionId));

        // Supprimer les anciennes valeurs
        List<ValeurReelleParametre> existantes = valeurReelleRepo.findByExecutionProduction(execution);
        if (!existantes.isEmpty()) {
            valeurReelleRepo.deleteAll(existantes);
        }

        // Créer et sauvegarder les nouvelles valeurs
        List<ValeurReelleParametre> nouvellesValeurs = request.getValeursReelles()
                .stream()
                .map(input -> {
                    ParametreEtape parametre = parametreEtapeRepo.findById(input.getParametreEtapeId())
                            .orElseThrow(
                                    () -> new RuntimeException("Paramètre non trouvé: " + input.getParametreEtapeId()));

                    String normalizedKey = normalizeParamKey(
                            parametre.getCodeParametre() != null && !parametre.getCodeParametre().isBlank()
                                    ? parametre.getCodeParametre()
                                    : parametre.getNomParametre());

                    if (!ALLOWED_PARAM_KEYS.contains(normalizedKey)) {
                        throw new IllegalArgumentException(
                                "Le parametre '" + normalizedKey + "' n'accepte pas de valeur reelle.");
                    }

                    Double valeurEstimee = extractEstimatedValue(execution, parametre);

                    ValeurReelleParametre valeur = new ValeurReelleParametre();
                    valeur.setExecutionProduction(execution);
                    valeur.setParametreEtape(parametre);
                    valeur.setValeurReelle(input.getValeurReelle());
                    valeur.setUniteMesure(input.getUniteMesure());
                    valeur.setValeurEstimee(valeurEstimee);

                    // Calculer déviation
                    Double deviation = valeur.calculerDeviation();
                    valeur.setDeviation(deviation);
                    valeur.setQualiteDeviation(valeur.determinerQualiteDeviation(TOLERANCE_DEFAULT));

                    return valeur;
                })
                .collect(Collectors.toList());

        List<ValeurReelleParametre> saved = valeurReelleRepo.saveAll(nouvellesValeurs);

        // Mettre à jour les champs d'exécution si paramètres principaux
        updateExecutionWithRealValues(execution, saved);
        executionRepo.save(execution);

        return saved.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les valeurs réelles d'une exécution
     */
    public List<ValeurReelleParametreDTO> getValeursReelles(Long executionId) throws Exception {
        ExecutionProduction execution = executionRepo.findById(executionId)
                .orElseThrow(() -> new Exception("Exécution non trouvée: " + executionId));

        return valeurReelleRepo.findByExecutionProduction(execution)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Exporte les valeurs réelles pour réentraînement du modèle
     */
    public List<ValeurReelleParametreDTO> getValeursForRetraining(
            LocalDateTime depuis,
            LocalDateTime jusqu) {
        return valeurReelleRepo.findForExport(depuis, jusqu)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les paramètres principaux (température, durée, vitesse, pression)
     */
    public List<ValeurReelleParametreDTO> getMainParameters(LocalDateTime depuis) {
        return valeurReelleRepo.findMainParametersSince(depuis)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Extrait la valeur estimée de l'exécution selon le paramètre
     */
    private Double extractEstimatedValue(ExecutionProduction execution, ParametreEtape parametre) {
        Double byCode = extractFromExecutionByKey(execution, parametre.getCodeParametre());
        if (byCode != null) {
            return byCode;
        }

        Double byNomParametre = extractFromExecutionByKey(execution, parametre.getNomParametre());
        if (byNomParametre != null) {
            return byNomParametre;
        }

        Double byNom = extractFromExecutionByKey(execution, parametre.getNom());
        if (byNom != null) {
            return byNom;
        }

        return parseDoubleSafely(parametre.getValeur());
    }

    private Double extractFromExecutionByKey(ExecutionProduction execution, String rawKey) {
        String key = normalizeParamKey(rawKey);
        return switch (key) {
            case "temperature_malaxage_c" -> execution.getTemperatureMalaxageC();
            case "temperature_malaxage" -> execution.getTemperatureMalaxageC();
            case "temperature" -> execution.getTemperatureMalaxageC();
            case "duree_malaxage_min" -> execution.getDureeMalaxageMin();
            case "duree_malaxage" -> execution.getDureeMalaxageMin();
            case "duree" -> execution.getDureeMalaxageMin();
            case "vitesse_decanteur_tr_min" -> execution.getVitesseDecanteurTrMin();
            case "vitesse_decanteur" -> execution.getVitesseDecanteurTrMin();
            case "vitesse" -> execution.getVitesseDecanteurTrMin();
            case "pression_extraction_bar" -> execution.getPressionExtractionBar();
            case "pression_extraction" -> execution.getPressionExtractionBar();
            case "pression" -> execution.getPressionExtractionBar();
            default -> null;
        };
    }

    private String normalizeParamKey(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .trim();
        return normalized
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private Double parseDoubleSafely(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String numeric = value.trim().replace(',', '.').replaceAll("[^0-9.\\-]", "");
        if (numeric.isBlank() || "-".equals(numeric) || ".".equals(numeric) || "-.".equals(numeric)) {
            return null;
        }

        try {
            return Double.parseDouble(numeric);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Met à jour les champs d'exécution avec les valeurs réelles
     */
    private void updateExecutionWithRealValues(ExecutionProduction execution, List<ValeurReelleParametre> valeurs) {
        for (ValeurReelleParametre valeur : valeurs) {
            String paramName = normalizeParamKey(valeur.getParametreEtape().getNomParametre());

            switch (paramName) {
                case "temperature_malaxage_c" -> execution.setTemperatureMalaxageC(valeur.getValeurReelle());
                case "duree_malaxage_min" -> execution.setDureeMalaxageMin(valeur.getValeurReelle());
                case "vitesse_decanteur_tr_min" -> execution.setVitesseDecanteurTrMin(valeur.getValeurReelle());
                case "pression_extraction_bar" -> execution.setPressionExtractionBar(valeur.getValeurReelle());
            }
        }
    }

    /**
     * Convertit l'entité en DTO
     */
    private ValeurReelleParametreDTO toDTO(ValeurReelleParametre entity) {
        ValeurReelleParametreDTO dto = new ValeurReelleParametreDTO();
        dto.setIdValeurReelleParametre(entity.getIdValeurReelleParametre());
        dto.setExecutionProductionId(entity.getExecutionProduction().getIdExecutionProduction());
        dto.setParametreEtapeId(entity.getParametreEtape().getIdParametreEtape());
        dto.setNomParametre(entity.getParametreEtape().getNomParametre());
        dto.setUniteMesure(entity.getUniteMesure());
        dto.setValeurEstimee(entity.getValeurEstimee());
        dto.setValeurReelle(entity.getValeurReelle());
        dto.setDeviation(entity.getDeviation());
        dto.setQualiteDeviation(entity.getQualiteDeviation());
        dto.setDateCreation(entity.getDateCreation() != null ? entity.getDateCreation().format(DATE_FORMATTER) : null);
        dto.setDateModification(
                entity.getDateModification() != null ? entity.getDateModification().format(DATE_FORMATTER) : null);
        dto.setIsOutsideTolerance(entity.getDeviation() != null && Math.abs(entity.getDeviation()) > TOLERANCE_DEFAULT);
        dto.setIsSignificantDeviation(entity.getDeviation() != null && Math.abs(entity.getDeviation()) > 15);
        return dto;
    }
}
