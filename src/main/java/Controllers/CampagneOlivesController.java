package Controllers;

import Config.PermissionAction;
import Config.RequirePermission;
import Services.CampagneOlivesService;
import dto.CampagneOlivesCreateDTO;
import dto.CampagneOlivesDTO;
import dto.CampagneOlivesUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campagnes")
@RequiredArgsConstructor
public class CampagneOlivesController {

    private final CampagneOlivesService campagneOlivesService;

    @PostMapping
    @RequirePermission(module = "CAMPAGNE_OLIVES", action = PermissionAction.CREATE)
    public ResponseEntity<CampagneOlivesDTO> create(@Valid @RequestBody CampagneOlivesCreateDTO dto) {
        return new ResponseEntity<>(campagneOlivesService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{idCampagne}")
    @RequirePermission(module = "CAMPAGNE_OLIVES", action = PermissionAction.UPDATE)
    public ResponseEntity<CampagneOlivesDTO> update(
            @PathVariable Long idCampagne,
            @Valid @RequestBody CampagneOlivesUpdateDTO dto) {
        return ResponseEntity.ok(campagneOlivesService.update(idCampagne, dto));
    }

    @DeleteMapping("/{idCampagne}")
    @RequirePermission(module = "CAMPAGNE_OLIVES", action = PermissionAction.DELETE)
    public ResponseEntity<Void> delete(@PathVariable Long idCampagne) {
        campagneOlivesService.delete(idCampagne);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idCampagne}")
    @RequirePermission(module = "CAMPAGNE_OLIVES", action = PermissionAction.READ)
    public ResponseEntity<CampagneOlivesDTO> findById(@PathVariable Long idCampagne) {
        return ResponseEntity.ok(campagneOlivesService.findDTOById(idCampagne));
    }

    @GetMapping
    @RequirePermission(module = "CAMPAGNE_OLIVES", action = PermissionAction.READ)
    public ResponseEntity<List<CampagneOlivesDTO>> findAll(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(campagneOlivesService.findAll(reference, huilerieNom));
    }

    @GetMapping("/reference/{reference}")
    @RequirePermission(module = "CAMPAGNE_OLIVES", action = PermissionAction.READ)
    public ResponseEntity<CampagneOlivesDTO> findByReference(@PathVariable String reference) {
        return ResponseEntity.ok(campagneOlivesService.findByReference(reference));
    }
}
