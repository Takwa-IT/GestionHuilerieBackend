package dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AnalyseLaboratoireDTO {
    private Long idAnalyse;
    private String reference;
    @JsonAlias({"acidite", "aciditeHuilePourcent"})
    private Double acidite_huile_pourcent;
    @JsonAlias({"indicePeroxyde", "indicePeroxydeMeqO2Kg"})
    private Double indice_peroxyde_meq_o2_kg;
    @JsonAlias({"polyphenolsMgKg", "polyphenols"})
    private Double polyphenols_mg_kg;
    private Double k232;
    private Double k270;
    private String dateAnalyse;
    private Long lotId;
}


