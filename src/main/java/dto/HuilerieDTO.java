package dto;

import lombok.Data;

@Data
public class HuilerieDTO {
    private Long idHuilerie;
    private String nom;
    private String localisation;
    private String type; // artisanal, semi-industriel, industriel
    private String certification;
    private Integer capaciteProduction;
    private Long entrepriseId;
    private Boolean active;
}


