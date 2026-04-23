package Models;

import Config.ReferenceUtils;
import jakarta.persistence.*;

@Entity
public class AnalyseLaboratoire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnalyse;

    private String reference;
    private Double acidite_huile_pourcent;
    private Double indice_peroxyde_meq_o2_kg;
    private Double polyphenols_mg_kg;
    private Double k232;
    private Double k270;
    private String dateAnalyse;

    @OneToOne
    @JoinColumn(name = "lot_id", nullable = false, unique = true)
    private LotOlives lot;

    public Long getIdAnalyse() {
        return idAnalyse;
    }

    public void setIdAnalyse(Long idAnalyse) {
        this.idAnalyse = idAnalyse;
    }

    public String getReference() {
        if (idAnalyse != null) {
            return ReferenceUtils.format("AL", idAnalyse);
        }
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Double getAcidite_huile_pourcent() {
        return acidite_huile_pourcent;
    }

    public void setAcidite_huile_pourcent(Double acidite_huile_pourcent) {
        this.acidite_huile_pourcent = acidite_huile_pourcent;
    }

    public Double getIndice_peroxyde_meq_o2_kg() {
        return indice_peroxyde_meq_o2_kg;
    }

    public void setIndice_peroxyde_meq_o2_kg(Double indice_peroxyde_meq_o2_kg) {
        this.indice_peroxyde_meq_o2_kg = indice_peroxyde_meq_o2_kg;
    }

    public Double getPolyphenols_mg_kg() {
        return polyphenols_mg_kg;
    }

    public void setPolyphenols_mg_kg(Double polyphenols_mg_kg) {
        this.polyphenols_mg_kg = polyphenols_mg_kg;
    }

    public Double getK232() {
        return k232;
    }

    public void setK232(Double k232) {
        this.k232 = k232;
    }

    public Double getK270() {
        return k270;
    }

    public void setK270(Double k270) {
        this.k270 = k270;
    }

    public String getDateAnalyse() {
        return dateAnalyse;
    }

    public void setDateAnalyse(String dateAnalyse) {
        this.dateAnalyse = dateAnalyse;
    }

    public LotOlives getLot() {
        return lot;
    }

    public void setLot(LotOlives lot) {
        this.lot = lot;
    }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idAnalyse != null) {
            reference = ReferenceUtils.format("AL", idAnalyse);
        }
    }
}
