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

    @NotNull
    private Long guideProductionId;

    @NotNull
    private Long lotId;
}
