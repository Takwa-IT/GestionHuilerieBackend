package Services;

import Config.ReferenceUtils;
import Mapper.MatierePremiereMapper;
import Models.MatierePremiere;
import Models.Utilisateur;
import Repositories.HuilerieRepository;
import Repositories.MatierePremiereRepository;
import dto.MatierePremiereCreateDTO;
import dto.MatierePremiereDTO;
import dto.MatierePremiereUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MatierePremiereService {

    private final MatierePremiereRepository matierePremiereRepository;
    private final HuilerieRepository huilerieRepository;
    private final MatierePremiereMapper matierePremiereMapper;
    private final CurrentUserService currentUserService;

    public MatierePremiereDTO create(MatierePremiereCreateDTO dto) {
        MatierePremiere entity = matierePremiereMapper.toEntity(dto);
        currentUserService.ensureCanAccessHuilerie(dto.getHuilerieId());
        entity.setHuilerie(huilerieRepository.findById(dto.getHuilerieId())
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee")));
        entity.setReference("TMP-MP-" + UUID.randomUUID());
        MatierePremiere saved = matierePremiereRepository.save(entity);
        saved.setReference(ReferenceUtils.format("MP", saved.getId()));
        return matierePremiereMapper.toDTO(matierePremiereRepository.save(saved));
    }

    public MatierePremiereDTO update(String reference, MatierePremiereUpdateDTO dto) {
        MatierePremiere entity = findMatiere(reference);
        matierePremiereMapper.updateFromDTO(dto, entity);
        return matierePremiereMapper.toDTO(matierePremiereRepository.save(entity));
    }

    public void delete(String reference) {
        matierePremiereRepository.delete(findMatiere(reference));
    }

    public MatierePremiereDTO findByReference(String reference) {
        return matierePremiereMapper.toDTO(findMatiere(reference));
    }

    public List<MatierePremiereDTO> findAll(String huilerieNom) {
        String normalizedHuilerieNom = normalizeName(huilerieNom);
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();
            List<MatierePremiere> source = normalizedHuilerieNom == null
                    ? matierePremiereRepository.findAll()
                    : matierePremiereRepository.findByHuilerie_NomIgnoreCase(normalizedHuilerieNom);

            return source.stream()
                    .filter(matiere -> matiere.getHuilerie() != null
                            && accessibleHuilerieIds.contains(matiere.getHuilerie().getIdHuilerie()))
                    .map(matierePremiereMapper::toDTO)
                    .toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return matierePremiereRepository.findByHuilerie_IdHuilerie(huilerieId)
                .stream()
                .filter(matiere -> normalizedHuilerieNom == null
                        || (matiere.getHuilerie() != null
                        && matiere.getHuilerie().getNom() != null
                        && matiere.getHuilerie().getNom().equalsIgnoreCase(normalizedHuilerieNom)))
                .map(matierePremiereMapper::toDTO)
                .toList();
    }

    //recupere une matiere premiere par reference pour le module CRUD
    public MatierePremiere findMatiere(String reference) {
        String rawValue = reference == null ? "" : reference.trim();
        String normalizedReference = normalizeReference(rawValue);
        String lookupKey = normalizeLookupKey(rawValue);

        if (!normalizedReference.isBlank()) {
            var foundByReference = matierePremiereRepository.findByNormalizedReference(normalizedReference);
            if (foundByReference.isPresent()) {
                return foundByReference.get();
            }

            Long extractedId = extractTrailingNumericId(normalizedReference);
            if (extractedId != null) {
                var foundById = matierePremiereRepository.findById(extractedId);
                if (foundById.isPresent()) {
                    return foundById.get();
                }
            }
        }

        if (!rawValue.isBlank()) {
            var foundByName = matierePremiereRepository.findByNomIgnoreCase(rawValue);
            if (foundByName.isPresent()) {
                return foundByName.get();
            }
        }

        var foundByFuzzyMatch = matierePremiereRepository.findAll().stream()
                .filter(matiere -> {
                    String candidateReferenceKey = normalizeLookupKey(matiere.getReference());
                    String candidateNameKey = normalizeLookupKey(matiere.getNom());
                    return (!lookupKey.isBlank() && lookupKey.equals(candidateReferenceKey))
                            || (!lookupKey.isBlank() && lookupKey.equals(candidateNameKey));
                })
                .findFirst();

        if (foundByFuzzyMatch.isPresent()) {
            return foundByFuzzyMatch.get();
        }

        throw new RuntimeException("Matiere premiere non trouvee");
    }

    //recupere une matiere premiere par ID + utilisable dans les autres methodes existantes
    public MatierePremiere findMatiere(Long id) {
        return matierePremiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matiere premiere non trouvee"));
    }

    private String normalizeName(String value) {
        String normalized = value == null ? null : value.trim();
        return (normalized == null || normalized.isEmpty()) ? null : normalized;
    }

    private String normalizeReference(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("[\\s,;:]+$", "")
                .toUpperCase();
    }

    private Long extractTrailingNumericId(String normalizedReference) {
        if (normalizedReference == null || normalizedReference.isBlank()) {
            return null;
        }

        String digitsOnly = normalizedReference.replaceAll("^\\D+", "");
        if (digitsOnly.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(digitsOnly);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeLookupKey(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "");
    }

}


