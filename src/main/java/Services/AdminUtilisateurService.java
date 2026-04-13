package Services;

import Models.Entreprise;
import Models.Huilerie;
import Models.Profil;
import Models.Utilisateur;
import Repositories.EntrepriseRepository;
import Repositories.HuilerieRepository;
import Repositories.ProfilRepository;
import Repositories.UtilisateurRepository;
import dto.UtilisateurAdminDTO;
import dto.UtilisateurAdminRequestDTO;
import dto.UtilisateurStatusUpdateDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final HuilerieRepository huilerieRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    @Value("${security.verification-email.expiration-hours:24}")
    private long verificationEmailExpirationHours;

    @Transactional(readOnly = true)
    public List<UtilisateurAdminDTO> findAll() {
        Long entrepriseId = currentUserService.getCurrentEntrepriseIdOrThrow();
        return utilisateurRepository.findAllByEntreprise_IdEntrepriseOrderByIdUtilisateurAsc(entrepriseId).stream()
                .map(this::toDTO)
                .toList();
    }

    public UtilisateurAdminDTO create(UtilisateurAdminRequestDTO request) {
        utilisateurRepository.findByEmail(request.getEmail())
                .ifPresent(u -> { throw new DataIntegrityViolationException("Email deja utilise"); });

        Profil profil = profilRepository.findById(request.getProfilId())
                .orElseThrow(() -> new EntityNotFoundException("Profil introuvable"));
        ResolvedUserScope scope = resolveUserScope(request, profil);

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setProfil(profil);
        utilisateur.setEntreprise(scope.entreprise());
        utilisateur.setHuilerie(scope.huilerie());
        utilisateur.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString()));
        utilisateur.setEmailVerified(false);
        utilisateur.setVerificationToken(UUID.randomUUID().toString());
        utilisateur.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(verificationEmailExpirationHours));

        return toDTO(utilisateurRepository.save(utilisateur));
    }

    public UtilisateurAdminDTO update(Long id, UtilisateurAdminRequestDTO request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        ensureSameEntreprise(utilisateur);

        utilisateurRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getIdUtilisateur().equals(id))
                .ifPresent(u -> { throw new DataIntegrityViolationException("Email deja utilise"); });

        Profil profil = profilRepository.findById(request.getProfilId())
                .orElseThrow(() -> new EntityNotFoundException("Profil introuvable"));
        ResolvedUserScope scope = resolveUserScope(request, profil);

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setProfil(profil);
        utilisateur.setEntreprise(scope.entreprise());
        utilisateur.setHuilerie(scope.huilerie());
        return toDTO(utilisateurRepository.save(utilisateur));
    }

    public UtilisateurAdminDTO updateStatus(Long id, UtilisateurStatusUpdateDTO request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        ensureSameEntreprise(utilisateur);
        utilisateur.setActif(request.getActif());
        return toDTO(utilisateurRepository.save(utilisateur));
    }

    public void delete(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        ensureSameEntreprise(utilisateur);
        utilisateurRepository.delete(utilisateur);
    }

    private void ensureSameEntreprise(Utilisateur utilisateur) {
        Long currentEntrepriseId = currentUserService.getCurrentEntrepriseIdOrThrow();
        Long targetEntrepriseId = utilisateur.getEntreprise() != null ? utilisateur.getEntreprise().getIdEntreprise() : null;

        if (targetEntrepriseId == null || !currentEntrepriseId.equals(targetEntrepriseId)) {
            throw new AccessDeniedException("Acces refuse a un utilisateur d'une autre entreprise");
        }
    }

    private UtilisateurAdminDTO toDTO(Utilisateur utilisateur) {
        UtilisateurAdminDTO dto = new UtilisateurAdminDTO();
        dto.setIdUtilisateur(utilisateur.getIdUtilisateur());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setActif(utilisateur.getActif());
        dto.setProfilId(utilisateur.getProfil() != null ? utilisateur.getProfil().getIdProfil() : null);
        dto.setProfilNom(utilisateur.getProfil() != null ? utilisateur.getProfil().getNom() : null);
        dto.setEntrepriseId(utilisateur.getEntreprise() != null ? utilisateur.getEntreprise().getIdEntreprise() : null);
        dto.setHuilerieId(utilisateur.getHuilerie() != null ? utilisateur.getHuilerie().getIdHuilerie() : null);
        dto.setHuilerieNom(utilisateur.getHuilerie() != null ? utilisateur.getHuilerie().getNom() : null);
        return dto;
    }

    private ResolvedUserScope resolveUserScope(UtilisateurAdminRequestDTO request, Profil profil) {
        Huilerie huilerie = null;
        Entreprise entreprise = null;

        if (request.getHuilerieId() != null) {
            huilerie = huilerieRepository.findById(request.getHuilerieId())
                    .orElseThrow(() -> new EntityNotFoundException("Huilerie introuvable"));
            entreprise = huilerie.getEntreprise();
        }

        if (request.getEntrepriseId() != null) {
            Entreprise requestedEntreprise = entrepriseRepository.findById(request.getEntrepriseId())
                    .orElseThrow(() -> new EntityNotFoundException("Entreprise introuvable"));

            if (entreprise != null && !requestedEntreprise.getIdEntreprise().equals(entreprise.getIdEntreprise())) {
                throw new IllegalArgumentException("L'huilerie n'appartient pas a l'entreprise selectionnee");
            }

            entreprise = requestedEntreprise;
        }

        if (entreprise == null) {
            throw new IllegalArgumentException("Entreprise obligatoire");
        }

        Long currentEntrepriseId = currentUserService.getCurrentEntrepriseIdOrThrow();
        if (!currentEntrepriseId.equals(entreprise.getIdEntreprise())) {
            throw new IllegalArgumentException("Acces refuse a une autre entreprise");
        }

        boolean isAdminProfile = profil.getNom() != null && "ADMIN".equalsIgnoreCase(profil.getNom());
        if (!isAdminProfile && huilerie == null) {
            throw new IllegalArgumentException("Huilerie obligatoire pour ce profil");
        }

        return new ResolvedUserScope(entreprise, huilerie);
    }

    private record ResolvedUserScope(Entreprise entreprise, Huilerie huilerie) {
    }
}
