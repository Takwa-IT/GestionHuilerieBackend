package dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordConfirmDTO {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "Le mot de passe doit contenir au moins une majuscule et un chiffre"
    )
    @JsonAlias({"newPassword", "password", "motDePasse"})
    private String nouveauMotDePasse;
}


