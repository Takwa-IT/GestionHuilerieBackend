package IntegrationTests.executions;

import IntegrationTests.config.AbstractIntegrationTest;
import dto.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExecutionProductionIntegrationTest extends AbstractIntegrationTest {

    private Long createGuide(String typeMachine) throws Exception {
        GuideProductionCreateDTO dto = new GuideProductionCreateDTO();
        dto.setNom("Guide " + typeMachine);
        dto.setTypeMachine(typeMachine);
        dto.setHuilerieId(this.huilerieId);
        dto.setDateCreation("2025-01-01");

        var request = post("/api/guide-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        GuideProductionDTO guide = objectMapper.readValue(
            result.getResponse().getContentAsString(), GuideProductionDTO.class);
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

        var request = post("/api/lots/arrivages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        LotOlivesDTO lot = objectMapper.readValue(
            result.getResponse().getContentAsString(), LotOlivesDTO.class);
        return lot.getIdLot();
    }

    @Test
    @DisplayName("create - exécution avec guide et lot même huilerie")
    void create_executionAvecMemehuilerie() throws Exception {
        Long guideId = createGuide("3_phase");
        Long lotId = createLot();

        ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
        dto.setGuideProductionId(guideId);
        dto.setLotId(lotId);
        dto.setReference("EXEC-IT-001");
        dto.setDateDebut("2025-01-01");
        dto.setDateFinPrevue("2025-01-02");
        dto.setStatut("EN_COURS");

        var request = post("/api/execution-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andReturn();

        ExecutionProductionDTO exec = objectMapper.readValue(
            result.getResponse().getContentAsString(), ExecutionProductionDTO.class);
        assertThat(exec.getReference()).isNotEmpty();
    }

    @Test
    @DisplayName("create - référence unique générée si doublon")
    void create_referenceUniqueGeneree_siDoublon() throws Exception {
        Long guideId = createGuide("2_phase");
        Long lotId = createLot();

        ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
        dto.setGuideProductionId(guideId);
        dto.setLotId(lotId);
        dto.setReference("EXEC-DOUBLON");
        dto.setDateDebut("2025-01-01");
        dto.setDateFinPrevue("2025-01-02");
        dto.setStatut("EN_COURS");

        var request = post("/api/execution-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        // Create a new lot for the second execution to avoid quantity issues
        Long lotId2 = createLot();
        dto.setLotId(lotId2);

        var request2 = post("/api/execution-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));
        
        if (jwtToken != null) {
            request2.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result2 = mockMvc.perform(request2)
                .andExpect(status().isCreated())
                .andReturn();

        ExecutionProductionDTO exec2 = objectMapper.readValue(
            result2.getResponse().getContentAsString(), ExecutionProductionDTO.class);
        assertThat(exec2.getReference()).isNotEqualTo("EXEC-DOUBLON");
        assertThat(exec2.getReference()).contains("EXEC-DOUBLON");
    }

    @Test
    @DisplayName("findAll - liste des exécutions")
    void findAll_listeExecutions() throws Exception {
        var getRequest = get("/api/execution-productions");
        if (jwtToken != null) {
            getRequest.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
