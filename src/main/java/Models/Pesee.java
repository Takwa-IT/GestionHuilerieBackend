package Models;

import jakarta.persistence.*;

@Entity
public class Pesee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPesee;

    private String datePesee;
    private Double poidsBrut;
    private Double poidsTare;
    private Double poidsNet;

    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = false)
    private LotOlives lot;

    public Long getIdPesee() { return idPesee; }
    public void setIdPesee(Long idPesee) { this.idPesee = idPesee; }

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
