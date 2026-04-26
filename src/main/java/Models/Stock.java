package Models;

import Config.ReferenceUtils;
import jakarta.persistence.*;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStock;

    private String reference;
    private String typeStock;
    @Column(name = "variete")
    private String variete;
    private Double quantiteDisponible;

    @ManyToOne
    @JoinColumn(name = "lot_id")
    private LotOlives lotOlives;

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

    public String getVariete() {
        return variete;
    }

    public void setVariete(String variete) {
        this.variete = variete;
    }

    public Double getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(Double quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public LotOlives getLotOlives() {
        return lotOlives;
    }

    public void setLotOlives(LotOlives lotOlives) {
        this.lotOlives = lotOlives;
    }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idStock != null) {
            reference = ReferenceUtils.format("ST", idStock);
        }
    }
}
