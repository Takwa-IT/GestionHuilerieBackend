package Controllers;

import Services.AdminModuleService;
import dto.ApiResponseDTO;
import dto.ModuleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final AdminModuleService adminModuleService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ModuleDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminModuleService.findAll(), "Liste des modules"));
    }
}