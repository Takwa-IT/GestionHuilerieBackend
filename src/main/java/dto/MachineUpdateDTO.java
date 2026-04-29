package dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MachineUpdateDTO {

    private String nomMachine;

    private String typeMachine;

    private String categorieMachine;

    private String etatMachine;
    private Integer capacite;
    private String huilerieNom;
}
