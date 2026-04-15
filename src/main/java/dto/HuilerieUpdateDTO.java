package dto;

import lombok.Data;

@Data
public class HuilerieUpdateDTO {
    private String nom;
    private String localisation;
    private String type;
    private String certification;
    private Integer capaciteProduction;
    private Long entrepriseId;
    private Boolean active;
}


