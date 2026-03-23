package Models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class ProduitFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduit;

    private String nomProduit;
    private Double quantiteProduite;
    private String dateProduction;

    @ManyToOne
    @JoinColumn(name = "production_id", nullable = false)
    private Production production;

    @OneToMany(mappedBy = "produitFinal")
    private List<Stock> stocks;

    public Long getIdProduit() { return idProduit; }
    public void setIdProduit(Long idProduit) { this.idProduit = idProduit; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }

    public Double getQuantiteProduite() { return quantiteProduite; }
    public void setQuantiteProduite(Double quantiteProduite) { this.quantiteProduite = quantiteProduite; }

    public String getDateProduction() { return dateProduction; }
    public void setDateProduction(String dateProduction) { this.dateProduction = dateProduction; }

    public Production getProduction() { return production; }
    public void setProduction(Production production) { this.production = production; }

    public List<Stock> getStocks() { return stocks; }
    public void setStocks(List<Stock> stocks) { this.stocks = stocks; }
}