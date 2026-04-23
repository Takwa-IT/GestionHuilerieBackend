package Models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "execution_production")
@Getter
@Setter
@NoArgsConstructor
public class ExecutionProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idExecutionProduction;

    @Column(nullable = false, unique = true)
    private String reference;

    private String dateDebut;

    private String dateFinPrevue;

    private String dateFinReelle;

    @Column(nullable = false)
    private String statut;

    private Double rendement;

    private String observations;

    private Boolean controleTemperature;

    @ManyToOne
    @JoinColumn(name = "guide_production_id", nullable = false)
    private GuideProduction guideProduction;

    @ManyToOne
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @ManyToOne
    @JoinColumn(name = "lot_olives_id", nullable = false)
    private LotOlives lot;

    @OneToMany(mappedBy = "executionProduction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProduitFinal> produitsFinaux = new ArrayList<>();

    @OneToMany(mappedBy = "executionProduction")
    private List<ParametreEtape> parametres = new ArrayList<>();

    @OneToMany(mappedBy = "executionProduction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ValeurReelleParametre> valeursReelles = new ArrayList<>();

    @OneToMany(mappedBy = "executionProduction", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Prediction> predictions = new ArrayList<>();

    public String getCodeLot() {
        return reference;
    }

    public void setCodeLot(String codeLot) {
        this.reference = codeLot;
    }

    public LotOlives getLotOlives() {
        return lot;
    }

    public void setLotOlives(LotOlives lotOlives) {
        this.lot = lotOlives;
    }

    public ProduitFinal getProduitFinal() {
        return produitsFinaux == null || produitsFinaux.isEmpty() ? null : produitsFinaux.get(0);
    }

    public void setProduitFinal(ProduitFinal produitFinal) {
        if (produitsFinaux == null) {
            produitsFinaux = new ArrayList<>();
        }
        produitsFinaux.clear();
        if (produitFinal != null) {
            produitsFinaux.add(produitFinal);
        }
    }
}
