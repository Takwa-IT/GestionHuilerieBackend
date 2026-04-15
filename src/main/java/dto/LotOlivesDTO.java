package dto;

import lombok.Data;

@Data
public class LotOlivesDTO {
    private Long idLot;
    private String reference;
    private String varieteOlive;
    private String maturite;
    private String origine;
    private String dateRecolte;
    private String dateReception;
    private String fournisseurNom;
    private String fournisseurCIN;
    private Integer dureeStockageAvantBroyage;
    private Double pesee;
    private Double quantiteInitiale;
    private Double quantiteRestante;
    private Long matierePremiereId;
    private String matierePremiereReference;
    private Long campagneId;
    private Long huilerieId;
    private String huilerieNom;
}
