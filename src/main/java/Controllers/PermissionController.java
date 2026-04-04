package Controllers;

import Services.AdminPermissionService;
import dto.ApiResponseDTO;
import dto.BulkPermissionUpsertRequestDTO;
import dto.PermissionModuleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final AdminPermissionService adminPermissionService;

    @GetMapping("/profil/{profilId}")
    public ResponseEntity<ApiResponseDTO<List<PermissionModuleDTO>>> findByProfil(@PathVariable Long profilId) {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminPermissionService.findByProfil(profilId), "Permissions du profil"));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponseDTO<List<PermissionModuleDTO>>> bulkUpsert(@Valid @RequestBody BulkPermissionUpsertRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminPermissionService.bulkUpsert(request), "Permissions mises a jour"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        adminPermissionService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.ok(null, "Permission supprimee"));
    }
}