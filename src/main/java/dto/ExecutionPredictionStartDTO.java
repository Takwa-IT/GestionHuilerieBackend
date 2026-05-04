package dto;

import lombok.Data;

@Data
public class ExecutionPredictionStartDTO {
    private String region;
    private String methodeRecolte;
    private String typeSol;
    private Boolean controleTemperature;
    private Double temperatureMalaxageC;
    private Double dureeMalaxageMin;
    private Double vitesseDecanteurTrMin;
    private Double humiditePourcent;
    private Double aciditeOlivesPourcent;
    private Double tauxFeuillesPourcent;
    private Double pressionExtractionBar;
    private Boolean presenceSeparateur;
    private Boolean presenceAjoutEau;
}
