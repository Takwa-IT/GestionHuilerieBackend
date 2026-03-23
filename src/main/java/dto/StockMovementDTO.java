package dto;

import Models.TypeMouvement;
import lombok.Data;

@Data
public class StockMovementDTO {
    private Long idStockMovement;
    private Long lotId;
    private Double quantite;
    private String commentaire;
    private String dateMouvement;
    private TypeMouvement typeMouvement;
}
