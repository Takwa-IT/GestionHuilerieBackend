package IntegrationTests.predictions;

import IntegrationTests.config.AbstractIntegrationTest;
import dto.ExecutionPredictionStartDTO;
import dto.GuideProductionCreateDTO;
import dto.LotArrivageCreateDTO;
import dto.PredictionDTO;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests end-to-end avec le VRAI service Python de prédiction
 * Le service Python doit être démarré sur le port 7500 avant d'exécuter ces tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test-e2e")
class PredictionE2ETest extends AbstractIntegrationTest {

    private Long createGuide(String typeMachine) throws Exception {
        GuideProductionCreateDTO dto = new GuideProductionCreateDTO();
        dto.setNom("Guide " + typeMachine);
        dto.setTypeMachine(typeMachine);
        dto.setHuilerieId(this.huilerieId);
        dto.setDateCreation("2025-01-01");

        var request = MockMvcRequestBuilders.post("/api/guide-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", "Bearer " + jwtToken);

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        dto.GuideProductionDTO guide = objectMapper.readValue(
            result.getResponse().getContentAsString(), dto.GuideProductionDTO.class);
        return guide.getIdGuideProduction();
    }

    private Long createLot() throws Exception {
        LotArrivageCreateDTO dto = new LotArrivageCreateDTO();
        dto.setFournisseurId(this.fournisseurId);
        dto.setVariete("Chemlali");
        dto.setMatierePremiereReference(this.matierePremiereReference);
        dto.setCampagneReference(this.campagneReference);
        dto.setHuilerieId(this.huilerieId);
        dto.setPesee(500.0);
        dto.setDateReception("2025-01-01");
        dto.setRegion("Nord");
        dto.setMethodeRecolte("manuelle");
        dto.setTypeSol("argileux");
        dto.setLavageEffectue("oui");
        dto.setHumiditePourcent(20.0);
        dto.setAciditeOlivesPourcent(1.0);
        dto.setTauxFeuillesPourcent(2.0);
        dto.setMaturite("3");
        dto.setDureeStockageAvantBroyage(2);
        dto.setTempsDepuisRecolteHeures(10);

        var request = MockMvcRequestBuilders.post("/api/lots/arrivages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", "Bearer " + jwtToken);

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        dto.LotOlivesDTO lot = objectMapper.readValue(
            result.getResponse().getContentAsString(), dto.LotOlivesDTO.class);
        return lot.getIdLot();
    }

    private Long createExecution(Long guideId, Long lotId) throws Exception {
        dto.ExecutionProductionCreateDTO dto = new dto.ExecutionProductionCreateDTO();
        dto.setGuideProductionId(guideId);
        dto.setLotId(lotId);
        dto.setReference("EXEC-E2E-001");
        dto.setDateDebut("2025-01-01");
        dto.setDateFinPrevue("2025-01-02");
        dto.setStatut("EN_COURS");
        dto.setControleTemperature(true);

        var request = MockMvcRequestBuilders.post("/api/execution-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", "Bearer " + jwtToken);

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        dto.ExecutionProductionDTO exec = objectMapper.readValue(
            result.getResponse().getContentAsString(), dto.ExecutionProductionDTO.class);
        return exec.getIdExecutionProduction();
    }

    @Test
    @Order(1)
    @DisplayName("E2E - predictOnStart avec vrai service Python - appel complet")
    void predictOnStart_appelComplet_retournePrediction() throws Exception {
        // Given: Créer guide, lot et exécution
        Long guideId = createGuide("2_phase");
        Long lotId = createLot();
        Long executionId = createExecution(guideId, lotId);

        // When: Appeler le vrai service Python via predictOnStart
        var request = MockMvcRequestBuilders.post("/api/predictions/predict-on-start/" + executionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken);

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rendementPreditPourcent").exists())
                .andExpect(jsonPath("$.qualitePredite").exists())
                .andExpect(jsonPath("$.probabiliteQualite").exists())
                .andReturn();

        // Then: Vérifier que la prédiction a été créée avec les données du service Python
        PredictionDTO prediction = objectMapper.readValue(
            result.getResponse().getContentAsString(), PredictionDTO.class);
        
        assertThat(prediction.getRendementPreditPourcent()).isNotNull();
        assertThat(prediction.getQualitePredite()).isNotNull();
        assertThat(prediction.getProbabiliteQualite()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("E2E - predictOnStart avec overrides - appel service Python")
    void predictOnStart_avecOverrides_retournePrediction() throws Exception {
        // Given: Créer guide, lot et exécution
        Long guideId = createGuide("3_phase");
        Long lotId = createLot();
        Long executionId = createExecution(guideId, lotId);

        // When: Appeler avec overrides de paramètres (valeurs dans les limites du service Python)
        ExecutionPredictionStartDTO overrides = new ExecutionPredictionStartDTO();
        overrides.setTemperatureMalaxageC(25.0);  // Max 27
        overrides.setDureeMalaxageMin(35.0);       // Max 40
        overrides.setVitesseDecanteurTrMin(3300.0); // Max 3400

        var request = MockMvcRequestBuilders.post("/api/predictions/predict-on-start/" + executionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overrides))
                .header("Authorization", "Bearer " + jwtToken);

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        // Then: Vérifier que la prédiction a été créée
        PredictionDTO prediction = objectMapper.readValue(
            result.getResponse().getContentAsString(), PredictionDTO.class);
        
        assertThat(prediction.getRendementPreditPourcent()).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("E2E - findAll avec vrai service Python - liste des prédictions")
    void findAll_listePredictions() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/predictions")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
