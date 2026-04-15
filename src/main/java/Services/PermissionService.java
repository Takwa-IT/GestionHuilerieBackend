package Services;

import Models.Permission;
import Models.Utilisateur;
import Repositories.PermissionRepository;
import Repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private static final long CACHE_TTL_MINUTES = 5;

    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;

    private final Map<Long, CacheEntry> permissionCache = new ConcurrentHashMap<>();

    public List<Permission> getPermissions(Long utilisateurId) {
        CacheEntry entry = permissionCache.get(utilisateurId);
        if (entry != null && entry.expiresAt().isAfter(Instant.now())) {
            return entry.permissions();
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (utilisateur.getProfil() == null) {
            return List.of();
        }

        List<Permission> permissions = permissionRepository.findByProfilIdWithModule(utilisateur.getProfil().getIdProfil());
        permissionCache.put(utilisateurId, new CacheEntry(permissions, Instant.now().plus(CACHE_TTL_MINUTES, ChronoUnit.MINUTES)));
        return permissions;
    }

    public boolean hasPermission(Long utilisateurId, String module, String action) {
        String normalizedModule = normalize(module);
        String normalizedAction = normalize(action);

        return getPermissions(utilisateurId).stream()
                .filter(permission -> normalize(permission.getModule().getNom()).equals(normalizedModule))
                .anyMatch(permission -> actionAllowed(permission, normalizedAction));
    }

    @Transactional
    public void evictUserPermissions(Long utilisateurId) {
        permissionCache.remove(utilisateurId);
    }

    @Transactional
    public void evictPermissionsByProfil(Long profilId) {
        utilisateurRepository.findByProfilIdProfil(profilId)
                .forEach(utilisateur -> permissionCache.remove(utilisateur.getIdUtilisateur()));
    }

    private boolean actionAllowed(Permission permission, String action) {
        return switch (action) {
            case "CREATE" -> Boolean.TRUE.equals(permission.getCanCreate());
            case "READ" -> Boolean.TRUE.equals(permission.getCanRead());
            case "UPDATE" -> Boolean.TRUE.equals(permission.getCanUpdate());
            case "DELETE" -> Boolean.TRUE.equals(permission.getCanDelete());
            case "EXECUTE" -> Boolean.TRUE.equals(permission.getCanExecuted());
            default -> false;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private record CacheEntry(List<Permission> permissions, Instant expiresAt) {
    }
}


