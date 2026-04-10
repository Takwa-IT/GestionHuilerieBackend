package Services;

import Models.Module;
import Repositories.ModuleRepository;
import dto.ModuleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminModuleService {

    private final ModuleRepository moduleRepository;

    private static final Set<String> REQUIRED_MODULES = new LinkedHashSet<>(List.of(
            "DASHBOARD",
            "RECEPTION",
            "GUIDE_PRODUCTION",
            "MACHINES",
            "MATIERES_PREMIERES",
            "STOCK",
            "STOCK_MOUVEMENT",
            "LOTS_TRAÇABILITE",
            "DASHBOARD_ADMIN",
            "HUILERIES",
            "COMPTES_PROFILS"
    ));

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