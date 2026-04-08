package dto;

import lombok.Data;

@Data
public class MatierePremiereDTO {
    private String reference;
    private String nom;
    private String type;
    private String uniteMesure;
    private String description;
}
