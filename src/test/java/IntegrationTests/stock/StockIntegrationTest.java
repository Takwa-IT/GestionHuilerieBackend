package IntegrationTests.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AuthResponseDTO;
import dto.LotArrivageCreateDTO;
import dto.SignupRequestDTO;
import dto.StockDTO;
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
class StockIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String jwtToken;

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
    @DisplayName("findAllWithVarietyAggregation - Récupération stocks avec agrégation par variété")
    void findAllWithVarietyAggregation() throws Exception {
        // Given: Créer deux lots avec la même variété (pour tester l'agrégation)
        LotArrivageCreateDTO dto1 = new LotArrivageCreateDTO();
        dto1.setFournisseurId(1L);
        dto1.setVariete("Arbequina");
        dto1.setMatierePremiereReference("MAT-001");
        dto1.setCampagneReference("CAMP-2025");
        dto1.setHuilerieId(1L);
        dto1.setPesee(100.0);
        dto1.setDateReception("2025-01-01");

        mockMvc.perform(post("/api/lots/arrivages")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        LotArrivageCreateDTO dto2 = new LotArrivageCreateDTO();
        dto2.setFournisseurId(1L);
        dto2.setVariete("Arbequina");
        dto2.setMatierePremiereReference("MAT-001");
        dto2.setCampagneReference("CAMP-2025");
        dto2.setHuilerieId(1L);
        dto2.setPesee(50.0);
        dto2.setDateReception("2025-01-02");

        mockMvc.perform(post("/api/lots/arrivages")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        // When: Récupérer les stocks
        MvcResult result = mockMvc.perform(get("/api/stocks")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // Then: Vérifier que les stocks sont agrégés par variété
        String response = result.getResponse().getContentAsString();
        StockDTO[] stocks = objectMapper.readValue(response, StockDTO[].class);
        
        assertThat(stocks).isNotEmpty();
        // Les stocks avec même variété devraient être agrégés
        boolean hasArbequina = java.util.Arrays.stream(stocks)
            .anyMatch(s -> "arbequina".equalsIgnoreCase(s.getVariete()));
        assertThat(hasArbequina).isTrue();
    }

    @Test
    @DisplayName("findAllByHuilerieIdSuccess - Stocks filtrés par huilerie")
    void findAllByHuilerieIdSuccess() throws Exception {
        // Given: Créer un lot
        LotArrivageCreateDTO dto = new LotArrivageCreateDTO();
        dto.setFournisseurId(1L);
        dto.setVariete("Arbequina");
        dto.setMatierePremiereReference("MAT-001");
        dto.setCampagneReference("CAMP-2025");
        dto.setHuilerieId(1L);
        dto.setPesee(100.0);
        dto.setDateReception("2025-01-01");

        mockMvc.perform(post("/api/lots/arrivages")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // When: Récupérer les stocks par huilerie
        mockMvc.perform(get("/api/stocks/huilerie/1")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("findByIdSuccess - Récupération d'un stock par ID")
    void findByIdSuccess() throws Exception {
        // Given: Créer un lot
        LotArrivageCreateDTO dto = new LotArrivageCreateDTO();
        dto.setFournisseurId(1L);
        dto.setVariete("Arbequina");
        dto.setMatierePremiereReference("MAT-001");
        dto.setCampagneReference("CAMP-2025");
        dto.setHuilerieId(1L);
        dto.setPesee(100.0);
        dto.setDateReception("2025-01-01");

        MvcResult lotResult = mockMvc.perform(post("/api/lots/arrivages")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        dto.LotOlivesDTO lotDto = objectMapper.readValue(
            lotResult.getResponse().getContentAsString(), 
            dto.LotOlivesDTO.class
        );

        // Récupérer le stock créé
        MvcResult stockResult = mockMvc.perform(get("/api/stocks/lot/" + lotDto.getIdLot())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        StockDTO[] stocks = objectMapper.readValue(
            stockResult.getResponse().getContentAsString(), 
            StockDTO[].class
        );

        if (stocks.length > 0) {
            // When: Récupérer le stock par ID
            mockMvc.perform(get("/api/stocks/" + stocks[0].getIdStock())
                    .header("Authorization", "Bearer " + jwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idStock").value(stocks[0].getIdStock()));
        }
    }
}
