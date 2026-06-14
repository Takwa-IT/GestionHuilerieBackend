package IntegrationTests.guides;

import IntegrationTests.config.AbstractIntegrationTest;
import dto.GuideProductionCreateDTO;
import dto.GuideProductionDTO;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GuideProductionIntegrationTest extends AbstractIntegrationTest {

    private GuideProductionCreateDTO buildGuideDTO(String typeMachine) {
        GuideProductionCreateDTO dto = new GuideProductionCreateDTO();
        dto.setNom("Guide Test " + typeMachine);
        dto.setTypeMachine(typeMachine);
        dto.setHuilerieId(this.huilerieId);
        dto.setDateCreation("2025-01-01");
        return dto;
    }

    @Test
    @DisplayName("create - étapes template auto générées pour 3_phase")
    void create_etapesTemplateAutoGenerees_3phase() throws Exception {
        var request = post("/api/guide-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildGuideDTO("3_phase")));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etapes").isArray())
                .andReturn();

        GuideProductionDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), GuideProductionDTO.class);
        assertThat(dto.getEtapes()).hasSize(7);
        assertThat(dto.getEtapes().get(0).getCodeEtape()).isNotEmpty();
    }

    @Test
    @DisplayName("create - étapes template auto générées pour 2_phase")
    void create_etapesTemplateAutoGenerees_2phase() throws Exception {
        var request = post("/api/guide-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildGuideDTO("2_phase")));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        GuideProductionDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), GuideProductionDTO.class);
        assertThat(dto.getEtapes()).hasSize(6);
    }

    @Test
    @DisplayName("create - étapes template auto générées pour presse")
    void create_etapesTemplateAutoGenerees_presse() throws Exception {
        var request = post("/api/guide-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildGuideDTO("presse")));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        GuideProductionDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), GuideProductionDTO.class);
        assertThat(dto.getEtapes()).hasSize(6);
    }

    @Test
    @DisplayName("findAll - liste des guides")
    void findAll_listeGuides() throws Exception {
        var request = post("/api/guide-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildGuideDTO("3_phase")));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var getRequest = get("/api/guide-productions");
        if (jwtToken != null) {
            getRequest.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("findById - récupération guide par ID")
    void findById_retourneGuide() throws Exception {
        var request = post("/api/guide-productions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildGuideDTO("3_phase")));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult createResult = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        GuideProductionDTO created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), GuideProductionDTO.class);

        var getRequest = get("/api/guide-productions/" + created.getIdGuideProduction());
        if (jwtToken != null) {
            getRequest.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGuideProduction")
                    .value(created.getIdGuideProduction()));
    }
}
