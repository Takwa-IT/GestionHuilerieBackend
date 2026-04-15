package dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UtilisateurAdminUpdateRequestDTO {
    private String nom;
    private String prenom;

    @Email
    private String email;

    private String telephone;
    private Long profilId;
    private Long huilerieId;
}


