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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "execution_production", uniqueConstraints = {
        @UniqueConstraint(name = "uk_execution_production_code_lot", columnNames = "codeLot")
})
@Getter
@Setter
@NoArgsConstructor
public class ExecutionProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idExecutionProduction;

    @Column(nullable = false, unique = true)
    private String codeLot;

    private String dateDebut;

    private String dateFinPrevue;

    private String dateFinReelle;

    @Column(nullable = false)
    private String statut;

    private Double rendement;

    private String observations;

    @ManyToOne
    @JoinColumn(name = "guide_production_id", nullable = false)
    private GuideProduction guideProduction;

    @ManyToOne
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @ManyToOne
    @JoinColumn(name = "lot_olives_id", nullable = false)
    private LotOlives lotOlives;

    @ManyToOne
    @JoinColumn(name = "matiere_premiere_id", nullable = false)
    private MatierePremiere matierePremiere;

    @OneToOne(mappedBy = "executionProduction")
    private ProduitFinal produitFinal;

    @OneToMany(mappedBy = "executionProduction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ValeurReelleParametre> valeursReelles;
}