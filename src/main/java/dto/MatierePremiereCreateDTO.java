package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatierePremiereCreateDTO {
    @NotBlank
    private String nom;

    @NotBlank
    private String type;

    @NotBlank
    private String uniteMesure;

    @NotNull
    private Long huilerieId;

    private String description;
}
