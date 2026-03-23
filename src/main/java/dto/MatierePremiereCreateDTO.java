package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MatierePremiereCreateDTO {
    @NotBlank
    private String nom;

    @NotBlank
    private String type;

    @NotBlank
    private String uniteMesure;

    private String description;
}
