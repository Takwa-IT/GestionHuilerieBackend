package Config;

import Models.*;
import Models.Module;
import Repositories.HuilerieRepository;
import Repositories.ModuleRepository;
import Repositories.PermissionRepository;
import Repositories.ProfilRepository;
import Repositories.UtilisateurRepository;
import Repositories.AdministrateurRepository;
import Repositories.EntrepriseRepository;
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
    private final AdministrateurRepository administrateurRepository;
    private final EntrepriseRepository entrepriseRepository;
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
                "CAMPAGNE_OLIVES",
                "GUIDE_PRODUCTION",
                "MACHINES",
                "MATIERES_PREMIERES",
                "STOCK",
                "STOCK_MOUVEMENT",
                "LOTS_TRAÇABILITE",
                "DASHBOARD_ADMIN",
                "HUILERIES",
                "COMPTES_PROFILS")) {
            modules.put(moduleName, findOrCreateModule(moduleName));
        }

        for (Module module : modules.values()) {
            findOrCreatePermission(admin, module, true, true, true, true, true);
        }

        findOrCreatePermission(responsableProduction, modules.get("DASHBOARD"), false, true, false, false, false);
        findOrCreatePermission(responsableProduction, modules.get("RECEPTION"), true, true, false, false, true);
        findOrCreatePermission(responsableProduction, modules.get("CAMPAGNE_OLIVES"), true, true, true, false,
                false);
        findOrCreatePermission(responsableProduction, modules.get("GUIDE_PRODUCTION"), true, true, true, false, true);
        findOrCreatePermission(responsableProduction, modules.get("MACHINES"), false, true, false, false, false);
        findOrCreatePermission(responsableProduction, modules.get("MATIERES_PREMIERES"), true, true, true, false,
                false);
        findOrCreatePermission(responsableProduction, modules.get("STOCK"), true, true, true, false, false);
        findOrCreatePermission(responsableProduction, modules.get("STOCK_MOUVEMENT"), true, true, true, false, false);
        findOrCreatePermission(responsableProduction, modules.get("LOTS_TRAÇABILITE"), true, true, true, false, false);
        findOrCreatePermission(responsableProduction, modules.get("DASHBOARD_ADMIN"), false, false, false, false,
                false);
        findOrCreatePermission(responsableProduction, modules.get("HUILERIES"), false, false, false, false, false);
        findOrCreatePermission(responsableProduction, modules.get("COMPTES_PROFILS"), false, false, false, false,
                false);

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
        // Verifier que l'admin par défaut n'existe pas déjà
        if (utilisateurRepository.findByEmail("admin@default.com").isPresent()) {
            log.info("[SEED] Admin par défaut existe déjà, aucune création");
            return;
        }

        // Récupérer une Entreprise pour l'Administrateur
        Entreprise entreprise = huilerieRepository.findAll().stream()
            .map(Huilerie::getEntreprise)
            .findFirst()
            .orElse(null);

        if (entreprise == null) {
            log.warn("[SEED] Aucun utilisateur admin cree: aucune entreprise disponible");
            return;
        }

        // Créer un Administrateur (avec profil ADMIN et entreprise liée)
        Administrateur admin = new Administrateur();
        admin.setNom("Admin");
        admin.setPrenom("Système");
        admin.setEmail("admin@default.com");
        admin.setMotDePasse(passwordEncoder.encode(adminPassword));
        admin.setTelephone(null);
        admin.setProfil(adminProfil);
        admin.setEntrepriseAdmin(entreprise);
        admin.setActif(StatutUtilisateur.ACTIF);

        // Verification email déjà active pour l'admin par défaut
        admin.setEmailVerified(true);
        admin.setVerificationToken(UUID.randomUUID().toString());
        admin.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(verificationEmailExpirationHours));

        utilisateurRepository.save(admin);
        log.info("[SEED] Administrateur par défaut cree: {}", admin.getEmail());
    }
}
