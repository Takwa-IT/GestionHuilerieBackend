package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CampagneOlivesCreateDTO {
    @NotBlank
    private String annee;

    private String dateDebut;
    private String dateFin;

    @NotNull
    private Long huilerieId;
}
