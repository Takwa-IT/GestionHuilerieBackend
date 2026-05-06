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
        computeDeviationAndQuality();
    }

    @PreUpdate
    public void onUpdate() {
        this.dateModification = LocalDateTime.now();
        computeDeviationAndQuality();
    }

    public Double calculerDeviation(Double valeurReference) {
        if (valeurReference == null || valeurReference == 0.0 || valeurReelle == null) {
            return null;
        }
        return ((valeurReelle - valeurReference) / valeurReference) * 100.0;
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

    private void computeDeviationAndQuality() {
        if (deviation != null && qualiteDeviation != null) {
            return;
        }

        if (parametreEtape == null || valeurReelle == null) {
            return;
        }

        Double valeurReference = null;
        String referenceText = parametreEtape.getValeur();
        if (referenceText != null && !referenceText.isBlank()) {
            try {
                valeurReference = Double.valueOf(referenceText.trim().replace(',', '.'));
            } catch (NumberFormatException ignored) {
                valeurReference = null;
            }
        }

        if (valeurReference == null || valeurReference == 0.0) {
            return;
        }

        this.deviation = calculerDeviation(valeurReference);
        if (this.deviation == null) {
            return;
        }

        double valeurAbsolue = Math.abs(this.deviation);
        if (valeurAbsolue <= 10.0) {
            this.qualiteDeviation = "FAIBLE";
        } else if (valeurAbsolue <= 15.0) {
            this.qualiteDeviation = "MODÉRÉE";
        } else {
            this.qualiteDeviation = "IMPORTANTE";
        }
    }
}
