package Models;

import Config.ReferenceUtils;
import jakarta.persistence.*;

@Entity
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStockMovement;

    private String reference;
    private String commentaire;
    private String dateMouvement;

    @Enumerated(EnumType.STRING)
    private TypeMouvement typeMouvement;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne
    @JoinColumn(name = "lot_id")
    private LotOlives lotOlives;

    public LotOlives getLotOlives() {
        return lotOlives;
    }

    public void setLotOlives(LotOlives lotOlives) {
        this.lotOlives = lotOlives;
    }

    public Long getIdStockMovement() {
        return idStockMovement;
    }

    public void setIdStockMovement(Long idStockMovement) {
        this.idStockMovement = idStockMovement;
    }

    public String getReference() {
        if (idStockMovement != null) {
            return ReferenceUtils.format("MS", idStockMovement);
        }
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(String dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public TypeMouvement getTypeMouvement() {
        return typeMouvement;
    }

    public void setTypeMouvement(TypeMouvement typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idStockMovement != null) {
            reference = ReferenceUtils.format("MS", idStockMovement);
        }
    }
}
