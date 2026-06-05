// Teste : Services.GuideProductionTemplateFactory — Génération des templates de guides de production
package Services;

import dto.EtapeProductionCreateDTO;
import dto.ParametreEtapeCreateDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests de GuideProductionTemplateFactory")
class GuideProductionTemplateFactoryTest {

    private ParametreEtapeCreateDTO findParametreByCode(List<EtapeProductionCreateDTO> etapes, String code) {
        for (EtapeProductionCreateDTO etape : etapes) {
            if (etape.getParametres() != null) {
                for (ParametreEtapeCreateDTO param : etape.getParametres()) {
                    if (code.equals(param.getCodeParametre())) {
                        return param;
                    }
                }
            }
        }
        return null;
    }

    @Nested
    @DisplayName("buildDefaultEtapes - 3_phase")
    class BuildDefaultEtapes3Phase {

        @Test
        @DisplayName("retourne 7 etapes pour 3Phase")
        void retourne7Etapes_pour3Phase() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("3_phase");
            assertThat(etapes).hasSize(7);
        }

        @Test
        @DisplayName("premiere etape est Reception pour 3Phase")
        void premiereEtapeEstReception_pour3Phase() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("3_phase");
            assertThat(etapes.get(0).getNom()).containsIgnoringCase("Réception");
            assertThat(etapes.get(0).getOrdre()).isEqualTo(1);
        }

        @Test
        @DisplayName("etape malaxeur contient temperature et duree pour 3Phase")
        void etapeMalaxeurContientTemperatureEtDuree_pour3Phase() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("3_phase");
            EtapeProductionCreateDTO etapeMalaxeur = etapes.get(3);
            assertThat(findParametreByCode(etapes, "temperature_malaxage_c")).isNotNull();
            assertThat(findParametreByCode(etapes, "duree_malaxage_min")).isNotNull();
        }

        @Test
        @DisplayName("etape decanteur contient vitesse pour 3Phase")
        void etapeDecanteurContientVitesse_pour3Phase() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("3_phase");
            assertThat(findParametreByCode(etapes, "vitesse_decanteur_tr_min")).isNotNull();
        }

        @Test
        @DisplayName("etape ajout eau presente pour 3Phase")
        void etapeAjoutEauPresente_pour3Phase() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("3_phase");
            ParametreEtapeCreateDTO param = findParametreByCode(etapes, "presence_ajout_eau");
            assertThat(param).isNotNull();
            assertThat(param.getValeur()).isEqualTo("1");
        }
    }

    @Nested
    @DisplayName("buildDefaultEtapes - 2_phase")
    class BuildDefaultEtapes2Phase {

        @Test
        @DisplayName("retourne 6 etapes pour 2Phase")
        void retourne6Etapes_pour2Phase() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("2_phase");
            assertThat(etapes).hasSize(6);
        }

        @Test
        @DisplayName("etape malaxeur contient temperature et duree pour 2Phase")
        void etapeMalaxeurContientTemperatureEtDuree_pour2Phase() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("2_phase");
            assertThat(findParametreByCode(etapes, "temperature_malaxage_c")).isNotNull();
            assertThat(findParametreByCode(etapes, "duree_malaxage_min")).isNotNull();
        }

        @Test
        @DisplayName("presence ajout eau vaut 0 pour 2Phase")
        void presenceAjoutEauVaut0_pour2Phase() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("2_phase");
            ParametreEtapeCreateDTO param = findParametreByCode(etapes, "presence_ajout_eau");
            assertThat(param).isNotNull();
            assertThat(param.getValeur()).isEqualTo("0");
        }

        @Test
        @DisplayName("ordres consecutifs et uniques pour 2Phase")
        void ordresConsecutifsEtUniques_pour2Phase() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("2_phase");
            assertThat(etapes).extracting("ordre").containsExactly(1, 2, 3, 4, 5, 6);
        }
    }

    @Nested
    @DisplayName("buildDefaultEtapes - presse")
    class BuildDefaultEtapesPresse {

        @Test
        @DisplayName("retourne 6 etapes pour Presse")
        void retourne6Etapes_pourPresse() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("presse");
            assertThat(etapes).hasSize(6);
        }

        @Test
        @DisplayName("etape extraction contient pression pour Presse")
        void etapeExtractionContientPression_pourPresse() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("presse");
            assertThat(findParametreByCode(etapes, "pression_extraction_bar")).isNotNull();
            ParametreEtapeCreateDTO param = findParametreByCode(etapes, "presence_presse");
            assertThat(param).isNotNull();
            assertThat(param.getValeur()).isEqualTo("1");
        }

        @Test
        @DisplayName("dernier etape est Stockage pour Presse")
        void dernierEtapeEstStockage_pourPresse() {
            List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("presse");
            assertThat(etapes.get(5).getNom()).containsIgnoringCase("Stockage");
        }
    }

    @Nested
    @DisplayName("buildDefaultEtapes - cas limites")
    class BuildDefaultEtapesCasLimites {

        @Test
        @DisplayName("leve exception pour type machine inconnu")
        void leveException_pourTypeMachineInconnu() {
            assertThatThrownBy(() -> GuideProductionTemplateFactory.buildDefaultEtapes("inconnu"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Type de machine non supporte");
        }

        @Test
        @DisplayName("leve exception pour type machine vide")
        void leveException_pourTypeMachineVide() {
            assertThatThrownBy(() -> GuideProductionTemplateFactory.buildDefaultEtapes(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("obligatoire");
        }

        @Test
        @DisplayName("leve exception pour type machine null")
        void leveException_pourTypeMachineNull() {
            assertThatThrownBy(() -> GuideProductionTemplateFactory.buildDefaultEtapes(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("obligatoire");
        }
    }

    @Nested
    @DisplayName("normalizeTypeMachine")
    class NormalizeTypeMachine {

        @Test
        @DisplayName("normalise majuscules en minuscules")
        void normalise_majusculesEnMinuscules() {
            assertThat(GuideProductionTemplateFactory.normalizeTypeMachine("3_PHASE")).isEqualTo("3_phase");
        }

        @Test
        @DisplayName("normalise espaces")
        void normalise_espaces() {
            assertThat(GuideProductionTemplateFactory.normalizeTypeMachine("  presse  ")).isEqualTo("presse");
        }

        @Test
        @DisplayName("retourne vide pour null")
        void retourneVide_pourNull() {
            assertThat(GuideProductionTemplateFactory.normalizeTypeMachine(null)).isEqualTo("");
        }
    }
}
