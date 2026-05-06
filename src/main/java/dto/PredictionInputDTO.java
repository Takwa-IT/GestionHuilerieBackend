package dto;

import lombok.Data;

/**
 * DTO intermédiaire pour la prédiction - contient les attributs
 * nécessaires au calcul de prédiction, séparé de ExecutionProduction.
 *
 * Synchronisé avec:
 * - systeme prediction/prediction/models/validation_config.json
 * - Frontend: chatbot-widget.component.ts (buildPredictionPayload)
 */
@Data
public class PredictionInputDTO {
    // ═══════════════════════════════════════════════════════════════════
    // CHAMPS CATÉGORIQUES (Énumérations alignées avec Python)
    // ═══════════════════════════════════════════════════════════════════

    // Variété d'olive: "Arbequina", "Chemlali", "Chetoui"
    private String variete;

    // Région: "Centre", "Nord", "Sud"
    private String region;

    // Méthode de récolte: "manuelle", "mecanique", "semi-mecanique"
    private String methodeRecolte;

    // Type de sol: "argileux", "calcaire", "sableux"
    private String typeSol;

    // Lavage effectué: "oui", "non"
    private String lavageEffectue;

    // Type de machine: "2_phase", "3_phase", "presse"
    private String typeMachine;

    // Type de broyeur: "marteaux", "meule"
    private String typeBroyeur;

    // Type de malaxeur: "horizontal", "vertical"
    private String typeMalaxeur;

    // Type de nettoyage: "laveuse_eau", "separateur_feuilles", "soufflerie"
    private String typeNettoyage;

    // Type de séparation: "decantation_naturelle", "decanteur_2_phases", "decanteur_3_phases"
    private String typeSeparation;

    // Contrôle de température: "oui", "non"
    private String controleTemperature;

    // ═══════════════════════════════════════════════════════════════════
    // CHAMPS NUMÉRIQUES (Ranges de validation)
    // ═══════════════════════════════════════════════════════════════════

    // Poids d'olives (kg): min 2201.1, max 12000.0
    private Double poidsOlivesKg;

    // Niveau de maturité (1-5): min 2.0, max 5.0
    private Double maturiteNiveau15;

    // Durée de stockage avant broyage (jours): min 0.0, max 5.0
    private Double dureeStockageJours;

    // Temps depuis récolte (heures): min 3.0, max 20.6
    private Double tempsDepuisRecolteHeures;

    // Température de malaxage (°C): min 23.5, max 29.0
    private Double temperatureMalaxageC;

    // Durée de malaxage (minutes): min 24.0, max 48.0
    private Double dureeMalaxageMin;

    // Vitesse du décanteur (tr/min): min 0.0, max 3400.0
    private Double vitesseDecanteurTrMin;

    // Humidité (pourcentage): min 12.0, max 22.5
    private Double humiditePourcent;

    // Acidité des olives (pourcentage): min 0.15, max 0.78
    private Double aciditeOlivesPourcent;

    // Taux de feuilles (pourcentage): min 0.1, max 1.9
    private Double tauxFeuillesPourcent;

    // Pression d'extraction (bar): min 95.0, max 145.0
    private Double pressionExtractionBar;

    // Nombre d'étapes de production
    private Integer nombreEtapes;

    // ═══════════════════════════════════════════════════════════════════
    // CHAMPS BOOLÉENS (Binaires: 0/1 ou true/false)
    // ═══════════════════════════════════════════════════════════════════

    // Présence d'ajout d'eau
    private Boolean presenceAjoutEau;

    // Présence de presse
    private Boolean presencePresse;

    // Présence de séparateur
    private Boolean presenceSeparateur;

    // ═══════════════════════════════════════════════════════════════════
    // CHAMPS LAB OPTIONNELS (Résultats d'analyse laboratoire)
    // Ces champs peuvent être null si analyse non effectuée
    // ═══════════════════════════════════════════════════════════════════

    // Acidité de l'huile (pourcentage): min 0.1, max 5.0
    private Double aciditeHuilePourcent;

    // Indice de peroxyde (meq O₂/kg): min 5.0, max 40.0
    private Double indicePeroxydeMeqO2Kg;

    // Polyphénols (mg/kg): min 100.0, max 800.0
    private Double polyphenolsMgKg;

    // Extinction K232 (UV à 232nm): min 1.5, max 3.5
    private Double k232;

    // Extinction K270 (UV à 270nm): min 0.1, max 0.50
    private Double k270;
}
