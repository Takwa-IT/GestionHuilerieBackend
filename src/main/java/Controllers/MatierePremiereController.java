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

    @PutMapping("/{idMatierePremiere}")
    @RequirePermission(module = "MATIERES_PREMIERES", action = PermissionAction.UPDATE)
    public ResponseEntity<MatierePremiereDTO> update(@PathVariable Long idMatierePremiere, @RequestBody MatierePremiereUpdateDTO dto) {
        return ResponseEntity.ok(matierePremiereService.update(idMatierePremiere, dto));
    }

    @DeleteMapping("/{idMatierePremiere}")
    public ResponseEntity<Void> delete(@PathVariable Long idMatierePremiere) {
        matierePremiereService.delete(idMatierePremiere);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idMatierePremiere}")
    public ResponseEntity<MatierePremiereDTO> findById(@PathVariable Long idMatierePremiere) {
        return ResponseEntity.ok(matierePremiereService.findById(idMatierePremiere));
    }

    @GetMapping
    public ResponseEntity<List<MatierePremiereDTO>> findAll() {
        return ResponseEntity.ok(matierePremiereService.findAll());
    }
}
