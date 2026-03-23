package Models;

import jakarta.persistence.*;

@Entity
public class ProductionLot {

    @EmbeddedId
    private ProductionLotId id;

    @ManyToOne
    @MapsId("productionId")
    @JoinColumn(name = "production_id", nullable = false)
    private Production production;

    @ManyToOne
    @MapsId("lotId")
    @JoinColumn(name = "lot_id", nullable = false)
    private LotOlives lot;

    private Double quantiteUtilisee;

    public ProductionLotId getId() { return id; }
    public void setId(ProductionLotId id) { this.id = id; }

    public Production getProduction() { return production; }
    public void setProduction(Production production) { this.production = production; }

    public LotOlives getLot() { return lot; }
    public void setLot(LotOlives lot) { this.lot = lot; }

    public Double getQuantiteUtilisee() { return quantiteUtilisee; }
    public void setQuantiteUtilisee(Double quantiteUtilisee) { this.quantiteUtilisee = quantiteUtilisee; }
}