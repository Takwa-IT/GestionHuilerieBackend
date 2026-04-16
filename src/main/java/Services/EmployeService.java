package Services;

import Mapper.EmployeMapper;
import Models.Employe;
import Models.Huilerie;
import Repositories.EmployeRepository;
import Repositories.HuilerieRepository;
import dto.EmployeCreateDTO;
import dto.EmployeDTO;
import dto.EmployeUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final HuilerieRepository huilerieRepository;
    private final EmployeMapper employeMapper;

    public EmployeDTO create(EmployeCreateDTO dto) {
        if (employeRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Un employe avec cet email existe deja");
        }

        Employe employe = employeMapper.toEntity(dto);
        Huilerie huilerie = huilerieRepository.findById(dto.getHuilerieEmpId())
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));
        
        employe.setHuilerieEmp(huilerie);
        employe.setEmail(dto.getEmail());
        employe.setMotDePasse(dto.getMotDePasse());
        employe.setNom(dto.getNom());
        employe.setPrenom(dto.getPrenom());
        employe.setTelephone(dto.getTelephone());

        Employe saved = employeRepository.save(employe);
        saved.setIdEmploye(saved.getIdUtilisateur());
        saved = employeRepository.save(saved);
        return employeMapper.toDTO(saved);
    }

    public EmployeDTO getById(Long idEmploye) {
        Employe employe = employeRepository.findById(idEmploye)
                .orElseThrow(() -> new RuntimeException("Employe non trouve"));
        return employeMapper.toDTO(employe);
    }

    public List<EmployeDTO> getAll() {
        return employeRepository.findAll().stream()
                .map(employeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeDTO> getByHuilerie(Long idHuilerie) {
        return employeRepository.findByHuilerieEmpIdHuilerie(idHuilerie).stream()
                .map(employeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public EmployeDTO update(Long idEmploye, EmployeUpdateDTO dto) {
        Employe employe = employeRepository.findById(idEmploye)
                .orElseThrow(() -> new RuntimeException("Employe non trouve"));

        employeMapper.updateFromDTO(dto, employe);

        if (dto.getHuilerieEmpId() != null) {
            Huilerie huilerie = huilerieRepository.findById(dto.getHuilerieEmpId())
                    .orElseThrow(() -> new RuntimeException("Huilerie non trouvee"));
            employe.setHuilerieEmp(huilerie);
        }

        Employe saved = employeRepository.save(employe);
        return employeMapper.toDTO(saved);
    }

    public void delete(Long idEmploye) {
        if (!employeRepository.existsById(idEmploye)) {
            throw new RuntimeException("Employe non trouve");
        }
        employeRepository.deleteById(idEmploye);
    }

    public EmployeDTO getByEmail(String email) {
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employe non trouve"));
        return employeMapper.toDTO(employe);
    }
}
