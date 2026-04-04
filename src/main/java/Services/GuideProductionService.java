package Services;

import Models.EtapeProduction;
import Models.GuideProduction;
import Models.Huilerie;
import Models.ParametreEtape;
import Repositories.GuideProductionRepository;
import Repositories.HuilerieRepository;
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

    public GuideProductionDTO create(GuideProductionCreateDTO dto) {
        Huilerie huilerie = huilerieRepository.findById(dto.getHuilerieId())
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));

        GuideProduction guideProduction = new GuideProduction();
        guideProduction.setNom(dto.getNom());
        guideProduction.setDescription(dto.getDescription());
        guideProduction.setDateCreation(dto.getDateCreation());
        guideProduction.setHuilerie(huilerie);

        if (dto.getEtapes() != null) {
            List<EtapeProduction> etapes = new ArrayList<>();
            for (EtapeProductionCreateDTO etapeDTO : dto.getEtapes()) {
                EtapeProduction etape = new EtapeProduction();
                etape.setNom(etapeDTO.getNom());
                etape.setOrdre(etapeDTO.getOrdre());
                etape.setDescription(etapeDTO.getDescription());
                etape.setGuideProduction(guideProduction);

                if (etapeDTO.getParametres() != null) {
                    List<ParametreEtape> parametres = new ArrayList<>();
                    for (ParametreEtapeCreateDTO parametreDTO : etapeDTO.getParametres()) {
                        ParametreEtape parametre = new ParametreEtape();
                        parametre.setNom(parametreDTO.getNom());
                        parametre.setUniteMesure(parametreDTO.getUniteMesure());
                        parametre.setDescription(parametreDTO.getDescription());
                        parametre.setValeur(parametreDTO.getValeur());
                        parametre.setEtapeProduction(etape);
                        parametres.add(parametre);
                    }
                    etape.setParametres(parametres);
                }

                etapes.add(etape);
            }
            guideProduction.setEtapes(etapes);
        }

        GuideProduction saved = guideProductionRepository.save(guideProduction);
        if (saved.getReference() == null && saved.getIdGuideProduction() != null) {
            saved.setReference("GP" + saved.getIdGuideProduction());
            saved = guideProductionRepository.save(saved);
        }

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public GuideProductionDTO findById(Long idGuideProduction) {
        return toDTO(findGuide(idGuideProduction));
    }

    @Transactional(readOnly = true)
    public List<GuideProductionDTO> findAll() {
        return guideProductionRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public GuideProduction findGuide(Long idGuideProduction) {
        return guideProductionRepository.findById(idGuideProduction)
                .orElseThrow(() -> new RuntimeException("Guide de production non trouve"));
    }

    private GuideProductionDTO toDTO(GuideProduction guideProduction) {
        GuideProductionDTO dto = new GuideProductionDTO();
        dto.setIdGuideProduction(guideProduction.getIdGuideProduction());
        dto.setReference(guideProduction.getReference());
        dto.setNom(guideProduction.getNom());
        dto.setDescription(guideProduction.getDescription());
        dto.setDateCreation(guideProduction.getDateCreation());
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
        if (etapeProduction.getParametres() != null) {
            dto.setParametres(etapeProduction.getParametres().stream().map(this::toDTO).toList());
        }
        return dto;
    }

    private ParametreEtapeDTO toDTO(ParametreEtape parametreEtape) {
        ParametreEtapeDTO dto = new ParametreEtapeDTO();
        dto.setIdParametreEtape(parametreEtape.getIdParametreEtape());
        dto.setNom(parametreEtape.getNom());
        dto.setUniteMesure(parametreEtape.getUniteMesure());
        dto.setDescription(parametreEtape.getDescription());
        dto.setValeur(parametreEtape.getValeur());
        return dto;
    }
}