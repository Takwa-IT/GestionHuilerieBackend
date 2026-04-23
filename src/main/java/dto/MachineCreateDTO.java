package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MachineCreateDTO {

    @NotBlank
    private String nomMachine;

    @NotBlank
    @Pattern(regexp = "^(2_phase|3_phase|presse)$", message = "typeMachine doit etre 2_phase, 3_phase ou presse")
    private String typeMachine;

    @NotBlank
    private String etatMachine;

    @NotNull
    private Integer capacite;

    @NotBlank
    private String huilerieNom;
}


