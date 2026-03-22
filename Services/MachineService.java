package Services;

import Mapper.MachineMapper;
import Models.Huilerie;
import Models.Machine;
import Repositories.HuilerieRepository;
import Repositories.MachineRepository;
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
    private final MachineMapper machineMapper;

    public MachineDTO create(MachineCreateDTO dto) {
        Machine machine = machineMapper.toEntity(dto);
        Huilerie huilerie = findHuilerie(dto.getHuilerieId());
        machine.setHuilerie(huilerie);

        Machine saved = machineRepository.save(machine);
        return machineMapper.toDTO(saved);
    }

    public MachineDTO update(Long id, MachineUpdateDTO dto) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));

        machineMapper.updateFromDTO(dto, machine);

        if (dto.getHuilerieId() != null) {
            Huilerie huilerie = findHuilerie(dto.getHuilerieId());
            machine.setHuilerie(huilerie);
        }

        Machine saved = machineRepository.save(machine);
        return machineMapper.toDTO(saved);
    }

    public void delete(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        machineRepository.delete(machine);
    }

    @Transactional(readOnly = true)
    public MachineDTO findById(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        return machineMapper.toDTO(machine);
    }

    public List<MachineDTO> findAll() {
        return machineRepository.findAll().stream()
                .map(machineMapper::toDTO)
                .toList();
    }

    public List<MachineDTO> findByHuilerie(Long huilerieId) {
        return machineRepository.findByIdHuilerie(huilerieId).stream()
                .map(machineMapper::toDTO)
                .toList();
    }

    private Huilerie findHuilerie(Long huilerieId) {
        return huilerieRepository.findById(huilerieId)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));
    }
}
