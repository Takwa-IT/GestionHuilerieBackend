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

    @GetMapping("/{idPesee}")
    public ResponseEntity<PeseeDTO> findById(@PathVariable Long idPesee) {
        return ResponseEntity.ok(peseeService.findById(idPesee));
    }

    @GetMapping
    public ResponseEntity<List<PeseeDTO>> findAll() {
        return ResponseEntity.ok(peseeService.findAll());
    }

    @GetMapping("/{idPesee}/pdf")
    public ResponseEntity<byte[]> generateBonPesee(@PathVariable Long idPesee) {
        byte[] pdf = peseeService.generateBonPeseePdf(idPesee);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("bon-pesee-" + idPesee + ".pdf").build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
