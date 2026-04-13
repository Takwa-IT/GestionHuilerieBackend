package Controllers;

import Services.AdminProfilService;
import dto.ApiResponseDTO;
import dto.ProfilDTO;
import dto.ProfilRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/profils")
@RequiredArgsConstructor
public class ProfilController {

    private final AdminProfilService adminProfilService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ProfilDTO>>> findAll(@RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminProfilService.findAll(huilerieNom), "Liste des profils"));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<ProfilDTO>> create(@Valid @RequestBody ProfilRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminProfilService.create(request), "Profil cree"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProfilDTO>> update(@PathVariable Long id, @Valid @RequestBody ProfilRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminProfilService.update(id, request), "Profil modifie"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {
        adminProfilService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.ok(null, "Profil supprime"));
    }
}