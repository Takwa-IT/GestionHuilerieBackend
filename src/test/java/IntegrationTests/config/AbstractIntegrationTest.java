package IntegrationTests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AuthResponseDTO;
import dto.LoginRequestDTO;
import dto.SignupRequestDTO;
import Models.CampagneOlives;
import Models.Entreprise;
import Models.Fournisseur;
import Models.Huilerie;
import Models.MatierePremiere;
import Models.Profil;
import Models.StatutUtilisateur;
import Models.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import Repositories.CampagneOlivesRepository;
import Repositories.EntrepriseRepository;
import Repositories.FournisseurRepository;
import Repositories.HuilerieRepository;
import Repositories.MatierePremiereRepository;
import Repositories.ProfilRepository;
import Repositories.UtilisateurRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = org.example.gestionhuilerieback.GestionHuilerieBackApplication.class
)
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    protected String jwtToken;
    protected Long huilerieId;
    protected Long fournisseurId;
    protected String matierePremiereReference;
    protected String campagneReference;

    // Repositories pour setup
    @Autowired
    private EntrepriseRepository entrepriseRepository;
    @Autowired
    private HuilerieRepository huilerieRepository;
    @Autowired
    private ProfilRepository profilRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private FournisseurRepository fournisseurRepository;
    @Autowired
    private MatierePremiereRepository matierePremiereRepository;
    @Autowired
    private CampagneOlivesRepository campagneOlivesRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void baseSetup() throws Exception {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
        objectMapper = new ObjectMapper();

        setupBaseData();
        jwtToken = obtainJwtToken();
    }

    private void setupBaseData() {
        // Entreprise
        Entreprise entreprise = new Entreprise();
        entreprise.setNom("Entreprise Test");
        entreprise.setAdresse("Adresse Test");
        entreprise.setTelephone("12345678");
        entreprise.setEmail("test@entreprise.com");
        entreprise = entrepriseRepository.save(entreprise);

        // Profil
        Profil profil = new Profil();
        profil.setNom("ADMIN_TEST");
        profil.setDescription("Profil administrateur test");
        profil = profilRepository.save(profil);

        // Huilerie
        Huilerie huilerie = new Huilerie();
        huilerie.setNom("Huilerie Test");
        huilerie.setLocalisation("Sfax");
        huilerie.setType("artisanal");
        huilerie.setActive(true);
        huilerie.setEntreprise(entreprise);
        huilerie = huilerieRepository.save(huilerie);
        this.huilerieId = huilerie.getIdHuilerie();

        // Fournisseur
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setNom("Fournisseur Test");
        fournisseur.setCin("12345678");
        fournisseur = fournisseurRepository.save(fournisseur);
        this.fournisseurId = fournisseur.getIdFournisseur();

        // MatierePremiere
        MatierePremiere matierePremiere = new MatierePremiere();
        matierePremiere.setReference("MP-TEST-001");
        matierePremiere.setNom("Olive Test");
        matierePremiere.setType("OLIVE");
        matierePremiere.setUniteMesure("KG");
        matierePremiere.setDescription("Matière première pour tests");
        matierePremiere.setHuilerie(huilerie);
        matierePremiere = matierePremiereRepository.save(matierePremiere);
        this.matierePremiereReference = matierePremiere.getReference();

        // CampagneOlives
        CampagneOlives campagne = new CampagneOlives();
        campagne.setReference("CP-TEST-001");
        campagne.setAnnee("2025");
        campagne.setDateDebut("2025-01-01");
        campagne.setDateFin("2025-12-31");
        campagne.setHuilerie(huilerie);
        campagne = campagneOlivesRepository.save(campagne);
        this.campagneReference = campagne.getReference();
    }

    private String obtainJwtToken() throws Exception {
        // Create user directly in database
        String email = "admin_test_" + System.nanoTime() + "@test.com";
        String password = "Admin123!";
        
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(passwordEncoder.encode(password));
        utilisateur.setNom("Admin");
        utilisateur.setPrenom("Test");
        utilisateur.setActif(StatutUtilisateur.ACTIF);
        utilisateur.setEmailVerified(true);
        utilisateur.setProfil(profilRepository.findByNom("ADMIN_TEST").orElse(null));
        utilisateur.setEntreprise(entrepriseRepository.findAll().stream().findFirst().orElse(null));
        utilisateur = utilisateurRepository.save(utilisateur);

        // Login
        LoginRequestDTO login = new LoginRequestDTO();
        login.setEmail(email);
        login.setMotDePasse(password);

        MvcResult result = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login))
        ).andReturn();

        AuthResponseDTO auth = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            AuthResponseDTO.class
        );
        return auth.getToken();
    }
}
