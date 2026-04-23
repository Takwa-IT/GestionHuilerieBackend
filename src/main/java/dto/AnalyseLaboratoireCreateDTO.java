package dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalyseLaboratoireCreateDTO {
    @NotNull
    @JsonProperty("acidite_huile_pourcent")
    @JsonAlias({"acidite", "aciditeHuilePourcent"})
    private Double acidite_huile_pourcent;

    @NotNull
    @JsonProperty("indice_peroxyde_meq_o2_kg")
    @JsonAlias({"indicePeroxyde", "indicePeroxydeMeqO2Kg"})
    private Double indice_peroxyde_meq_o2_kg;

    @JsonProperty("polyphenols_mg_kg")
    @JsonAlias({"polyphenolsMgKg", "polyphenols"})
    private Double polyphenols_mg_kg;

    @NotNull
    @JsonProperty("k232")
    private Double k232;

    @NotNull
    @JsonProperty("k270")
    private Double k270;
    @JsonProperty("dateAnalyse")
    @JsonAlias({"date_analyse"})
    private String dateAnalyse;

    @NotNull
    @JsonProperty("lotId")
    @JsonAlias({"lot_id"})
    private Long lotId;
}


