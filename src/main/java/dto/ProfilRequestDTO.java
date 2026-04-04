package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfilRequestDTO {
    @NotBlank
    private String nom;

    @NotBlank
    private String description;
}