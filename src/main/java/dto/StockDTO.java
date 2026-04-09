package dto;

import lombok.Data;

@Data
public class StockDTO {
    private Long idStock;
    private String reference;
    private Long huilerieId;
    private String typeStock;
    private Long referenceId;
    private Double quantiteDisponible;
}
