package dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MachineRawMaterialAssignmentDTO {
    @NotNull
    private Long matierePremiereId;
}
