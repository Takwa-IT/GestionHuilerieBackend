package dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class LotOlivesUpdateDTO {
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
    @JsonProperty("humidite_pourcent")
    @PositiveOrZero
    private Double humiditePourcent;

    @JsonAlias({ "acidite_olives_pourcent" })
    @JsonProperty("acidite_olives_pourcent")
    @PositiveOrZero
    private Double aciditeOlivesPourcent;

    @JsonAlias({ "taux_feuilles_pourcent" })
    @JsonProperty("taux_feuilles_pourcent")
    @PositiveOrZero
    private Double tauxFeuillesPourcent;

    @JsonAlias({ "lavage_effectue", "lavageEffectue" })
    private String lavageEffectue;

    private String dateRecolte;
    private String dateReception;
    private String fournisseurNom;
    private String fournisseurCIN;

    @JsonAlias({ "duree_stockage_jours" })
    private Integer dureeStockageAvantBroyage;

    @JsonAlias({ "poids_olives_kg" })
    @Positive
    private Double pesee;

    @Positive
    private Double quantiteInitiale;

    @Positive
    private Double quantiteRestante;

    private String matierePremiereReference;
    private String campagneReference;
}
