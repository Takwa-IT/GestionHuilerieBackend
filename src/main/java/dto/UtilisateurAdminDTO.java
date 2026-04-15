package dto;

import Models.StatutUtilisateur;
import lombok.Data;

@Data
public class UtilisateurAdminDTO {
    private Long idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private StatutUtilisateur actif;
    private Long profilId;
    private String profilNom;
    private Long entrepriseId;
    private Long huilerieId;
    private String huilerieNom;
}


