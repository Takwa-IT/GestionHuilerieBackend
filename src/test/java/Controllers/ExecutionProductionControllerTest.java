// Teste : Controllers.ExecutionProductionController — Tests unitaires pour l'API d'exécution de production
package Controllers;

import Services.ExecutionProductionService;
import Services.PredictionService;
import dto.ExecutionPredictionStartDTO;
import dto.ExecutionProductionDTO;
import dto.ExecutionProductionUpdateDTO;
import dto.PredictionDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ExecutionProductionController")
class ExecutionProductionControllerTest {

    @Mock
    private ExecutionProductionService executionProductionService;

    @Mock
    private PredictionService predictionService;

    @InjectMocks
    private ExecutionProductionController executionProductionController;

    private ExecutionProductionDTO buildExecutionDTO() {
        ExecutionProductionDTO dto = new ExecutionProductionDTO();
        dto.setIdExecutionProduction(1L);
        dto.setReference("LOT-01");
        dto.setStatut("EN_COURS");
        return dto;
    }

    private PredictionDTO buildPredictionDTO() {
        PredictionDTO dto = new PredictionDTO();
        dto.setIdPrediction(1L);
        dto.setModePrediction("no_lab");
        dto.setRendementPreditPourcent(22.5);
        return dto;
    }

    @Nested
    @DisplayName("Tests sur findAll()")
    class FindAllTests {

        @Test
        @DisplayName("findAll retourne liste vide quand aucun execution")
        void findAll_retourneListeVide_quandAucunExecution() {
            when(executionProductionService.findAll(null)).thenReturn(List.of());

            var result = executionProductionController.findAll(null);

            assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        }

        @Test
        @DisplayName("findAll appelle executionProductionService")
        void findAll_appelleExecutionProductionService() {
            executionProductionController.findAll(null);

            verify(executionProductionService, times(1)).findAll(null);
        }
    }

    @Nested
    @DisplayName("Tests sur findById()")
    class FindByIdTests {

        @Test
        @DisplayName("findById retourne DTO quand existe")
        void findById_retourneDTO_quandExiste() {
            when(executionProductionService.findById(1L)).thenReturn(buildExecutionDTO());

            var result = executionProductionController.findById(1L);

            assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        }

        @Test
        @DisplayName("findById appelle executionProductionService")
        void findById_appelleExecutionProductionService() {
            executionProductionController.findById(1L);

            verify(executionProductionService, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Tests sur create()")
    class CreateTests {

        @Test
        @DisplayName("create appelle executionProductionService")
        void create_appelleExecutionProductionService() {
            dto.ExecutionProductionCreateDTO dto = new dto.ExecutionProductionCreateDTO();
            dto.setReference("LOT-01");
            dto.setDateDebut("2024-01-01");
            dto.setDateFinPrevue("2024-01-02");
            dto.setStatut("EN_COURS");
            dto.setGuideProductionId(1L);
            dto.setLotId(1L);

            when(executionProductionService.create(any())).thenReturn(buildExecutionDTO());

            executionProductionController.create(dto);

            verify(executionProductionService, times(1)).create(any());
        }

        @Test
        @DisplayName("create retourne DTO cree")
        void create_retourneDTOCree() {
            dto.ExecutionProductionCreateDTO dto = new dto.ExecutionProductionCreateDTO();
            dto.setReference("LOT-01");
            dto.setDateDebut("2024-01-01");
            dto.setDateFinPrevue("2024-01-02");
            dto.setStatut("EN_COURS");
            dto.setGuideProductionId(1L);
            dto.setLotId(1L);

            when(executionProductionService.create(any())).thenReturn(buildExecutionDTO());

            var result = executionProductionController.create(dto);

            assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        }
    }

    @Nested
    @DisplayName("Tests sur update()")
    class UpdateTests {

        @Test
        @DisplayName("update appelle executionProductionService")
        void update_appelleExecutionProductionService() {
            ExecutionProductionUpdateDTO dto = new ExecutionProductionUpdateDTO();
            dto.setStatut("TERMINE");

            when(executionProductionService.update(eq(1L), any())).thenReturn(buildExecutionDTO());

            executionProductionController.update(1L, dto);

            verify(executionProductionService, times(1)).update(eq(1L), any());
        }
    }

    @Nested
    @DisplayName("Tests sur saveValeursReelles()")
    class SaveValeursReellesTests {

        @Test
        @DisplayName("saveValeursReelles appelle executionProductionService")
        void saveValeursReelles_appelleExecutionProductionService() {
            dto.ValeurReelleParametreDTO dto = new dto.ValeurReelleParametreDTO();
            dto.setValeurReelle(30.0);

            executionProductionController.saveValeursReelles(1L, List.of(dto));

            verify(executionProductionService, times(1)).saveValeursReelles(eq(1L), any());
        }
    }

    @Nested
    @DisplayName("Tests sur predictOnStart()")
    class PredictOnStartTests {

        @Test
        @DisplayName("predictOnStart appelle predictionService")
        void predictOnStart_appellePredictionService() {
            ExecutionPredictionStartDTO dto = new ExecutionPredictionStartDTO();
            dto.setRegion("Nord");

            when(predictionService.predictOnStart(eq(1L), any())).thenReturn(buildPredictionDTO());

            executionProductionController.predictOnStart(1L, dto);

            verify(predictionService, times(1)).predictOnStart(eq(1L), any());
        }

        @Test
        @DisplayName("predictOnStart retourne prediction DTO")
        void predictOnStart_retournePredictionDTO() {
            ExecutionPredictionStartDTO dto = new ExecutionPredictionStartDTO();
            dto.setRegion("Nord");

            when(predictionService.predictOnStart(eq(1L), any())).thenReturn(buildPredictionDTO());

            var result = executionProductionController.predictOnStart(1L, dto);

            assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        }
    }

    @Nested
    @DisplayName("Tests sur buildCodeLot()")
    class BuildCodeLotTests {

        @Test
        @DisplayName("buildCodeLot appelle executionProductionService")
        void buildCodeLot_appelleExecutionProductionService() {
            executionProductionController.buildCodeLot(1L);

            verify(executionProductionService, times(1)).buildCodeLotForLot(1L);
        }
    }
}
