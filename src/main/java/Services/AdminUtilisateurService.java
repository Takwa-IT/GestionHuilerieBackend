package Services;

import Models.Huilerie;
import Models.Profil;
import Models.Utilisateur;
import Repositories.HuilerieRepository;
import Repositories.ProfilRepository;
import Repositories.UtilisateurRepository;
import dto.UtilisateurAdminDTO;
import dto.UtilisateurAdminRequestDTO;
import dto.UtilisateurStatusUpdateDTO;
import dto.UtilisateurAdminUpdateRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${security.verification-email.expiration-hours:24}")
    private long verificationEmailExpirationHours;

    @Transactional(readOnly = true)
    public List<UtilisateurAdminDTO> findAll() {
        return utilisateurRepository.findAllByOrderByIdUtilisateurAsc().stream().map(this::toDTO).toList();
    }

    public UtilisateurAdminDTO create(UtilisateurAdminRequestDTO request) {
        utilisateurRepository.findByEmail(request.getEmail())
                .ifPresent(u -> { throw new DataIntegrityViolationException("Email deja utilise"); });

        Profil profil = profilRepository.findById(request.getProfilId())
                .orElseThrow(() -> new EntityNotFoundException("Profil introuvable"));
        Huilerie huilerie = huilerieRepository.findById(request.getHuilerieId())
                .orElseThrow(() -> new EntityNotFoundException("Huilerie introuvable"));

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setProfil(profil);
        utilisateur.setHuilerie(huilerie);
        utilisateur.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString()));
        utilisateur.setEmailVerified(false);
        utilisateur.setVerificationToken(UUID.randomUUID().toString());
        utilisateur.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(verificationEmailExpirationHours));

        return toDTO(utilisateurRepository.save(utilisateur));
    }

    public UtilisateurAdminDTO update(Long id, UtilisateurAdminRequestDTO request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));

        utilisateurRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getIdUtilisateur().equals(id))
                .ifPresent(u -> { throw new DataIntegrityViolationException("Email deja utilise"); });

        Profil profil = profilRepository.findById(request.getProfilId())
                .orElseThrow(() -> new EntityNotFoundException("Profil introuvable"));
        Huilerie huilerie = huilerieRepository.findById(request.getHuilerieId())
                .orElseThrow(() -> new EntityNotFoundException("Huilerie introuvable"));

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setProfil(profil);
        utilisateur.setHuilerie(huilerie);
        return toDTO(utilisateurRepository.save(utilisateur));
    }

    public UtilisateurAdminDTO updateStatus(Long id, UtilisateurStatusUpdateDTO request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        utilisateur.setActif(request.getActif());
        return toDTO(utilisateurRepository.save(utilisateur));
    }

    public void delete(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        utilisateurRepository.delete(utilisateur);
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
        dto.setHuilerieId(utilisateur.getHuilerie().getIdHuilerie());
        dto.setHuilerieNom(utilisateur.getHuilerie().getNom());
        return dto;
    }
}
