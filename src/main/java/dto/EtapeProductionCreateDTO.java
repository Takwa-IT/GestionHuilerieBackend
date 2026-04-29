package dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EtapeProductionCreateDTO {

    @NotBlank
    private String nom;

    @NotNull
    private Integer ordre;

    private String description;

    private String codeEtape;

    @Valid
    private List<ParametreEtapeCreateDTO> parametres;

    private Long machineId;
}
