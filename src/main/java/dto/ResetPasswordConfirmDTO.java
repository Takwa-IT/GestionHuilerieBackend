package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordConfirmDTO {
    @NotBlank
    private String token;

    @NotBlank
    private String nouveauMotDePasse;
}