package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParametreEtapeCreateDTO {

    @NotBlank
    private String nom;

    @NotBlank
    private String codeParametre;

    private String uniteMesure;

    private String description;

    @NotBlank
    private String valeur;
}


