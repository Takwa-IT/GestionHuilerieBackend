package dto;

import lombok.Data;

import java.util.List;

@Data
public class ExecutionProductionDTO {

    private Long idExecutionProduction;
    private String reference;
    private String dateDebut;
    private String dateFinPrevue;
    private String dateFinReelle;
    private String statut;
    private Double rendement;
    private String observations;
    private Boolean controleTemperature;
    private Long huilerieId;
    private String huilerieNom;
    private Long guideProductionId;
    private String guideProductionReference;
    private Long lotId;
    private String lotVariete;
    private Long produitFinalId;
    private String produitFinalReference;
    private String produitFinalNomProduit;
    private String produitFinalQualite;
    private Double produitFinalQuantiteProduite;
    private List<ValeurReelleParametreDTO> valeursReelles;
    private List<PredictionDTO> predictions;
}
