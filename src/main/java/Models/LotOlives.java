package Models;

import Config.ReferenceUtils;
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
    private String dateRecolte;
    private String dateReception;
    private String fournisseurNom;
    private String fournisseurCIN;
    private Integer dureeStockageAvantBroyage;
    private Double pesee;
    private Double quantiteInitiale;
    private Double quantiteRestante;

    @ManyToOne
    @JoinColumn(name = "matiere_premiere_id", nullable = false)
    private MatierePremiere matierePremiere;

    @ManyToOne
    @JoinColumn(name = "campagne_id", nullable = false)
    private CampagneOlives campagne;

    @ManyToOne
    @JoinColumn(name = "huilerie_id")
    private Huilerie huilerie;

    @OneToOne(mappedBy = "lot")
    private AnalyseLaboratoire analyseLaboratoire;

    @OneToMany(mappedBy = "lotOlives")
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
