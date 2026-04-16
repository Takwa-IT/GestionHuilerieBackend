package Controllers;

import Services.EmployeService;
import dto.EmployeCreateDTO;
import dto.EmployeDTO;
import dto.EmployeUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employes")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;

    @PostMapping
    public ResponseEntity<EmployeDTO> create(@Valid @RequestBody EmployeCreateDTO dto) {
        return new ResponseEntity<>(employeService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{idEmploye}")
    public ResponseEntity<EmployeDTO> getById(@PathVariable Long idEmploye) {
        return ResponseEntity.ok(employeService.getById(idEmploye));
    }

    @GetMapping
    public ResponseEntity<List<EmployeDTO>> getAll() {
        return ResponseEntity.ok(employeService.getAll());
    }

    @GetMapping("/huilerie/{idHuilerie}")
    public ResponseEntity<List<EmployeDTO>> getByHuilerie(@PathVariable Long idHuilerie) {
        return ResponseEntity.ok(employeService.getByHuilerie(idHuilerie));
    }

    @PutMapping("/{idEmploye}")
    public ResponseEntity<EmployeDTO> update(@PathVariable Long idEmploye, @Valid @RequestBody EmployeUpdateDTO dto) {
        return ResponseEntity.ok(employeService.update(idEmploye, dto));
    }

    @DeleteMapping("/{idEmploye}")
    public ResponseEntity<Void> delete(@PathVariable Long idEmploye) {
        employeService.delete(idEmploye);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<EmployeDTO> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(employeService.getByEmail(email));
    }
}
