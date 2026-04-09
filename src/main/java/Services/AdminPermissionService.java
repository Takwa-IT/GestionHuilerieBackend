package Services;

import Models.Module;
import Models.Permission;
import Models.Profil;
import Repositories.ModuleRepository;
import Repositories.PermissionRepository;
import Repositories.ProfilRepository;
import dto.BulkPermissionUpsertRequestDTO;
import dto.PermissionItemUpsertDTO;
import dto.PermissionModuleDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminPermissionService {

    private final PermissionRepository permissionRepository;
    private final ProfilRepository profilRepository;
    private final ModuleRepository moduleRepository;
    private final PermissionService permissionService;

    private static final Set<String> REQUIRED_MODULES = new LinkedHashSet<>(List.of(
            "DASHBOARD",
            "RECEPTION",
            "GUIDE_PRODUCTION",
            "MACHINES",
            "MATIERES_PREMIERES",
            "STOCK",
            "STOCK_MOUVEMENT",
            "LOTS_TRAÇABILITE",
            "DASHBOARD_ADMIN",
            "HUILERIES",
            "COMPTES_PROFILS"
    ));

    private static final Set<String> STOCK_MOVEMENT_ALIASES = Set.of(
            "STOCK_MOUVEMENT",
            "MOUVEMENT_STOCK",
            "STOCK_MOVEMENT",
            "STOCK_MOUVEMENTS",
            "STOCK_MOVEMENTS"
    );

    @Transactional(readOnly = true)
    public List<PermissionModuleDTO> findByProfil(Long profilId) {
        Profil profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new EntityNotFoundException("Profil introuvable"));

        List<Module> modules = ensureAndGetModules();
        Map<Long, Permission> byModuleId = new HashMap<>();
        permissionRepository.findByProfilIdWithModule(profilId)
                .forEach(p -> byModuleId.put(p.getModule().getIdModule(), p));

        return modules.stream().map(module -> {
            Permission permission = byModuleId.get(module.getIdModule());
            PermissionModuleDTO dto = new PermissionModuleDTO();
            dto.setModuleId(module.getIdModule());
            dto.setModuleNom(module.getNom());
            if (permission != null) {
                dto.setIdPrivilege(permission.getIdPrivilege());
                dto.setCanCreate(permission.getCanCreate());
                dto.setCanRead(permission.getCanRead());
                dto.setCanUpdate(permission.getCanUpdate());
                dto.setCanDelete(permission.getCanDelete());
                dto.setCanExecuted(permission.getCanExecuted());
            } else {
                dto.setCanCreate(false);
                dto.setCanRead(false);
                dto.setCanUpdate(false);
                dto.setCanDelete(false);
                dto.setCanExecuted(false);
            }
            return dto;
        }).toList();
    }

    public List<PermissionModuleDTO> bulkUpsert(BulkPermissionUpsertRequestDTO request) {
        Profil profil = profilRepository.findById(request.getProfilId())
                .orElseThrow(() -> new EntityNotFoundException("Profil introuvable"));

        for (PermissionItemUpsertDTO item : request.getPermissions()) {
            Module module = moduleRepository.findById(item.getModuleId())
                    .orElseThrow(() -> new EntityNotFoundException("Module introuvable: " + item.getModuleId()));

            Permission permission = permissionRepository
                    .findByProfilIdProfilAndModuleIdModule(profil.getIdProfil(), module.getIdModule())
                    .orElseGet(Permission::new);

            PermissionItemUpsertDTO normalizedItem = normalizePermissions(item, module);

            permission.setProfil(profil);
            permission.setModule(module);
            permission.setCanCreate(normalizedItem.getCanCreate());
            permission.setCanRead(normalizedItem.getCanRead());
            permission.setCanUpdate(normalizedItem.getCanUpdate());
            permission.setCanDelete(normalizedItem.getCanDelete());
            permission.setCanExecuted(normalizedItem.getCanExecuted());
            permissionRepository.save(permission);
        }

        permissionService.evictPermissionsByProfil(profil.getIdProfil());

        return findByProfil(profil.getIdProfil());
    }

    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission introuvable"));
        Long profilId = permission.getProfil().getIdProfil();
        permissionRepository.delete(permission);
        permissionService.evictPermissionsByProfil(profilId);
    }

    private List<Module> ensureAndGetModules() {
        List<Module> existingModules = moduleRepository.findAll();
        Set<String> existingNames = new LinkedHashSet<>();
        for (Module module : existingModules) {
            existingNames.add(module.getNom());
        }

        boolean createdAny = false;
        for (String requiredModule : REQUIRED_MODULES) {
            if (existingNames.contains(requiredModule)) {
                continue;
            }

            Module module = new Module();
            module.setNom(requiredModule);
            moduleRepository.save(module);
            createdAny = true;
        }

        if (!createdAny) {
            return existingModules;
        }

        return moduleRepository.findAll();
    }

    private PermissionItemUpsertDTO normalizePermissions(PermissionItemUpsertDTO item, Module module) {
        boolean canCreate = Boolean.TRUE.equals(item.getCanCreate());
        boolean canRead = Boolean.TRUE.equals(item.getCanRead());
        boolean canUpdate = Boolean.TRUE.equals(item.getCanUpdate());
        boolean canDelete = Boolean.TRUE.equals(item.getCanDelete());
        boolean canExecuted = Boolean.TRUE.equals(item.getCanExecuted());

        boolean isStockMovement = isStockMovementModule(module.getNom());

        if (!canRead) {
            canCreate = false;
            canUpdate = false;
            canDelete = false;
            canExecuted = false;
        } else if (canCreate) {
            canRead = true;
            canUpdate = true;
            canDelete = true;
            if (isStockMovement) {
                canExecuted = true;
            }
        } else {
            canUpdate = false;
            canDelete = false;
            canExecuted = false;
        }

        PermissionItemUpsertDTO normalized = new PermissionItemUpsertDTO();
        normalized.setModuleId(item.getModuleId());
        normalized.setCanCreate(canCreate);
        normalized.setCanRead(canRead);
        normalized.setCanUpdate(canUpdate);
        normalized.setCanDelete(canDelete);
        normalized.setCanExecuted(canExecuted);
        return normalized;
    }

    private boolean isStockMovementModule(String moduleName) {
        return STOCK_MOVEMENT_ALIASES.contains(normalizeModuleKey(moduleName));
    }

    private String normalizeModuleKey(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replaceAll("[\\s-]+", "_")
                .trim();

        return normalized;
    }
}