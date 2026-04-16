package Services;

import Mapper.EntrepriseMapper;
import Models.Entreprise;
import Repositories.EntrepriseRepository;
import dto.EntrepriseCreateDTO;
import dto.EntrepriseDTO;
import dto.EntrepriseUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EntrepriseService {

    private final EntrepriseRepository entrepriseRepository;
    private final EntrepriseMapper entrepriseMapper;

    public EntrepriseDTO create(EntrepriseCreateDTO dto) {
        Entreprise entreprise = entrepriseMapper.toEntity(dto);
        Entreprise saved = entrepriseRepository.save(entreprise);
        return entrepriseMapper.toDTO(saved);
    }

    public EntrepriseDTO getById(Long idEntreprise) {
        Entreprise entreprise = entrepriseRepository.findById(idEntreprise)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvee"));
        return entrepriseMapper.toDTO(entreprise);
    }

    public List<EntrepriseDTO> getAll() {
        return entrepriseRepository.findAll().stream()
                .map(entrepriseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public EntrepriseDTO update(Long idEntreprise, EntrepriseUpdateDTO dto) {
        Entreprise entreprise = entrepriseRepository.findById(idEntreprise)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvee"));

        entrepriseMapper.updateFromDTO(dto, entreprise);
        Entreprise saved = entrepriseRepository.save(entreprise);
        return entrepriseMapper.toDTO(saved);
    }

    public void delete(Long idEntreprise) {
        if (!entrepriseRepository.existsById(idEntreprise)) {
            throw new RuntimeException("Entreprise non trouvee");
        }
        entrepriseRepository.deleteById(idEntreprise);
    }
}
