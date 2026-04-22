package Services;

import Config.ReferenceUtils;
import Mapper.CampagneOlivesMapper;
import Models.CampagneOlives;
import Models.Huilerie;
import Models.Utilisateur;
import Repositories.CampagneOlivesRepository;
import Repositories.HuilerieRepository;
import Repositories.LotOlivesRepository;
import dto.CampagneOlivesCreateDTO;
import dto.CampagneOlivesDTO;
import dto.CampagneOlivesUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CampagneOlivesService {

    private final CampagneOlivesRepository campagneOlivesRepository;
    private final HuilerieRepository huilerieRepository;
    private final LotOlivesRepository lotOlivesRepository;
    private final CampagneOlivesMapper campagneOlivesMapper;
    private final CurrentUserService currentUserService;

    public CampagneOlivesDTO create(CampagneOlivesCreateDTO dto) {
        currentUserService.ensureCanAccessHuilerie(dto.getHuilerieId());

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

        if (lotOlivesRepository.existsByCampagne_IdCampagne(idCampagne)) {
            throw new DataIntegrityViolationException(
                    "Impossible de supprimer cette campagne car elle est liee a un ou plusieurs lots");
        }

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
        return findAll(null, huilerieNom);
    }

    @Transactional(readOnly = true)
    public List<CampagneOlivesDTO> findAll(String reference, String huilerieNom) {
        List<CampagneOlives> campagnes;

        campagnes = findAllScopedToCurrentUser();

        if (reference != null && !reference.isBlank()) {
            String normalizedReference = reference.trim().toLowerCase();
            campagnes = campagnes.stream()
                    .filter(c -> c.getReference() != null
                            && c.getReference().toLowerCase().contains(normalizedReference))
                    .toList();
        }

        if (huilerieNom != null && !huilerieNom.isBlank()) {
            String normalizedHuilerieNom = huilerieNom.trim().toLowerCase();
            campagnes = campagnes.stream()
                    .filter(c -> c.getHuilerie() != null
                            && c.getHuilerie().getNom() != null
                            && c.getHuilerie().getNom().toLowerCase().contains(normalizedHuilerieNom))
                    .toList();
        }

        return campagnes.stream()
                .map(campagneOlivesMapper::toDTO)
                .toList();
    }

    // Internal methods
    @Transactional(readOnly = true)
    public CampagneOlives findById(Long idCampagne) {
        CampagneOlives campagne = campagneOlivesRepository.findById(idCampagne)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
        ensureCanAccessCampagne(campagne);
        return campagne;
    }

    @Transactional(readOnly = true)
    public CampagneOlives findCampagneByReference(String reference) {
        String normalizedReference = reference == null ? "" : reference.trim();
        CampagneOlives campagne = campagneOlivesRepository.findByNormalizedReference(normalizedReference)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée avec la référence: " + reference));
        ensureCanAccessCampagne(campagne);
        return campagne;
    }

    @Transactional(readOnly = true)
    public CampagneOlives findByAnnee(String annee) {
        return campagneOlivesRepository.findByAnnee(annee)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée pour l'année: " + annee));
    }

    private List<CampagneOlives> findAllScopedToCurrentUser() {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            Long entrepriseId = currentUserService.getCurrentEntrepriseIdOrThrow();
            return campagneOlivesRepository.findAllByHuilerie_Entreprise_IdEntreprise(entrepriseId);
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return campagneOlivesRepository.findAllByHuilerie_IdHuilerie(huilerieId);
    }

    private void ensureCanAccessCampagne(CampagneOlives campagne) {
        Long huilerieId = campagne.getHuilerie() != null ? campagne.getHuilerie().getIdHuilerie() : null;
        currentUserService.ensureCanAccessHuilerie(huilerieId);
    }
}
