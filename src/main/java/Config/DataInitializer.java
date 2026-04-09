package Config;

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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProfilRepository profilRepository;
    private final ModuleRepository moduleRepository;
    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final HuilerieRepository huilerieRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password:Admin123!}")
    private String adminPassword;

    @Value("${security.verification-email.expiration-hours:24}")
    private long verificationEmailExpirationHours;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Profil admin = findOrCreateProfil("ADMIN", "Acces total");
        Profil responsableProduction = findOrCreateProfil("RESPONSABLE_PRODUCTION", "Acces operations metier");

        Map<String, Module> modules = new LinkedHashMap<>();
        for (String moduleName : List.of(
                "DASHBOARD",
                "RECEPTION",
                "GUIDE_PRODUCTION",
                "MACHINES",
                "MATIERES_PREMIERES",
                "STOCK",
                "LOTS_TRAÇABILITE",
                "DASHBOARD_ADMIN",
                "HUILERIES",
                "STOCK_MOUVEMENT",
                "COMPTES_PROFILS")) {
            modules.put(moduleName, findOrCreateModule(moduleName));
        }

        for (Module module : modules.values()) {
            findOrCreatePermission(admin, module, true, true, true, true, true);
        }

        findOrCreatePermission(responsableProduction, modules.get("DASHBOARD"), false, true, false, false, false);
        findOrCreatePermission(responsableProduction, modules.get("RECEPTION"), true, true, false, false, true);
        findOrCreatePermission(responsableProduction, modules.get("GUIDE_PRODUCTION"), true, true, true, false, true);
        findOrCreatePermission(responsableProduction, modules.get("MACHINES"), false, true, false, false, false);
        findOrCreatePermission(responsableProduction, modules.get("MATIERES_PREMIERES"), true, true, true, false, false);
        findOrCreatePermission(responsableProduction, modules.get("STOCK"), true, true, true, false, false);
        findOrCreatePermission(responsableProduction, modules.get("LOTS_TRAÇABILITE"), true, true, true, false, false);
        findOrCreatePermission(responsableProduction, modules.get("DASHBOARD_ADMIN"), false, false, false, false, false);
        findOrCreatePermission(responsableProduction, modules.get("HUILERIES"), false, false, false, false, false);
        findOrCreatePermission(responsableProduction, modules.get("COMPTES_PROFILS"), false, false, false, false, false);
        findOrCreatePermission(responsableProduction, modules.get("STOCK_MOUVEMENT"), false, false, false, false, false);

        seedDefaultAdminUser(admin);
    }

    private Profil findOrCreateProfil(String nom, String description) {
        return profilRepository.findByNom(nom)
                .orElseGet(() -> {
                    Profil profil = new Profil();
                    profil.setNom(nom);
                    profil.setDescription(description);
                    Profil saved = profilRepository.save(profil);
                    log.info("[SEED] Profil cree: {}", saved.getNom());
                    return saved;
                });
    }

    private Module findOrCreateModule(String nom) {
        return moduleRepository.findByNom(nom)
                .orElseGet(() -> {
                    Module module = new Module();
                    module.setNom(nom);
                    Module saved = moduleRepository.save(module);
                    log.info("[SEED] Module cree: {}", saved.getNom());
                    return saved;
                });
    }

    private void findOrCreatePermission(Profil profil, Module module, boolean canCreate, boolean canRead,
                                        boolean canUpdate, boolean canDelete, boolean canExecuted) {
        permissionRepository.findByProfilIdProfilAndModuleIdModule(profil.getIdProfil(), module.getIdModule())
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setProfil(profil);
                    permission.setModule(module);
                    permission.setCanCreate(canCreate);
                    permission.setCanRead(canRead);
                    permission.setCanUpdate(canUpdate);
                    permission.setCanDelete(canDelete);
                    permission.setCanExecuted(canExecuted);
                    Permission saved = permissionRepository.save(permission);
                    log.info("[SEED] Permission creee: profil={}, module={}", profil.getNom(), module.getNom());
                    return saved;
                });
    }

    private void seedDefaultAdminUser(Profil adminProfil) {
        if (utilisateurRepository.findByEmail("smatitakwapro@gmail.com").isPresent()) {
            return;
        }

        Huilerie huilerie = huilerieRepository.findAll().stream().findFirst().orElse(null);
        if (huilerie == null) {
            log.warn("[SEED] Aucun utilisateur admin cree: aucune huilerie disponible");
            return;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom("Admin");
        utilisateur.setPrenom("Système");
        utilisateur.setEmail("smatitakwapro@gmail.com");
        utilisateur.setMotDePasse(passwordEncoder.encode(adminPassword));
        utilisateur.setTelephone(null);
        utilisateur.setProfil(adminProfil);
        utilisateur.setHuilerie(huilerie);
        utilisateur.setActif(StatutUtilisateur.ACTIF);

        utilisateur.setEmailVerified(true);
        utilisateur.setVerificationToken(UUID.randomUUID().toString());
        utilisateur.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(verificationEmailExpirationHours));

        utilisateurRepository.save(utilisateur);
        log.info("[SEED] Utilisateur admin cree: {}", utilisateur.getEmail());
    }
}
