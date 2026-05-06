package Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fournisseur", uniqueConstraints = @UniqueConstraint(columnNames = "cin"))
@Getter
@Setter
@NoArgsConstructor
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFournisseur;

    private String nom;

    @Column(nullable = false, unique = true)
    private String cin;
}

