package Services;

import Models.Profil;
import Repositories.ProfilRepository;
import Repositories.UtilisateurRepository;
import dto.ProfilDTO;
import dto.ProfilRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProfilService {

    private final ProfilRepository profilRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<ProfilDTO> findAll(String huilerieNom) {
        Long entrepriseId = currentUserService.getCurrentEntrepriseIdOrThrow();

        // Retorna todos los profils disponibles (no solo los asociados a usuarios)
        List<Profil> profils = profilRepository.findAllByOrderByIdProfilAsc();

        return profils.stream()
                .map(this::toDTO)
                .toList();
    }

    public ProfilDTO create(ProfilRequestDTO request) {
        profilRepository.findByNom(request.getNom())
                .ifPresent(p -> { throw new DataIntegrityViolationException("Nom de profil deja utilise"); });

        Profil profil = new Profil();
        profil.setNom(request.getNom());
        profil.setDescription(request.getDescription());
        return toDTO(profilRepository.save(profil));
    }

    public ProfilDTO update(Long id, ProfilRequestDTO request) {
        ensureProfilInAccessibleHuileries(id);

        Profil profil = profilRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profil introuvable"));

        profilRepository.findByNom(request.getNom())
                .filter(existing -> !existing.getIdProfil().equals(id))
                .ifPresent(p -> { throw new DataIntegrityViolationException("Nom de profil deja utilise"); });

        profil.setNom(request.getNom());
        profil.setDescription(request.getDescription());
        return toDTO(profilRepository.save(profil));
    }

    public void delete(Long id) {
        ensureProfilInAccessibleHuileries(id);

        Profil profil = profilRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profil introuvable"));

        if (utilisateurRepository.existsByProfilIdProfil(id)) {
            throw new IllegalArgumentException("Suppression impossible: profil utilise par des utilisateurs");
        }

        profilRepository.delete(profil);
    }

    private void ensureProfilInAccessibleHuileries(Long profilId) {
        Long entrepriseId = currentUserService.getCurrentEntrepriseIdOrThrow();
        if (entrepriseId == null) {
            throw new AccessDeniedException("Acces refuse au profil demande");
        }

        boolean belongsToEntreprise = utilisateurRepository.findDistinctProfilsByEntrepriseIdOrderByIdProfilAsc(entrepriseId)
                .stream()
                .anyMatch(profil -> profilId.equals(profil.getIdProfil()));

        if (!belongsToEntreprise) {
            throw new AccessDeniedException("Acces refuse a un profil hors entreprise accessible");
        }
    }

    private String normalizeHuilerieNom(String huilerieNom) {
        String normalized = huilerieNom == null ? null : huilerieNom.trim();
        return (normalized == null || normalized.isEmpty()) ? null : normalized;
    }

    private ProfilDTO toDTO(Profil profil) {
        ProfilDTO dto = new ProfilDTO();
        dto.setIdProfil(profil.getIdProfil());
        dto.setNom(profil.getNom());
        dto.setDescription(profil.getDescription());
        dto.setDateCreation(profil.getDateCreation());
        return dto;
    }
}