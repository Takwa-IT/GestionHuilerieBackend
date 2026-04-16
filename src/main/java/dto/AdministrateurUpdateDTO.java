package dto;

import lombok.Data;

@Data
public class AdministrateurUpdateDTO {
    private String nom;
    private String prenom;
    private String telephone;
    private Long entrepriseAdminId;
}
