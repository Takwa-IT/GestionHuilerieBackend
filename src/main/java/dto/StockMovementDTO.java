package dto;

import Models.TypeMouvement;
import lombok.Data;

@Data
public class StockMovementDTO {
    private Long id;
    private Long huilerieId;
    private Long referenceId;
    private Double quantite;
    private String commentaire;
    private String dateMouvement;
    private TypeMouvement typeMouvement;
}
