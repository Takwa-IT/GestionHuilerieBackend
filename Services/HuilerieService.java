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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HuilerieService {

    private final HuilerieRepository huilerieRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final HuilerieMapper huilerieMapper;

    public HuilerieDTO create(HuilerieCreateDTO dto) {
        Huilerie huilerie = huilerieMapper.toEntity(dto);

        Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
        huilerie.setEntreprise(entreprise);

        Huilerie saved = huilerieRepository.save(huilerie);
        return huilerieMapper.toDTO(saved);
    }

    public HuilerieDTO update(Long id, HuilerieUpdateDTO dto) {
        Huilerie huilerie = huilerieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvée"));

        huilerieMapper.updateFromDTO(dto, huilerie);

        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
            huilerie.setEntreprise(entreprise);
        }

        Huilerie saved = huilerieRepository.save(huilerie);
        return huilerieMapper.toDTO(saved);
    }

    public void activate(Long id) {
        Huilerie huilerie = huilerieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvée"));
        huilerie.setActive(true);
        huilerieRepository.save(huilerie);
    }

    public void deactivate(Long id) {
        Huilerie huilerie = huilerieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvée"));
        huilerie.setActive(false);
        huilerieRepository.save(huilerie);
    }

    public HuilerieDTO findById(Long id) {
        Huilerie huilerie = huilerieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvée"));
        return huilerieMapper.toDTO(huilerie);
    }

    public List<HuilerieDTO> findAll() {
        return huilerieRepository.findAll().stream()
                .map(huilerieMapper::toDTO)
                .collect(Collectors.toList());
    }
}
