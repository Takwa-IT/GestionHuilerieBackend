package Models;

import jakarta.persistence.*;

@Entity
public class Pesee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pesee")
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    private String datePesee;
    private Double poidsBrut;
    private Double poidsTare;
    private Double poidsNet;

    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = false)
    private LotOlives lot;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDatePesee() { return datePesee; }
    public void setDatePesee(String datePesee) { this.datePesee = datePesee; }

    public Double getPoidsBrut() { return poidsBrut; }
    public void setPoidsBrut(Double poidsBrut) { this.poidsBrut = poidsBrut; }

    public Double getPoidsTare() { return poidsTare; }
    public void setPoidsTare(Double poidsTare) { this.poidsTare = poidsTare; }

    public Double getPoidsNet() { return poidsNet; }
    public void setPoidsNet(Double poidsNet) { this.poidsNet = poidsNet; }

    public LotOlives getLot() { return lot; }
    public void setLot(LotOlives lot) { this.lot = lot; }
}
