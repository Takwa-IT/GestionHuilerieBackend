package dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MachineUpdateDTO {

    private String nomMachine;

    @Pattern(regexp = "^(2_phase|3_phase|presse)$", message = "typeMachine doit etre 2_phase, 3_phase ou presse")
    private String typeMachine;

    private String etatMachine;
    private Integer capacite;
    private String huilerieNom;
}


