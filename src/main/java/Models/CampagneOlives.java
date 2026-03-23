package Models;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "campagne_olives", uniqueConstraints = {
    @UniqueConstraint(name = "uk_campagne_annee", columnNames = "annee")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampagneOlives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCampagne;

    @Column(nullable = false, unique = true)
    private String annee;
    private String dateDebut;
    private String dateFin;

    @ManyToOne
    @JoinColumn(name = "huilerie_id")
    private Huilerie huilerie;

    @OneToMany(mappedBy = "campagne")
    private List<LotOlives> lots;
}
