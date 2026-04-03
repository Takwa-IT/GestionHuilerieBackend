package dto;

import lombok.Data;

import java.util.List;

@Data
public class ExecutionProductionDTO {

    private Long idExecutionProduction;
    private String codeLot;
    private String dateDebut;
    private String dateFinPrevue;
    private String dateFinReelle;
    private String statut;
    private Double rendement;
    private String observations;
    private Long guideProductionId;
    private String guideProductionCode;
    private Long machineId;
    private String machineNom;
    private Long lotOlivesId;
    private String lotOlivesVariete;
    private Long matierePremiereId;
    private String matierePremiereNom;
    private Long produitFinalId;
    private String produitFinalNomProduit;
    private List<ValeurReelleParametreDTO> valeursReelles;
}