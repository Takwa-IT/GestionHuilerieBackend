package dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GuideProductionCreateDTO {

    @NotBlank
    private String nom;

    private String description;

    @NotBlank
    private String dateCreation;

    @NotNull
    private Long huilerieId;

    @Valid
    private List<EtapeProductionCreateDTO> etapes;
}


