package Services;

import Models.Profil;
import Repositories.ProfilRepository;
import Repositories.UtilisateurRepository;
import dto.ProfilDTO;
import dto.ProfilRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProfilService {

    private final ProfilRepository profilRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public List<ProfilDTO> findAll() {
        return profilRepository.findAll().stream().map(this::toDTO).toList();
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
        Profil profil = profilRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profil introuvable"));

        if (utilisateurRepository.existsByProfilIdProfil(id)) {
            throw new IllegalArgumentException("Suppression impossible: profil utilise par des utilisateurs");
        }

        profilRepository.delete(profil);
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