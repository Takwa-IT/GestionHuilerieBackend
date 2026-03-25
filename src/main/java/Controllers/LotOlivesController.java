package Controllers;

import Services.LotOlivesService;
import dto.LotOlivesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
public class LotOlivesController {

    private final LotOlivesService lotOlivesService;

    @GetMapping
    public ResponseEntity<List<LotOlivesDTO>> findAll() {
        return ResponseEntity.ok(lotOlivesService.findAll());
    }

    @GetMapping("/{idLot}")
    public ResponseEntity<LotOlivesDTO> findById(@PathVariable Long idLot) {
        return ResponseEntity.ok(lotOlivesService.findById(idLot));
    }
}
