package Services;

import Models.Administrateur;
import Models.Employe;
import Models.Entreprise;
import Models.Huilerie;
import Models.Utilisateur;
import Repositories.HuilerieRepository;
import Repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UtilisateurRepository utilisateurRepository;
    private final HuilerieRepository huilerieRepository;

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
        if (utilisateur == null || utilisateur.getProfil() == null || utilisateur.getProfil().getNom() == null) {
            return false;
        }

        String profilNom = utilisateur.getProfil().getNom().trim().toUpperCase();
        return "ADMIN".equals(profilNom)
                || "ADMINISTRATEUR".equals(profilNom)
                || profilNom.contains("ADMIN");
    }

    public Long getCurrentHuilerieIdOrThrow() {
        Utilisateur utilisateur = getAuthenticatedUtilisateur();
        if (utilisateur instanceof Employe employe) {
            Huilerie huilerie = employe.getHuilerieEmp();
            if (huilerie == null || huilerie.getIdHuilerie() == null) {
                throw new AccessDeniedException("Employe sans huilerie associee");
            }
            return huilerie.getIdHuilerie();
        }

        if (utilisateur instanceof Administrateur) {
            throw new AccessDeniedException("Administrateur sans huilerie associee");
        }

        if (utilisateur.getHuilerie() == null || utilisateur.getHuilerie().getIdHuilerie() == null) {
            throw new AccessDeniedException("Utilisateur sans huilerie associee");
        }
        return utilisateur.getHuilerie().getIdHuilerie();
    }

    public Long getCurrentEntrepriseIdOrThrow() {
        Utilisateur utilisateur = getAuthenticatedUtilisateur();
        
        // Pour les administrateurs, utiliser la vraie relation persistée
        if (utilisateur instanceof Administrateur) {
            Administrateur admin = (Administrateur) utilisateur;
            Entreprise entreprise = admin.getEntrepriseAdmin();
            if (entreprise == null || entreprise.getIdEntreprise() == null) {
                throw new AccessDeniedException("Administrateur sans entreprise associee");
            }
            return entreprise.getIdEntreprise();
        }
        
        // Pour les employés, récupérer l'entreprise via la huilerie
        if (utilisateur instanceof Employe) {
            Employe employe = (Employe) utilisateur;
            Huilerie huilerie = employe.getHuilerieEmp();
            if (huilerie == null || huilerie.getEntreprise() == null || huilerie.getEntreprise().getIdEntreprise() == null) {
                throw new AccessDeniedException("Employe sans huilerie ou entreprise associee");
            }
            return huilerie.getEntreprise().getIdEntreprise();
        }
        
        // Fallback pour les autres utilisateurs
        if (utilisateur.getEntreprise() == null || utilisateur.getEntreprise().getIdEntreprise() == null) {
            throw new AccessDeniedException("Utilisateur sans entreprise associee");
        }
        return utilisateur.getEntreprise().getIdEntreprise();
    }

    public List<Long> getAccessibleHuilerieIds() {
        Utilisateur utilisateur = getAuthenticatedUtilisateur();
        if (!isAdmin(utilisateur)) {
            return List.of(getCurrentHuilerieIdOrThrow());
        }

        Long entrepriseId = getCurrentEntrepriseIdOrThrow();
        return huilerieRepository.findByEntreprise_IdEntreprise(entrepriseId).stream()
                .map(Models.Huilerie::getIdHuilerie)
                .toList();
    }

    public void ensureCanAccessHuilerie(Long huilerieId) {
        if (huilerieId == null) {
            throw new AccessDeniedException("Huilerie obligatoire");
        }

        Utilisateur utilisateur = getAuthenticatedUtilisateur();
        if (!isAdmin(utilisateur)) {
            if (!huilerieId.equals(getCurrentHuilerieIdOrThrow())) {
                throw new AccessDeniedException("Acces refuse a une autre huilerie");
            }
            return;
        }

        Long entrepriseId = getCurrentEntrepriseIdOrThrow();
        if (!huilerieRepository.existsByIdHuilerieAndEntreprise_IdEntreprise(huilerieId, entrepriseId)) {
            throw new AccessDeniedException("Acces refuse a une huilerie d'une autre entreprise");
        }
    }
}


