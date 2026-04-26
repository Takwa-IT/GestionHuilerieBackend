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
    private String region;
    private String methodeRecolte;
    private String typeSol;
    private Double temperatureMalaxageC;
    private Double dureeMalaxageMin;
    private Double vitesseDecanteurTrMin;
    private Double humiditePourcent;
    private Double aciditeOlivesPourcent;
    private Double tauxFeuillesPourcent;
    private Double pressionExtractionBar;
    private Long huilerieId;
    private String huilerieNom;
    private Long guideProductionId;
    private String guideProductionReference;
    private Long machineId;
    private String machineNom;
    private Long lotId;
    private String lotVariete;
    private Long produitFinalId;
    private String produitFinalReference;
    private String produitFinalNomProduit;
    private List<ValeurReelleParametreDTO> valeursReelles;
    private List<PredictionDTO> predictions;
}
