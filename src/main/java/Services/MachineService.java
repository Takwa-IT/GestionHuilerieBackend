package Services;

import Config.ReferenceUtils;
import Mapper.MachineMapper;
import Models.Huilerie;
import Models.Machine;
import Models.Utilisateur;
import Repositories.HuilerieRepository;
import Repositories.MachineRepository;
import dto.MachineRawMaterialAssignmentDTO;
import dto.MachineCreateDTO;
import dto.MachineDTO;
import dto.MachineUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MachineService {

    private final MachineRepository machineRepository;
    private final HuilerieRepository huilerieRepository;
    private final MatierePremiereService matierePremiereService;
    private final MachineMapper machineMapper;
    private final CurrentUserService currentUserService;

    public MachineDTO create(MachineCreateDTO dto) {
        Machine machine = machineMapper.toEntity(dto);
        machine.setTypeMachine(normalizeMachineType(machine.getTypeMachine()));
        machine.setCategorieMachine(normalizeOptionalField(machine.getCategorieMachine()));
        Huilerie huilerie = findHuilerieByNom(dto.getHuilerieNom());
        currentUserService.ensureCanAccessHuilerie(huilerie.getIdHuilerie());
        machine.setHuilerie(huilerie);

        Machine saved = machineRepository.save(machine);
        saved.setReference(ReferenceUtils.format("MC", saved.getIdMachine()));
        saved = machineRepository.save(saved);
        return machineMapper.toDTO(saved);
    }

    public MachineDTO update(Long idMachine, MachineUpdateDTO dto) {
        Machine machine = machineRepository.findById(idMachine)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));

        machineMapper.updateFromDTO(dto, machine);

        if (dto.getTypeMachine() != null) {
            machine.setTypeMachine(normalizeMachineType(dto.getTypeMachine()));
        }

        if (dto.getCategorieMachine() != null) {
            machine.setCategorieMachine(normalizeOptionalField(dto.getCategorieMachine()));
        }

        if (dto.getHuilerieNom() != null) {
            Huilerie huilerie = findHuilerieByNom(dto.getHuilerieNom());
            currentUserService.ensureCanAccessHuilerie(huilerie.getIdHuilerie());
            machine.setHuilerie(huilerie);
        }

        Machine saved = machineRepository.save(machine);
        return machineMapper.toDTO(saved);
    }

    public void delete(Long idMachine) {
        desactiver(idMachine);
    }

    public MachineDTO activer(Long idMachine) {
        Machine machine = machineRepository.findById(idMachine)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        machine.setEtatMachine("EN_SERVICE");
        return machineMapper.toDTO(machineRepository.save(machine));
    }

    public MachineDTO desactiver(Long idMachine) {
        Machine machine = machineRepository.findById(idMachine)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        machine.setEtatMachine("DESACTIVEE");
        return machineMapper.toDTO(machineRepository.save(machine));
    }

    public MachineDTO findById(Long idMachine) {
        Machine machine = machineRepository.findById(idMachine)
                .orElseThrow(() -> new RuntimeException("Machine non trouvee"));
        return machineMapper.toDTO(machine);
    }

    public List<MachineDTO> findAll(String huilerieNom, String typeMachine) {
        String normalizedType = hasText(typeMachine) ? normalizeMachineType(typeMachine) : null;
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<Machine> machines;
            if (hasText(huilerieNom) && normalizedType != null) {
                machines = machineRepository.findByHuilerie_NomIgnoreCaseAndTypeMachineIgnoreCase(huilerieNom,
                        normalizedType);
            } else if (hasText(huilerieNom)) {
                machines = machineRepository.findByHuilerie_NomIgnoreCase(huilerieNom);
            } else if (normalizedType != null) {
                machines = machineRepository.findByTypeMachineIgnoreCase(normalizedType);
            } else {
                machines = machineRepository.findAll();
            }
            return machines.stream()
                    .filter(machine -> machine.getHuilerie() != null
                            && currentUserService.getAccessibleHuilerieIds()
                                    .contains(machine.getHuilerie().getIdHuilerie()))
                    .map(machineMapper::toDTO)
                    .toList();
        }

        Long idHuilerie = currentUserService.getCurrentHuilerieIdOrThrow();
        List<Machine> machines = normalizedType != null
                ? machineRepository.findByHuilerie_IdHuilerieAndTypeMachineIgnoreCase(idHuilerie, normalizedType)
                : machineRepository.findByHuilerie_IdHuilerie(idHuilerie);

        return machines.stream()
                .map(machineMapper::toDTO)
                .toList();
    }

    public List<MachineDTO> findByHuilerie(String huilerieNom) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (!currentUserService.isAdmin(utilisateur)) {
            String currentHuilerieNom = utilisateur.getHuilerie() != null ? utilisateur.getHuilerie().getNom() : null;
            if (currentHuilerieNom == null || !currentHuilerieNom.equalsIgnoreCase(huilerieNom)) {
                throw new AccessDeniedException("Acces refuse a une autre huilerie");
            }
        }

        return machineRepository.findByHuilerieNom(huilerieNom).stream()
                .filter(machine -> machine.getHuilerie() != null
                        && currentUserService.getAccessibleHuilerieIds()
                                .contains(machine.getHuilerie().getIdHuilerie()))
                .map(machineMapper::toDTO)
                .toList();
    }

    // affectation du matiere premiere a une machine
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeMachineType(String value) {
        if (!hasText(value)) {
            throw new IllegalArgumentException("Le type de machine est obligatoire.");
        }
        return value.trim().toLowerCase();
    }

    private String normalizeOptionalField(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
