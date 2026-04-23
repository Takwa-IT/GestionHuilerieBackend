package dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class LotArrivageCreateDTO {
    @JsonAlias({ "varieteOlive" })
    @NotBlank
    private String variete;

    @JsonAlias({ "maturite_niveau_1_5" })
    private String maturite;

    private String origine;
    private String region;

    @JsonAlias({ "methode_recolte" })
    private String methodeRecolte;

    @JsonAlias({ "type_sol" })
    private String typeSol;

    @JsonAlias({ "temps_depuis_recolte_heures" })
    private Integer tempsDepuisRecolteHeures;

    @JsonAlias({ "humidite_pourcent" })
    @PositiveOrZero
    private Double humiditePourcent;

    @JsonAlias({ "acidite_olives_pourcent" })
    @PositiveOrZero
    private Double aciditeOlivesPourcent;

    @JsonAlias({ "taux_feuilles_pourcent" })
    @PositiveOrZero
    private Double tauxFeuillesPourcent;

    @JsonAlias({ "lavage_effectue", "lavageEffectue" })
    private String lavageEffectue;

    private String dateRecolte;
    private String fournisseurNom;
    private String fournisseurCIN;

    @NotBlank
    private String dateReception;

    @JsonAlias({ "duree_stockage_jours" })
    private Integer dureeStockageAvantBroyage;

    @JsonAlias({ "poids_olives_kg" })
    @NotNull
    @Positive
    private Double pesee;

    @NotBlank
    private String matierePremiereReference;

    @NotBlank
    private String campagneReference;

    private Long huilerieId;

}
