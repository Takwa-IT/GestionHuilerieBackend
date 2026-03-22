package Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "campagne_olives")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampagneOlives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCampagne;

    private Integer annee;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @ManyToOne
    @JoinColumn(name = "huilerie_id")
    private Huilerie huilerie;
}