package dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ExecutionProductionCreateDTO {

    @NotBlank
    private String codeLot;

    @NotBlank
    private String dateDebut;

    @NotBlank
    private String dateFinPrevue;

    private String dateFinReelle;

    @NotBlank
    private String statut;

    private Double rendement;

    private String observations;

    @NotNull
    private Long guideProductionId;

    @NotNull
    private Long machineId;

    @NotNull
    private Long lotOlivesId;

    @NotNull
    private Long matierePremiereId;

    @Valid
    private List<ValeurReelleParametreCreateDTO> valeursReelles;
}