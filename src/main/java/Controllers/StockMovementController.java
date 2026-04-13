package Controllers;

import Config.PermissionAction;
import Config.RequirePermission;
import Services.StockMovementService;
import dto.StockMovementCreateDTO;
import dto.StockMovementDTO;
import dto.StockMovementUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stockMovements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    @RequirePermission(module = "STOCK_MOUVEMENT", action = PermissionAction.CREATE)
    public ResponseEntity<StockMovementDTO> create(@Valid @RequestBody StockMovementCreateDTO dto) {
        return new ResponseEntity<>(stockMovementService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{idStockMovement}")
    @RequirePermission(module = "STOCK_MOUVEMENT", action = PermissionAction.UPDATE)
    public ResponseEntity<StockMovementDTO> update(
            @PathVariable Long idStockMovement,
            @Valid @RequestBody StockMovementUpdateDTO dto) {
        return ResponseEntity.ok(stockMovementService.update(idStockMovement, dto));
    }

    @GetMapping
    @RequirePermission(module = "STOCK_MOUVEMENT", action = PermissionAction.READ)
    public ResponseEntity<List<StockMovementDTO>> findAll(@RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(stockMovementService.findAll(huilerieNom));
    }

    @GetMapping("/huilerie/{huilerieId}")
    @RequirePermission(module = "STOCK_MOUVEMENT", action = PermissionAction.READ)
    public ResponseEntity<List<StockMovementDTO>> findByHuilerie(@PathVariable Long huilerieId) {
        return ResponseEntity.ok(stockMovementService.findByHuilerie(huilerieId));
    }
}
