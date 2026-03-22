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

    @PutMapping("/{id}")
    public ResponseEntity<HuilerieDTO> update(@PathVariable Long id, @Valid @RequestBody HuilerieUpdateDTO dto) {
        return ResponseEntity.ok(huilerieService.update(id, dto));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        huilerieService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        huilerieService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HuilerieDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(huilerieService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<HuilerieDTO>> findAll() {
        return ResponseEntity.ok(huilerieService.findAll());
    }
}