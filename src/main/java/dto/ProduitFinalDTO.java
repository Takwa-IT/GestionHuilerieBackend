package dto;

import lombok.Data;

@Data
public class ProduitFinalDTO {
    private Long idProduit;
    private String reference;
    private String nomProduit;
    private Double quantiteProduite;
    private String qualite;
    private String dateProduction;
    private Long executionProductionId;
}


