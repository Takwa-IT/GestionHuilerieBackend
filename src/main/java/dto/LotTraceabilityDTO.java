package dto;

import lombok.Data;

import java.util.List;

@Data
public class LotTraceabilityDTO {
    private Long lotId;
    private String varieteOlive;
    private String origine;
    private Double quantiteInitiale;
    private Double quantiteRestante;
    private List<PeseeItem> pesees;
    private List<AnalyseItem> analyses;
    private List<LifecycleItem> cycleVie;

    @Data
    public static class PeseeItem {
        private String reference;
        private String date;
        private Double poidsBrut;
        private Double poidsTare;
        private Double poidsNet;
    }

    @Data
    public static class AnalyseItem {
        private Long idAnalyse;
        private String date;
        private Double acidite;
        private Double indicePeroxyde;
        private Double k232;
        private Double k270;
        private String classeQualiteFinale;
    }

    @Data
    public static class LifecycleItem {
        private String date;
        private String etape;
        private String description;
        private String reference;
    }
}
