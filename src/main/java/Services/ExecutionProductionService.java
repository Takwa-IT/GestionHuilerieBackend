package Services;

import Models.ExecutionProduction;
import Models.LotOlives;
import Models.Machine;
import Models.MatierePremiere;
import Models.ParametreEtape;
import Models.ValeurReelleParametre;
import Repositories.ExecutionProductionRepository;
import Repositories.GuideProductionRepository;
import Repositories.LotOlivesRepository;
import Repositories.MachineRepository;
import Repositories.MatierePremiereRepository;
import Repositories.ParametreEtapeRepository;
import dto.ExecutionProductionCreateDTO;
import dto.ExecutionProductionDTO;
import dto.ValeurReelleParametreCreateDTO;
import dto.ValeurReelleParametreDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExecutionProductionService {

    private final ExecutionProductionRepository executionProductionRepository;
    private final GuideProductionRepository guideProductionRepository;
    private final MachineRepository machineRepository;
    private final LotOlivesRepository lotOlivesRepository;
    private final MatierePremiereRepository matierePremiereRepository;
    private final ParametreEtapeRepository parametreEtapeRepository;

    public ExecutionProductionDTO create(ExecutionProductionCreateDTO dto) {
        var guideProduction = guideProductionRepository.findById(dto.getGuideProductionId())
                .orElseThrow(() -> new RuntimeException("Guide de production non trouve"));
        Machine machine = machineRepository.findById(dto.getMachineId())
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        LotOlives lotOlives = lotOlivesRepository.findById(dto.getLotOlivesId())
                .orElseThrow(() -> new RuntimeException("Lot d'olives non trouve"));
        MatierePremiere matierePremiere = matierePremiereRepository.findById(dto.getMatierePremiereId())
                .orElseThrow(() -> new RuntimeException("Matiere premiere non trouvee"));

        machine.setMatierePremiere(matierePremiere);
        machineRepository.save(machine);

        ExecutionProduction executionProduction = new ExecutionProduction();
        executionProduction.setCodeLot(dto.getCodeLot());
        executionProduction.setDateDebut(dto.getDateDebut());
        executionProduction.setDateFinPrevue(dto.getDateFinPrevue());
        executionProduction.setDateFinReelle(dto.getDateFinReelle());
        executionProduction.setStatut(dto.getStatut());
        executionProduction.setRendement(dto.getRendement());
        executionProduction.setObservations(dto.getObservations());
        executionProduction.setGuideProduction(guideProduction);
        executionProduction.setMachine(machine);
        executionProduction.setLotOlives(lotOlives);
        executionProduction.setMatierePremiere(matierePremiere);

        if (dto.getValeursReelles() != null) {
            List<ValeurReelleParametre> valeursReelles = new ArrayList<>();
            for (ValeurReelleParametreCreateDTO valeurDTO : dto.getValeursReelles()) {
                ParametreEtape parametreEtape = parametreEtapeRepository.findById(valeurDTO.getParametreEtapeId())
                        .orElseThrow(() -> new RuntimeException("Parametre d'etape non trouve"));

                ValeurReelleParametre valeurReelle = new ValeurReelleParametre();
                valeurReelle.setValeurReelle(valeurDTO.getValeurReelle());
                valeurReelle.setParametreEtape(parametreEtape);
                valeurReelle.setExecutionProduction(executionProduction);
                valeursReelles.add(valeurReelle);
            }
            executionProduction.setValeursReelles(valeursReelles);
        }

        return toDTO(executionProductionRepository.save(executionProduction));
    }

    @Transactional(readOnly = true)
    public ExecutionProductionDTO findById(Long idExecutionProduction) {
        return toDTO(findExecution(idExecutionProduction));
    }

    @Transactional(readOnly = true)
    public List<ExecutionProductionDTO> findAll() {
        return executionProductionRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ExecutionProduction findExecution(Long idExecutionProduction) {
        return executionProductionRepository.findById(idExecutionProduction)
                .orElseThrow(() -> new RuntimeException("Execution de production non trouvee"));
    }

    private ExecutionProductionDTO toDTO(ExecutionProduction executionProduction) {
        ExecutionProductionDTO dto = new ExecutionProductionDTO();
        dto.setIdExecutionProduction(executionProduction.getIdExecutionProduction());
        dto.setCodeLot(executionProduction.getCodeLot());
        dto.setDateDebut(executionProduction.getDateDebut());
        dto.setDateFinPrevue(executionProduction.getDateFinPrevue());
        dto.setDateFinReelle(executionProduction.getDateFinReelle());
        dto.setStatut(executionProduction.getStatut());
        dto.setRendement(executionProduction.getRendement());
        dto.setObservations(executionProduction.getObservations());

        if (executionProduction.getGuideProduction() != null) {
            dto.setGuideProductionId(executionProduction.getGuideProduction().getIdGuideProduction());
            dto.setGuideProductionReference(executionProduction.getGuideProduction().getReference());
        }
        if (executionProduction.getMachine() != null) {
            dto.setMachineId(executionProduction.getMachine().getIdMachine());
            dto.setMachineNom(executionProduction.getMachine().getNomMachine());
        }
        if (executionProduction.getLotOlives() != null) {
            dto.setLotOlivesId(executionProduction.getLotOlives().getIdLot());
            dto.setLotOlivesVariete(executionProduction.getLotOlives().getVarieteOlive());
        }
        if (executionProduction.getMatierePremiere() != null) {
            dto.setMatierePremiereId(executionProduction.getMatierePremiere().getId());
            dto.setMatierePremiereNom(executionProduction.getMatierePremiere().getNom());
        }
        if (executionProduction.getProduitFinal() != null) {
            dto.setProduitFinalId(executionProduction.getProduitFinal().getIdProduit());
            dto.setProduitFinalReference(executionProduction.getProduitFinal().getReference());
            dto.setProduitFinalNomProduit(executionProduction.getProduitFinal().getNomProduit());
        }
        if (executionProduction.getValeursReelles() != null) {
            dto.setValeursReelles(executionProduction.getValeursReelles().stream().map(this::toDTO).toList());
        }
        return dto;
    }

    private ValeurReelleParametreDTO toDTO(ValeurReelleParametre valeurReelleParametre) {
        ValeurReelleParametreDTO dto = new ValeurReelleParametreDTO();
        dto.setIdValeurReelleParametre(valeurReelleParametre.getIdValeurReelleParametre());
        dto.setValeurReelle(valeurReelleParametre.getValeurReelle());
        if (valeurReelleParametre.getParametreEtape() != null) {
            dto.setParametreEtapeId(valeurReelleParametre.getParametreEtape().getIdParametreEtape());
            dto.setParametreEtapeNom(valeurReelleParametre.getParametreEtape().getNom());
        }
        return dto;
    }
}
