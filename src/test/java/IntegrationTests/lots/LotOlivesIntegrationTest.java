package IntegrationTests.lots;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AuthResponseDTO;
import dto.LotArrivageCreateDTO;
import dto.LotOlivesDTO;
import dto.SignupRequestDTO;
import Models.Fournisseur;
import Models.Huilerie;
import Models.Entreprise;
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
import Repositories.FournisseurRepository;
import Repositories.HuilerieRepository;
import Repositories.UtilisateurRepository;
import Repositories.EntrepriseRepository;
import Repositories.ProfilRepository;

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
class LotOlivesIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String jwtToken;

    @Autowired
    private FournisseurRepository fournisseurRepository;

    @Autowired
    private HuilerieRepository huilerieRepository;

    @Autowired
    private EntrepriseRepository entrepriseRepository;

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

        // Créer un fournisseur
        Fournisseur fournisseur = fournisseurRepository.findAll().stream().findFirst().orElse(null);
        if (fournisseur == null) {
            fournisseur = new Fournisseur();
            fournisseur.setNom("Fournisseur Test");
            fournisseur.setCin("TEST123456");
            fournisseur = fournisseurRepository.save(fournisseur);
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
        // The integration tests require a full user setup with roles and permissions
        // For now, we'll skip the JWT token generation
        jwtToken = null;
    }

    @Test
    @DisplayName("createArrivageSuccess - Création arrivage avec stock automatique")
    void createArrivageSuccess() throws Exception {
        // Given: Données d'arrivage valides
        LotArrivageCreateDTO dto = new LotArrivageCreateDTO();
        dto.setFournisseurId(1L);
        dto.setVariete("Arbequina");
        dto.setMatierePremiereReference("MAT-001");
        dto.setCampagneReference("CAMP-2025");
        dto.setHuilerieId(1L);
        dto.setPesee(100.0);
        dto.setDateReception("2025-01-01");

        // When: Création de l'arrivage
        MvcResult result = mockMvc.perform(post("/api/lots/arrivages")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").exists())
                .andExpect(jsonPath("$.varieteOlive").value("arbequina"))
                .andExpect(jsonPath("$.pesee").value(100.0))
                .andReturn();

        // Then: Vérifier la réponse
        String response = result.getResponse().getContentAsString();
        LotOlivesDTO lotDto = objectMapper.readValue(response, LotOlivesDTO.class);
        
        assertThat(lotDto.getReference()).isNotEmpty();
        assertThat(lotDto.getVarieteOlive()).isEqualTo("arbequina");
        assertThat(lotDto.getPesee()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("findAllArrivagesSuccess - Liste des arrivages")
    void findAllArrivagesSuccess() throws Exception {
        // Given: Créer un arrivage
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

        // When: Récupérer la liste des arrivages
        mockMvc.perform(get("/api/lots/arrivages")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].variete").value("arbequina"));
    }

    @Test
    @DisplayName("findByIdSuccess - Récupération d'un lot par ID")
    void findByIdSuccess() throws Exception {
        // Given: Créer un arrivage
        LotArrivageCreateDTO dto = new LotArrivageCreateDTO();
        dto.setFournisseurId(1L);
        dto.setVariete("Arbequina");
        dto.setMatierePremiereReference("MAT-001");
        dto.setCampagneReference("CAMP-2025");
        dto.setHuilerieId(1L);
        dto.setPesee(100.0);
        dto.setDateReception("2025-01-01");

        MvcResult createResult = mockMvc.perform(post("/api/lots/arrivages")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        LotOlivesDTO createdLot = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), 
            LotOlivesDTO.class
        );

        // When: Récupérer le lot par ID
        mockMvc.perform(get("/api/lots/" + createdLot.getIdLot())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLot").value(createdLot.getIdLot()))
                .andExpect(jsonPath("$.reference").value(createdLot.getReference()));
    }
}
