package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LotOlivesDTO {
    private Long idLot;
    private String reference;

    @JsonProperty("variete")
    private String varieteOlive;

    @JsonProperty("maturite_niveau_1_5")
    private String maturite;

    private String origine;
    private String region;

    @JsonProperty("methode_recolte")
    private String methodeRecolte;

    @JsonProperty("type_sol")
    private String typeSol;

    @JsonProperty("temps_depuis_recolte_heures")
    private Integer tempsDepuisRecolteHeures;

    @JsonProperty("humidite_pourcent")
    private Double humiditePourcent;

    @JsonProperty("acidite_olives_pourcent")
    private Double aciditeOlivesPourcent;

    @JsonProperty("taux_feuilles_pourcent")
    private Double tauxFeuillesPourcent;

    @JsonProperty("lavage_effectue")
    private String lavageEffectue;

    private String dateRecolte;
    private String dateReception;
    private String fournisseurNom;
    private String fournisseurCIN;

    @JsonProperty("duree_stockage_jours")
    private Integer dureeStockageAvantBroyage;

    @JsonProperty("poids_olives_kg")
    private Double pesee;
    private Double quantiteInitiale;
    private Double quantiteRestante;
    private String bonPeseePdfPath;
    private Long matierePremiereId;
    private String matierePremiereReference;
    private Long campagneId;
    private Long huilerieId;
    private String huilerieNom;
}
