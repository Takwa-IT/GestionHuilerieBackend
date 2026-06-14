package IntegrationTests.lots;

import IntegrationTests.config.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.LotArrivageCreateDTO;
import dto.LotOlivesDTO;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LotOlivesIntegrationTest extends AbstractIntegrationTest {

    private LotArrivageCreateDTO buildLotDTO(String variete) {
        LotArrivageCreateDTO dto = new LotArrivageCreateDTO();
        dto.setFournisseurId(this.fournisseurId);
        dto.setVariete(variete);
        dto.setMatierePremiereReference(this.matierePremiereReference);
        dto.setCampagneReference(this.campagneReference);
        dto.setHuilerieId(this.huilerieId);
        dto.setPesee(100.0);
        dto.setDateReception("2025-01-01");
        return dto;
    }

    @Test
    @DisplayName("createArrivage - stock créé automatiquement")
    void createArrivage_stockCreeAutomatiquement() throws Exception {
        LotArrivageCreateDTO dto = buildLotDTO("Arbequina");

        var request = post("/api/lots/arrivages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").exists())
                .andReturn();
    }

    @Test
    @DisplayName("createArrivage - variété normalisée en minuscules")
    void createArrivage_varieteNormaliseeEnMinuscules() throws Exception {
        LotArrivageCreateDTO dto = buildLotDTO("KORONEIKI");

        var request = post("/api/lots/arrivages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        LotOlivesDTO lotDto = objectMapper.readValue(
            result.getResponse().getContentAsString(), LotOlivesDTO.class);
        assertThat(lotDto.getVarieteOlive()).isEqualTo("KORONEIKI");
    }

    @Test
    @DisplayName("findAll - liste des arrivages")
    void findAll_listeLots() throws Exception {
        var request = post("/api/lots/arrivages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildLotDTO("Arbequina")));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var getRequest = get("/api/lots");
        if (jwtToken != null) {
            getRequest.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("findById - récupération lot par ID")
    void findById_retourneLot() throws Exception {
        var request = post("/api/lots/arrivages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildLotDTO("Chemlali")));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult createResult = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        LotOlivesDTO created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), LotOlivesDTO.class);

        var getRequest = get("/api/lots/" + created.getIdLot());
        if (jwtToken != null) {
            getRequest.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLot").value(created.getIdLot()));
    }
}
