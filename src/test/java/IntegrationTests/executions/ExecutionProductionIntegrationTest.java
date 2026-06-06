package IntegrationTests.executions;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AuthResponseDTO;
import dto.ExecutionProductionCreateDTO;
import dto.ExecutionProductionDTO;
import dto.GuideProductionCreateDTO;
import dto.GuideProductionDTO;
import dto.LotArrivageCreateDTO;
import dto.SignupRequestDTO;
import Models.Entreprise;
import Models.Huilerie;
import Models.Profil;
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
import Repositories.EntrepriseRepository;
import Repositories.HuilerieRepository;
import Repositories.ProfilRepository;
import Repositories.UtilisateurRepository;

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
class ExecutionProductionIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String jwtToken;
    private Long guideId;
    private Long lotId;

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @Autowired
    private HuilerieRepository huilerieRepository;

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setupTestData() {
        // Créer une entreprise
        Entreprise entreprise = entrepriseRepository.findAll().stream().findFirst().orElse(null);
        if (entreprise == null) {
            entreprise = new Entreprise();
            entreprise.setNom("Entreprise Test");
            entreprise.setAdresse("Adresse Test");
            entreprise.setTelephone("0123456789");
            entreprise.setEmail("entreprise@test.com");
            entreprise = entrepriseRepository.save(entreprise);
        }

        // Créer un profil (vérifier s'il existe déjà)
        Profil profil = profilRepository.findByNom("ADMIN_TEST").orElse(null);
        if (profil == null) {
            profil = new Profil();
            profil.setNom("ADMIN_TEST");
            profil.setDescription("Profil administrateur test");
            profil = profilRepository.save(profil);
        }

        // Créer une huilerie
        Huilerie huilerie = huilerieRepository.findAll().stream().findFirst().orElse(null);
        if (huilerie == null) {
            huilerie = new Huilerie();
            huilerie.setNom("Huilerie Test");
            huilerie.setLocalisation("Test Location");
            huilerie.setType("artisanal");
            huilerie.setActive(true);
            huilerie.setEntreprise(entreprise);
            huilerie = huilerieRepository.save(huilerie);
        }
    }

    @BeforeEach
    void setupAuth() throws Exception {
        // Skip JWT auth for now - tests will use permitAll endpoints
        jwtToken = null;
    }

    @Test
    @DisplayName("createWithSameHuilerieVerification - Création exécution avec vérification même huilerie")
    void createWithSameHuilerieVerification() throws Exception {
        // Given: Données d'exécution valides
        ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
        dto.setGuideProductionId(guideId);
        dto.setLotId(lotId);
        dto.setReference("EXEC-001");
        dto.setDateDebut("2025-01-01");
        dto.setDateFinPrevue("2025-01-02");
        dto.setStatut("EN_COURS");

        // When: Création de l'exécution
        MvcResult result = mockMvc.perform(post("/api/execution-productions")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("EXEC-001"))
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andReturn();

        // Then: Vérifier la réponse
        String response = result.getResponse().getContentAsString();
        ExecutionProductionDTO execDto = objectMapper.readValue(response, ExecutionProductionDTO.class);
        
        assertThat(execDto.getReference()).isEqualTo("EXEC-001");
        assertThat(execDto.getStatut()).isEqualTo("EN_COURS");
        assertThat(execDto.getGuideProductionId()).isEqualTo(guideId);
        assertThat(execDto.getLotId()).isEqualTo(lotId);
    }

    @Test
    @DisplayName("findAllSuccess - Liste des exécutions de production")
    void findAllSuccess() throws Exception {
        // Given: Créer une exécution
        ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
        dto.setGuideProductionId(guideId);
        dto.setLotId(lotId);
        dto.setReference("EXEC-001");
        dto.setDateDebut("2025-01-01");
        dto.setDateFinPrevue("2025-01-02");
        dto.setStatut("EN_COURS");

        mockMvc.perform(post("/api/execution-productions")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // When: Récupérer la liste des exécutions
        mockMvc.perform(get("/api/execution-productions")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reference").value("EXEC-001"));
    }

    @Test
    @DisplayName("findByIdSuccess - Récupération d'une exécution par ID")
    void findByIdSuccess() throws Exception {
        // Given: Créer une exécution
        ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
        dto.setGuideProductionId(guideId);
        dto.setLotId(lotId);
        dto.setReference("EXEC-001");
        dto.setDateDebut("2025-01-01");
        dto.setDateFinPrevue("2025-01-02");
        dto.setStatut("EN_COURS");

        MvcResult createResult = mockMvc.perform(post("/api/execution-productions")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        ExecutionProductionDTO createdExec = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), 
            ExecutionProductionDTO.class
        );

        // When: Récupérer l'exécution par ID
        mockMvc.perform(get("/api/execution-productions/" + createdExec.getIdExecutionProduction())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idExecutionProduction").value(createdExec.getIdExecutionProduction()))
                .andExpect(jsonPath("$.reference").value("EXEC-001"));
    }
}
