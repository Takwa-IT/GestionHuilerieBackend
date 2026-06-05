// Teste : Services.FeatureAggregator — Agrégation des features pour le modèle ML
package Services;

import Models.EtapeProduction;
import Models.ExecutionProduction;
import Models.GuideProduction;
import Models.ParametreEtape;
import Models.ValeurReelleParametre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@DisplayName("Tests de FeatureAggregator")
class FeatureAggregatorTest {

    private FeatureAggregator aggregator = new FeatureAggregator();

    private GuideProduction buildGuide(Map<String, String>... etapesParams) {
        GuideProduction guide = new GuideProduction();
        List<EtapeProduction> etapes = new ArrayList<>();

        for (int i = 0; i < etapesParams.length; i++) {
            EtapeProduction etape = new EtapeProduction();
            etape.setOrdre(i + 1);
            etape.setNom("Etape " + (i + 1));
            etape.setGuideProduction(guide);

            List<ParametreEtape> parametres = new ArrayList<>();
            for (Map.Entry<String, String> entry : etapesParams[i].entrySet()) {
                ParametreEtape param = new ParametreEtape();
                param.setCodeParametre(entry.getKey());
                param.setValeur(entry.getValue());
                param.setEtapeProduction(etape);
                parametres.add(param);
            }
            etape.setParametres(parametres);
            etapes.add(etape);
        }

        guide.setEtapes(etapes);
        return guide;
    }

    @Nested
    @DisplayName("extractAllParametersWithEtapeIndex")
    class ExtractAllParametersWithEtapeIndex {

        @Test
        @DisplayName("retourne map vide pour guide null")
        void retourneMapVide_pourGuideNull() {
            Map<String, String> result = aggregator.extractAllParametersWithEtapeIndex(null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("retourne map vide pour guide avec etapes null")
        void retourneMapVide_pourGuideAvecEtapesNull() {
            GuideProduction guide = new GuideProduction();
            guide.setEtapes(null);
            Map<String, String> result = aggregator.extractAllParametersWithEtapeIndex(guide);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("parametre etape 1 nomme code etape 1")
        void parametreEtape1_nommé_code_etape_1() {
            GuideProduction guide = buildGuide(
                    Map.of("temperature_malaxage_c", "27")
            );
            Map<String, String> result = aggregator.extractAllParametersWithEtapeIndex(guide);
            assertThat(result).containsKey("temperature_malaxage_c_etape_1");
            assertThat(result.get("temperature_malaxage_c_etape_1")).isEqualTo("27");
        }

        @Test
        @DisplayName("deux etapes meme parametre genere deux cles")
        void deuxEtapesMemeParametre_generDeuxCles() {
            GuideProduction guide = buildGuide(
                    Map.of("temperature_malaxage_c", "27"),
                    Map.of("temperature_malaxage_c", "25")
            );
            Map<String, String> result = aggregator.extractAllParametersWithEtapeIndex(guide);
            assertThat(result).containsKey("temperature_malaxage_c_etape_1");
            assertThat(result).containsKey("temperature_malaxage_c_etape_2");
        }

        @Test
        @DisplayName("parametres multiples tous indexes")
        void parametresMultiples_tousIndexes() {
            GuideProduction guide = buildGuide(
                    Map.of("temperature_malaxage_c", "27", "duree_malaxage_min", "30")
            );
            Map<String, String> result = aggregator.extractAllParametersWithEtapeIndex(guide);
            assertThat(result).containsKey("temperature_malaxage_c_etape_1");
            assertThat(result).containsKey("duree_malaxage_min_etape_1");
        }
    }

    @Nested
    @DisplayName("groupParametersByCanonicalCode")
    class GroupParametersByCanonicalCode {

        @Test
        @DisplayName("groupe par code une seule valeur")
        void groupeParCode_uneSeuleValeur() {
            GuideProduction guide = buildGuide(
                    Map.of("temperature_malaxage_c", "27")
            );
            Map<String, List<String>> result = aggregator.groupParametersByCanonicalCode(guide);
            assertThat(result).containsKey("temperature_malaxage_c");
            assertThat(result.get("temperature_malaxage_c")).containsExactly("27");
        }

        @Test
        @DisplayName("groupe par code deux valeurs meme code")
        void groupeParCode_deuxValeursMemeCode() {
            GuideProduction guide = buildGuide(
                    Map.of("temperature_malaxage_c", "27"),
                    Map.of("temperature_malaxage_c", "25")
            );
            Map<String, List<String>> result = aggregator.groupParametersByCanonicalCode(guide);
            assertThat(result.get("temperature_malaxage_c")).containsExactlyInAnyOrder("27", "25");
        }

        @Test
        @DisplayName("retourne map vide pour guide vide")
        void retourneMapVide_pourGuideVide() {
            GuideProduction guide = new GuideProduction();
            guide.setEtapes(new ArrayList<>());
            Map<String, List<String>> result = aggregator.groupParametersByCanonicalCode(guide);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("aggregateCanonicalParameter")
    class AggregateCanonicalParameter {

        @Test
        @DisplayName("retourne moyenne pour deux valeurs")
        void retourneMoyenne_pourDeuxValeurs() {
            List<String> values = List.of("30.0", "26.0");
            Double result = aggregator.aggregateCanonicalParameter("temperature_malaxage_c", values);
            assertThat(result).isEqualTo(28.0);
        }

        @Test
        @DisplayName("retourne valeur unique pour une seule valeur")
        void retourneValeurUnique_pourUneSeuleValeur() {
            List<String> values = List.of("27.0");
            Double result = aggregator.aggregateCanonicalParameter("temperature_malaxage_c", values);
            assertThat(result).isEqualTo(27.0);
        }

        @Test
        @DisplayName("retourne null pour liste null")
        void retourneNull_pourListeNull() {
            Double result = aggregator.aggregateCanonicalParameter("temperature_malaxage_c", null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("retourne null pour liste vide")
        void retourneNull_pourListeVide() {
            Double result = aggregator.aggregateCanonicalParameter("temperature_malaxage_c", new ArrayList<>());
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("ignore valeurs non numeriques")
        void ignoreValeursNonNumeriques() {
            List<String> values = List.of("30.0", "abc", "26.0");
            Double result = aggregator.aggregateCanonicalParameter("temperature_malaxage_c", values);
            assertThat(result).isEqualTo(28.0);
        }

        @Test
        @DisplayName("gere virgule comme decimale")
        void gereVirguleCommeDecimale() {
            List<String> values = List.of("27,5");
            Double result = aggregator.aggregateCanonicalParameter("temperature_malaxage_c", values);
            assertThat(result).isEqualTo(27.5);
        }
    }

    @Nested
    @DisplayName("extractExecutionRealValues")
    class ExtractExecutionRealValues {

        @Test
        @DisplayName("retourne map vide pour execution null")
        void retourneMapVide_pourExecutionNull() {
            Map<String, String> result = aggregator.extractExecutionRealValues(null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("retourne map vide pour valeurs reelles null")
        void retourneMapVide_pourValeursReellesNull() {
            ExecutionProduction execution = new ExecutionProduction();
            execution.setValeursReelles(null);
            Map<String, String> result = aggregator.extractExecutionRealValues(execution);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("retourne valeur avec suffixe override")
        void retourneValeurAvecSuffixeOverride() {
            ExecutionProduction execution = new ExecutionProduction();
            List<ValeurReelleParametre> valeurs = new ArrayList<>();

            ValeurReelleParametre valeur = new ValeurReelleParametre();
            valeur.setValeurReelle(28.5);

            ParametreEtape param = new ParametreEtape();
            param.setCodeParametre("temperature_malaxage_c");
            valeur.setParametreEtape(param);

            valeurs.add(valeur);
            execution.setValeursReelles(valeurs);

            Map<String, String> result = aggregator.extractExecutionRealValues(execution);
            assertThat(result).containsKey("temperature_malaxage_c_override");
            assertThat(result.get("temperature_malaxage_c_override")).isEqualTo("28.5");
        }

        @Test
        @DisplayName("retourne plusieurs overrides")
        void retournePlusieursOverrides() {
            ExecutionProduction execution = new ExecutionProduction();
            List<ValeurReelleParametre> valeurs = new ArrayList<>();

            ValeurReelleParametre valeur1 = new ValeurReelleParametre();
            valeur1.setValeurReelle(28.5);
            ParametreEtape param1 = new ParametreEtape();
            param1.setCodeParametre("temperature_malaxage_c");
            valeur1.setParametreEtape(param1);

            ValeurReelleParametre valeur2 = new ValeurReelleParametre();
            valeur2.setValeurReelle(35.0);
            ParametreEtape param2 = new ParametreEtape();
            param2.setCodeParametre("duree_malaxage_min");
            valeur2.setParametreEtape(param2);

            valeurs.add(valeur1);
            valeurs.add(valeur2);
            execution.setValeursReelles(valeurs);

            Map<String, String> result = aggregator.extractExecutionRealValues(execution);
            assertThat(result).containsKey("temperature_malaxage_c_override");
            assertThat(result).containsKey("duree_malaxage_min_override");
        }
    }
}
