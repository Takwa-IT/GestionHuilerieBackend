package Controllers;

import Services.ExecutionProductionService;
import dto.ExecutionProductionCreateDTO;
import dto.ExecutionProductionDTO;
import dto.ValeurReelleParametreDTO;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/build-code-lot/{lotId}")
    public ResponseEntity<String> buildCodeLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(executionProductionService.buildCodeLotForLot(lotId));
    }

    @GetMapping("/{idExecutionProduction}")
    public ResponseEntity<ExecutionProductionDTO> findById(@PathVariable Long idExecutionProduction) {
        return ResponseEntity.ok(executionProductionService.findById(idExecutionProduction));
    }

    @GetMapping
    public ResponseEntity<List<ExecutionProductionDTO>> findAll(@RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(executionProductionService.findAll(huilerieNom));
    }

        @PostMapping("/{id}/valeurs-reelles")
        public void saveValeursReelles(
                @PathVariable Long id,
                @RequestBody List<ValeurReelleParametreDTO> valeurs
        ) {
            executionProductionService.saveValeursReelles(id, valeurs);
        }
    }



