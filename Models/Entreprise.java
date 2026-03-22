package Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "entreprise")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEntreprise;

    private String nom;
    private String adresse;
    private String telephone;
    private String email;

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL)
    private List<Huilerie> huileries;
}