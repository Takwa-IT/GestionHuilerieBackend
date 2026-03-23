package Models;

import jakarta.persistence.*;

@Entity
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStockMovement;

    private Double quantite;
    private String commentaire;
    private String dateMouvement;

    @Enumerated(EnumType.STRING)
    private TypeMouvement typeMouvement;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    public Long getIdStockMovement() {
        return idStockMovement;
    }

    public void setIdStockMovement(Long idStockMovement) {
        this.idStockMovement = idStockMovement;
    }
    public Double getQuantite() { return quantite; }
    public void setQuantite(Double quantite) { this.quantite = quantite; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(String dateMouvement) { this.dateMouvement = dateMouvement; }

    public TypeMouvement getTypeMouvement() { return typeMouvement; }
    public void setTypeMouvement(TypeMouvement typeMouvement) { this.typeMouvement = typeMouvement; }

    public Stock getStock() { return stock; }
    public void setStock(Stock stock) { this.stock = stock; }
}
