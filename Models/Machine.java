package Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "machine")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMachine;

    private String nomMachine;
    private String typeMachine;
    private String etatMachine;
    private Integer capacite;

    @ManyToOne
    @JoinColumn(name = "huilerie_id")
    private Huilerie huilerie;

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL)
    private List<Maintenance> maintenances;
}