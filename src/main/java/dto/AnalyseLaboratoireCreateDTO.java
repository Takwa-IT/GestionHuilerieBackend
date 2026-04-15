package dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalyseLaboratoireCreateDTO {
    @NotNull
    private Double acidite;

    @NotNull
    private Double indicePeroxyde;

    @NotNull
    private Double k232;

    @NotNull
    private Double k270;

    private String classeQualiteFinale;
    private String dateAnalyse;

    @NotNull
    private Long lotId;
}


