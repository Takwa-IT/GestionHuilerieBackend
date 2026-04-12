package Services;

import Models.Utilisateur;
import Repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UtilisateurRepository utilisateurRepository;

    public Utilisateur getAuthenticatedUtilisateur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Non authentifie");
        }

        Object principal = authentication.getPrincipal();
        String email = principal instanceof User user ? user.getUsername() : authentication.getName();

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Utilisateur non authentifie"));
    }

    public boolean isAdmin(Utilisateur utilisateur) {
        return utilisateur.getProfil() != null
                && utilisateur.getProfil().getNom() != null
                && "ADMIN".equalsIgnoreCase(utilisateur.getProfil().getNom());
    }

    public Long getCurrentHuilerieIdOrThrow() {
        Utilisateur utilisateur = getAuthenticatedUtilisateur();
        if (utilisateur.getHuilerie() == null || utilisateur.getHuilerie().getIdHuilerie() == null) {
            throw new AccessDeniedException("Utilisateur sans huilerie associee");
        }
        return utilisateur.getHuilerie().getIdHuilerie();
    }
}