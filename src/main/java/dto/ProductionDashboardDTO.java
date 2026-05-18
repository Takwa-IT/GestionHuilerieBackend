package dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductionDashboardDTO {

    private GlobalIndicatorsDTO globalIndicators;
    private ReceptionLotsDTO receptionLots;
    private ProductionProcessDTO productionProcess;
    private MachinesDTO machines;
    private QualityDTO quality;
    private StockMovementsDTO stockMovements;

    @Data
    public static class GlobalIndicatorsDTO {
        private Long executionsEnCours;
        private Long executionsTermineesAujourdhui;
        private Double rendementMoyenReel;
        private Double rendementMoyenAujourdhui;
        private Double quantiteProduitePeriode;
        private Double quantiteProduiteAujourdhui;
        private java.util.List<DailyYieldDTO> dailyRendements;
    }

    @Data
    public static class DailyYieldDTO {
        private String date; // ISO date YYYY-MM-DD
        private Double rendement;
    }

    @Data
    public static class ReceptionLotsDTO {
        private Double matiereRecueAujourdhui;
        private Long lotsRecusAujourdhui;
        private Double stockUtilisable;
    }

    @Data
    public static class ProductionProcessDTO {
        private Double ecartReelVsPreditMoyen;
        private List<HourlyExtractionDTO> extractionHoraire;
        private List<OperationStatusDTO> topOperations;
    }

    @Data
    public static class HourlyExtractionDTO {
        private String heure;
        private Double quantite;
    }

    @Data
    public static class OperationStatusDTO {
        private String reference;
        private String statut;
        private String machine;
    }

    @Data
    public static class MachinesDTO {
        private Long machinesActives;
        private Long machinesInactives;
        private List<MachineLoadDTO> chargeParMachine;
    }

    @Data
    public static class MachineLoadDTO {
        private String machine;
        private Double quantite;
        private String unite;
        private Boolean active;
    }

    @Data
    public static class QualityDTO {
        private Double aciditeMoyenne;
        private Double indicePeroxydeMoyen;
        private Double humiditeMoyennePate;
        private Double polyphenolsMoyen;
        private QualityDistributionDTO repartitionQualiteFinale;
    }

    @Data
    public static class QualityDistributionDTO {
        private Long extraVierge;
        private Long vierge;
        private Long lampante;
    }

    @Data
    public static class StockMovementsDTO {
        private Long entreesAujourdhui;
        private Long sortiesAujourdhui;
        private Long transfertsAujourdhui;
    }
}
