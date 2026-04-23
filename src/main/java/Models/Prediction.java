package Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prediction")
@Getter
@Setter
@NoArgsConstructor
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prediction")
    private Long idPrediction;

    @Column(name = "mode_prediction", nullable = false, length = 50)
    private String modePrediction;

    @Column(name = "qualite_predite", length = 100)
    private String qualitePredite;

    @Column(name = "probabilite_qualite")
    private Double probabiliteQualite;

    @Column(name = "rendement_predit_pourcent")
    private Double rendementPreditPourcent;

    @Column(name = "quantite_huile_recalculee_litres")
    private Double quantiteHuileRecalculeeLitres;

    @ManyToOne
    @JoinColumn(name = "execution_production_id", nullable = false)
    private ExecutionProduction executionProduction;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private String dateCreation;
}
