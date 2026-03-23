package Models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Production {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduction;

    private String dateDebut;
    private String dateFin;
    private Double temperatureMalaxage;
    private Integer dureeMalaxage;
    private Double pressionExtraction;
    private Double vitesseDecanteur;
    private Double rendementExtraction;

    @ManyToOne
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @ManyToOne
    @JoinColumn(name = "huilerie_id", nullable = false)
    private Huilerie huilerie;

    @OneToMany(mappedBy = "production")
    private List<ProductionLot> productionLots;

    @OneToMany(mappedBy = "production")
    private List<ProduitFinal> produitsFinaux;

    public Long getIdProduction() { return idProduction; }
    public void setIdProduction(Long idProduction) { this.idProduction = idProduction; }

    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }

    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }

    public Double getTemperatureMalaxage() { return temperatureMalaxage; }
    public void setTemperatureMalaxage(Double temperatureMalaxage) { this.temperatureMalaxage = temperatureMalaxage; }

    public Integer getDureeMalaxage() { return dureeMalaxage; }
    public void setDureeMalaxage(Integer dureeMalaxage) { this.dureeMalaxage = dureeMalaxage; }

    public Double getPressionExtraction() { return pressionExtraction; }
    public void setPressionExtraction(Double pressionExtraction) { this.pressionExtraction = pressionExtraction; }

    public Double getVitesseDecanteur() { return vitesseDecanteur; }
    public void setVitesseDecanteur(Double vitesseDecanteur) { this.vitesseDecanteur = vitesseDecanteur; }

    public Double getRendementExtraction() { return rendementExtraction; }
    public void setRendementExtraction(Double rendementExtraction) { this.rendementExtraction = rendementExtraction; }

    public Machine getMachine() { return machine; }
    public void setMachine(Machine machine) { this.machine = machine; }

    public Huilerie getHuilerie() { return huilerie; }
    public void setHuilerie(Huilerie huilerie) { this.huilerie = huilerie; }

    public List<ProductionLot> getProductionLots() { return productionLots; }
    public void setProductionLots(List<ProductionLot> productionLots) { this.productionLots = productionLots; }

    public List<ProduitFinal> getProduitsFinaux() { return produitsFinaux; }
    public void setProduitsFinaux(List<ProduitFinal> produitsFinaux) { this.produitsFinaux = produitsFinaux; }
}
