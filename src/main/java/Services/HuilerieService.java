package Services;

import Mapper.HuilerieMapper;
import Models.Entreprise;
import Models.Huilerie;
import Repositories.EntrepriseRepository;
import Repositories.HuilerieRepository;
import dto.HuilerieCreateDTO;
import dto.HuilerieDTO;
import dto.HuilerieUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HuilerieService {

    private final HuilerieRepository huilerieRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final HuilerieMapper huilerieMapper;

    public HuilerieDTO create(HuilerieCreateDTO dto) {
        if (huilerieRepository.existsByNom(dto.getNom())) {
            throw new RuntimeException("Une huilerie avec ce nom existe deja");
        }

        Huilerie huilerie = huilerieMapper.toEntity(dto);
        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvee"));
        huilerie.setEntreprise(entreprise);

        Huilerie saved = huilerieRepository.save(huilerie);
        return huilerieMapper.toDTO(saved);
    }

    public HuilerieDTO update(Long idHuilerie, HuilerieUpdateDTO dto) {
        Huilerie huilerie = huilerieRepository.findById(idHuilerie)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));

        if (dto.getNom() != null) {
            huilerieRepository.findByNom(dto.getNom())
                    .filter(existing -> !existing.getIdHuilerie().equals(idHuilerie))
                    .ifPresent(existing -> {
                        throw new RuntimeException("Une huilerie avec ce nom existe deja");
                    });
        }

        huilerieMapper.updateFromDTO(dto, huilerie);

        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvee"));
            huilerie.setEntreprise(entreprise);
        }

        Huilerie saved = huilerieRepository.save(huilerie);
        return huilerieMapper.toDTO(saved);
    }

    public void activate(Long idHuilerie) {
        Huilerie huilerie = huilerieRepository.findById(idHuilerie)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));
        huilerie.setActive(true);
        huilerieRepository.save(huilerie);
    }

    public void deactivate(Long idHuilerie) {
        Huilerie huilerie = huilerieRepository.findById(idHuilerie)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));
        huilerie.setActive(false);
        huilerieRepository.save(huilerie);
    }

    public HuilerieDTO findById(Long idHuilerie) {
        Huilerie huilerie = huilerieRepository.findById(idHuilerie)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));
        return huilerieMapper.toDTO(huilerie);
    }

    public List<HuilerieDTO> findAll() {
        return huilerieRepository.findAll().stream()
                .map(huilerieMapper::toDTO)
                .toList();
    }
}

