package dto;

import Models.TypeMouvement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockMovementTypeUpdateDTO {
    @NotNull
    private TypeMouvement typeMouvement;

    @NotNull
    @Positive
    private Double quantite;
}
