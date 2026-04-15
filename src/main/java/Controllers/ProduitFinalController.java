package Controllers;

import Services.ProduitFinalService;
import dto.ExecutionProductionDTO;
import dto.ProduitFinalCreateDTO;
import dto.ProduitFinalDTO;
import dto.ProduitFinalUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/produitsFinaux")
@RequiredArgsConstructor
@Valid
public class ProduitFinalController {

    private final ProduitFinalService produitFinalService;

    @PostMapping
    public ResponseEntity<ExecutionProductionDTO> create(@Valid @RequestBody ProduitFinalCreateDTO dto) {
        return new ResponseEntity<>(produitFinalService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{idProduit}")
    public ResponseEntity<ProduitFinalDTO> update(@PathVariable Long idProduit, @RequestBody ProduitFinalUpdateDTO dto) {
        return ResponseEntity.ok(produitFinalService.update(idProduit, dto));
    }

    @DeleteMapping("/{idProduit}")
    public ResponseEntity<Void> delete(@PathVariable Long idProduit) {
        produitFinalService.delete(idProduit);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idProduit}")
    public ResponseEntity<ProduitFinalDTO> findById(@PathVariable Long idProduit) {
        return ResponseEntity.ok(produitFinalService.findById(idProduit));
    }

    @GetMapping
    public ResponseEntity<List<ProduitFinalDTO>> findAll() {
        return ResponseEntity.ok(produitFinalService.findAll());
    }
}


