package Models;

import Config.ReferenceUtils;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "campagne_olives")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampagneOlives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCampagne;

    @Column(unique = true)
    private String reference;

    @Column(nullable = false)
    private String annee;
    private String dateDebut;
    private String dateFin;

    @ManyToOne
    @JoinColumn(name = "huilerie_id")
    private Huilerie huilerie;

    @OneToMany(mappedBy = "campagne")
    private List<LotOlives> lots;

    public String getReference() {
        if (idCampagne != null) {
            return ReferenceUtils.format("CP", idCampagne);
        }
        return reference;
    }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idCampagne != null) {
            reference = ReferenceUtils.format("CP", idCampagne);
        }
    }
}
