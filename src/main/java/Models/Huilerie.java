package Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "huilerie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Huilerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHuilerie;

    private String nom;
    private String localisation;
    private String type; // "artisanal", "semi-industriel", "industriel"
    private String certification;
    private Integer capaciteProduction;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    private Boolean active = true;

    @OneToMany(mappedBy = "huilerie", cascade = CascadeType.ALL)
    private List<Machine> machines;

    @OneToMany(mappedBy = "huilerie", cascade = CascadeType.ALL)
    private List<CampagneOlives> campagnesOlives;
}