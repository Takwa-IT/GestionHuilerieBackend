package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValeurReelleParametreDTO {
    private Long idValeurReelleParametre;
    private Long executionProductionId;
    private Long parametreEtapeId;
    private String nomParametre;
    private String uniteMesure;
    private Double valeurReelle;
    private Double deviation;
    private String qualiteDeviation;
    private String dateCreation;
    private String dateModification;
    private Boolean isOutsideTolerance;
    private Boolean isSignificantDeviation;
}