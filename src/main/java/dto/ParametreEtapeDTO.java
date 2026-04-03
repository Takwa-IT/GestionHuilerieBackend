package dto;

import lombok.Data;

@Data
public class ParametreEtapeDTO {

    private Long idParametreEtape;
    private String nom;
    private String uniteMesure;
    private String description;
    private String valeur;
}