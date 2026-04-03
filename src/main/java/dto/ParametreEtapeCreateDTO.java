package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParametreEtapeCreateDTO {

    @NotBlank
    private String nom;

    private String uniteMesure;

    private String description;

    private String valeur;
}