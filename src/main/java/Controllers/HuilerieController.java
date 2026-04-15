package Controllers;

import Services.HuilerieService;
import dto.HuilerieCreateDTO;
import dto.HuilerieDTO;
import dto.HuilerieUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/huileries")
@RequiredArgsConstructor
public class HuilerieController {

    private final HuilerieService huilerieService;

    @PostMapping
    public ResponseEntity<HuilerieDTO> create(@Valid @RequestBody HuilerieCreateDTO dto) {
        return new ResponseEntity<>(huilerieService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{idHuilerie}")
    public ResponseEntity<HuilerieDTO> update(@PathVariable Long idHuilerie, @Valid @RequestBody HuilerieUpdateDTO dto) {
        return ResponseEntity.ok(huilerieService.update(idHuilerie, dto));
    }

    @PatchMapping("/{idHuilerie}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long idHuilerie) {
        huilerieService.activate(idHuilerie);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idHuilerie}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long idHuilerie) {
        huilerieService.deactivate(idHuilerie);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idHuilerie}")
    public ResponseEntity<HuilerieDTO> findById(@PathVariable Long idHuilerie) {
        return ResponseEntity.ok(huilerieService.findById(idHuilerie));
    }

    @GetMapping
    public ResponseEntity<List<HuilerieDTO>> findAll() {
        return ResponseEntity.ok(huilerieService.findAll());
    }
}


