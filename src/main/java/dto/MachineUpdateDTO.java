package dto;

import lombok.Data;

@Data
public class MachineUpdateDTO {

    private String nomMachine;
    private String typeMachine;
    private String etatMachine;
    private Integer capacite;
    private String huilerieNom;
}
