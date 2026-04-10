package Controllers;

import Services.AdminUtilisateurService;
import dto.ApiResponseDTO;
import dto.UtilisateurAdminDTO;
import dto.UtilisateurAdminRequestDTO;
import dto.UtilisateurAdminUpdateRequestDTO;
import dto.UtilisateurStatusUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final AdminUtilisateurService adminUtilisateurService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<UtilisateurAdminDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminUtilisateurService.findAll(), "Liste des utilisateurs"));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<UtilisateurAdminDTO>> create(@Valid @RequestBody UtilisateurAdminRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminUtilisateurService.create(request), "Utilisateur cree"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UtilisateurAdminDTO>> update(@PathVariable Long id, @Valid @RequestBody UtilisateurAdminRequestDTO request) {        return ResponseEntity.ok(ApiResponseDTO.ok(adminUtilisateurService.update(id, request), "Utilisateur modifie"));
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<ApiResponseDTO<UtilisateurAdminDTO>> updateStatus(@PathVariable Long id, @Valid @RequestBody UtilisateurStatusUpdateDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminUtilisateurService.updateStatus(id, request), "Statut utilisateur mis a jour"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        adminUtilisateurService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.ok(null, "Utilisateur supprime"));
    }
}
