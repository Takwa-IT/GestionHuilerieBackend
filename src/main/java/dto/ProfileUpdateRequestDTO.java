package dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequestDTO {
    private String nom;
    private String prenom;

    @Email
    private String email;

    private String telephone;

    @JsonAlias({"currentPassword", "motDePasseActuel"})
    private String currentPassword;

    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$|^$",
            message = "Le mot de passe doit contenir au moins une majuscule et un chiffre"
    )
    @JsonAlias({"newPassword", "nouveauMotDePasse"})
    private String newPassword;

    @JsonAlias({"confirmPassword", "confirmationMotDePasse", "confirmNewPassword"})
    private String confirmPassword;
}


