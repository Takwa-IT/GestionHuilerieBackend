package Services;

import dto.EtapeProductionCreateDTO;
import dto.ParametreEtapeCreateDTO;

import java.util.ArrayList;
import java.util.List;

public final class GuideProductionTemplateFactory {

        private static final String PARAM_TEMPERATURE_MALAXAGE = "temperature_malaxage_c";
        private static final String PARAM_DUREE_MALAXAGE = "duree_malaxage_min";
        private static final String PARAM_PRESENCE_AJOUT_EAU = "presence_ajout_eau";
        private static final String PARAM_VITESSE_DECANTEUR = "vitesse_decanteur_tr_min";
        private static final String PARAM_PRESENCE_SEPARATEUR = "presence_separateur";
        private static final String PARAM_PRESSION_EXTRACTION = "pression_extraction_bar";
        private static final String PARAM_PRESENCE_PRESSE = "presence_presse";

        private GuideProductionTemplateFactory() {
        }

        public static List<EtapeProductionCreateDTO> buildDefaultEtapes(String typeMachine) {
                String normalizedTypeMachine = normalizeTypeMachine(typeMachine);
                if (normalizedTypeMachine.isBlank()) {
                        throw new IllegalArgumentException("Le type de machine est obligatoire pour générer un guide.");
                }

                return switch (normalizedTypeMachine) {
                        case "3_phase" -> buildThreePhaseEtapes();
                        case "2_phase" -> buildTwoPhaseEtapes();
                        case "presse" -> buildPresseEtapes();
                        default -> throw new IllegalArgumentException(
                                        "Type de machine non supporte: " + normalizedTypeMachine);
                };
        }

        public static String normalizeTypeMachine(String typeMachine) {
                if (typeMachine == null) {
                        return "";
                }

                return typeMachine.trim().toLowerCase();
        }

        private static EtapeProductionCreateDTO createEtape(int ordre, String nom, String description,
                        ParametreEtapeCreateDTO... parametres) {
                EtapeProductionCreateDTO etape = new EtapeProductionCreateDTO();
                etape.setNom(nom);
                etape.setOrdre(ordre);
                etape.setDescription(description);
                if (parametres != null && parametres.length > 0) {
                        etape.setParametres(List.of(parametres));
                }
                return etape;
        }

        private static ParametreEtapeCreateDTO createParametre(String code, String nom, String unite, String valeur,
                        String description) {
                ParametreEtapeCreateDTO parametre = new ParametreEtapeCreateDTO();
                parametre.setCodeParametre(code);
                parametre.setNom(nom);
                parametre.setUniteMesure(unite);
                parametre.setValeur(valeur);
                parametre.setDescription(description);
                return parametre;
        }

        private static List<EtapeProductionCreateDTO> buildThreePhaseEtapes() {
                List<EtapeProductionCreateDTO> etapes = new ArrayList<>();
                etapes.add(createEtape(1, "Réception",
                                "Réception des olives et contrôle initial de la matière première."));
                etapes.add(createEtape(2, "Nettoyage / Lavage",
                                "Nettoyage et lavage des olives avant transformation."));
                etapes.add(createEtape(3, "Broyage",
                                "Broyage de la matière première avant malaxage."));
                etapes.add(createEtape(
                                4,
                                "Malaxeur double cuve (optionnel)",
                                "Homogénéisation de la pâte avec contrôle de température et durée.",
                                createParametre(PARAM_TEMPERATURE_MALAXAGE, "Temperature de malaxage", "C", "27",
                                                "Temperature de malaxage"),
                                createParametre(PARAM_DUREE_MALAXAGE, "Duree de malaxage", "min", "40",
                                                "Duree de malaxage")));
                etapes.add(createEtape(
                                5,
                                "Ajout d'eau",
                                "Ajout d'eau nécessaire au procédé 3 phases.",
                                createParametre(PARAM_PRESENCE_AJOUT_EAU, "Presence ajout eau", "", "1",
                                                "1 = ajout d'eau actif")));
                etapes.add(createEtape(
                                6,
                                "Décanteur 3 phases + Séparateur vertical",
                                "Extraction et séparation par décanteur 3 phases suivi d'un séparateur vertical.",
                                createParametre(PARAM_VITESSE_DECANTEUR, "Vitesse du decanteur", "tr/min", "3200",
                                                "Vitesse du decanteur 3 phases"),
                                createParametre(PARAM_PRESENCE_SEPARATEUR, "Presence separateur", "", "1",
                                                "1 = separateur obligatoire")));
                etapes.add(createEtape(7, "Stockage", "Stockage de l'huile obtenue dans des conditions adaptées."));
                return etapes;
        }

        private static List<EtapeProductionCreateDTO> buildTwoPhaseEtapes() {
                List<EtapeProductionCreateDTO> etapes = new ArrayList<>();
                etapes.add(createEtape(1, "Réception",
                                "Réception des olives et contrôle initial de la matière première."));
                etapes.add(createEtape(2, "Nettoyage", "Nettoyage des olives avant transformation."));
                etapes.add(createEtape(3, "Broyage",
                                "Broyage de la matière première avant malaxage."));
                etapes.add(createEtape(
                                4,
                                "Malaxeur double cuve (optionnel)",
                                "Homogénéisation de la pâte avec contrôle de température et durée.",
                                createParametre(PARAM_TEMPERATURE_MALAXAGE, "Temperature de malaxage", "C", "27",
                                                "Temperature de malaxage"),
                                createParametre(PARAM_DUREE_MALAXAGE, "Duree de malaxage", "min", "40",
                                                "Duree de malaxage")));
                etapes.add(createEtape(
                                5,
                                "Décanteur 2 phases + Séparateur optionnel",
                                "Extraction et séparation par décanteur 2 phases sans ajout d'eau, avec séparateur optionnel selon la qualité obtenue.",
                                createParametre(PARAM_VITESSE_DECANTEUR, "Vitesse du decanteur", "tr/min", "3000",
                                                "Vitesse du decanteur 2 phases"),
                                createParametre(PARAM_PRESENCE_AJOUT_EAU, "Presence ajout eau", "", "0",
                                                "0 = pas d'ajout d'eau"),
                                createParametre(PARAM_PRESENCE_SEPARATEUR, "Presence separateur", "", "0",
                                                "0 ou 1 selon configuration")));
                etapes.add(createEtape(6, "Stockage", "Stockage de l'huile obtenue dans des conditions adaptées."));
                return etapes;
        }

        private static List<EtapeProductionCreateDTO> buildPresseEtapes() {
                List<EtapeProductionCreateDTO> etapes = new ArrayList<>();
                etapes.add(createEtape(1, "Réception",
                                "Réception des olives et contrôle initial de la matière première."));
                etapes.add(createEtape(2, "Lavage", "Lavage des olives avant transformation."));
                etapes.add(createEtape(3, "Broyage (meule)",
                                "Broyage traditionnel par meule."));
                etapes.add(createEtape(
                                4,
                                "Malaxeur double cuve (optionnel)",
                                "Homogénéisation de la pâte avec contrôle de température et durée.",
                                createParametre(PARAM_TEMPERATURE_MALAXAGE, "Temperature de malaxage", "C", "27",
                                                "Temperature de malaxage"),
                                createParametre(PARAM_DUREE_MALAXAGE, "Duree de malaxage", "min", "40",
                                                "Duree de malaxage")));
                etapes.add(createEtape(
                                5,
                                "Extraction et Séparation (Décantation naturelle)",
                                "Extraction par presse hydraulique suivi d'une décantation naturelle.",
                                createParametre(PARAM_PRESSION_EXTRACTION, "Pression d extraction", "bar", "2.5",
                                                "Pression du pressage hydraulique"),
                                createParametre(PARAM_PRESENCE_PRESSE, "Presence presse", "", "1",
                                                "1 = pressage actif")));
                etapes.add(createEtape(6, "Stockage", "Stockage de l'huile obtenue dans des conditions adaptées."));
                return etapes;
        }
}