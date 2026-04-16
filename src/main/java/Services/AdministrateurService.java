package Services;

import Mapper.AdministrateurMapper;
import Models.Administrateur;
import Models.Entreprise;
import Repositories.AdministrateurRepository;
import Repositories.EntrepriseRepository;
import dto.AdministrateurCreateDTO;
import dto.AdministrateurDTO;
import dto.AdministrateurUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdministrateurService {

    private final AdministrateurRepository administrateurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final AdministrateurMapper administrateurMapper;

    public AdministrateurDTO create(AdministrateurCreateDTO dto) {
        if (administrateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Un administrateur avec cet email existe deja");
        }

        Administrateur administrateur = administrateurMapper.toEntity(dto);
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseAdminId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvee"));
        
        administrateur.setEntrepriseAdmin(entreprise);
        administrateur.setEmail(dto.getEmail());
        administrateur.setMotDePasse(dto.getMotDePasse());
        administrateur.setNom(dto.getNom());
        administrateur.setPrenom(dto.getPrenom());
        administrateur.setTelephone(dto.getTelephone());

        Administrateur saved = administrateurRepository.save(administrateur);
        return administrateurMapper.toDTO(saved);
    }

    public AdministrateurDTO getById(Long idAdministrateur) {
        Administrateur administrateur = administrateurRepository.findById(idAdministrateur)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouve"));
        return administrateurMapper.toDTO(administrateur);
    }

    public List<AdministrateurDTO> getAll() {
        return administrateurRepository.findAll().stream()
                .map(administrateurMapper::toDTO)
                .collect(Collectors.toList());
    }

    public AdministrateurDTO update(Long idAdministrateur, AdministrateurUpdateDTO dto) {
        Administrateur administrateur = administrateurRepository.findById(idAdministrateur)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouve"));

        administrateurMapper.updateFromDTO(dto, administrateur);

        if (dto.getEntrepriseAdminId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseAdminId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvee"));
            administrateur.setEntrepriseAdmin(entreprise);
        }

        Administrateur saved = administrateurRepository.save(administrateur);
        return administrateurMapper.toDTO(saved);
    }

    public void delete(Long idAdministrateur) {
        if (!administrateurRepository.existsById(idAdministrateur)) {
            throw new RuntimeException("Administrateur non trouve");
        }
        administrateurRepository.deleteById(idAdministrateur);
    }

    public AdministrateurDTO getByEmail(String email) {
        Administrateur administrateur = administrateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouve"));
        return administrateurMapper.toDTO(administrateur);
    }

    public AdministrateurDTO getByEntrepriseId(Long idEntreprise) {
        Administrateur administrateur = administrateurRepository.findByEntrepriseAdminIdEntreprise(idEntreprise)
                .orElseThrow(() -> new RuntimeException("Administrateur de cette entreprise non trouve"));
        return administrateurMapper.toDTO(administrateur);
    }
}
