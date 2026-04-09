package Models;

import jakarta.persistence.*;

@Entity
public class AnalyseLaboratoire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnalyse;

    private String reference;
    private Double acidite;
    private Double indicePeroxyde;
    private Double k232;
    private Double k270;
    private String classeQualiteFinale;
    private String dateAnalyse;

    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = false)
    private LotOlives lot;

    public Long getIdAnalyse() { return idAnalyse; }
    public void setIdAnalyse(Long idAnalyse) { this.idAnalyse = idAnalyse; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Double getAcidite() { return acidite; }
    public void setAcidite(Double acidite) { this.acidite = acidite; }

    public Double getIndicePeroxyde() { return indicePeroxyde; }
    public void setIndicePeroxyde(Double indicePeroxyde) { this.indicePeroxyde = indicePeroxyde; }

    public Double getK232() { return k232; }
    public void setK232(Double k232) { this.k232 = k232; }

    public Double getK270() { return k270; }
    public void setK270(Double k270) { this.k270 = k270; }

    public String getClasseQualiteFinale() { return classeQualiteFinale; }
    public void setClasseQualiteFinale(String classeQualiteFinale) { this.classeQualiteFinale = classeQualiteFinale; }

    public String getDateAnalyse() { return dateAnalyse; }
    public void setDateAnalyse(String dateAnalyse) { this.dateAnalyse = dateAnalyse; }

    public LotOlives getLot() { return lot; }
    public void setLot(LotOlives lot) { this.lot = lot; }
}
