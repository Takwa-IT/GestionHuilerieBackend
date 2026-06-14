package IntegrationTests.stock;

import IntegrationTests.config.AbstractIntegrationTest;
import dto.LotArrivageCreateDTO;
import dto.StockDTO;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StockIntegrationTest extends AbstractIntegrationTest {

    private void createLot(String variete, double pesee) throws Exception {
        LotArrivageCreateDTO dto = new LotArrivageCreateDTO();
        dto.setFournisseurId(this.fournisseurId);
        dto.setVariete(variete);
        dto.setMatierePremiereReference(this.matierePremiereReference);
        dto.setCampagneReference(this.campagneReference);
        dto.setHuilerieId(this.huilerieId);
        dto.setPesee(pesee);
        dto.setDateReception("2025-01-01");

        var request = post("/api/lots/arrivages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));
        
        if (jwtToken != null) {
            request.header("Authorization", "Bearer " + jwtToken);
        }

        mockMvc.perform(request)
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("findAll - stocks agrégés par variété")
    void findAll_stocksAgregesParVariete() throws Exception {
        createLot("Arbequina", 100.0);
        createLot("arbequina", 50.0);

        var getRequest = get("/api/stocks");
        if (jwtToken != null) {
            getRequest.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        StockDTO[] stocks = objectMapper.readValue(
            result.getResponse().getContentAsString(), StockDTO[].class);

        long arbequinaCount = java.util.Arrays.stream(stocks)
            .filter(s -> "arbequina".equalsIgnoreCase(s.getVariete()))
            .count();
        assertThat(arbequinaCount).isEqualTo(1);
    }

    @Test
    @DisplayName("findAll - quantités sommées pour même variété")
    void findAll_quantitesSommees_memVariete() throws Exception {
        createLot("Koroneiki", 100.0);
        createLot("KORONEIKI", 50.0);

        var getRequest = get("/api/stocks");
        if (jwtToken != null) {
            getRequest.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andReturn();

        StockDTO[] stocks = objectMapper.readValue(
            result.getResponse().getContentAsString(), StockDTO[].class);

        StockDTO koroneiki = java.util.Arrays.stream(stocks)
            .filter(s -> "koroneiki".equalsIgnoreCase(s.getVariete()))
            .findFirst().orElseThrow();
        assertThat(koroneiki.getQuantiteDisponible()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("findAll - stocks différentes variétés séparés")
    void findAll_stocksDifferentesVarietesSepares() throws Exception {
        createLot("Chemlali", 100.0);
        createLot("Chetoui", 80.0);

        var getRequest = get("/api/stocks");
        if (jwtToken != null) {
            getRequest.header("Authorization", "Bearer " + jwtToken);
        }

        MvcResult result = mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andReturn();

        StockDTO[] stocks = objectMapper.readValue(
            result.getResponse().getContentAsString(), StockDTO[].class);
        assertThat(stocks.length).isGreaterThanOrEqualTo(2);
    }
}
