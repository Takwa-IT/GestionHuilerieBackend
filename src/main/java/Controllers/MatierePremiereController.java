package Controllers;

import Config.PermissionAction;
import Config.RequirePermission;
import Services.MatierePremiereService;
import dto.MatierePremiereCreateDTO;
import dto.MatierePremiereDTO;
import dto.MatierePremiereUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matieresPremieres")
@RequiredArgsConstructor
@Valid
public class MatierePremiereController {

    private final MatierePremiereService matierePremiereService;

    @PostMapping
    public ResponseEntity<MatierePremiereDTO> create(@Valid @RequestBody MatierePremiereCreateDTO dto) {
        return new ResponseEntity<>(matierePremiereService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{reference}")
    @RequirePermission(module = "MATIERES_PREMIERES", action = PermissionAction.UPDATE)
    public ResponseEntity<MatierePremiereDTO> update(@PathVariable String reference, @RequestBody MatierePremiereUpdateDTO dto) {
        return ResponseEntity.ok(matierePremiereService.update(reference, dto));
    }

    @DeleteMapping("/{reference}")
    public ResponseEntity<Void> delete(@PathVariable String reference) {
        matierePremiereService.delete(reference);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reference}")
    public ResponseEntity<MatierePremiereDTO> findByReference(@PathVariable String reference) {
        return ResponseEntity.ok(matierePremiereService.findByReference(reference));
    }

    @GetMapping
    public ResponseEntity<List<MatierePremiereDTO>> findAll(@RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(matierePremiereService.findAll(huilerieNom));
    }
}


