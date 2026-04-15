package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValeurReelleParametreCreateDTO {

    @NotNull
    private Long parametreEtapeId;

    @NotBlank
    private String valeurReelle;
}


