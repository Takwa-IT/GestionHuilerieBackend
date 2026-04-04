package Services;

import Models.Module;
import Repositories.ModuleRepository;
import dto.ModuleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminModuleService {

    private final ModuleRepository moduleRepository;

    public List<ModuleDTO> findAll() {
        return moduleRepository.findAll().stream().map(this::toDTO).toList();
    }

    private ModuleDTO toDTO(Module module) {
        ModuleDTO dto = new ModuleDTO();
        dto.setIdModule(module.getIdModule());
        dto.setNom(module.getNom());
        return dto;
    }
}