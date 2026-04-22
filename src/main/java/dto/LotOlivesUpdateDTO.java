package dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class LotOlivesUpdateDTO {
    private String variete;
    private String maturite;
    private String origine;
    private String dateRecolte;
    private String dateReception;
    private String fournisseurNom;
    private String fournisseurCIN;
    private Integer dureeStockageAvantBroyage;

    @Positive
    private Double pesee;

    @Positive
    private Double quantiteInitiale;

    @Positive
    private Double quantiteRestante;

    private String matierePremiereReference;
    private String campagneReference;
}
