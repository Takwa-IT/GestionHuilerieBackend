package Controllers;

import Services.PeseeService;
import dto.PeseeDTO;
import dto.ReceptionPeseeCreateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pesees")
@RequiredArgsConstructor
public class PeseeController {

    private final PeseeService peseeService;

    @PostMapping
    public ResponseEntity<PeseeDTO> createReception(@Valid @RequestBody ReceptionPeseeCreateDTO dto) {
        return new ResponseEntity<>(peseeService.createReception(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeseeDTO> updateReception(@PathVariable Long id, @Valid @RequestBody ReceptionPeseeCreateDTO dto) {
        return ResponseEntity.ok(peseeService.updateReception(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReception(@PathVariable Long id) {
        peseeService.deleteReception(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reference}")
    public ResponseEntity<PeseeDTO> findByReference(@PathVariable String reference) {
        return ResponseEntity.ok(peseeService.findByReference(reference));
    }

    @GetMapping
    public ResponseEntity<List<PeseeDTO>> findAll() {
        return ResponseEntity.ok(peseeService.findAll());
    }

    @GetMapping("/{reference}/pdf")
    public ResponseEntity<byte[]> generateBonPesee(@PathVariable String reference) {
        byte[] pdf = peseeService.generateBonPeseePdf(reference);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("bon-pesee-" + reference + ".pdf").build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
