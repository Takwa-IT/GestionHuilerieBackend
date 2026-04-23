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
@Table(name = "parametre_etape")
@Getter
@Setter
@NoArgsConstructor
public class ParametreEtape {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idParametreEtape;

    @Column(nullable = false)
    private String nom;

    @Column(name = "code_parametre")
    private String codeParametre;

    private String uniteMesure;

    private String description;

    private String valeurEstime;

    private String valeurReelle;

    @ManyToOne
    @JoinColumn(name = "etape_production_id", nullable = false)
    private EtapeProduction etapeProduction;

    @ManyToOne
    @JoinColumn(name = "execution_production_id")
    private ExecutionProduction executionProduction;

    public String getValeur() {
        return valeurEstime;
    }

    public void setValeur(String valeur) {
        this.valeurEstime = valeur;
    }
}
