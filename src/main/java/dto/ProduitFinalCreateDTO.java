package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProduitFinalCreateDTO {
    @NotNull
    private Long executionProductionId;

    @NotBlank
    private String nomProduit;

    @NotNull
    private Double quantiteProduite;

    private String qualite;

    @NotBlank
    private String dateProduction;
}


