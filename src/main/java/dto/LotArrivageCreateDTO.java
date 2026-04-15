package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class LotArrivageCreateDTO {
    private String variete;
    private String maturite;
    private String origine;
    private String dateRecolte;
    private String fournisseurNom;
    private String fournisseurCIN;

    @NotBlank
    private String dateReception;

    private Integer dureeStockageAvantBroyage;

    @NotNull
    @Positive
    private Double pesee;

    @NotBlank
    private String matierePremiereReference;

    @NotBlank
    private String campagneReference;

    private Long huilerieId;
}
