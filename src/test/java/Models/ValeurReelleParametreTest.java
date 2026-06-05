// Teste : Models.ValeurReelleParametre — Calcul des déviations et qualité
package Models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests de ValeurReelleParametre")
class ValeurReelleParametreTest {

    @Nested
    @DisplayName("Tests de calculerDeviation")
    class CalculerDeviation {

        @Test
        @DisplayName("calculerDeviation retourne zero quand valeur egale reference")
        void calculerDeviation_retourneZero_quandValeurEgaleReference() {
            ValeurReelleParametre valeur = new ValeurReelleParametre();
            valeur.setValeurReelle(30.0);
            Double deviation = valeur.calculerDeviation(30.0);
            assertThat(deviation).isEqualTo(0.0);
        }

        @Test
        @DisplayName("calculerDeviation retourne positif quand valeur superieure")
        void calculerDeviation_retournePositif_quandValeurSuperieure() {
            ValeurReelleParametre valeur = new ValeurReelleParametre();
            valeur.setValeurReelle(33.0);
            Double deviation = valeur.calculerDeviation(30.0);
            assertThat(deviation).isEqualTo(10.0);
        }

        @Test
        @DisplayName("calculerDeviation retourne negatif quand valeur inferieure")
        void calculerDeviation_retourneNegatif_quandValeurInferieure() {
            ValeurReelleParametre valeur = new ValeurReelleParametre();
            valeur.setValeurReelle(27.0);
            Double deviation = valeur.calculerDeviation(30.0);
            assertThat(deviation).isEqualTo(-10.0);
        }

        @Test
        @DisplayName("calculerDeviation retourne null quand valeur reference null")
        void calculerDeviation_retourneNull_quandValeurReferenceNull() {
            ValeurReelleParametre valeur = new ValeurReelleParametre();
            valeur.setValeurReelle(30.0);
            Double deviation = valeur.calculerDeviation(null);
            assertThat(deviation).isNull();
        }

        @Test
        @DisplayName("calculerDeviation retourne null quand valeur reference zero")
        void calculerDeviation_retourneNull_quandValeurReferenceZero() {
            ValeurReelleParametre valeur = new ValeurReelleParametre();
            valeur.setValeurReelle(30.0);
            Double deviation = valeur.calculerDeviation(0.0);
            assertThat(deviation).isNull();
        }
    }

    @Nested
    @DisplayName("Tests de determinerQualiteDeviation")
    class DeterminerQualiteDeviation {

        @Test
        @DisplayName("determinerQualiteDeviation FAIBLE quand deviation inferieure 10")
        void determinerQualiteDeviation_FAIBLE_quandDeviationInferieure10() {
            ValeurReelleParametre valeur = new ValeurReelleParametre();
            valeur.setDeviation(5.0);
            String qualite = valeur.determinerQualiteDeviation(10.0);
            assertThat(qualite).isEqualTo("FAIBLE");
        }

        @Test
        @DisplayName("determinerQualiteDeviation MODEREE quand deviation entre 10 et 15")
        void determinerQualiteDeviation_MODEREE_quandDeviationEntre10Et15() {
            ValeurReelleParametre valeur = new ValeurReelleParametre();
            valeur.setDeviation(12.0);
            String qualite = valeur.determinerQualiteDeviation(10.0);
            assertThat(qualite).isEqualTo("MODÉRÉE");
        }

        @Test
        @DisplayName("determinerQualiteDeviation IMPORTANTE quand deviation superieure 15")
        void determinerQualiteDeviation_IMPORTANTE_quandDeviationSuperieure15() {
            ValeurReelleParametre valeur = new ValeurReelleParametre();
            valeur.setDeviation(20.0);
            String qualite = valeur.determinerQualiteDeviation(10.0);
            assertThat(qualite).isEqualTo("IMPORTANTE");
        }
    }
}
