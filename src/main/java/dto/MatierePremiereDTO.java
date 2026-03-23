package dto;

import lombok.Data;

@Data
public class MatierePremiereDTO {
    private Long idMatierePremiere;
    private String nom;
    private String type;
    private String uniteMesure;
    private String description;
}
