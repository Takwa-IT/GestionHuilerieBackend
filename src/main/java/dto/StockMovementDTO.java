package dto;

import Models.TypeMouvement;
import lombok.Data;

@Data
public class StockMovementDTO {
    private Long id;
    private String reference;
    private Long huilerieId;
    private String huilerieNom;
    private Long lotId;
    private String commentaire;
    private String dateMouvement;
    private TypeMouvement typeMouvement;
}
