package dto;

import lombok.Data;

@Data
public class PeseeDTO {
    private String reference;
    private Long lotId;
    private String datePesee;
    private Double poidsBrut;
    private Double poidsTare;
    private Double poidsNet;
}
