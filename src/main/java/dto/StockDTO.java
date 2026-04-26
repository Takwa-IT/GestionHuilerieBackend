package dto;

import lombok.Data;
import java.util.List;

@Data
public class StockDTO {
    private Long idStock;
    private String reference;
    private Long huilerieId;
    private String huilerieNom;
    private String typeStock;
    private String variete;
    private Long referenceId;
    private Double quantiteDisponible;
    private Long matierePremiereId;
    private List<String> lotReferences;
}


