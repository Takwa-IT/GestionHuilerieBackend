package Controllers;

import Config.PermissionAction;
import Config.RequirePermission;
import Services.LotOlivesService;
import dto.LotArrivageCreateDTO;
import dto.LotOlivesDTO;
import dto.LotOlivesUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/arrivages")
    @RequirePermission(module = "RECEPTION", action = PermissionAction.READ)
    public ResponseEntity<List<LotOlivesDTO>> findAllArrivages(@RequestParam(required = false) String huilerieNom) {
        return ResponseEntity.ok(lotService.findAll(huilerieNom));
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

    @PutMapping("/{idLot}")
    @RequirePermission(module = "LOTS_TRAÇABILITE", action = PermissionAction.UPDATE)
    public ResponseEntity<LotOlivesDTO> update(
            @PathVariable Long idLot,
            @Valid @RequestBody LotOlivesUpdateDTO dto) {
        return ResponseEntity.ok(lotService.update(idLot, dto));
    }

    @DeleteMapping("/{idLot}")
    @RequirePermission(module = "LOTS_TRAÇABILITE", action = PermissionAction.DELETE)
    public ResponseEntity<Void> delete(@PathVariable Long idLot) {
        lotService.delete(idLot);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/bon-pesee/pdf")
    @RequirePermission(module = "RECEPTION", action = PermissionAction.READ)
    public ResponseEntity<byte[]> downloadBonPeseeByReference(@RequestParam String reference) {
        byte[] pdf = lotService.generateBonPeseePdf(reference);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("bon-pesee-" + reference + ".pdf")
                .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PostMapping("/{idLot}/bon-pesee/pdf/upload")
    @RequirePermission(module = "RECEPTION", action = PermissionAction.CREATE)
    public ResponseEntity<LotOlivesDTO> uploadBonPeseePdf(
            @PathVariable Long idLot,
            @RequestParam("file") MultipartFile file) {
        LotOlivesDTO result = lotService.uploadBonPeseePdf(idLot, file);
        return ResponseEntity.ok(result);
    }
}