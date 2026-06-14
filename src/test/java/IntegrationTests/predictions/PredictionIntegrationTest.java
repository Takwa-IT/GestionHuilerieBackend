package IntegrationTests.predictions;

import IntegrationTests.config.AbstractIntegrationTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dto.PredictionInputDTO;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PredictionIntegrationTest extends AbstractIntegrationTest {

    private static WireMockServer wireMock;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(8089);
        wireMock.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void setupWireMockStubs() {
        wireMock.resetAll();
        wireMock.stubFor(post(urlEqualTo("/predict"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"rendement\": 22.5, \"qualite\": \"HAUTE\", " +
                          "\"confidence\": 0.87}")));
    }

    private PredictionInputDTO buildValidInput() {
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
    @DisplayName("validateInput - données valides retourne 200")
    void validateInput_donneesValides_retourne200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/predictions/validate-input")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidInput())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("validateInput - variété invalide retourne erreur")
    void validateInput_varieteInvalide_retourneErreur() throws Exception {
        PredictionInputDTO dto = buildValidInput();
        dto.setVariete("VarieteInconnue");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/predictions/validate-input")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("validateInput - région normalisée automatiquement")
    void validateInput_regionNormaliseeAutomatiquement() throws Exception {
        PredictionInputDTO dto = buildValidInput();
        dto.setRegion("Sfax");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/predictions/validate-input")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("findAll - liste des prédictions")
    void findAll_listePredictions() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/predictions")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
