package Models;

import Config.ReferenceUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class MatierePremiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_matiere_premiere")
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    private String nom;
    private String type;
    private String uniteMesure;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "huilerie_id", nullable = false)
    private Huilerie huilerie;

    @OneToMany(mappedBy = "matierePremiere")
    private List<LotOlives> lots;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        if (id != null) {
            return ReferenceUtils.format("MP", id);
        }
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUniteMesure() {
        return uniteMesure;
    }

    public void setUniteMesure(String uniteMesure) {
        this.uniteMesure = uniteMesure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Huilerie getHuilerie() {
        return huilerie;
    }

    public void setHuilerie(Huilerie huilerie) {
        this.huilerie = huilerie;
    }

    public List<LotOlives> getLots() {
        return lots;
    }

    public void setLots(List<LotOlives> lots) {
        this.lots = lots;
    }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && id != null) {
            reference = ReferenceUtils.format("MP", id);
        }
    }
}
