package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecutionProductionCreateDTO {

    @NotBlank
    private String reference;

    @NotBlank
    private String dateDebut;

    @NotBlank
    private String dateFinPrevue;

    private String dateFinReelle;

    @NotBlank
    private String statut;

    private Double rendement;

    private Boolean controleTemperature;

    private String observations;

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

    @NotNull
    private Long guideProductionId;

    @NotNull
    private Long lotId;
}


