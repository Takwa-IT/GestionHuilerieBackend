package dto;

import lombok.Data;

@Data
public class AdministrateurCreateDTO {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private Long entrepriseAdminId;
}
