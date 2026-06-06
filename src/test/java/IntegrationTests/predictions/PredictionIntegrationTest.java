package IntegrationTests.predictions;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AuthResponseDTO;
import dto.PredictionCreateDTO;
import dto.PredictionDTO;
import dto.PredictionInputDTO;
import dto.SignupRequestDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = org.example.gestionhuilerieback.GestionHuilerieBackApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@Transactional
class PredictionIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setupAuth() throws Exception {
        // Créer un utilisateur pour les tests
        SignupRequestDTO signupRequest = new SignupRequestDTO();
        signupRequest.setEmail("admin@example.com");
        signupRequest.setMotDePasse("admin123");
        signupRequest.setNom("Admin");
        signupRequest.setPrenom("User");

        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDTO authResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(), 
            AuthResponseDTO.class
        );
        jwtToken = authResponse.getToken();
    }

    @Test
    @DisplayName("validateInputSuccess - Validation des données de prédiction")
    void validateInputSuccess() throws Exception {
        // Given: Données de prédiction valides
        PredictionInputDTO dto = new PredictionInputDTO();
        dto.setVariete("Arbequina");
        dto.setRegion("Nord");
        dto.setTypeMachine("3_phase");
        dto.setHumiditePourcent(60.0);

        // When: Validation des données
        mockMvc.perform(post("/api/predictions/validate-input")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Données de prédiction valides"));
    }

    @Test
    @DisplayName("validateInputFailure - Données de prédiction invalides")
    void validateInputFailure() throws Exception {
        // Given: Données de prédiction invalides (variete vide)
        PredictionInputDTO dto = new PredictionInputDTO();
        dto.setVariete("");
        dto.setRegion("Nord");
        dto.setTypeMachine("3_phase");

        // When: Validation des données
        mockMvc.perform(post("/api/predictions/validate-input")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Données de prédiction invalides"));
    }

    @Test
    @DisplayName("createPredictionSuccess - Création prédiction avec données valides")
    void createPredictionSuccess() throws Exception {
        // Given: Données de prédiction valides
        PredictionCreateDTO dto = new PredictionCreateDTO();
        dto.setModePrediction("no_lab");
        dto.setRendementPreditPourcent(22.5);
        dto.setQualitePredite("HAUTE");
        dto.setExecutionProductionId(1L);

        // When: Création de la prédiction
        MvcResult result = mockMvc.perform(post("/api/predictions")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPrediction").exists())
                .andReturn();

        // Then: Vérifier la réponse
        String response = result.getResponse().getContentAsString();
        PredictionDTO predictionDto = objectMapper.readValue(response, PredictionDTO.class);
        
        assertThat(predictionDto.getIdPrediction()).isNotNull();
        assertThat(predictionDto.getModePrediction()).isEqualTo("no_lab");
    }

    @Test
    @DisplayName("findAllSuccess - Liste des prédictions")
    void findAllSuccess() throws Exception {
        // Given: Créer une prédiction
        PredictionCreateDTO dto = new PredictionCreateDTO();
        dto.setModePrediction("no_lab");
        dto.setRendementPreditPourcent(22.5);
        dto.setQualitePredite("HAUTE");
        dto.setExecutionProductionId(1L);

        mockMvc.perform(post("/api/predictions")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // When: Récupérer la liste des prédictions
        mockMvc.perform(get("/api/predictions")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].modePrediction").value("no_lab"));
    }
}
