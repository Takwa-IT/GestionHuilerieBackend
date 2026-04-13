package dto;

import lombok.Data;

@Data
public class AuthUtilisateurDTO {
    private Long id;
    private Long entrepriseId;
    private Long huilerieId;
    private String nom;
    private String prenom;
    private String email;
    private String profil;
}
