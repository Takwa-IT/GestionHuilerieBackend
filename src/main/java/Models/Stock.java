package Models;

import Config.ReferenceUtils;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStock;

    private String reference;
    private String typeStock;
    private Double quantiteDisponible;

    @ManyToOne
    @JoinColumn(name = "huilerie_id", nullable = false)
    private Huilerie huilerie;

    @ManyToOne
    @JoinColumn(name = "lot_id")
    private LotOlives lotOlives;

    @ManyToOne
    @JoinColumn(name = "matiere_premiere_id")
    private MatierePremiere matierePremiere;

    @ManyToOne
    @JoinColumn(name = "produit_id")
    private ProduitFinal produitFinal;

    @OneToMany(mappedBy = "stock")
    private List<StockMovement> stockMovements;

    public Long getIdStock() {
        return idStock;
    }

    public void setIdStock(Long idStock) {
        this.idStock = idStock;
    }

    public String getReference() {
        if (idStock != null) {
            return ReferenceUtils.format("ST", idStock);
        }
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTypeStock() {
        return typeStock;
    }

    public void setTypeStock(String typeStock) {
        this.typeStock = typeStock;
    }

    public Double getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(Double quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public Huilerie getHuilerie() {
        return huilerie;
    }

    public void setHuilerie(Huilerie huilerie) {
        this.huilerie = huilerie;
    }

    public LotOlives getLotOlives() {
        return lotOlives;
    }

    public void setLotOlives(LotOlives lotOlives) {
        this.lotOlives = lotOlives;
    }

    public MatierePremiere getMatierePremiere() {
        return matierePremiere;
    }

    public void setMatierePremiere(MatierePremiere matierePremiere) {
        this.matierePremiere = matierePremiere;
    }

    public ProduitFinal getProduitFinal() {
        return produitFinal;
    }

    public void setProduitFinal(ProduitFinal produitFinal) {
        this.produitFinal = produitFinal;
    }

    public List<StockMovement> getStockMovements() {
        return stockMovements;
    }

    public void setStockMovements(List<StockMovement> stockMovements) {
        this.stockMovements = stockMovements;
    }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idStock != null) {
            reference = ReferenceUtils.format("ST", idStock);
        }
    }
}
