package Controllers;

import Services.GuideProductionService;
import dto.GuideProductionCreateDTO;
import dto.GuideProductionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/guide-productions")
@RequiredArgsConstructor
public class GuideProductionController {

    private final GuideProductionService guideProductionService;

    @PostMapping
    public ResponseEntity<GuideProductionDTO> create(@Valid @RequestBody GuideProductionCreateDTO dto) {
        return new ResponseEntity<>(guideProductionService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{idGuideProduction}")
    public ResponseEntity<GuideProductionDTO> update(
            @PathVariable Long idGuideProduction,
            @Valid @RequestBody GuideProductionCreateDTO dto
    ) {
        return ResponseEntity.ok(guideProductionService.update(idGuideProduction, dto));
    }

    @DeleteMapping("/{idGuideProduction}")
    public ResponseEntity<Void> delete(@PathVariable Long idGuideProduction) {
        guideProductionService.delete(idGuideProduction);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idGuideProduction}")
    public ResponseEntity<GuideProductionDTO> findById(@PathVariable Long idGuideProduction) {
        return ResponseEntity.ok(guideProductionService.findById(idGuideProduction));
    }

    @GetMapping
    public ResponseEntity<List<GuideProductionDTO>> findAll() {
        return ResponseEntity.ok(guideProductionService.findAll());
    }
}
