package Services;

import Mapper.MachineMapper;
import Models.Huilerie;
import Models.Machine;
import Repositories.HuilerieRepository;
import Repositories.MachineRepository;
import dto.MachineRawMaterialAssignmentDTO;
import dto.MachineCreateDTO;
import dto.MachineDTO;
import dto.MachineUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MachineService {

    private final MachineRepository machineRepository;
    private final HuilerieRepository huilerieRepository;
    private final MatierePremiereService matierePremiereService;
    private final MachineMapper machineMapper;

    public MachineDTO create(MachineCreateDTO dto) {
        Machine machine = machineMapper.toEntity(dto);
        Huilerie huilerie = findHuilerieByNom(dto.getHuilerieNom());
        machine.setHuilerie(huilerie);

        Machine saved = machineRepository.save(machine);
        return machineMapper.toDTO(saved);
    }

    public MachineDTO update(Long idMachine, MachineUpdateDTO dto) {
        Machine machine = machineRepository.findById(idMachine)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));

        machineMapper.updateFromDTO(dto, machine);

        if (dto.getHuilerieNom() != null) {
            Huilerie huilerie = findHuilerieByNom(dto.getHuilerieNom());
            machine.setHuilerie(huilerie);
        }

        Machine saved = machineRepository.save(machine);
        return machineMapper.toDTO(saved);
    }

    public void delete(Long idMachine) {
        Machine machine = machineRepository.findById(idMachine)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        machineRepository.delete(machine);
    }

    public MachineDTO findById(Long idMachine) {
        Machine machine = machineRepository.findById(idMachine)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        return machineMapper.toDTO(machine);
    }

    public List<MachineDTO> findAll() {
        return machineRepository.findAll().stream()
                .map(machineMapper::toDTO)
                .toList();
    }

    public List<MachineDTO> findByHuilerie(String huilerieNom) {
        return machineRepository.findByHuilerieNom(huilerieNom).stream()
                .map(machineMapper::toDTO)
                .toList();
    }

    //affectation du matiere premiere a une machine
    public MachineDTO assignMatierePremiere(Long machineId, MachineRawMaterialAssignmentDTO dto) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        machine.setMatierePremiere(matierePremiereService.findMatiere(dto.getMatierePremiereId()));
        return machineMapper.toDTO(machineRepository.save(machine));
    }

    private Huilerie findHuilerieByNom(String huilerieNom) {
        return huilerieRepository.findByNom(huilerieNom)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));
    }
}
