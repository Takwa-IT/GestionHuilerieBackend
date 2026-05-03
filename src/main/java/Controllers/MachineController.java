package Controllers;

import Config.PermissionAction;
import Config.RequirePermission;
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
    @RequirePermission(module = "MACHINES", action = PermissionAction.CREATE)
    public ResponseEntity<MachineDTO> create(@Valid @RequestBody MachineCreateDTO dto) {
        return new ResponseEntity<>(machineService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{idMachine}")
    public ResponseEntity<MachineDTO> update(@PathVariable Long idMachine, @Valid @RequestBody MachineUpdateDTO dto) {
        return ResponseEntity.ok(machineService.update(idMachine, dto));
    }

    @DeleteMapping("/{idMachine}")
    public ResponseEntity<Void> delete(@PathVariable Long idMachine) {
        machineService.delete(idMachine);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idMachine}/activer")
    public ResponseEntity<MachineDTO> activer(@PathVariable Long idMachine) {
        return ResponseEntity.ok(machineService.activer(idMachine));
    }

    @PutMapping("/{idMachine}/desactiver")
    public ResponseEntity<MachineDTO> desactiver(@PathVariable Long idMachine) {
        return ResponseEntity.ok(machineService.desactiver(idMachine));
    }

    @GetMapping("/{idMachine}")
    public ResponseEntity<MachineDTO> findById(@PathVariable Long idMachine) {
        return ResponseEntity.ok(machineService.findById(idMachine));
    }

    @GetMapping
    public ResponseEntity<List<MachineDTO>> findAll(
            @RequestParam(required = false) String huilerieNom,
            @RequestParam(required = false) String typeMachine) {
        return ResponseEntity.ok(machineService.findAll(huilerieNom, typeMachine));
    }

    @GetMapping("/huilerie/{huilerieNom}")
    public ResponseEntity<List<MachineDTO>> findByHuilerie(@PathVariable String huilerieNom) {
        return ResponseEntity.ok(machineService.findByHuilerie(huilerieNom));
    }
}





