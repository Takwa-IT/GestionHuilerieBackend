package Controllers;

import Config.PermissionAction;
import Config.RequirePermission;
import Services.LotOlivesService;
import dto.LotOlivesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
public class LotOlivesController {

    private final LotOlivesService lotOlivesService;

    @GetMapping
    @RequirePermission(module = "LOTS_TRAÇABILITE", action = PermissionAction.READ)
    public ResponseEntity<List<LotOlivesDTO>> findAll(@RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(lotOlivesService.findAll(huilerieNom));
    }

    @GetMapping("/{idLot}")
    public ResponseEntity<LotOlivesDTO> findById(@PathVariable Long idLot) {
        return ResponseEntity.ok(lotOlivesService.findById(idLot));
    }
}
