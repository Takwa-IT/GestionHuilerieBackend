package dto;

import lombok.Data;

@Data
public class AnalyseLaboratoireDTO {
    private Long idAnalyse;
    private Double acidite;
    private Double indicePeroxyde;
    private Double k232;
    private Double k270;
    private String classeQualiteFinale;
    private String dateAnalyse;
    private Long lotId;
}
