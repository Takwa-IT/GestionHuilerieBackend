package dto;

import Models.TypeMouvement;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockMovementTypeUpdateDTO {
    @NotNull
    private TypeMouvement typeMouvement;
}
