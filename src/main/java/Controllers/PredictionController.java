package Controllers;

import Services.PredictionService;
import dto.PredictionCreateDTO;
import dto.PredictionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping
    public ResponseEntity<PredictionDTO> create(@Valid @RequestBody PredictionCreateDTO dto) {
        return new ResponseEntity<>(predictionService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PredictionDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(predictionService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<PredictionDTO>> findAll() {
        return ResponseEntity.ok(predictionService.findAll());
    }

    @GetMapping("/execution/{executionId}")
    public ResponseEntity<List<PredictionDTO>> findByExecutionProductionId(@PathVariable Long executionId) {
        return ResponseEntity.ok(predictionService.findByExecutionProductionId(executionId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PredictionDTO> update(@PathVariable Long id, @Valid @RequestBody PredictionCreateDTO dto) {
        return ResponseEntity.ok(predictionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        predictionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
