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

    private String varieteOlive;
    private String maturite;
    private String origine;
    private String dateRecolte;
    private String dateReception;
    private Integer dureeStockageAvantBroyage;
    private Double quantiteInitiale;
    private Double quantiteRestante;

    @ManyToOne
    @JoinColumn(name = "matiere_premiere_id", nullable = false)
    private MatierePremiere matierePremiere;

    @ManyToOne
    @JoinColumn(name = "campagne_id", nullable = false)
    private CampagneOlives campagne;

    @OneToMany(mappedBy = "lot")
    private List<Pesee> pesees;

    @OneToMany(mappedBy = "lot")
    private List<AnalyseLaboratoire> analyses;

    @OneToMany(mappedBy = "lotOlives")
    private List<ExecutionProduction> executionsProduction;

    @OneToMany(mappedBy = "lotOlives")
    private List<Stock> stocks;

    public Long getIdLot() { return idLot; }
    public void setIdLot(Long idLot) { this.idLot = idLot; }

    public String getReference() {
        if (idLot != null) {
            return ReferenceUtils.format("LO", idLot);
        }
        return reference;
    }
    public void setReference(String reference) { this.reference = reference; }

    public String getVarieteOlive() { return varieteOlive; }
    public void setVarieteOlive(String varieteOlive) { this.varieteOlive = varieteOlive; }

    public String getMaturite() { return maturite; }
    public void setMaturite(String maturite) { this.maturite = maturite; }

    public String getOrigine() { return origine; }
    public void setOrigine(String origine) { this.origine = origine; }

    public String getDateRecolte() { return dateRecolte; }
    public void setDateRecolte(String dateRecolte) { this.dateRecolte = dateRecolte; }

    public String getDateReception() { return dateReception; }
    public void setDateReception(String dateReception) { this.dateReception = dateReception; }

    public Integer getDureeStockageAvantBroyage() { return dureeStockageAvantBroyage; }
    public void setDureeStockageAvantBroyage(Integer dureeStockageAvantBroyage) { this.dureeStockageAvantBroyage = dureeStockageAvantBroyage; }

    public Double getQuantiteInitiale() { return quantiteInitiale; }
    public void setQuantiteInitiale(Double quantiteInitiale) { this.quantiteInitiale = quantiteInitiale; }

    public Double getQuantiteRestante() { return quantiteRestante; }
    public void setQuantiteRestante(Double quantiteRestante) { this.quantiteRestante = quantiteRestante; }

    public MatierePremiere getMatierePremiere() { return matierePremiere; }
    public void setMatierePremiere(MatierePremiere matierePremiere) { this.matierePremiere = matierePremiere; }

    public CampagneOlives getCampagne() { return campagne; }
    public void setCampagne(CampagneOlives campagne) { this.campagne = campagne; }

    public List<Pesee> getPesees() { return pesees; }
    public void setPesees(List<Pesee> pesees) { this.pesees = pesees; }

    public List<AnalyseLaboratoire> getAnalyses() { return analyses; }
    public void setAnalyses(List<AnalyseLaboratoire> analyses) { this.analyses = analyses; }

    public List<ExecutionProduction> getExecutionsProduction() { return executionsProduction; }
    public void setExecutionsProduction(List<ExecutionProduction> executionsProduction) { this.executionsProduction = executionsProduction; }

    public List<Stock> getStocks() { return stocks; }
    public void setStocks(List<Stock> stocks) { this.stocks = stocks; }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idLot != null) {
            reference = ReferenceUtils.format("LO", idLot);
        }
    }
}
