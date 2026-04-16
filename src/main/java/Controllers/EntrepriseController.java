package Controllers;

import Services.EntrepriseService;
import dto.EntrepriseCreateDTO;
import dto.EntrepriseDTO;
import dto.EntrepriseUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entreprises")
@RequiredArgsConstructor
public class EntrepriseController {

    private final EntrepriseService entrepriseService;

    @PostMapping
    public ResponseEntity<EntrepriseDTO> create(@Valid @RequestBody EntrepriseCreateDTO dto) {
        return new ResponseEntity<>(entrepriseService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{idEntreprise}")
    public ResponseEntity<EntrepriseDTO> getById(@PathVariable Long idEntreprise) {
        return ResponseEntity.ok(entrepriseService.getById(idEntreprise));
    }

    @GetMapping
    public ResponseEntity<List<EntrepriseDTO>> getAll() {
        return ResponseEntity.ok(entrepriseService.getAll());
    }

    @PutMapping("/{idEntreprise}")
    public ResponseEntity<EntrepriseDTO> update(@PathVariable Long idEntreprise, @Valid @RequestBody EntrepriseUpdateDTO dto) {
        return ResponseEntity.ok(entrepriseService.update(idEntreprise, dto));
    }

    @DeleteMapping("/{idEntreprise}")
    public ResponseEntity<Void> delete(@PathVariable Long idEntreprise) {
        entrepriseService.delete(idEntreprise);
        return ResponseEntity.noContent().build();
    }
}
