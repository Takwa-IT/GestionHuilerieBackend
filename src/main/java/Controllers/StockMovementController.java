package Controllers;

import Services.StockMovementService;
import dto.StockMovementCreateDTO;
import dto.StockMovementDTO;
import dto.StockMovementUpdateDTO;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Models.LotOlives;

import java.util.List;

@RestController
@RequestMapping("/api/stockMouvements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    public ResponseEntity<StockMovementDTO> create(@Valid @RequestBody StockMovementCreateDTO dto) {
        return new ResponseEntity<>(stockMovementService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{idStockMovement}")
    public ResponseEntity<StockMovementDTO> update(
            @PathVariable Long idStockMovement,
            @Valid @RequestBody StockMovementUpdateDTO dto) {
        return ResponseEntity.ok(stockMovementService.update(idStockMovement, dto));
    }

    @GetMapping
    public ResponseEntity<List<StockMovementDTO>> findAll(@RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(stockMovementService.findAll(huilerieNom));
    }

    @GetMapping("/huilerie/{huilerieId}")
    public ResponseEntity<List<StockMovementDTO>> findByHuilerie(@PathVariable Long huilerieId) {
        return ResponseEntity.ok(stockMovementService.findByHuilerie(huilerieId));
    }

    @ManyToOne
    @JoinColumn(name = "lot_id")
    private LotOlives lotOlives;

    public LotOlives getLotOlives() {
        return lotOlives;
    }

    public void setLotOlives(LotOlives lotOlives) {
        this.lotOlives = lotOlives;
    }
}
