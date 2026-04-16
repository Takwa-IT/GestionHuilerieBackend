package dto;

import lombok.Data;

import java.util.List;

@Data
public class EntrepriseDTO {
    private Long idEntreprise;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private Long administrateurId;
    private List<Long> huilerieIds;
}
