package Services;

import Config.ReferenceUtils;
import Models.EtapeProduction;
import Models.GuideProduction;
import Models.Huilerie;
import Models.Machine;
import Models.ParametreEtape;
import Models.Utilisateur;
import Repositories.GuideProductionRepository;
import Repositories.HuilerieRepository;
import Repositories.MachineRepository;
import dto.EtapeProductionCreateDTO;
import dto.EtapeProductionDTO;
import dto.GuideProductionCreateDTO;
import dto.GuideProductionDTO;
import dto.ParametreEtapeCreateDTO;
import dto.ParametreEtapeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GuideProductionService {

    private final GuideProductionRepository guideProductionRepository;
    private final HuilerieRepository huilerieRepository;
    private final MachineRepository machineRepository;
    private final CurrentUserService currentUserService;

    public GuideProductionDTO create(GuideProductionCreateDTO dto) {
        Huilerie huilerie = huilerieRepository.findById(dto.getHuilerieId())
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));
        currentUserService.ensureCanAccessHuilerie(huilerie.getIdHuilerie());

        GuideProduction guideProduction = new GuideProduction();
        applyRequestToGuide(guideProduction, dto, huilerie, true);

        GuideProduction saved = guideProductionRepository.save(guideProduction);
        if (saved.getIdGuideProduction() != null) {
            saved.setReference(ReferenceUtils.format("GP", saved.getIdGuideProduction()));
            saved = guideProductionRepository.save(saved);
        }

        return toDTO(saved);
    }

    public GuideProductionDTO update(Long idGuideProduction, GuideProductionCreateDTO dto) {
        GuideProduction guideProduction = findGuide(idGuideProduction);
        Huilerie huilerie = huilerieRepository.findById(dto.getHuilerieId())
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));
        currentUserService.ensureCanAccessHuilerie(huilerie.getIdHuilerie());

        applyRequestToGuide(guideProduction, dto, huilerie, false);
        return toDTO(guideProductionRepository.save(guideProduction));
    }

    public void delete(Long idGuideProduction) {
        guideProductionRepository.delete(findGuide(idGuideProduction));
    }

    @Transactional(readOnly = true)
    public GuideProductionDTO findById(Long idGuideProduction) {
        return toDTO(findGuide(idGuideProduction));
    }

    @Transactional(readOnly = true)
    public List<GuideProductionDTO> findAll(String huilerieNom) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();

        List<GuideProduction> guides;
        if (currentUserService.isAdmin(utilisateur)) {
            guides = hasText(huilerieNom)
                    ? guideProductionRepository.findByHuilerie_NomIgnoreCase(huilerieNom)
                    : guideProductionRepository.findAll();
            guides = guides.stream()
                    .filter(guide -> guide.getHuilerie() != null
                            && currentUserService.getAccessibleHuilerieIds()
                                    .contains(guide.getHuilerie().getIdHuilerie()))
                    .toList();
        } else {
            guides = guideProductionRepository
                    .findByHuilerie_IdHuilerie(currentUserService.getCurrentHuilerieIdOrThrow());
        }

        return guides.stream()
                .map(this::toDTO)
                .toList();
    }

    public GuideProduction findGuide(Long idGuideProduction) {
        return guideProductionRepository.findById(idGuideProduction)
                .orElseThrow(() -> new RuntimeException("Guide de production non trouve"));
    }

    private void applyRequestToGuide(GuideProduction guideProduction, GuideProductionCreateDTO dto, Huilerie huilerie,
            boolean replaceEtapesCollection) {
        guideProduction.setNom(dto.getNom());
        guideProduction.setDescription(dto.getDescription());
        guideProduction.setDateCreation(dto.getDateCreation());
        guideProduction.setHuilerie(huilerie);
        guideProduction.setTypeMachine(normalizeTypeMachine(dto.getTypeMachine()));

        List<EtapeProduction> etapes = dto.getEtapes() != null && !dto.getEtapes().isEmpty()
                ? buildManualEtapes(guideProduction, dto.getEtapes())
                : buildTemplateEtapes(guideProduction, dto.getTypeMachine());

        if (replaceEtapesCollection || guideProduction.getEtapes() == null) {
            guideProduction.setEtapes(etapes);
            return;
        }

        guideProduction.getEtapes().clear();
        guideProduction.getEtapes().addAll(etapes);
    }

    private GuideProductionDTO toDTO(GuideProduction guideProduction) {
        GuideProductionDTO dto = new GuideProductionDTO();
        dto.setIdGuideProduction(guideProduction.getIdGuideProduction());
        dto.setReference(guideProduction.getReference());
        dto.setNom(guideProduction.getNom());
        dto.setDescription(guideProduction.getDescription());
        dto.setDateCreation(guideProduction.getDateCreation());
        dto.setTypeMachine(guideProduction.getTypeMachine());
        if (guideProduction.getHuilerie() != null) {
            dto.setHuilerieId(guideProduction.getHuilerie().getIdHuilerie());
            dto.setHuilerieNom(guideProduction.getHuilerie().getNom());
        }
        if (guideProduction.getEtapes() != null) {
            dto.setEtapes(guideProduction.getEtapes().stream().map(this::toDTO).toList());
        }
        return dto;
    }

    private EtapeProductionDTO toDTO(EtapeProduction etapeProduction) {
        EtapeProductionDTO dto = new EtapeProductionDTO();
        dto.setIdEtapeProduction(etapeProduction.getIdEtapeProduction());
        dto.setNom(etapeProduction.getNom());
        dto.setOrdre(etapeProduction.getOrdre());
        dto.setDescription(etapeProduction.getDescription());
        dto.setCodeEtape(etapeProduction.getCodeEtape());
        dto.setMachineId(etapeProduction.getMachine() != null ? etapeProduction.getMachine().getIdMachine() : null);
        if (etapeProduction.getParametres() != null) {
            dto.setParametres(etapeProduction.getParametres().stream().map(this::toDTO).toList());
        }
        return dto;
    }

    private ParametreEtapeDTO toDTO(ParametreEtape parametreEtape) {
        ParametreEtapeDTO dto = new ParametreEtapeDTO();
        dto.setIdParametreEtape(parametreEtape.getIdParametreEtape());
        dto.setNom(parametreEtape.getNom());
        dto.setCodeParametre(parametreEtape.getCodeParametre());
        dto.setUniteMesure(parametreEtape.getUniteMesure());
        dto.setDescription(parametreEtape.getDescription());
        dto.setValeur(parametreEtape.getValeur());
        dto.setValeurReelle(null);
        return dto;
    }

    private List<EtapeProduction> buildTemplateEtapes(GuideProduction guideProduction, String typeMachine) {
        List<EtapeProduction> etapes = new ArrayList<>();
        for (EtapeProductionCreateDTO etapeDTO : GuideProductionTemplateFactory.buildDefaultEtapes(typeMachine)) {
            etapes.add(mapEtapeToEntity(guideProduction, etapeDTO));
        }
        return etapes;
    }

    private List<EtapeProduction> buildManualEtapes(GuideProduction guideProduction,
            List<EtapeProductionCreateDTO> etapeDTOs) {
        List<EtapeProduction> etapes = new ArrayList<>();
        if (etapeDTOs == null) {
            return etapes;
        }

        for (EtapeProductionCreateDTO etapeDTO : etapeDTOs) {
            etapes.add(mapEtapeToEntity(guideProduction, etapeDTO));
        }
        return etapes;
    }

    private EtapeProduction mapEtapeToEntity(GuideProduction guideProduction, EtapeProductionCreateDTO etapeDTO) {
        EtapeProduction etape = new EtapeProduction();
        etape.setNom(etapeDTO.getNom());
        etape.setOrdre(etapeDTO.getOrdre());
        etape.setDescription(etapeDTO.getDescription());
        etape.setCodeEtape(hasText(etapeDTO.getCodeEtape())
                ? normalizeCodeEtapeValue(etapeDTO.getCodeEtape())
                : normalizeCodeEtape(etapeDTO.getNom(), etapeDTO.getOrdre()));
        etape.setGuideProduction(guideProduction);

        if (etapeDTO.getMachineId() != null) {
            Machine machine = machineRepository.findById(etapeDTO.getMachineId())
                    .orElseThrow(() -> new RuntimeException("Machine non trouvee: " + etapeDTO.getMachineId()));

            if (machine.getHuilerie() == null || guideProduction.getHuilerie() == null
                    || !machine.getHuilerie().getIdHuilerie().equals(guideProduction.getHuilerie().getIdHuilerie())) {
                throw new IllegalArgumentException(
                        "La machine selectionnee n'appartient pas a la meme huilerie que le guide.");
            }

            etape.setMachine(machine);
        }

        List<ParametreEtape> parametres = new ArrayList<>();
        if (etapeDTO.getParametres() != null) {
            for (ParametreEtapeCreateDTO parametreDTO : etapeDTO.getParametres()) {
                ParametreEtape parametre = new ParametreEtape();
                parametre.setNom(parametreDTO.getNom());
                parametre.setCodeParametre(parametreDTO.getCodeParametre());
                parametre.setUniteMesure(parametreDTO.getUniteMesure());
                parametre.setDescription(parametreDTO.getDescription());
                parametre.setValeur(parametreDTO.getValeur());
                parametre.setEtapeProduction(etape);
                parametres.add(parametre);
            }
        }
        etape.setParametres(parametres);
        return etape;
    }

    private String normalizeCodeEtapeValue(String codeEtape) {
        String normalized = codeEtape.trim().toLowerCase();
        normalized = normalized.replaceAll("[^a-z0-9_]+", "_").replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Le codeEtape est invalide");
        }
        return normalized;
    }

    private String normalizeTypeMachine(String value) {
        String normalized = GuideProductionTemplateFactory.normalizeTypeMachine(value);
        if (!hasText(normalized)) {
            throw new IllegalArgumentException("Le type de machine est obligatoire pour le guide de production.");
        }
        return normalized;
    }

    private String normalizeCodeEtape(String nom, Integer ordre) {
        String normalized = nom == null ? "" : nom.trim().toLowerCase();
        normalized = normalized.replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (normalized.isBlank()) {
            return ordre == null ? "etape" : "etape_" + ordre;
        }
        return normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
