package Controllers;

import Services.StockService;
import dto.StockDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockDTO>> findAll(@RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(stockService.findAll(huilerieNom));
    }

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<List<StockDTO>> findByLot(
            @PathVariable Long lotId,
            @RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(stockService.findByLot(lotId, huilerieNom));
    }

    @GetMapping("/resolve")
    public ResponseEntity<StockDTO> resolveByLotAndHuilerie(
            @RequestParam Long lotId,
            @RequestParam Long huilerieId) {
        return ResponseEntity.ok(stockService.findByLotAndHuilerie(lotId, huilerieId));
    }

    @GetMapping("/huilerie/{huilerieId}")
    public ResponseEntity<List<StockDTO>> findAllByHuilerieId(@PathVariable Long huilerieId) {
        return ResponseEntity.ok(stockService.findAllByHuilerieId(huilerieId));
    }

    @GetMapping("/{idStock}")
    public ResponseEntity<StockDTO> findById(@PathVariable Long idStock) {
        return ResponseEntity.ok(stockService.findById(idStock));
    }
}
