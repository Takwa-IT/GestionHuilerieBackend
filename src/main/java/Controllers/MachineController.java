package Controllers;

import Services.MachineService;
import dto.MachineCreateDTO;
import dto.MachineDTO;
import dto.MachineUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    @PostMapping
    public ResponseEntity<MachineDTO> create(@Valid @RequestBody MachineCreateDTO dto) {
        return new ResponseEntity<>(machineService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MachineDTO> update(@PathVariable Long id, @Valid @RequestBody MachineUpdateDTO dto) {
        return ResponseEntity.ok(machineService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        machineService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MachineDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(machineService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<MachineDTO>> findAll() {
        return ResponseEntity.ok(machineService.findAll());
    }

    @GetMapping("/huilerie/{huilerieId}")
    public ResponseEntity<List<MachineDTO>> findByHuilerie(@PathVariable Long huilerieId) {
        return ResponseEntity.ok(machineService.findByHuilerie(huilerieId));
    }
}
