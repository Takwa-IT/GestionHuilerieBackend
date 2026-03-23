package Controllers;

import Services.TraceabilityService;
import dto.LotTraceabilityDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traceability")
@RequiredArgsConstructor
public class TraceabilityController {

    private final TraceabilityService traceabilityService;

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<LotTraceabilityDTO> getLotHistory(@PathVariable Long lotId) {
        return ResponseEntity.ok(traceabilityService.getLotHistory(lotId));
    }
}
