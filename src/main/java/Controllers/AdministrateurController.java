package Controllers;

import Services.AdministrateurService;
import dto.AdministrateurCreateDTO;
import dto.AdministrateurDTO;
import dto.AdministrateurUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administrateurs")
@RequiredArgsConstructor
public class AdministrateurController {

    private final AdministrateurService administrateurService;

    @PostMapping
    public ResponseEntity<AdministrateurDTO> create(@Valid @RequestBody AdministrateurCreateDTO dto) {
        return new ResponseEntity<>(administrateurService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{idAdministrateur}")
    public ResponseEntity<AdministrateurDTO> getById(@PathVariable Long idAdministrateur) {
        return ResponseEntity.ok(administrateurService.getById(idAdministrateur));
    }

    @GetMapping
    public ResponseEntity<List<AdministrateurDTO>> getAll() {
        return ResponseEntity.ok(administrateurService.getAll());
    }

    @PutMapping("/{idAdministrateur}")
    public ResponseEntity<AdministrateurDTO> update(@PathVariable Long idAdministrateur, @Valid @RequestBody AdministrateurUpdateDTO dto) {
        return ResponseEntity.ok(administrateurService.update(idAdministrateur, dto));
    }

    @DeleteMapping("/{idAdministrateur}")
    public ResponseEntity<Void> delete(@PathVariable Long idAdministrateur) {
        administrateurService.delete(idAdministrateur);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<AdministrateurDTO> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(administrateurService.getByEmail(email));
    }

    @GetMapping("/entreprise/{idEntreprise}")
    public ResponseEntity<AdministrateurDTO> getByEntrepriseId(@PathVariable Long idEntreprise) {
        return ResponseEntity.ok(administrateurService.getByEntrepriseId(idEntreprise));
    }
}
