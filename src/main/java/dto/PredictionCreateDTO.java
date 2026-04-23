package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PredictionCreateDTO {

    @NotBlank
    private String modePrediction;

    private String qualitePredite;

    private Double probabiliteQualite;

    private Double rendementPreditPourcent;

    private Double quantiteHuileRecalculeeLitres;

    @NotNull
    private Long executionProductionId;
}
