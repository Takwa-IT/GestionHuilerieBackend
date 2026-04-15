package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MachineCreateDTO {

    @NotBlank
    private String nomMachine;

    @NotBlank
    private String typeMachine;

    @NotBlank
    private String etatMachine;

    @NotNull
    private Integer capacite;

    @NotBlank
    private String huilerieNom;
}


