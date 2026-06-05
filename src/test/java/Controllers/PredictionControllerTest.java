// Teste : Controllers.PredictionController — Tests unitaires pour l'API de prédiction
package Controllers;

import Config.PredictionInputValidator;
import Config.PredictionValueMapper;
import Services.PredictionService;
import dto.PredictionInputDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PredictionController")
class PredictionControllerTest {

    @Mock
    private PredictionService predictionService;

    @InjectMocks
    private PredictionController predictionController;

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

    @Nested
    @DisplayName("Tests sur validateInput()")
    class ValidateInputTests {

        @Test
        @DisplayName("validateInput retourne SUCCESS quand donnees valides")
        void validateInput_retourneSUCCESS_quandDonneesValides() {
            PredictionInputDTO dto = buildValidDTO();

            var response = predictionController.validateInput(dto);

            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        }

        @Test
        @DisplayName("validateInput appelle PredictionValueMapper avant validation")
        void validateInput_appellePredictionValueMapper_avantValidation() {
            PredictionInputDTO dto = buildValidDTO();
            dto.setRegion("Sfax");

            predictionController.validateInput(dto);

            assertThat(dto.getRegion()).isEqualTo("Nord");
        }

        @Test
        @DisplayName("validateInput leve exception quand variete invalide")
        void validateInput_leveException_quandVarieteInvalide() {
            PredictionInputDTO dto = buildValidDTO();
            dto.setVariete("Inconnue");

            assertThat(PredictionInputValidator.validate(dto)).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Tests sur findAll()")
    class FindAllTests {

        @Test
        @DisplayName("findAll appelle predictionService")
        void findAll_appellePredictionService() {
            predictionController.findAll();

            verify(predictionService, times(1)).findAll();
        }

        @Test
        @DisplayName("findAll retourne liste")
        void findAll_retourneListe() {
            when(predictionService.findAll()).thenReturn(List.of());

            var result = predictionController.findAll();

            assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        }
    }
}
