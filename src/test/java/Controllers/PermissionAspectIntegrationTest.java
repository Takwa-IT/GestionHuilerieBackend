package Controllers;

import Models.Huilerie;
import Models.Module;
import Models.Permission;
import Models.Profil;
import Models.StatutUtilisateur;
import Models.Utilisateur;
import Repositories.HuilerieRepository;
import Repositories.ModuleRepository;
import Repositories.PermissionRepository;
import Repositories.ProfilRepository;
import Repositories.UtilisateurRepository;
import Services.JwtService;
import org.example.gestionhuilerieback.GestionHuilerieBackApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GestionHuilerieBackApplication.class)
@Transactional
class PermissionAspectIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private HuilerieRepository huilerieRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Module lotsTraceabiliteModule;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        lotsTraceabiliteModule = moduleRepository.findAll().stream()
                .filter(module -> "LOTS_TRAÇABILITE".equals(module.getNom()))
                .findFirst()
                .orElseGet(() -> {
                    Module module = new Module();
                    module.setNom("LOTS_TRAÇABILITE");
                    return moduleRepository.save(module);
                });
    }

    @Test
    void shouldReturn200WhenUserHasReadPermission() throws Exception {
        String token = buildTokenForUser(true);

        mockMvc.perform(get("/api/lots")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403WhenUserHasNoReadPermission() throws Exception {
        String token = buildTokenForUser(false);

        mockMvc.perform(get("/api/lots")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Permission insuffisante"))
                .andExpect(jsonPath("$.module").value("LOTS_TRAÇABILITE"))
                .andExpect(jsonPath("$.action").value("READ"));
    }

    @Test
    void shouldReturn403WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/lots"))
                .andExpect(status().isForbidden());
    }

    private String buildTokenForUser(boolean canRead) {
        String suffix = String.valueOf(System.nanoTime());

        Profil profil = new Profil();
        profil.setNom("PROFIL_PERM_" + suffix);
        profil.setDescription("Profil test permissions");
        profil = profilRepository.save(profil);

        Huilerie huilerie = new Huilerie();
        huilerie.setNom("HUILERIE_PERM_" + suffix);
        huilerie = huilerieRepository.save(huilerie);

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom("User");
        utilisateur.setPrenom("Permission");
        utilisateur.setEmail("perm." + suffix + "@test.com");
        utilisateur.setMotDePasse("encoded");
        utilisateur.setTelephone("0600000000");
        utilisateur.setActif(StatutUtilisateur.ACTIF);
        utilisateur.setProfil(profil);
        utilisateur.setHuilerie(huilerie);
        utilisateur = utilisateurRepository.save(utilisateur);

        if (canRead) {
            Permission permission = new Permission();
            permission.setProfil(profil);
            permission.setModule(lotsTraceabiliteModule);
            permission.setCanCreate(false);
            permission.setCanRead(true);
            permission.setCanUpdate(false);
            permission.setCanDelete(false);
            permission.setCanExecuted(false);
            permissionRepository.save(permission);
        }

        return jwtService.generateToken(utilisateur);
    }
}

