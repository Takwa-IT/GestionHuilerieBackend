package Controllers;

import Config.PermissionAction;
import Config.RequirePermission;
import Services.LotOlivesService;
import dto.LotArrivageCreateDTO;
import dto.LotOlivesDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
public class LotController {

    private final LotOlivesService lotService;

    @PostMapping("/arrivages")
    @RequirePermission(module = "RECEPTION", action = PermissionAction.CREATE)
    public ResponseEntity<LotOlivesDTO> createArrivage(@Valid @RequestBody LotArrivageCreateDTO dto) {
        return new ResponseEntity<>(lotService.createArrivage(dto), HttpStatus.CREATED);
    }

    @GetMapping
    @RequirePermission(module = "LOTS_TRAÇABILITE", action = PermissionAction.READ)
    public ResponseEntity<List<LotOlivesDTO>> findAll(@RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(lotService.findAll(huilerieNom));
    }

    @GetMapping("/{idLot}")
    public ResponseEntity<LotOlivesDTO> findById(@PathVariable Long idLot) {
        return ResponseEntity.ok(lotService.findById(idLot));
    }

    @GetMapping("/{idLot}/bon-pesee/pdf")
    @RequirePermission(module = "RECEPTION", action = PermissionAction.READ)
    public ResponseEntity<byte[]> downloadBonPesee(@PathVariable Long idLot) {
        LotOlivesDTO lot = lotService.findById(idLot);
        byte[] pdf = lotService.downloadBonPesee(idLot);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("bon-pesee-" + lot.getReference() + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
