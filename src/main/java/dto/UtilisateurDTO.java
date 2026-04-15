package dto;

import Models.StatutUtilisateur;
import lombok.Data;

@Data
public class UtilisateurDTO {
    private Long idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private StatutUtilisateur actif;
    private Long profilId;
    private Long huilerieId;
    private Boolean emailVerified;
}


