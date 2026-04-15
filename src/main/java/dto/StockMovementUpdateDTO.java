package dto;

import Models.TypeMouvement;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockMovementUpdateDTO {
    @NotNull
    private Long lotId;

    @NotNull
    private Long huilerieId;

    private String commentaire;
    private String dateMouvement;

    @NotNull
    private TypeMouvement typeMouvement;
}
