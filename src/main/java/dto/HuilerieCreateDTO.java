package dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class HuilerieCreateDTO {
    @NotBlank
    private String nom;

    private String localisation;

    @NotBlank
    private String type;

    private String certification;

    @NotNull
    private Integer capaciteProduction;

    @NotNull
    private Long entrepriseId;

    private Boolean active = true;
}


