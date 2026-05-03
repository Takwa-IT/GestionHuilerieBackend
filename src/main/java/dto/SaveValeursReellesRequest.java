package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveValeursReellesRequest {

    @NotNull(message = "L'ID d'exécution ne peut pas être nul")
    private Long executionProductionId;

    @Valid
    @NotNull(message = "La liste des valeurs réelles ne peut pas être nulle")
    private List<ValeurReelleInput> valeursReelles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValeurReelleInput {

        @NotNull(message = "L'ID du paramètre ne peut pas être nul")
        private Long parametreEtapeId;

        @NotNull(message = "La valeur réelle ne peut pas être nulle")
        @jakarta.validation.constraints.DecimalMin("0.0")
        private Double valeurReelle;

        private String uniteMesure;
    }
}
