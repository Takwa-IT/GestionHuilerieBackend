package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReceptionPeseeCreateDTO {
    private Long lotId;

    @NotBlank
    private String datePesee;

    @NotNull
    @Positive
    private Double poidsBrut;

    private Double poidsTare;

    private String varieteOlive;
    private String maturite;
    private String origine;
    private String dateRecolte;
    private String dateReception;
    private Integer dureeStockageAvantBroyage;
    private Long matierePremiereId;
    private String campagneAnnee;
    private Long huilerieId;
}
