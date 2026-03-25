package dto;

import Models.TypeMouvement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockMovementCreateDTO {
    @NotNull
    private Long referenceId;

    @NotNull
    private Long huilerieId;

    @NotNull
    @Positive
    private Double quantite;

    private String commentaire;
    private String dateMouvement;

    @NotNull
    private TypeMouvement typeMouvement;
}
