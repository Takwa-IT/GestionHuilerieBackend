package dto;

import lombok.Data;

@Data
public class MachineDTO {

    private Long idMachine;
    private String nomMachine;
    private String typeMachine;
    private String etatMachine;
    private Integer capacite;
    private String huilerieNom;
}
