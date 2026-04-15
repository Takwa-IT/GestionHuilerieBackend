package Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "module", uniqueConstraints = {
        @UniqueConstraint(name = "uk_module_nom", columnNames = "nom")
})
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idModule;

    @Column(nullable = false, unique = true)
    private String nom;

    public Long getIdModule() {
        return idModule;
    }

    public void setIdModule(Long idModule) {
        this.idModule = idModule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}


