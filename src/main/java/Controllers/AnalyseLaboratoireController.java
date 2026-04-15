package Controllers;

import Services.AnalyseLaboratoireService;
import dto.AnalyseLaboratoireCreateDTO;
import dto.AnalyseLaboratoireDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysesLaboratoire")
@RequiredArgsConstructor
public class AnalyseLaboratoireController {

    private final AnalyseLaboratoireService analyseLaboratoireService;

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<List<AnalyseLaboratoireDTO>> findByLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(analyseLaboratoireService.findByLot(lotId));
    }

    @PostMapping
    public ResponseEntity<AnalyseLaboratoireDTO> create(@Valid @RequestBody AnalyseLaboratoireCreateDTO dto) {
        return new ResponseEntity<>(analyseLaboratoireService.create(dto), HttpStatus.CREATED);
    }
}


