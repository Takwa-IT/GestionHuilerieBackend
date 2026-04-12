package dto;

import lombok.Data;

@Data
public class PeseeDTO {
    private Long idPesee;
    private String reference;
    private Long lotId;
    private Long huilerieId;
    private String datePesee;
    private Double poidsBrut;
    private Double poidsTare;
    private Double poidsNet;
    private String bonPeseePdfPath;
}
