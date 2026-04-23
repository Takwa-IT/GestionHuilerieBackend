package Models;

import Config.ReferenceUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class LotOlives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLot;

    @Column(unique = true)
    private String reference;

    private String variete;
    private String maturite;
    private String origine;
    private String region;
    private String methodeRecolte;
    private String typeSol;
    private Integer tempsDepuisRecolteHeures;
    private Double humiditePourcent;
    private Double aciditeOlivesPourcent;
    private Double tauxFeuillesPourcent;
    private String lavageEffectue;
    private String dateRecolte;
    private String dateReception;
    private String fournisseurNom;
    private String fournisseurCIN;
    private Integer dureeStockageAvantBroyage;
    private Double pesee;
    private Double quantiteInitiale;
    private Double quantiteRestante;

    @Column(name = "bon_pesee_pdf_path")
    private String bonPeseePdfPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "matiere_premiere_id", nullable = false)
    private MatierePremiere matierePremiere;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "campagne_id", nullable = false)
    private CampagneOlives campagne;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "huilerie_id")
    private Huilerie huilerie;

    @OneToOne(mappedBy = "lot")
    private AnalyseLaboratoire analyseLaboratoire;

    @OneToMany(mappedBy = "lot")
    private List<ExecutionProduction> executionsProduction;

    @OneToMany(mappedBy = "lotOlives")
    private List<Stock> stocks;

    public Long getIdLot() {
        return idLot;
    }

    public void setIdLot(Long idLot) {
        this.idLot = idLot;
    }

    public String getReference() {
        if (idLot != null) {
            return ReferenceUtils.format("LO", idLot);
        }
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getVariete() {
        return variete;
    }

    public void setVariete(String variete) {
        this.variete = variete;
    }

    public String getVarieteOlive() {
        return variete;
    }

    public void setVarieteOlive(String varieteOlive) {
        this.variete = varieteOlive;
    }

    public String getMaturite() {
        return maturite;
    }

    public void setMaturite(String maturite) {
        this.maturite = maturite;
    }

    public String getOrigine() {
        return origine;
    }

    public void setOrigine(String origine) {
        this.origine = origine;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getMethodeRecolte() {
        return methodeRecolte;
    }

    public void setMethodeRecolte(String methodeRecolte) {
        this.methodeRecolte = methodeRecolte;
    }

    public String getTypeSol() {
        return typeSol;
    }

    public void setTypeSol(String typeSol) {
        this.typeSol = typeSol;
    }

    public Integer getTempsDepuisRecolteHeures() {
        return tempsDepuisRecolteHeures;
    }

    public void setTempsDepuisRecolteHeures(Integer tempsDepuisRecolteHeures) {
        this.tempsDepuisRecolteHeures = tempsDepuisRecolteHeures;
    }

    public Double getHumiditePourcent() {
        return humiditePourcent;
    }

    public void setHumiditePourcent(Double humiditePourcent) {
        this.humiditePourcent = humiditePourcent;
    }

    public Double getAciditeOlivesPourcent() {
        return aciditeOlivesPourcent;
    }

    public void setAciditeOlivesPourcent(Double aciditeOlivesPourcent) {
        this.aciditeOlivesPourcent = aciditeOlivesPourcent;
    }

    public Double getTauxFeuillesPourcent() {
        return tauxFeuillesPourcent;
    }

    public void setTauxFeuillesPourcent(Double tauxFeuillesPourcent) {
        this.tauxFeuillesPourcent = tauxFeuillesPourcent;
    }

    public String getLavageEffectue() {
        return lavageEffectue;
    }

    public void setLavageEffectue(String lavageEffectue) {
        this.lavageEffectue = lavageEffectue;
    }

    public String getDateRecolte() {
        return dateRecolte;
    }

    public void setDateRecolte(String dateRecolte) {
        this.dateRecolte = dateRecolte;
    }

    public String getDateReception() {
        return dateReception;
    }

    public void setDateReception(String dateReception) {
        this.dateReception = dateReception;
    }

    public String getFournisseurNom() {
        return fournisseurNom;
    }

    public void setFournisseurNom(String fournisseurNom) {
        this.fournisseurNom = fournisseurNom;
    }

    public String getFournisseurCIN() {
        return fournisseurCIN;
    }

    public void setFournisseurCIN(String fournisseurCIN) {
        this.fournisseurCIN = fournisseurCIN;
    }

    public Integer getDureeStockageAvantBroyage() {
        return dureeStockageAvantBroyage;
    }

    public void setDureeStockageAvantBroyage(Integer dureeStockageAvantBroyage) {
        this.dureeStockageAvantBroyage = dureeStockageAvantBroyage;
    }

    public Double getPesee() {
        return pesee;
    }

    public void setPesee(Double pesee) {
        this.pesee = pesee;
    }

    public Double getQuantiteInitiale() {
        return quantiteInitiale;
    }

    public void setQuantiteInitiale(Double quantiteInitiale) {
        this.quantiteInitiale = quantiteInitiale;
    }

    public Double getQuantiteRestante() {
        return quantiteRestante;
    }

    public void setQuantiteRestante(Double quantiteRestante) {
        this.quantiteRestante = quantiteRestante;
    }

    public String getBonPeseePdfPath() {
        return bonPeseePdfPath;
    }

    public void setBonPeseePdfPath(String bonPeseePdfPath) {
        this.bonPeseePdfPath = bonPeseePdfPath;
    }

    public MatierePremiere getMatierePremiere() {
        return matierePremiere;
    }

    public void setMatierePremiere(MatierePremiere matierePremiere) {
        this.matierePremiere = matierePremiere;
    }

    public CampagneOlives getCampagne() {
        return campagne;
    }

    public void setCampagne(CampagneOlives campagne) {
        this.campagne = campagne;
    }

    public Huilerie getHuilerie() {
        return huilerie;
    }

    public void setHuilerie(Huilerie huilerie) {
        this.huilerie = huilerie;
    }

    public AnalyseLaboratoire getAnalyseLaboratoire() {
        return analyseLaboratoire;
    }

    public void setAnalyseLaboratoire(AnalyseLaboratoire analyseLaboratoire) {
        this.analyseLaboratoire = analyseLaboratoire;
    }

    public List<ExecutionProduction> getExecutionsProduction() {
        return executionsProduction;
    }

    public void setExecutionsProduction(List<ExecutionProduction> executionsProduction) {
        this.executionsProduction = executionsProduction;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idLot != null) {
            reference = ReferenceUtils.format("LO", idLot);
        }
    }
}
