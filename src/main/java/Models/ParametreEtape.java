package Models;

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

import java.util.List;

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

    private String uniteMesure;

    private String description;

    private String valeur;

    @ManyToOne
    @JoinColumn(name = "etape_production_id", nullable = false)
    private EtapeProduction etapeProduction;

    @OneToMany(mappedBy = "parametreEtape")
    private List<ValeurReelleParametre> valeursReelles;
}


