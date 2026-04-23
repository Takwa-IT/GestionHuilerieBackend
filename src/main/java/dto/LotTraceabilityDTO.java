package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LotTraceabilityDTO {
    private Long lotId;

    @JsonProperty("variete")
    private String varieteOlive;

    private String origine;
    private String region;

    @JsonProperty("methode_recolte")
    private String methodeRecolte;

    @JsonProperty("type_sol")
    private String typeSol;

    @JsonProperty("temps_depuis_recolte_heures")
    private Integer tempsDepuisRecolteHeures;

    private Double quantiteInitiale;
    private Double quantiteRestante;
    private List<AnalyseItem> analyses;
    private List<LifecycleItem> cycleVie;

    @Data
    public static class AnalyseItem {
        private Long idAnalyse;
        private String date;
        private Double acidite_huile_pourcent;
        private Double indice_peroxyde_meq_o2_kg;
        private Double polyphenols_mg_kg;
        private Double k232;
        private Double k270;
    }

    @Data
    public static class LifecycleItem {
        private String date;
        private String etape;
        private String description;
        private String reference;
    }
}
