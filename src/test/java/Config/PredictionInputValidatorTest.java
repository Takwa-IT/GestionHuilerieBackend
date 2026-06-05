// Teste : Config.PredictionInputValidator — Validation des DTOs de prédiction
package Config;

import dto.PredictionInputDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests de PredictionInputValidator")
class PredictionInputValidatorTest {

    private PredictionInputDTO buildValidDTO() {
        PredictionInputDTO dto = new PredictionInputDTO();
        dto.setVariete("Chemlali");
        dto.setRegion("Nord");
        dto.setMethodeRecolte("manuelle");
        dto.setTypeSol("argileux");
        dto.setLavageEffectue("oui");
        dto.setTypeMachine("2_phase");
        dto.setTypeBroyeur("marteaux");
        dto.setTypeMalaxeur("horizontal");
        dto.setTypeNettoyage("laveuse_eau");
        dto.setTypeSeparation("decantation_naturelle");
        dto.setControleTemperature("oui");
        dto.setPoidsOlivesKg(500.0);
        dto.setMaturiteNiveau15(3.0);
        dto.setDureeStockageJours(2.0);
        dto.setTempsDepuisRecolteHeures(10.0);
        dto.setTemperatureMalaxageC(25.0);
        dto.setDureeMalaxageMin(30.0);
        dto.setVitesseDecanteurTrMin(3200.0);
        dto.setHumiditePourcent(20.0);
        dto.setAciditeOlivesPourcent(1.0);
        dto.setTauxFeuillesPourcent(2.0);
        dto.setPressionExtractionBar(100.0);
        dto.setPresenceAjoutEau(true);
        dto.setPresencePresse(false);
        dto.setPresenceSeparateur(true);
        return dto;
    }

    @Test
    @DisplayName("validate retourne liste vide quand DTO valide")
    void validate_retourneListeVide_quandDTOValide() {
        PredictionInputDTO dto = buildValidDTO();
        List<String> errors = PredictionInputValidator.validate(dto);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("validate retourne erreur quand variete invalide")
    void validate_retourneErreur_quandVarieteInvalide() {
        PredictionInputDTO dto = buildValidDTO();
        dto.setVariete("Inconnue");
        List<String> errors = PredictionInputValidator.validate(dto);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(error -> error.contains("variete"));
    }

    @Test
    @DisplayName("validate retourne erreur quand variete null")
    void validate_retourneErreur_quandVarieteNull() {
        PredictionInputDTO dto = buildValidDTO();
        dto.setVariete(null);
        List<String> errors = PredictionInputValidator.validate(dto);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(error -> error.contains("variete"));
    }

    @Test
    @DisplayName("validate retourne erreur quand temperature hors range")
    void validate_retourneErreur_quandTemperatureHorsRange() {
        PredictionInputDTO dto = buildValidDTO();
        dto.setTemperatureMalaxageC(99.0);
        List<String> errors = PredictionInputValidator.validate(dto);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(error -> error.contains("temperatureMalaxageC"));
    }

    @Test
    @DisplayName("validate retourne erreur quand champ boolean null")
    void validate_retourneErreur_quandChampBooleanNull() {
        PredictionInputDTO dto = buildValidDTO();
        dto.setPresenceAjoutEau(null);
        List<String> errors = PredictionInputValidator.validate(dto);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(error -> error.contains("presenceAjoutEau"));
    }

    @Test
    @DisplayName("validate retourne erreur quand region invalide")
    void validate_retourneErreur_quandRegionInvalide() {
        PredictionInputDTO dto = buildValidDTO();
        dto.setRegion("Tunis");
        List<String> errors = PredictionInputValidator.validate(dto);
        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(error -> error.contains("region"));
    }

    @Test
    @DisplayName("isValid retourne true quand DTO complet")
    void isValid_retourneTrue_quandDTOComplet() {
        PredictionInputDTO dto = buildValidDTO();
        assertThat(PredictionInputValidator.isValid(dto)).isTrue();
    }

    @Nested
    @DisplayName("Tests champs lab optionnels")
    class ChampsLabOptionnels {

        @Test
        @DisplayName("validate champs lab optionnels accepte null")
        void validate_champsLabOptionnels_accepteNull() {
            PredictionInputDTO dto = buildValidDTO();
            dto.setAciditeHuilePourcent(null);
            dto.setK232(null);
            dto.setK270(null);
            dto.setIndicePeroxydeMeqO2Kg(null);
            dto.setPolyphenolsMgKg(null);
            List<String> errors = PredictionInputValidator.validate(dto);
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("validate champs lab optionnels validates si presents")
        void validate_champsLabOptionnels_validatesSiPresents() {
            PredictionInputDTO dto = buildValidDTO();
            dto.setK232(99.0);
            List<String> errors = PredictionInputValidator.validate(dto);
            assertThat(errors).isNotEmpty();
            assertThat(errors).anyMatch(error -> error.contains("k232"));
        }
    }
}
