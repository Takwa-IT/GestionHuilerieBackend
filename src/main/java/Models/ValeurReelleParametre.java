package Models;

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
@Table(name = "valeur_reelle_parametre")
@Getter
@Setter
@NoArgsConstructor
public class ValeurReelleParametre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idValeurReelleParametre;

    private String valeurReelle;

    @ManyToOne
    @JoinColumn(name = "execution_production_id", nullable = false)
    private ExecutionProduction executionProduction;

    @ManyToOne
    @JoinColumn(name = "parametre_etape_id", nullable = false)
    private ParametreEtape parametreEtape;
}
