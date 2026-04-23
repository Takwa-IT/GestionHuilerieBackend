package dto;

import lombok.Data;

@Data
public class PredictionDTO {

    private Long idPrediction;
    private String modePrediction;
    private String qualitePredite;
    private Double probabiliteQualite;
    private Double rendementPreditPourcent;
    private Double quantiteHuileRecalculeeLitres;
    private Long executionProductionId;
    private String dateCreation;
}
