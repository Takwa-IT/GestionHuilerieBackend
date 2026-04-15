package Models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class ProduitFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduit;

    @Column(nullable = false, unique = true)
    private String reference;

    private String nomProduit;
    private Double quantiteProduite;
    private String dateProduction;

    @Column(name = "production_id", nullable = false)
    private Long executionProductionId;

    @OneToOne
    @JoinColumn(name = "production_id", referencedColumnName = "idExecutionProduction", insertable = false, updatable = false)
    private ExecutionProduction executionProduction;

    @OneToMany(mappedBy = "produitFinal")
    private List<Stock> stocks;

    public Long getIdProduit() { return idProduit; }
    public void setIdProduit(Long idProduit) { this.idProduit = idProduit; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }

    public Double getQuantiteProduite() { return quantiteProduite; }
    public void setQuantiteProduite(Double quantiteProduite) { this.quantiteProduite = quantiteProduite; }

    public String getDateProduction() { return dateProduction; }
    public void setDateProduction(String dateProduction) { this.dateProduction = dateProduction; }

    public Long getExecutionProductionId() { return executionProductionId; }
    public void setExecutionProductionId(Long executionProductionId) { this.executionProductionId = executionProductionId; }

    public ExecutionProduction getExecutionProduction() { return executionProduction; }
    public void setExecutionProduction(ExecutionProduction executionProduction) { this.executionProduction = executionProduction; }

    public List<Stock> getStocks() { return stocks; }
    public void setStocks(List<Stock> stocks) { this.stocks = stocks; }
}


