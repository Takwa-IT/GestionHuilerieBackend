package Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "valeur_reelle_parametre")
@Getter
@Setter
@NoArgsConstructor
public class ValeurReelleParametre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idValeurReelleParametre;

    @Column(name = "valeur_reelle", nullable = false)
    private Double valeurReelle;

    @Column(name = "unite_mesure")
    private String uniteMesure;

    @Column(name = "valeur_estimee")
    private Double valeurEstimee;

    @Column(name = "deviation")
    private Double deviation;

    @Column(name = "qualite_deviation")
    private String qualiteDeviation;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToOne
    @JoinColumn(name = "execution_production_id", nullable = false)
    private ExecutionProduction executionProduction;

    @ManyToOne
    @JoinColumn(name = "parametre_etape_id", nullable = false)
    private ParametreEtape parametreEtape;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.dateCreation = now;
        this.dateModification = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    public Double calculerDeviation() {
        if (valeurEstimee == null || valeurEstimee == 0.0 || valeurReelle == null) {
            return null;
        }
        return ((valeurReelle - valeurEstimee) / valeurEstimee) * 100.0;
    }

    public String determinerQualiteDeviation(double toleranceDefault) {
        if (deviation == null) {
            return null;
        }
        double valeurAbsolue = Math.abs(deviation);
        if (valeurAbsolue <= toleranceDefault) {
            return "FAIBLE";
        }
        if (valeurAbsolue <= 15.0) {
            return "MODÉRÉE";
        }
        return "IMPORTANTE";
    }
}
