package Models;

import Config.ReferenceUtils;
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
@Table(name = "guide_production")
@Getter
@Setter
@NoArgsConstructor
public class GuideProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGuideProduction;

    @Column(unique = true)
    private String reference;

    @Column(nullable = false)
    private String nom;

    private String description;

    private String dateCreation;

    @ManyToOne
    @JoinColumn(name = "huilerie_id", nullable = false)
    private Huilerie huilerie;

    @OneToMany(mappedBy = "guideProduction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EtapeProduction> etapes;

    @OneToMany(mappedBy = "guideProduction")
    private List<ExecutionProduction> executions;

    public String getReference() {
        if (idGuideProduction != null) {
            return ReferenceUtils.format("GP", idGuideProduction);
        }
        return reference;
    }

    @jakarta.persistence.PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idGuideProduction != null) {
            reference = ReferenceUtils.format("GP", idGuideProduction);
        }
    }
}


