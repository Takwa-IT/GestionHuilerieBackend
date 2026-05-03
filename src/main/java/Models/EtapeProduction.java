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

import java.util.List;

@Entity
@Table(name = "etape_production")
@Getter
@Setter
@NoArgsConstructor
public class EtapeProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEtapeProduction;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private Integer ordre;

    private String description;

    @Column(name = "code_etape")
    private String codeEtape;

    @ManyToOne
    @JoinColumn(name = "guide_production_id", nullable = false)
    private GuideProduction guideProduction;

    @ManyToOne
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @OneToMany(mappedBy = "etapeProduction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParametreEtape> parametres;
}
