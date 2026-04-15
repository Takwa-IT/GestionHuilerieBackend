package Controllers;

import Models.Huilerie;
import Models.Module;
import Models.Profil;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GestionHuilerieBackApplication.class)
@Transactional
class AdminPermissionIntegrationTest {

    private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private HuilerieRepository huilerieRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private String adminToken;
    private Profil adminProfil;
    private Profil targetProfil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        String suffix = String.valueOf(System.nanoTime());

        adminProfil = profilRepository.findByNom("ADMIN").orElseGet(() -> {
          Profil profil = new Profil();
          profil.setNom("ADMIN");
          profil.setDescription("Profil admin");
          return profilRepository.save(profil);
        });

        targetProfil = new Profil();
        targetProfil.setNom("PROFIL_TARGET_" + suffix);
        targetProfil.setDescription("Profil cible test permissions");
        targetProfil = profilRepository.save(targetProfil);

        Huilerie huilerie = new Huilerie();
        huilerie.setNom("HUILERIE_TEST_" + suffix);
        huilerie = huilerieRepository.save(huilerie);

        Utilisateur admin = new Utilisateur();
        admin.setNom("Admin");
        admin.setPrenom("Test");
        admin.setEmail("admin." + suffix + "@test.com");
        admin.setMotDePasse("encoded");
        admin.setTelephone("0000000000");
        admin.setProfil(adminProfil);
        admin.setHuilerie(huilerie);
        admin = utilisateurRepository.save(admin);

        adminToken = jwtService.generateToken(admin);
    }

    @Test
    void getPermissionsByProfil_shouldReturnAllModules() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        moduleRepository.save(buildModule("LOT_" + suffix));
        moduleRepository.save(buildModule("STOCK_" + suffix));

        mockMvc.perform(get("/api/admin/permissions/profil/{profilId}", targetProfil.getIdProfil())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void bulkUpsert_shouldCreateThreePermissions() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        Module m1 = moduleRepository.save(buildModule("MODULE_A_" + suffix));
        Module m2 = moduleRepository.save(buildModule("MODULE_B_" + suffix));
        Module m3 = moduleRepository.save(buildModule("MODULE_C_" + suffix));

        String payload = """
                {
                  "profilId": %d,
                  "permissions": [
                    {"moduleId": %d, "canCreate": true, "canRead": true, "canUpdate": false, "canDelete": false, "canExecuted": false},
                    {"moduleId": %d, "canCreate": false, "canRead": true, "canUpdate": true, "canDelete": false, "canExecuted": false},
                    {"moduleId": %d, "canCreate": false, "canRead": true, "canUpdate": false, "canDelete": true, "canExecuted": true}
                  ]
                }
                """.formatted(targetProfil.getIdProfil(), m1.getIdModule(), m2.getIdModule(), m3.getIdModule());

        mockMvc.perform(post("/api/admin/permissions/bulk")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(permissionRepository.findByProfilIdWithModule(targetProfil.getIdProfil())).hasSize(3);
    }

    @Test
    void bulkUpsert_shouldRollbackWhenModuleInvalid() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        Module validModule = moduleRepository.save(buildModule("VALID_" + suffix));

        String payload = """
                {
                  "profilId": %d,
                  "permissions": [
                    {"moduleId": %d, "canCreate": true, "canRead": true, "canUpdate": false, "canDelete": false, "canExecuted": false},
                    {"moduleId": 999999999, "canCreate": false, "canRead": true, "canUpdate": false, "canDelete": false, "canExecuted": false}
                  ]
                }
                """.formatted(targetProfil.getIdProfil(), validModule.getIdModule());

        mockMvc.perform(post("/api/admin/permissions/bulk")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        assertThat(permissionRepository.findByProfilIdWithModule(targetProfil.getIdProfil())).isEmpty();
    }

    private Module buildModule(String nom) {
        Module module = new Module();
        module.setNom(nom);
        return module;
    }
}

