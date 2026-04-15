package Services;

import Config.ReferenceUtils;
import Mapper.CampagneOlivesMapper;
import Models.CampagneOlives;
import Models.Huilerie;
import Repositories.CampagneOlivesRepository;
import Repositories.HuilerieRepository;
import dto.CampagneOlivesCreateDTO;
import dto.CampagneOlivesDTO;
import dto.CampagneOlivesUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CampagneOlivesService {

    private final CampagneOlivesRepository campagneOlivesRepository;
    private final HuilerieRepository huilerieRepository;
    private final CampagneOlivesMapper campagneOlivesMapper;
    private final CurrentUserService currentUserService;

    public CampagneOlivesDTO create(CampagneOlivesCreateDTO dto) {
        currentUserService.ensureCanAccessHuilerie(dto.getHuilerieId());

        if (campagneOlivesRepository.findByAnnee(dto.getAnnee()).isPresent()) {
            throw new RuntimeException("Une campagne avec cette année existe déjà");
        }

        CampagneOlives entity = campagneOlivesMapper.toEntity(dto);
        Huilerie huilerie = huilerieRepository.findById(dto.getHuilerieId())
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvée"));
        entity.setHuilerie(huilerie);

        CampagneOlives saved = campagneOlivesRepository.save(entity);
        saved.setReference(ReferenceUtils.format("CP", saved.getIdCampagne()));
        saved = campagneOlivesRepository.save(saved);

        return campagneOlivesMapper.toDTO(saved);
    }

    public CampagneOlivesDTO update(Long idCampagne, CampagneOlivesUpdateDTO dto) {
        CampagneOlives entity = findById(idCampagne);
        campagneOlivesMapper.updateFromDTO(dto, entity);
        CampagneOlives saved = campagneOlivesRepository.save(entity);
        return campagneOlivesMapper.toDTO(saved);
    }

    public void delete(Long idCampagne) {
        CampagneOlives entity = findById(idCampagne);
        campagneOlivesRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public CampagneOlivesDTO findByReference(String reference) {
        CampagneOlives entity = findCampagneByReference(reference);
        return campagneOlivesMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public CampagneOlivesDTO findDTOById(Long idCampagne) {
        CampagneOlives entity = findById(idCampagne);
        return campagneOlivesMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<CampagneOlivesDTO> findAll(String huilerieNom) {
        List<CampagneOlives> campagnes;

        if (huilerieNom != null && !huilerieNom.isBlank()) {
            campagnes = campagneOlivesRepository.findAll().stream()
                    .filter(c -> c.getHuilerie() != null && c.getHuilerie().getNom().equalsIgnoreCase(huilerieNom))
                    .toList();
        } else {
            campagnes = campagneOlivesRepository.findAll();
        }

        return campagnes.stream()
                .map(campagneOlivesMapper::toDTO)
                .toList();
    }

    // Internal methods
    @Transactional(readOnly = true)
    public CampagneOlives findById(Long idCampagne) {
        return campagneOlivesRepository.findById(idCampagne)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
    }

    @Transactional(readOnly = true)
    public CampagneOlives findCampagneByReference(String reference) {
        String annee = reference.substring(2); // CP00001 -> 00001
        return campagneOlivesRepository.findByAnnee(annee)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée avec la référence: " + reference));
    }

    @Transactional(readOnly = true)
    public CampagneOlives findByAnnee(String annee) {
        return campagneOlivesRepository.findByAnnee(annee)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée pour l'année: " + annee));
    }
}
