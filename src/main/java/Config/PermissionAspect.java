package Config;

import Models.Utilisateur;
import Repositories.UtilisateurRepository;
import Services.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

    private final PermissionService permissionService;
    private final UtilisateurRepository utilisateurRepository;

    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Non authentifie");
        }

        String email = extractEmail(authentication);
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Utilisateur non authentifie"));

        String module = requirePermission.module();
        String action = requirePermission.action().name();

        // Allow regular users (non-admin) to READ from LOTS_TRAÇABILITE module
        if ("LOTS_TRAÇABILITE".equalsIgnoreCase(module) && "READ".equalsIgnoreCase(action) && !isAdminUser(utilisateur)) {
            return;
        }

        if (!permissionService.hasPermission(utilisateur.getIdUtilisateur(), module, action)) {
            log.warn("[SECURITY] userId={} a tente action={} sur module={} -> REFUSE", utilisateur.getIdUtilisateur(), action, module);
            throw new PermissionDeniedException(module, action);
        }
    }

    private String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getUsername();
        }
        return authentication.getName();
    }

    private boolean isAdminUser(Utilisateur utilisateur) {
        return utilisateur.getProfil() != null && "ADMIN".equalsIgnoreCase(utilisateur.getProfil().getNom());
    }
}


