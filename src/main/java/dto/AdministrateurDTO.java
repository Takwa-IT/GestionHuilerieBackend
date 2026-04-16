package dto;

import lombok.Data;

@Data
public class AdministrateurDTO {
    private Long idUtilisateur;
    private Long idAdministrateur;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Long entrepriseAdminId;
    private Boolean emailVerified;
}
