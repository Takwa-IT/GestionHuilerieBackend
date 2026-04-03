package Controllers;

import Services.ExecutionProductionService;
import dto.ExecutionProductionCreateDTO;
import dto.ExecutionProductionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/execution-productions")
@RequiredArgsConstructor
public class ExecutionProductionController {

    private final ExecutionProductionService executionProductionService;

    @PostMapping
    public ResponseEntity<ExecutionProductionDTO> create(@Valid @RequestBody ExecutionProductionCreateDTO dto) {
        return new ResponseEntity<>(executionProductionService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{idExecutionProduction}")
    public ResponseEntity<ExecutionProductionDTO> findById(@PathVariable Long idExecutionProduction) {
        return ResponseEntity.ok(executionProductionService.findById(idExecutionProduction));
    }

    @GetMapping
    public ResponseEntity<List<ExecutionProductionDTO>> findAll() {
        return ResponseEntity.ok(executionProductionService.findAll());
    }
}